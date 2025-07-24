package io.vertx.grpc.plugin;

import com.google.protobuf.Descriptors;
import com.google.protobuf.compiler.PluginProtos;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STGroupString;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

class EnumGenerator {

  private final Descriptors.FileDescriptor fileDesc;

  public EnumGenerator(Descriptors.FileDescriptor fileDesc) {
    this.fileDesc = fileDesc;
  }

  public List<PluginProtos.CodeGeneratorResponse.File> generate() {
    String javaPkgFqn = Utils.extractJavaPkgFqn(fileDesc.toProto());
    return fileDesc.getEnumTypes()
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
    STGroup group = new STGroupFile("enum.stg");
    ST st = group.getInstanceOf("unit");
    st.add("pkg", javaPkgFqn);
    st.add("name", enumType.getName());
    for (Descriptors.EnumValueDescriptor enumValue : enumType.getValues()) {
      Constant constant = new Constant(enumValue.getName(), enumValue.getIndex());
      st.add("constant", constant);
    }
    String result = st.render();
    return Utils.buildFile(javaPkgFqn, enumType, result);
  }
}
