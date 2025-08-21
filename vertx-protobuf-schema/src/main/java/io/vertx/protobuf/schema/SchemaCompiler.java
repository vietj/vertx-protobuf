package io.vertx.protobuf.schema;

import com.google.protobuf.Descriptors;

import java.util.ArrayList;
import java.util.List;

/**
 * Compile {@link com.google.protobuf.Descriptors.FileDescriptor} to a {@link Schema}
 */
public class SchemaCompiler {

  private DefaultSchema schema = new DefaultSchema();

  public Schema schema() {
    return schema;
  }

  public List<DefaultMessageType> compile(Descriptors.FileDescriptor file) {
    List<DefaultMessageType> list = new ArrayList<>();
    for (Descriptors.Descriptor messageDesc : file.getMessageTypes()) {
      list.add(compile(messageDesc));
    }
    return list;
  }

  public DefaultMessageType compile(Descriptors.Descriptor messageDesc) {
    DefaultMessageType messageType = schema.peek(messageDesc.getName());
    if (messageType == null) {
      messageType = schema.of(messageDesc.getName());
      for (Descriptors.FieldDescriptor field : messageDesc.getFields()) {
        Type type;
        switch (field.getType()) {
          case STRING:
            type = ScalarType.STRING;
            break;
          case ENUM:
            type = new DefaultEnumType();
            break;
          case DOUBLE:
            type = ScalarType.DOUBLE;
            break;
          case BOOL:
            type = ScalarType.BOOL;
            break;
          case MESSAGE:
            type = compile(field.getMessageType());
            break;
          default:
            throw new UnsupportedOperationException("" + field.getType());
        }
        boolean isMapEntry = field.getContainingType().toProto().getOptions().getMapEntry();
        messageType.addField(builder -> {
          builder.type(type);
          builder.map(field.isMapField());
          builder.name(field.getName());
          builder.repeated(field.isRepeated());
          builder.mapKey(isMapEntry && field.getContainingType().getFields().get(0) == field);
          builder.mapValue(isMapEntry && field.getContainingType().getFields().get(1) == field);
          builder.number(field.getNumber());
          builder.packed(field.isPacked());
        });
      }
    }
    return messageType;
  }
}
