package io.vertx.tests.protobuf;

import io.vertx.protobuf.schema.EnumType;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.ScalarType;
import io.vertx.protobuf.schema.Schema;
import org.junit.Test;

public class DescriptorTest {

  @Test
  public void testSome() {

    Schema schema = new Schema();

    MessageType field = schema.of("entry");
    MessageType struct = schema.of("Struct");
    struct.addField(1, field);

    MessageType list = schema.of("ListValue");

    MessageType value = schema.of("Value");
    value.addField(1, new EnumType());
    value.addField(2, ScalarType.DOUBLE);
    value.addField(3, ScalarType.STRING);
    value.addField(4, ScalarType.BOOL);
    value.addField(5, struct);
    value.addField(6, list);

    list.addField(1, value);

    field.addField(1, ScalarType.STRING);
    field.addField(2, value);

  }

}
