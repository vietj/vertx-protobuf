package io.vertx.protobuf.schema;

public interface MapField extends Field {

  @Override
  default boolean isMap() {
    return true;
  }

  Field key();

  Field value();

}
