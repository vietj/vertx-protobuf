package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class ProtoReaderGenerator {

  private final Descriptors.FileDescriptor fileDesc;

  public ProtoReaderGenerator(Descriptors.FileDescriptor fileDesc) {
    this.fileDesc = fileDesc;
  }

  public enum VisitorKind {
    String,
    Bytes,
    VarInt32,
    Double,
    Message
  }

  public static class FieldDescriptor {
    public Descriptors.FieldDescriptor.Type type;
    public VisitorKind kind;
    public boolean map;
  }

  public PluginProtos.CodeGeneratorResponse.File generate() {

    String javaPkgFqn = Utils.extractJavaPkgFqn(fileDesc.toProto());




    Map<String, Descriptors.Descriptor> all = Utils.transitiveClosure(fileDesc.getMessageTypes());



    List<FieldDescriptor> collected = new ArrayList<>();
    for (Descriptors.Descriptor mt : fileDesc.getMessageTypes()) {
      for (Descriptors.FieldDescriptor fd : mt.getFields()) {
        VisitorKind kind;
        switch (fd.getType()) {
          // Bytes
          case BYTES:
            kind = VisitorKind.Bytes;
            break;
          // VarInt32
          case BOOL:
          case ENUM:
          case INT32:
            kind = VisitorKind.VarInt32;
            break;
          // String
          case STRING:
            kind = VisitorKind.String;
            break;
          // Double
          case DOUBLE:
            kind = VisitorKind.Double;
            break;
          case MESSAGE:
            kind = VisitorKind.Message;
            break;
          default:
            continue;
        }
        FieldDescriptor descriptor = new FieldDescriptor();
        descriptor.type = fd.getType();
        descriptor.kind = kind;
        collected.add(descriptor);
      }
    }

    //
    STGroup group = new STGroupFile("reader.stg");
    ST st = group.getInstanceOf("unit");

    st.add("pkg", javaPkgFqn);

    StringBuilder content = new StringBuilder();
    content.append("/*");
    content.append(st.render());
    content.append("*/");









    content.append("package ").append(javaPkgFqn).append(";\r\n");
    content.append("import io.vertx.protobuf.Visitor;\r\n");
    content.append("import io.vertx.protobuf.schema.MessageType;\r\n");
    content.append("import io.vertx.protobuf.schema.Field;\r\n");
    content.append("import java.util.Deque;\r\n");
    content.append("import java.util.ArrayDeque;\r\n");
    content.append("public class ProtoReader implements Visitor {\r\n");

    content.append("  public final Deque<Object> stack;\r\n");
    content.append("  private Visitor next;");
    content.append("  public ProtoReader(Deque<Object> stack) {\r\n");
    content.append("    this.stack = stack;\r\n");
    content.append("  }\r\n");
    content.append("  public ProtoReader() {\r\n");
    content.append("    this(new ArrayDeque<>());\r\n");
    content.append("  }\r\n");

    // **************
    // INIT
    // **************

    content.append("  public void init(MessageType type) {\r\n");
    boolean first = true;
    for (Descriptors.Descriptor messageType : fileDesc.getMessageTypes()) {
      if (first) {
        content.append("    ");
        first = false;
      } else {
        content.append(" else ");
      }
      content.append("if (type == SchemaLiterals.").append(Utils.schemaLiteralOf(messageType)).append(") {\r\n");
      content.append("      stack.push(new ").append(messageType.getName()).append("());\r\n");
      content.append("    }");
    }
    if (first) {
      content.append("    ");
    } else {
      content.append(" else ");
    }
    content.append("if (next != null) {\r\n");
    content.append("      next.init(type);\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    // **************
    // VISIT STRING
    // **************

    class Foo {
      final String methodStart;
      final Set<Descriptors.FieldDescriptor.Type> types;
      final String next;

      Foo(String methodStart, String next, Descriptors.FieldDescriptor.Type... types) {
        this.methodStart = methodStart;
        this.types = new HashSet<>(Arrays.asList(types));
        this.next = next;
      }
    }

    Foo[] foos = {
      new Foo("visitBytes(Field field, byte[] value)", "visitBytes(field, value)", Descriptors.FieldDescriptor.Type.BYTES),
      new Foo("visitString(Field field, String value)", "visitString(field, value)", Descriptors.FieldDescriptor.Type.STRING),
      new Foo("visitDouble(Field field, double value)", "visitDouble(field, value)", Descriptors.FieldDescriptor.Type.DOUBLE),
      new Foo("visitVarInt32(Field field, int value)", "visitVarInt32(field, value)", Descriptors.FieldDescriptor.Type.BOOL, Descriptors.FieldDescriptor.Type.ENUM, Descriptors.FieldDescriptor.Type.INT32)
    };

    for (Foo foo : foos) {
      content.append("  public void ").append(foo.methodStart).append(" {\r\n");
      first = true;
      for (Descriptors.Descriptor mt : all.values()) {
        for (Descriptors.FieldDescriptor fd : mt.getFields()) {
          if (foo.types.contains(fd.getType())) {
            if (first) {
              content.append("    ");
              first = false;
            } else {
              content.append(" else ");
            }
            content.append("if (field == SchemaLiterals.").append(Utils.schemaLiteralOf(fd)).append(") {\r\n");
            Function<String, String> converter = Function.identity();
            switch (fd.getType()) {
              case BOOL:
                converter = s -> "value == 1";
                break;
              case ENUM:
                converter = s -> Utils.javaTypeOf(fd) + ".valueOf(" + s + ")";
                break;
              case BYTES:
                converter = s -> "io.vertx.core.buffer.Buffer.buffer(" + s + ")";
                break;
            }
            if (fd.getContainingType().toProto().getOptions().getMapEntry()) {
              content.append("      stack.push(value);\r\n");
            } else if (fd.isRepeated()) {
              content.append("      ").append(Utils.javaTypeOf(fd.getContainingType())).append(" blah = (").append(Utils.javaTypeOf(fd.getContainingType())).append(")stack.peek()").append(";\t\n");
              content.append("      if (blah.").append(Utils.getterOf(fd)).append("() == null) {\r\n");
              content.append("        blah.").append(Utils.setterOf(fd)).append("(new java.util.ArrayList<>());\r\n");
              content.append("      }\r\n");
              content.append("      blah.").append(Utils.getterOf(fd)).append("().add(").append(converter.apply("value")).append(");\t\n");
            } else {
              content.append("      ((").append(Utils.javaTypeOf(fd.getContainingType())).append(")stack.peek()).").append(Utils.setterOf(fd)).append("(").append(converter.apply("value")).append(");\t\n");
            }
            content.append("    }");
          }
        }
      }
      if (first) {
        content.append("    ");
      } else {
        content.append(" else ");
      }
      content.append("if (next != null) {\r\n");
      content.append("      next.").append(foo.next).append(";\r\n");
      content.append("    } else {\r\n");
      content.append("      throw new UnsupportedOperationException();\r\n");
      content.append("    }\r\n");
      content.append("  }\r\n");
    }

    // **************
    // ENTER
    // **************

    content.append("  public void enter(Field field) {\r\n");
    first = true;
    for (Descriptors.Descriptor messageType : all.values()) {
      for (Descriptors.FieldDescriptor field : messageType.getFields()) {
        if (field.getType() != Descriptors.FieldDescriptor.Type.MESSAGE) {
          continue;
        }
        if (first) {
          content.append("    ");
          first = false;
        } else {
          content.append(" else ");
        }
        content.append("if (field == SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(") {\r\n");
        if (field.isMapField()) {
          content.append("      ").append(field.getContainingType().getName()).append(" container = (").append(field.getContainingType().getName()).append(")stack.peek();").append("\r\n");
          content.append("      ").append(Utils.javaTypeOf(field)).append(" map = container.").append(Utils.getterOf(field)).append("();\r\n");
          content.append("      if (map == null) {\r\n");
          content.append("        map = new java.util.HashMap<>();\r\n");
          content.append("        container.").append(Utils.setterOf(field)).append("(map);\r\n");
          content.append("      }\r\n");
          content.append("      stack.push(map);\r\n");
        } else {
          if (field.getMessageType().getFile() == fileDesc) {
            String i_type;
            if (field.isRepeated()) {
              i_type = "java.util.ArrayList<" + Utils.javaTypeOfInternal(field) + ">";
            } else {
              i_type = Utils.javaTypeOf(field);
            }
            content.append("      ").append(Utils.javaTypeOf(field)).append(" v = new ").append(i_type).append("();\r\n");
            content.append("      stack.push(v);\r\n");
          } else {
            content.append("      Visitor v = new ").append(field.getMessageType().getFile().getOptions().getJavaPackage()).append(".ProtoReader(stack);\r\n");
            content.append("      v.init((MessageType)field.type);\r\n");
            content.append("      next = v;\r\n");
          }
        }
        content.append("    }");
      }
    }
    if (first) {
      content.append("    ");
    } else {
      content.append(" else ");
    }
    content.append("if (next != null) {\r\n");
    content.append("      next.enter(field);\r\n");
    content.append("    } else {\r\n");
    content.append("      throw new UnsupportedOperationException();\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    // **************
    // VISIT LEAVE
    // **************

    content.append("  public void leave(Field field) {\r\n");
    first = true;
    for (Descriptors.Descriptor messageType : all.values()) {
      for (Descriptors.FieldDescriptor field : messageType.getFields()) {
        if (field.getType() != Descriptors.FieldDescriptor.Type.MESSAGE) {
          continue;
        }
        if (first) {
          content.append("    ");
          first = false;
        } else {
          content.append(" else ");
        }
        content.append("if (field == SchemaLiterals.").append(Utils.schemaLiteralOf(field)).append(") {\r\n");
        if (field.isMapField()) {
          content.append("      Object value = stack.pop();\r\n");
          content.append("      Object key = stack.pop();\r\n");
          content.append("      java.util.Map entries = (java.util.Map)stack.pop();\r\n");
          content.append("      entries.put(key, value);\r\n");
        } else if (field.getContainingType().toProto().getOptions().getMapEntry()) {

        } else {
          if (field.getMessageType().getFile() != fileDesc) {
            content.append("      next.destroy();\r\n");
            content.append("      next = null;\r\n");
          }
          content.append("      ").append(Utils.javaTypeOf(field)).append(" v = (").append(Utils.javaTypeOf(field)).append(")stack.pop();\r\n");
          content.append("      ((").append(messageType.getName()).append(")stack.peek()).").append(Utils.setterOf(field)).append("(v);\n");
        }
        content.append("    }");
      }
    }
    if (first) {
      content.append("    ");
    } else {
      content.append(" else ");
    }
    content.append("if (next != null) {\r\n");
    content.append("      next.leave(field);\r\n");
    content.append("    } else {\r\n");
    content.append("      throw new UnsupportedOperationException();\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    // **************
    // DESTROY
    // **************

    content.append("  public void destroy() {\r\n");
    content.append("    if (next != null) {\r\n");
    content.append("      next.destroy();\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    content.append("}\r\n");

    return PluginProtos.CodeGeneratorResponse.File
      .newBuilder()
      .setName(Utils.absoluteFileName(javaPkgFqn, "ProtoReader"))
      .setContent(content.toString())
      .build();
  }
}
