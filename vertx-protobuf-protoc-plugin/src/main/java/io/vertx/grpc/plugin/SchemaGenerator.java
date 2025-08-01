package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
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
    public final String messageTypeRef;
    public final int number;
    public final String typeExpr;

    public FieldDeclaration(String identifier, String messageTypeRef, int number, String typeExpr) {
      this.identifier = identifier;
      this.messageTypeRef = messageTypeRef;
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
            typeExpr = "new EnumType()";
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
      "import io.vertx.protobuf.schema.MessageType;",
      "import io.vertx.protobuf.schema.ScalarType;",
      "import io.vertx.protobuf.schema.EnumType;",
      "import io.vertx.protobuf.schema.Field;",
      "",
      "public class SchemaLiterals {",
      "",
      "  public static final Schema SCHEMA = new Schema();",
      "");

    list.forEach(decl -> {
      writer.println("  public static final MessageType " + decl.identifier + " = SCHEMA.of(\"" + decl.name +  "\");");
    });

    writer.println();

    list2.forEach(decl -> {
      writer.println("  public static final Field " + decl.identifier + " = " + decl.messageTypeRef + ".addField(" + decl.number + ", " + decl.typeExpr + ");");
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
