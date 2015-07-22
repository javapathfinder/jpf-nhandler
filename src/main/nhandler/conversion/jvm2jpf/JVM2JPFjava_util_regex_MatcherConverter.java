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

package nhandler.conversion.jvm2jpf;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.JPF_java_util_regex_Matcher;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.StaticElementInfo;
import nhandler.conversion.ConversionException;

public class JVM2JPFjava_util_regex_MatcherConverter extends JVM2JPFConverter {

  @Override
  protected void setStaticFields (Class<?> JVMCls, StaticElementInfo sei, MJIEnv env) throws ConversionException {
    // TODO Auto-generated method stub

  }

  /**
   * This works like JVM2JPF for java.text.DecimalFormat
   * Please see that for explanation
   * We only update if the JVMobj was not a delegatee
   */
  @Override
  protected void setInstanceFields (Object JVMObj, DynamicElementInfo dei, MJIEnv env) throws ConversionException {
    assert JVMObj instanceof Matcher : "Not the right converter";
    
    int id = dei.getIntField("id");
    
    Field matchersField = null;
    try {
      matchersField = JPF_java_util_regex_Matcher.class.getDeclaredField("matchers");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    matchersField.setAccessible(true);
    
    Map<Integer, Matcher> matchers = null;
    NativePeer nativePeer = dei.getClassInfo().getNativePeer();
    
    try {
      matchers = (Map<Integer, Matcher>) matchersField.get(nativePeer);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    assert matchers != null;
    if (JVM2JPFUtilities.hasMapExactObject(matchers, JVMObj)) {
      Matcher value = matchers.get(id);
      assert value != null;
      assert value == JVMObj;   // This is an update call, so JVMObj is an existing delegatee
    } else { // This is not an update call
      // Update nInstances:
      ClassInfo ci = dei.getClassInfo();
      StaticElementInfo sei = ci.getModifiableStaticElementInfo();
      int nInstances = sei.getIntField("nInstances");
      dei.setIntField("id", nInstances);
      sei.setIntField("nInstances", nInstances + 1);
      
      // Set the "input" field:
      setInputField((Matcher) JVMObj, dei, env);
      
      // Set the "pattern" field:
      int JPFPat = obtainJPFObj(((Matcher) JVMObj).pattern(), env);
      dei.setReferenceField("pattern", JPFPat);
      
      matchers.put(nInstances, (Matcher) JVMObj);
    }
  }
  
  /**
   * Set the "input" field (on which the Matcher operates)
   * from the given JVM Matcher object
   * @param matcher JVM Matcher object
   * @param dei DynamicElementInfo for the JPF object to set the field on
   */
  private void setInputField(Matcher matcher, DynamicElementInfo dei, MJIEnv env) throws ConversionException{
    Field field = null;
    try {
      field = Matcher.class.getDeclaredField("text");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    }
    
    field.setAccessible(true);
    
    CharSequence cs = null;
    
    try {
      cs = (CharSequence) field.get(matcher);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    
    int JPFString = obtainJPFObj(new StringBuilder(cs).toString(), env);
    
    System.out.println(cs);
    dei.setReferenceField("input", JPFString);
  }

}
