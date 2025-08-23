package io.vertx.protobuf.codegen;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.vertx.grpc.plugin.schema.SchemaGenerator;
import io.vertx.protobuf.codegen.annotations.ProtoField;
import io.vertx.protobuf.codegen.annotations.ProtoMessage;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

@SupportedOptions({"codegen.generators"})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ProtoProcessor extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(ProtoMessage.class.getName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    Set<? extends Element> rootElements = roundEnv.getRootElements();

    DescriptorProtos.FileDescriptorProto.Builder proto = DescriptorProtos.FileDescriptorProto
      .newBuilder();

    if (annotations.isEmpty()) {
      return true;
    }

    rootElements
      .stream()
      .filter(elt -> elt.getAnnotation(ProtoMessage.class) != null)
      .forEach(msgElt -> {
        DescriptorProtos.DescriptorProto.Builder b = DescriptorProtos.DescriptorProto.newBuilder();
        b.setName(msgElt.getSimpleName().toString());

        msgElt
          .getEnclosedElements()
          .stream()
          .filter(elt -> elt.getKind() == ElementKind.METHOD && elt.getAnnotation(ProtoField.class) != null)
          .forEach(fieldElt -> {
            ProtoField protoField = fieldElt.getAnnotation(ProtoField.class);
            DescriptorProtos.FieldDescriptorProto f = DescriptorProtos.FieldDescriptorProto
              .newBuilder()
              .setNumber(protoField.number())
              .setName(protoField.name())
              .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING)
              .build();
            b.addField(f);
          });

        DescriptorProtos.DescriptorProto p = b.build();

        proto.addMessageType(p);


      });

    proto.setSyntax("proto3");

    proto.setPackage("test.proto");
    proto.setOptions(DescriptorProtos.FileOptions.newBuilder().setJavaPackage("test.proto").build());

    DescriptorProtos.FileDescriptorProto b = proto.build();

    Descriptors.FileDescriptor fd;
    try {
      fd = Descriptors.FileDescriptor.buildFrom(b, new Descriptors.FileDescriptor[0]);
    } catch (Descriptors.DescriptorValidationException e) {
      throw new RuntimeException(e);
    }

    SchemaGenerator schemaGenerator = new SchemaGenerator("test.proto");
    schemaGenerator.init(fd.getMessageTypes());

    try {
      Filer filer = processingEnv.getFiler();
      JavaFileObject messageLiteralsFile = filer.createSourceFile("test.proto.MessageLiteral");
      JavaFileObject fieldLiteralsFile = filer.createSourceFile("test.proto.FieldLiteral");
      try (Writer writer = messageLiteralsFile.openWriter()) {
        writer.write(schemaGenerator.generateMessageLiterals());
      }
      try (Writer writer = fieldLiteralsFile.openWriter()) {
        writer.write(schemaGenerator.generateFieldLiterals());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return true;
  }




}
