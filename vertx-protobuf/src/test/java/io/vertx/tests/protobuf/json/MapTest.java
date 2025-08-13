package io.vertx.tests.protobuf.json;

import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.map.MapValueVariant;
import io.vertx.tests.map.ProtoReader;
import io.vertx.tests.map.MessageLiteral;
import io.vertx.tests.map.MapKeyVariant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapTest {

  @Test
  public void testParseStringKey() {
    MapKeyVariant map = parseMap("{\"string\":{\"foo\":50}}");
    assertEquals(50, (int)map.getString().get("foo"));
  }

  @Test
  public void testParseInt32Key() {
    MapKeyVariant map = parseMap("{\"int32\":{\"4\":50}}");
    assertEquals(50, (int)map.getInt32().get(4));
  }

  @Test
  public void testParseInt64Key() {
    MapKeyVariant map = parseMap("{\"int64\":{\"4\":50}}");
    assertEquals(50, (int)map.getInt64().get(4L));
  }

  @Test
  public void testParseUInt32Key() {
    MapKeyVariant map = parseMap("{\"uint32\":{\"4\":50}}");
    assertEquals(50, (int)map.getUint32().get(4));
  }

  @Test
  public void testParseUInt64Key() {
    MapKeyVariant map = parseMap("{\"uint64\":{\"4\":50}}");
    assertEquals(50, (int)map.getUint64().get(4L));
  }

  @Test
  public void testParseSInt32Key() {
    MapKeyVariant map = parseMap("{\"sint32\":{\"4\":50}}");
    assertEquals(50, (int)map.getSint32().get(4));
  }

  @Test
  public void testParseSInt64Key() {
    MapKeyVariant map = parseMap("{\"sint64\":{\"4\":50}}");
    assertEquals(50, (int)map.getSint64().get(4L));
  }

  @Test
  public void testParseBoolKey() {
    MapKeyVariant map = parseMap("{\"bool\":{\"true\":50}}");
    assertEquals(50, (int)map.getBool().get(true));
  }

  @Test
  public void testParseFixed64Key() {
    MapKeyVariant map = parseMap("{\"fixed64\":{\"4\":50}}");
    assertEquals(50, (int)map.getFixed64().get(4L));
  }

  @Test
  public void testParseSFixed64Key() {
    MapKeyVariant map = parseMap("{\"sfixed64\":{\"4\":50}}");
    assertEquals(50, (int)map.getSfixed64().get(4L));
  }

  @Test
  public void testParseFixed32Key() {
    MapKeyVariant map = parseMap("{\"fixed32\":{\"4\":50}}");
    assertEquals(50, (int)map.getFixed32().get(4));
  }

  @Test
  public void testParseSFixed32Key() {
    MapKeyVariant map = parseMap("{\"sfixed32\":{\"4\":50}}");
    assertEquals(50, (int)map.getSfixed32().get(4));
  }

  private MapKeyVariant parseMap(String json) {
    ProtoReader pr = new ProtoReader();
    JsonReader.parse(json, MessageLiteral.MapKeyVariant, pr);
    return (MapKeyVariant) pr.stack.pop();
  }

  @Test
  public void testSerializeMapValue() {
    ProtoReader pr = new ProtoReader();
    JsonReader.parse("{\"_message_v\":{\"15\":{\"value\":4}}}", MessageLiteral.MapValueVariant, pr);
    MapValueVariant pop = (MapValueVariant) pr.stack.pop();
    assertEquals(4, (int)pop.getMessageV().get(15).getValue());
  }
}
