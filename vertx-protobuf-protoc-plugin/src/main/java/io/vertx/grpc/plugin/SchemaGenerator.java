package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class SchemaGenerator {

  private final String javaPkgFqn;
  private final List<Descriptors.Descriptor> fileDesc;

  public SchemaGenerator(String javaPkgFqn, List<Descriptors.Descriptor> fileDesc) {
    this.javaPkgFqn = javaPkgFqn;
    this.fileDesc = fileDesc;
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
    public final String messageTypeIdentifier;
    public final String messageName;
    public final int number;
    public final String typeExpr;

    public FieldDeclaration(String identifier, String name, String messageTypeIdentifier, int number, String messageName, String typeExpr) {
      this.identifier = identifier;
      this.name = name;
      this.messageTypeIdentifier = messageTypeIdentifier;
      this.messageName = messageName;
      this.number = number;
      this.typeExpr = typeExpr;
    }
  }

  PluginProtos.CodeGeneratorResponse.File generate() {
    List<MessageTypeDeclaration> list = new ArrayList<>();
    List<FieldDeclaration> list2 = new ArrayList<>();
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
            typeExpr = "new DefaultEnumType()";
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
            typeExpr = Utils.extractJavaPkgFqn(field.getMessageType().getFile()) + ".SchemaLiterals.MessageLiteral." + Utils.literalIdentifier(field.getMessageType());
            break;
          default:
            return;
        }
        list2.add(new FieldDeclaration(identifier, field.getName(), messageTypeRef, number, field.getContainingType().getName(), typeExpr));
      });
    });

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
      "public class SchemaLiterals {",
      "");

    writer.println("  public enum MessageLiteral implements MessageType {");
    for (Iterator<MessageTypeDeclaration> it  = list.iterator();it.hasNext();) {
      MessageTypeDeclaration decl = it.next();
      writer.print("    " + decl.name + "(\"" + decl.name +  "\")");
      if (it.hasNext()) {
        writer.println(",");
      } else {
        writer.println(";");
      }
    }
    writer.println("    private final java.util.Map<Integer, FieldLiteral> fields;");
    writer.println("    MessageLiteral(String name) {");
    writer.println("      this.fields = new java.util.HashMap<>();");
    writer.println("    }");
    writer.println("    public Field field(int number) {");
    writer.println("      return fields.get(number);");
    writer.println("    }");
    writer.println("    static {");
    for (FieldDeclaration decl : list2) {
      // Force init
      writer.println("      Object o = FieldLiteral." + decl.messageName + "_" + decl.name + ";");
      break;
    }
    writer.println("    }");
    writer.println("  }");

    writer.println("  public enum FieldLiteral implements Field {");
    for (Iterator<FieldDeclaration> it  = list2.iterator();it.hasNext();) {
      FieldDeclaration decl = it.next();
      writer.print("    " + decl.messageName + "_" + decl.name + "(MessageLiteral." + decl.messageName + ", " + decl.number + ", " + decl.typeExpr + ")");
      if (it.hasNext()) {
        writer.println(",");
      } else {
        writer.println(";");
      }
    }

    writer.println("    private final MessageLiteral owner;");
    writer.println("    private final int number;");
    writer.println("    private final io.vertx.protobuf.schema.Type type;");
    writer.println("    FieldLiteral(MessageLiteral owner, int number, io.vertx.protobuf.schema.Type type) {");
    writer.println("      this.owner = owner;");
    writer.println("      this.number = number;");
    writer.println("      this.type = type;");
    writer.println("    }");
    writer.println("    public MessageType owner() {");
    writer.println("      return owner;");
    writer.println("    }");
    writer.println("    public int number() {");
    writer.println("      return number;");
    writer.println("    }");
    writer.println("    public io.vertx.protobuf.schema.Type type() {");
    writer.println("      return type;");
    writer.println("    }");
    writer.println("    static {");
    for (FieldDeclaration decl : list2) {
      writer.println("      MessageLiteral." + decl.messageName + ".fields.put(" + decl.number + ", FieldLiteral." + decl.messageName + "_" + decl.name + ");");
    }
    writer.println("    }");
    writer.println("  }");

    writer.println(
      "",
      "}");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "SchemaLiterals"))
      .setContent(writer.toString())
      .build();
  }
}
