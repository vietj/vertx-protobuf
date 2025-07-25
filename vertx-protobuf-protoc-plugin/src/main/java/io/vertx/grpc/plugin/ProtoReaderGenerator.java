package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public boolean entry;
    public String identifier;
    public String javaType;
    public boolean repeated;
    public String getterMethod;
    public String setterMethod;
    public String containingJavaType;
    public Function<String, String> converter;
    public boolean imported;
  }

  public PluginProtos.CodeGeneratorResponse.File generate() {

    String javaPkgFqn = Utils.extractJavaPkgFqn(fileDesc.toProto());


    Map<String, Descriptors.Descriptor> all = Utils.transitiveClosure(fileDesc.getMessageTypes());



    List<FieldDescriptor> collected = new ArrayList<>();
    for (Descriptors.Descriptor mt : all.values()) {
      for (Descriptors.FieldDescriptor fd : mt.getFields()) {

        FieldDescriptor descriptor = new FieldDescriptor();

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
        descriptor.type = fd.getType();
        descriptor.kind = kind;
        descriptor.map = fd.isMapField();
        descriptor.entry = fd.getContainingType().toProto().getOptions().getMapEntry();
        descriptor.identifier = Utils.schemaLiteralOf(fd);
        descriptor.javaType = Utils.javaTypeOf(fd);
        descriptor.repeated = fd.isRepeated();
        descriptor.getterMethod = Utils.getterOf(fd);
        descriptor.setterMethod = Utils.setterOf(fd);
        descriptor.containingJavaType = Utils.javaTypeOf(fd.getContainingType());
        descriptor.converter = converter;
        descriptor.imported = fd.getType() == Descriptors.FieldDescriptor.Type.MESSAGE && fd.getMessageType().getFile() != fileDesc;
        collected.add(descriptor);
      }
    }

    List<Descriptors.FieldDescriptor> messageFields = all.values().stream()
      .flatMap(mt -> mt.getFields().stream()).filter(f -> f.getType() == Descriptors.FieldDescriptor.Type.MESSAGE).collect(Collectors.toList());

    StringBuilder content = new StringBuilder();





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
    content.append("    ");
    for (Descriptors.Descriptor messageType : fileDesc.getMessageTypes()) {
      content.append("if (type == SchemaLiterals.").append(Utils.schemaLiteralOf(messageType)).append(") {\r\n");
      content.append("      stack.push(new ").append(messageType.getName()).append("());\r\n");
      content.append("    } else ");
    }
    content.append("if (next != null) {\r\n");
    content.append("      next.init(type);\r\n");
    content.append("    }\r\n");
    content.append("  }\r\n");

    // **************
    // VISIT STRING
    // **************

    class VisitMethod {
      final String methodStart;
      final Set<Descriptors.FieldDescriptor.Type> types;
      final String next;

      VisitMethod(String methodStart, String next, Descriptors.FieldDescriptor.Type... types) {
        this.methodStart = methodStart;
        this.types = new HashSet<>(Arrays.asList(types));
        this.next = next;
      }
    }

    VisitMethod[] visitMethods = {
      new VisitMethod("visitBytes(Field field, byte[] value)", "visitBytes(field, value)", Descriptors.FieldDescriptor.Type.BYTES),
      new VisitMethod("visitString(Field field, String value)", "visitString(field, value)", Descriptors.FieldDescriptor.Type.STRING),
      new VisitMethod("visitDouble(Field field, double value)", "visitDouble(field, value)", Descriptors.FieldDescriptor.Type.DOUBLE),
      new VisitMethod("visitVarInt32(Field field, int value)", "visitVarInt32(field, value)", Descriptors.FieldDescriptor.Type.BOOL, Descriptors.FieldDescriptor.Type.ENUM, Descriptors.FieldDescriptor.Type.INT32)
    };

    for (VisitMethod visitMethod : visitMethods) {
      content.append("  public void ").append(visitMethod.methodStart).append(" {\r\n");
      content.append("    ");
      for (FieldDescriptor fd : collected.stream().filter(f -> visitMethod.types.contains(f.type)).collect(Collectors.toList())) {
        content.append("if (field == SchemaLiterals.").append(fd.identifier).append(") {\r\n");
        if (fd.entry) {
          content.append("      stack.push(value);\r\n");
        } else if (fd.repeated) {
          content.append("      ").append(fd.containingJavaType).append(" blah = (").append(fd.containingJavaType).append(")stack.peek()").append(";\t\n");
          content.append("      if (blah.").append(fd.getterMethod).append("() == null) {\r\n");
          content.append("        blah.").append(fd.setterMethod).append("(new java.util.ArrayList<>());\r\n");
          content.append("      }\r\n");
          content.append("      blah.").append(fd.getterMethod).append("().add(").append(fd.converter.apply("value")).append(");\t\n");
        } else {
          content.append("      ((").append(fd.containingJavaType).append(")stack.peek()).").append(fd.setterMethod).append("(").append(fd.converter.apply("value")).append(");\t\n");
        }
        content.append("    } else ");
      }
      content.append("if (next != null) {\r\n");
      content.append("      next.").append(visitMethod.next).append(";\r\n");
      content.append("    } else {\r\n");
      content.append("      throw new UnsupportedOperationException();\r\n");
      content.append("    }\r\n");
      content.append("  }\r\n");
    }

    // **************
    // ENTER
    // **************

    content.append("  public void enter(Field field) {\r\n");
    content.append("    ");
    for (Descriptors.FieldDescriptor field : messageFields) {
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
      content.append("    } else ");
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
    content.append("    ");
    collected
      .stream()
      .filter(field -> field.type == Descriptors.FieldDescriptor.Type.MESSAGE)
      .forEach(field -> {
        content.append("if (field == SchemaLiterals.").append(field.identifier).append(") {\r\n");
        if (field.map) {
          content.append("      Object value = stack.pop();\r\n");
          content.append("      Object key = stack.pop();\r\n");
          content.append("      java.util.Map entries = (java.util.Map)stack.pop();\r\n");
          content.append("      entries.put(key, value);\r\n");
        } else if (field.entry) {
          content.append("      //\r\n");
        } else {
          if (field.imported) {
            content.append("      next.destroy();\r\n");
            content.append("      next = null;\r\n");
          }
          content.append("      ").append(field.javaType).append(" v = (").append(field.javaType).append(")stack.pop();\r\n");
          content.append("      ((").append(field.containingJavaType).append(")stack.peek()).").append(field.setterMethod).append("(v);\n");
        }
        content.append("    } else ");
    });
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
