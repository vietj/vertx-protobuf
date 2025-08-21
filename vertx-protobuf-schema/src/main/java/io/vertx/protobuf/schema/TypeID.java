package io.vertx.protobuf.schema;

public enum TypeID {

  // VARINT
  INT32(),
  INT64(),
  UINT32(),
  UINT64(),
  SINT32(),
  SINT64(),
  BOOL(),
  ENUM(),

  // I64
  FIXED64(),
  SFIXED64(),
  DOUBLE(),

  // I32
  FIXED32(),
  SFIXED32(),
  FLOAT(),

  // LEN
  STRING(),
  BYTES(),
  MESSAGE()

}
