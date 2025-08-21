package io.vertx.protobuf.schema;

import java.util.HashMap;
import java.util.Map;

public class DefaultSchema implements Schema {

  private final Map<String, DefaultMessageType> messages = new HashMap<>();

  public DefaultMessageType of(String messageName) {
    return messages.computeIfAbsent(messageName, DefaultMessageType::new);
  }

  public DefaultMessageType peek(String messageName) {
    return messages.get(messageName);
  }
}
