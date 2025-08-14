package io.vertx.grpc.plugin;

import com.google.api.AnnotationsProto;
import com.salesforce.jprotoc.ProtocPlugin;
import io.vertx.protobuf.extension.VertxProto;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.*;
import java.util.concurrent.Callable;

@Command(
  name = "vertx-protobuf-generator",
  mixinStandardHelpOptions = true,
  version = "vertx-protobuf-generator 1.0",
  description = "Generates Vert.x Protobuf code from proto files."
)
public class VertxGrpcGenerator implements Callable<Integer> {
  @Override
  public Integer call() {
    VertxGrpcGeneratorImpl generator = new VertxGrpcGeneratorImpl();
    ProtocPlugin.generate(List.of(generator), List.of(AnnotationsProto.http, VertxProto.vertxJsonObject, VertxProto.vertxDuration));
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new VertxGrpcGenerator()).execute(args);
    System.exit(exitCode);
  }
}
