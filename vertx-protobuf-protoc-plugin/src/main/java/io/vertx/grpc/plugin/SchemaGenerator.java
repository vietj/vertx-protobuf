package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class SchemaGenerator {

  private final Descriptors.FileDescriptor file;

  public SchemaGenerator(Descriptors.FileDescriptor file) {
    this.file = file;
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
    String javaPkgFqn = Utils.extractJavaPkgFqn(file.toProto());
    Map<String, Descriptors.Descriptor> all = Utils.transitiveClosure(file.getMessageTypes());
    List<MessageTypeDeclaration> list = new ArrayList<>();
    List<FieldDeclaration> list2 = new ArrayList<>();
    all.values().forEach(messageType -> {
      list.add(new MessageTypeDeclaration(Utils.schemaLiteralOf(messageType), messageType.getName()));
      messageType.getFields().forEach(field -> {
        String identifier = Utils.schemaLiteralOf(field);
        String messageTypeRef = Utils.schemaLiteralOf(messageType);
        int number = field.getNumber();
        String typeExpr;
        switch (field.getType()) {
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
          case MESSAGE:
            typeExpr = field.getMessageType().getFile().getOptions().getJavaPackage() + ".SchemaLiterals." + Utils.schemaLiteralOf(field.getMessageType());
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
