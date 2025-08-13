package io.vertx.tests.protobuf.json;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.tests.json.JsonProto;
import io.vertx.tests.json.ProtoWriter;
import io.vertx.tests.json.Unpacked;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class JsonWriterTest {

  @Test
  public void testListOfNumbers() {
    JsonProto.Unpacked expected = JsonProto.Unpacked.newBuilder().addAllListOfNumbers(Arrays.asList(1, 2, 3, 4)).build();
    Unpacked unpacked = new Unpacked();
    unpacked.getListOfNumbers().addAll(Arrays.asList(1, 2, 3, 4));
    assertEquals(expected, unpacked);
  }

  @Test
  public void testListOfEmbedded() {
    JsonProto.Unpacked expected = JsonProto.Unpacked.newBuilder().addListOfEmbedded(JsonProto.Unpacked.newBuilder().addAllListOfNumbers(Arrays.asList(1, 2, 3, 4)).build()).build();
    Unpacked unpacked = new Unpacked();
    unpacked.getListOfEmbedded().add(new Unpacked().setListOfNumbers(Arrays.asList(1, 2, 3, 4)));
    assertEquals(expected, unpacked);
  }

  private void assertEquals(JsonProto.Unpacked expected, Unpacked unpacked) {
    JsonObject expectedJson;
    try {
      expectedJson = new JsonObject(JsonFormat.printer().print(expected));
    } catch (InvalidProtocolBufferException e) {
      throw new AssertionFailedError(e.getMessage());
    }
    JsonObject json = JsonWriter.encode(v -> ProtoWriter.emit(unpacked, v));
    Assert.assertEquals(expectedJson, json);
  }
}
