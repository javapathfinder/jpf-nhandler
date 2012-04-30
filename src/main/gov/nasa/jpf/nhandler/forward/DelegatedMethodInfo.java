package gov.nasa.jpf.nhandler.forward;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.NativeMethodInfo;
import gov.nasa.jpf.jvm.NativePeer;
import gov.nasa.jpf.nhandler.PeerClassCreator;

/** 
 * This is a subclass of NativeMethodInfo which makes executeNative() delegate
 * the execution of the method to the JVM level.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class DelegatedMethodInfo extends NativeMethodInfo {

  public DelegatedMethodInfo (MethodInfo mi) {
    super(mi, null, null);
  }

  protected boolean isUnsatisfiedLinkError (MJIEnv env){
    System.out.println("*** DELEGATING - Native method " + this.ci.getName() + "." + this.name + " is NULL");
    PeerClassCreator peerCreator = PeerClassCreator.getPeerCreator(this.getClassInfo(), env);
    mth = peerCreator.createMethod(this);
    this.peer = new NativePeer(peerCreator.getPeer(), this.ci);
    assert (this.peer != null && mth != null);
    return false;
  }
}
