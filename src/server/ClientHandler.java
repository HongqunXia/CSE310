package server;

import protocols.AGProtocol;
import protocols.LoginProtocol;
import protocols.LogoutProtocol;
import protocols.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.io.FileReader;

/**
 * The main thread class that handles all of the servers actions.
 * Does all communication with the client
 */
class ClientHandler extends Thread implements  ServerInfoDir{
    private Socket client = null;
    private ObjectInputStream fromClient = null;
    private ObjectOutputStream toClient = null;
    private Protocol receviedProtocol = null;

    private ClientInfo clientInfo = null;
    private static int startIndex = 0; /*It wiill track where next N groupID shall start at*/
    private static int N = 5;
    private boolean login = false;
    private boolean logout = false;

    /**
     * Constructor to get the socket from server
     */
    ClientHandler(Socket socket) {
        this.client = socket;
    }

    /**
     * Services this current threads input stream to grab unique user ID from the client.
     * Then it grabs the client's output stream and adds it to the available output streams in the server.
     */
    public void run() {
        try {
            //Create the input object stream and output object stream for this client
            toClient = new ObjectOutputStream(client.getOutputStream());
            fromClient = new ObjectInputStream(client.getInputStream());
            login = false;

            while (!login) {/*Wait for a client to login*/
                try {
                    receviedProtocol = (Protocol) fromClient.readObject();
                    if (receviedProtocol.getMethod().equals("HELP"))
                        sendHelpMenu();
                    else if (receviedProtocol.getMethod().equals("LOGIN")) {
                        authorize(receviedProtocol);
                        login = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            logout = false;
            while (!logout) {/*Keep conncetion between client and server until the client logout*/
                try{
                    receviedProtocol = (Protocol)fromClient.readObject(); //Wait for client input
                    startIndex = 0;//This will be used to track the starting point of next n groups to be send
                    if(receviedProtocol.getMethod().equals("AG"))
                        allGroupHandler(receviedProtocol);
                    else if(receviedProtocol.getMethod().equals("LOGOUT")){
                         logout();
                         logout = true;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        } catch (IOException e) {
            System.out.println(e);

        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private void logout() {
        /*Save client info*/
        String where = ServerInfoDir.CLIENTS_INFO_DIR + "/" + clientInfo.getClientID() +".txt";
        clientInfo.saveClientInfo(where);

        LogoutProtocol quit = new LogoutProtocol();
        quit.setStatus(true);
        try {
            toClient.writeObject(quit);/*Confirm client's logout request*/
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendHelpMenu() {
        Protocol helpMenu = new Protocol("HELP");
        try {
            helpMenu.setHelpMenu(getHelpMenu());
            toClient.writeObject(helpMenu);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void authorize(Protocol protocol) {
        LoginProtocol login = (LoginProtocol)protocol;

        if(!checkClientInfo(login.getClientID()))
            createClientInfo(login.getClientID());

        clientInfo = getClientInfo(login.getClientID());/*Init client info*/
        try {
            login.authorize();
            toClient.writeObject(login);
        }catch (Exception e){
            e.printStackTrace();
        }


    }


    private boolean checkClientInfo(String clientID){

        String clientFile_name = clientID +".txt";
        return new File(ServerInfoDir.CLIENTS_INFO_DIR, clientFile_name ).exists();

    }
    private void createClientInfo(String clientID){

        String clientFile_name = clientID +".txt";

        File clientFile = new File(ServerInfoDir.CLIENTS_INFO_DIR + "/"+ clientFile_name);
        try {
            clientFile.createNewFile();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    * Collect information from a cilent file
    * */
    private ClientInfo getClientInfo(String clientID){

        ClientInfo clientInfo = new ClientInfo((clientID));

        String clientFile_name = clientID +".txt";

        File clientFile = new File(ServerInfoDir.CLIENTS_INFO_DIR, clientFile_name);

        try{
            //Create object of FileReader
            FileReader inputFile = new FileReader(clientFile);

            //Instantiate the BufferedReader Class
            BufferedReader bufferReader = new BufferedReader(inputFile);

            //Variable to hold the one line data
            String line = null, groupID = null, postname = null;
            String parts[], posts[];

            while ((line = bufferReader.readLine()) != null) {
                parts = line.split(":");

                clientInfo.subscribe(parts[0]);

                if(parts.length > 1) {
                    posts = parts[1].split(","); /*Get what the client has read in a group*/

                    for (String str : posts)
                        clientInfo.readPost(parts[0], str);
                }

            }

            bufferReader.close();
        }catch(Exception e){
            System.out.println("Error while reading file:" + e.getMessage());
        }


        return clientInfo;
    }


    private void allGroupHandler(Protocol protocol){

        AGProtocol receviedAG = (AGProtocol)protocol;
        if(receviedAG.getSubCommand().equals("N"))
            sendnextGroups(receviedAG.getNumber_of_request_groups());


        while(true){/*Enter subCommand mode*/
            try {
                receviedProtocol = (Protocol) fromClient.readObject();
                receviedAG = (AGProtocol)receviedProtocol;

                if(receviedAG.getSubCommand().equals("N")) {
                    sendnextGroups(receviedAG.getNumber_of_request_groups());

                } else if (receviedAG.getSubCommand().equals("S")) {
                    updateSubscribedGroup(receviedProtocol, true);
                } else if(receviedAG.getSubCommand().equals("U")) {
                    updateSubscribedGroup(receviedProtocol, false);
                } else if(receviedAG.getSubCommand().equals("Q")) {
                    AGProtocol agProtocol = new AGProtocol("Q");
                    agProtocol.setStatus(true);
                    toClient.writeObject(agProtocol);
                    break;
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    private boolean sendnextGroups(int number_of_request_groups) {
        AGProtocol sendAG = new AGProtocol("N");


        int oldIndex = startIndex;
        boolean success = true;/* When there is more than on group left to be send*/

        String [] groups = getAllGroupNames();/*Get current groups name*/

        int  totalGroups = groups.length;

        if(startIndex == totalGroups) {/*All groups have been send*/
            sendAG.setNumber_of_success(0);
            success = false;
        }
        else {
            for (int i = 0; i < number_of_request_groups; i++) {
                if (startIndex == totalGroups)
                    break;
                if (clientInfo.isSubscribed(groups[startIndex]))
                    sendAG.addGroup(groups[startIndex++], true);
                else
                    sendAG.addGroup(groups[startIndex++], false);
            }
            sendAG.setNumber_of_success(startIndex - oldIndex);
        }

        try {

            sendAG.setStatus(true);
            toClient.writeObject(sendAG);
        }catch (Exception e){
            e.printStackTrace();
        }

        return success;
    }

    public void updateSubscribedGroup(Protocol receviedProtocol, boolean subscribe){
        AGProtocol recevied =(AGProtocol)receviedProtocol;
        if(subscribe) {
            ArrayList<String> subscribedGroups = recevied.getSubscribedGroups();
            for (String group : subscribedGroups)
                clientInfo.subscribe(group);
        }
        else{
            ArrayList<String> unSubscribedGroups = recevied.getUnSubscribedGroups();
            for (String group : unSubscribedGroups)
                clientInfo.unsubscribe(group);
        }
        try {
            String command = subscribe == true ? "S":"U";
            AGProtocol send = new AGProtocol(command);
            send.setStatus(true);
            toClient.writeObject(send);
        }catch (Exception e){
            e.printStackTrace();
        }

    }





    private void readGroupHandler(String input) {
    }


    private boolean lisitngGroups(String input) {

        String inputArray[] = input.split(" ");
        if(inputArray.length >= 2){
            int n = Integer.parseInt(inputArray[1]);
             //return printNGroups(n);
        }

        return  false;
    }


    private String getHelpMenu() {
        StringBuilder helpMenu = new StringBuilder();
        BufferedReader bufferedReader = null;
        FileReader fileReader = null;

        try {
            fileReader = new FileReader(ServerInfoDir.HELP_MENU);
            bufferedReader = new BufferedReader(fileReader);
            String strLine = null;
            while ((strLine = bufferedReader.readLine()) != null)
                helpMenu.append(strLine).append("\n");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
       return  helpMenu.toString();
    }

    private String[] getAllGroupNames(){


        File file = new File(ServerInfoDir.GROUPS_INFO_DIR);

        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        return  directories;

    }





}
