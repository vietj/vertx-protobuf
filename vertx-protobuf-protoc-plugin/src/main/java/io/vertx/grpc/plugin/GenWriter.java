package io.vertx.grpc.plugin;

import java.util.Spliterators;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GenWriter {

  private StringBuilder content = new StringBuilder();

  public GenWriter println() {
    content.append("\r\n");
    return this;
  }

  public GenWriter println(String s) {
    print(s);
    println();
    return this;
  }

  public GenWriter println(String... m) {
    for (String s : m) {
      println(s);
    }
    return this;
  }

  public GenWriter print(String s) {
    content.append(s);
    return this;
  }

  @Override
  public String toString() {
    return content.toString();
  }
}
