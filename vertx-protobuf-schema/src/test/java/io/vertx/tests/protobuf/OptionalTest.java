package io.vertx.tests.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import io.vertx.protobuf.schema.DefaultField;
import io.vertx.protobuf.schema.DefaultMessageType;
import io.vertx.protobuf.schema.OneOf;
import io.vertx.protobuf.schema.SchemaCompiler;
import io.vertx.tests.protobuf.schema.MessageWithOneOf;
import io.vertx.tests.protobuf.schema.MessageWithOptional;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class OptionalTest {

  @Test
  public void testFields() {
    Descriptors.Descriptor desc = MessageWithOptional.getDescriptor();
    List<Descriptors.FieldDescriptor> fields = desc.getFields();
    fields.forEach(field -> {
      System.out.println(field.getName() + " " + field.isOptional() + " " + field.getContainingOneof() + " " + field.getRealContainingOneof());
    });
    desc.getOneofs().forEach(oneOf -> {
      System.out.println(oneOf.getName() + " ");
    });
//    DefaultMessageType type = new SchemaCompiler().compile(MessageWithOptional.getDescriptor());
//    DefaultField f1 = type.field(1);
//    DefaultField f2 = type.field(2);
//    DefaultField f3 = type.field(3);
//    DefaultField f4 = type.field(3);
//    assertTrue(f1.isOptional());
//    assertTrue(f2.isOptional());
//    assertTrue(f3.isOptional());
  }
}
