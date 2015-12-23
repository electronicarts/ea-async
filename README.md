EA Async
============

EA Async implements async-await methods in the JVM.
It allows programmers to write asynchronous code in a sequential fashion.
It was developed by [BioWare](http://www.bioware.com), a division of [Electronic Arts](http://www.ea.com).

If you're looking for async await on the .NET CLR, see [Asynchronous Programming with Async and Await](https://msdn.microsoft.com/en-us/library/hh191443.aspx).

License
=======
EA Async is licensed under the [BSD 3-Clause License](./LICENSE).

Example
=======
#### With EA Async

```java
import static com.ea.async.Async.await;

public class Store
{
    public CompletableFuture<Boolean> buyItem(String itemTypeId, int cost)
    {
        if(!await(getBank().decrement(cost))) {
            return CompletableFuture.fromValue(false);
        }
        await(getInventory().giveItem(itemTypeId));
        return CompletableFuture.fromValue(true);
    }
}
```
In this example `Bank.decrement` returns `CompletableFuture<Boolean>` and `Inventory.giveItem` returns `CompletableFuture<String>`

EA Async rewrites the calls to `Async.await` making your methods non blocking.

The methods look blocking but are actually transformed into asynchronous methods that use
CompletableFutures to continue the execution as intermediary results arrive.

#### Without EA Async

```java
public class Store
{
    public CompletableFuture<Boolean> buyItem(String itemTypeId, int cost)
    {
        return getBank().decrement(cost)
            .thenCompose(result -> {
                if(!result) {
                    return CompletableFuture.fromValue(false);
                }
                return getInventory().giveItem(itemTypeId).thenApply(res -> true);
            });
    }
}
```
This is a small example... A method with a few more CompletableFutures can look very convoluted.

EA-async abstracts away the complexity of the CompletableFutures.

Getting started
---------------

EA Async requires JVM 1.8.x.

It works with java and scala and with most JVM languages.
The only requirement to use EA Async is that must be used only inside methods that return return `CompletableFuture`, `CompletionStage`, or subclasses of `CompletableFuture`.

### Using with maven

```xml
<dependency>
    <groupId>com.ea.async</groupId>
    <artifactId>ea-async</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Instrumenting your code

#### Option 1 - JVM parameter

Start your application with an extra JVM parameter: `-javaagent:ea-async-VERSION.jar`
```
 java -javaagent:ea-async-VERSION.jar -cp your_claspath YourMainClass args...
```

It's recommended to add this as a default option to launchers in intellij projects that use ea-async.  

#### Option 2 - Runtime
On your main class or as early as possible, call at least once:
```
Async.init();
```
Provided that your JVM has the capability enabled, this will start a runtime instrumentation agent.
If you forget to invoke this function, the first call to `await` will initialize the system (and print a warning).

This is a solution for testing and development, it has the least amount of configuration.
It might interfere with jvm debugging. This alternative is present as a fallback.

#### Option 3 - Build time instrumentation, with Maven - Preferred

Use the [ea-async-maven-plugin](maven-plugin). It will instrument your classes in compile time and 
remove all references to `Async.await` and `Async.init()`.

With build time instrumentation our project users won't need to have ea-async in their classpath unless they also choose to use it.

This is the best option for libraries and maven projects.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.ea.async</groupId>
            <artifactId>ea-async-maven-plugin</artifactId>
            <version>${ea-async.version}</version>
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

