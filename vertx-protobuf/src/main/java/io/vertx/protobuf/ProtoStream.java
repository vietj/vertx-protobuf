package io.vertx.protobuf;

import java.util.function.Consumer;

@FunctionalInterface
public interface ProtoStream extends Consumer<ProtoVisitor> {

  void accept(ProtoVisitor visitor);

}
