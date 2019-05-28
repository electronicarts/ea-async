EA Async
============

[![Release](https://img.shields.io/github/release/electronicarts/ea-async.svg)](https://github.com/electronicarts/ea-async/releases)
[![Maven Central](https://img.shields.io/maven-central/v/com.ea.async/ea-async-parent.svg)](http://repo1.maven.org/maven2/com/ea/async/)
[![Javadocs](https://img.shields.io/maven-central/v/com.ea.async/ea-async.svg?label=Javadocs)](http://www.javadoc.io/doc/com.ea.async/ea-async)
[![Build Status](https://img.shields.io/travis/electronicarts/ea-async.svg)](https://travis-ci.org/electronicarts/ea-async)

EA Async implements Async-Await methods in the JVM.
It allows programmers to write asynchronous code in a sequential fashion.

It is heavily inspired by Async-Await on the .NET CLR, see [Asynchronous Programming with Async and Await](https://msdn.microsoft.com/en-us/library/hh191443.aspx) for more information.

Who should use it?
------

EA Async should be used to write non-blocking asynchronous code that makes heavy use of CompletableFutures or CompletionStage.
It improves scalability by freeing worker threads while your code awaits other processes;
And improves productivity by making asynchronous code simpler and more readable.

Developer & License
======
This project was developed by [Electronic Arts](http://www.ea.com) and is licensed under the [BSD 3-Clause License](LICENSE).

Examples
=======
#### With EA Async

```java
import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class Store
{
    public CompletableFuture<Boolean> buyItem(String itemTypeId, int cost)
    {
        if(!await(bank.decrement(cost))) {
            return completedFuture(false);
        }
        await(inventory.giveItem(itemTypeId));
        return completedFuture(true);
    }
}
```
In this example `Bank.decrement` returns `CompletableFuture<Boolean>` and `Inventory.giveItem` returns `CompletableFuture<String>`

EA Async rewrites the calls to `Async.await` making your methods non-blocking.

The methods look blocking but are actually transformed into asynchronous methods that use
CompletableFutures to continue the execution as intermediary results arrive.

#### Without EA Async

This is how the first example looks without EA Async. It is a bit less readable.

```java
import static java.util.concurrent.CompletableFuture.completedFuture;

public class Store
{
    public CompletableFuture<Boolean> buyItem(String itemTypeId, int cost)
    {
        return bank.decrement(cost)
            .thenCompose(result -> {
                if(!result) {
                    return completedFuture(false);
                }
                return inventory.giveItem(itemTypeId).thenApply(res -> true);
            });
    }
}
```
This is a small example... A method with a few more CompletableFutures can look very convoluted.

EA Async abstracts away the complexity of the CompletableFutures.

#### With EA Async (2)

So you like CompletableFutures?
Try converting this method to use only CompletableFutures without ever blocking (so no joining):

```java
import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class Store
{
    public CompletableFuture<Boolean> buyItem(String itemTypeId, int cost)
    {
        if(!await(bank.decrement(cost))) {
            return completedFuture(false);
        }
        try {
            await(inventory.giveItem(itemTypeId));
            return completedFuture(true);
        } catch (Exception ex) {
            await(bank.refund(cost));
            throw new AppException(ex);
        }
    }
}
```

Got it? Send it [to us](https://github.com/electronicarts/ea-async/issues/new). It probably looks ugly...

Getting started
---------------

EA Async currently supports JDK 8-10.

It works with Java and Scala and should work with most JVM languages.
The only requirement to use EA Async is that must be used only inside methods that return `CompletableFuture`, `CompletionStage`, or subclasses of `CompletableFuture`.

### Using with maven

```xml
<dependency>
    <groupId>com.ea.async</groupId>
    <artifactId>ea-async</artifactId>
    <version>1.2.3</version>
</dependency>
```

### Gradle

```
'com.ea.async:ea-async:1.2.3'
```

### Instrumenting your code

#### Option 1 - JVM parameter

Start your application with an extra JVM parameter: `-javaagent:ea-async-1.2.3.jar`
```
 java -javaagent:ea-async-1.2.3.jar -cp your_claspath YourMainClass args...
```

It's recommended to add this as a default option to launchers in IntelliJ projects that use ea-async.

#### Option 2 - Runtime
On your main class or as early as possible, call at least once:
```
Async.init();
```
Provided that your JVM has the capability enabled, this will start a runtime instrumentation agent.
If you forget to invoke this function, the first call to `await` will initialize the system (and print a warning).

This is a solution for testing and development, it has the least amount of configuration.
It might interfere with JVM debugging. This alternative is present as a fallback.

#### Option 3 - Run instrumentation tool

The ea-async-1.2.3.jar is a runnable jar that can pre-instrument your files.

Usage:

```bash
java -cp YOUR_PROJECT_CLASSPATH -jar ea-async-1.2.3.jar classDirectory
```

Example:

```bash
java -cp guava.jar;commons-lang.jar  -jar ea-async-1.2.3.jar target/classes
```

After that all the files in target/classes will have been instrumented.
There will be no references to `Async.await` and `Async.init` left in those classes.


#### Option 4 - Build time instrumentation, with Maven - Preferred

Use the [ea-async-maven-plugin](maven-plugin). It will instrument your classes in compile time and
remove all references to `Async.await` and `Async.init()`.

With build time instrumentation your project users won't need to have EA Async in their classpath unless they also choose to use it.
This means that EA Async <i>does not need to be a transitive dependency</i>.

This is the best option for libraries and maven projects.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.ea.async</groupId>
            <artifactId>ea-async-maven-plugin</artifactId>
            <version>1.2.3</version>
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

