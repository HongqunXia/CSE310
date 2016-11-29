package client;


import server.Group;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class GroupClient {

    private String clientID;
    private PrintWriter output;
    private Scanner inputScanner;
    private ArrayList<Group> subscribedGroups;

    /**
     * Public Constructor for the Server
     * @param clientID
     */
    public GroupClient(String clientID, ArrayList<Group> subscribedGroups){
        this.clientID = clientID;
        this.subscribedGroups = subscribedGroups;
    }

    /**
     * Private Constructor to instantiate the Scanner
     */
    private GroupClient(){
        inputScanner = new Scanner(System.in);
    }

    /**
     * Gets the Server's IP that they want to connect to from the Client
     * @return serverIP
     */
    private String getServerAddress(){
        System.out.print("Enter in Server's IP Address: ");
        return inputScanner.nextLine();
    }

    /**
     * Get the User's ID from the client
     * @return userID
     */
    private String getUserID(){
        System.out.print("Login with your Client ID: ");
        return inputScanner.nextLine();
    }

    /**
     * Writes what the User types to Output Stream.
     * Prevents the client from typing garbage to the server.
     */
    private void retrieveClientMessage(){
        System.out.print("Input: ");
        String clientMessage = inputScanner.nextLine();
        if(!(clientMessage.equals("login") || clientMessage.equals("help"))){
            System.out.println("Please only enter in 'login' or 'help'.");
            retrieveClientMessage();
        } else {
            output.println(clientMessage);
        }
    }

    /**
     * Prints the messages to the console from all other Users
     */
    private void printMessage(String message){
        System.out.println(message);
    }

    /**
     * Run class that does all the Server interactions
     * @throws IOException
     */
    private void run() throws IOException {
        Socket socket = new Socket(getServerAddress(), 8026);
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Welcome to Interest Groups. \n" +
                "You can use the commands login and help.");
        retrieveClientMessage();

        /**
         * While Loop to continuously communicate with the server
         */
        while(true){
            String currentServerMessage = input.readLine();
            if(currentServerMessage.startsWith("CLIENTLOGIN")){
                output.println(getUserID());
            } else if(currentServerMessage.startsWith("USERIDACCEPTED")){
                System.out.println("User ID was accepted. Welcome to Interest Groups." + "\n"
                        + "You may begin typing..");
            } else if(currentServerMessage.startsWith("SUCCESSFULLLOGIN")){
                System.out.println("You successfully logged back in. Welcome back to Interest Groups." + "\n"
                        + "You may begin typing..");
            } else if(currentServerMessage.startsWith("GROUPS")){
                String groupArray[] = currentServerMessage.split("~");
                for(int i = 1; i < groupArray.length; i++){
                    printMessage(groupArray[i]);
                }
            } else if(currentServerMessage.equals("GETNEWINPUT")){
                retrieveClientMessage();
            } else if(currentServerMessage.equals("ALLGROUPSUBMENU")){
                System.out.println("\nGroup sub menu. \n" +
                        "The allowed commands are s, u, n, and q exits.");
                retrieveClientMessage();
            } else if(currentServerMessage.equals("LOGOUT")){
                socket.close();
                System.out.print("You have successfully logged out.");
                return;
            } else if(currentServerMessage.startsWith("HELP")){
                String helpArray[] = currentServerMessage.split("~");
                for(int i = 0; i < helpArray.length; i++){
                    if(i == 0){
                        printMessage(helpArray[i].substring(5));
                    } else{
                        printMessage(helpArray[i]);
                    }
                }
            }
        }
    }

    /**
     * Main Method that instantiates the GroupClient and calls the run method
     */
    public static void main(String[] args){
        GroupClient client = new GroupClient();
        try{
            client.run();
        } catch (IOException e){
            System.out.println(e);
        }
    }
}
