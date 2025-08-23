package io.vertx.protobuf.annotations;

import io.vertx.protobuf.schema.TypeID;

public @interface ProtoField {

  int number();
  String name();
  TypeID type() default TypeID.UNDEFINED;

}
