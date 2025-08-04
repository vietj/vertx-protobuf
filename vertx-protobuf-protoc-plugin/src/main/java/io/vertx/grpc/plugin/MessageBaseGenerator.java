package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.vertx.grpc.plugin.Utils.absoluteFileName;

class MessageBaseGenerator {

  private final String javaPkgFqn;

  public MessageBaseGenerator(String javaPkgFqn) {
    this.javaPkgFqn = javaPkgFqn;
  }

  PluginProtos.CodeGeneratorResponse.File generate() {
    GenWriter writer = new GenWriter();
    writer.println(
      "package " + javaPkgFqn + ";",
      "import java.util.Map;",
      "import java.util.LinkedHashMap;",
      "import java.util.List;",
      "import java.util.ArrayList;",
      "import io.vertx.core.buffer.Buffer;",
      "import io.vertx.protobuf.schema.Field;",
      "class MessageBase {",
      "  Map<Field, List<Object>> unknownFields;",
      "  List<Object> unknownField(Field field) {",
      "    if (unknownFields == null) {",
      "      unknownFields = new LinkedHashMap<>();",
      "    }",
      "    return unknownFields.computeIfAbsent(field, f -> new ArrayList<>());",
      "  }",
      "}"
    );
    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(absoluteFileName(javaPkgFqn, "MessageBase"))
      .setContent(writer.toString())
      .build();

  }
}
