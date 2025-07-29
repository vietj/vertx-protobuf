package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.tests.oneof.ProtoReader;
import io.vertx.tests.oneof.SchemaLiterals;
import io.vertx.tests.oneof.AppleMsg;
import io.vertx.tests.oneof.BananaMsg;
import io.vertx.tests.oneof.Container;
import io.vertx.tests.oneof.OneOfProto;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class OneOfTest {

  @Test
  public void testOneOf() throws Exception {
    byte[] bytes = OneOfProto.Container.newBuilder().setBanana(OneOfProto.BananaMsg.newBuilder().build()).build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(SchemaLiterals.CONTAINER, reader, Buffer.buffer(bytes));
    Container msg = (Container) reader.stack.pop();
    assertNotNull(msg.getFruit());
    assertEquals(Container.FruitDiscriminant.BANANA, msg.getFruit().discriminant());
  }

  @Test
  public void testAPI() throws Exception {
    BananaMsg bananaMsg = new BananaMsg();
    Container.Fruit<BananaMsg> v = Container.Fruit.ofBanana(bananaMsg);
    assertSame(bananaMsg, v.get());
    Optional<AppleMsg> apple = v.asApple();
    assertFalse(apple.isPresent());
    assertTrue(v.asBanana().isPresent());
    assertEquals(Container.FruitDiscriminant.BANANA, v.discriminant());
  }
}
