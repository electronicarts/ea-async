
Maven plugin
---------------

 * Implement or remove inclusion and exclusion options.

Gradle plugin
---------------

 * Create a gradle plugin

Gradle plugin
---------------

 * Create a gradle plugin

Command line
---------------

 * Implement command line instrumenter.
   Example:

       java -cp project_classpath;ea-async.jar com.ea.async.instrumentation.Transformer class_file_or_directory1 class_file_or_directory2
   or

       java -cp project_classpath -jar ea-async.jar class_file_or_directory1 class_file_or_directory2

IDE integration
---------------

 * Intellij IDEA plugin: compile time instrumentation
 * Eclipse plugin: compile time instrumentation


Other features
-----------------

 * Instrument methods that return Object but where all `return` keywords are used with some class derived from CompletionStage.
   This would help with lambdas.

 * Check the generic return type of a function besides the erased type.

 * Unlikely: Instrument methods that don't return task but are called with a wrapper?
   Probably not a good idea.

```java

void someMethod() {
    int a = 1;
    Task<Integer> res = async(()-> a + await(bla1()) + await(bla2()));
    // Equivalent to:
    // Task<Integer> res = async(()-> Task.fromValue(a + await(bla1()) + await(bla2())));
    doSomething(res);
}

void someMethod() {
    int a = 1;
    Task<Integer> res = async(()-> {
        int x = await(bla1());
        int y = await(bla2());
        return a + x + y;  // no task
    });
}

```



