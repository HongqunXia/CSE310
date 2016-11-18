package server;

public class Group {

    private String name;
    private boolean subscribe;
    private int numOfNewPosts;

    public Group(String groupName, int numOfNewPosts){
        this.name = groupName;
        this.numOfNewPosts = numOfNewPosts;
    }

    public String getName(){
        return name;
    }

    public void setName(String groupName){
        this.name = groupName;
    }

    public boolean isSubscribed(){
        return subscribe;
    }

    public void setNumOfNewPosts(int newPosts){
        this.numOfNewPosts = newPosts;
    }

    public int getNumOfNewPosts(){
        return numOfNewPosts;
    }

    public void subscribe(){
        subscribe = true;
    }

    public void unsubscribe(){
        subscribe = false;
    }
}
