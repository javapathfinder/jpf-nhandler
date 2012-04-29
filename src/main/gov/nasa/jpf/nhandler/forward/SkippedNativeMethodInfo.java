package gov.nasa.jpf.nhandler.forward;

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.NativeMethodInfo;
import gov.nasa.jpf.jvm.NativePeer;
import gov.nasa.jpf.nhandler.PeerClassCreator;

public class SkippedNativeMethodInfo extends NativeMethodInfo {

  public SkippedNativeMethodInfo (MethodInfo mi) {
    super(mi, null, null);
  }

  protected boolean isUnsatisfiedLinkError (MJIEnv env){
    if (mth == null){
      System.out.println("*** SKIPPING - Native method " + this.ci.getName() + "." + this.name + " is NULL");
      PeerClassCreator peerCreator = PeerClassCreator.getPeerCreator(this.getClassInfo(), env);
      mth = peerCreator.createEmptyMethod(this);
      this.peer = new NativePeer(peerCreator.getPeer(), this.ci);
      assert (this.peer != null && mth != null);
    }
    return false;
  }
}
