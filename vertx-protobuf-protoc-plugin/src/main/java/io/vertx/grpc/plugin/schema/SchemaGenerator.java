package io.vertx.grpc.plugin.schema;

import io.vertx.grpc.plugin.GenWriter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SchemaGenerator {

  private final String javaPkgFqn;
  private final List<MessageTypeDeclaration> list;
  private final List<FieldDeclaration> list2;

  public SchemaGenerator(String javaPkgFqn, List<MessageTypeDeclaration> list, List<FieldDeclaration> list2) {
    this.javaPkgFqn = javaPkgFqn;
    this.list = list;
    this.list2 = list2;
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

    for (Iterator<FieldDeclaration> it = list2.iterator(); it.hasNext(); ) {
      FieldDeclaration decl = it.next();
      writer.print("  " + decl.messageName + "_" + decl.name + "(" +
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
      } else {
        writer.println(";");
      }
    }

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
      writer.println("    FieldLiteral." + decl.messageName + "_" + decl.name + ".owner = MessageLiteral." + decl.messageName + ";");
      writer.println("    FieldLiteral." + decl.messageName + "_" + decl.name + ".type = " + decl.typeExpr + ";");
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
      "public enum MessageLiteral implements MessageType {",
      "");

    for (Iterator<MessageTypeDeclaration> it = list.iterator(); it.hasNext(); ) {
      MessageTypeDeclaration decl = it.next();
      writer.print("    " + decl.name + "(\"" + decl.name + "\")");
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
      writer.println("    MessageLiteral." + decl.messageName + ".byNumber.put(" + decl.number + ", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
      writer.println("    MessageLiteral." + decl.messageName + ".byJsonName.put(\"" + decl.jsonName + "\", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
      writer.println("    MessageLiteral." + decl.messageName + ".byName.put(\"" + decl.name + "\", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
    }
    writer.println("  }");
    writer.println("}");

    return writer.toString();
  }
}
