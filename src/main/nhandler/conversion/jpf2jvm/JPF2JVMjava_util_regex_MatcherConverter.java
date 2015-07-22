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

package nhandler.conversion.jpf2jvm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;

import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_util_regex_Matcher;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JPF2JVMjava_util_regex_MatcherConverter extends JPF2JVMConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {

  }

  @Override
  protected Object instantiateFrom (Class<?> cl, int JPFRef, MJIEnv env) {
    assert cl == Matcher.class : "Not the right converter";
    
    Object JVMObj = null;
    JVMObj = getInstanceFromNativePeer(JPFRef, env);
    return JVMObj;
  }
  
  /**
   * Get delegatee object from the native peer
   * @param JPFRef
   * @param env
   * @return
   */
  private Matcher getInstanceFromNativePeer(int JPFRef, MJIEnv env) {
    Matcher matcher = null;
    
    JPF_java_util_regex_Matcher nativePeer = (JPF_java_util_regex_Matcher) env.getClassInfo(JPFRef).getNativePeer();
    Method getInstance = null;
    try {
      getInstance = JPF_java_util_regex_Matcher.class.getDeclaredMethod("getInstance", MJIEnv.class, int.class);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    getInstance.setAccessible(true);
    
    try {
      matcher = (Matcher) getInstance.invoke(nativePeer, env, JPFRef);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return matcher;
  }

}
