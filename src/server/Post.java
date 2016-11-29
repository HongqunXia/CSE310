package server;

/**
 * Post Class that contains the Post Object Information
 */
public class Post {

    private int id;
    private String subject;

    public Post(int id, String subject, String contentBody) {
        this.id = id;
        this.subject = subject;
        this.contentBody = contentBody;
    }

    private String contentBody;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String contentBody) {
        this.contentBody = contentBody;
    }
}
