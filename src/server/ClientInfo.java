package server;


import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import  java.io.PrintWriter;
import  java.util.*;

/**
 * Created by Xia on 12/4/16.
 */
public class ClientInfo {
    /* Key = subscribed group ID,
    * Value =  the posts that the client has read
    */
    private HashMap<String, ArrayList<String>> client_history_info = null;
    private String clientID = null;
    
    public ClientInfo(String clientID){
        this.clientID = clientID;
        client_history_info = new HashMap<>();
    }

    public String getClientID(){
        return  this.clientID;
    }
    
    public void unsubscribe(String groupID){
        client_history_info.remove(groupID);
    }
    public void subscribe(String groupID){
        if(!client_history_info.containsKey(groupID))
            client_history_info.put(groupID, new ArrayList<String>());
    }

    public void readPost(String groupID, String postID){
        if(client_history_info.containsKey(groupID))
            client_history_info.get(groupID).add(postID);
    }

    public void saveClientInfo(String where){

        try{
            PrintWriter writer = new PrintWriter(where, "UTF-8");

            generateClientInfo(writer);

            writer.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    public int number_of_subscribed_group(){

        return this.client_history_info.size();
    }

    public boolean isSubscribed(String groupID){

        return  this.client_history_info.containsKey(groupID);

    }

    private void generateClientInfo(PrintWriter writer) {
        StringBuilder result = new StringBuilder();
        String groupID = null;
        ArrayList<String>  readPost = null;

        /*Save client information in the following format:
        * groupID:post,post,
        * */
        for (Map.Entry<String, ArrayList<String>> entry : client_history_info.entrySet()) {
            groupID = entry.getKey();
            readPost = entry.getValue();

            result.append(groupID).append(":");

            for(String post : readPost)
               result.append(post).append(",");

            /*Remove the last ',' symbol */
            if(result.charAt(result.length()-1) == ',')
                result.setCharAt(result.length()-1, '\0');


            writer.println(result.toString());

            result.setLength(0);

        }


    }

}
