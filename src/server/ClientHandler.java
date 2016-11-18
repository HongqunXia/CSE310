package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

class ClientHandler extends Thread{
    private String userID;
    private Socket socket;
    private PrintWriter clientOutput;
    private final ArrayList<String> userIDs;
    private ArrayList<PrintWriter> outputStreams;
    private ArrayList<Group> groups;

    /**
     * Constructor to get the socket from server
     */
    ClientHandler(Socket socket, ArrayList<String> userIDs, ArrayList<PrintWriter> outputStreams, ArrayList<Group> groups){
        this.socket = socket;
        this.userIDs = userIDs;
        this.outputStreams = outputStreams;
        this.groups = groups;
    }

    /**
     * Services this current threads input stream to grab unique user ID from the client.
     * Then it grabs the client's output stream and adds it to the available output streams in the server.
     */
    public void run() {

        boolean allGroupsSubMenu;

        try {

            //Create the input stream and output stream for this client
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            clientOutput = new PrintWriter(socket.getOutputStream(), true);

            String in = clientInput.readLine();

            if(in.equals("login")){
                //Ask the client for an ID to use. If the ID is already in use, request for another
                //Put a lock around the userIDs list so there is no thread interference
                while (true) {
                    clientOutput.println("CREATEUSERID");
                    userID = clientInput.readLine();
                    if (userID == null) {
                        return;
                    }

                    //Lock the list of userIDs
                    synchronized (userIDs) {
                        if (!userIDs.contains(userID)) {
                            userIDs.add(userID);
                            break;
                        }
                    }
                }
            } else if(in.equals("help")){
                printHelpMenu();
            }

            //Successfully created Unique UserID so add them to the output streams
            clientOutput.println("USERIDACCEPTED");
            outputStreams.add(clientOutput);

            //Creates an initial list of groups
            groups = instantiateGroupList();

            while (true) {
                clientOutput.println("GETNEWINPUT");
                String input = clientInput.readLine();

                if (input == null) {
                    return;
                }

                //All Groups command and Enter into sub menu
                if(input.startsWith("ag")){
                    allGroupsSubMenu = true;
                    String number = input.substring(2);

                    if(number.equals("")){
                        printAllGroups();
                    } else {
                        printNGroups(Integer.parseInt(number));
                    }
                    while(allGroupsSubMenu){
                        clientOutput.println("ALLGROUPSUBMENU");
                        input = clientInput.readLine();

                        if(input == null){
                            return;
                        }
                        else if(input.startsWith("s")){
                            handleSubscriptions(input, true);
                        } else if(input.startsWith("u")) {
                            handleSubscriptions(input, false);
                        } else if(input.equals("n")){

                            //TODO Print the next 'N' Groups.

                        } else if(input.equals("q")){
                            allGroupsSubMenu = false;
                            clientOutput.flush();
                        }
                    }

                } else if(input.startsWith("sg")){
                    String number = input.substring(2);
                    //No N provided
                    if(number.equals("")){
                        printAllSubscribedGroups();
                    } else {
                        printNSubscribedGroups(Integer.parseInt(number));
                    }

                } else if(input.equals("rg")){

                    //TODO add Read Group Functionality

                } else if(input.equals("help")){
                    printHelpMenu();
                } else if(input.equals("logout")){
                    clientOutput.println("LOGOUT");
                    return;
                }
            }

        } catch (IOException e) {
            System.out.println(e);
        } finally {
            //Client is exiting the program.
            //Remove UserID from active User IDs and from output streams
            if (userIDs != null) {
                userIDs.remove(userID);
            }
            if (clientOutput != null) {
                outputStreams.remove(clientOutput);
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private ArrayList<Group> instantiateGroupList(){
        Group group1 = new Group("comp.programming", 18);
        Group group2 = new Group("comp.lang.python", 2);
        Group group3 = new Group("comp.lang.java", 3);
        Group group4 = new Group("comp.lang.C", 27);
        Group group5 = new Group("comp.os.threads", 0);
        Group group6 = new Group("comp.os.signals", 0);

        ArrayList<Group> groupList = new ArrayList<>();

        groupList.add(group1);
        groupList.add(group2);
        groupList.add(group3);
        groupList.add(group4);
        groupList.add(group5);
        groupList.add(group6);

        return groupList;
    }

    private void printHelpMenu(){
        clientOutput.println("HELP " + "Support commands are: " +
                "~'All Groups' : ag | ag N, where N is a number of groups." +
                "~'Help Menu' : HELP");
    }

    private void printAllGroups(){
        String groupString = "GROUPS ";
        int counter = 1;
        for(Group group : groups){
            if(group.isSubscribed()){
                groupString += "~" + counter++ + ". (s) " + group.getName();
            } else {
                groupString += "~" + counter++ + ". ( "+ ") " + group.getName();
            }
        }
        clientOutput.println(groupString);
    }

    private void printNGroups(int n){
        String groupString = "GROUPS ";
        int counter = 1;

        for(int i = 1; i < n; i++){
            if(groups.get(i-1).isSubscribed()){
                groupString += "~" + counter++ + "." + groups.get(i-1).getNumOfNewPosts() + groups.get(i-1).getName();
            } else {
                groupString += "~" + counter++ + ". ( "+ ") " + groups.get(i-1).getName();
            }
        }
        clientOutput.println(groupString);
    }

    private void printAllSubscribedGroups(){
        String groupString = "GROUPS ";
        int counter = 1;

        for(Group group : groups){
            if(group.isSubscribed()){
                groupString += "~" + counter++ + "." + group.getNumOfNewPosts() + " " + group.getName();
            }
        }
        clientOutput.println(groupString);
    }

    private void printNSubscribedGroups(int n){
        String groupString = "GROUPS ";
        int counter = 1;

        for(int i = 1; i < n; i++){
            if(groups.get(i-1).isSubscribed()){
                groupString += "~" + counter++ + ". " + groups.get(i-1).getNumOfNewPosts() + " " + groups.get(i-1).getName();
            }
        }
        clientOutput.println(groupString);
    }

    private void handleSubscriptions(String input, boolean subscribing){
        String inputArray[] = input.split(" ");

        if(subscribing){
            for(int i = 1; i < inputArray.length; i++){
                groups.get(Integer.parseInt(inputArray[i])-1).subscribe();
            }
        } else {
            for(int i = 1; i < inputArray.length; i++){
                groups.get(Integer.parseInt(inputArray[i])-1).unsubscribe();
            }
        }
    }
}
