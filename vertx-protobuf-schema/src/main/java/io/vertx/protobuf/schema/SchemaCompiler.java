package io.vertx.protobuf.schema;

import com.google.protobuf.Descriptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compile {@link com.google.protobuf.Descriptors.FileDescriptor} to a {@link Schema}
 */
public class SchemaCompiler {

  private Map<Descriptors.Descriptor, DefaultMessageType> typeMap = new LinkedHashMap();
  private Map<Descriptors.EnumDescriptor, DefaultEnumType> enumMap = new LinkedHashMap<>();

  public List<DefaultMessageType> compile(Descriptors.FileDescriptor file) {
    List<DefaultMessageType> list = new ArrayList<>();
    for (Descriptors.Descriptor messageDesc : file.getMessageTypes()) {
      list.add(compile(messageDesc));
    }
    return list;
  }

  public DefaultEnumType compile(Descriptors.EnumDescriptor enumDesc) {
    DefaultEnumType enumType = enumMap.get(enumDesc);
    if (enumType == null) {
      enumType = new DefaultEnumType(enumDesc.getName());
      enumMap.put(enumDesc, enumType);
      for (Descriptors.EnumValueDescriptor enumValueDesc : enumDesc.getValues()) {
        enumType.addValue(enumValueDesc.getNumber(), enumValueDesc.getName());
      }
    }
    return enumType;
  }

  public DefaultField compile(Descriptors.FieldDescriptor fieldDesc) {
    DefaultMessageType type = compile(fieldDesc.getContainingType());
    return type.field(fieldDesc.getNumber());
  }

  public DefaultMessageType compile(Descriptors.Descriptor typeDesc) {
    DefaultMessageType messageType = typeMap.get(typeDesc);
    if (messageType == null) {
      messageType = new DefaultMessageType(typeDesc.getName());
      typeMap.put(typeDesc, messageType);
      for (Descriptors.FieldDescriptor field : typeDesc.getFields()) {
        Type type;
        switch (field.getType()) {
          case INT32:
            type = ScalarType.INT32;
            break;
          case STRING:
            type = ScalarType.STRING;
            break;
          case ENUM:
            type = compile(field.getEnumType());
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
          builder.optional(field.hasPresence());
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
