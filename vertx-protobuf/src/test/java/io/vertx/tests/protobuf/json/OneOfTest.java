package io.vertx.tests.protobuf.json;

import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.json.ProtoJsonReader;
import io.vertx.tests.oneof.Container;
import io.vertx.tests.oneof.FieldLiteral;
import io.vertx.tests.oneof.MessageLiteral;
import io.vertx.tests.oneof.ProtoReader;
import org.junit.Test;

public class OneOfTest {

  /**
   * For now duplicate field are accepted which contradicts the behavior of ProtoJSON, note that Protobuf
   * accepts this behavior.
   */
  @Test
  public void testDuplicate() {

    ProtoReader reader = new ProtoReader();
    JsonObject json = new JsonObject()
      .put(FieldLiteral.Container_integer.jsonName(), 1)
      .put(FieldLiteral.Container_string.jsonName(), "str");
    ProtoJsonReader.parse(json.encode(), MessageLiteral.Container, reader);
    Container msg = (Container) reader.stack.pop();
    System.out.println(msg.getScalar().discriminant());
  }
}
