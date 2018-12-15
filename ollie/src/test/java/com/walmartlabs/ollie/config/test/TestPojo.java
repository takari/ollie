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

import com.typesafe.config.ConfigMemorySize;

public interface TestPojo {

  public boolean isTestBoolean();

  public boolean isTestYesBoolean();

  public long getTestLong();

  public byte getTestByte();

  public int getTestInt();

  public double getTestDouble();

  public float getTestFloat();

  public String getTestString();

  public List<Boolean> getTestListOfBoolean();

  public List<Integer> getTestListOfInteger();

  public List<Double> getTestListOfDouble();

  public List<Long> getTestListOfLong();

  public List<String> getTestListOfString();

  public List<Duration> getTestListOfDuration();

  public List<ConfigMemorySize> getTestListOfSize();

  public List<NestedPojo> getTestListOfNested();

  public Duration getTestDuration();

  public ConfigMemorySize getTestSize();

  public Map<String, Integer> getTestMap();

  public Map<Integer, String> getTestMapIntkey();

  public NestedPojo getTestNestedPojo();

  public String getNullValue();

  public String getMissingValue();
}
