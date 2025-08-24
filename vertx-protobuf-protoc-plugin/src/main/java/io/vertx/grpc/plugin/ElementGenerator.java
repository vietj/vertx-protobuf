package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import io.vertx.protobuf.extension.VertxProto;

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

    private List<Descriptors.FieldDescriptor> fields;
    private List<Descriptors.OneofDescriptor> oneOfs;

    public DataObjectElement(Descriptors.Descriptor descriptor) {
      super(descriptor);
      fields = Utils.actualFields(descriptor);
      oneOfs = descriptor.getRealOneofs();
    }

    @Override
    Descriptors.Descriptor containingType() {
      return descriptor.getContainingType();
    }

    void generate2(GenWriter writer) {
      writer.println("public " + (container != null ? "static " : "") + "class " + descriptor.getName() + " extends io.vertx.protobuf.lang.MessageBase {");
      fields.forEach(fd -> {
        String javaType = Utils.javaTypeOf(fd);
        if (javaType != null) {
          writer.print("  " + javaType + " " + Utils.nameOf(fd));
          if (fd.isMapField()) {
            writer.println(" = new java.util.HashMap<>();");
          } else if (fd.isRepeated()) {
            writer.println(" = new java.util.ArrayList<>();");
          } else {
            writer.println(";");
          }
        }
      });
      oneOfs.forEach(oneOf -> {
        writer.println("  private " + Utils.nameOf(oneOf) + "<?> " + oneOf.getName() + ";");
      });
      writer.println("  public " + descriptor.getName() + " init() {\r\n");
//      fields.forEach(field -> {
//        if (field.getType() == Descriptors.FieldDescriptor.Type.ENUM && !field.isRepeated()) {
//          writer.println("    this." + field.getJsonName() + " = " + Utils.javaTypeOf(field) + ".valueOf(0);");
//        }
//      });
      writer.println("    return this;");
      writer.println("  }");
      fields.forEach(field -> {
        String javaType = Utils.javaTypeOf(field);
        if (javaType != null) {
          String getter = Utils.getterOf(field);
          String setter = Utils.setterOf(field);
          writer.println("  public " + javaType + " " + getter + "() {");
          if (field.getType() != Descriptors.FieldDescriptor.Type.MESSAGE && !field.isRepeated()) {
            writer.println("    " + javaType + " val = this." + Utils.nameOf(field) + ";");
            writer.println("    return val != null ? val : " + defaultValueOf(field) + ";");
          } else {
            writer.println("    return " + field.getJsonName() + ";");
          }
          writer.println("  };");
          writer.println("  public " + descriptor.getName() + " " + setter + "(" + javaType + " " + Utils.nameOf(field) + ") {");
          writer.println("    this." + Utils.nameOf(field) + " = " + Utils.nameOf(field) + ";");
          writer.println("    return this;");
          writer.println("  };");
        }
      });
      oneOfs.forEach(oneOf -> {
        String getter = Utils.getterOf(oneOf);
        String setter = Utils.setterOf(oneOf);
        writer.println("  public " + Utils.nameOf(oneOf) + "<?> " + getter + "() {");
        writer.println("    return " + oneOf.getName() + ";");
        writer.println("  };");
        writer.println("  public " + descriptor.getName() + " " + setter + "(" + Utils.nameOf(oneOf) + "<?> " + oneOf.getName() + ") {");
        writer.println("    this." + oneOf.getName() + " = " +  oneOf.getName() + ";");
        writer.println("    return this;");
        writer.println("  };");
      });
      oneOfs.forEach(oneOf -> {
/*
        writer.println("  public enum " + Utils.nameOf(oneOf) + " {");
        for (Iterator<Descriptors.FieldDescriptor> it = oneOf.getFields().iterator(); it.hasNext();) {
          Descriptors.FieldDescriptor field = it.next();
          writer.println("    " + field.getJsonName() + (it.hasNext() ? ", " : ""));
        }
        writer.println("  }");
*/
        writer.println("  public static enum " + Utils.nameOf(oneOf) + "Discriminant {");
        for (Iterator<Descriptors.FieldDescriptor> it = oneOf.getFields().iterator(); it.hasNext();) {
          Descriptors.FieldDescriptor field = it.next();
          writer.println("    " + field.getName().toUpperCase() + (it.hasNext() ? ", " : ""));
        }
        writer.println("  }");
        writer.println("  public static abstract class " + Utils.nameOf(oneOf) + "<T> {");
        writer.println("    public abstract " + Utils.nameOf(oneOf) + "Discriminant discriminant();");
        writer.println("    public abstract T get();");
        for (Descriptors.FieldDescriptor field : oneOf.getFields()) {
          writer.println("    public java.util.Optional<" + Utils.javaTypeOf(field) + "> as" + Utils.oneOfTypeName(field) + "() {");
          writer.println("      return java.util.Optional.empty();");
          writer.println("    }");
        }
        for (Descriptors.FieldDescriptor field : oneOf.getFields()) {
          writer.println("    public static " + Utils.nameOf(oneOf) + "<" + Utils.javaTypeOf(field) + "> of" + Utils.oneOfTypeName(field) + "(" + Utils.javaTypeOf(field) + " value) {");
          writer.println("      return new " + Utils.oneOfTypeName(field) + "(value);");
          writer.println("    }");
        }
        for (Descriptors.FieldDescriptor field : oneOf.getFields()) {
          writer.println("    private static class " + Utils.oneOfTypeName(field) + " extends " + Utils.nameOf(oneOf) + "<" + Utils.javaTypeOf(field) + "> {");
          writer.println("      private final java.util.Optional<" + Utils.javaTypeOf(field) + "> value;");
          writer.println("      private " + Utils.oneOfTypeName(field) + "(" + Utils.javaTypeOf(field) + " value) {");
          writer.println("        this.value = java.util.Optional.of(java.util.Objects.requireNonNull(value));");
          writer.println("      }");
          writer.println("      public " + Utils.nameOf(oneOf) + "Discriminant discriminant() {");
          writer.println("        return " + Utils.nameOf(oneOf) + "Discriminant." + field.getName().toUpperCase() + ";");
          writer.println("      }");
          writer.println("      public java.util.Optional<" + Utils.javaTypeOf(field) + "> as" + Utils.oneOfTypeName(field) + "() {");
          writer.println("        return value;");
          writer.println("      }");
          writer.println("      public " + Utils.javaTypeOf(field) + " get() {");
          writer.println("        return value.get();");
          writer.println("      }");
          writer.println("    }");
        }
        writer.println("  }");
      });
      nested.forEach(n -> {
        n.generate2(writer);
      });
      writer.println("}");
    }
  }

  private static String defaultValueOf(Descriptors.FieldDescriptor field) {
    switch (field.getType()) {
      case ENUM:
        return Utils.javaTypeOf(field) + "." + Utils.defaultEnumValue(field.getEnumType()).getName();
      case BOOL:
        return "false";
      case BYTES:
        // Not sure about that because it is mutable
        return "io.vertx.core.buffer.Buffer.buffer()";
      case STRING:
        return "\"\"";
      case FLOAT:
        return "0F";
      case DOUBLE:
        return "0D";
      case INT32:
      case UINT32:
      case SINT32:
      case FIXED32:
      case SFIXED32:
        return "0";
      case INT64:
      case UINT64:
      case SINT64:
      case FIXED64:
      case SFIXED64:
        return "0L";
      default:
        throw new UnsupportedOperationException();
    }
  }

  class EnumElement extends Element<Descriptors.EnumDescriptor> {

    private List<Constant> constants;

    public EnumElement(Descriptors.EnumDescriptor descriptor) {
      super(descriptor);

      constants = new ArrayList<>();

      for (Descriptors.EnumValueDescriptor enumValue : descriptor.getValues()) {
        Constant constant = new Constant(enumValue.getName(), enumValue.getNumber());
        constants.add(constant);
      }
    }

    @Override
    Descriptors.Descriptor containingType() {
      return descriptor.getContainingType();
    }

    @Override
    void generate2(GenWriter writer) {
      writer.println("public interface " + descriptor.getName() + " {",
        "");

      writer.println("  enum Enum implements " + descriptor.getName() + " {");
      for (Iterator<Constant> it = constants.iterator(); it.hasNext();) {
        Constant constant = it.next();
        writer.println(
          "    " + constant.identifier + "(" + constant.number + ")" + (it.hasNext() ? "," : ";"));
      }
      writer.println(
        "    private final int number;",
        "",
        "    Enum(int number) {",
        "      this.number = number;",
        "    }",
        "",
        "    public int number() {",
        "      return number;",
        "    }",
        "    public Enum asEnum() {",
        "      return this;",
        "    }",
        "  }");

      writer.println(
        "",
        "",
        "  public static " + descriptor.getName() + " valueOf(int number) {",
        "    String name = " + Utils.extractJavaPkgFqn(descriptor.getFile()) + ".EnumLiteral." + Utils.literalIdentifier(descriptor) + ".nameOf(number);",
        "    if (name != null) {",
        "      return Enum.valueOf(name);",
        "    } else {",
        "      return new " + descriptor.getName() + "() {",
        "        public String name() {",
        "          throw new IllegalStateException(\"Unknown\");",
        "        }",
        "        public int number() {",
        "          return number;",
        "        }",
        "        public Enum asEnum() {",
        "          return null;",
        "        }",
        "      };",
        "    }",
        "  }",
        "  public static " + descriptor.getName() + " valueOf(String name) {",
        "    try {",
        "      return Enum.valueOf(name);",
        "    } catch (IllegalArgumentException e) {",
        "      return new " + descriptor.getName() + "() {",
        "        public String name() {",
        "          return name;",
        "        }",
        "        public int number() {",
        "          throw new IllegalStateException(\"Unknown\");",
        "        }",
        "        public Enum asEnum() {",
        "          return null;",
        "        }",
        "      };",
        "    }",
        "  }",
        "",
        "  String name();",
        "  int number();",
        "  default boolean isUnknown() {",
        "    return asEnum() == null;",
        "  }",
        "  Enum asEnum();"
      );
      for (Iterator<Constant> it = constants.iterator(); it.hasNext();) {
        Constant constant = it.next();
        writer.println(
          "  " + descriptor.getName() + " " + constant.identifier + " = Enum." + constant.identifier + ";");
      }
      writer.println("}");
    }

    private class Constant {
      public final String identifier;
      public final int number;
      public Constant(String identifier, int number) {
        this.identifier = identifier;
        this.number = number;
      }
    }
  }
}
