package io.vertx.tests.protobuf;

import io.vertx.tests.protobuf.enumeration.EnumLiteral;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class EnumTest {

  @Test
  public void testAliases() {
    assertEquals(0, EnumLiteral.EnumWithAliases.numberOf("ZERO").getAsInt());
    assertEquals(1, EnumLiteral.EnumWithAliases.numberOf("ONE").getAsInt());
    assertEquals(2, EnumLiteral.EnumWithAliases.numberOf("TWO").getAsInt());
    assertEquals(1, EnumLiteral.EnumWithAliases.numberOf("UNO").getAsInt());
    assertEquals(2, EnumLiteral.EnumWithAliases.numberOf("DOS").getAsInt());
  }
}
