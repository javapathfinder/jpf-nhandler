package gov.nasa.jpf.jvm;

/**
 * A ConversionException is thrown when an incorrect input parameters are sent
 * to a methods of Converter, JPF2JVM, and JVM2JPF classes.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class ConversionException extends Exception {
  /**
   * Constructs a ConversionException without a detail message.
   */
  public ConversionException () {
    super();
  }

  /**
   * Constructs an ConversionException with a detail message.
   * 
   * @param s
   *          the detail message.
   */
  public ConversionException (String s) {
    super(s);
  }
}
