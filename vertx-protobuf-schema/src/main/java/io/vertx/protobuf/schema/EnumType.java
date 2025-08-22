package io.vertx.protobuf.schema;

import java.util.OptionalInt;

public interface EnumType extends Type {

  String name();

  String packageName();

  String javaPackageName();

  OptionalInt numberOf(String name);

  String nameOf(int number);

  default MessageType enclosingType() {
    return null;
  }

}
