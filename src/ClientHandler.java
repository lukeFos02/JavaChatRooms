import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUserName;
    private Client client;
    private JSONArray messages;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            this.client = new Client(this.socket, this.clientUserName); 
            this.messages = new JSONArray();
            clientHandlers.add(this);
            broadcastMessage("SERVER: " + clientUserName + " Has Connected");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        String clientMessage;
        while(socket.isConnected()){
            try {
                clientMessage = bufferedReader.readLine();
                Date date = new Date(System.currentTimeMillis());
                HashMap<String, Object> hashMessage = new HashMap<String, Object>();
                hashMessage.put("from", clientUserName);
                hashMessage.put("when", date);
                hashMessage.put("body", clientMessage);
                JSONObject messageJsonObject = new JSONObject(hashMessage);
                HashMap<String, Object> mgs = new HashMap<>();
                mgs.put("message", messageJsonObject);
                JSONObject fullMessage = new JSONObject(mgs);
                this.messages.add(fullMessage);
                try (FileWriter file = new FileWriter("messages.json", true)){
                    file.append(fullMessage.toJSONString());
                    file.append("\n");
                    file.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                broadcastMessage(clientMessage);
                client = client.getClientInfo();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }      
    }

    public void broadcastMessage(String message){
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.client = clientHandler.client.getClientInfo();
            try{
                if(!clientHandler.clientUserName.equals(clientUserName)){
                    if(clientHandler.client.currentRoom == this.client.currentRoom){
                        clientHandler.bufferedWriter.write(message);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                }
            }
            catch (Exception e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clientUserName + " Has Left");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        try {
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
