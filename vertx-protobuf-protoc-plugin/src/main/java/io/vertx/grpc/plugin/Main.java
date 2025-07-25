package io.vertx.grpc.plugin;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STGroupString;

public class Main {

  public static void main(String[] args) {

    STGroup group = new STGroupFile("test.stg");


    ST st = group.getInstanceOf("unit");

    st.add("value", "A");
    st.add("value", "B");
    st.add("value", "C");

    String result = st.render();

    System.out.println("result = " + result);


  }
}
