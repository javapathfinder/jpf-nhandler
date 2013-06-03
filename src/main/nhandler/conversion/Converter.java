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
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ReferenceArrayFields;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
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

  JVM2JPF jvm2jpf;
  
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

    this.jvm2jpf = new JVM2JPF(env);
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
   *           if any incorrect input parameter is observed during the
   *           conversion
   */
  public ClassInfo getJPFCls (Class<?> JVMCls) throws ConversionException {
    return this.jvm2jpf.getJPFCls(JVMCls);
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
   *           if any incorrect input parameter is observed during the
   *           conversion
   */
  public int getJPFObj (Object JVMObj) throws ConversionException {
    return this.jvm2jpf.getJPFObj(JVMObj);
  }

  /**
   * Update the given JPF object according to the given JVM object. For the case
   * of the non-array object, its JPF class is also updated according to the
   * class of the given JVM object.
   * 
   * @param JVMObj
   *          a JVM object
   * @param JPFObj
   *          a JPF object
   * 
   * @throws ConversionException
   *           if any incorrect input parameter is observed during the
   *           conversion
   */
  public void updateJPFObj (Object JVMObj, int JPFObj) throws ConversionException {
    this.jvm2jpf.updateJPFObj(JVMObj, JPFObj);
  }
}