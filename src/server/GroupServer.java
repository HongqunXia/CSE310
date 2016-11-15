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
    public static ArrayList<String> groups = new ArrayList<>();

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
                new ClientHandler(serverSocket.accept()).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    /**
     * Private inner class that handles each client in a separate thread
     */
    private static class ClientHandler extends Thread{

        private String userID;
        private Socket socket;
        private BufferedReader clientInput;
        private PrintWriter clientOutput;

        /**
         * Private constructor to get the socket from server
         * @param socket
         */
        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        /**
         * Services this current threads input stream to grab unique user ID from the client.
         * Then it grabs the client's output stream and adds it to the available output streams in the server.
         */
        public void run() {

            boolean uniqueID = false;

            try {

                //Create the input stream and output stream for this client
                clientInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                clientOutput = new PrintWriter(socket.getOutputStream(), true);

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

                //Successfully created Unique UserID so add them to the output streams
                clientOutput.println("USERIDACCEPTED");
                outputStreams.add(clientOutput);

                //Now begin grabbing messages from this client and printing the messages to all other users
                while (true) {
                    String input = clientInput.readLine();

                    if (input == null) {
                        return;
                    }

                    for(PrintWriter outputStream : outputStreams) {
                        outputStream.println("MESSAGE " + userID + ": " + input);
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
    }
}
