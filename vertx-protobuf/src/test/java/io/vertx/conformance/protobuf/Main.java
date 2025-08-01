package io.vertx.conformance.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.conformance.Conformance;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoWriter;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.TestAllTypesProto3;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.ProtoReader;
import io.vertx.protobuf.com.google.protobuf_test_messages.proto3.SchemaLiterals;

public class Main {

  private static int testCount;

  private static void run() throws Exception {
    while (doTestIo()) {
      testCount++;
    }
  }

  private static boolean doTestIo() throws Exception {
    int bytes = readLittleEndianIntFromStdin();

    if (bytes == -1) {
      return false; // EOF
    }

    byte[] serializedInput = new byte[bytes];

    if (!readFromStdin(serializedInput, bytes)) {
      throw new RuntimeException("Unexpected EOF from test program.");
    }

    Conformance.ConformanceRequest request =
      Conformance.ConformanceRequest.parseFrom(serializedInput);
    Conformance.ConformanceResponse response = doTest(request);
    byte[] serializedOutput = response.toByteArray();

    writeLittleEndianIntToStdout(serializedOutput.length);
    writeToStdout(serializedOutput);


    return true;
  }

  private static Conformance.ConformanceResponse doTest(Conformance.ConformanceRequest request) {
    TestAllTypesProto3 testMessage;
    String messageType = request.getMessageType();

/*
    ExtensionRegistry extensions = ExtensionRegistry.newInstance();
    try {
      createTestFile(messageType)
        .getMethod("registerAllExtensions", ExtensionRegistry.class)
        .invoke(null, extensions);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
*/

    switch (request.getPayloadCase()) {
      case PROTOBUF_PAYLOAD: {

        if (messageType.equals("protobuf_test_messages.proto3.TestAllTypesProto3")) {
//          TestAllTypesProto3.
          ProtoReader reader = new ProtoReader();
          try {
            Buffer buffer = Buffer.buffer(request.getProtobufPayload().toByteArray());
            ProtobufReader.parse(SchemaLiterals.MessageLiteral.TestAllTypesProto3, reader, buffer);
          } catch (DecodeException | IndexOutOfBoundsException e) {
            return Conformance.ConformanceResponse.newBuilder().setParseError(e.getMessage() != null ? e.getMessage() : e.getClass().getName()).build();
          }
          testMessage = (TestAllTypesProto3) reader.stack.pop();
        } else {
          throw new UnsupportedOperationException("Invalid " + messageType);
        }

        String s = "";
        String t = "";
/*
        try {
          testMessage =
            parseBinary(
              request.getProtobufPayload(),
              (Parser<AbstractMessage>)
                createTestMessage(messageType).getMethod("parser").invoke(null),
              extensions);
        } catch (InvalidProtocolBufferException e) {
          return Conformance.ConformanceResponse.newBuilder()
            .setParseError(e.getMessage())
            .build();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
*/
        break;
      }
      case JSON_PAYLOAD: {
//        try {
//          JsonFormat.Parser parser = JsonFormat.parser().usingTypeRegistry(typeRegistry);
//          if (request.getTestCategory()
//            == Conformance.TestCategory.JSON_IGNORE_UNKNOWN_PARSING_TEST) {
//            parser = parser.ignoringUnknownFields();
//          }
//          AbstractMessage.Builder<?> builder =
//            (AbstractMessage.Builder<?>)
//              createTestMessage(messageType).getMethod("newBuilder").invoke(null);
//          parser.merge(request.getJsonPayload(), builder);
//          testMessage = (AbstractMessage) builder.build();
//        } catch (InvalidProtocolBufferException e) {
//          return Conformance.ConformanceResponse.newBuilder()
//            .setParseError(e.getMessage())
//            .build();
//        } catch (Exception e) {
//          throw new RuntimeException(e);
//        }
//        break;
      }
      case TEXT_PAYLOAD: {
//        try {
//          AbstractMessage.Builder<?> builder =
//            (AbstractMessage.Builder<?>)
//              createTestMessage(messageType).getMethod("newBuilder").invoke(null);
//          TextFormat.merge(request.getTextPayload(), extensions, builder);
//          testMessage = (AbstractMessage) builder.build();
//        } catch (TextFormat.ParseException e) {
//          return Conformance.ConformanceResponse.newBuilder()
//            .setParseError(e.getMessage())
//            .build();
//        } catch (Exception e) {
//          throw new RuntimeException(e);
//        }
//        break;
      }
      case PAYLOAD_NOT_SET: {
        throw new IllegalArgumentException("Request didn't have payload.");
      }

      default: {
        throw new IllegalArgumentException("Unexpected payload case.");
      }
    }

    switch (request.getRequestedOutputFormat()) {
      case UNSPECIFIED:
        throw new IllegalArgumentException("Unspecified output format.");

      case PROTOBUF: {
        Buffer result = ProtobufWriter.encode(visitor -> {
          ProtoWriter.emit(testMessage, visitor);
        });
//        ByteString messageString = testMessage.toByteString();
        return Conformance.ConformanceResponse.newBuilder()
          .setProtobufPayload(ByteString.copyFrom(result.getBytes()))
          .build();
      }

      case JSON:
//        try {
//          return Conformance.ConformanceResponse.newBuilder()
//            .setJsonPayload(
//              JsonFormat.printer().usingTypeRegistry(typeRegistry).print(testMessage))
//            .build();
//        } catch (InvalidProtocolBufferException | IllegalArgumentException e) {
//          return Conformance.ConformanceResponse.newBuilder()
//            .setSerializeError(e.getMessage())
//            .build();
//        }

      case TEXT_FORMAT:
//        return Conformance.ConformanceResponse.newBuilder()
//          .setTextPayload(TextFormat.printer().printToString(testMessage))
//          .build();

      default:
      {
        throw new IllegalArgumentException("Unexpected request output.");
      }
    }
  }

  private static void writeLittleEndianIntToStdout(int val) throws Exception {
    byte[] buf = new byte[4];
    buf[0] = (byte) val;
    buf[1] = (byte) (val >> 8);
    buf[2] = (byte) (val >> 16);
    buf[3] = (byte) (val >> 24);
    writeToStdout(buf);
  }

  private static void writeToStdout(byte[] buf) throws Exception {
    System.out.write(buf);
  }

  private static int readLittleEndianIntFromStdin() throws Exception {
    byte[] buf = new byte[4];
    if (!readFromStdin(buf, 4)) {
      return -1;
    }
    return (buf[0] & 0xff)
      | ((buf[1] & 0xff) << 8)
      | ((buf[2] & 0xff) << 16)
      | ((buf[3] & 0xff) << 24);
  }

  private static boolean readFromStdin(byte[] buf, int len) throws Exception {
    int ofs = 0;
    while (len > 0) {
      int read = System.in.read(buf, ofs, len);
      if (read == -1) {
        return false; // EOF
      }
      ofs += read;
      len -= read;
    }

    return true;
  }


  public static void main(String[] args) throws Exception {
//    Thread.sleep(100000000);
    run();
  }
}
