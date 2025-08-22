package io.vertx.protobuf.schema;

public interface OneOf {

  String name();

  Iterable<Field> fields();

}
