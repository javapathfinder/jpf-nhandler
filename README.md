jpf-nhandler
============

jpf-nhandler is an extension of Java PathFinder (JPF). It automatically 
delegates the execution of SUT methods from JPF to the host JVM. Execution 
of a call o.m(a) delegated by jpf-nhandler follows three main steps:

  1. It transforms the JPF representation of o and a to the host JVM 
     level.

  2. It delegates the execution to the original (non-native or native) 
     method m by invoking it on the host JVM.

  3. Finally, it transforms the result of the method call back to its 
     JPF representation.

The implementation of jpf-nhandler mostly relies on MJI. jpf-nhandler 
creates bytecode for native peers on-the-fly (they are called OTF peers 
from now on) using the BCEL library. To delegate the execution of a method
to the host JVM, jpf-nhandler adds a method in the corresponding OTF native 
peer which implements the three steps described above.

The main applications of jpf-nhandler:

  1. The key application of jpf-nhandler is to automatically intercept
     and handle native calls within JPF. This extends the JPF functionality 
     considerably, since it allows JPF to verify numerous SUTs on which 
     JPF otherwise would crash.

  2. By using jpf-nhandler, rather than model checking a call, the call 
     is executed outside of JPF, in its normal environment. Hence, this 
     tool can be used to reduce the state space and improve the scalability 
     of JPF.

  3. JPF creates execution traces as it runs the SUT. Long traces can 
     cause JPF to run out of memory. In such cases, jpf-nhandler can be 
     used to delegate methods with long traces, and execute them on the 
     host JVM.

  4. Delegating a method may also speed up JPF.

jpf-nhandler can be configured in variety of ways. Here are some examples:

  - It can be used to skip calls instead of delegating them. In this case 
    methods are executed as if they are empty and they just return some 
    dummy value.

  - It also provides a way to specify which methods are delegated or skipped.
    To force JPF to delegate the constructor of the class a.b.C, use

        nhandler.spec.delegate = a.b.C.<init>

    To force JPF to delegate all method in the String class, use

        nhandler.spec.delegate = java.lang.String.*

    To force JPF to skip java.io.FileDescriptor.write(), use

        nhandler.spec.skip = java.io.FileDescriptor.write

  - jpf-nhandler can also be configured to only delegate native calls which 
    are not handled in JPF

  - jpf-nhandler can be also configured to generate source code for OTF 
    peers on-the-fly, which allows the user to subsequently refine its 
    implementation. Note that you can find bytecode and sources of
    OTF peers in the following directory.

        /jpf-nhandler/onthefly/

    To generate sources, use

        nhandler.genSource = true

    If you refined and edited OTF sources and wish to compile them, run the 
    following command from jpf-nhandler.

        javac -cp "<JPF_HOME>/build/jpf.jar:<NHANDLER_HOME>/build/jpf-nhandler.jar" <NHANDLER_HOME>/onthefly/*.java

  - Since on-the-fly bytecode generation is expensive, one can also configure 
    jpf-nhandler to retain and reuse OTF peers for future runs, i.e. their 
    body may be extended as jpf-nhandler delegates more calls in the future.

    To reuse sources, use

        nhandler.clean = false





Installing of jpf-nhandler
--------------------------

To install jpf-nhandler, follow the steps below.

1. Use [Java](https://www.oracle.com/ca-en/java/technologies/javase/javase8-archive-downloads.html)'s version 8 (we have successfully used 1.8.0_251, 1.8.0_281, and 1.8.0_301).  To check which version of Java (if any) is currently in use, issue the following command.
```
> java -version
java version "1.8.0_251"
Java(TM) SE Runtime Environment (build 1.8.0_251-b08)
Java HotSpot(TM) 64-Bit Server VM (build 25.251-b08, mixed mode)
```

2. To check if any version of [Git](https://git-scm.com/downloads) is currently in use, issue the following command.
```
> git --version
git version 2.26.2.windows.1
```

3. Install [jpf-core](https://github.com/javapathfinder/jpf-core) following the instructions on the [jpf-core wiki](https://github.com/javapathfinder/jpf-core/wiki/How-to-install-JPF). We recommend cloning the master branch using Git and building JPF with the Gradle wrapper. Note that some tests may fail. This should not prevent you from using JPF.

4. Clone jpf-nhandler using Git: go the directory where you want to put jpf-nhandler and issue the following command.
```
> git clone https://github.com/javapathfinder/jpf-nhandler.git
Cloning into 'jpf-nhandler'...
remote: Enumerating objects: 1882, done.
remote: Counting objects: 100% (1882/1882), done.
remote: Compressing objects: 100% (581/581), done.
remote: Total 1882 (delta 1122), reused 1882 (delta 1122), pack-reused 0
Receiving objects: 100% (1882/1882), 6.84 MiB | 1.17 MiB/s, done.
Resolving deltas: 100% (1122/1122), done.
```
5. Build jpf-nhandler with the Gradle wrapper: inside the jpf-nhandler directory, issue the following command.
```
> .\gradlew

> Task :compileJava
C:\Users\montreal\Downloads\jpf\jpf-nhandler\src\main\java\nhandler\conversion\jpf2jvm\JPF2JVMGenericConverter.java:227: warning: sun.reflect.ReflectionFactory is internal proprietary API and may be removed in a future release
        ctor = sun.reflect.ReflectionFactory.getReflectionFactory().newConstructorForSerialization(cl, Object.class.getConstructor());
                          ^
Note: Some input files use unchecked or unsafe operations.
Note: Recompile with -Xlint:unchecked for details.
1 warning

BUILD SUCCESSFUL in 1s
2 actionable tasks: 2 executed
```

6. Run jpf-nhandler's tests: issue the following command.
```
> .\gradlew test

> Task :test

converter.JPF2JVMTest > convertClassTest PASSED

...

on_the_fly.StringTest > testIndexOf PASSED
Test Execution: SUCCESS
Summary: 35 tests, 35 passed, 0 failed, 0 skipped

BUILD SUCCESSFUL in 3s
3 actionable tasks: 2 executed, 1 up-to-date
```

5. Add jpf-nhandler to the file site.properties.  See the [jpf-core wiki](https://github.com/javapathfinder/jpf-core/wiki/Creating-site-properties-file) for details.


Running JPF with jpf-nhandler
-----------------------------

To run JPF on the class Example the *.jpf file includes

    @using = jpf-nhandler

    target = Example

    nhandler.delegateUnhandledNative = true

    classpath = path-to-application-classes

    native_classpath = path-to-application-classes

Note that to use jpf-nhandler, the classes used in the system under test 
should be specified both in classpath and native_classpath. Because the 
execution goes back and forth between JPF and the underlying host JVM, 
both JPF and the host JVM should be able to access these classes.


Configuration Options
---------------------
1.Delegating Methods

Delegate a specific constructor:
```
nhandler.spec.delegate = a.b.C.<init>
```

Delegate all methods of a class:
```
nhandler.spec.delegate = java.lang.String.*
```
2.Skipping Methods

Skip a method (executed as empty and returns a dummy value):
```
nhandler.spec.skip = java.io.FileDescriptor.write
```
3.Delegate Only Unhandled Native Calls
```
nhandler.delegateUnhandledNative = true
```
4.On-the-Fly Native Peers
```
jpf-nhandler generates native peers dynamically.
```
Generated peers are stored in:
```
jpf-nhandler/onthefly/
```
5.Generate Source Code for OTF Peers
```
nhandler.genSource = true
```
6.Compile Generated Sources Manually
```
javac -cp "<JPF_HOME>/build/jpf.jar:<NHANDLER_HOME>/build/jpf-nhandler.jar" onthefly/*.java
```
7.Reuse Generated Peers Across Runs

Since on-the-fly bytecode generation is expensive, peers can be reused:
```
nhandler.clean = false
```


Limitations of jpf-nhandler
---------------------------

  1. Platform-specific classes (e.g., java.lang.System) cannot be handled due to inconsistencies between JPF and the host JVM. 

  2. Delegated objects and classes must have identical fields and superclasses in both environments.

  3. Side effects must be observable only via return values, arguments, or the invoking object.

  4. Objects with native-managed state (e.g., java.awt.Window) cannot be handled.
  5. Methods such as java.util.concurrent.locks.ReentrantLock.lock() cannot be delegated.


Licensing of jpf-nhandler
-------------------------

jpf-nhandler is free software distributed under the
GNU General Public License v3 (or later).

Licenses of third-party JAR files in lib/example are included with the project.

You can find the GNU GPL at:
http://www.gnu.org/licenses

Questions/Feedback
------------------------------

For questions, comments, or suggestions, please contact:

📧 nastaran.shafiei@gmail.com


ACknowledgements
------

Thanks to Peter Mehlitz for his help with the development of jpf-nhandler.
