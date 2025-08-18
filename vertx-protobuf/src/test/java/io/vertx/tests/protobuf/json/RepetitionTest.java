package io.vertx.tests.protobuf.json;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.protobuf.RecordingVisitor;
import io.vertx.tests.protobuf.RepetitionTestBase;
import io.vertx.tests.repetition.ProtoReader;
import junit.framework.AssertionFailedError;

public class RepetitionTest extends RepetitionTestBase {

  @Override
  protected void assertRepetition(MessageLite message, MessageType type, RecordingVisitor visitor) {
    String json;
    try {
      json = JsonFormat.printer().print((MessageOrBuilder) message);
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    JsonReader.parse(json, type, visitor.checker());
  }

  @Override
  protected <T> T parseRepetition(MessageLite message, MessageType type) {
    String json;
    try {
      json = JsonFormat.printer().print((MessageOrBuilder) message);
    } catch (InvalidProtocolBufferException e) {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(e);
      throw afe;
    }
    ProtoReader reader = new ProtoReader();
    JsonReader.parse(json, type, reader);
    return (T) reader.stack.pop();
  }
}
