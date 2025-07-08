package io.vertx.protobuf.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.Visitor;

import java.util.function.Consumer;

public class JsonWriter {

  public static Buffer encode(JsonObject json) {
    Consumer<Visitor> consumer = visitor -> {
      JsonDriver.visitStruct(json, visitor);
    };
    return ProtobufWriter.encode(consumer);
  }

}
