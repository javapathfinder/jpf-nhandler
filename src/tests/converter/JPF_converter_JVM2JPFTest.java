package converter;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.nhandler.conversion.ConversionException;
import gov.nasa.jpf.nhandler.conversion.Converter;
import gov.nasa.jpf.nhandler.conversion.JVM2JPF;
import gov.nasa.jpf.util.test.TestJPF;

public class JPF_converter_JVM2JPFTest extends TestJPF {

  public static int createJPFInt____Ljava_lang_Object_2 (MJIEnv env, int objRef) throws ConversionException {
    System.out.println("peer4JVM2JPFTest-2");
    Integer i1 = new Integer(100);
    Converter converter = new Converter(env);
    int i = (new JVM2JPF(env)).getJPFObj(i1);
    converter.updateJPFObj(i1, i);
    return i;
  }
}
