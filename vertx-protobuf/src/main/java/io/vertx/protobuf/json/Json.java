package io.vertx.protobuf.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.interop.ProtoReader;
import io.vertx.protobuf.interop.ProtoWriter;
import io.vertx.protobuf.well_known_types.MessageLiteral;

import java.util.function.Consumer;

public class Json {

  public static Buffer encodeToBuffer(JsonObject json) {
    Consumer<RecordVisitor> consumer = visitor -> {
      ProtoWriter.emit(json, visitor);
    };
    return ProtobufWriter.encode(consumer);
  }

  public static byte[] encodeToByteArray(JsonObject json) {
    Consumer<RecordVisitor> consumer = visitor -> {
      ProtoWriter.emit(json, visitor);
    };
    return ProtobufWriter.encodeToByteArray(consumer);
  }

  public static JsonObject parseStruct(Buffer buffer) {
    ProtoReader builder = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Struct, builder, buffer);
    return (JsonObject) builder.pop();
  }
}
