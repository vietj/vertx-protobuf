package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.map.Enum;
import io.vertx.tests.map.MapKeyVariant;
import io.vertx.tests.map.MapProto;
import io.vertx.tests.map.MapValueVariant;
import io.vertx.tests.map.ProtoReader;
import io.vertx.tests.map.ProtoWriter;
import io.vertx.tests.map.SchemaLiterals;
import io.vertx.tests.map.Value;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MapTest {

  @Test
  public void testEmptyMapKeyVariant() throws Exception {
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_string, MapKeyVariant::getString, "");
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_int32, MapKeyVariant::getInt32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_int64, MapKeyVariant::getInt64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_uint32, MapKeyVariant::getUint32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_uint64, MapKeyVariant::getUint64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_sint32, MapKeyVariant::getSint32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_sint64, MapKeyVariant::getSint64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_bool, MapKeyVariant::getBool, false);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_fixed64, MapKeyVariant::getFixed64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_sfixed64, MapKeyVariant::getSfixed64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_fixed32, MapKeyVariant::getFixed32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_sfixed32, MapKeyVariant::getSfixed32, 0);
  }

  public <K> void testEmptyMapKeyVariant(Field mapField, Function<MapKeyVariant, Map<K, Integer>> extractor, K expected) throws Exception {
    Buffer buffer = ProtobufWriter.encode(visitor -> {
      visitor.init(SchemaLiterals.MessageLiteral.MapKeyVariant);
      visitor.enter(mapField);
      visitor.leave(mapField);
    });
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.MapKeyVariant, reader, buffer);
    MapKeyVariant map = (MapKeyVariant) reader.stack.pop();
    Map<K, Integer> entries = extractor.apply(map);
    assertEquals(1, entries.size());
    Map.Entry<K, Integer> entry = entries.entrySet().iterator().next();
    assertEquals(expected, entry.getKey());
    assertEquals(0, (int)entry.getValue());
  }

  @Test
  public void testReorderedMapKeyVariant() throws Exception {
    Field mapField = SchemaLiterals.FieldLiteral.MapKeyVariant_string;
    Buffer buffer = ProtobufWriter.encode(visitor -> {
      visitor.init(SchemaLiterals.MessageLiteral.MapKeyVariant);
      visitor.enter(mapField);
      visitor.visitVarInt32(((MessageType)mapField.type()).field(2), 4);
//      visitor.enter(((MessageType) mapField.type()).field(1));
      visitor.visitString(((MessageType)mapField.type()).field(1), "string-value");
//      visitor.leave(((MessageType) mapField.type()).field(1));
      visitor.leave(mapField);
    });
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.MapKeyVariant, reader, buffer);
    MapKeyVariant map = (MapKeyVariant) reader.stack.pop();
    Map<String, Integer> entries = map.getString();
    assertEquals(1, entries.size());
    Map.Entry<String, Integer> entry = entries.entrySet().iterator().next();
    assertEquals("string-value", entry.getKey());
    assertEquals(4, (int)entry.getValue());
    byte[] serialized = ProtobufWriter.encodeToByteArray(v -> ProtoWriter.emit(map, v));
    MapProto.MapKeyVariant.parseFrom(serialized);
  }

  @Test
  public void testEmptyMapValueVariant() throws Exception {
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_string_v, MapValueVariant::getStringV, "");
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_bytes_v, MapValueVariant::getBytesV, Buffer.buffer());
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_int32_v, MapValueVariant::getInt32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_int64_v, MapValueVariant::getInt64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_uint32_v, MapValueVariant::getUint32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_uint64_v, MapValueVariant::getUint64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_sint32_v, MapValueVariant::getSint32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_sint64_v, MapValueVariant::getSint64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapKeyVariant_bool, MapValueVariant::getBoolV, false);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant__enum_v, MapValueVariant::getEnumV, Enum.constant_0);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_fixed64_v, MapValueVariant::getFixed64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_sfixed64_v, MapValueVariant::getSfixed64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant__double_v, MapValueVariant::getDoubleV, 0D);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_fixed32_v, MapValueVariant::getFixed32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant_sfixed32_v, MapValueVariant::getSfixed32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant__float_v, MapValueVariant::getFloatV, 0F);
    this.<Value>testEmptyMapValueVariant(SchemaLiterals.FieldLiteral.MapValueVariant__message_v, MapValueVariant::getMessageV, Assert::assertNotNull);
  }

  public <V> void testEmptyMapValueVariant(Field mapField, Function<MapValueVariant, Map<Integer, V>> extractor, V expected) throws Exception {
    this.<V>testEmptyMapValueVariant(mapField, extractor, actual -> assertEquals(expected, actual));
  }

  public <V> void testEmptyMapValueVariant(Field mapField, Function<MapValueVariant, Map<Integer, V>> extractor, Consumer<V> predicate) throws Exception {
    Buffer buffer = ProtobufWriter.encode(visitor -> {
      visitor.init(SchemaLiterals.MessageLiteral.MapValueVariant);
      visitor.enter(mapField);
      visitor.leave(mapField);
    });
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.MessageLiteral.MapValueVariant, reader, buffer);
    MapValueVariant map = (MapValueVariant) reader.stack.pop();
    Map<Integer, V> entries = extractor.apply(map);
    assertEquals(1, entries.size());
    Map.Entry<Integer, V> entry = entries.entrySet().iterator().next();
    assertEquals(0, (int)entry.getKey());
    predicate.accept(entry.getValue());
  }
}
