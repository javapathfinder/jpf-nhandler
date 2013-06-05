package nhandler.conversion;

import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.vm.MJIEnv;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import nhandler.util.ValueIdentityHashMap;

/**
 * This class includes helper methods used in the Converter implementation
 * 
 * @author Nastaran Shafiei
 */
public class ConverterBase {

  public static boolean resetState;

  /**
   * Keeps track of the JVM objects that have been already created from their
   * corresponding JPF objects, while performing conversion from JPF to JVM
   */
  public static ValueIdentityHashMap<Integer, Object> objMapJPF2JVM = new ValueIdentityHashMap<Integer, Object>();

  /**
   * Keeps track of the JVM classes that have been already created from their
   * corresponding JPF classes, while performing conversion from JPF to JVM
   */
  public static ValueIdentityHashMap<Integer, Class<?>> classMapJPF2JVM = new ValueIdentityHashMap<Integer, Class<?>>();

  /**
   * Keeps track of the JPF objects that have been already updated from their
   * corresponding JVM objects, while performing conversion from JVM to JPF
   */
  public static HashMap<Integer, Object> updatedJPFObj = new ValueIdentityHashMap<Integer, Object>();

  /**
   * Keeps track of the JPF classes that have been already updated from their
   * corresponding JVM classes, while performing conversion from JVM to JPF
   */
  public static Set<Integer> updatedJPFCls = new HashSet<Integer>();

  public static ConverterFactory converterFactory;

  public static void init() {
    converterFactory = new DefaultConverterFactory();
  }

  /**
   * This needs to be invoked at the beginning of every method created on-the-fly
   */
  public static void reset(MJIEnv env) {
    ConverterBase.resetState = env.getConfig().getBoolean("nhandler.resetVMState");

    if (ConverterBase.resetState) {
      // these are reset on-demond by setting the nhandler.resetVMState
      // property in the properties file
      ConverterBase.objMapJPF2JVM.clear();
      ConverterBase.classMapJPF2JVM.clear();
    }

    // these always need to be reset
    ConverterBase.updatedJPFObj.clear();
    ConverterBase.updatedJPFCls.clear();
  }
  
  /********** Utilities needed for the conversion from JPF to JVM ***********/  

  /**
   * Returns a new JVM object instantiated from the given class
   * 
   * @param cl
   *          a JVM class
   * 
   * @return a new JVM object instantiated from the given class, cl
   */
  public static Object instantiateFrom (Class<?> cl) {
    Object JVMObj = null;

    if (cl == Class.class) { 
      return cl; 
    }

    Constructor<?> ctor = getNoArgCtor(cl);
    try {
      ctor.setAccessible(true);
      JVMObj = ctor.newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return JVMObj;
  }

  /**
   * Returns a constructor with no arguments.
   * 
   * @param cl
   *          a JVM class
   * 
   * @return a constructor with no arguments
   */
  public static Constructor<?> getNoArgCtor (Class<?> cl) {
    Constructor<?>[] ctors = cl.getDeclaredConstructors();
    Constructor<?> ctor = null;

    // Check if the given class has a constructor with no arguments
    for (Constructor<?> c : ctors) {
      if (c.getParameterTypes().length == 0) {
        ctor = c;
      }
    }

    if (ctor == null) {
      try {
        ctor = sun.reflect.ReflectionFactory.getReflectionFactory().newConstructorForSerialization(cl, Object.class.getConstructor());
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
    return ctor;
  }

  /**
   * Sets a primitive field of a JVM object to a value of the corresponding
   * field of the given JPF object.
   * 
   * @param fld
   *          a field of a JVM object which is of primitive type.
   * @param obj
   *          The JVM object that includes the field fld.
   * @param ei
   *          a JPF object which is corresponding to the given JVM object, obj.
   * 
   * @throws IllegalAccessException
   *           when method trying to access a private field of an JVM object
   *           whose "isAccessible" is not true. But that will not happen,
   *           because in "getJVMObj" before invoking this method we always
   *           invoke "fld.setAccessible(true)".
   * 
   * @throws ConversionException
   *           if the given field is not of primitive type
   */
  public static void setJVMPrimitiveField (Field fld, Object obj, ElementInfo ei, FieldInfo fi) throws IllegalAccessException, ConversionException {
    if (fi.isBooleanField()) {
      fld.setBoolean(obj, ei.getBooleanField(fi));
    } else if (fi.isByteField()) {
      fld.setByte(obj, ei.getByteField(fi));
    } else if (fi.isShortField()) {
      fld.setShort(obj, ei.getShortField(fi));
    } else if (fi.isIntField()) {
      fld.setInt(obj, ei.getIntField(fi));
    } else if (fi.isLongField()) {
      fld.setLong(obj, ei.getLongField(fi));
    } else if (fi.isCharField()) {
      fld.setChar(obj, ei.getCharField(fi));
    } else if (fi.isFloatField()) {
      fld.setFloat(obj, ei.getFloatField(fi));
    } else if (fi.isDoubleField()) {
      fld.setDouble(obj, ei.getDoubleField(fi));
    } else {
      throw new ConversionException("Unknown premitive type " + fi.getType());
    }
  }

  /**
   * Creates an array of primitive type which is corresponding to the given JPF
   * array.
   * 
   * @param ei
   *          An ElementInfo which represents a JPF array of primitive type
   * 
   * @return a JVM array of primitive type which is created corresponding to the
   *         given JPF array represented by ei
   * 
   * @throws ConversionException
   *           if the given array is not of primitive type
   */
  public static Object createJVMPrimitiveArr (ElementInfo ei) throws ConversionException {
    String type = ei.getType();
    Object JVMObj = null;

    // byte[]
    if (type.equals("[B")) {
      JVMObj = ((ArrayFields) ei.getFields()).asByteArray();
    }
    // char[]
    else if (type.equals("[C")) {
      JVMObj = ((ArrayFields) ei.getFields()).asCharArray();
    }
    // short[]
    else if (type.equals("[S")) {
      JVMObj = ((ArrayFields) ei.getFields()).asShortArray();
    }
    // int[]
    else if (type.equals("[I")) {
      JVMObj = ((ArrayFields) ei.getFields()).asIntArray();
    }
    // float[]
    else if (type.equals("[F")) {
      JVMObj = ((ArrayFields) ei.getFields()).asFloatArray();
    }
    // long[]
    else if (type.equals("[J")) {
      JVMObj = ((ArrayFields) ei.getFields()).asLongArray();
    }
    // double[]
    else if (type.equals("[D")) {
      JVMObj = ((ArrayFields) ei.getFields()).asDoubleArray();
    }
    // boolean[]
    else if (type.equals("[Z")) {
      JVMObj = ((ArrayFields) ei.getFields()).asBooleanArray();
    } else {
      throw new ConversionException("Unknown array type " + type);
    }
    return JVMObj;
  }

  public static boolean isPrimitiveClass (String name) {
    return (name.equals("boolean") || name.equals("byte") || name.equals("int") || name.equals("short") || name.equals("long") || name.equals("char") || name.equals("float") || name.equals("double"));
  }


  /**
   * Returns a class corresponding to the given primitive type
   * 
   * @param name
   *          primitive type name
   *          
   * @return class corresponding to the given primitive type
   */
  public static Class<?> getPrimitiveClass (String name) {
    if (name.equals("boolean")) {
      return boolean.class;
    } else if (name.equals("byte")) {
      return byte.class;
    } else if (name.equals("int")) {
      return int.class;
    } else if (name.equals("short")) {
      return short.class;
    } else if (name.equals("long")) {
      return long.class;
    } else if (name.equals("char")) {
      return char.class;
    } else if (name.equals("float")) {
      return float.class;
    } else if (name.equals("double")) { 
      return double.class; 
    }
    return null;
  }

  public static boolean isArray(String cname) {
    return cname.startsWith("[");
  }
  
  /********** Utilities needed for the conversion from JVM to JPF ***********/

  /**
   * Creates a new JPF array which has the same type and same size as the given
   * JVM array.
   * 
   * @param JVMArr
   *          a JVM array
   * 
   * @return a new JPF array of the same type and same size as the given JVM
   *         array.
   */
  public static int createNewJPFArray (Object JVMArr, MJIEnv env){
    String type = JVMArr.getClass().getName();
    int JPFArr = MJIEnv.NULL;
    // byte[]
    if (type.equals("[B")){
      JPFArr = env.newByteArray(((byte[]) JVMArr).length);
    }
    // char[]
    else if (type.equals("[C")){
      JPFArr = env.newCharArray(((char[]) JVMArr).length);
    }
    // short[]
    else if (type.equals("[S")){
      JPFArr = env.newShortArray(((short[]) JVMArr).length);
    }
    // int[]
    else if (type.equals("[I")){
      JPFArr = env.newIntArray(((int[]) JVMArr).length);
    }
    // float[]
    else if (type.equals("[F")){
      JPFArr = env.newFloatArray(((float[]) JVMArr).length);
    }
    // long[]
    else if (type.equals("[J")){
      JPFArr = env.newLongArray(((long[]) JVMArr).length);
    }
    // double[]
    else if (type.equals("[D")){
      JPFArr = env.newDoubleArray(((double[]) JVMArr).length);
    }
    // boolean[]
    else if (type.equals("[Z")){
      JPFArr = env.newBooleanArray(((boolean[]) JVMArr).length);
    }
    // Object[]
    else{
      JPFArr = env.newObjectArray(((Object[]) JVMArr).getClass().getComponentType().getName(), ((Object[]) JVMArr).length);
    }

    return JPFArr;
  }

  /**
   * Sets an element of the given JPF primitive array to the same value as the given JVM
   * object.
   * 
   * @param ei
   *          an object that represents the JPF array
   * @param index
   *          index of the element to be set
   * @param fld
   *          represents an array element in JVM
   * @param JVMObj
   *          the value of the filed in JVM
   * 
   * @throws IllegalAccessException
   *           method does not have access to the specified field
   * 
   * @throws ConversionException
   *           if the given field is not of primitive type
   */
  public static void setJPFPrimitiveField (ElementInfo ei, int index, Field fld, Object JVMObj) throws IllegalAccessException, ConversionException{
    Fields fields = ei.getFields();
    if (fld.getType().getName().equals("boolean")){
      fields.setBooleanValue(index, fld.getBoolean(JVMObj));
    } else if (fld.getType().getName().equals("byte")){
      fields.setByteValue(index, fld.getByte(JVMObj));
    } else if (fld.getType().getName().equals("int")){
      fields.setIntValue(index, fld.getInt(JVMObj));
    } else if (fld.getType().getName().equals("short")){
      fields.setShortValue(index, fld.getShort(JVMObj));
    } else if (fld.getType().getName().equals("long")){
      fields.setLongValue(index, fld.getLong(JVMObj));
    } else if (fld.getType().getName().equals("char")){
      fields.setCharValue(index, fld.getChar(JVMObj));
    } else if (fld.getType().getName().equals("float")){
      fields.setFloatValue(index, fld.getFloat(JVMObj));
    } else if (fld.getType().getName().equals("double")){
      fields.setDoubleValue(index, fld.getDouble(JVMObj));
    } else{
      throw new ConversionException("Unknown premitive type " + fld.getType().getName());
    }
  }

  /**
   * Sets a JPF array of primitive type according to the value of the given JVM
   * array.
   * 
   * @param ei
   *          an object that represents the JPF array of primitive type
   * @param JVMObj
   *          a JVM array of primitive type
   * 
   * @throws ConversionException
   *           if the given array is not of primitive type
   */
  public static void setJPFPrimitiveArr (ElementInfo ei, Object JVMObj, MJIEnv env) throws ConversionException{
    String type = ei.getType();
    // byte[]
    if (type.equals("[B")){
      byte[] JVMArr = (byte[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setByteArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    }
    // char[]
    else if (type.equals("[C")){
      char[] JVMArr = (char[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setCharArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    }
    // short[]
    else if (type.equals("[S")){
      short[] JVMArr = (short[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setShortArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    }
    // int[]
    else if (type.equals("[I")){
      int[] JVMArr = (int[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setIntArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    }
    // float[]
    else if (type.equals("[F")){
      float[] JVMArr = (float[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setFloatArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    }
    // long[]
    else if (type.equals("[J")){
      long[] JVMArr = (long[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setLongArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    }
    // double[]
    else if (type.equals("[D")){
      double[] JVMArr = (double[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setDoubleArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    }
    // boolean[]
    else if (type.equals("[Z")){
      boolean[] JVMArr = (boolean[]) JVMObj;
      for (int i = 0; i < JVMArr.length; i++){
        env.setBooleanArrayElement(ei.getObjectRef(), i, JVMArr[i]);
      }
    } else{
      throw new ConversionException("Unknown array type " + type);
    }
  }
}
