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
    public final String messageTypeIdentifier;
    public final int number;
    public final String typeExpr;

    public FieldDeclaration(String identifier, String messageTypeIdentifier, int number, String typeExpr) {
      this.identifier = identifier;
      this.messageTypeIdentifier = messageTypeIdentifier;
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
            typeExpr = Utils.extractJavaPkgFqn(field.getMessageType().getFile()) + ".SchemaLiterals." + Utils.schemaIdentifier(field.getMessageType());
            break;
          default:
            return;
        }
        list2.add(new FieldDeclaration(identifier, messageTypeRef, number, typeExpr));
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
      "",
      "  public static final DefaultSchema SCHEMA = new DefaultSchema();",
      "");

    writer.println("  public enum Messages {");
    for (Iterator<MessageTypeDeclaration> it  = list.iterator();it.hasNext();) {
      MessageTypeDeclaration decl = it.next();
      writer.print("    " + decl.identifier + "(\"" + decl.name +  "\")");
      if (it.hasNext()) {
        writer.println(",");
      } else {
        writer.println(";");
      }
    }
    writer.println("    public final DefaultMessageType type;");
    writer.println("    Messages(String name) {");
    writer.println("      this.type = SCHEMA.of(name);");
    writer.println("    }");
    writer.println("  }");

    writer.println("  public enum Fields {");
    for (Iterator<FieldDeclaration> it  = list2.iterator();it.hasNext();) {
      FieldDeclaration decl = it.next();
      writer.print("    " + decl.identifier + "(Messages." + decl.messageTypeIdentifier + ", " + decl.number + ", " + decl.typeExpr + ")");
      if (it.hasNext()) {
        writer.println(",");
      } else {
        writer.println(";");
      }
    }
    writer.println("    public final Messages owner;");
    writer.println("    public final int number;");
    writer.println("    public final io.vertx.protobuf.schema.Type type;");
    writer.println("    Fields(Messages owner, int number, io.vertx.protobuf.schema.Type type) {");
    writer.println("      this.owner = owner;");
    writer.println("      this.number = number;");
    writer.println("      this.type = type;");
    writer.println("    }");
    writer.println("  }");


    list.forEach(decl -> {
      writer.println("  public static final DefaultMessageType " + decl.identifier + " = SCHEMA.of(\"" + decl.name +  "\");");
    });

    writer.println();

    list2.forEach(decl -> {
      writer.println("  public static final Field " + decl.identifier + " = " + decl.messageTypeIdentifier + ".addField(" + decl.number + ", " + decl.typeExpr + ");");
    });

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
