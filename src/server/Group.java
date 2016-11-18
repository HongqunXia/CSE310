package server;

public class Group {

    private String name;
    private boolean subscribe;

    public Group(String groupName){
        this.name = groupName;
    }

    public String getName(){
        return name;
    }

    public void setName(String groupName){
        this.name = groupName;
    }

    public void subscribe(){
        subscribe = true;
    }

    public void unsubscribe(){
        subscribe = false;
    }
}
