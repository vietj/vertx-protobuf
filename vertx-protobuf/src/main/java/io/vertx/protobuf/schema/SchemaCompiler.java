package io.vertx.protobuf.schema;

import com.google.protobuf.Descriptors;

import java.util.List;

public class SchemaCompiler {

  private DefaultSchema schema = new DefaultSchema();

  public MessageType compile(Descriptors.Descriptor descriptor) {
    return compile(schema, descriptor);
  }

  public Schema schema() {
    return schema;
  }

  private static MessageType compile(DefaultSchema schema, Descriptors.Descriptor descriptor) {

    DefaultMessageType mt = schema.peek(descriptor.getName());
    if (mt != null) {
      return mt;
    }

    mt = schema.of(descriptor.getName());

    List<Descriptors.FieldDescriptor> fields = descriptor.getFields();
    for (Descriptors.FieldDescriptor field : fields) {
      Descriptors.FieldDescriptor.Type type = field.getType();
      switch (type) {
        case STRING:
          mt.addField(field.getNumber(), ScalarType.STRING);
          break;
        case ENUM:
          mt.addField(field.getNumber(), new DefaultEnumType());
          break;
        case DOUBLE:
          mt.addField(field.getNumber(), ScalarType.DOUBLE);
          break;
        case BOOL:
          mt.addField(field.getNumber(), ScalarType.BOOL);
          break;
        case MESSAGE:
          MessageType nested = compile(schema, field.getMessageType());
          mt.addField(field.getNumber(), nested);
          break;
        default:
          throw new UnsupportedOperationException("" + type);
      }
    }

    return mt;
  }

}
