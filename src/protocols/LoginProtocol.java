package protocols;

/**
 * Created by Xia on 12/4/16.
 */
public class LoginProtocol extends Protocol {
    private boolean isAuthorized = false;
    private String clientID = null;

    public LoginProtocol(String protocol){
        super(protocol);
    }

    public void authorize(){
        this.isAuthorized = true;
    }
    public void unauthroize(){
        this.isAuthorized = false;
    }
    public void setClientID(String id){
        this.clientID = id;
    }
    public String getClientID(){
        return this.clientID;
    }



    public boolean isAuthorized(){
        return  this.isAuthorized;
    }
}
