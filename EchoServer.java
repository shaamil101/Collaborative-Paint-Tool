import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * EchoServer is a simple server which accepts a connection and 
 * simply reads input and echos it back to the sender.
 * 
 * Code provided to enable testing of (1) sending/receiving messages from 
 * the server, and (2) updating a sketch based on messages.
 *
 * @author Shaamil and Nicolas
 */
public class EchoServer {

    private ServerSocket listen;  // for accepting connections
    
    public EchoServer(ServerSocket listen) {
        this.listen = listen;
    }

    ///////////////////////////////////////////////////////////////////////
    
    private class EchoServerCommunicator extends Thread {
        private Socket sock;
        private BufferedReader in;  // from client
        private PrintWriter out;    // to client

        public EchoServerCommunicator(Socket sock) {
            this.sock = sock;
        }

        public void run() {
            try {
                System.out.println("editor connected for testing...");
                
                // Communication channel
                in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                out = new PrintWriter(sock.getOutputStream(), true);

                // Echo loop
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("received: " + line);
                    send(line);
                }

                // Clean up
                out.close();
                in.close();
                sock.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void send(String msg) {
            System.out.println("send: " + msg);
            out.println(msg);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    
    public void getConnections() throws IOException {
        while (true) {
            EchoServerCommunicator comm = new EchoServerCommunicator(listen.accept());
            comm.setDaemon(true);
            comm.start();
        }
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("Starting up the EchoServer...");
        new EchoServer(new ServerSocket(4242)).getConnections();        
    }
        
}
