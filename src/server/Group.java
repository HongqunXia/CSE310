package server;

import java.util.ArrayList;

/**
 * Group Class that contains all the information for a Group Object
 */
public class Group {
    private String name;
    private boolean subscribe;
    private int numOfNewPosts;
    private ArrayList<Post> posts;

    public Group(String groupName, int numOfNewPosts, ArrayList<Post> posts){
        this.name = groupName;
        this.numOfNewPosts = numOfNewPosts;
        this.posts = posts;
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

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
    }
}
