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
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Fields;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Converts objects and classes from JVM to JPF.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class JVM2JPF {

  private MJIEnv env;

  public JVM2JPF (MJIEnv env) {
    this.env = env;
  }

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
      JPFCls = ClassInfo.getResolvedClassInfo(JVMCls.getName());
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
    }
    return JPFCls;
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
        ClassInfo ci = this.getJPFCls(JVMObj.getClass());
        DynamicElementInfo dei = (DynamicElementInfo) env.getHeap().getModifiable(JPFObj);

        List<Class<?>> JVMClsList = new LinkedList<Class<?>>();
        List<ClassInfo> JPFClsList = new LinkedList<ClassInfo>();

        Class<?> JVMCl = JVMObj.getClass();
        ClassInfo JPFCl = ci;

        // Include declared fields along with all the fields inherited from
        // ancestors
        do{
          JVMClsList.add(JVMCl);
          JVMCl = JVMCl.getSuperclass();

          JPFClsList.add(JPFCl);
          JPFCl = JPFCl.getSuperClass();
        } while (JVMCl != null && JPFCl != null);

        while (!JVMClsList.isEmpty()){
          int index = JVMClsList.size() - 1;
          JVMCl = JVMClsList.remove(index);
          JPFCl = JPFClsList.remove(index);

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
        }
      }
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
   * Sets an element of the given JPF array to the same value as the given JVM
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
}
