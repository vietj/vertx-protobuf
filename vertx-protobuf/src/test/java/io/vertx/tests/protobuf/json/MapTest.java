package io.vertx.tests.protobuf.json;

import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.map.ProtoReader;
import io.vertx.tests.map.MessageLiteral;
import io.vertx.tests.map.MapKeyVariant;
import org.junit.Ignore;
import org.junit.Test;

public class MapTest {

  @Ignore
  @Test
  public void testSerializeMap() {

    ProtoReader pr = new ProtoReader();
    JsonReader.parse("{\"bool\":{\"true\":50}}", MessageLiteral.MapKeyVariant, pr);
    MapKeyVariant pop = (MapKeyVariant) pr.stack.pop();

  }
}
