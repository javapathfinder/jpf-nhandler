package nhandler.forward;

import nhandler.peerGen.PeerClassGen;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativePeer;

/** 
 * This is a subclass of NativeMethodInfo which makes executeNative() skip the
 * execution of the method by executing the corresponding empty method at the 
 * JVM level which returns a dummy value.
 * 
 * @author Nastaran Shafiei
 * @author Franck van Breugel
 */
public class SkippedMethodInfo extends HandledMethodInfo {

  public SkippedMethodInfo (MethodInfo mi) {
    super(mi);
  }

  protected boolean isUnsatisfiedLinkError (MJIEnv env){
    if(mth == null){
      System.out.println(printInfo());

      PeerClassGen peerCreator = PeerClassGen.getPeerCreator(this.getClassInfo(), env);
      mth = peerCreator.createEmptyMethod(this);

      Class<?> peerClass = peerCreator.getPeer();
      this.peer = NativePeer.getInstance(peerClass, NativePeer.class);
      this.peer.initialize(peerClass, this.ci, true);

      //this.peer = new NativePeer(peerCreator.getPeer(), this.ci);
      assert (this.peer != null && mth != null);
    }

    return false;
  }

  @Override
  protected String printInfo() {
    return("* SKIPPING -> " + this.ci.getName() + "." + this.name);
  }
}
