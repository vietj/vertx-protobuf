package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class SchemaGenerator {

  private final String javaPkgFqn;
  private final List<Descriptors.Descriptor> fileDesc;

  private List<MessageTypeDeclaration> list = new ArrayList<>();
  private List<FieldDeclaration> list2 = new ArrayList<>();

  public SchemaGenerator(String javaPkgFqn, List<Descriptors.Descriptor> fileDesc) {
    this.javaPkgFqn = javaPkgFqn;
    this.fileDesc = fileDesc;



  }

  private void init() {
    Map<Descriptors.EnumDescriptor, EnumTypeDeclaration> list3 = new LinkedHashMap<>();
    fileDesc.forEach(messageType -> {
      list.add(new MessageTypeDeclaration(Utils.schemaIdentifier(messageType), messageType.getName()));
      messageType.getFields().forEach(field -> {
        String identifier = Utils.schemaIdentifier(field);
        String messageTypeRef = Utils.schemaIdentifier(messageType);
        int number = field.getNumber();
        String typeExpr;
        switch (field.getType()) {
          case FLOAT:
            typeExpr = "ScalarType.FLOAT";
            break;
          case DOUBLE:
            typeExpr = "ScalarType.DOUBLE";
            break;
          case BOOL:
            typeExpr = "ScalarType.BOOL";
            break;
          case STRING:
            typeExpr = "ScalarType.STRING";
            break;
          case ENUM:
            typeExpr = Utils.javaTypeOfInternal(field) + ".TYPE";
            break;
          case BYTES:
            typeExpr = "ScalarType.BYTES";
            break;
          case INT32:
            typeExpr = "ScalarType.INT32";
            break;
          case INT64:
            typeExpr = "ScalarType.INT64";
            break;
          case UINT32:
            typeExpr = "ScalarType.UINT32";
            break;
          case UINT64:
            typeExpr = "ScalarType.UINT64";
            break;
          case SINT32:
            typeExpr = "ScalarType.SINT32";
            break;
          case SINT64:
            typeExpr = "ScalarType.SINT64";
            break;
          case FIXED32:
            typeExpr = "ScalarType.FIXED32";
            break;
          case FIXED64:
            typeExpr = "ScalarType.FIXED64";
            break;
          case SFIXED32:
            typeExpr = "ScalarType.SFIXED32";
            break;
          case SFIXED64:
            typeExpr = "ScalarType.SFIXED64";
            break;
          case MESSAGE:
            typeExpr = Utils.extractJavaPkgFqn(field.getMessageType().getFile()) + ".MessageLiteral." + Utils.literalIdentifier(field.getMessageType());
            break;
          default:
            return;
        }
        list2.add(new FieldDeclaration(identifier, field.getName(), field.isMapField(), Utils.isMapKey(field), Utils.isMapValue(field), field.isRepeated(), field.isPacked(), field.getJsonName(), messageTypeRef, number, field.getContainingType().getName(), typeExpr));
        if (field.getType() == Descriptors.FieldDescriptor.Type.ENUM) {
          Descriptors.EnumDescriptor enumType = field.getEnumType();
          if (!list3.containsKey(enumType)) {
            EnumTypeDeclaration decl = new EnumTypeDeclaration(enumType.getName());
            list3.put(enumType, decl);
            enumType.getValues().forEach(value -> {
              decl.numberToIdentifier.put(value.getNumber(), value.getName());
            });
          }
        }
      });
    });
  }

  public static class EnumTypeDeclaration {

    public final String name;
    public final Map<Integer, String> numberToIdentifier;

    public EnumTypeDeclaration(String name) {
      this.name = name;
      this.numberToIdentifier = new LinkedHashMap<>();
    }
  }

  public static class MessageTypeDeclaration {

    public final String identifier;
    public final String name;

    public MessageTypeDeclaration(String identifier, String name) {
      this.identifier = identifier;
      this.name = name;
    }
  }

  public static class FieldDeclaration {

    public final String identifier;
    public final String name;
    public final String jsonName;
    public final boolean map;
    public final boolean mapKey;
    public final boolean mapValue;
    public final boolean repeated;
    public final boolean packed;
    public final String messageTypeIdentifier;
    public final String messageName;
    public final int number;
    public final String typeExpr;

    public FieldDeclaration(String identifier, String name, boolean map, boolean mapKey, boolean mapValue, boolean repeated, boolean packed, String jsonName, String messageTypeIdentifier, int number, String messageName, String typeExpr) {
      this.identifier = identifier;
      this.name = name;
      this.jsonName = jsonName;
      this.messageTypeIdentifier = messageTypeIdentifier;
      this.messageName = messageName;
      this.map = map;
      this.mapKey = mapKey;
      this.mapValue = mapValue;
      this.repeated = repeated;
      this.packed = packed;
      this.number = number;
      this.typeExpr = typeExpr;
    }
  }

  List<PluginProtos.CodeGeneratorResponse.File> generate() {
    init();
    return Arrays.asList(generateFieldLiterals(), generateMessageLiterals());
  }

  PluginProtos.CodeGeneratorResponse.File generateFieldLiterals() {
    GenWriter writer = new GenWriter();

    writer.println(
      "package " + javaPkgFqn + ";",
      "",
      "import io.vertx.protobuf.schema.Schema;",
      "import io.vertx.protobuf.schema.DefaultSchema;",
      "import io.vertx.protobuf.schema.MessageType;",
      "import io.vertx.protobuf.schema.DefaultMessageType;",
      "import io.vertx.protobuf.schema.ScalarType;",
      "import io.vertx.protobuf.schema.EnumType;",
      "import io.vertx.protobuf.schema.DefaultEnumType;",
      "import io.vertx.protobuf.schema.Field;",
      "",
      "public enum FieldLiteral implements Field {",
      "");

    for (Iterator<FieldDeclaration> it  = list2.iterator();it.hasNext();) {
      FieldDeclaration decl = it.next();
      writer.print("  " + decl.messageName + "_" + decl.name + "(MessageLiteral." + decl.messageName + ", "
              + decl.number + ", " + decl.typeExpr + ", " + decl.map + ", " + decl.mapKey + ", " + decl.mapValue
        + ", " +decl.repeated + ", " +decl.packed + ", \"" + decl.name + "\", \"" + decl.jsonName + "\")");
      if (it.hasNext()) {
        writer.println(",");
      } else {
        writer.println(";");
      }
    }

    writer.println("  private final MessageLiteral owner;");
    writer.println("  private final int number;");
    writer.println("  private final io.vertx.protobuf.schema.Type type;");
    writer.println("  private final boolean map;");
    writer.println("  private final boolean mapKey;");
    writer.println("  private final boolean mapValue;");
    writer.println("  private final boolean repeated;");
    writer.println("  private final boolean packed;");
    writer.println("  private final String name;");
    writer.println("  private final String jsonName;");
    writer.println("  FieldLiteral(MessageLiteral owner, int number, io.vertx.protobuf.schema.Type type, boolean map, boolean mapKey, boolean mapValue, boolean repeated, boolean packed, String name, String jsonName) {");
    writer.println("    this.owner = owner;");
    writer.println("    this.number = number;");
    writer.println("    this.type = type;");
    writer.println("    this.map = map;");
    writer.println("    this.mapKey = mapKey;");
    writer.println("    this.mapValue = mapValue;");
    writer.println("    this.repeated = repeated;");
    writer.println("    this.packed = packed;");
    writer.println("    this.name = name;");
    writer.println("    this.jsonName = jsonName;");
    writer.println("  }");
    writer.println("  public MessageType owner() {");
    writer.println("    return owner;");
    writer.println("  }");
    writer.println("  public int number() {");
    writer.println("    return number;");
    writer.println("  }");
    writer.println("  public String jsonName() {");
    writer.println("    return jsonName;");
    writer.println("  }");
    writer.println("  public boolean isMap() {");
    writer.println("    return map;");
    writer.println("  }");
    writer.println("  public boolean isMapKey() {");
    writer.println("    return mapKey;");
    writer.println("  }");
    writer.println("  public boolean isMapValue() {");
    writer.println("    return mapValue;");
    writer.println("  }");
    writer.println("  public boolean isRepeated() {");
    writer.println("    return repeated;");
    writer.println("  }");
    writer.println("  public boolean isPacked() {");
    writer.println("    return packed;");
    writer.println("  }");
    writer.println("  public io.vertx.protobuf.schema.Type type() {");
    writer.println("    return type;");
    writer.println("  }");
    writer.println("  static {");
    for (FieldDeclaration decl : list2) {
      writer.println("    MessageLiteral." + decl.messageName + ".byNumber.put(" + decl.number + ", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
      writer.println("    MessageLiteral." + decl.messageName + ".byJsonName.put(\"" + decl.jsonName + "\", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
      writer.println("    MessageLiteral." + decl.messageName + ".byName.put(\"" + decl.name + "\", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
    }
    writer.println("  }");
    writer.println("}");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "FieldLiteral"))
      .setContent(writer.toString())
      .build();
  }

  PluginProtos.CodeGeneratorResponse.File generateMessageLiterals() {


    GenWriter writer = new GenWriter();

    writer.println(
      "package " + javaPkgFqn + ";",
      "",
      "import io.vertx.protobuf.schema.Schema;",
      "import io.vertx.protobuf.schema.DefaultSchema;",
      "import io.vertx.protobuf.schema.MessageType;",
      "import io.vertx.protobuf.schema.DefaultMessageType;",
      "import io.vertx.protobuf.schema.ScalarType;",
      "import io.vertx.protobuf.schema.EnumType;",
      "import io.vertx.protobuf.schema.DefaultEnumType;",
      "import io.vertx.protobuf.schema.Field;",
      "",
      "public enum MessageLiteral implements MessageType {",
      "");

    for (Iterator<MessageTypeDeclaration> it  = list.iterator();it.hasNext();) {
      MessageTypeDeclaration decl = it.next();
      writer.print("    " + decl.name + "(\"" + decl.name +  "\")");
      if (it.hasNext()) {
        writer.println(",");
      } else {
        writer.println(";");
      }
    }
    writer.println("  final java.util.Map<Integer, FieldLiteral> byNumber;");
    writer.println("  final java.util.Map<String, FieldLiteral> byJsonName;");
    writer.println("  final java.util.Map<String, FieldLiteral> byName;");
    writer.println("  MessageLiteral(String name) {");
    writer.println("    this.byNumber = new java.util.HashMap<>();");
    writer.println("    this.byJsonName = new java.util.HashMap<>();");
    writer.println("    this.byName = new java.util.HashMap<>();");
    writer.println("  }");
    writer.println("  public Field field(int number) {");
    writer.println("    return byNumber.get(number);");
    writer.println("  }");
    writer.println("  public Field fieldByJsonName(String name) {");
    writer.println("    return byJsonName.get(name);");
    writer.println("  }");
    writer.println("  public Field fieldByName(String name) {");
    writer.println("    return byName.get(name);");
    writer.println("  }");
    writer.println("  static {");
    for (FieldDeclaration decl : list2) {
      // Force init
      writer.println("    Object o = FieldLiteral." + decl.messageName + "_" + decl.name + ";");
      break;
    }
    writer.println("  }");
    writer.println("}");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "MessageLiteral"))
      .setContent(writer.toString())
      .build();
  }
}
