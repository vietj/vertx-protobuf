package io.vertx.tests.protobuf.json;

import com.google.protobuf.util.JsonFormat;
import io.vertx.protobuf.json.JsonReader;
import io.vertx.tests.embedding.Container;
import io.vertx.tests.embedding.EmbeddingProto;
import io.vertx.tests.embedding.MessageLiteral;
import io.vertx.tests.embedding.ProtoReader;
import org.junit.Test;

import static org.junit.Assert.*;

public class EmbeddingTest {

  @Test
  public void testEmbedding() throws Exception {
    EmbeddingProto.Container expected = EmbeddingProto.Container.newBuilder().setEmbedded(EmbeddingProto.Embedded.newBuilder().setValue(4).build()).build();
    String json = JsonFormat.printer().print(expected);
    ProtoReader visitor = new ProtoReader();
    JsonReader.parse(json, MessageLiteral.Container, visitor);
    Container container = (Container) visitor.stack.pop();
    assertNotNull(container.getEmbedded());
    assertEquals(4, (int)container.getEmbedded().getValue());
  }

  @Test
  public void testRepeating() throws Exception {
    EmbeddingProto.Container expected = EmbeddingProto.Container.newBuilder()
      .addRepeated(EmbeddingProto.Embedded.newBuilder().setValue(4).build())
      .addRepeated(EmbeddingProto.Embedded.newBuilder().setValue(6).build())
      .build();
    String json = JsonFormat.printer().print(expected);
    ProtoReader visitor = new ProtoReader();
    JsonReader.parse(json, MessageLiteral.Container, visitor);
    Container container = (Container) visitor.stack.pop();
    assertNotNull(container.getRepeated());
    assertEquals(2, container.getRepeated().size());
  }
}
