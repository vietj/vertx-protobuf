package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class ElementGenerator {

  private final String javaPkgFqn;
  private final List<Descriptors.Descriptor> typeDescriptors;
  private final List<Descriptors.EnumDescriptor> enumDescriptors;

  public ElementGenerator(String javaPkgFqn, List<Descriptors.Descriptor> typeDescriptors, List<Descriptors.EnumDescriptor> enumDescriptors) {
    this.javaPkgFqn = javaPkgFqn;
    this.typeDescriptors = typeDescriptors;
    this.enumDescriptors = enumDescriptors;
  }

  List<PluginProtos.CodeGeneratorResponse.File> generate() {

    Map<String, Element<?>> map = new LinkedHashMap<>();
    for (Descriptors.Descriptor desc : typeDescriptors) {
      map.put(desc.getFullName(), new DataObjectElement(desc));
    }
    for (Descriptors.EnumDescriptor desc : enumDescriptors) {
      map.put(desc.getFullName(), new EnumElement(desc));
    }
    List<Element<?>> units = new ArrayList<>();
    for (Element<?> desc : map.values()) {
      if (desc.containingType() != null) {
        Element<?> container = map.get(desc.containingType().getFullName());
        if (container == null) {
          StringBuilder log = new StringBuilder();
          typeDescriptors.forEach(d -> {
            log.append("<").append(d.getFullName()).append(">\r\n");
          });
          throw new IllegalStateException(log + "Cannot find " + desc.containingType().getFullName() + " for " + desc.descriptor.getFullName());
        }
        container.nested.add(desc);
        desc.container = container;
      } else {
        units.add(desc);
      }
    }

    return units
      .stream()
      .map(mt -> {
        GenWriter writer = new GenWriter();
        mt.generate(writer);
        return Utils.buildFile(javaPkgFqn, mt.descriptor, writer.toString());
      })
      .collect(Collectors.toList());
  }

  abstract class Element<D extends Descriptors.GenericDescriptor> {

    protected D descriptor;
    protected String simpleName;
    protected List<Element<?>> nested = new ArrayList<>();
    protected Element<?> container;

    public Element(D descriptor) {
      this.descriptor = descriptor;
      this.simpleName = descriptor.getName();
    }

    void generate(GenWriter writer) {
      writer.println("package " + javaPkgFqn + ";\r\n");
      generate2(writer);
    }

    abstract void generate2(GenWriter writer);

    abstract Descriptors.Descriptor containingType();

  }

  class DataObjectElement extends Element<Descriptors.Descriptor> {

    public DataObjectElement(Descriptors.Descriptor descriptor) {
      super(descriptor);
    }

    @Override
    Descriptors.Descriptor containingType() {
      return descriptor.getContainingType();
    }

    void generate2(GenWriter writer) {
      writer.println("public " + (container != null ? "static " : "") + "class " + descriptor.getName() + " {");
      descriptor.getFields().forEach(fd -> {
        String javaType = Utils.javaTypeOf(fd);
        if (javaType != null) {
          writer.println("  private " + javaType + " " + fd.getJsonName() + ";");
        }
      });
      writer.println("  public " + descriptor.getName() + " init() {\r\n");
      descriptor.getFields().forEach(field -> {
        if (field.getType() == Descriptors.FieldDescriptor.Type.ENUM && !field.isRepeated()) {
          writer.println("    this." + field.getJsonName() + " = " + Utils.javaTypeOf(field) + ".valueOf(0);");
        }
      });
      writer.println("    return this;");
      writer.println("  }");
      descriptor.getFields().forEach(field -> {
        String javaType = Utils.javaTypeOf(field);
        if (javaType != null) {
          String getter = Utils.getterOf(field);
          String setter = Utils.setterOf(field);
          writer.println("  public " + javaType + " " + getter + "() {");
          writer.println("    return " + field.getJsonName() + ";\r\n");
          writer.println("  };\r\n");
          writer.println("  public " + descriptor.getName() + " " + setter + "(" + javaType + " " + field.getJsonName() + ") {");
          writer.println("    this." + field.getJsonName() + " = " + field.getJsonName() + ";");
          writer.println("    return this;");
          writer.println("  };");
        }
      });
      nested.forEach(n -> {
        n.generate2(writer);
      });
      writer.println("}");
    }
  }

  class EnumElement extends Element<Descriptors.EnumDescriptor> {

    private List<Constant> constants;

    public EnumElement(Descriptors.EnumDescriptor descriptor) {
      super(descriptor);

      constants = new ArrayList<>();

      for (Descriptors.EnumValueDescriptor enumValue : descriptor.getValues()) {
        Constant constant = new Constant(enumValue.getName(), enumValue.getIndex());
        constants.add(constant);
      }
    }

    @Override
    Descriptors.Descriptor containingType() {
      return descriptor.getContainingType();
    }

    @Override
    void generate2(GenWriter writer) {
      writer.println("public enum " + descriptor.getName() + " {",
        "");

      for (Iterator<Constant> it = constants.iterator(); it.hasNext();) {
        Constant constant = it.next();
        writer.println(
          "  " + constant.identifier + "(" + constant.index + ")" + (it.hasNext() ? "," : ";"));
      }

      writer.println(
        "",
        "  private static final java.util.Map<Integer, " + descriptor.getName() + "> BY_INDEX = new java.util.HashMap<>();",
        "",
        "  public static " + descriptor.getName() + " valueOf(int index) {",
        "    return BY_INDEX.get(index);",
        "  }",
        "",
        "  static {",
        "    for (" + descriptor.getName() + " value : values()) {",
        "      BY_INDEX.put(value.index, value);",
        "    }",
        "  }",
        "",
        "  private final int index;",
        "",
        "  " + descriptor.getName() + "(int index) {",
        "    this.index = index;",
        "  }",
        "",
        "  public int index() {",
        "    return index;",
        "  }",
        "}"
      );
    }

    private class Constant {
      public final String identifier;
      public final int index;
      public Constant(String identifier, int index) {
        this.identifier = identifier;
        this.index = index;
      }
    }
  }
}
