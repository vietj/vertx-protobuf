package io.vertx.tests.protobuf.json;

import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.protobuf.MessageLiteral;
import io.vertx.tests.protobuf.ProtoReader;
import io.vertx.tests.protobuf.SimpleMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldNameTest {

  @Test
  public void testOriginalFieldName() {
    ProtoReader reader = new ProtoReader();
    JsonReader.parse("{\"string_field\":\"the-string\"}", MessageLiteral.SimpleMessage, reader);
    SimpleMessage pop = (SimpleMessage) reader.stack.pop();
    assertEquals("the-string", pop.getStringField());
  }

  @Test
  public void testInferedFieldName() {
    ProtoReader reader = new ProtoReader();
    JsonReader.parse("{\"stringField\":\"the-string\"}", MessageLiteral.SimpleMessage, reader);
    SimpleMessage pop = (SimpleMessage) reader.stack.pop();
    assertEquals("the-string", pop.getStringField());
  }
}
