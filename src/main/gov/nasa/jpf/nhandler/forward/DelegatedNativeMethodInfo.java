package gov.nasa.jpf.nhandler.forward;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.NativeMethodInfo;
import gov.nasa.jpf.jvm.NativePeer;
import gov.nasa.jpf.nhandler.PeerClassCreator;

/** 
 * This is a subclass of NativeMethodInfo which makes executeNative() delegate
 * the execution of the unhandled native method to the JVM level.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class DelegatedNativeMethodInfo extends NativeMethodInfo {

  public DelegatedNativeMethodInfo (MethodInfo mi) {
    super(mi, null, null);
  }

  protected boolean isUnsatisfiedLinkError (MJIEnv env){
    if (mth == null){
      System.out.println("*** DELEGATING - Unhandled Native method " + this.ci.getName() + "." + this.name + " is NULL");
      PeerClassCreator peerCreator = PeerClassCreator.getPeerCreator(this.getClassInfo(), env);
      mth = peerCreator.createMethod(this);
      this.peer = new NativePeer(peerCreator.getPeer(), this.ci);
      assert (this.peer != null && mth != null);
    }

    return false;
  }
}
