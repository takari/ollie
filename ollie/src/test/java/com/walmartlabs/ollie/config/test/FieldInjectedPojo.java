package com.walmartlabs.ollie.config.test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.ConfigMemorySize;
import com.walmartlabs.ollie.config.Config;
import org.eclipse.sisu.Nullable;

@Singleton
public class FieldInjectedPojo implements TestPojo {

  @Inject
  @Config("field.boolean")
  boolean testBoolean;
  @Inject
  @Config("field.yesBoolean")
  boolean testYesBoolean;
  @Inject
  @Config("field.long")
  long testLong;
  @Inject
  @Config("field.byte")
  byte testByte;
  @Inject
  @Config("field.int")
  int testInt;
  @Inject
  @Config("field.double")
  double testDouble;
  @Inject
  @Config("field.float")
  float testFloat;
  @Inject
  @Config("field.string")
  String testString;
  @Inject
  @Config("field.list.boolean")
  List<Boolean> testListOfBoolean;
  @Inject
  @Config("field.list.integer")
  List<Integer> testListOfInteger;
  @Inject
  @Config("field.list.double")
  List<Double> testListOfDouble;
  @Inject
  @Config("field.list.long")
  List<Long> testListOfLong;
  @Inject
  @Config("field.list.string")
  List<String> testListOfString;
  @Inject
  @Config("field.list.duration")
  List<Duration> testListOfDuration;
  @Inject
  @Config("field.list.size")
  List<ConfigMemorySize> testListOfSize;
  @Inject
  @Config("field.list.nested")
  List<NestedPojo> testListOfNested;
  @Inject
  @Config("field.duration")
  Duration testDuration;
  @Inject
  @Config("field.size")
  ConfigMemorySize testSize;
  @Inject
  @Config("field.map")
  Map<String, Integer> testMap;
  @Inject
  @Config("field.map.intkey")
  Map<Integer, String> testMapIntkey;
  @Inject
  @Config("field.nested")
  NestedPojo testNestedPojo;
  @Inject
  @Config("field.nullValue")
  @Nullable
  String nullValue;
  @Inject
  @Config("field.missingValue")
  @Nullable
  String missingValue;

  public void setTestBoolean(boolean testBoolean) {
    this.testBoolean = testBoolean;
  }

  public boolean isTestBoolean() {
    return testBoolean;
  }

  public boolean isTestYesBoolean() {
    return testYesBoolean;
  }

  public long getTestLong() {
    return testLong;
  }

  public byte getTestByte() {
    return testByte;
  }

  public int getTestInt() {
    return testInt;
  }

  public double getTestDouble() {
    return testDouble;
  }

  public float getTestFloat() {
    return testFloat;
  }

  public String getTestString() {
    return testString;
  }

  public List<Boolean> getTestListOfBoolean() {
    return testListOfBoolean;
  }

  public List<Integer> getTestListOfInteger() {
    return testListOfInteger;
  }

  public List<Double> getTestListOfDouble() {
    return testListOfDouble;
  }

  public List<Long> getTestListOfLong() {
    return testListOfLong;
  }

  public List<String> getTestListOfString() {
    return testListOfString;
  }

  public List<Duration> getTestListOfDuration() {
    return testListOfDuration;
  }

  public List<ConfigMemorySize> getTestListOfSize() {
    return testListOfSize;
  }

  public List<NestedPojo> getTestListOfNested() {
    return testListOfNested;
  }

  public Duration getTestDuration() {
    return testDuration;
  }

  public ConfigMemorySize getTestSize() {
    return testSize;
  }

  public Map<String, Integer> getTestMap() {
    return testMap;
  }

  public Map<Integer, String> getTestMapIntkey() {
    return testMapIntkey;
  }

  public NestedPojo getTestNestedPojo() {
    return testNestedPojo;
  }

  public String getNullValue() {
    return nullValue;
  }

  public String getMissingValue() {
    return missingValue;
  }
}
