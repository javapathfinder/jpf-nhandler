package reflection;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

public class StringTest extends TestJPF {
  private final static String[] JPF_ARGS = { "+nhandler.delegate.spec=java.lang.String.equals," +
  		                                     "java.lang.String.equalsIgnoreCase," +
  		                                     "java.lang.String.toCharArray," +
  		                                     "java.lang.String.split," +
  		                                     "java.lang.String.valueOf"};

  public static void main (String[] args) {
    runTestsOfThisClass(args);
  }

  @Test
  public void testEquals () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      assertFalse("ABC".equals("abc"));
      assertFalse("abc".equals("ABC"));
      assertTrue("abc".equals("abc"));
      assertTrue("ABC".equals("ABC"));
      assertFalse("AbC".equals("aBC"));
      assertFalse("AbC".equals("aBC"));
      assertTrue("".equals(""));
      assertFalse("".equals(null));
    }
  }

  @Test
  public void testEqualsIgnoreCase () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      assertTrue("ABC".equalsIgnoreCase("abc"));
      assertTrue("abc".equalsIgnoreCase("ABC"));
      assertTrue("abc".equalsIgnoreCase("abc"));
      assertTrue("ABC".equalsIgnoreCase("ABC"));
      assertTrue("AbC".equalsIgnoreCase("aBC"));
      assertTrue("AbC".equalsIgnoreCase("aBC"));
      assertTrue("".equalsIgnoreCase(""));
      assertFalse("".equalsIgnoreCase(null));
    }
  }

  @Test
  public void testToCharArray () {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      char[] a1 = "abc".toCharArray();
      char[] a2 = new char[] { 'a', 'b', 'c' };
      for (int i = 0; i < a1.length; i++) {
        assertEquals(a1[i], a2[i]);
      }
    }
  }

//  @Test
//  public void testIndexOf () {
//    if (verifyNoPropertyViolation()) {
//      String haystack = "abcdefghi";
//      assertEquals(haystack.indexOf("q"), -1);
//      assertEquals(haystack.indexOf('q'), -1);
//      assertEquals(haystack.indexOf("a"), 0);
//      assertEquals(haystack.indexOf('a'), 0);
//      assertEquals(haystack.indexOf('a', 1), -1);
//      assertEquals(haystack.indexOf("bc"), 1);
//      assertEquals(haystack.indexOf(""), 0);
//    }
//  }
//
//  @Test
//  public void testLastIndexOf () {
//    if (verifyNoPropertyViolation()) {
//      String x = "abcdeabcdef";
//      assertEquals(9, x.lastIndexOf("e"));
//      assertEquals(10, x.lastIndexOf("f"));
//      assertEquals(-1, x.lastIndexOf("f", 1));
//    }
//  }
//
//  @Test
//  public void testHashCode () {
//    if (verifyNoPropertyViolation()) {
//      String[] testStrings = { "watch", "unwatch", "toString", "toSource", "eval", "valueOf", "constructor", "__proto__", "polygenelubricants", "xy", "x", "" };
//      int[] javaHashes = { 112903375, -274141738, -1776922004, -1781441930, 3125404, 231605032, -1588406278, 2139739112, Integer.MIN_VALUE, 3841, 120, 0 };
//
//      for (int i = 0; i < testStrings.length; ++i) {
//        String testString = testStrings[i];
//        int expectedHash = javaHashes[i];
//
//        // verify that the hash codes of these strings match their java
//        // counterparts
//        assertEquals("Unexpected hash for string " + testString, expectedHash, testString.hashCode());
//
//        /*
//         * Verify that the resulting hash code is numeric, since this is not
//         * enforced in Production Mode.
//         */
//        String str = Integer.toString(expectedHash);
//        for (int j = 0; j < str.length(); ++j) {
//          char ch = str.charAt(j);
//          assertTrue("Bad character '" + ch + "' (U+0" + Integer.toHexString(ch) + ")", ch == '-' || ch == ' ' || Character.isDigit(ch));
//        }
//
//        // get hashes again to verify the values are constant for a given string
//        assertEquals(expectedHash, testStrings[i].hashCode());
//      }
//    }
//  }
//
//  @Test
//  public void testMatches () {
//    if (verifyNoPropertyViolation()) {
//      assertFalse("1f", "abbbbcd".matches("b*"));
//      assertFalse("2f", "abbbbcd".matches("b+"));
//      assertTrue("3t", "abbbbcd".matches("ab*bcd"));
//      assertTrue("4t", "abbbbcd".matches("ab+cd"));
//      assertTrue("5t", "abbbbcd".matches("ab+bcd"));
//      assertFalse("6f", "abbbbcd".matches(""));
//      assertTrue("7t", "abbbbcd".matches("a.*d"));
//      assertFalse("8f", "abbbbcd".matches("a.*e"));
//    }
//  }
//
//  @Test
//  public void testGetBytes () {
//    if (verifyNoPropertyViolation()) {
//      String str = "This is a simple ASCII string";
//      byte[] bytes = str.getBytes();
//      assertEquals(str.length(), bytes.length);
//      for (int i = 0; i < str.length(); ++i) {
//        assertEquals((byte) str.charAt(i), bytes[i]);
//      }
//    }
//  }
//
//  @Test
//  public void testToUpperCase () {
//    if (verifyNoPropertyViolation()) {
//      assertEquals("ABC", "AbC".toUpperCase());
//      assertEquals("ABC", "abc".toUpperCase());
//      assertEquals("", "".toUpperCase());
//    }
//  }
//
//  @Test
//  public void testToLowerCase () {
//    if (verifyNoPropertyViolation()) {
//      assertEquals("abc", "AbC".toLowerCase());
//      assertEquals("abc", "abc".toLowerCase());
//      assertEquals("", "".toLowerCase());
//    }
//  }

  private static <T> T hideFromCompiler (T value) {
    int i = 7;
    while (i > 0) {
      i -= 2;
    }
    return (i & 1) != 0 ? value : null;
  }

  private void compareList (String category, String[] desired, String[] got) {
    assertEquals(category + " length", desired.length, got.length);
    for (int i = 0; i < desired.length; i++) {
      assertEquals(category + " " + i, desired[i], got[i]);
    }
  }

  @Test
  public void testSplit () {
    if (verifyNoPropertyViolation()) {
      compareList("fullSplit", new String[] { "abc", "", "", "de", "f" }, hideFromCompiler("abcxxxdexfxx").split("x"));
      compareList("emptyRegexSplit", new String[] { "", "a", "b", "c", "x", "x", "d", "e", "x", "f", "x" }, hideFromCompiler("abcxxdexfx").split(""));
      String booAndFoo = hideFromCompiler("boo:and:foo");
      compareList("2:", new String[] { "boo", "and:foo" }, booAndFoo.split(":", 2));
      compareList("5:", new String[] { "boo", "and", "foo" }, booAndFoo.split(":", 5));
      compareList("-2:", new String[] { "boo", "and", "foo" }, booAndFoo.split(":", -2));
      compareList("5o", new String[] { "b", "", ":and:f", "", "" }, booAndFoo.split("o", 5));
      compareList("-2o", new String[] { "b", "", ":and:f", "", "" }, booAndFoo.split("o", -2));
      compareList("0o", new String[] { "b", "", ":and:f" }, booAndFoo.split("o", 0));
      compareList("0:", new String[] { "boo", "and", "foo" }, booAndFoo.split(":", 0));
      // issue 2742
      compareList("issue2742", new String[] {}, hideFromCompiler("/").split("/", 0));

      // Splitting an empty string should result in an array containing a single
      // empty string.
      String[] s = "".split(",");
      assertTrue(s != null);
      assertTrue(s.length == 1);
      assertTrue(s[0] != null);
      assertTrue(s[0].length() == 0);
    }
  }

//  public void trimRightAssertEquals (String left, String right) {
//    assertEquals(left, right.trim());
//  }
//
//  @Test
//  public void testTrim () {
//    if (verifyNoPropertyViolation()) {
//      assertEquals("abc", "   \t abc \n  ".trim());
//      assertEquals("abc", "abc".trim());
//      assertEquals("abc", " abc".trim());
//      assertEquals("abc", "abc  ".trim());
//      assertEquals("", "".trim());
//      assertEquals("", "   \t ".trim());
//    }
//  }
//
  @Test
  public void testConcat () {
    if (verifyNoPropertyViolation()) {
      String abc = String.valueOf(new char[] { 'a', 'b', 'c' });
      String def = String.valueOf(new char[] { 'd', 'e', 'f' });
      String empty = String.valueOf(new char[] {});
      assertEquals("abcdef", abc + def);
      assertEquals("abcdef", abc.concat(def));
      assertEquals("", empty.concat(empty));
      char c = def.charAt(0);
      String s = abc;
      assertEquals("abcd", abc + 'd');
      assertEquals("abcd", abc + c);
      assertEquals("abcd", s + 'd');
      assertEquals("abcd", s + c);
      s += c;
      assertEquals("abcd", s);
    }
  }

//  private String toS (char from) {
//    return Character.toString(from);
//  }
//
//  @Test
//  public void testReplace () {
//    if (verifyNoPropertyViolation()) {
//      String axax = String.valueOf(new char[] { 'a', 'x', 'a', 'x' });
//      String aaaa = String.valueOf(new char[] { 'a', 'a', 'a', 'a' });
//      assertEquals("aaaa", axax.replace('x', 'a'));
//      assertEquals("aaaa", aaaa.replace('x', 'a'));
//      for (char from = 32; from < 250; ++from) {
//        char to = (char) (from + 5);
//        assertEquals(toS(to), toS(from).replace(from, to));
//      }
//      for (char to = 32; to < 250; ++to) {
//        char from = (char) (to + 5);
//        assertEquals(toS(to), toS(from).replace(from, to));
//      }
//      // issue 1480
//      String exampleXd = String.valueOf(new char[] { 'e', 'x', 'a', 'm', 'p', 'l', 'e', ' ', 'x', 'd' });
//      assertEquals("example xd", exampleXd.replace('\r', ' ').replace('\n', ' '));
//      String dogFood = String.valueOf(new char[] { 'd', 'o', 'g', '\u0120', 'f', 'o', 'o', 'd' });
//      assertEquals("dog food", dogFood.replace('\u0120', ' '));
//      String testStr = String.valueOf(new char[] { '\u1111', 'B', '\u1111', 'B', '\u1111', 'B' });
//      assertEquals("ABABAB", testStr.replace('\u1111', 'A'));
//    }
//  }
//
//  @Test
//  public void testReplaceAll () {
//    if (verifyNoPropertyViolation()) {
//      String regex = hideFromCompiler("*[").replaceAll("([/\\\\\\.\\*\\+\\?\\|\\(\\)\\[\\]\\{\\}])", "\\\\$1");
//      assertEquals("\\*\\[", regex);
//      String replacement = hideFromCompiler("\\").replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$", "\\\\$");
//      assertEquals("\\\\", replacement);
//      assertEquals("+1", hideFromCompiler("*[1").replaceAll(regex, "+"));
//      String x1 = String.valueOf(new char[] { 'x', 'x', 'x', 'a', 'b', 'c', 'x', 'x', 'd', 'e', 'x', 'f' });
//      assertEquals("abcdef", x1.replaceAll("x*", ""));
//      String x2 = String.valueOf(new char[] { '1', 'a', 'b', 'c', '1', '2', '3', 'd', 'e', '1', '2', '3', '4', 'f' });
//      assertEquals("1\\1abc123\\123de1234\\1234f", x2.replaceAll("([1234]+)", "$1\\\\$1"));
//      String x3 = String.valueOf(new char[] { 'x', ' ', ' ', 'x' });
//      assertEquals("\n  \n", x3.replaceAll("x", "\n"));
//      String x4 = String.valueOf(new char[] { '\n', ' ', ' ', '\n' });
//      assertEquals("x  x", x4.replaceAll("\\\n", "x"));
//      String x5 = String.valueOf(new char[] { 'x' });
//      assertEquals("x\"\\", x5.replaceAll("x", "\\x\\\"\\\\"));
//      assertEquals("$$x$", x5.replaceAll("(x)", "\\$\\$$1\\$"));
//    }
//  }
//
//  @Test
//  public void testReplaceString () {
//    if (verifyNoPropertyViolation()) {
//      assertEquals("foobar", hideFromCompiler("bazbar").replace("baz", "foo"));
//      assertEquals("$0bar", hideFromCompiler("foobar").replace("foo", "$0"));
//      assertEquals("$1bar", hideFromCompiler("foobar").replace("foo", "$1"));
//      assertEquals("\\$1bar", hideFromCompiler("foobar").replace("foo", "\\$1"));
//      assertEquals("\\1", hideFromCompiler("*[)1").replace("*[)", "\\"));
//
//      // issue 2363
//      assertEquals("cb", hideFromCompiler("$ab").replace("$a", "c"));
//      assertEquals("cb", hideFromCompiler("^ab").replace("^a", "c"));
//
//      // test JS replacement characters
//      assertEquals("a$$b", hideFromCompiler("a[x]b").replace("[x]", "$$"));
//      assertEquals("a$1b", hideFromCompiler("a[x]b").replace("[x]", "$1"));
//      assertEquals("a$`b", hideFromCompiler("a[x]b").replace("[x]", "$`"));
//      assertEquals("a$'b", hideFromCompiler("a[x]b").replace("[x]", "$'"));
//    }
//  }
//  
//  @Test
//  public void testIntern () {
//    if (verifyNoPropertyViolation()) {
//      String s1 = String.valueOf(new char[] { 'a', 'b', 'c', 'd', 'e', 'f' });
//      String s2 = String.valueOf(new char[] { 'a', 'b', 'c', 'd', 'e', 'f' });
//      assertTrue("strings not equal", s1.equals(s2));
//      assertEquals("interns are not equal", s1.intern(), s2.intern());
//    }
//  }

}
