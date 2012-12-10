package nhandler.forward;

import nhandler.peerGen.PeerClassGen;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.NativePeer;

/**
 * This is a subclass of NativeMethodInfo which makes executeNative() skip the
 * execution of the unhandled native method by executing the corresponding empty 
 * method at the JVM level which returns a dummy value.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class SkippedNativeMethodInfo extends SkippedMethodInfo {

  public SkippedNativeMethodInfo (MethodInfo mi) {
    super(mi);
  }

  @Override
  protected String printInfo() {
    return("* SKIPPING Unhandled Native -> " + this.ci.getName() + "." + this.name);
  }
}
