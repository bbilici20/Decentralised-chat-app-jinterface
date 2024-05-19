import java.net.InetAddress;
import com.ericsson.otp.erlang.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Arrays;
import java.util.Queue;

public class CommunicationNode {
    private OtpNode node;
    private String address; // the nodes communication address
    private ExecutorService executorService; // manages threads
    private Map<String, OtpMbox> mailboxes;
    private Map<String, ArrayList<Message>> messageHistory;
    private Map<String, ArrayList<String>> groupInfo; // maps group name to list of member addresses
    private Map<String, ArrayList<String>> joinRequests;
    private ArrayList<String> privateContacts; // will contain the node addresses who chats with the user
    private int maxMessageHistorySize;

    public CommunicationNode(String nodeName, int maxMessageHistorySize, int poolSize) throws Exception {
        this.maxMessageHistorySize = maxMessageHistorySize;
        privateContacts = new ArrayList<>();
        groupInfo = new HashMap<>();
        joinRequests = new HashMap<>();
        messageHistory = new HashMap<>();
        try {
            String ipAddress = InetAddress.getLocalHost().getHostAddress();
            address = nodeName + "@" + ipAddress;
            node = new OtpNode(nodeName);
            System.out.println("Node " + nodeName + " created successfully at " + address);
            executorService = Executors.newFixedThreadPool(poolSize);
            mailboxes = new HashMap<>();
            createMailbox(nodeName); // the mailbox that shares the same name with the username will handle private
                                     // messages.
            receiveMessages(); // Start listening for messages after setup
        } catch (Exception e) {
            System.err.println("Error creating JInterface node: " + e.getMessage());
            throw e;
        }
    }

    public OtpMbox createMailbox(String mboxName) {
        OtpMbox mbox = node.createMbox(mboxName);
        mailboxes.put(mboxName, mbox); // put the new mailbox to the hash table
        messageHistory.put(mboxName, new ArrayList<>()); // Initialize message history for the new mailbox
        return mbox;
    }

    public OtpMbox getMailbox(String mboxName) {
        return mailboxes.get(mboxName);
    }

    public String getAddress() {
        return address;
    }

    public Map<String, ArrayList<String>> getRequests() {
        return joinRequests;
    }

    public ArrayList<String> getPrivateContacts() {
        return privateContacts;
    }

    public OtpErlangTuple createMessage(OtpErlangPid pid, String content, String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = sdf.format(new Date());
        OtpErlangObject[] msgContent = new OtpErlangObject[4];
        msgContent[0] = pid;
        msgContent[1] = new OtpErlangString(this.address);
        msgContent[2] = new OtpErlangString(content);
        msgContent[3] = new OtpErlangString(timeStamp);
        msgContent[4] = new OtpErlangString(type);

        return new OtpErlangTuple(msgContent);
    }

    public OtpErlangTuple sendMessage(String mboxName, String nodeAddress, String content, String type) {
        OtpMbox mbox = mailboxes.get(mboxName);
        if (mbox != null) {
            try {
                OtpErlangTuple msg = createMessage(mbox.self(), content, type);
                mbox.send(mboxName, nodeAddress, msg);
                return msg;
            } catch (Exception e) {
                System.err.println(
                        "Error sending message to " + nodeAddress + " on mailbox " + mboxName + ": " + e.getMessage());
            }
        } else {
            System.err.println("Mailbox " + mboxName + " not found.");
            return null;
        }
        return null;
    }

    public void broadcastMessage(String mboxName, ArrayList<String> nodeAddresses, String content, String type) {
        for (String nodeAddress : nodeAddresses) {
            sendMessage(mboxName, nodeAddress, content, type);
        }
    }

    // mailboxes share the same name with the group names, it makes it easier to
    // identify them and give parameters
    public void receiveMessages() {
        for (Map.Entry<String, OtpMbox> entry : mailboxes.entrySet()) {
            String mboxName = entry.getKey();
            OtpMbox mbox = entry.getValue();
            executorService.submit(() -> {
                while (true) {
                    try {
                        OtpErlangObject o = mbox.receive();
                        if (o instanceof OtpErlangTuple) {
                            OtpErlangTuple msgTuple = (OtpErlangTuple) o;
                            handleMessage(mboxName, msgTuple);
                        }
                    } catch (Exception e) {
                        System.err.println("Error receiving message on mailbox " + mboxName + ": " + e.getMessage());
                    }
                }
            });
        }
    }

    private void handleMessage(String mboxName, OtpErlangTuple msgTuple) {
        String messageType = ((OtpErlangString) msgTuple.elementAt(4)).stringValue();
        String senderAddress = ((OtpErlangString) msgTuple.elementAt(1)).stringValue();
        switch (messageType) {
            case "private_chat":
                handlePrivateMessage(senderAddress, msgTuple);
                break;
            case "group_chat":
                handleGroupMessage(mboxName, msgTuple);
                break;
            case "join_request":
                handleJoinRequest(mboxName, msgTuple);
                break;
            case "request_accept":
                handleRequestAccept(mboxName, msgTuple);
                break;
            case "add_member":
                handleAddMember(mboxName, msgTuple);
                break;
            default:
                System.err.println("Unknown message type: " + messageType);
                break;
        }
    }

    // if it is a private message, the messages are stored in the map using the
    // sender users name as the key so that we can identify which message history is
    // from which user
    private void handlePrivateMessage(String senderAddress, OtpErlangTuple msgTuple) {
        executorService.submit(() -> {
            String senderName = senderAddress.split("@")[0];
            if (!messageHistory.containsKey(senderName)) {
                messageHistory.put(senderName, new ArrayList<>());
                privateContacts.add(senderAddress);
            }
            ArrayList<Message> history = messageHistory.get(senderName);
            if (history.size() >= maxMessageHistorySize) {
                history.remove(0); // Remove the oldest message
            }
            history.add(new Message(msgTuple));
            sortMessageHistory(senderName);
        });
    }

    // if it is a group message, the messages are stored in the map using th email
    // boxes name which is also the group name.
    private void handleGroupMessage(String mboxName, OtpErlangTuple msgTuple) {
        executorService.submit(() -> {
            ArrayList<Message> history = messageHistory.get(mboxName);
            if (history.size() >= maxMessageHistorySize) {
                history.remove(0); // Remove the oldest message
            }
            history.add(new Message(msgTuple));
            System.out.println("Group message received: " + msgTuple);
        });
    }

    private void handleJoinRequest(String mboxName, OtpErlangTuple msgTuple) {
        executorService.submit(() -> {
            String groupName = ((OtpErlangString) msgTuple.elementAt(2)).stringValue();
            String requesterAddress = ((OtpErlangString) msgTuple.elementAt(1)).stringValue();
            addNewRequest(groupName, requesterAddress);
            System.out.println("Join request received: " + msgTuple);
        });
    }

    private void handleRequestAccept(String mboxName, OtpErlangTuple msgTuple) {
        executorService.submit(() -> {
            String msg = ((OtpErlangString) msgTuple.elementAt(2)).stringValue();
            receiveAcceptRequest(msg);
            System.out.println("Request accept received: " + msgTuple);
        });
    }

    private void handleAddMember(String mboxName, OtpErlangTuple msgTuple) {
        executorService.submit(() -> {
            String member = ((OtpErlangString) msgTuple.elementAt(2)).stringValue();
            addMembertoGroup(mboxName, member);
            System.out.println("Request accept received: " + msgTuple);
        });
    }

    public ArrayList<Message> getMessageHistory(String mboxName) {
        return messageHistory.get(mboxName);
    }

    private void sortMessageHistory(String mboxName) {
        ArrayList<Message> history = messageHistory.get(mboxName);
        Collections.sort(history, Comparator.comparing(Message::getTimeStamp));
    }

    public boolean createGroup(String groupName) {
        if (groupInfo.containsKey(groupName)) {
            return false;
        } else {
            groupInfo.put(groupName, new ArrayList<>());
            joinRequests.put(groupName, new ArrayList<>());
            ArrayList<String> members = groupInfo.get(groupName);
            members.add(this.address); // when creating a group, you need to add yourself to the group
            return true;
        }
    }

    public ArrayList<String> getGroupMembers(String groupName) {
        return groupInfo.get(groupName);
    }

    // executed when the user recives add_new_member type of message, broadcasted to
    // group members to add the new member
    public void addMembertoGroup(String groupName, String memberAddress) {
        ArrayList<String> members = groupInfo.get(groupName);
        members.add(memberAddress);
    }

    // executed when the user receives request_accept type of message
    public void receiveAcceptRequest(String msg) {
        try {
            String[] parts = msg.split(" ");
            String receivedGroupName = parts[0];
            createMailbox(receivedGroupName); // the mailbox where the user will receive its group messages
            ArrayList<String> memberList = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));
            groupInfo.put(receivedGroupName, memberList);
            joinRequests.put(receivedGroupName, new ArrayList<>());
        } catch (Exception e) {
            System.err.println("Couldn't add the group: " + e.getMessage());
        }
    }

    // executed when user receives join_request type of message
    public void addNewRequest(String groupName, String requesterAddress) {
        joinRequests.get(groupName).add(requesterAddress);
    }

    public void removeRequest(String groupName, String requesterAddress) {
        joinRequests.get(groupName).remove(requesterAddress);
    }

    public void sendPrivateChatMessage(String receiverAddress, String msg) {
        String receiverName = receiverAddress.split("@")[0];
        OtpErlangTuple message = sendMessage(receiverAddress.split("@")[0], receiverAddress, msg, "private_chat");
        // if we are sending message to that user for the first time
        if (!messageHistory.containsKey(receiverName)) {
            messageHistory.put(receiverName, new ArrayList<>());
            privateContacts.add(receiverName);
        }
        ArrayList<Message> history = messageHistory.get(receiverName);
        history.add(new Message(message));
        if (history.size() >= maxMessageHistorySize) {
            history.remove(0); // Remove the oldest message
        }
        sortMessageHistory(receiverName);
    }

    public void shutdown() {
        executorService.shutdown();
    }

}
