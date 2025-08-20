package io.vertx.protobuf.schema;

import java.util.OptionalInt;
import java.util.Set;

public interface EnumType extends Type {

  OptionalInt numberOf(String name);

  String nameOf(int number);

  Set<Integer> numbers();

}
