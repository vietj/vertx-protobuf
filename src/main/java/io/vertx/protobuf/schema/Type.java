package io.vertx.protobuf.schema;

public interface Type {
  TypeID id();
  WireType wireType();
}
