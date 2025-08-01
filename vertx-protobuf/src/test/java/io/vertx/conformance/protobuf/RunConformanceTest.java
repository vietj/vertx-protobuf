package io.vertx.conformance.protobuf;

import com.google.common.io.LineReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class RunConformanceTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() throws Exception {
    InputStream is = FilterTests.class.getResourceAsStream("/all.txt");
    LineReader reader = new LineReader(new InputStreamReader(is));
    List<Object[]> list = new ArrayList<>();
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      if (line.contains("JsonInput") || line.contains("JsonOutput") || line.contains("Proto2")) {
        continue;
      }
      line = line.replace("[", "\\[");
      line = line.replace("]", "\\]");
      System.out.println("--test " + line + " \\");
      list.add(new Object[] { line });
      break;  // Keep one line for now
    }
    return list;
  }

  private final String test;

  public RunConformanceTest(String test) {
    this.test = test;
  }

  @Test
  public void testConformance() throws Exception {
    String conformance = "\"/Users/julien/java/vertx-protobuf/vertx-protobuf/conformance.sh";
    ProcessBuilder processBuilder = new ProcessBuilder(
      "/Users/julien/java/vertx-protobuf/vertx-protobuf/conformance_test_runner",
      "--maximum_edition", "PROTO3",
      "--output_dir", ".",
      "--test", test,
      conformance);
    processBuilder.redirectOutput();
    Process process = processBuilder.start();
    InputStream out = process.getErrorStream();
    Thread th = new Thread(() -> {
      byte[] buffer = new byte[256];
      try {
        while (true) {
          int amount = out.read(buffer, 0, 256);
          if (amount == -1) {
            break;
          }
          System.out.write(buffer, 0, amount);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    th.start();
    int i = process.waitFor();
    th.join();
  }
}
