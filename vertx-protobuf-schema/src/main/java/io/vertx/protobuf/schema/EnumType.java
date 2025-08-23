package io.vertx.protobuf.schema;

import java.util.OptionalInt;

public interface EnumType extends Type {

  String name();

  OptionalInt numberOf(String name);

  String nameOf(int number);

}
