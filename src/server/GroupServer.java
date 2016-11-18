package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GroupServer {

    /**
     * The port that the server listens on.
     */
    private static final int PORT = 8026;

    /**
     * List of Unique User IDs
     */
    private static ArrayList<String> userIDs = new ArrayList<>();

    /**
     * List of available Client output streams
     */
    private static ArrayList<PrintWriter> outputStreams = new ArrayList<>();

    /**
     * List of Current Groups
     */
    private static ArrayList<Group> groups;

    /**
     * Main method that spins up a thread that handles each new client
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Group Server is running...");
        try{
            while(true){
                new ClientHandler(serverSocket.accept(), userIDs, outputStreams, groups).start();
            }
        } finally {
            serverSocket.close();
        }
    }
}
