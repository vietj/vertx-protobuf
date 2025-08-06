package io.vertx.tests.protobuf.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.JacksonCodec;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.tests.protobuf.ProtoReader;
import io.vertx.tests.protobuf.ProtoWriter;
import io.vertx.tests.protobuf.SchemaLiterals;
import io.vertx.tests.protobuf.SimpleMessage;
import io.vertx.tests.protobuf.TestProto;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class JsonTest {

  @Test
  public void testSimple() throws Exception {

    String json = JsonFormat.printer().print(TestProto.SimpleMessage.newBuilder()
      .setStringField("the-string")
      .setInt32Field(4)
      .addStringListField("s1")
      .addStringListField("s2")
      .build());

    ProtoReader pr = new ProtoReader();

    JsonReader.parse(json, SchemaLiterals.MessageLiteral.SimpleMessage, pr);

    SimpleMessage pop = (SimpleMessage) pr.stack.pop();

    assertEquals("the-string", pop.getStringField());
    assertEquals(4, (int)pop.getInt32Field());
    assertEquals(Arrays.asList("s1", "s2"), pop.getStringListField());


    StringWriter out = new StringWriter();
    JsonGenerator generator = JsonFactory.builder().build().createGenerator(out);
    JsonWriter writer = new JsonWriter(generator);
    ProtoWriter.emit(pop, writer);

    generator.close();

    JsonObject j = new JsonObject(out.toString());
    assertEquals("the-string", j.getString("stringField"));
    assertEquals(4, (int)j.getInteger("int32Field"));
    assertEquals(Arrays.asList("s1", "s2"), j.getJsonArray("stringListField"));




  }

}
