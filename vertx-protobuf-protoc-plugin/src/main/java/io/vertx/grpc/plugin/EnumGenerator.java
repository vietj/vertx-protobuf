package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class EnumGenerator {

  private final String javaPkgFqn;
  private final List<Descriptors.EnumDescriptor> descs;

  public EnumGenerator(String javaPkgFqn, List<Descriptors.EnumDescriptor> descs) {
    this.javaPkgFqn = javaPkgFqn;
    this.descs = descs;
  }

  public List<PluginProtos.CodeGeneratorResponse.File> generate() {
    return descs
      .stream()
      .map(mt -> buildFiles(javaPkgFqn, mt))
      .collect(Collectors.toList());
  }

  public static class Constant {
    public final String identifier;
    public final int index;
    public Constant(String identifier, int index) {
      this.identifier = identifier;
      this.index = index;
    }
  }

  private PluginProtos.CodeGeneratorResponse.File buildFiles(String javaPkgFqn, Descriptors.EnumDescriptor enumType) {
    List<Constant> constants = new ArrayList<>();
    for (Descriptors.EnumValueDescriptor enumValue : enumType.getValues()) {
      Constant constant = new Constant(enumValue.getName(), enumValue.getIndex());
      constants.add(constant);
    }

    GenWriter writer = new GenWriter();

    writer.println(
      "package " + javaPkgFqn + ";",
      "public enum " + enumType.getName() + " {",
      "");

    for (Iterator<Constant> it = constants.iterator(); it.hasNext();) {
      Constant constant = it.next();
      writer.println(
        "  " + constant.identifier + "(" + constant.index + ")" + (it.hasNext() ? "," : ";"));
    }

    writer.println(
      "",
      "  private static final java.util.Map<Integer, " + enumType.getName() + "> BY_INDEX = new java.util.HashMap<>();",
      "",
      "  public static " + enumType.getName() + " valueOf(int index) {",
      "    return BY_INDEX.get(index);",
      "  }",
      "",
      "  static {",
      "    for (" + enumType.getName() + " value : values()) {",
      "      BY_INDEX.put(value.index, value);",
      "    }",
      "  }",
      "",
      "  private final int index;",
      "",
      "  " + enumType.getName() + "(int index) {",
      "    this.index = index;",
      "  }",
      "",
      "  public int index() {",
      "    return index;",
      "  }",
      "}"
    );


    return Utils.buildFile(javaPkgFqn, enumType, writer.toString());
  }
}
