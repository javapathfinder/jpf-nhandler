package nhandler.forward;

import nhandler.peerGen.PeerClassGen;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.NativePeer;

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
      System.out.println("* DELEGATING Unhandled Native -> " + this.ci.getName() + "." + this.name);
      PeerClassGen peerCreator = PeerClassGen.getPeerCreator(this.getClassInfo(), env);
      mth = peerCreator.createMethod(this);

      Class<?> peerClass = peerCreator.getPeer();
      this.peer = NativePeer.getInstance(peerClass, NativePeer.class);
      this.peer.initialize(peerClass, this.ci, true);

      //this.peer = new NativePeer(peerCreator.getPeer(), this.ci);
      assert (this.peer != null && mth != null);
    }

    return false;
  }
}
