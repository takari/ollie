package com.walmartlabs.ollie.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
//import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigMemorySize;
import com.walmartlabs.ollie.config.Config;
import com.walmartlabs.ollie.config.ConfigurationModule;
import com.walmartlabs.ollie.config.test.ConstructorInjectedPojo;
import com.walmartlabs.ollie.config.test.FieldInjectedPojo;
import com.walmartlabs.ollie.config.test.MethodInjectedPojo;
import com.walmartlabs.ollie.config.test.NestedPojo;
import com.walmartlabs.ollie.config.test.ProvidedPojo;
import com.walmartlabs.ollie.config.test.TestPojo;

public class ConfigurationModuleTest {

  private Injector injector;

  @Before
  public void setup() {
    com.typesafe.config.Config testConf = ConfigFactory.load("conf/test.conf");
    Module testModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(ConstructorInjectedPojo.class).asEagerSingleton();
        bind(FieldInjectedPojo.class).asEagerSingleton();
        bind(MethodInjectedPojo.class).asEagerSingleton();
      }

      @Provides
      @Singleton
      ProvidedPojo providePojo(
        @Config("provided.boolean") boolean testBoolean,
        @Config("provided.yesBoolean") boolean testYesBoolean,
        @Config("provided.long") long testLong,
        @Config("provided.byte") byte testByte,
        @Config("provided.int") int testInt,
        @Config("provided.double") double testDouble,
        @Config("provided.float") float testFloat,
        @Config("provided.string") String testString,
        @Config("provided.list.boolean") List<Boolean> testListOfBoolean,
        @Config("provided.list.integer") List<Integer> testListOfInteger,
        @Config("provided.list.double") List<Double> testListOfDouble,
        @Config("provided.list.long") List<Long> testListOfLong,
        @Config("provided.list.string") List<String> testListOfString,
        @Config("provided.list.duration") List<Duration> testListOfDuration,
        @Config("provided.list.size") List<ConfigMemorySize> testListOfSize,
        @Config("provided.list.nested") List<NestedPojo> testListOfNested,
        @Config("provided.duration") Duration testDuration,
        @Config("provided.size") ConfigMemorySize testSize,
        @Config("provided.map") Map<String, Integer> testMap,
        @Config("provided.map.intkey") Map<Integer, String> testMapIntkey,
        @Config("provided.nested") NestedPojo testNestedPojo) {
        return new ProvidedPojo(testBoolean, testYesBoolean, testLong, testByte, testInt, testDouble, testFloat, testString, testListOfBoolean, testListOfInteger, testListOfDouble, testListOfLong,
          testListOfString, testListOfDuration, testListOfSize, testListOfNested, testDuration, testSize, testMap, testMapIntkey, testNestedPojo);
      }
    };

    injector = Guice.createInjector(
      ConfigurationModule.fromConfigWithPackage(testConf, "com.walmartlabs.ollie.config"),
      testModule);
  }

  @Test
  public void canInjectPojoViaConstructor() {
    ConstructorInjectedPojo pojo = injector.getInstance(ConstructorInjectedPojo.class);
    assertPojoIsCorrect(pojo);
  }

  @Test
  public void canInjectPojoViaFields() {
    FieldInjectedPojo pojo = injector.getInstance(FieldInjectedPojo.class);
    assertPojoIsCorrect(pojo);
  }

  @Test
  public void canInjectPojoViaMethods() {
    MethodInjectedPojo pojo = injector.getInstance(MethodInjectedPojo.class);
    assertPojoIsCorrect(pojo);
  }

  @Test
  public void canGetProvidedPojo() {
    ProvidedPojo pojo = injector.getInstance(ProvidedPojo.class);
    assertPojoIsCorrect(pojo);
  }

  private void assertPojoIsCorrect(TestPojo pojo) {
    Assert.assertTrue(pojo.isTestBoolean());
    Assert.assertTrue(pojo.isTestYesBoolean());
    Assert.assertEquals(12345679123l, pojo.getTestLong());
    Assert.assertEquals(1, pojo.getTestInt());
    Assert.assertEquals(123, pojo.getTestByte());
    Assert.assertEquals(2.0, pojo.getTestDouble(), 0.001);
    Assert.assertEquals(2.0f, pojo.getTestFloat(), 0.001);
    Assert.assertEquals("test", pojo.getTestString());
    Assert.assertEquals(Duration.of(10, ChronoUnit.SECONDS), pojo.getTestDuration());
    Assert.assertEquals(ConfigMemorySize.ofBytes(524288), pojo.getTestSize());

    NestedPojo nestedListPojo = pojo.getTestListOfNested().get(0);
    Assert.assertEquals(3, nestedListPojo.getNestInt());

    Map<String, Integer> testMap = pojo.getTestMap();
    Assert.assertEquals(1, testMap.get("one").intValue());

    Map<Integer, String> testMapIntkey = pojo.getTestMapIntkey();
    Assert.assertEquals("one", testMapIntkey.get("1"));

    Assert.assertEquals(Arrays.asList(true, false, true), pojo.getTestListOfBoolean());
    Assert.assertEquals(Arrays.asList(1, 2, 3), pojo.getTestListOfInteger());
    Assert.assertEquals(Arrays.asList(1.1, 2.2, 3.3), pojo.getTestListOfDouble());
    Assert.assertEquals(Arrays.asList(12345679121L, 12345679122L, 12345679123L), pojo.getTestListOfLong());
    Assert.assertEquals(Arrays.asList("a", "b", "c"), pojo.getTestListOfString());
    Assert.assertEquals(Arrays.asList(Duration.of(1, ChronoUnit.SECONDS), Duration.of(2, ChronoUnit.SECONDS), Duration.of(3, ChronoUnit.SECONDS)), pojo.getTestListOfDuration());
    Assert.assertEquals(Arrays.asList(ConfigMemorySize.ofBytes(524288), ConfigMemorySize.ofBytes(1048576), ConfigMemorySize.ofBytes(1073741824)), pojo.getTestListOfSize());
  }
}
