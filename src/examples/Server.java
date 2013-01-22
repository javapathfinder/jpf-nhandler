import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class Server {
    public static void main(String[] arguments) {
        try {
            final int PORT = 1024;

            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for a client ...");
            Socket socket = serverSocket.accept();
            System.out.println("Client has arrived ...");
            OutputStream socketOutput = socket.getOutputStream();
            InputStream socketInput = socket.getInputStream();

            int number = socketInput.read();
            System.out.println("Client's number has been received ...");
            socketOutput.write(number);
            System.out.println("Client's result has been sent ...");

            socket.close();
        }
        catch (IOException e) {
            System.out.println("I/O error occured when opening the socket");
        }
    }
}