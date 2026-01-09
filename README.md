# jpf-nhandler

jpf-nhandler is an extension of **Java PathFinder (JPF)** that automatically delegates the execution of selected methods from JPF to the host JVM.

When a method call `o.m(a)` is delegated, jpf-nhandler performs the following steps:

1. Transforms the JPF representation of the object `o` and arguments `a` to their host JVM representations.
2. Executes the original (native or non-native) method `m` on the host JVM.
3. Transforms the result of the method call back to its JPF representation.

The implementation of jpf-nhandler primarily relies on **MJI (Model Java Interface)**. Native peers are generated on-the-fly using the **BCEL** library. These generated peers are referred to as **on-the-fly (OTF) peers**.

---

## Main Applications

jpf-nhandler is mainly used for the following purposes:

1. **Handling native calls automatically**  
   jpf-nhandler intercepts and handles native calls within JPF, allowing JPF to verify systems that would otherwise crash.

2. **Reducing state space**  
   Delegated method calls are executed outside JPF in the host JVM, which can significantly reduce the state space and improve scalability.

3. **Avoiding memory issues**  
   Long execution traces may cause JPF to run out of memory. Delegating such methods avoids trace explosion.

4. **Performance improvements**  
   Delegating methods may also speed up JPF execution.

---

## Requirements

- **Java 11 (recommended)**
  - Java 8 may still work, but Java 11 is the primary supported version.
- Git
- Gradle (via the included Gradle wrapper)
- `jpf-core` installed and built

To check your Java version:

```bash
java -version
