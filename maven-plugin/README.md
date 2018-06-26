EA Async Maven Plugin
============

EA Async implements async-await methods in the JVM. It allows programmers to write asynchronous code in a sequential fashion. It was developed by [BioWare](http://www.bioware.com), a division of [Electronic Arts](http://www.ea.com).

The EA Async Maven Plugin executes compile time instrumentation of classes that use EA Async.

A sample project can be found [here](src/test/project-to-test/pom.xml).

Documentation
=======
Documentation is located [here](http://orbit.bioware.com/).

License
=======
EA Async is licensed under the [BSD 3-Clause License](../LICENSE).

Usage
=======

Add the EA Async dependency:

```xml
<dependency>
    <groupId>com.ea.async</groupId>
    <artifactId>ea-async</artifactId>
    <version>1.2.0</version>
</dependency>
```

Add the build plugin that will instrument the uses of `await`

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.ea.async</groupId>
            <artifactId>ea-async-maven-plugin</artifactId>
            <version>1.2.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>instrument</goal>
                        <goal>instrument-test</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

