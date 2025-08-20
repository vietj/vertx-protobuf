package io.vertx.grpc.plugin;

import com.google.common.base.Strings;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import io.vertx.protobuf.extension.VertxProto;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {

  static List<Descriptors.FieldDescriptor> actualFields(Descriptors.Descriptor descriptor) {
    List<Descriptors.FieldDescriptor> fields = new ArrayList<>(descriptor.getFields());
    for (Descriptors.OneofDescriptor oneOf : descriptor.getOneofs()) {
      for (Descriptors.FieldDescriptor fd : oneOf.getFields()) {
        if (!isOptional(fd)) {
          fields.remove(fd);
        }
      }
    }
    return fields;
  }

  static boolean isStruct(Descriptors.FieldDescriptor desc) {
    return desc.getType() == Descriptors.FieldDescriptor.Type.MESSAGE && desc.getMessageType().getFullName().equals("google.protobuf.Struct");
  }

  static boolean isStruct(Descriptors.Descriptor desc) {
    return desc.getFullName().equals("google.protobuf.Struct");
  }

  static boolean isDuration(Descriptors.Descriptor desc) {
    return desc.getFullName().equals("google.protobuf.Duration");
  }

  static boolean isTimestamp(Descriptors.Descriptor desc) {
    return desc.getFullName().equals("google.protobuf.Timestamp");
  }

  static boolean useJsonObject(Descriptors.FileDescriptor fd) {
    return fd.getOptions().getExtension(VertxProto.vertxJsonObject);
  }

  static boolean useDuration(Descriptors.FileDescriptor fd) {
    return fd.getOptions().getExtension(VertxProto.vertxDuration);
  }

  static boolean useTimestamp(Descriptors.FileDescriptor fd) {
    return fd.getOptions().getExtension(VertxProto.vertxTimestamp);
  }

  static boolean isOptional(Descriptors.FieldDescriptor field) {
    return field.toProto().hasProto3Optional();
  }

  static List<Descriptors.OneofDescriptor> oneOfs(Descriptors.Descriptor descriptor) {
    List<Descriptors.OneofDescriptor> oneOfs = new ArrayList<>();
    for (Descriptors.OneofDescriptor oneOf : descriptor.getOneofs()) {
      boolean optional = oneOf.getFields().stream().anyMatch(Utils::isOptional);
      if (!optional) {
        oneOfs.add(oneOf);
      }
    }
    return oneOfs;
  }

  static String setterOf(Descriptors.FieldDescriptor field) {
    return "set" + Character.toUpperCase(field.getJsonName().charAt(0)) + field.getJsonName().substring(1);
  }

  static String getterOf(Descriptors.FieldDescriptor field) {
    return "get" + Character.toUpperCase(field.getJsonName().charAt(0)) + field.getJsonName().substring(1);
  }

  static String setterOf(Descriptors.OneofDescriptor oneOf) {
    return "set" + nameOf(oneOf);
  }

  static String getterOf(Descriptors.OneofDescriptor oneOf) {
    return "get" + nameOf(oneOf);
  }

  static String schemaIdentifier(Descriptors.FieldDescriptor field) {
    return schemaIdentifier(field.getContainingType()) + "_" + field.getName().toUpperCase();
  }

  static String literalIdentifier(Descriptors.FieldDescriptor field) {
    return literalIdentifier(field.getContainingType()) + "_" + field.getName();
  }

  static String schemaIdentifier(Descriptors.Descriptor type) {
    return type.getName().toUpperCase();
  }

  static String literalIdentifier(Descriptors.Descriptor type) {
    return type.getName();
  }

  static boolean isMapKey(Descriptors.FieldDescriptor field) {
    return field.getContainingType().toProto().getOptions().getMapEntry() && field.getContainingType().getFields().get(0) == field;
  }

  static boolean isMapValue(Descriptors.FieldDescriptor field) {
    return field.getContainingType().toProto().getOptions().getMapEntry() && field.getContainingType().getFields().get(1) == field;
  }

  static Map<String, Descriptors.Descriptor> transitiveClosure(List<Descriptors.Descriptor> descriptors) {
    Map<String, Descriptors.Descriptor> all = new LinkedHashMap<>();
    descriptors.forEach(messageType -> {
      transitiveClosure(messageType, all);
    });
    return all;
  }

  private static void transitiveClosure(Descriptors.Descriptor descriptor, Map<String, Descriptors.Descriptor> result) {
    result.put(descriptor.getFullName(), descriptor);
    descriptor.getNestedTypes().forEach(nested -> transitiveClosure(nested, result));
  }

  public static String extractJavaPkgFqn(Descriptors.FileDescriptor proto) {
    return extractJavaPkgFqn(proto.toProto());
  }

  static String extractJavaPkgFqn(DescriptorProtos.FileDescriptorProto proto) {
    String googlePrefix = "com.google.protobuf";
    if (proto.getOptions().getJavaPackage().startsWith(googlePrefix) && (proto.getPackage().equals("google.protobuf") || proto.getPackage().equals("pb"))) {
      return "io.vertx.protobuf.well_known_types" + proto.getOptions().getJavaPackage().substring(googlePrefix.length());
    }
    DescriptorProtos.FileOptions options = proto.getOptions();
    String javaPackage = options.getJavaPackage();
    if (!Strings.isNullOrEmpty(javaPackage)) {
      return javaPackage;
    }
    return Strings.nullToEmpty(proto.getPackage());
  }

  static String nameOf(Descriptors.FieldDescriptor descriptor) {
    String name = descriptor.getJsonName();
    switch (name) {
      case "package":
        name = "package_";
        break;
    }
    return name;
  }

  static String nameOf(Descriptors.OneofDescriptor descriptor) {
    String name = descriptor.getName();
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  static String oneOfTypeName(Descriptors.FieldDescriptor descriptor) {
    String name = descriptor.getJsonName();
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  static String javaTypeOf(Descriptors.OneofDescriptor mt) {
    return javaTypeOf(mt.getContainingType()) + "." + nameOf(mt);
  }

  static String javaTypeOf(Descriptors.Descriptor mt) {
    String pkg = extractJavaPkgFqn(mt.getFile());
    return pkg + "." + simpleNameOf(mt);
  }

  static Descriptors.EnumValueDescriptor defaultEnumValue(Descriptors.EnumDescriptor desc) {
    return desc.getValues().stream().filter(vd -> vd.getIndex() == 0).findAny().get();
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
      case FLOAT:
        return "java.lang.Float";
      case DOUBLE:
        return "java.lang.Double";
      case INT32:
      case UINT32:
      case SINT32:
      case FIXED32:
      case SFIXED32:
        return "java.lang.Integer";
      case INT64:
      case UINT64:
      case SINT64:
      case FIXED64:
      case SFIXED64:
        return "java.lang.Long";
      case ENUM:
        pkg = extractJavaPkgFqn(field.getEnumType().getFile());
        return pkg + "." + simpleNameOf(field.getEnumType());
      case MESSAGE:
        Descriptors.Descriptor messageType = field.getMessageType();
        if (isStruct(messageType) && useJsonObject(field.getFile())) {
          return "io.vertx.core.json.JsonObject";
        } else if (isDuration(messageType) && useDuration(field.getFile())) {
          return Duration.class.getName();
        } else if (isTimestamp(messageType) && useTimestamp(field.getFile())) {
          return OffsetDateTime.class.getName();
        }
        pkg = extractJavaPkgFqn(messageType.getFile());
        return pkg + "." + simpleNameOf(messageType);
      default:
        return null;
    }
  }

  private static String simpleNameOf(Descriptors.Descriptor type) {
    Descriptors.Descriptor containing = type.getContainingType();
    return containing == null ? type.getName() : (simpleNameOf(containing) + "." + type.getName());
  }

  private static String simpleNameOf(Descriptors.EnumDescriptor descriptor) {
    Descriptors.Descriptor containing = descriptor.getContainingType();
    return containing == null ? descriptor.getName() : (simpleNameOf(containing) + "." + descriptor.getName());
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
