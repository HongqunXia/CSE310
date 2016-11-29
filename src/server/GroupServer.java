package server;

import client.GroupClient;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Small Server class that just handles creating the server socket
 * and spawning new ClientHandler Threads.
 */
public class GroupServer {

    //TODO Create methods to read ClientMap and Groups from File

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 8026;

    /**
     * Map of Saved Clients
     */
    private static Map<String, GroupClient> clientMap = Collections.synchronizedMap(new HashMap<>());

    /**
     * List of Current Groups
     */
    private static ArrayList<Group> groups = new ArrayList<>();

    /**
     * Main method that spins up a thread that handles each new client
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Group Server is running...");

        GroupClient tempClient = new GroupClient("Jimmy", groups);

        clientMap.put("Jimmy", tempClient);

        try{
            while(true){
                new ClientHandler(serverSocket.accept(), clientMap, groups).start();
            }
        } finally {
            serverSocket.close();
        }
    }
}
