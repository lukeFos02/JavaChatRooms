import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    String username;
    Room currentRoom;
    ArrayList<Room> rooms;
    boolean pastLogin;

    public Client(Socket socket, String username){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.currentRoom = null;
            this.rooms = new ArrayList<Room>();
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public Client(String username, Room currentRoom, ArrayList<Room> rooms, Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){

    }

    public Client getClientInfo(){
        Client client = new Client(this.username, this.currentRoom, this.rooms, this.socket, this.bufferedReader, this.bufferedWriter);
        return client;
    }

    public void sendMessage(Client client, Scanner scanner){
        try {
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            while(socket.isConnected()){
                String message = scanner.nextLine();
                if(message.equalsIgnoreCase("menu") || message.equalsIgnoreCase("quit")){
                    showMenu(client, scanner);
                    client.listenForMessage();
                    client.sendMessage(client, scanner);
                    break;                    
                }
                bufferedWriter.write(currentRoom.name + " => " + username + ": " + message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String message;
                while(socket.isConnected()){
                    try {
                        if(pastLogin == true){
                            message = bufferedReader.readLine();
                            String[] tokens = message.split(" ");
                            if(tokens[0].equalsIgnoreCase(currentRoom.name) || tokens[0].equalsIgnoreCase("SERVER:")){
                                System.out.println(message);
                            }
                        }
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
            
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
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

    public void addRoom(Room room){
        rooms.add(room);
    }

    public void removeRoom(Room room){
        rooms.remove(room);
    }

    public static void clearConsole(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void showMenu(Client client, Scanner scanner){
        clearConsole();
        System.out.println("############################");
        System.out.println("# Welcome To The Chat Room #");
        System.out.println("#                          #");
        System.out.println("# Press 1 for public chat  #");
        System.out.println("# Press 2 to look at rooms #");
        System.out.println("# Press 3 to join a room   #");
        System.out.println("# Press 4 to quit          #");
        System.out.println("#                          #");
        System.out.println("############################");
        System.out.println();
        while (true){
            String command = scanner.nextLine();
            if (command.equals("1")){
                clearConsole();
                System.out.println("Current Chat Room: " + client.currentRoom.name);
                System.out.println();
                pastLogin = true;
                break;
            }
            else if (command.equals("2")){
                showRooms(client, scanner);
                break;
            }
            else if (command.equals("3")){
                listRooms(client, scanner);
                break;
            }
            else if (command.equals("4")){
                System.exit(0);
            }
        }
    }

    public void showRooms(Client client, Scanner scanner){
        clearConsole();
        System.out.println("############################");
        System.out.println("# List Of Rooms            #");
        System.out.println("#                          #");
        System.out.println("# 1.Sports                 #");
        System.out.println("# 2.Movies And TV          #");
        System.out.println("# 3.Gaming                 #");
        System.out.println("#                          #");
        System.out.println("# To Join A Group Enter    #");
        System.out.println("# Number Next To Your      #");
        System.out.println("# Chosen Group             #");
        System.out.println("#                          #");
        System.out.println("############################");
        System.out.println();
        while(true){
            String command = scanner.nextLine();
            if (command.equals("1")){
                Room room = new Room("Sports");
                client.addRoom(room);
                break;
            }
            else if (command.equals("2")){
                Room room = new Room("Movies And TV");
                client.addRoom(room);
                break;
            }
            else if (command.equals("3")){
                Room room = new Room("Gaming");
                client.addRoom(room);
                break;
            }
            else if (command.equalsIgnoreCase("menu")){
                break;
            }
        }
        showMenu(client, scanner);
    }

    public void listRooms(Client client, Scanner scanner){
        clearConsole();
        System.out.println("All Your Joined Rooms");
        System.out.println();
        Integer count = 1;
        for (Room room : rooms) {
            System.out.println(count.toString() + ". " + room.name);
            count += 1;
        }
        System.out.println();
        System.out.println("Enter The Name of The Group You Wish To Join");
        boolean roomSelected = false;
        while(true){
            String command = scanner.nextLine();
            String[] tokens = command.split(" ");
            for (Room room : rooms) {
                if (room.name.equalsIgnoreCase(command)){
                    client.currentRoom = room;
                    roomSelected = true;
                    pastLogin = true;
                    clearConsole();
                    System.out.println("Current Chat Room: " + client.currentRoom.name);
                    client.listenForMessage();
                    client.sendMessage(client, scanner);
                    break;
                }
                else if (command.equalsIgnoreCase("menu")){
                    showMenu(client, scanner);
                    roomSelected = true;
                    break;
                }
                else if (tokens[0].equalsIgnoreCase("leave")){
                    if (tokens[1].equalsIgnoreCase(room.name)){
                        client.removeRoom(room);
                        clearConsole();
                        showMenu(client, scanner);
                    }
                }
            }
            if (roomSelected == true){
                break;
            }
            System.out.println("Unrecognised Room - Try Again");
        }
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter Your Username: ");
            String username = scanner.nextLine();
            clearConsole();
            Room room = new Room("Public");
            Socket socket = new Socket("localhost", 1234);
            Client client = new Client(socket, username);
            client.currentRoom = room;
            client.rooms.add(room);
            client.showMenu(client, scanner);
            clearConsole();
            System.out.println("Current Chat Room: " + client.currentRoom.name);
            System.out.println();
            client.listenForMessage();
            client.sendMessage(client, scanner);
        }
    }
}
