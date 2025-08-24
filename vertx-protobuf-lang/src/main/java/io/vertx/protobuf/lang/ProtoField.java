package io.vertx.protobuf.lang;

import io.vertx.protobuf.schema.TypeID;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface ProtoField {

  int number();
  String name();
  TypeID type() default TypeID.UNDEFINED;

}
