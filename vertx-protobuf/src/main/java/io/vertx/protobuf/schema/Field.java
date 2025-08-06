package io.vertx.protobuf.schema;

public interface Field {

  static String toJsonName(String fieldName) {
    StringBuilder sb = new StringBuilder(fieldName.length());
    char prev = ' ';
    for (int i = 0;i < fieldName.length();i++) {
      char ch = fieldName.charAt(i);
      boolean upperCase = ch >= 'a' && ch <= 'z' && prev == '_';
      prev = ch;
      if (ch != '_') {
        ch = (char)(upperCase ? ch + ('A' - 'a') : ch);
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  MessageType owner();
  int number();
  Type type();
  boolean isRepeated();
  boolean isPacked();
  String jsonName();
  default boolean isUnknown() {
    return false;
  }
}
