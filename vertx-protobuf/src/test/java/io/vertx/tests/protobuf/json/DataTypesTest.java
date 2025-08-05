package io.vertx.tests.protobuf.json;

import io.vertx.tests.protobuf.datatypes.ScalarTypes;
import org.junit.Test;

public class DataTypesTest {

  @Test
  public void testBoolean() {
    ScalarTypes dataTypes = new ScalarTypes();
    dataTypes.setBool(true);

  }

}
