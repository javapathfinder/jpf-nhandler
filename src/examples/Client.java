import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

public class Client {
    public static void main(String[] arguments) {
        try {
            final String HOST = "indigo.cse.yorku.ca";
            final int PORT = 1024;

            Socket socket = new Socket(HOST, PORT);
            OutputStream socketOutput = socket.getOutputStream();
            InputStream socketInput = socket.getInputStream();

            socketOutput.write(5);
            int i = socketInput.read();
            while (i != -1) {
                System.out.print(i);
                i = socketInput.read();
            }
            System.out.println();

            socket.close();
        }
        catch (UnknownHostException e) {
                System.out.println("Host is unknown");
        }
        catch (IOException e) {
            System.out.println("I/O error occured when creating the socket");
        }
    }
}