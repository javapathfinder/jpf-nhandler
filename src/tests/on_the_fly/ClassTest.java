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

package on_the_fly;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.Serializable;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

public class ClassTest<E> extends TestJPF {
  private final static String[] JPF_ARGS = { "+nhandler.spec.delegate=java.lang.Class.*" };

  public static void main (String[] args){
    runTestsOfThisClass(args);
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  public @interface TestAnnotation {
  }

  @TestAnnotation()
  public static class ParentAnnotated {
  }

  public static class ChildAnnotated extends ParentAnnotated {
  }

  public enum TestEnum{
    item;
  }

  @TestAnnotation()
  public static class TestEnclosedClass {
    public Object foo;

    public TestEnclosedClass () {
      class LocalClass {
      }
      ;
      foo = new LocalClass();
    }

    public static class MemberClass {

    }

    public Object getLocalClassObj (){

      class LocalClass {
      }
      ;

      return new LocalClass();
    }

    public Object getAnonymousClassObj (){
      return new Object() {
      };

    }
  }

  @Test
  public void getCanonicalNameTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertEquals(ArrayList.class.getCanonicalName(), "java.util.ArrayList");
      assertEquals(Class.class.getCanonicalName(), "java.lang.Class");
      assertEquals(String.class.getCanonicalName(), "java.lang.String");
      assertEquals((new Object[0]).getClass().getCanonicalName(), "java.lang.Object[]");
    }
  }

  @Test
  public void getDeclaredAnnotationsTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertTrue(ClassTest.ParentAnnotated.class.getDeclaredAnnotations().length == 1);
      assertTrue(ChildAnnotated.class.getDeclaredAnnotations().length == 0);
    }
  }

  @Test
  public void getDeclaredClassesTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Class<?>[] cls = Class.class.getDeclaredClasses();
      assertTrue(cls.length == 2);
      assertEquals(cls[0].getName(), "java.lang.Class$MethodArray");
      assertEquals(cls[1].getName(), "java.lang.Class$EnclosingMethodInfo");
    }
  }

  @Test
  public void getDeclaringClassTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Class<?>[] cls = Class.class.getDeclaredClasses();
      assertTrue(cls.length == 2);
      assertEquals(cls[0].getName(), "java.lang.Class$MethodArray");
      assertEquals(cls[1].getName(), "java.lang.Class$EnclosingMethodInfo");
      assertTrue(cls[0].getDeclaringClass() == Class.class);
      assertTrue(cls[1].getDeclaringClass() == Class.class);
      assertTrue(Class.class.getDeclaringClass() == null);
    }
  }

  @Test
  public void getEnclosingClassTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Class<?>[] cls = Class.class.getDeclaredClasses();
      assertTrue(cls.length == 2);
      assertEquals(cls[0].getName(), "java.lang.Class$MethodArray");
      assertEquals(cls[1].getName(), "java.lang.Class$EnclosingMethodInfo");
      assertTrue(cls[0].getEnclosingClass() == Class.class);
      assertTrue(cls[1].getEnclosingClass() == Class.class);
      assertTrue(Class.class.getEnclosingClass() == null);
    }
  }

  @Test
  public void getFieldsTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertTrue(TestEnclosedClass.class.getFields().length == 1);
      assertEquals(TestEnclosedClass.class.getFields()[0].getName(), "foo");
    }
  }

  @Test
  public void getGenericInterfacesTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Type[] type = Class.class.getGenericInterfaces();
      assertTrue(type.length == 4);
      assertSame(type[0], Serializable.class);
      assertSame(type[1], GenericDeclaration.class);
      assertSame(type[2], Type.class);
      assertSame(type[3], AnnotatedElement.class);
    }
  }

  @Test
  public void getGenericSuperclassTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertSame(Class.class.getGenericSuperclass(), Object.class);
      assertSame(BigDecimal.class.getGenericSuperclass(), Number.class);
      assertSame(Object.class.getGenericSuperclass(), null);
    }
  }

  @Test
  public void getProtectionDomainTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      ProtectionDomain pd = ClassTest.class.getProtectionDomain();
      assertTrue(pd.getPrincipals().length == 0);
      assertNotNull(pd.getCodeSource().getLocation());
      assertEquals(pd.getCodeSource().getLocation(), ClassTest.TestEnclosedClass.class.getProtectionDomain().getCodeSource().getLocation());
    }
  }

  @Test
  public void getSignersTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Class.class.getSigners();
    }
  }

  @Test
  public void getTypeParametersTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertTrue(ArrayList.class.getTypeParameters().length == 1);
      assertEquals(ArrayList.class.getTypeParameters()[0].getName(), "E");

      assertTrue(HashMap.class.getTypeParameters().length == 2);
      assertEquals(HashMap.class.getTypeParameters()[0].getName(), "K");
      assertEquals(HashMap.class.getTypeParameters()[1].getName(), "V");
    }
  }

  @Test
  public void isAnonymousClassTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Class<?> cls = (new ClassTest.TestEnclosedClass()).getAnonymousClassObj().getClass();
      assertTrue(cls.isAnonymousClass());
      assertFalse(Class.class.isAnonymousClass());
    }
  }

  @Test
  public void isEnumTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertTrue(TestEnum.class.isEnum());
      assertFalse(Class.class.isEnum());
    }
  }

  @Test
  public void isLocalClassTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      TestEnclosedClass testObj = new ClassTest.TestEnclosedClass();
      assertTrue(testObj.foo.getClass().isLocalClass());
      assertTrue(testObj.getLocalClassObj().getClass().isLocalClass());
      assertFalse(Class.class.isLocalClass());
    }
  }

  @Test
  public void isMemberClassTest (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertTrue(TestEnclosedClass.MemberClass.class.isMemberClass());
      assertFalse(Class.class.isMemberClass());
      assertFalse(((new TestEnclosedClass()).getLocalClassObj().getClass().isMemberClass()));
    }
  }

  // Note: extend this test to a class that returns true isSynthetic() is
  // invoked
  @Test
  public void isSyntheticTest () throws ClassNotFoundException, SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertFalse(Class.class.isSynthetic());
    }
  }
}
