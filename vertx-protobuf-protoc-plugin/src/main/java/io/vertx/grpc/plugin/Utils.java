package io.vertx.grpc.plugin;

import com.google.common.base.Strings;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {

  static String setterOf(Descriptors.FieldDescriptor field) {
    return "set" + Character.toUpperCase(field.getJsonName().charAt(0)) + field.getJsonName().substring(1);
  }

  static String getterOf(Descriptors.FieldDescriptor field) {
    return "get" + Character.toUpperCase(field.getJsonName().charAt(0)) + field.getJsonName().substring(1);
  }

  static String schemaLiteralOf(Descriptors.FieldDescriptor field) {
    return schemaLiteralOf(field.getContainingType()) + "_" + field.getName().toUpperCase();
  }

  static String schemaLiteralOf(Descriptors.Descriptor field) {
    return field.getName().toUpperCase();
  }

  static Map<String, Descriptors.Descriptor> transitiveClosure(List<Descriptors.Descriptor> descriptors) {
    Map<String, Descriptors.Descriptor> all = new LinkedHashMap<>();
    descriptors.forEach(messageType -> {
      transitiveClosure(messageType, all);
    });
    return all;
  }

  private static void transitiveClosure(Descriptors.Descriptor descriptor, Map<String, Descriptors.Descriptor> result) {
    result.put(descriptor.getName(), descriptor);
    descriptor.getNestedTypes().forEach(nested -> transitiveClosure(nested, result));
  }

  private static String extractJavaPkgFqn(Descriptors.FileDescriptor proto) {
    DescriptorProtos.FileOptions options = proto.getOptions();
    String javaPackage = options.getJavaPackage();
    if (!Strings.isNullOrEmpty(javaPackage)) {
      return javaPackage;
    }
    return Strings.nullToEmpty(proto.getPackage());
  }

  static String extractJavaPkgFqn(DescriptorProtos.FileDescriptorProto proto) {
    DescriptorProtos.FileOptions options = proto.getOptions();
    String javaPackage = options.getJavaPackage();
    if (!Strings.isNullOrEmpty(javaPackage)) {
      return javaPackage;
    }
    return Strings.nullToEmpty(proto.getPackage());
  }

  static String javaTypeOf(Descriptors.Descriptor mt) {
    String pkg = extractJavaPkgFqn(mt.getFile());
    return pkg + "." + mt.getName();
  }

  static String javaTypeOf(Descriptors.FieldDescriptor field) {
    if (field.isMapField()) {
      String keyType = javaTypeOf(field.getMessageType().getFields().get(0));
      String valueType = javaTypeOf(field.getMessageType().getFields().get(1));
      return "java.util.Map<" + keyType + ", " + valueType + ">";
    } else {
      String javaType = javaTypeOfInternal(field);
      if (javaType != null && field.isRepeated()) {
        javaType = "java.util.List<" + javaType + ">";
      }
      return javaType;
    }
  }

  static String javaTypeOfInternal(Descriptors.FieldDescriptor field) {
    String pkg;
    switch (field.getType()) {
      case BYTES:
        return "io.vertx.core.buffer.Buffer";
      case BOOL:
        return "java.lang.Boolean";
      case STRING:
        return "java.lang.String";
      case DOUBLE:
        return "java.lang.Double";
      case INT32:
      case UINT32:
      case SINT32:
        return "java.lang.Integer";
      case INT64:
      case UINT64:
      case SINT64:
        return "java.lang.Long";
      case ENUM:
        pkg = extractJavaPkgFqn(field.getEnumType().getFile());
        return pkg + "." + field.getEnumType().getName();
      case MESSAGE:
        pkg = extractJavaPkgFqn(field.getMessageType().getFile());
        return pkg + "." + field.getMessageType().getName();
      default:
        return null;
    }
  }

  static String absoluteFileName(String javaPkgFqn, Descriptors.GenericDescriptor messageType) {
    return absoluteFileName(javaPkgFqn, messageType.getName());
  }

  static String absoluteFileName(String javaPkgFqn, String simpleName) {
    String dir = javaPkgFqn.replace('.', '/');
    if (dir.isEmpty()) {
      return simpleName + ".java";
    } else {
      return dir + "/" + simpleName + ".java";
    }
  }

  static PluginProtos.CodeGeneratorResponse.File buildFile(String javaPkgFqn, Descriptors.GenericDescriptor messageType, String content) {
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(absoluteFileName(javaPkgFqn, messageType))
      .setContent(content)
      .build();
  }
}
