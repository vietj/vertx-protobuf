package io.vertx.grpc.plugin.schema;

import com.google.protobuf.Descriptors;
import io.vertx.grpc.plugin.GenWriter;
import io.vertx.grpc.plugin.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchemaGenerator {

  private final String javaPkgFqn;
  private final List<MessageTypeDeclaration> list;
  private final List<FieldDeclaration> list2;
  private final List<EnumTypeDeclaration> list3;

  public SchemaGenerator(String javaPkgFqn) {
    this.javaPkgFqn = javaPkgFqn;
    this.list = new ArrayList<>();
    this.list2 = new ArrayList<>();
    this.list3 = new ArrayList<>();
  }

  public void init(Collection<Descriptors.Descriptor> fileDesc, Collection<Descriptors.EnumDescriptor> enums) {
    enums.forEach(ed -> {
      EnumTypeDeclaration decl = new EnumTypeDeclaration(Utils.literalIdentifier(ed), ed.getName());
      list3.add(decl);
      ed.getValues().forEach(value -> {
        decl.identifierToNumber.put(value.getName(), value.getNumber());
      });
    });
    fileDesc.forEach(messageType -> {
      list.add(new MessageTypeDeclaration(Utils.literalIdentifier(messageType), messageType.getName(), Utils.javaTypeOf(messageType)));
      messageType.getFields().forEach(field -> {
        String identifier = Utils.literalIdentifier(field);
        String messageTypeRef = Utils.literalIdentifier(messageType);
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
            typeExpr = Utils.extractJavaPkgFqn(field.getEnumType().getFile()) + ".EnumLiteral." + Utils.literalIdentifier(field.getEnumType());
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
      });
    });

  }

  public String generateFieldLiterals() {
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

    writer.print("  ");
    for (Iterator<FieldDeclaration> it = list2.iterator(); it.hasNext(); ) {
      FieldDeclaration decl = it.next();
      writer.print(decl.identifier + "(" +
        decl.number + ", " +
        decl.map + ", " +
        decl.mapKey + ", " +
        decl.mapValue + ", " +
        decl.repeated + ", " +
        decl.packed + ", " +
        "\"" + decl.name + "\", " +
        "\"" + decl.jsonName + "\"" +
        ")"
      );
      if (it.hasNext()) {
        writer.println(",");
        writer.print("  ");
      }
    }
    writer.println(";");

    writer.println("  private MessageLiteral owner;");
    writer.println("  private io.vertx.protobuf.schema.Type type;");
    writer.println("  private final int number;");
    writer.println("  private final boolean map;");
    writer.println("  private final boolean mapKey;");
    writer.println("  private final boolean mapValue;");
    writer.println("  private final boolean repeated;");
    writer.println("  private final boolean packed;");
    writer.println("  private final String name;");
    writer.println("  private final String jsonName;");
    writer.println("  FieldLiteral(int number, boolean map, boolean mapKey, boolean mapValue, boolean repeated, boolean packed, String name, String jsonName) {");
    writer.println("    this.number = number;");
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
      writer.println("    FieldLiteral." + decl.messageTypeIdentifier + "_" + decl.name + ".owner = MessageLiteral." + decl.messageTypeIdentifier + ";");
      writer.println("    FieldLiteral." + decl.messageTypeIdentifier + "_" + decl.name + ".type = " + decl.typeExpr + ";");
    }
    writer.println("  }");
    writer.println("}");

    return writer.toString();
  }

  public String generateMessageLiterals() {
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
      "public enum MessageLiteral implements MessageType, java.util.function.Function<Object, io.vertx.protobuf.ProtoStream> {",
      "");

    writer.print("  ");
    for (Iterator<MessageTypeDeclaration> it = list.iterator(); it.hasNext(); ) {
      MessageTypeDeclaration decl = it.next();
      writer.print(decl.identifier + "(\"" + decl.name + "\")");
      if (it.hasNext()) {
        writer.println(",");
        writer.print("  ");
      }
    }
    writer.println(";");
    writer.println("  private static final java.util.List<java.util.function.Function<?, io.vertx.protobuf.ProtoStream>> streamFactories = new java.util.ArrayList<>();");
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
    writer.println("  public io.vertx.protobuf.ProtoStream apply(Object o) {");
    writer.println("    java.util.function.Function<Object, io.vertx.protobuf.ProtoStream> fn = (java.util.function.Function<Object, io.vertx.protobuf.ProtoStream>)streamFactories.get(ordinal());");
    writer.println("    return fn.apply(o);");
    writer.println("  }");
    writer.println("  static {");
    for (FieldDeclaration decl : list2) {
      writer.println("    MessageLiteral." + decl.messageTypeIdentifier + ".byNumber.put(" + decl.number + ", FieldLiteral." + decl.identifier + ");");
      writer.println("    MessageLiteral." + decl.messageTypeIdentifier + ".byJsonName.put(\"" + decl.jsonName + "\", FieldLiteral." + decl.identifier + ");");
      writer.println("    MessageLiteral." + decl.messageTypeIdentifier + ".byName.put(\"" + decl.name + "\", FieldLiteral." + decl.identifier + ");");
    }
    for (MessageTypeDeclaration decl : list) {
      writer.println("    java.util.function.Function<" + decl.className + ", io.vertx.protobuf.ProtoStream> fn_" + decl.name + " = " + javaPkgFqn + ".ProtoWriter::streamOf;");
      writer.print("      streamFactories.add(fn_" + decl.name + ");");
    }
    writer.println("  }");
    writer.println("}");

    return writer.toString();
  }

  public String generateEnumLiterals() {
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
      "import java.util.Map;",
      "import java.util.HashMap;",
      "import java.util.OptionalInt;",
      "",
      "public enum EnumLiteral implements EnumType {",
      "");

    writer.print("  ");
    for (Iterator<EnumTypeDeclaration> it = list3.iterator(); it.hasNext(); ) {
      EnumTypeDeclaration decl = it.next();
      writer.print(decl.identifier + "()");
      if (it.hasNext()) {
        writer.println(",");
        writer.print("  ");
      }
    }
    writer.println(";");

    writer.println("  private final Map<String, Integer> numberByName = new HashMap<>();");
    writer.println("  private final Map<Integer, String> nameByNumber = new HashMap<>();");
    writer.println("  EnumLiteral() {");
    writer.println("  }");
//    writer.println("  public Field field(int number) {");
//    writer.println("    return byNumber.get(number);");
//    writer.println("  }");
//    writer.println("  public Field fieldByJsonName(String name) {");
//    writer.println("    return byJsonName.get(name);");
//    writer.println("  }");
//    writer.println("  public Field fieldByName(String name) {");
//    writer.println("    return byName.get(name);");
//    writer.println("  }");
    writer.println("  public OptionalInt numberOf(String name) {");
    writer.println("    Integer number = numberByName.get(name);");
    writer.println("    return number == null ? OptionalInt.empty() : OptionalInt.of(number);");
    writer.println("  }");
    writer.println("  public String nameOf(int number) {");
    writer.println("    return nameByNumber.get(number);");
    writer.println("  }");
    writer.println("  static {");
    for (EnumTypeDeclaration decl : list3) {
      for (Map.Entry<String, Integer> entry : decl.identifierToNumber.entrySet()) {
        writer.println("    EnumLiteral." + decl.identifier + ".nameByNumber.put(" + entry.getValue() + ", \"" + entry.getKey() + "\");");
        writer.println("    EnumLiteral." + decl.identifier + ".numberByName.put(\"" + entry.getKey() + "\", " + entry.getValue() + ");");
      }
    }
//      writer.println("    MessageLiteral." + decl.messageName + ".byNumber.put(" + decl.number + ", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
//      writer.println("    MessageLiteral." + decl.messageName + ".byJsonName.put(\"" + decl.jsonName + "\", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
//      writer.println("    MessageLiteral." + decl.messageName + ".byName.put(\"" + decl.name + "\", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
//    }
    writer.println("  }");
    writer.println("}");

    return writer.toString();
  }
}
