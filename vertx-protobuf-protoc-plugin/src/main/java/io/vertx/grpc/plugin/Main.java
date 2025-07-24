package io.vertx.grpc.plugin;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STGroupString;

public class Main {

  public static void main(String[] args) {

    STGroup group = new STGroupFile("enum.stg");


    ST st = group.getInstanceOf("unit");

    st.add("pkg", "com.foo.bar");
    st.add("name", "Color");
    st.add("constant", "RED");
    st.add("constant", "GREEN");
    st.add("constant", "BLUE");

    String result = st.render();

    System.out.println("result = " + result);


  }
}
