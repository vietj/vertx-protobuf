package io.vertx.protobuf.codegen;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.vertx.grpc.plugin.reader.ProtoReaderGenerator;
import io.vertx.grpc.plugin.schema.SchemaGenerator;
import io.vertx.protobuf.annotations.ProtoEnum;
import io.vertx.protobuf.annotations.ProtoField;
import io.vertx.protobuf.annotations.ProtoMessage;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ProtoProcessor extends AbstractProcessor {

  private Types typeUtils;
  private Elements elementUtils;

  private TypeMirror voidType;
  private TypeMirror javaLangLong;
  private TypeMirror javaLangString;
  private TypeMirror javaLangBoolean;

  Map<String, DescriptorProtos.FileDescriptorProto.Builder> protoMap = new HashMap<>();

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Set.of(ProtoMessage.class.getName(), ProtoEnum.class.getName());
  }

  private DescriptorProtos.FileDescriptorProto.Builder blah(String pkg) {
    DescriptorProtos.FileDescriptorProto.Builder bbb = protoMap.get(pkg);
    if (bbb == null) {
      bbb = DescriptorProtos.FileDescriptorProto.newBuilder();
      bbb.setSyntax("proto3");
      bbb.setPackage(pkg);
      bbb.setOptions(DescriptorProtos.FileOptions.newBuilder().setJavaPackage(pkg).build());
      protoMap.put(pkg, bbb);
    }
    return bbb;
  }

  private static String pkgOf(TypeElement elt) {
    Name qn = elt.getQualifiedName();
    return qn.subSequence(0, qn.length() - elt.getSimpleName().length() - 1).toString();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    Set<? extends Element> rootElements = roundEnv.getRootElements();

    if (annotations.isEmpty()) {
      // Generate ?
      return true;
    }


    // Process enums
    rootElements
      .stream()
      .filter(elt -> elt.getAnnotation(ProtoEnum.class) != null)
      .map(TypeElement.class::cast)
      .forEach(enumElt -> {
        DescriptorProtos.FileDescriptorProto.Builder fileDesc = blah(pkgOf(enumElt));
        DescriptorProtos.EnumDescriptorProto.Builder enumDesc = DescriptorProtos.EnumDescriptorProto.newBuilder();
        enumDesc.setName(enumElt.getSimpleName().toString());
        List<? extends Element> constantElts = enumElt
          .getEnclosedElements()
          .stream()
          .filter(elt -> elt.getKind() == ElementKind.ENUM_CONSTANT).collect(Collectors.toList());
        for (int i = 0;i < constantElts.size();i++) {
          DescriptorProtos.EnumValueDescriptorProto.Builder enumValueDesc = DescriptorProtos.EnumValueDescriptorProto
            .newBuilder()
            .setNumber(i)
            .setName(constantElts.get(i).getSimpleName().toString());
          enumDesc.addValue(enumValueDesc);
        }
        fileDesc.addEnumType(enumDesc);
      });

    rootElements
      .stream()
      .filter(elt -> elt.getAnnotation(ProtoMessage.class) != null)
      .map(TypeElement.class::cast)
      .forEach(msgElt -> {
        DescriptorProtos.DescriptorProto.Builder b = DescriptorProtos.DescriptorProto.newBuilder();
        b.setName(msgElt.getSimpleName().toString());

        String pkg = pkgOf(msgElt);
        DescriptorProtos.FileDescriptorProto.Builder bbb = blah(pkg);

        List<ExecutableElement> list = msgElt
          .getEnclosedElements()
          .stream()
          .filter(elt ->
            elt.getKind() == ElementKind.METHOD && elt.getAnnotation(ProtoField.class) != null && accept(elt))
          .map(ExecutableElement.class::cast)
          .collect(Collectors.toList());


        for (ExecutableElement fieldElt : list) {
          ProtoField protoField = fieldElt.getAnnotation(ProtoField.class);
          TypeMirror typeMirror = extractMethodType(fieldElt);
          DescriptorProtos.FieldDescriptorProto.Type type = protoTypeOf(typeMirror);
          DescriptorProtos.FieldDescriptorProto.Builder f = DescriptorProtos.FieldDescriptorProto
            .newBuilder()
            .setNumber(protoField.number())
            .setName(protoField.name())
            .setType(type);
          if (type == DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM) {
            DeclaredType dt = (DeclaredType) typeMirror;
            TypeElement element = (TypeElement) dt.asElement();
            String p = pkgOf(element);
            String name = element.getSimpleName().toString();
            if (p.equals(pkg)) {
              Optional<DescriptorProtos.EnumDescriptorProto.Builder> found = bbb.getEnumTypeBuilderList().stream()
                .filter(blah -> blah.getName().equals(name))
                .findFirst();
              if (found.isPresent()) {
                DescriptorProtos.EnumDescriptorProto.Builder b2 = found.get();
                f.setTypeName(b2.getName());
              } else {
                throw new UnsupportedOperationException("Not yet implemented");
              }
             } else {
              throw new UnsupportedOperationException("Not yet implemented");
            }
          }
          b.addField(f);
        }

        DescriptorProtos.DescriptorProto p = b.build();
        bbb.addMessageType(p);
      });

    List<Descriptors.FileDescriptor> protos = new ArrayList<>();
    for (DescriptorProtos.FileDescriptorProto.Builder b : protoMap.values()) {
      try {
        protos.add(Descriptors.FileDescriptor.buildFrom(b.build(), new Descriptors.FileDescriptor[0]));
      } catch (Descriptors.DescriptorValidationException e) {
        throw new RuntimeException(e);
      }
    }

    for (Descriptors.FileDescriptor p : protos) {
      String javaPkg = p.getOptions().getJavaPackage();
      SchemaGenerator schemaGenerator = new SchemaGenerator(javaPkg);
      schemaGenerator.init(p.getMessageTypes(), p.getEnumTypes());
      ProtoReaderGenerator protoReaderGenerator = new ProtoReaderGenerator(javaPkg, true, p.getMessageTypes());
      try {
        Filer filer = processingEnv.getFiler();
        JavaFileObject messageLiteralFile = filer.createSourceFile(javaPkg + ".MessageLiteral");
        JavaFileObject fieldLiteralFile = filer.createSourceFile(javaPkg + ".FieldLiteral");
        JavaFileObject enumLiteralFile = filer.createSourceFile(javaPkg + ".EnumLiteral");
        JavaFileObject protoReaderFile = filer.createSourceFile(javaPkg + ".ProtoReader");
        try (Writer writer = messageLiteralFile.openWriter()) {
          writer.write(schemaGenerator.generateMessageLiterals());
        }
        try (Writer writer = fieldLiteralFile.openWriter()) {
          writer.write(schemaGenerator.generateFieldLiterals());
        }
        try (Writer writer = enumLiteralFile.openWriter()) {
          writer.write(schemaGenerator.generateEnumLiterals());
        }
        try (Writer writer = protoReaderFile.openWriter()) {
          writer.write(protoReaderGenerator.generate());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }


    return true;
  }

  public boolean accept(Element element) {
    Set<Modifier> modifiers = element.getModifiers();
    return modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC);
  }

  private TypeMirror extractMethodType(ExecutableElement elt) {
    String name = elt.getSimpleName().toString();
    if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3)) && elt.getParameters().isEmpty() && elt.getReturnType().getKind() != TypeKind.VOID) {
      return elt.getReturnType();
    } else if (name.startsWith("is") && name.length() > 2 && Character.isUpperCase(name.charAt(2)) &&
      elt.getParameters().isEmpty() && elt.getReturnType().getKind() != TypeKind.VOID) {
      return elt.getReturnType();
    } else {
      if (name.startsWith("set") && name.length() > 3 && Character.isUpperCase(name.charAt(3)) && elt.getParameters().size() == 1 &&
        typeUtils.isSameType(elt.getReturnType(), voidType)) {
        return elt.getParameters().get(0).asType();
      } else {
        return null;
      }
    }
  }

  private DescriptorProtos.FieldDescriptorProto.Type protoTypeOf(TypeMirror type) {
    switch (type.getKind()) {
      case DECLARED:
        DeclaredType declaredType = (DeclaredType) type;
        Element typeElt = declaredType.asElement();
        switch (typeElt.getKind()) {
          case CLASS:
            if (typeUtils.isSameType(javaLangString, type)) {
              return DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING;
            } else if (typeUtils.isSameType(javaLangLong, type)) {
              return DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64;
            } else if (typeUtils.isSameType(javaLangBoolean, type)) {
              return DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL;
            }
            break;
          case ENUM:
            return DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM;
        }
        break;
    }
    return null;
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
    super.init(processingEnv);
    voidType = elementUtils.getTypeElement("java.lang.Void").asType();
    javaLangLong = elementUtils.getTypeElement("java.lang.Long").asType();
    javaLangString = elementUtils.getTypeElement("java.lang.String").asType();
    javaLangBoolean = elementUtils.getTypeElement("java.lang.Boolean").asType();
  }
}
