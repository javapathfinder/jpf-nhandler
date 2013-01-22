import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class SimpleSender extends ReceiverAdapter {
  JChannel channel;

  String user_name = System.getProperty("user.name", "n/a") + "-" + "sender";

  private void start () throws Exception {
    System.out.println("Starting...");
    channel = new JChannel();
    channel.setReceiver(this);

    // joining a cluster
    channel.connect("SimpleCluster");
    // sendMessage("Is any body out there?");
  }

  private void sendMessage (String text) {
    String line = "[" + user_name + "] " + text;
    Message msg = new Message(null, null, line);
    try {
      channel.send(msg);
      // System.exit(0);
    } catch (Exception e) {
      System.out.println("Sending failed!");
    }
  }

  // The viewAccepted() callback is called whenever a new instance
  // joins the cluster, or an existing instance leaves (crashes included)
  public void viewAccepted (View new_view) {
    System.out.println("** view: " + new_view);
  }

  public void receive (Message msg) {
    String recievedMsg = msg.getObject().toString();
    System.out.println(msg.getSrc() + ": " + recievedMsg);
    // Thread.dumpStack();
    // System.exit(0);
    if (recievedMsg.contains("I am here!")) {
      System.out.println("sender is closing the channel");
      channel.close();
      // System.exit(0);
    }
  }

  public static void main (String[] args) throws Exception {
    // This allows Java applications to connect too, and accept connections
    // from, both IPv4 and IPv6 hosts.
    System.setProperty("java.net.preferIPv4Stack", "true");

    SimpleSender ss = new SimpleSender();
    ss.start();
    ss.sendMessage("Is any body out there?");
    ss.channel.close();
  }
}
