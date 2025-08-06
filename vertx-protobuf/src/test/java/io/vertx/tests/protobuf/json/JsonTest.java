package io.vertx.tests.protobuf.json;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.json.Container;
import io.vertx.tests.json.JsonProto;
import io.vertx.tests.json.MessageLiteral;
import io.vertx.tests.json.ProtoReader;
import org.junit.Ignore;
import org.junit.Test;

public class JsonTest {

  @Ignore("Cannot pass for now")
  @Test
  public void testStruct() throws Exception {

    String json = JsonFormat.printer().print(JsonProto.Container.newBuilder()
      .setStruct(Struct.newBuilder().putFields("string-key", Value.newBuilder().setStringValue("string-value").build()).build())
      .build());

    ProtoReader pr = new ProtoReader();

    JsonReader.parse(json, MessageLiteral.Container, pr);

    Container pop = (Container) pr.stack.pop();

  }
}
