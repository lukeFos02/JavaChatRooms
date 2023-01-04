import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket serverSocket){
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try {
            while(!serverSocket.isClosed()){
                Socket socket = serverSocket.accept();
                System.out.println("New Client Accepted...");
                ClientHandler ClientHandler = new ClientHandler(socket);
                Thread thread = new Thread(ClientHandler);
                thread.start();
            }
        } catch (Exception e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket(){
        try{
            if(serverSocket != null){
                serverSocket.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
