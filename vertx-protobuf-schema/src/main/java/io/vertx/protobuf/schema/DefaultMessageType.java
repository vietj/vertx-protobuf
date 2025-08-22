package io.vertx.protobuf.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

public class DefaultMessageType implements MessageType {

  private final String name;
  private final Map<Integer, DefaultField> fields = new TreeMap<>();
  private final Map<String, DefaultField> byName = new HashMap<>();
  private final Map<String, DefaultField> byJsonName = new HashMap<>();
  private DefaultMessageType enclosingType;
  private Map<String, DefaultOneOf> oneOfs = new TreeMap<>();

  public DefaultMessageType(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  public DefaultMessageType enclosingType(DefaultMessageType enclosingType) {
    this.enclosingType = enclosingType;
    return this;
  }

  @Override
  public MessageType enclosingType() {
    return enclosingType;
  }

  @Override
  public TypeID id() {
    return TypeID.MESSAGE;
  }

  @Override
  public WireType wireType() {
    return WireType.LEN;
  }

  public Collection<DefaultField> fields() {
    return fields.values();
  }

  @Override
  public OneOf oneOf(String name) {
    return oneOfs.get(name);
  }

  @Override
  public Collection<? extends OneOf> oneOfs() {
    return oneOfs.values();
  }

  public DefaultMessageType addOneOf(DefaultOneOf oneOf) {
    if (oneOfs.containsKey(oneOf.name)) {
      throw new IllegalStateException();
    }
    if (oneOf.owner != null) {
      throw new IllegalArgumentException();
    }
    oneOfs.put(oneOf.name(), oneOf);
    oneOf.owner = this;
    return this;
  }

  public DefaultField addField(Consumer<DefaultFieldBuilder> cfg) {
    DefaultFieldBuilder builder = new DefaultFieldBuilder();
    cfg.accept(builder);
    String name = builder.name;
    String jsonName = builder.jsonName;
    if (jsonName == null && name != null) {
      jsonName = DefaultField.toJsonName(builder.name);
    }
    int number = builder.number;
    if (number == 0) {
      throw new IllegalArgumentException();
    }
    boolean packed = builder.packed != null ? builder.packed : builder.repeated;
    DefaultField field = new DefaultField(this, number, name, jsonName, builder.map, builder.mapKey, builder.mapValue, builder.repeated, packed, builder.optional, Objects.requireNonNull(builder.type));
    if (fields.containsKey(number)) {
      throw new IllegalStateException("Duplicate field " + number);
    }
    if (byName.containsKey(name)) {
      throw new IllegalStateException("Duplicate field " + name);
    }
    if (byJsonName.containsKey(jsonName)) {
      throw new IllegalStateException("Duplicate field " + jsonName);
    }
    fields.put(number, field);
    byName.put(name, field);
    byJsonName.put(jsonName, field);
    return field;
  }

  public DefaultField addField(int number, String name, Type type) {
    String jsonName = DefaultField.toJsonName(name);
    DefaultField field = new DefaultField(this, number, name, jsonName, false, false, false, false, false, false, type);
    if (byName.containsKey(name)) {
      throw new IllegalStateException("Duplicate field " + name);
    }
    if (byJsonName.containsKey(jsonName)) {
      throw new IllegalStateException("Duplicate field " + name);
    }
    fields.put(number, field);
    byName.put(name, field);
    byJsonName.put(jsonName, field);
    return field;
  }

  public DefaultField addField(int number, Type type) {
    DefaultField field = new DefaultField(this, number, null, null, false, false, false, false, false, false, type);
    fields.put(number, field);
    return field;
  }

  public DefaultField field(int number) {
    return fields.get(number);
  }

  @Override
  public Field fieldByName(String jsonName) {
    return byName.get(jsonName);
  }

  @Override
  public Field fieldByJsonName(String jsonName) {
    return byJsonName.get(jsonName);
  }

  @Override
  public String toString() {
    return "MessageType[name=" + name + "]";
  }
}
