/* 
 * Copyright (C) 2013  Nastaran Shafiei and Franck van Breugel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */

package nhandler.conversion;

import gov.nasa.jpf.vm.ArrayFields;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassInfoException;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ReferenceArrayFields;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nhandler.util.ValueIdentityHashMap;

/**
 * Converts objects and classes from JPF to JVM, and from JVM to JPF.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class Converter {

  MJIEnv env;

  private ClassLoader cl;

  /**
   * Keeps track of the JVM objects that have been already created from their
   * corresponding JPF objects, while performing conversion from JPF to JVM
   */
  static ValueIdentityHashMap<Integer, Object> objMapJPF2JVM = new ValueIdentityHashMap<Integer, Object>();

  /**
   * Keeps track of the JVM classes that have been already created from their
   * corresponding JPF classes, while performing conversion from JPF to JVM
   */
  static ValueIdentityHashMap<Integer, Class<?>> classMapJPF2JVM = new ValueIdentityHashMap<Integer, Class<?>>();

  /**
   * Keeps track of the JPF objects that have been already updated from their
   * corresponding JVM objects, while performing conversion from JVM to JPF
   */
  static HashMap<Integer, Object> updatedJPFObj = new ValueIdentityHashMap<Integer, Object>();

  /**
   * Keeps track of the JPF classes that have been already updated from their
   * corresponding JVM classes, while performing conversion from JVM to JPF
   */
  static Set<Integer> updatedJPFCls = new HashSet<Integer>();

  static boolean resetState;

  public Converter (MJIEnv env) {
    this.env = env;
    this.cl = env.getConfig().getClassLoader();

    resetState = env.getConfig().getBoolean("nhandler.resetVMState");

    if (resetState) {
      // these are reset on-demond by setting the nhandler.resetVMState
      // property in the properties file
      objMapJPF2JVM.clear();
      classMapJPF2JVM.clear();
    }

    // these always need to be reset
    updatedJPFObj.clear();
    updatedJPFCls.clear();
  }

  /********** Conversion from JPF to JVM ***********/
  
  public Class<?> loadClass(String cname) throws ClassNotFoundException {
    if(isArray(cname)) {
      return Class.forName(cname);
    } else {
      return cl.loadClass(cname);
    }
  }

  /**
   * Returns a new JVM Class object corresponding to the given JPF class. If
   * such a Class object already exists, it is returned. Otherwise a new one is
   * created.
   * 
   * @param JPFRef
   *          an integer representing a JPF class
   * 
   * @return a JVM Class object corresponding to the given JPF class, JPFRef
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  public Class<?> getJVMCls (int JPFRef) throws ConversionException {
    Class<?> JVMCls = null;
    if (JPFRef != MJIEnv.NULL) {
      // First check if the class object has been already created.
      JVMCls = Converter.classMapJPF2JVM.get(JPFRef);

      /**
       * If the Class object has not been created & the given JPF class is not
       * NULL, the corresponding JVM class object is created from JPFRef
       */
      if (JVMCls == null) {
        ClassInfo ci = env.getReferredClassInfo(JPFRef);

        // Used to store static fields
        StaticElementInfo sei = ci.getStaticElementInfo();

        try {
          JVMCls = loadClass(sei.getClassInfo().getName());
          Converter.classMapJPF2JVM.put(JPFRef, JVMCls);
        } catch (ClassNotFoundException e) {
          throw new NoClassDefFoundError(sei.getClassInfo().getName());
        }

        assert (JVMCls.getName() != ci.getName());

        setJVMClassFields(JVMCls, sei);
      }
    }
    return JVMCls;
  }

  public void setJVMClassFields(Class<?> JVMCls, StaticElementInfo sei) throws ConversionException {
    ClassInfo ci = sei.getClassInfo();
    while (JVMCls!=null) {
      Field fld[] = JVMCls.getDeclaredFields();

      for (int i = 0; i < fld.length; i++) {
        boolean isStatic = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") != -1);
        boolean isFinal = ((Modifier.toString(fld[i].getModifiers())).indexOf("final") != -1);

        // Provide access to private and final fields
        fld[i].setAccessible(true);
        FieldInfo fi = sei.getFieldInfo(fld[i].getName());

        // For class only set the values of static fields
        if (fi != null && isStatic) {
          /**
           * Why we check for !(isFinal)?
           * 
           * We do not set the value for "static final" fields. But we take
           * care of "non-static final" fields.
           * 
           * static final fields can be initialized at the declaration time,
           * OW it MUST be initialized inside the static block. By using
           * Class.forName() the class is initialized. Since when the class
           * is initialized the static blocks are executed, the static final
           * fields of object returned by Class.forName() have already have
           * the right values and we do not need to update their value.
           * 
           * Non-static final fields can be initialized at the declaration
           * time. But if the non-static field is final blank, it MUST be
           * initialized in the constructor. By using Class.newInstance()
           * the class is instantiated as if by a new expression with an
           * empty argument list. If the object represented by JPFRef
           * created using different constructor, the value of final blank
           * fields might be different when using the constructor with an
           * empty argument list. Therefore the values of non-static final
           * fields have to be set.
           */
          if (!isFinal) {
            // If the current field is of reference type
            if (fi.isReference()) {
              int fieldValueRef = sei.getFields().getReferenceValue(fi.getStorageOffset());
              Object JVMField = this.getJVMObj(fieldValueRef);
              try {
                fld[i].set(null, JVMField);
              } catch (IllegalArgumentException e) {
                e.printStackTrace();
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              }
            }
            // If the current field is of primitive type
            else {
              try {
                setJVMPrimitiveField(fld[i], JVMCls, sei, fi);
              } catch (IllegalAccessException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }

      JVMCls = JVMCls.getSuperclass();
      ci = ci.getSuperClass();
    }
  }
  
  /**
   * Returns a JVM object corresponding to the given JPF object. If such an
   * object already exists, it is returned. Otherwise a new one is created.
   * 
   * @param JPFRef
   *          an integer representing a JPF object
   * 
   * @return a JVM object corresponding to the given JPF object, JPFRef
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  public Object getJVMObj (int JPFRef) throws ConversionException {
    if (JPFRef == MJIEnv.NULL) return null;
    if (env.isArray(JPFRef)) {
      return this.getArr(JPFRef);
    } else {
      return this.getObj(JPFRef);
    }
  }

  /**
   * Returns a non-array JVM object corresponding to the given non-array JPF
   * object. If such an object already exists, it is returned. Otherwise a new
   * one is created.
   * 
   * @param JPFRef
   *          an integer representing a non-array JPF object
   * 
   * @return a JVM object corresponding to the given non-array JPF object,
   *         JPFRef
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  private Object getObj (int JPFRef) throws ConversionException {
    Object JVMObj = null;
    if (JPFRef != MJIEnv.NULL) {
      // First check if the object has been already created
      JVMObj = Converter.objMapJPF2JVM.get(JPFRef);
      /**
       * If the object has not been created & the given JPF object is not NULL,
       * the corresponding JVM object is created from JPFRef
       */
      if (JVMObj == null) {
        // Used to store instance fields
        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().get(JPFRef);
        ClassInfo JPFCl = dei.getClassInfo();

        // we treat Strings differently
        if(JPFCl.isStringClassInfo()) {
          JVMObj = createStringObject(JPFRef);
        } else {
          int JPFClsRef = JPFCl.getStaticElementInfo().getClassObjectRef();
          Class<?> JVMCl = this.getJVMCls(JPFClsRef);

          // There is only one instance of every class. There is no need to update
          // Class objects
          if (JVMCl == Class.class) {
            try {
              String name = env.getReferredClassInfo(JPFRef).getName();
              if (isPrimitiveClass(name)) {
                JVMObj = getPrimitiveClass(name);
              } else {
                JVMObj = loadClass(name);
              }
            } catch (ClassNotFoundException e) {
              e.printStackTrace();
            }
            return JVMObj;
          } else {
            // Creates a new instance of JVMCl
            JVMObj = instantiateFrom(JVMCl);
          }

          Converter.objMapJPF2JVM.put(JPFRef, JVMObj);
          setJVMObjFields(JVMObj, dei);
        }
      }
    }
    return JVMObj;
  }

  public void setJVMObjFields(Object JVMObj, DynamicElementInfo dei) throws ConversionException {
    Class<?> cls = JVMObj.getClass();
    ClassInfo JPFCl = dei.getClassInfo();

    while (cls!=null) {
      Field fld[] = cls.getDeclaredFields();

      for (int i = 0; i < fld.length; i++) {

        // It is true if the field is declared as static.
        boolean isNonStaticField = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") == -1);

        // Provide access to private and final fields
        fld[i].setAccessible(true);
        FieldInfo fi = JPFCl.getInstanceField(fld[i].getName());

        if (fi != null && isNonStaticField) {
          // Field is of reference type
          if (fi.isReference()) {
            int fieldValueRef = dei.getFields().getReferenceValue(fi.getStorageOffset());
            Object JVMField = this.getJVMObj(fieldValueRef);

            try {
              fld[i].set(JVMObj, JVMField);
            } catch (IllegalArgumentException e) {
              e.printStackTrace();
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
          }
          // Field is of primitive type
          else {
            try {
              setJVMPrimitiveField(fld[i], JVMObj, dei, fi);
            } catch (IllegalAccessException e) {
              e.printStackTrace();
            }
          }
        }
      }
      cls = cls.getSuperclass();
      JPFCl = JPFCl.getSuperClass();
    }
  }

  /**
   * Returns a JVM array corresponding to the given JPF array. If such an array
   * already exists, it is returned. Otherwise a new one is created.
   * 
   * @param JPFRef
   *          an integer representing a JPF array
   * 
   * @return a JVM array corresponding to the given JPF array, JPFRef
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  private Object getArr (int JPFRef) throws ConversionException {
    Object JVMArr = null;
    if (JPFRef != MJIEnv.NULL) {
      // First check if the array has been already created
      JVMArr = Converter.objMapJPF2JVM.get(JPFRef);

      /**
       * If the array has not been created & the given JPF array is not NULL,
       * the corresponding JVM array is created from JPFRef
       */
      if (JVMArr == null) {
        // Used to store array elements
        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().get(JPFRef);

        // Array of primitive type
        if (dei.getClassInfo().getComponentClassInfo().isPrimitive()) {
          JVMArr = createJVMPrimitiveArr(dei);
        }
        // Array of Non-primitives
        else {
          int[] JPFArr = ((ReferenceArrayFields) dei.getFields()).asReferenceArray();
          int arrSize = JPFArr.length;

          Class<?> compType = null;
          try {
            compType = loadClass(dei.getClassInfo().getComponentClassInfo().getName());
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }

          Object[] arrObj = (Object[]) Array.newInstance(compType, arrSize);

          for (int i = 0; i < arrSize; i++) {
            arrObj[i] = this.getJVMObj(JPFArr[i]);
          }
          JVMArr = arrObj;
        }
        Converter.objMapJPF2JVM.put(JPFRef, JVMArr);
      }
    }
    return JVMArr;
  }

  /**
   * Returns a new JVM object instantiated from the given class
   * 
   * @param cl
   *          a JVM class
   * 
   * @return a new JVM object instantiated from the given class, cl
   */
  private static Object instantiateFrom (Class<?> cl) {
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
  private static Constructor<?> getNoArgCtor (Class<?> cl) {
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
  private static void setJVMPrimitiveField (Field fld, Object obj, ElementInfo ei, FieldInfo fi) throws IllegalAccessException, ConversionException {
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
  private static Object createJVMPrimitiveArr (ElementInfo ei) throws ConversionException {
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

  private static boolean isPrimitiveClass (String name) {
    return (name.equals("boolean") || name.equals("byte") || name.equals("int") || name.equals("short") || name.equals("long") || name.equals("char") || name.equals("float") || name.equals("double"));
  }

  public Object createStringObject(int JPFRef) throws ConversionException {
    DynamicElementInfo str = (DynamicElementInfo) env.getHeap().get(JPFRef);
    if(!str.getClassInfo().isStringClassInfo()) {
      throw new ConversionException();
    }

    FieldInfo fi = str.getFieldInfo("value");
    int fieldValueRef = str.getFields().getReferenceValue(fi.getStorageOffset());

    // this is String.value which is of type of char[]
    Object value = this.getJVMObj(fieldValueRef);
    Object JVMObj = new String((char[])value);
    Converter.objMapJPF2JVM.put(JPFRef, JVMObj);
    return JVMObj;
  }

  /**
   * Returns a class corresponding to the given primitive type
   * 
   * @param name
   *          primitive type name
   *          
   * @return class corresponding to the given primitive type
   */
  private static Class<?> getPrimitiveClass (String name) {
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

  protected static boolean isArray(String cname) {
    return cname.startsWith("[");
  }
  
  /********** Conversion from JVM to JPF ***********/
 
  /**
   * Returns a JPF class corresponding to the given JVM Class object. If such an
   * class exists, it is updated (if it has not been updated) corresponding to
   * the given JVMObj. Otherwise a new JPF class corresponding to the given JVM
   * class object is created and added to the list of the JPF loaded classes.
   * 
   * @param JVMCls
   *          a JVM Class object
   * 
   * @return a JPF class corresponding to the given JVM class, JVMCls
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  public ClassInfo getJPFCls (Class<?> JVMCls) throws ConversionException{
    ClassInfo JPFCls = null;
    int JPFClsRef = Integer.MIN_VALUE;

    if (JVMCls != null){
      // retrieving the integer representing the Class in JPF
      JPFCls = ClassLoaderInfo.getCurrentResolvedClassInfo(JVMCls.getName());
      StaticElementInfo sei = JPFCls.getModifiableStaticElementInfo();

      if (sei != null){
        JPFClsRef = sei.getObjectRef();
      }

      // First check if the class has been already updated
      if (!Converter.updatedJPFCls.contains(JPFClsRef)){
        /**
         * If the corresponding ClassInfo does not exist, a new ClassInfo object
         * is created and will be added to the loadedClasses.
         */
        if (!JPFCls.isRegistered()){
          JPFCls.registerClass(env.getThreadInfo());
          sei = JPFCls.getModifiableStaticElementInfo();
          JPFClsRef = sei.getObjectRef();
        }

        // This is to avoid JPF to initialized the class
        JPFCls.setInitialized();

        Converter.updatedJPFCls.add(JPFClsRef);

        setJPFClassFields(JVMCls, sei);
      }
    }
    return JPFCls;
  }

  /**
   * Sets the static fields of a JPF class corresponding to the given JVM class
   * @param JVMCls a class object in JVM
   * @param sei captures the value of static fields
   * @throws ConversionException
   */
  public void setJPFClassFields(Class<?> JVMCls, StaticElementInfo sei) throws ConversionException {
    Field fld[] = JVMCls.getDeclaredFields();

    for (int i = 0; i < fld.length; i++){
      boolean isStatic = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") != -1);
      boolean isFinal = ((Modifier.toString(fld[i].getModifiers())).indexOf("final") != -1);

      // Provide access to private and final fields
      fld[i].setAccessible(true);
      FieldInfo fi = sei.getFieldInfo(fld[i].getName());

      // Provide access to private and final fields
      if (fi != null){
        if (isStatic && !isFinal){
          // If the current field is of reference type
          if (fi.isReference()){
            int JPFfldValue = MJIEnv.NULL;
            Object JVMfldValue = null;

            try{
              // retrieving the value of the field in JVM
              JVMfldValue = fld[i].get(JVMCls);
            } catch (IllegalAccessException e2){
              e2.printStackTrace();
            }

            JPFfldValue = sei.getReferenceField(fi);

            if (JVMfldValue == null){
              JPFfldValue = MJIEnv.NULL;
            } else if (JPFfldValue == MJIEnv.NULL || Converter.objMapJPF2JVM.get(JPFfldValue) != JVMfldValue){
              JPFfldValue = this.getJPFObj(JVMfldValue);
            } else if (Converter.objMapJPF2JVM.get(JPFfldValue) == JVMfldValue){
              this.updateJPFObj(JVMfldValue, JPFfldValue);
            } else{
              throw new ConversionException("Unconsidered case observed! - JVM2JPF.getJPFCls()");
            }
            sei.setReferenceField(fi, JPFfldValue);
          }
          // If the current field is of primitive type
          else{
            try{
              setJPFPrimitiveField(sei, fi.getStorageOffset(), fld[i], JVMCls);
            } catch (IllegalAccessException e){
              e.printStackTrace();
            }
          }
        }
      }
    }
  }
  /**
   * Returns a JPF object corresponding to the given JVM object. If such an
   * object exists, it is updated (if it has not been updated) corresponding to
   * the given JVMObj. Otherwise a new JPF object corresponding to the given JVM
   * object is created.
   * 
   * @param JVMObj
   *          a JVM object
   * 
   * @return a JPF object corresponding to the given JVM object, JVMObj
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  public int getJPFObj (Object JVMObj) throws ConversionException{
    int JPFRef = MJIEnv.NULL;
    if (JVMObj != null){
      if (JVMObj.getClass() == Class.class){
        JPFRef = (this.getJPFCls((Class<?>) JVMObj)).getClassObjectRef();
      } else{
        JPFRef = this.getExistingJPFRef(JVMObj, true);
        if (JPFRef == MJIEnv.NULL){
          JPFRef = this.getNewJPFRef(JVMObj);
        }
      }
    }
    return JPFRef;
  }

  /**
   * Updates the given JPF object, according to the given JVM object. For the
   * case of the non-array object, its JPF class is also updated according to
   * the class of the given JVM object.
   * 
   * @param JVMObj
   *          a JVM object
   * @param JPFObj
   *          a JPF object
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  public void updateJPFObj (Object JVMObj, int JPFObj) throws ConversionException{
    //if (this.isValidJPFRef(JVMObj, JPFObj)){
      // Both JVM and JPF objects are null, No need for updating
      if (JVMObj == null){ return; }
      if (JVMObj.getClass().isArray()){
        this.updateArr(JVMObj, JPFObj);
      } else{
        this.updateObj(JVMObj, JPFObj);
      }
   // } else{
   //   throw new ConversionException("The given JPFObj is not valid!");
   // }
  }

  /**
   * Checks if the given JPF reference is valid, meaning that either the JPF
   * reference represents the same object as the given JVM object. This method
   * should be always invoked before updating the given JPF object corresponding
   * to the given JVM object.
   * 
   * @param JVMObj
   *          a JVM object
   * @param JPFObj
   *          a JPF object
   * @return true if the given JPFObj represents the same object as JPFObj, OW
   *         false is returned.
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  // todo - investigate this using Socket.jpf example
  private boolean isValidJPFRef (Object JVMObj, int JPFObj) throws ConversionException{
    boolean isValid;

    if(!Converter.resetState) {
      return true;
    }

    if (JPFObj == MJIEnv.NULL || JVMObj == null){
      isValid = (JPFObj == MJIEnv.NULL && JVMObj == null);
    } else if (JVMObj.getClass() == Class.class){
      isValid = true;
    } else{
      int existingJPFObj = this.getExistingJPFRef(JVMObj, false);
      isValid = (existingJPFObj == JPFObj);// || existingJPFObj == MJIEnv.NULL);
    }
    return isValid;
  }

  /**
   * Updates the given non-array JPF object according to the given non-array JVM
   * object. The class of the object is also updated according to the class of
   * the given JVM object.
   * 
   * @param JVMObj
   *          a non-array JVM object
   * @param JPFObj
   *          a non-array JPF object
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  private void updateObj (Object JVMObj, int JPFObj) throws ConversionException{
    if (JVMObj != null){
      // First check if the JPF object has been already updated
      if (!Converter.updatedJPFObj.containsKey(JPFObj)){
        Converter.updatedJPFObj.put(JPFObj, JVMObj);
        Converter.objMapJPF2JVM.put(JPFObj, JVMObj);

        // Why do we need that? Because JPF might have not leaded the class
        // before! JPF classloader does not recognize them!
        // I don't now why exactly!
        // INVESTIGATE: Why not arrays?
        if (JVMObj.getClass() == Class.class){
          try{
            Class<?> temp = (Class<?>) JVMObj;
            if (!temp.isArray() && !temp.isPrimitive())
              env.getConfig().getClassLoader().loadClass(temp.getName());
          } catch (ClassNotFoundException e1){
            e1.printStackTrace();
          }
        }
        
        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().getModifiable(JPFObj);

        setJPFObjFields(JVMObj, dei);
      }
    }
  }

  public void setJPFObjFields(Object JVMObj, DynamicElementInfo dei) throws ConversionException {
    Class<?> JVMCl = JVMObj.getClass();
    ClassInfo JPFCl = this.getJPFCls(JVMObj.getClass());

    while (JVMCl!=null){
      Field fld[] = JVMCl.getDeclaredFields();

      for (int i = 0; i < fld.length; i++){
        // Check if the field is declared as non-static
        boolean isNonStaticField = ((Modifier.toString(fld[i].getModifiers())).indexOf("static") == -1);
        FieldInfo fi = JPFCl.getInstanceField(fld[i].getName());
        fld[i].setAccessible(true);

        if (fi != null && isNonStaticField){
          // If the current field is of reference type
          if (fi.isReference()){
            int JPFfldValue = MJIEnv.NULL;
            Object JVMfldValue = null;

            try{
              // retrieving the value of the field in JVM
              JVMfldValue = fld[i].get(JVMObj);
            } catch (IllegalAccessException e2){
              e2.printStackTrace();
            }

            JPFfldValue = dei.getReferenceField(fi);

            if (JVMfldValue == null){
              JPFfldValue = MJIEnv.NULL;
            } else if (JPFfldValue == MJIEnv.NULL || Converter.objMapJPF2JVM.get(JPFfldValue) != JVMfldValue){
              JPFfldValue = this.getJPFObj(JVMfldValue);
            } else if (Converter.objMapJPF2JVM.get(JPFfldValue) == JVMfldValue){
              this.updateJPFObj(JVMfldValue, JPFfldValue);
            } else{
              throw new ConversionException("Unconsidered case observed! - JVM2JPF.updateObj()");
            }
            dei.setReferenceField(fi, JPFfldValue);
          }
          // If the current field is of primitive type
          else{
            try{
              setJPFPrimitiveField(dei, fi.getStorageOffset(), fld[i], JVMObj);
            } catch (IllegalAccessException e){
              e.printStackTrace();
            }
          }
        }
      }
      JVMCl = JVMCl.getSuperclass();
      JPFCl = JPFCl.getSuperClass();
    }
  }
  /**
   * Updates the given JPF array according to the given JVM array.
   * 
   * @param JVMArr
   *          a JVM array
   * @param JPFArr
   *          a JPF array
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  private void updateArr (Object JVMArr, int JPFArr) throws ConversionException{
    if (JVMArr != null){
      // First check if the JPF array has been already updated
      if (!Converter.updatedJPFObj.containsKey(JPFArr)){
        Converter.updatedJPFObj.put(JPFArr, JVMArr);
        Converter.objMapJPF2JVM.put(JPFArr, JVMArr);

        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().getModifiable(JPFArr);

        ArrayFields fields = (ArrayFields) dei.getFields();

        // Array of primitive type
        if (dei.getClassInfo().getComponentClassInfo().isPrimitive()){
          this.setJPFPrimitiveArr(dei, JVMArr);
        }
        // Array of Non-primitives
        else{
          int arrSize = fields.arrayLength();
          Object[] arrObj = (Object[]) JVMArr;

          for (int i = 0; i < arrSize; i++){
            int elementValueRef = dei.getReferenceElement(i);

            if (arrObj[i] == null){
              elementValueRef = MJIEnv.NULL;
            } else if (elementValueRef == MJIEnv.NULL || Converter.objMapJPF2JVM.get(elementValueRef) != arrObj[i]){
              elementValueRef = this.getJPFObj(arrObj[i]);
            } else if (Converter.objMapJPF2JVM.get(elementValueRef) == arrObj[i]){
              this.updateJPFObj(arrObj[i], elementValueRef);
            } else{
              throw new ConversionException("Unconsidered case observed! - JVM2JPF.updateArr()");
            }
            dei.setReferenceElement(i, elementValueRef);
          }
        }
      }
    }
  }

  /**
   * Looks info the existing hash tables, declared in Converter, to see if the
   * JPF object corresponding to the given JVM object has been already created.
   * 
   * @param JVMObj
   *          a JVM object
   * 
   * @return a non-null JPF object if there already exists a JPF object
   *         corresponding to the given JVM object. OW null JPF object is
   *         returned.
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   */
  private int getExistingJPFRef (Object JVMObj, boolean update) throws ConversionException{
    int JPFRef = MJIEnv.NULL;
    boolean found = false;
    if (Converter.updatedJPFObj.containsValue(JVMObj)){
      Iterator<Integer> iterator = (Converter.updatedJPFObj.keySet()).iterator();
      Integer key;
      while (!found && iterator.hasNext()){
        key = iterator.next();
        Object value = Converter.updatedJPFObj.get(key);
        if (value == JVMObj){
          found = true;
          JPFRef = key;
        }
      }
    }

    if (!found && Converter.objMapJPF2JVM.containsValue(JVMObj)){
      Iterator<Integer> iterator = (Converter.objMapJPF2JVM.keySet()).iterator();
      Integer key;
      while (!found && iterator.hasNext()){
        key = iterator.next();
        Object value = Converter.objMapJPF2JVM.get(key);
        if (value == JVMObj){
          found = true;
          JPFRef = key;
          if (update == true){
            this.updateJPFObj(JVMObj, JPFRef);
          }
        }
      }
    }
    return JPFRef;
  }

  /**
   * Creates a new JPF object corresponding to the given JVM object. It also
   * updates the class of the JPF object according to the class of the given JVM
   * object.
   * 
   * @param JVMObj
   *          a JVM object
   * @return a new JPF object corresponding to the given JVM object.
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed
   * 
   */
  private int getNewJPFRef (Object JVMObj) throws ConversionException{
    int JPFRef = MJIEnv.NULL;
    Class<?> JVMCls = JVMObj.getClass();
    if (!JVMCls.isArray()){
      // we treat Strings differently, untill we immigrate to JDK7
      if(JVMObj.getClass()==String.class) {
        JPFRef = env.newString(JVMObj.toString());
      } else {
        ClassInfo fci = null;
        try{
          fci = this.getJPFCls(JVMCls);
        } catch (ClassInfoException e){
          System.out.println("WARNING: the class " + JVMCls + " is ignored!");
          return MJIEnv.NULL;
        }

        JPFRef = env.newObject(fci);
        this.updateObj(JVMObj, JPFRef);
      }
    } else{
      JPFRef = this.createNewJPFArray(JVMObj);
      this.updateArr(JVMObj, JPFRef);
    }

    return JPFRef;
  }

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
  private int createNewJPFArray (Object JVMArr){
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
  private static void setJPFPrimitiveField (ElementInfo ei, int index, Field fld, Object JVMObj) throws IllegalAccessException, ConversionException{
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
  private void setJPFPrimitiveArr (ElementInfo ei, Object JVMObj) throws ConversionException{
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
  
//  /**
//   * Returns a JPF class corresponding to the given JVM Class object. If such an
//   * class exists, it is updated (if it has not been updated) corresponding to
//   * the given JVMObj. Otherwise a new JPF class corresponding to the given JVM
//   * class object is created and added to the list of the JPF loaded classes.
//   * 
//   * @param JVMCls
//   *          a JVM Class object
//   * 
//   * @return a JPF class corresponding to the given JVM class, JVMCls
//   * 
//   * @throws ConversionException
//   *           if any incorrect input parameter is observed during the
//   *           conversion
//   */
//  public ClassInfo getJPFCls (Class<?> JVMCls) throws ConversionException {
//    return this.jvm2jpf.getJPFCls(JVMCls);
//  }
//
//  /**
//   * Returns a JPF object corresponding to the given JVM object. If such an
//   * object exists, it is updated (if it has not been updated) corresponding to
//   * the given JVMObj. Otherwise a new JPF object corresponding to the given JVM
//   * object is created.
//   * 
//   * @param JVMObj
//   *          a JVM object
//   * 
//   * @return a JPF object corresponding to the given JVM object, JVMObj
//   * 
//   * @throws ConversionException
//   *           if any incorrect input parameter is observed during the
//   *           conversion
//   */
//  public int getJPFObj (Object JVMObj) throws ConversionException {
//    return this.jvm2jpf.getJPFObj(JVMObj);
//  }
//
//  /**
//   * Update the given JPF object according to the given JVM object. For the case
//   * of the non-array object, its JPF class is also updated according to the
//   * class of the given JVM object.
//   * 
//   * @param JVMObj
//   *          a JVM object
//   * @param JPFObj
//   *          a JPF object
//   * 
//   * @throws ConversionException
//   *           if any incorrect input parameter is observed during the
//   *           conversion
//   */
//  public void updateJPFObj (Object JVMObj, int JPFObj) throws ConversionException {
//    this.jvm2jpf.updateJPFObj(JVMObj, JPFObj);
//  }
}