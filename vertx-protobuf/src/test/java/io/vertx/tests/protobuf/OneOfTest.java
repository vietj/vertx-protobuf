package io.vertx.tests.protobuf;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.protobuf.ProtobufEncoder;
import io.vertx.protobuf.ProtobufReader;
import io.vertx.protobuf.ProtobufWriter;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.oneof.FieldLiteral;
import io.vertx.tests.oneof.ProtoWriter;
import io.vertx.tests.oneof.ProtoReader;
import io.vertx.tests.oneof.MessageLiteral;
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
    byte[] bytes = OneOfProto.Container.newBuilder().setBanana(OneOfProto.BananaMsg.newBuilder().setWeight(15).build()).build().toByteArray();
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Container, reader, Buffer.buffer(bytes));
    Container msg = (Container) reader.stack.pop();
    assertNotNull(msg.getFruit());
    assertEquals(Container.FruitDiscriminant.BANANA, msg.getFruit().discriminant());
    assertEquals(15, (int)msg.getFruit().asBanana().get().getWeight());
    bytes = ProtobufWriter.encodeToByteArray(visitor -> ProtoWriter.emit(msg, visitor));
    OneOfProto.Container c2 = OneOfProto.Container.parseFrom(bytes);
    assertEquals(15, c2.getBanana().getWeight());
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

  @Test
  public void testDuplicate() {
    RecordingVisitor visitor = new RecordingVisitor();
    visitor.init(MessageLiteral.Container);
    visitor.visitString(FieldLiteral.Container_string, "str");
    visitor.visitInt32(FieldLiteral.Container_integer, 4);
    visitor.destroy();
    Buffer encoded = ProtobufWriter.encode(visitor::apply);
    ProtoReader reader = new ProtoReader();
    ProtobufReader.parse(MessageLiteral.Container, reader, encoded);
    Container msg = (Container) reader.stack.pop();
    assertEquals(Container.ScalarDiscriminant.INTEGER, msg.getScalar().discriminant());
  }
}
