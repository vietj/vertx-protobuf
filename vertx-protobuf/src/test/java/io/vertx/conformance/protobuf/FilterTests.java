package io.vertx.conformance.protobuf;

import com.google.common.io.LineReader;

import java.io.InputStream;
import java.io.InputStreamReader;

public class FilterTests {

  public static void main(String[] args) throws Exception {

    InputStream is = FilterTests.class.getResourceAsStream("/all.txt");

    LineReader reader = new LineReader(new InputStreamReader(is));

    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      if (line.contains("ProtobufInput") || line.contains("JsonOutput") || line.contains("Proto2")) {
        continue;
      }
      line = line.replace("[", "\\[");
      line = line.replace("]", "\\]");
      System.out.println("--test " + line + " \\");
    }

  }

}
