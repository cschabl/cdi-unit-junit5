# cdi-unit-junit5
JUnit 5 Extension for [CDI-Unit](https://github.com/cdi-unit/cdi-unit).

`CdiUnitExtension` of this project is the JUnit 5 counterpart of the JUnit 4 runner `CdiRunner` of CDI-Unit.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.cschabl.cdi-unit-junit5/cdi-unit-junit5/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.cschabl.cdi-unit-junit5/cdi-unit-junit5/)

## Usage

```java
import com.github.cschabl.cdiunit.junit5.CdiUnitExtension;

// PER_CLASS lifecycle is supported:
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(CdiUnitExtension.class) // Runs the test with CDI-Unit
class MyTest {
    @Inject
     MyBean beanUnderTest; // This will be injected before the tests are run!
 
      //The rest of the test goes here.
}
```

## Adding cdi-unit-junit5 to your project

### Prerequisites

* JUnit 5.4 or higher
* CDI-Unit 4.x

### Maven

Add the cdi-unit-junit5 dependency:

```xml
<dependency>
  <groupId>com.github.cschabl.cdi-unit-junit5</groupId>
  <artifactId>cdi-unit-junit5</artifactId>
  <version>0.4</version>
  <scope>test</scope>
</dependency>
```

Make sure you've added the CDI-Unit dependency and the preferred Weld SE dependency:

```xml
<dependency>
  <groupId>io.github.cdi-unit</groupId>
  <artifactId>cdi-unit</artifactId>
  <version>${cdi-unit-version}</version>
  <scope>test</scope>
</dependency>
```

```xml
<dependency>
  <groupId>org.jboss.weld.se</groupId>
  <!-- or weld-se -->
  <artifactId>weld-se-core</artifactId>
  <!-- Your preferred Weld version: -->
  <version>${weld.version}</version>
  <scope>test</scope>
</dependency>
```

And the JUnit 5 dependencies:

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-api</artifactId>
  <version>${junit5-version}</version>
  <scope>test</scope>
</dependency>
```

```xml
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter-engine</artifactId>
  <version>${junit5-version}</version>
  <scope>test</scope>
</dependency>
```

### Gradle

```
dependencies {
    ...
    testImplementation "org.junit.jupiter:junit-jupiter-api:${junit5-version}"
    testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:${junit5-version}"

    testImplementation "org.jglue.cdi-unit:cdi-unit:${cdi-unit-version}"
    testImplementation "org.jboss.weld.se:weld-se-core:${weld-version}"

    testImplementation "com.github.cschabl.cdi-unit-junit5:cdi-unit-junit5:0.4"
    
    ...
}
```

### Restrictions

The following features aren't supported: 

* Nested Tests (@Nested)
* Test class constructors with parameters, i.e. JUnit 5 dependency injection to constructors.
* Probably further JUnit-5-specific features.
* CDI extension `@io.github.cdiunit.ProducerConfig`.
