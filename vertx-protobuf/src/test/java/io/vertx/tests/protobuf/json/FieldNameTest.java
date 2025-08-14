package io.vertx.tests.protobuf.json;

import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.protobuf.MessageLiteral;
import io.vertx.tests.protobuf.ProtoReader;
import io.vertx.tests.protobuf.SimpleMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class FieldNameTest {

  @Test
  public void testOriginalFieldName() {
    ProtoReader reader = new ProtoReader();
    JsonReader.parse("{\"string_field\":\"the-string\"}", MessageLiteral.SimpleMessage, reader);
    SimpleMessage pop = (SimpleMessage) reader.stack.pop();
    assertEquals("the-string", pop.getStringField());
  }

  @Test
  public void testInferredFieldName() {
    ProtoReader reader = new ProtoReader();
    JsonReader.parse("{\"stringField\":\"the-string\"}", MessageLiteral.SimpleMessage, reader);
    SimpleMessage pop = (SimpleMessage) reader.stack.pop();
    assertEquals("the-string", pop.getStringField());
  }

  @Test
  public void testUnknownFieldName() {
    ProtoReader reader = new ProtoReader();
    try {
      JsonReader.parse("{\"does_not_exist\":\"whatever\"}", MessageLiteral.SimpleMessage, reader);
      fail();
    } catch (DecodeException expected) {
    }
  }
}
