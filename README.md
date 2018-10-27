# :fishing_pole_and_fish: Annotated

[![jitpack][jitpack]][jitpack-url]
[![tests][tests]][tests-url]
[![license][license]][license-url]

**Annotated** is an easy to use Java API to redefine class, methods and fields annotations at runtime.

## Features

- Flexible and simple API built for all specific use cases
- Supports annotation construction from a `Map<String, Object>` representation of the [annotation elements](https://docs.oracle.com/javase/tutorial/java/annotations/basics.html)
- High performance: all reflection objects are grabbed and cached during the JVM startup

## Getting started

Install Annotated using [`Maven`](https://maven.apache.org/) by adding the JitPack repository to your `pom.xml` file:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Next, add the `Annotated` dependency:

```xml
<dependency>
    <groupId>com.github.hugmanrique</groupId>
    <artifactId>Annotated</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

Please note Annotated only supports **Java 8**.

## Documentation

> TODO

Additional documentation for individual features can be found in the [Javadoc](https://jitpack.io/com/github/hugmanrique/Annotated/master-SNAPSHOT/javadoc/). For additional help, you can create an issue and I will try to respond to it as fast as I can.

## Building Annotated

Annotated uses [Maven](https://maven.apache.org/). To perform a build, execute `mvn package` from within the project root directory.

# License

[MIT](LICENSE) &copy; [Hugo Manrique](https://hugmanrique.me)

[jitpack]: https://jitpack.io/v/hugmanrique/Annotated.svg
[jitpack-url]: https://jitpack.io/#hugmanrique/Annotated
[tests]: https://img.shields.io/travis/hugmanrique/Annotated/master.svg
[tests-url]: https://travis-ci.org/hugmanrique/Annotated
[license]: https://img.shields.io/github/license/hugmanrique/Annotated.svg
[license-url]: LICENSE
