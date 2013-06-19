package nhandler.conversion;

/**
 * Thrown when the generic converter cannot be used and there is no suitable
 * converter to use with a class
 * 
 * @author chinmay
 */
public class NoSuitableConverterFoundException extends ConversionException {

  /**
   * Construct an exception without message
   */
  public NoSuitableConverterFoundException () {
    super();
  }

  /**
   * Construct an exception with message
   * 
   * @param message
   */
  public NoSuitableConverterFoundException (String message) {
    super(message);
  }
}
