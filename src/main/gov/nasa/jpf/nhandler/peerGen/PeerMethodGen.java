package gov.nasa.jpf.nhandler.peerGen;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.NativeMethodInfo;
import gov.nasa.jpf.jvm.ThreadInfo;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.Type;

/**
 * Creates the body of the methods within the native peer class using the Byte
 * Code Engineering Library (BCEL).
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class PeerMethodGen {

  private InstructionList il;

  private NativeMethodInfo mi;

  private MethodGen nativeMth;

  private String name;

  private ThreadInfo ti;

  private MJIEnv env;

  private PeerClassGen peerCreator;

  private static final int methodAcc = Constants.ACC_PUBLIC | Constants.ACC_STATIC;

  private static final String conversionPkg = "gov.nasa.jpf.nhandler.conversion";

  /**
   * Creates a new instance of the PeerMethodCreator class.
   * 
   * @param mi
   *          an object that represents a native method in JPF
   * @param env
   *          an interface between JPF and the underlying JVM
   * 
   * @param pc
   *          a PeerClassCreator object that corresponds to the class of the
   *          given method
   */
  public PeerMethodGen (NativeMethodInfo mi, MJIEnv env, PeerClassGen pc) {
    this.peerCreator = pc;
    this.il = new InstructionList();
    this.mi = mi;
    this.name = mi.getJNIName();
    this.ti = env.getThreadInfo();
    this.env = env;
    Type returnType = PeerMethodGen.getType(mi.getReturnTypeName());
    this.nativeMth = new MethodGen(methodAcc, (returnType.equals(Type.OBJECT)) ? Type.INT : returnType, PeerMethodGen.getArgumentsType(mi), PeerMethodGen.getArgumentsName(mi), name, PeerClassGen.getNativePeerClsName(mi.getClassName()), il, peerCreator._cp);
  }

  /**
   * Creates bytecode for the body of the method and adds it to the peer class
   * of this method.
   */
  public void create (){
    this.addException();
    int converter = this.createConverter();
    int caller = this.createCaller(converter);
    int argValue = this.createArgValue(converter);
    int argType = this.createArgType(argValue);
    int callerClass = this.getCallerClass(caller);
    int method = this.getMethod(callerClass, argType);
    this.setAccessible(method);
    int returnValue = this.invokeMethod(caller, method, argValue);
    int jpfReturnValue = -1;

    if (!mi.getReturnTypeName().equals("void")){
      if (!PeerMethodGen.isPrimitiveType(this.mi.getReturnTypeName()))
        // If the method is not of type void, converts returnValue to a JPF
        // object
        jpfReturnValue = this.convertJVM2JPF(converter, returnValue);
      else
        jpfReturnValue = returnValue;
    }

    if (mi.isStatic())
      this.getJPFClass(converter, callerClass);
    else
      this.updateJPFObj(converter, caller, 1);

    this.updateJPFArguments(converter, argValue);
    this.addReturnStatement(jpfReturnValue);

    this.nativeMth.setMaxStack();
    this.nativeMth.setMaxLocals();
    peerCreator._cg.addMethod(this.nativeMth.getMethod());
    this.il.dispose();
  }

  /**
   * Creates bytecode for the empty method and adds it to the peer class
   * of this method.
   */
  public void createEmpty (){
    this.addDummyReturnStatement();

    this.nativeMth.setMaxStack();
    this.nativeMth.setMaxLocals();
    peerCreator._cg.addMethod(this.nativeMth.getMethod());
    this.il.dispose();
  }

  /**
   * Adds bytecode to the body of the method for exceptions that are possibly
   * thrown by this method. Adds "throws SecurityException,
   * NoSuchMethodException, IllegalAccessException" to the declaration of the
   * method.
   */
  private void addException (){
    this.nativeMth.addException("java.lang.IllegalArgumentException");
    this.nativeMth.addException("java.lang.SecurityException");
    this.nativeMth.addException("java.lang.NoSuchMethodException");
    this.nativeMth.addException("java.lang.IllegalAccessException");
    this.nativeMth.addException(conversionPkg + ".ConversionException");
    // It throws NoClassDefFoundError exception while loading OTF peers. I
    // just exclude it for now! But it has to be fixed
    // this.nativeMth.addException("java.lang.InvocationTargetException");
  }

  /**
   * Adds bytecode to the body of the method that creates an instance of the
   * Converter class. Adds "Converter converter = new Converter(env)" to the
   * body of the method.
   * 
   * @return an index of the local variable that represents the Converter object
   */
  private int createConverter (){
    this.il.append(peerCreator._factory.createNew(conversionPkg + ".Converter"));
    // Duplicate the top operand stack value
    this.il.append(InstructionConstants.DUP);
    // Load from local variable
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "<init>", Type.VOID, new Type[] { new ObjectType("gov.nasa.jpf.jvm.MJIEnv") }, Constants.INVOKESPECIAL));
    // Store into local variable
    LocalVariableGen lg = this.nativeMth.addLocalVariable("converter", new ObjectType(conversionPkg + ".Converter"), null, null);
    int converter = lg.getIndex();
    this.il.append(InstructionFactory.createStore(Type.OBJECT, converter));
    return converter;
  }

  /**
   * For the non-static method, adds bytecode to the body of the method that
   * creates a new instance of the object invoking the native method. For static
   * method, adds bytecode to the body of the method that creates an instance of
   * the Class class representing the class invoking the native method.
   * 
   * @param converter
   *          an index of the local variable that represents the Converter
   *          object
   * 
   * @return an index of the local variable that represents the caller object
   *         (non-static method) or class (static method)
   */
  private int createCaller (int converter){
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, converter));
    this.il.append(InstructionFactory.createLoad(Type.INT, 1));
    LocalVariableGen lg;

    if (this.mi.isStatic()){
      this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "getJVMCls", new ObjectType("java.lang.Class"), new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
      lg = this.nativeMth.addLocalVariable("caller", new ObjectType("java.lang.Class"), null, null);
    } else{
      this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "getJVMObj", Type.OBJECT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
      lg = this.nativeMth.addLocalVariable("caller", Type.OBJECT, null, null);
    }
    int caller = lg.getIndex();
    this.il.append(InstructionFactory.createStore(Type.OBJECT, caller));
    return caller;
  }

  /**
   * Adds bytecode to the body of the method that creates an array of type
   * Object including the arguments values of the method.
   * 
   * @param converter
   *          an index of the local variable that represents the Converter
   *          object
   * 
   * @return an index of the local variable which is an array of type Object
   *         including the arguments values of the method
   */
  private int createArgValue (int converter){
    String[] argTypes = this.mi.getArgumentTypeNames();
    int nArgs = argTypes.length;

    /** Create an array of objects (Object[] args = new Objects[nArgs]) */
    this.il.append(new PUSH(peerCreator._cp, nArgs));
    this.il.append(peerCreator._factory.createNewArray(Type.OBJECT, (short) 1));
    LocalVariableGen lg = this.nativeMth.addLocalVariable("argValue", new ArrayType(Type.OBJECT, 1), null, null);
    int argValue = lg.getIndex();
    this.il.append(InstructionFactory.createStore(Type.OBJECT, argValue));

    /** Setting args elements to the arguments of the native method */
    // To skip the first and second arguements (MJIEnv & objRef/clsRef)
    int j = 2;
    for (int i = 0; i < nArgs; i++){
      // Loading the array element args[i];
      this.il.append(InstructionFactory.createLoad(Type.OBJECT, argValue));
      this.il.append(new PUSH(peerCreator._cp, i));
      // if the current argument representing an object
      if (!PeerMethodGen.isPrimitiveType(argTypes[i])){
        this.il.append(InstructionFactory.createLoad(Type.OBJECT, converter));
        this.il.append(InstructionFactory.createLoad(Type.INT, j));
        this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "getJVMObj", Type.OBJECT, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        j++;
      }
      // if the current argument representing a primitive type we create the
      // corresponding wrapper class
      else if ("boolean".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Boolean"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.BOOLEAN, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Boolean", "<init>", Type.VOID, new Type[] { Type.BOOLEAN }, Constants.INVOKESPECIAL));
        j++;
      } else if ("int".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Integer"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.INT, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Integer", "<init>", Type.VOID, new Type[] { Type.INT }, Constants.INVOKESPECIAL));
        j++;
      } else if ("long".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Long"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.LONG, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Long", "<init>", Type.VOID, new Type[] { Type.LONG }, Constants.INVOKESPECIAL));
        j += 2;
      } else if ("byte".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Byte"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.BYTE, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Byte", "<init>", Type.VOID, new Type[] { Type.BYTE }, Constants.INVOKESPECIAL));
        j++;
      } else if ("char".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Character"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.CHAR, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Character", "<init>", Type.VOID, new Type[] { Type.CHAR }, Constants.INVOKESPECIAL));
        j++;
      } else if ("short".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Short"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.SHORT, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Short", "<init>", Type.VOID, new Type[] { Type.SHORT }, Constants.INVOKESPECIAL));
        j++;
      } else if ("float".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Float"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.FLOAT, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Float", "<init>", Type.VOID, new Type[] { Type.FLOAT }, Constants.INVOKESPECIAL));
        j++;
      } else if ("double".equals(argTypes[i])){
        this.il.append(peerCreator._factory.createNew("java.lang.Double"));
        this.il.append(InstructionConstants.DUP);
        this.il.append(InstructionFactory.createLoad(Type.DOUBLE, j));
        this.il.append(peerCreator._factory.createInvoke("java.lang.Double", "<init>", Type.VOID, new Type[] { Type.DOUBLE }, Constants.INVOKESPECIAL));
        j += 2;
      }
      this.il.append(InstructionConstants.AASTORE);
    }
    return argValue;
  }

  /**
   * Adds bytecode to the body of the method that creates an array of type
   * Class<?> including the type of the arguments of the method
   * 
   * @param argValue
   *          an index of the local variable which is an array of type Object
   *          including the arguments of the method
   * 
   * @return an index of the local variable which is an array of type Class<?>
   *         including the type of the arguments of the method
   */
  private int createArgType (int argValue){
    String[] argTypes = this.mi.getArgumentTypeNames();
    int nArgs = argTypes.length;

    /** Create an array of Class<?> (Class<?>[] argType = new Class<?>[nArgs]) */
    this.il.append(new PUSH(peerCreator._cp, nArgs));
    this.il.append(peerCreator._factory.createNewArray(new ObjectType("java.lang.Class"), (short) 1));
    LocalVariableGen lg = this.nativeMth.addLocalVariable("argType", new ArrayType(new ObjectType("java.lang.Class"), 1), null, null);
    int argType = lg.getIndex();
    this.il.append(InstructionFactory.createStore(Type.OBJECT, argType));

    String wrapperClsName = null;
    for (int i = 0; i < nArgs; i++){
      // loading the element argType[i]
      this.il.append(InstructionFactory.createLoad(Type.OBJECT, argType));
      this.il.append(new PUSH(peerCreator._cp, i));

      if (!PeerMethodGen.isPrimitiveType(argTypes[i])){
        il.append(new PUSH(peerCreator._cp, argTypes[i]));
        il.append(peerCreator._factory.createInvoke("java.lang.Class", "forName", new ObjectType("java.lang.Class"), new Type[] { Type.STRING }, Constants.INVOKESTATIC));
      } else{
        if ("boolean".equals(argTypes[i]))
          wrapperClsName = "java.lang.Boolean";
        else if ("int".equals(argTypes[i]))
          wrapperClsName = "java.lang.Integer";
        else if ("long".equals(argTypes[i]))
          wrapperClsName = "java.lang.Long";
        else if ("byte".equals(argTypes[i]))
          wrapperClsName = "java.lang.Byte";
        else if ("char".equals(argTypes[i]))
          wrapperClsName = "java.lang.Character";
        else if ("short".equals(argTypes[i]))
          wrapperClsName = "java.lang.Short";
        else if ("float".equals(argTypes[i]))
          wrapperClsName = "java.lang.Float";
        else if ("double".equals(argTypes[i]))
          wrapperClsName = "java.lang.Double";

        il.append(peerCreator._factory.createFieldAccess(wrapperClsName, "TYPE", new ObjectType("java.lang.Class"), Constants.GETSTATIC));
      }

      this.il.append(InstructionConstants.AASTORE);
    }
    return argType;
  }

  /**
   * Adds bytecode to the body of the method that gets the class of the native
   * method. For the static method, "caller" that has been already obtained from
   * createCaller is what we want. But for non-static method, we need to get the
   * class of object invoking the method (Class<?> callerClass =
   * caller.getClass())
   * 
   * @param caller
   *          an index of the local variable that represents the caller object
   *          (non-static method) or class (static method)
   * 
   * @return an index of the local variable that represents the caller class
   *         (static method) or the class of the caller object (non-static
   *         method)
   */
  private int getCallerClass (int caller){
    int callerClass;
    if (this.mi.isStatic())
      callerClass = caller;
    else{
      this.il.append(InstructionFactory.createLoad(Type.OBJECT, caller));
      this.il.append(peerCreator._factory.createInvoke("java.lang.Object", "getClass", new ObjectType("java.lang.Class"), Type.NO_ARGS, Constants.INVOKEVIRTUAL));
      LocalVariableGen lg = this.nativeMth.addLocalVariable("callerClass", new ObjectType("java.lang.Class"), null, null);
      callerClass = lg.getIndex();
      this.il.append(InstructionFactory.createStore(Type.OBJECT, callerClass));
    }
    return callerClass;
  }

  /**
   * Adds bytecode to the body of the method that uses reflection to get the
   * method from the class
   * 
   * @param callerClass
   *          an index of the local variable that represents the caller class
   *          (static method) or the class of the caller object (non-static
   *          method)
   * @param argType
   *          an index of the local variable which is an array of type Class<?>
   *          including the type of the arguments of the method
   * 
   * @return an index of the local variable that represents the Method instance
   *         representing this native method
   */
  private int getMethod (int callerClass, int argType){
    String name = this.mi.getName();
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, callerClass));
    this.il.append(new PUSH(peerCreator._cp, name));
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, argType));

    this.il.append(peerCreator._factory.createInvoke("java.lang.Class", "getDeclaredMethod", new ObjectType("java.lang.reflect.Method"), new Type[] { Type.STRING, new ArrayType(new ObjectType("java.lang.Class"), 1) }, Constants.INVOKEVIRTUAL));

    LocalVariableGen lg = this.nativeMth.addLocalVariable("method", new ObjectType("java.lang.reflect.Method"), null, null);
    int method = lg.getIndex();
    this.il.append(InstructionFactory.createStore(Type.OBJECT, method));
    return method;
  }

  /**
   * Adds bytecode to the body of the method that provides access to a private
   * method.
   * 
   * @param method
   *          an index of the local variable that represents the Method instance
   *          representing this native method
   */
  private void setAccessible (int method){
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, method));
    this.il.append(new PUSH(peerCreator._cp, 1));
    this.il.append(peerCreator._factory.createInvoke("java.lang.reflect.Method", "setAccessible", Type.VOID, new Type[] { Type.BOOLEAN }, Constants.INVOKEVIRTUAL));
  }

  /**
   * Adds bytecode to the body of the method that uses reflection to invoke the
   * method. For the static method, adds method.invoke(null, argValue); For the
   * non-static method, adds method.invoke(caller, argValue);
   * 
   * @param caller
   *          an index of the local variable that represents the caller object
   *          (non-static method) or class (static method)
   * @param method
   *          an index of the local variable that represents the Method instance
   *          representing this native method
   * @param argValue
   *          an index of the local variable which is an array of type Object
   *          including the arguments of the method
   * 
   * @return an index of the local variable that represents the return value of
   *         the method
   */
  private int invokeMethod (int caller, int method, int argValue){
    int returnValue = -1;
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, method));

    if (this.mi.isStatic())
      // loading the value NULL
      this.il.append(InstructionConstants.ACONST_NULL);
    else
      // loading the caller object
      this.il.append(InstructionFactory.createLoad(Type.OBJECT, caller));

    this.il.append(InstructionFactory.createLoad(Type.OBJECT, argValue));
    this.il.append(peerCreator._factory.createInvoke("java.lang.reflect.Method", "invoke", Type.OBJECT, new Type[] { Type.OBJECT, new ArrayType(Type.OBJECT, 1) }, Constants.INVOKEVIRTUAL));
    if (!mi.getReturnTypeName().equals("void")){
      LocalVariableGen lg = this.nativeMth.addLocalVariable("returnValue", Type.OBJECT, null, null);
      returnValue = lg.getIndex();
      this.il.append(InstructionFactory.createStore(Type.OBJECT, returnValue));
    } else{
      this.il.append(InstructionConstants.POP);
    }
    return returnValue;
  }

  /**
   * Adds bytecode to the body of the method that converts a given JVM object to
   * a JPF object.
   * 
   * @param converter
   *          an index of the local variable that represents the Converter
   *          object
   * 
   * @param JVMObj
   *          an index of the local variable that represents a JVM object
   * 
   * @return an index of the local variable that represents the JPF object
   *         corresponding to the given JVM object
   */
  private int convertJVM2JPF (int converter, int JVMObj){
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, converter));
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, JVMObj));
    this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "getJPFObj", Type.INT, new Type[] { Type.OBJECT }, Constants.INVOKEVIRTUAL));
    LocalVariableGen lg = this.nativeMth.addLocalVariable("JPFObj", Type.INT, null, null);
    int JPFObj = lg.getIndex();
    this.il.append(InstructionFactory.createStore(Type.INT, JPFObj));
    return JPFObj;
  }

  /**
   * Adds bytecode to the body of the method that updates a JPF object according
   * to the given JVM object.
   * 
   * @param converter
   *          an index of the local variable that represents the Converter
   *          object
   * 
   * @param JVMObj
   *          an index of the local variable that represents a JVM object
   * 
   * @param JPFObj
   *          an index of the local variable that represents a JPF object
   */
  private void updateJPFObj (int converter, int JVMObj, int JPFObj){
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, converter));
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, JVMObj));
    this.il.append(InstructionFactory.createLoad(Type.INT, JPFObj));
    this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "updateJPFObj", Type.VOID, new Type[] { Type.OBJECT, Type.INT }, Constants.INVOKEVIRTUAL));
  }

  /**
   * Adds bytecode to the body of the method that updates a JPF class according
   * to the given JVM object.
   * 
   * @param converter
   *          an index of the local variable that represents the Converter
   *          object
   * 
   * @param JPFCls
   *          an index of the local variable that represents a JPF class
   */
  private void getJPFClass (int converter, int JVMCls){
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, converter));
    this.il.append(InstructionFactory.createLoad(Type.OBJECT, JVMCls));
    this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "getJPFCls", new ObjectType("gov.nasa.jpf.jvm.ClassInfo"), new Type[] { new ObjectType("java.lang.Class") }, Constants.INVOKEVIRTUAL));
    this.il.append(InstructionConstants.POP);
  }

  /**
   * Adds bytecode to the body of the method that updates the arguments of the
   * JPF method according to the given JVM values.
   * 
   * @param converter
   *          an index of the local variable that represents the Converter
   *          object
   * 
   * @param argValue
   *          an index of the local variable that represents a JVM array that
   *          holds the value of input parameters
   */
  private void updateJPFArguments (int converter, int argValue){
    String[] type = mi.getArgumentTypeNames();
    int nArgs = type.length;

    int j = 2;
    for (int i = 0; i < nArgs; i++){

      if (!PeerMethodGen.isPrimitiveType(type[i])){
        this.il.append(InstructionFactory.createLoad(Type.OBJECT, converter));
        // Loading the array element argsValue[i];
        this.il.append(InstructionFactory.createLoad(Type.OBJECT, argValue));
        this.il.append(new PUSH(peerCreator._cp, i));
        this.il.append(InstructionConstants.AALOAD);
        // Loading the nth input parameter
        this.il.append(InstructionFactory.createLoad(Type.INT, j));
        // Invoking the method "updateJPFObj"
        this.il.append(peerCreator._factory.createInvoke(conversionPkg + ".Converter", "updateJPFObj", Type.VOID, new Type[] { Type.OBJECT, Type.INT }, Constants.INVOKEVIRTUAL));
        j++;
      }
    }
  }

  /**
   * Adds bytecode to the body of the method for return statement
   * 
   * @param returnValue
   *          an index of the local variable that represents the return value of
   *          the method
   */
  private void addReturnStatement (int returnValue){
    String returnType = this.mi.getReturnTypeName();
    // if the return type is Object
    if (!PeerMethodGen.isPrimitiveType(returnType)){
      this.il.append(InstructionFactory.createLoad(Type.INT, returnValue));
      this.il.append(InstructionFactory.createReturn(Type.INT));
    } else if ("void".equals(returnType)){
      this.il.append(InstructionFactory.createReturn(Type.VOID));
    } else{
      String className = null;
      String methodName = null;
      Type type = null;
      if ("boolean".equals(returnType)){
        className = "java.lang.Boolean";
        methodName = "booleanValue";
        type = Type.BOOLEAN;
      } else if ("int".equals(returnType)){
        className = "java.lang.Integer";
        methodName = "intValue";
        type = Type.INT;
      } else if ("long".equals(returnType)){
        className = "java.lang.Long";
        methodName = "longValue";
        type = Type.LONG;
      } else if ("byte".equals(returnType)){
        className = "java.lang.Byte";
        methodName = "byteValue";
        type = Type.BYTE;
      } else if ("char".equals(returnType)){
        className = "java.lang.Character";
        methodName = "charValue";
        type = Type.CHAR;
      } else if ("short".equals(returnType)){
        className = "java.lang.Short";
        methodName = "shortValue";
        type = Type.SHORT;
      } else if ("float".equals(returnType)){
        className = "java.lang.Float";
        methodName = "floatValue";
        type = Type.FLOAT;
      } else if ("double".equals(returnType)){
        className = "java.lang.Double";
        methodName = "doubleValue";
        type = Type.DOUBLE;
      }
      this.il.append(InstructionFactory.createLoad(Type.OBJECT, returnValue));
      this.il.append(peerCreator._factory.createCheckCast(new ObjectType(className)));
      this.il.append(peerCreator._factory.createInvoke(className, methodName, type, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
      this.il.append(InstructionFactory.createReturn(type));
    }
  }

  /**
   * Adds bytecode to the body of the method for a return statement that returns
   * a dummy value
   * 
   * @param returnValue
   *          an index of the local variable that represents the return value of
   *          the method
   */
  private void addDummyReturnStatement (){
    String returnType = this.mi.getReturnTypeName();
    // if the return type is Object
    if (!PeerMethodGen.isPrimitiveType(returnType)) {
      this.il.append(new PUSH(peerCreator._cp, MJIEnv.NULL));
      this.il.append(InstructionFactory.createReturn(Type.INT));
    } else if ("void".equals(returnType)) {
      this.il.append(InstructionFactory.createReturn(Type.VOID));
    } else {
      if ("boolean".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0));
        this.il.append(InstructionFactory.createReturn(Type.BOOLEAN));
      } else if ("int".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0));
        this.il.append(InstructionFactory.createReturn(Type.INT));
      } else if ("long".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0));
        this.il.append(InstructionFactory.createReturn(Type.LONG));
      } else if ("byte".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0));
        this.il.append(InstructionFactory.createReturn(Type.BYTE));
      } else if ("char".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0));
        this.il.append(InstructionFactory.createReturn(Type.CHAR));
      } else if ("short".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0));
        this.il.append(InstructionFactory.createReturn(Type.SHORT));
      } else if ("float".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0.0));
        this.il.append(InstructionFactory.createReturn(Type.FLOAT));
      } else if ("double".equals(returnType)) {
        this.il.append(new PUSH(peerCreator._cp, 0.0));
        this.il.append(InstructionFactory.createReturn(Type.DOUBLE));
      }
    }
  }

  /**
   * Returns the type corresponding to the given string.
   * 
   * @param typeName
   *          a string that stores a type name
   * 
   * @return the type corresponding to the given string
   */
  private static Type getType (String typeName){
    Type returnType = null;
    if ("int".equals(typeName)) {
      returnType = Type.INT;
    } else if ("long".equals(typeName)) {
      returnType = Type.LONG;
    } else if ("boolean".equals(typeName)) {
      returnType = Type.BOOLEAN;
    } else if ("void".equals(typeName)) {
      returnType = Type.VOID;
    } else if ("byte".equals(typeName)) {
      returnType = Type.BYTE;
    } else if ("char".equals(typeName)) {
      returnType = Type.CHAR;
    } else if ("short".equals(typeName)) {
      returnType = Type.SHORT;
    } else if ("float".equals(typeName)) {
      returnType = Type.FLOAT;
    } else if ("double".equals(typeName)) {
      returnType = Type.DOUBLE;
    }
    // The type should be a type of an object
    else {
      returnType = Type.OBJECT;
    }
    return returnType;
  }

  /**
   * Checks if the given string represents a primitive type.
   * 
   * @param t
   *          a string that stores a type name
   * 
   * @return true of the given string stores a primitive type. OW, it returns
   *         false.
   */
  protected static boolean isPrimitiveType (String t){
    return ("int".equals(t) || "long".equals(t) || "boolean".equals(t) || "void".equals(t) || "byte".equals(t) || "char".equals(t) || "short".equals(t) || "float".equals(t) || "double".equals(t));
  }

  /**
   * Creates an array of type Type including the type of the arguments of the
   * method.
   * 
   * @param mi
   *          an object that represents a method in JPF
   * 
   * @return an array of type Type including the type of the arguments of the
   *         method
   */
  private static Type[] getArgumentsType (NativeMethodInfo mi){
    Type[] argTypes = new Type[mi.getNumberOfArguments() + 2];
    argTypes[0] = new ObjectType("gov.nasa.jpf.jvm.MJIEnv");
    argTypes[1] = Type.INT;
    String[] argTypesName = mi.getArgumentTypeNames();
    for (int i = 2; i < mi.getNumberOfArguments() + 2; i++){
      Type type = PeerMethodGen.getType(argTypesName[i - 2]);
      argTypes[i] = (type == Type.OBJECT) ? Type.INT : type;
    }
    return argTypes;
  }

  /**
   * Creates an array of type String including the arguments names of the
   * method.
   * 
   * @param mi
   *          an object that represents a method in JPF
   * 
   * @return an array of type String including the arguments names of the method
   */
  private static String[] getArgumentsName (NativeMethodInfo mi){
    String[] argName = new String[mi.getNumberOfArguments() + 2];
    argName[0] = "env";

    if (mi.isStatic()) {
      argName[1] = "rcls";
    } else {
      argName[1] = "robj";
    }
    for (int i = 2; i < mi.getNumberOfArguments() + 2; i++) {
      argName[i] = "arg" + (i - 2);
    }
    return argName;
  }
}
