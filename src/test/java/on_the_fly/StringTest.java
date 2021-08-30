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

import org.junit.Test;

public class StringTest extends TestJPF {
  // override vm.class property as it may have been set by other extensions of JPF
  private final static String[] JPF_ARGS = { "+nhandler.spec.delegate=java.lang.String.*", 						"+test.vm.class = gov.nasa.jpf.vm.SingleProcessVM" };

  public static void main (String[] args){
    runTestsOfThisClass(args);
  }

  @Test
  public void testEquals (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertFalse("JPF-NHANDLER".equals("jpf-nhandler"));
      assertTrue("jpf-nhandler".equals("jpf-nhandler"));
      assertTrue("".equals(""));
      assertFalse("".equals(null));
    }
  }

  @Test
  public void testEqualsIgnoreCase (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertTrue("JPF-NHANDLER".equalsIgnoreCase("jpf-nhandler"));
      assertTrue("jpf-NHANDLER".equalsIgnoreCase("JPF-nhandler"));
    }
  }

  @Test
  public void testToCharArray (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      char[] array1 = "hello".toCharArray();
      char[] array2 = new char[] { 'h', 'e', 'l', 'l' };
      for (int i = 0; i < array2.length; i++){
        assertEquals(array1[i], array2[i]);
      }
    }
  }

  @Test
  public void testIndexOf (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      String name = "nastaran";
      assertEquals(name.indexOf("a"), 1);
      assertEquals(name.indexOf("m"), -1);
      assertEquals(name.indexOf('x'), -1);
      assertEquals(name.indexOf('s', 3), -1);
      assertEquals(name.indexOf("tar"), 3);
      assertEquals(name.indexOf(""), 0);
    }
  }

  @Test
  public void testLastIndexOf (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      String name = "nastaran";
      assertEquals(3, name.lastIndexOf("t"));
      assertEquals(-1, name.lastIndexOf("t", 2));
    }
  }

  @Test
  public void testMatches (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertFalse("aaabbb".matches("b*"));
      assertFalse("aaabbb".matches("b+"));
      assertFalse("aaabbb".matches(""));
      assertTrue("abbb".matches("a.*b"));
    }
  }

  @Test
  public void testGetBytes (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      String str = "Hello World!";
      byte[] bytes = str.getBytes();
      assertEquals(str.length(), bytes.length);
      for (int i = 0; i < str.length(); ++i){
        assertEquals((byte) str.charAt(i), bytes[i]);
      }
    }
  }

  @Test
  public void testToUpperCase (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertEquals("JPF-NHANDLER", "jpf-nhandler".toUpperCase());
      assertEquals("JPF-NHANDLER", "jpf-NHAndler".toUpperCase());
    }
  }

  @Test
  public void testToLowerCase (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertEquals("jpf-nhandler", "JPF-NHANDLER".toLowerCase());
      assertEquals("jpf-nhandler", "jpf-NHANdLeR".toLowerCase());
    }
  }

  @Test
  public void testSplit (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      String str = "aaa:bbb:ccc";
      String[] splited = str.split(":");
      assertEquals(splited.length, 3);
      assertEquals(splited[0], "aaa");
      assertEquals(splited[1], "bbb");
      assertEquals(splited[2], "ccc");
    }
  }

  @Test
  public void testTrim (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertEquals(" hello ".trim(), "hello");
    }
  }

  @Test
  public void testConcat (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertEquals("aaa".concat("bbb"), "aaabbb");
    }
  }

  @Test
  public void testReplace (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      assertEquals("ababa".replace("a", "b"), "bbbbb");
      assertEquals("b b b".replace(' ', 'b'), "bbbbb");
    }
  }

  @Test
  public void testIntern (){
    if (verifyNoPropertyViolation(JPF_ARGS)){
      String str1 = String.valueOf(new char[] { 'h', 'e', 'l', 'l', 'o'});
      String str2 = String.valueOf(new char[] { 'h', 'e', 'l', 'l', 'o'});
      assertTrue(str1.equals(str2));
      // Note that in reality str1.intern()==str2.intern() but due
      // to nhandler limitation we end up with just equal strings
      assertEquals(str1.intern(), str2.intern());
    }
  }
}