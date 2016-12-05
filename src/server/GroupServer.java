package server;


import java.net.ServerSocket;
/**
 * Small Server class that just handles creating the server socket
 * and spawning new ClientHandler Threads.
 */
public class GroupServer {
    /**
     * The port that the server listens on.
     */
    private static final int PORT = 8026;


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
}
