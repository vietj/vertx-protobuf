package io.vertx.tests.protobuf;

import org.junit.Test;

import java.util.Optional;

public class EnumTest {

  public interface Color {
    enum Enum implements Color {
      BLUE, RED;
      public Enum asEnum() {
        return this;
      }
      public int value() {
        return 0;
      }
    }
    int value();
    Enum asEnum();
  }


  @Test
  public void testFoo() {
    System.out.println(Color.Enum.BLUE.asEnum());
  }


}
