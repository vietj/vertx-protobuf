package io.vertx.protobuf.json;

import io.vertx.protobuf.RecordVisitor;
import io.vertx.protobuf.schema.Field;

public interface JsonVisitor extends RecordVisitor {

  void visitEnum(Field field, String name);

}
