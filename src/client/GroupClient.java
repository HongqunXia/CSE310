package client;


import protocols.AGProtocol;
import protocols.LoginProtocol;
import protocols.LogoutProtocol;
import protocols.Protocol;

import java.net.Socket;

import java.util.HashMap;
import java.util.Scanner;
import  java.io.*;

public class GroupClient {

    private Scanner inputScanner;
    private ObjectInputStream fromServer = null;
    private ObjectOutputStream toServer = null;
    private Protocol protocol = null;
    private  int N = 5;
    private HashMap<Integer,String> infoOnConsole  = null;

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
     * Writes what the User types to Output Stream.
     * Prevents the client from typing garbage to the server.
     */
    private String retrieveClientMessage(){
        System.out.print("Input: ");
        String clientMessage = inputScanner.nextLine();
        return  clientMessage;

    }

    /**
     * Prints the messages to the console from all other Users
     */
    private void printMessage(String message){
        System.out.println(message);
    }

    /*
    * Connect to the server
    * */

    private  boolean connectToServer(String inputString){

        boolean status = false;

        String str[] = inputString.split(" ");
        if(str.length < 2) {
            printMessage("ERROR: Invalid Arguments");
            return false;
        }
        try {

            LoginProtocol login = new LoginProtocol("LOGIN");
            login.setClientID(str[1]);

            toServer.writeObject(login);

            protocol = (Protocol) fromServer.readObject();

            if(protocol.getMethod().equals("LOGIN")){
                login = (LoginProtocol)protocol;
                if((status = login.isAuthorized()))
                    System.out.println("Welcome to Interest Groups. \n");
            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return status;
    }

/*
* Request help menu and print it out
* */
    private void requestHelpMenu() {
        try {
            Protocol protocol = new Protocol("HELP");
            toServer.writeObject(protocol);

            protocol = (Protocol)fromServer.readObject();

            if(protocol.getMethod().equals("HELP"))
                printMessage(protocol.getHelpMenu());

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /*
    * Request next n groups from The Server
    * @ n           the next n groups
    * @return       the number of groups has been recevied
    * */
    private  int request_N_Groups(int n){
        int nGroup = 0;
        try {
            AGProtocol ag = new AGProtocol("N");
            ag.setNumber_of_request_groups(n);
            toServer.writeObject(ag);

            Protocol protocol = (Protocol)fromServer.readObject();
            if(!protocol.getStatus())
                printMessage("ERROR: failed to get " + n +" group(s)");

            if(protocol.getMethod().equals("AG")){
                ag = (AGProtocol)protocol;
                nGroup = ag.getNumber_of_success();
                if(nGroup !=0) {
                    printMessage(ag.nGroupsString());
                    infoOnConsole = ag.getinfoOnConsole();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return  nGroup;
    }

    private  void handlesubscription(String input, boolean subscribe){
        String str[] = input.split(" ");
        AGProtocol agProtocol = null;
        if(str.length < 2) return;

        String  command = subscribe == true ? "S" : "U";
        int number = 0;
        String groupID = null;
        agProtocol = new AGProtocol(command);
        StringBuilder groupsStr = new StringBuilder();

        /*Prepare information to be send*/
        for (int i = 1; i < str.length; i++) {

            number = Integer.parseInt(str[i]);
            groupID = this.infoOnConsole.get(number);

            if(subscribe)
                agProtocol.addSubscribedGroup(groupID);
            else
                agProtocol.addUnsubscribedGroup(groupID);

            groupsStr.append(groupID).append(",");
        }

        try {
            toServer.writeObject(agProtocol);

            Protocol protocol = (Protocol)fromServer.readObject();


            if(protocol.getStatus()){
                if(command.equals("S"))
                    printMessage("Successfully subscribed " + groupsStr.toString() + " group(s)");

                else
                    printMessage("Successfuly unsubscribed " + groupsStr.toString() + "group(s)");

            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private  void quitSubMode(){
        try {
            AGProtocol send = new AGProtocol("Q");
            toServer.writeObject(send);

            Protocol rcv = (Protocol)fromServer.readObject();
            if(rcv.getMethod().equals("AG"))
                if(rcv.getStatus())
                    printMessage("Successfully quit AG Mode.");

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void switchToAG(String inputString) {
        boolean quit = false;
        String str[] = inputString.split(" ");
        int number_of_groups_per_request = N;
        if(str.length == 0) return;
        try {
            /*Request n groups from server and display them on console*/
            if(str.length == 1)
                request_N_Groups(N);/*Request default N groups*/
            else if(str.length >= 2){
                int n = Integer.parseInt(str[1]);
                number_of_groups_per_request = n;
                request_N_Groups(n);
            }

            String subCommand = null;
            int nGroup = 0;// The number of groups that is send by server
            while (!quit){
                subCommand = retrieveClientMessage();
                if(subCommand.startsWith("s")) {
                    handlesubscription(subCommand, true);
                } else if(subCommand.startsWith("u")){
                    handlesubscription(subCommand, false);
                } else if(subCommand.startsWith("n")){
                    nGroup = request_N_Groups(number_of_groups_per_request);
                    if(nGroup == 0){
                        quitSubMode();
                        quit = true;
                    }
                } else if( subCommand.startsWith("q")) {
                    quitSubMode();
                    quit = true;
                }else{
                    printMessage("ERROR: an invalid subcommand in AG Mode.");
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void switchToSG(String inputString) {
    }

    private  void  switchToRG(String inputString){


    }
    private  void logout(){
        try {
            LogoutProtocol logout = new LogoutProtocol();
            toServer.writeObject(logout);

            Protocol protocol = (Protocol)fromServer.readObject();
            if(protocol.getStatus())
                printMessage("Successfully Logout.");

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * Run class that does all the Server interactions
     * @throws IOException
     */
    private void run() throws IOException {
        Socket clientSocket = new Socket(getServerAddress(), 8026);

        toServer = new ObjectOutputStream(clientSocket.getOutputStream());
        fromServer = new ObjectInputStream(clientSocket.getInputStream());

        boolean loginStatus = false;
        String inputString = retrieveClientMessage();
        String str[] = null;
        do{
             str  = inputString.split(" ");

            if(str[0].equals("help"))
                requestHelpMenu();

            else if(connectToServer(inputString))
                loginStatus = true;
            if(!loginStatus)
                inputString = retrieveClientMessage();

        }while(!loginStatus && !inputString.equals("exit"));

        if(inputString.equals("exit")) return;

        /**
         * While Loop to continuously communicate with the server
         */
        while(loginStatus){

            inputString = retrieveClientMessage();
            str = inputString.split(" ");
            if(str[0].equals("ag"))
                switchToAG(inputString);
            else if(str[0].equals("sg"))
                switchToSG(inputString);
            else if(str[0].equals("rg"))
                switchToRG(inputString);
            else if(str[0].equals("logout")){
                logout();
                loginStatus = false;
                break;
            }
            else
                printMessage("ERROR:  Invalid Command");

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
