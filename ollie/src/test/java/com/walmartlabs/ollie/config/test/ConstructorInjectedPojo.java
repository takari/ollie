package com.walmartlabs.ollie.config.test;

/*-
 * *****
 * Ollie
 * -----
 * Copyright (C) 2018 Takari
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.ConfigMemorySize;
import com.walmartlabs.ollie.config.Config;
import org.eclipse.sisu.Nullable;

@Singleton
public class ConstructorInjectedPojo implements TestPojo {

  private final boolean testBoolean;
  private final boolean testYesBoolean;
  private final long testLong;
  private final byte testByte;
  private final int testInt;
  private final double testDouble;
  private final float testFloat;
  private final String testString;
  private final List<Boolean> testListOfBoolean;
  private final List<Integer> testListOfInteger;
  private final List<Double> testListOfDouble;
  private final List<Long> testListOfLong;
  private final List<String> testListOfString;
  private final List<Duration> testListOfDuration;
  private final List<ConfigMemorySize> testListOfSize;
  private final List<NestedPojo> testListOfNested;
  private final Duration testDuration;
  private final ConfigMemorySize testSize;
  private final Map<String, Integer> testMap;
  private final Map<Integer, String> testMapIntkey;
  private final NestedPojo testNestedPojo;
  private final String nullValue;
  private final String missingValue;

  @Inject
  public ConstructorInjectedPojo(
    @Config("constructor.boolean") boolean testBoolean,
    @Config("constructor.boolean") boolean testBooleanAgain,
    @Config("constructor.yesBoolean") boolean testYesBoolean,
    @Config("constructor.long") long testLong,
    @Config("constructor.byte") byte testByte,
    @Config("constructor.int") int testInt,
    @Config("constructor.double") double testDouble,
    @Config("constructor.float") float testFloat,
    @Config("constructor.string") String testString,
    @Config("constructor.list.boolean") List<Boolean> testListOfBoolean,
    @Config("constructor.list.integer") List<Integer> testListOfInteger,
    @Config("constructor.list.double") List<Double> testListOfDouble,
    @Config("constructor.list.long") List<Long> testListOfLong,
    @Config("constructor.list.string") List<String> testListOfString,
    @Config("constructor.list.duration") List<Duration> testListOfDuration,
    @Config("constructor.list.size") List<ConfigMemorySize> testListOfSize,
    @Config("constructor.list.nested") List<NestedPojo> testListOfNested,
    @Config("constructor.duration") Duration testDuration,
    @Config("constructor.size") ConfigMemorySize testSize,
    @Config("constructor.map") Map<String, Integer> testMap,
    @Config("constructor.map.intkey") Map<Integer, String> testMapIntkey,
    @Config("constructor.nested") NestedPojo testNestedPojo,
    @Config("constructor.nullValue") @Nullable String testNullValue,
    @Config("constructor.missingValue") @Nullable String testMissingValue) {
    this.testBoolean = testBoolean;
    this.testYesBoolean = testYesBoolean;
    this.testLong = testLong;
    this.testByte = testByte;
    this.testInt = testInt;
    this.testDouble = testDouble;
    this.testFloat = testFloat;
    this.testString = testString;
    this.testListOfBoolean = testListOfBoolean;
    this.testListOfInteger = testListOfInteger;
    this.testListOfDouble = testListOfDouble;
    this.testListOfLong = testListOfLong;
    this.testListOfString = testListOfString;
    this.testListOfDuration = testListOfDuration;
    this.testListOfSize = testListOfSize;
    this.testListOfNested = testListOfNested;
    this.testDuration = testDuration;
    this.testSize = testSize;
    this.testMap = testMap;
    this.testMapIntkey = testMapIntkey;
    this.testNestedPojo = testNestedPojo;
    this.nullValue = testNullValue;
    this.missingValue = testMissingValue;
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

  @Override
  public String getNullValue() {
    return nullValue;
  }

  @Override
  public String getMissingValue() {
    return missingValue;
  }
}
