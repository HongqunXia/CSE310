package protocols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;

/**
 * Created by Xia on 12/4/16.
 */
public class AGProtocol extends Protocol {
    private String  subCommand = null;
    private ArrayList<String> subscribedGroups = null;
    private ArrayList<String> unSubscribedGroups = null;

    private HashMap<String, Boolean> nGroups =  null;

    private HashMap<Integer,String>  infoOnConsole = null;

    private int number_of_request_groups = 0;
    private int number_of_success = 0;






    public  AGProtocol(String subCommand){
        super("AG");
        this.subCommand = subCommand;
        this.nGroups = new HashMap<>();
        this.subscribedGroups = new ArrayList<>();
        this.unSubscribedGroups = new ArrayList<>();
        this.infoOnConsole = new HashMap<>();
    }

    public HashMap<String, Boolean> getNGroupsList(){
        return this.nGroups;
    }
    public void addGroup(String groupID, boolean subscribe){
        this.nGroups.put(groupID, subscribe);
    }

    public String nGroupsString(){
        StringBuffer stringBuffer = new StringBuffer();
        int count = 0;
        String subscribedSymbol = null;
        for (Map.Entry<String, Boolean> entry : nGroups.entrySet()) {

            String groupID = entry.getKey();
            subscribedSymbol =  entry.getValue() == true ? "s" : " ";

            stringBuffer.append(++count).append(". ").append("(").
                    append(subscribedSymbol).append(")").
                    append(" ").append(groupID).append("\n");

            infoOnConsole.put(count, groupID);

        }

        return  stringBuffer.toString();
    }

    public void setSubCommand(String subCommand){
        this.subCommand = subCommand;
    }

    public String getSubCommand(){
        return  this.subCommand;
    }

    public void addSubscribedGroup(String group){
        this.subscribedGroups.add(group);
    }

    public void removeSubscribedGroup(String group){
        this.subscribedGroups.remove(group);
    }

    public ArrayList<String> getSubscribedGroups(){return this.subscribedGroups;}


    public  void addUnsubscribedGroup(String group){
        this.unSubscribedGroups.add(group);
    }

    public  void removeUnsubscribedGroup(String group){
        this.unSubscribedGroups.remove(group);
    }
    public ArrayList<String> getUnSubscribedGroups(){return  this.unSubscribedGroups; }

    public  HashMap<Integer,String> getinfoOnConsole(){
        return  this.infoOnConsole;
    }

    public void setNumber_of_success(int n){
        this.number_of_success = n;
    }
    public int getNumber_of_success(){
        return  this.number_of_success;
    }

    public void setNumber_of_request_groups(int n){
        this.number_of_request_groups = n;
    }
    public int getNumber_of_request_groups(){
        return  this.number_of_request_groups;

    }

}
