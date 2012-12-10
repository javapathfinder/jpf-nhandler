package nhandler.forward;

import gov.nasa.jpf.vm.MethodInfo;

/** 
 * This is a subclass of NativeMethodInfo which makes executeNative() delegate
 * the execution of the unhandled native method to the JVM level.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class DelegatedNativeMethodInfo extends DelegatedMethodInfo {

  public DelegatedNativeMethodInfo (MethodInfo mi) {
    super(mi);
  }

  @Override
  protected String printInfo() {
    return("* DELEGATING Unhandled Native -> " + this.ci.getName() + "." + this.name);
  }
}
