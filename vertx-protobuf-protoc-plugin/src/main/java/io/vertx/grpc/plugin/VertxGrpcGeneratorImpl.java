/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.grpc.plugin;

import com.google.protobuf.AnyProto;
import com.google.protobuf.ApiProto;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DurationProto;
import com.google.protobuf.EmptyProto;
import com.google.protobuf.FieldMaskProto;
import com.google.protobuf.JavaFeaturesProto;
import com.google.protobuf.SourceContextProto;
import com.google.protobuf.StructProto;
import com.google.protobuf.TimestampProto;
import com.google.protobuf.TypeProto;
import com.google.protobuf.WrappersProto;
import com.google.protobuf.compiler.PluginProtos;
import com.salesforce.jprotoc.Generator;
import com.salesforce.jprotoc.GeneratorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VertxGrpcGeneratorImpl extends Generator {

  public VertxGrpcGeneratorImpl() {
  }

  @Override
  protected List<PluginProtos.CodeGeneratorResponse.Feature> supportedFeatures() {
    return Collections.singletonList(PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL);
  }

  private static class Node {
    final DescriptorProtos.FileDescriptorProto fileDescProto;
    final List<Node> dependencies;
    Descriptors.FileDescriptor fileDesc;
    Node(DescriptorProtos.FileDescriptorProto fileDescProto) {
      this.fileDescProto = fileDescProto;
      this.dependencies = new ArrayList<>();
    }
    Descriptors.FileDescriptor build() throws Descriptors.DescriptorValidationException {
      if (fileDesc == null) {
        List<Descriptors.FileDescriptor> deps = new ArrayList<>();
        for (Node dep : dependencies) {
          deps.add(dep.build());
        }
        fileDesc = Descriptors.FileDescriptor.buildFrom(fileDescProto, deps.toArray(new Descriptors.FileDescriptor[0]));
      }
      return fileDesc;
    }
  }

  @Override
  public List<PluginProtos.CodeGeneratorResponse.File> generateFiles(PluginProtos.CodeGeneratorRequest request) throws GeneratorException {

    List<DescriptorProtos.FileDescriptorProto> protosToGenerate = request.getProtoFileList().stream()
      .filter(protoFile -> request.getFileToGenerateList().contains(protoFile.getName()))
      .collect(Collectors.toList());

    Map<String, Node> nodeMap = new LinkedHashMap<>();
    for (DescriptorProtos.FileDescriptorProto fileDescProto : protosToGenerate) {
      nodeMap.put(fileDescProto.getName(), new Node(fileDescProto));
    }
    Map<String, Node> wellKnownDependencies = new LinkedHashMap<>();
    for (Node node : nodeMap.values()) {
      for (String dependency : node.fileDescProto.getDependencyList()) {
        Node depNode = nodeMap.get(dependency);
        if (depNode == null) {
          switch (dependency) {
            case "google/protobuf/any.proto":
              depNode = new Node(AnyProto.getDescriptor().toProto());
              break;
            case "google/protobuf/api.proto":
              depNode = new Node(ApiProto.getDescriptor().toProto());
              break;
            case "google/protobuf/descriptor.proto":
              depNode = new Node(DescriptorProtos.getDescriptor().toProto());
              break;
            case "google/protobuf/duration.proto":
              depNode = new Node(DurationProto.getDescriptor().toProto());
              break;
            case "google/protobuf/empty.proto":
              depNode = new Node(EmptyProto.getDescriptor().toProto());
              break;
            case "google/protobuf/field_mask.proto":
              depNode = new Node(FieldMaskProto.getDescriptor().toProto());
              break;
            case "google/protobuf/java_features.proto":
              depNode = new Node(JavaFeaturesProto.getDescriptor().toProto());
              break;
            case "google/protobuf/source_context.proto":
              depNode = new Node(SourceContextProto.getDescriptor().toProto());
              break;
            case "google/protobuf/struct.proto":
              depNode = new Node(StructProto.getDescriptor().toProto());
              break;
            case "google/protobuf/timestamp.proto":
              depNode = new Node(TimestampProto.getDescriptor().toProto());
              break;
            case "google/protobuf/type.proto":
              depNode = new Node(TypeProto.getDescriptor().toProto());
              break;
            case "google/protobuf/wrappers.proto":
              depNode = new Node(WrappersProto.getDescriptor().toProto());
              break;
            default:
              throw new UnsupportedOperationException("Import not found " + dependency);
          }
          wellKnownDependencies.put(dependency, depNode);
        }
      }
    }
    nodeMap.putAll(wellKnownDependencies);
    nodeMap.values().forEach(node -> {
      for (String dependency : node.fileDescProto.getDependencyList()) {
        node.dependencies.add(nodeMap.get(dependency));
      }
    });

    List<PluginProtos.CodeGeneratorResponse.File> files = new ArrayList<>();


    Map<String, List<Descriptors.FileDescriptor>> byPkg = new LinkedHashMap<>();
    for (Node fileDescProto : nodeMap.values()) {

      Descriptors.FileDescriptor fileDesc;
      try {
        fileDesc = fileDescProto.build();
      } catch (Descriptors.DescriptorValidationException e) {
        GeneratorException ex = new GeneratorException(e.getMessage());
        ex.initCause(e);
        throw ex;
      }

      String key = Utils.extractJavaPkgFqn(fileDesc);
      byPkg.computeIfAbsent(key, k -> new ArrayList<>()).add(fileDesc);
    }

    byPkg.forEach((javaPkgFqn, v) -> {
      Map<String, Descriptors.Descriptor> messages = new LinkedHashMap<>();
      List<Descriptors.EnumDescriptor> enums = new ArrayList<>();
      for (Descriptors.FileDescriptor f : v) {
        Map<String, Descriptors.Descriptor> res = Utils.transitiveClosure(f.getMessageTypes());
        messages.putAll(res);
        enums.addAll(f.getEnumTypes());
        res
          .values()
          .stream()
          .flatMap(descriptor -> descriptor.getEnumTypes().stream())
          .forEach(enums::add);
      }

      files.add(new MessageBaseGenerator(javaPkgFqn).generate());
      files.addAll(new ElementGenerator(javaPkgFqn, new ArrayList<>(messages.values()), enums).generate());
//      files.add(new SchemaGenerator(javaPkgFqn, new ArrayList<>(messages.values())).generate());
      files.addAll(new SchemaGenerator(javaPkgFqn, new ArrayList<>(messages.values())).generate());
      files.add(new ProtoReaderGenerator(javaPkgFqn, new ArrayList<>(messages.values())).generate());
      files.add(new ProtoWriterGenerator(javaPkgFqn, new ArrayList<>(messages.values())).generate());
    });

    return files;
  }
}
