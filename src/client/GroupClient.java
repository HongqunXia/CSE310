package client;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GroupClient {

    private BufferedReader input;
    private PrintWriter output;
    JFrame frame = new JFrame("Interest Groups");
    JTextField txtField = new JTextField(40);
    JTextArea msgArea = new JTextArea(8, 40);

    //Default constructor
    public GroupClient(){
        txtField.setEditable(false);
        msgArea.setEditable(false);
        frame.getContentPane().add(txtField, "North");
        frame.getContentPane().add(new JScrollPane(msgArea), "Center");
        frame.pack();

        //Listener for text field
        txtField.addActionListener(action -> {
            output.println(txtField.getText());
            txtField.setText("");
        });
    }

    //Get the Server's IP
    private String getServerAddress(){
        return JOptionPane.showInputDialog(frame,
                "Enter Server's IP Address: ",
                "Welcome to Interest Groups",
                JOptionPane.QUESTION_MESSAGE);
    }

    //Get the User's ID
    private String getUserID(){
        return JOptionPane.showInputDialog(frame,
                "Create a User ID: ",
                "User ID Creation",
                JOptionPane.QUESTION_MESSAGE);
    }

    public void run() throws IOException{
        Socket socket = new Socket(getServerAddress(), 8026);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);

        while(true){
            String currentServerMessage = input.readLine();

            if(currentServerMessage.startsWith("CREATEUSERID")){
                output.println(getUserID());
            }

            else if(currentServerMessage.startsWith("USERIDACCEPTED")){
                txtField.setEditable(true);
            }

            else if(currentServerMessage.startsWith("MESSAGE")){
                msgArea.append(currentServerMessage.substring(8) + "\n");
            }
        }
    }

    public static void main(String[] args) throws Exception{
        GroupClient client = new GroupClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}
