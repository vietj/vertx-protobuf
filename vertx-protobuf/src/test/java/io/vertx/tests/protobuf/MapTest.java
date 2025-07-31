package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.schema.Field;
import io.vertx.protobuf.schema.MessageType;
import io.vertx.tests.map.Enum;
import io.vertx.tests.map.MapKeyVariant;
import io.vertx.tests.map.MapValueVariant;
import io.vertx.tests.map.ProtoReader;
import io.vertx.tests.map.SchemaLiterals;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class MapTest {

  @Test
  public void testEmptyMapKeyVariant() throws Exception {
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_STRING, MapKeyVariant::getString, "");
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_INT32, MapKeyVariant::getInt32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_INT64, MapKeyVariant::getInt64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_UINT32, MapKeyVariant::getUint32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_UINT64, MapKeyVariant::getUint64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_SINT32, MapKeyVariant::getSint32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_SINT64, MapKeyVariant::getSint64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_BOOL, MapKeyVariant::getBool, false);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_FIXED64, MapKeyVariant::getFixed64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_SFIXED64, MapKeyVariant::getSfixed64, 0L);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_FIXED32, MapKeyVariant::getFixed32, 0);
    testEmptyMapKeyVariant(SchemaLiterals.MAPKEYVARIANT_SFIXED32, MapKeyVariant::getSfixed32, 0);
  }

  public <K> void testEmptyMapKeyVariant(Field mapField, Function<MapKeyVariant, Map<K, Integer>> extractor, K expected) throws Exception {
    Buffer buffer = ProtobufWriter.encode(visitor -> {
      visitor.init(SchemaLiterals.MAPKEYVARIANT);
      visitor.enter(mapField);
      visitor.leave(mapField);
    });
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.MAPKEYVARIANT, reader, buffer);
    MapKeyVariant map = (MapKeyVariant) reader.stack.pop();
    Map<K, Integer> entries = extractor.apply(map);
    assertEquals(1, entries.size());
    Map.Entry<K, Integer> entry = entries.entrySet().iterator().next();
    assertEquals(expected, entry.getKey());
    assertEquals(0, (int)entry.getValue());
  }

  @Test
  public void testEmptyMapValueVariant() throws Exception {
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_STRING_V, MapValueVariant::getStringV, "");
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_BYTES_V, MapValueVariant::getBytesV, Buffer.buffer());
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_INT32_V, MapValueVariant::getInt32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_INT64_V, MapValueVariant::getInt64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_UINT32_V, MapValueVariant::getUint32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_UINT64_V, MapValueVariant::getUint64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_SINT32_V, MapValueVariant::getSint32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_SINT64_V, MapValueVariant::getSint64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_BOOL_V, MapValueVariant::getBoolV, false);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_BOOL_V, MapValueVariant::getBoolV, false);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT__ENUM_V, MapValueVariant::getEnumV, Enum.constant_0);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_FIXED64_V, MapValueVariant::getFixed64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_SFIXED64_V, MapValueVariant::getSfixed64V, 0L);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT__DOUBLE_V, MapValueVariant::getDoubleV, 0D);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_FIXED32_V, MapValueVariant::getFixed32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT_SFIXED32_V, MapValueVariant::getSfixed32V, 0);
    testEmptyMapValueVariant(SchemaLiterals.MAPVALUEVARIANT__FLOAT_V, MapValueVariant::getFloatV, 0F);
  }

  public <V> void testEmptyMapValueVariant(Field mapField, Function<MapValueVariant, Map<Integer, V>> extractor, V expected) throws Exception {
    Buffer buffer = ProtobufWriter.encode(visitor -> {
      visitor.init(SchemaLiterals.MAPVALUEVARIANT);
      visitor.enter(mapField);
      visitor.leave(mapField);
    });
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.MAPVALUEVARIANT, reader, buffer);
    MapValueVariant map = (MapValueVariant) reader.stack.pop();
    Map<Integer, V> entries = extractor.apply(map);
    assertEquals(1, entries.size());
    Map.Entry<Integer, V> entry = entries.entrySet().iterator().next();
    assertEquals(0, (int)entry.getKey());
    assertEquals(expected, entry.getValue());
  }
}
