package protocols;

/**
 * Created by Xia on 12/4/16.
 */
public class Protocol implements java.io.Serializable{
    public  String method = null;
    public String helpMenu = null;
    private  boolean status;

    public  Protocol(){
        setMethod(null);
    }
    public Protocol(String method){
        setMethod(method);
    }

    public String getMethod(){
        return  this.method;
    }

    public  void setMethod(String method){
        this.method = method;

    }

    public  void setHelpMenu(String str){
        this.helpMenu = str;
    }

    public  String getHelpMenu(){
        return  this.helpMenu;
    }

    public boolean getStatus(){
        return  this.status;
    }
    public void setStatus(boolean s){
        this.status = s;
    }
}
