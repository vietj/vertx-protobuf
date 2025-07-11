package io.vertx.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.EncodeException;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.protobuf.schema.WireType;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static java.lang.Character.MIN_SUPPLEMENTARY_CODE_POINT;

public class ProtobufWriter {

  public static Buffer encode(Consumer<Visitor> consumer) {
    return Buffer.buffer(encodeToByteArray(consumer));
  }

  public static byte[] encodeToByteArray(Consumer<Visitor> consumer) {
    State state = new State();
    ComputePhase visitor = new ComputePhase();
    visitor.state = state;
    consumer.accept(visitor);
    EncodingPhase encoder = new EncodingPhase();
    encoder.state = state;
    consumer.accept(encoder);
    return state.buffer.getBytes();
  }

  static class State {
    int[] capture = new int[20];
    Buffer buffer;
    int[] strings = new int[20];
  }

  static class ComputePhase implements Visitor {

    State state;
    int[] numbers = new int[10];
    int[] lengths = new int[10];
    int[] indices = new int[10];
    int depth;
    int ptr;
    int string_ptr;

    @Override
    public void visitVarInt32(Field field, int v) {
      lengths[depth] += 1 + ProtobufEncoder.computeRawVarint32Size(v);
    }

    @Override
    public void visitDouble(Field field, double d) {
      lengths[depth] += 1 + 8;
    }

    @Override
    public void visitString(Field field, String s) {
      int length = 0;
      int a = s.length();
      for (int i = 0;i < a;i++) {
        char c = s.charAt(i);
        if (c < 128) {
          length++;
        } else {
          length = s.getBytes(StandardCharsets.UTF_8).length;
          break;
        }
      }
      state.strings[string_ptr++] = length;
      lengths[depth] += 1 + ProtobufEncoder.computeRawVarint32Size(length) + length;
    }

    @Override
    public void init(MessageType type) {
      string_ptr = 0;
      depth = 0;
      ptr = 0;
      indices[0] = ptr++;
    }

    @Override
    public void enter(Field field) {
      numbers[depth] = field.number;
      depth++;
      indices[depth] = ptr++;
      lengths[depth] = 0;
    }

    @Override
    public void leave(Field field) {
      int l = lengths[depth];
      lengths[depth] = 0;
      int index = indices[depth];
      state.capture[index] = l;
      l += 1 + ProtobufEncoder.computeRawVarint32Size(l);
      depth--;
      lengths[depth] += l;
    }

    @Override
    public void destroy() {
      int l = lengths[depth];
      state.capture[indices[depth]] = l;
    }
  }

  static class EncodingPhase implements Visitor {

    State state;
    ProtobufEncoder encoder;
    int ptr_;
    int string_ptr;

    @Override
    public void init(MessageType type) {
      ptr_ = 0;
      state.buffer = Buffer.buffer(state.capture[ptr_++]);
      encoder = new ProtobufEncoder(state.buffer);
    }

    @Override
    public void visitVarInt32(Field field, int v) {
      encoder.writeTag(field.number, WireType.VARINT.id);
      encoder.writeVarInt32(v);
    }

    @Override
    public void visitDouble(Field field, double d) {
      encoder.writeTag(field.number, WireType.I64.id);
      encoder.writeDouble(d);
    }

    @Override
    public void visitString(Field field, String s) {
      int length = state.strings[string_ptr++];
      encoder.writeTag(field.number, WireType.LEN.id);
      encoder.writeVarInt32(length);
      encoder.writeString(s);
    }

    @Override
    public void enter(Field field) {
      encoder.writeTag(field.number, field.type.wireType().id);
      encoder.writeVarInt32(state.capture[ptr_++]);
    }

    @Override
    public void leave(Field field) {
    }

    @Override
    public void destroy() {
    }
  }

  private static int encodedLengthGeneral(String string, int start) {
    int utf16Length = string.length();
    int utf8Length = 0;
    for (int i = start; i < utf16Length; i++) {
      char c = string.charAt(i);
      if (c < 0x800) {
        utf8Length += (0x7f - c) >>> 31; // branch free!
      } else {
        utf8Length += 2;
        // jdk7+: if (Character.isSurrogate(c)) {
        if (Character.MIN_SURROGATE <= c && c <= Character.MAX_SURROGATE) {
          // Check that we have a well-formed surrogate pair.
          int cp = Character.codePointAt(string, i);
          if (cp < MIN_SUPPLEMENTARY_CODE_POINT) {
            throw new EncodeException();
          }
          i++;
        }
      }
    }
    return utf8Length;
  }
}
