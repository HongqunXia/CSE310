package server;

import client.GroupClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The main thread class that handles all of the servers actions.
 * Does all communication with the client
 */
class ClientHandler extends Thread {
    private Socket socket;
    private PrintWriter clientOutput;
    //Use Synchronized Map for to handle concurrency issues.
    private final Map<String, GroupClient> clientsMap = Collections.synchronizedMap(new HashMap<>());
    private ArrayList<Group> groups = new ArrayList<>();

    /**
     * Constructor to get the socket from server
     */
    ClientHandler(Socket socket, Map<String, GroupClient> clientMap, ArrayList<Group> groups) {
        this.socket = socket;
        this.groups = groups;

        //TODO Populate ClientsMap with entries from ClientMap
        for (Map.Entry<String, GroupClient> entry : clientMap.entrySet()) {
            clientsMap.put(entry.getKey(), entry.getValue());
        }
        //TODO Populate Groups with entries from Groups list
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

            //Ask the client for an ID to use. If the ID is already in use, request for another
            //Client Map is a synchronized data structure so there is no thread interference
            if (in.equals("login")) {
                while (true) {
                    clientOutput.println("CLIENTLOGIN");
                    String clientID = clientInput.readLine();
                    if (clientID == null) {
                        return;
                    }

                    //Doesn't exist so create a new entry in the map
                    if (clientsMap.get(clientID) == null) {
                        ArrayList<Group> subGroups = new ArrayList<>();
                        GroupClient client = new GroupClient(clientID, subGroups);
                        clientsMap.put(clientID, client);
                        //Successfully created a new Group Client
                        clientOutput.println("USERIDACCEPTED");
                        break;
                    } else {
                        clientOutput.println("SUCCESSFULLLOGIN");
                        break;
                    }
                }
            } else if (in.equals("help")) {
                printHelpMenu();
            }

            //TODO Remove this and replace with reading from file
            //Creates an initial list of groups
            groups = instantiateGroupList();

            while (true) {
                clientOutput.println("GETNEWINPUT");
                String input = clientInput.readLine();

                if (input == null) {
                    return;
                }

                //All Groups command and Enter into sub menu
                if (input.startsWith("ag")) {
                    allGroupsSubMenu = true;
                    String number = input.substring(2);

                    if (number.equals("")) {
                        printAllGroups();
                    } else {
                        printNGroups(Integer.parseInt(number));
                    }
                    while (allGroupsSubMenu) {
                        clientOutput.println("ALLGROUPSUBMENU");
                        input = clientInput.readLine();

                        if (input == null) {
                            return;
                        } else if (input.startsWith("s")) {
                            handleSubscriptions(input, true);
                        } else if (input.startsWith("u")) {
                            handleSubscriptions(input, false);
                        } else if (input.equals("n")) {

                            //TODO Print the next 'N' Groups.

                        } else if (input.equals("q")) {
                            allGroupsSubMenu = false;
                            clientOutput.flush();
                        }
                    }

                } else if (input.startsWith("sg")) {
                    String number = input.substring(2);
                    //No N provided
                    if (number.equals("")) {
                        printAllSubscribedGroups();
                    } else {
                        printNSubscribedGroups(Integer.parseInt(number));
                    }

                } else if (input.equals("rg")) {

                    //TODO Add Read Group Functionality
                    //Remember posting to a group must be thread safe. 'Synchronized'

                } else if (input.equals("help")) {
                    printHelpMenu();
                } else if (input.equals("logout")) {
                    clientOutput.println("LOGOUT");
                    return;
                }
            }

        } catch (IOException e) {
            System.out.println(e);
        } finally {
            //Client is exiting the program.
            //TODO Add functionality to save Client to file
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    //TODO Get rid of this. Will be Replaced with reading from file.
    private ArrayList<Group> instantiateGroupList() {
        ArrayList<Post> groupPosts = null;
        Group group1 = new Group("comp.programming", 18, groupPosts);
        Group group2 = new Group("comp.lang.python", 2, groupPosts);
        Group group3 = new Group("comp.lang.java", 3, groupPosts);
        Group group4 = new Group("comp.lang.C", 27, groupPosts);
        Group group5 = new Group("comp.os.threads", 0, groupPosts);
        Group group6 = new Group("comp.os.signals", 0, groupPosts);

        ArrayList<Group> groupList = new ArrayList<>();

        groupList.add(group1);
        groupList.add(group2);
        groupList.add(group3);
        groupList.add(group4);
        groupList.add(group5);
        groupList.add(group6);

        return groupList;
    }

    /**
     * Prints out the help menu to the Client
     */
    //TODO Update Help Menu
    private void printHelpMenu() {
        clientOutput.println("HELP " + "Support commands are: " +
                "~'All Groups' : ag | ag N, where N is a number of groups." +
                "~'Help Menu' : HELP");
    }

    /**
     * Prints all the current Groups that the server has
     */
    private void printAllGroups() {
        String groupString = "GROUPS ";
        int counter = 1;
        for (Group group : groups) {
            if (group.isSubscribed()) {
                groupString += "~" + counter++ + ". (s) " + group.getName();
            } else {
                groupString += "~" + counter++ + ". ( " + ") " + group.getName();
            }
        }
        clientOutput.println(groupString);
    }

    /**
     * Prints a specified number of Groups
     *
     * @param n
     */
    private void printNGroups(int n) {
        String groupString = "GROUPS ";
        int counter = 1;

        for (int i = 1; i < n; i++) {
            if (groups.get(i - 1).isSubscribed()) {
                groupString += "~" + counter++ + "." + groups.get(i - 1).getNumOfNewPosts() + groups.get(i - 1).getName();
            } else {
                groupString += "~" + counter++ + ". ( " + ") " + groups.get(i - 1).getName();
            }
        }
        clientOutput.println(groupString);
    }

    /**
     * Prints all the Groups that the client is currently subscribed to.
     */
    private void printAllSubscribedGroups() {
        String groupString = "GROUPS ";
        int counter = 1;

        for (Group group : groups) {
            if (group.isSubscribed()) {
                groupString += "~" + counter++ + "." + group.getNumOfNewPosts() + " " + group.getName();
            }
        }
        clientOutput.println(groupString);
    }

    /**
     * Prints a specified number of Groups that the current client is subscribed to
     *
     * @param n
     */
    private void printNSubscribedGroups(int n) {
        String groupString = "GROUPS ";
        int counter = 1;

        for (int i = 1; i < n; i++) {
            if (groups.get(i - 1).isSubscribed()) {
                groupString += "~" + counter++ + ". " + groups.get(i - 1).getNumOfNewPosts() + " " + groups.get(i - 1).getName();
            }
        }
        clientOutput.println(groupString);
    }

    /**
     * A method that handles the client subscriptions/unscriptions
     *
     * @param input
     * @param subscribing
     */
    private void handleSubscriptions(String input, boolean subscribing) {
        String inputArray[] = input.split(" ");

        if (subscribing) {
            for (int i = 1; i < inputArray.length; i++) {
                groups.get(Integer.parseInt(inputArray[i]) - 1).subscribe();
            }
        } else {
            for (int i = 1; i < inputArray.length; i++) {
                groups.get(Integer.parseInt(inputArray[i]) - 1).unsubscribe();
            }
        }
    }
}
