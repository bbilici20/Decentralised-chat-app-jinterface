import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

//we can use this controller to interact between the domain and the ui
public class Controller {
    public static Map<String, User> myUsers = new HashMap<String, User>();
    public User user;
    public String name;
    private String password;

    public Controller() {

    }

    // should be called when the user signs up. checks if the user already exists,
    // if not creates a new one.
    public int createUser(String username, int maxMessageHistorySize, String password) {
        if ((username != null) & (password != null)) {
            if (!myUsers.containsKey(username)) {
                this.name = username;
                this.password = password;
                this.user = new User(username, maxMessageHistorySize, password);
                myUsers.put(username, user);
                return 0; // success
            }
            return 1; // user already exists
        }
        return 2; // null error
    }

    // called when the user logs in
    public int login(String username, String password) {
        if (myUsers.containsKey(username)) {
            if (myUsers.get(username).getPassword().equals(password)) {
                this.user = myUsers.get(username);
                return 0; // success
            }
            return 1; // wrong password
        }
        return 2; // non-existent user
    }

    public String getPassword() {
        return password;
    }

    // returns a list of Message objects which contain the senders name/pid, message
    // content and the timestamp.
    // should be called whenever the user enteres a chat window and whenever
    // the message history is updated. If entering a private chat, username should
    // be extracted from the chosen address.
    // if it is a group, pass the group's name.
    public List<Message> getMessageHistory(String key) {
        return user.getNode().getMessageHistory(key);
    }

    // these will be used when printing the messages on the screen.
    // will iterate the list returned from getGroupMessageHistory function and call
    // for each Message element.
    public String getContent(Message msg) {
        return msg.getContent();
    }

    public String getSenderName(Message msg) {
        return msg.getSenderName();
    }

    public String getTime(Message msg) {
        return msg.getTimeStamp();
    }

    // send chat messages, special information or requests

    /*
     * send member information of a group. mailboxes have the same name as the
     * groups.
     * executed when the user clicks send request. Message content is the
     * name of the group chat the user wants to join
     */
    public void sendJoinRequest(String groupName, String requestUserAddress) {
        user.getNode().sendMessage(groupName, requestUserAddress, groupName, "join_request");
    }

    public int createGroup(String groupName) {
        return this.user.getNode().createGroup(groupName);
    }

    /*
     * the users should display the requests they received with accept or reject
     * buttons. Executed when click accept request. Message content is the node
     * addresses of the members in that group.
     */
    public void acceptJoinRequest(String groupName, String requestingNodeAddress) {
        ArrayList<String> groupMembers = user.getNode().getGroupMembers(groupName);
        String content = groupName + " " + String.join(" ", groupMembers);
        int atIndex = requestingNodeAddress.indexOf('@');
        String userName = requestingNodeAddress.substring(0, atIndex);
        user.getNode().sendMessage(userName, requestingNodeAddress, content, "request_accept");
        user.getNode().broadcastMessage(groupName, groupMembers, requestingNodeAddress, "add_member");
    }

    // executed when click reject request
    public void rejectJoinRequest(String groupName, String requestingNodeAddress) {
        user.getNode().removeRequest(groupName, requestingNodeAddress);
    }

    // requests should be displayed on the screen with an accept and a reject button
    // key is the group name, value is the list of user addresses who sent the join
    // request to that group.
    // the requests should be displayed as (group name, user address, accept or
    // reject button)
    public List<String[]> fetchRequests() {
        List<String[]> formattedRequests = new ArrayList<>();
        Map<String, ArrayList<String>> requests = user.getNode().getRequests();
        for (Map.Entry<String, ArrayList<String>> entry : requests.entrySet()) {
            String groupName = entry.getKey();
            ArrayList<String> userAddresses = entry.getValue();
            for (String userAddress : userAddresses) {
                formattedRequests.add(new String[] { groupName, userAddress });
            }
        }
        return formattedRequests;

    }

    // executed when click send message on group chat. Message content should be the
    // message the user will write in the chatbox
    public void sendGroupChatMessage(String groupName, String msg) {
        if (msg != null) {
            ArrayList<String> groupMembers = this.getGroupMembers(groupName);
            user.getNode().broadcastMessage(groupName, groupMembers, msg, "group_chat");
        }
    }

    /* executed when user clicks send message on private chat */
    public void sendPrivateChatMessage(String receiverAddress, String msg) {
        user.getNode().sendPrivateChatMessage(receiverAddress, msg);
    }

    /* returns an array of member addresses in a group */
    public ArrayList<String> getGroupMembers(String groupName) {
        return user.getNode().getGroupMembers(groupName);
    }

    public ArrayList<String> getGroupNames() {
        return user.getNode().getGroupNames();
    }

    public void exitGroup(String groupName) {
        user.getNode().exitGroup(groupName);
    }

    public ArrayList<String> getPrivateContacts() {
        return user.getNode().getPrivateContacts();
    }

    public int addPrivateChat(String receiverAddress) {
        if (!receiverAddress.contains("@")) {
            return 1;
        } else if (user.getNode().getPrivateContacts().contains(receiverAddress)) {
            return 2;
        } else {
            user.getNode().getPrivateContacts().add(receiverAddress);
            return 0;
        }
    }

}
