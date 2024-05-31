
public class User {
    private String username;
    private String password;
    public int maxMessageHistorySize; // Maximum size limit for message history
    private CommunicationNode node;

    public User(String username, int maxMessageHistorySize, String password) {
        this.password = password;
        this.username = username;
        this.maxMessageHistorySize = maxMessageHistorySize;
        try {
            this.node = new CommunicationNode(username, maxMessageHistorySize, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public CommunicationNode getNode() {
        return node;
    }
}