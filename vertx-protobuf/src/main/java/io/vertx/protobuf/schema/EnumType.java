package io.vertx.protobuf.schema;

import java.util.OptionalInt;

public interface EnumType extends Type {

  OptionalInt numberOf(String name);

  String nameOf(int number);
}
