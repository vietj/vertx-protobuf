package io.vertx.grpc.plugin;

public class GenWriter {

  private StringBuilder content = new StringBuilder();
  private int margin = 0;
  private int cols = 0;

  public GenWriter println() {
    content.append("\r\n");
    cols = 0;
    return this;
  }

  public GenWriter margin(int val) {
    if (val < 0) {
      throw new IllegalArgumentException();
    }
    margin = val;
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
    if (cols == 0) {
      content.append(" ".repeat(margin));
    }
    content.append(s);
    cols += s.length();
    return this;
  }

  @Override
  public String toString() {
    return content.toString();
  }
}
