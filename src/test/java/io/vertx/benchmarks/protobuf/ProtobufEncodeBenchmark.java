package io.vertx.benchmarks.protobuf;

import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.NullValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.json.JsonWriter;
import io.vertx.protobuf.json.StructWriter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@State(Scope.Thread)
public class ProtobufEncodeBenchmark extends BenchmarkBase {

  private Struct struct;
  private JsonObject json;

  @Setup
  public void setup() {
    struct = Struct
      .newBuilder()
      .putFields("the-string", Value.newBuilder().setStringValue("the-string-value").build())
      .putFields("the-number", Value.newBuilder().setNumberValue(4).build())
      .putFields("the-boolean", Value.newBuilder().setBoolValue(true).build())
      .putFields("the-null", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
      .putFields("the-object", Value.newBuilder().setStructValue(Struct
        .newBuilder()
        .putFields("the-string", Value.newBuilder().setStringValue("the-string-value").build())
        .putFields("the-number", Value.newBuilder().setNumberValue(4).build())
        .putFields("the-boolean", Value.newBuilder().setBoolValue(true).build())
        .putFields("the-null", Value.newBuilder().setNullValue(NullValue.NULL_VALUE).build())
        .build()).build())
      .build();
    json = new JsonObject()
      .put("the-string", "the-string-value")
      .put("the-number", 4)
      .put("the-boolean", true)
      .put("the-null", null)
      .put("the-object", new JsonObject()
        .put("the-string", "the-string-value")
        .put("the-number", 4)
        .put("the-boolean", true)
        .put("the-null", null));
  }

  @Benchmark
  public byte[] structToByteArray() {
    return struct.toByteArray();
  }

  @Benchmark
  public byte[] structToOutputStream() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    CodedOutputStream cos = CodedOutputStream.newInstance(baos);
    try {
      struct.writeTo(cos);
    } finally {
      cos.flush();
      baos.close();
    }
    return baos.toByteArray();
  }

  @Benchmark
  public byte[] vertxStruct() {
    return StructWriter.encodeToByteArray(struct);
  }

  @Benchmark
  public byte[] jsonObject() {
    return JsonWriter.encodeToByteArray(json);
  }
}
