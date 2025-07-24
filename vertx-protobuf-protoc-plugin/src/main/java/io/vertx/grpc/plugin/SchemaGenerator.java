package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

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
    STGroup group = new STGroupFile("schema.stg");
    ST st = group.getInstanceOf("unit");
    String javaPkgFqn = Utils.extractJavaPkgFqn(file.toProto());
    st.add("pkg", javaPkgFqn);
    Map<String, Descriptors.Descriptor> all = Utils.transitiveClosure(file.getMessageTypes());
    all.values().forEach(messageType -> {
      st.add("message", new MessageTypeDeclaration(Utils.schemaLiteralOf(messageType), messageType.getName()));
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
        st.add("field", new FieldDeclaration(identifier, messageTypeRef, number, typeExpr));
      });
    });

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "SchemaLiterals"))
      .setContent(st.render())
      .build();
  }
}
