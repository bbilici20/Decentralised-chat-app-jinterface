import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UserInterface {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Controller currentUser;
    private Timer refreshTimer;
    private String chatName = "group1";

    public UserInterface(Controller temp) {
        currentUser = temp;
        frame = new JFrame("Chat Application"); // create a new page
        cardLayout = new CardLayout(); // so we can navigate trough pages
        mainPanel = new JPanel(cardLayout); // principal page that use cardLayout to manage all the other pages

        // Create all the pages inside de main one
        mainPanel.add(createLoginPage(), "LoginPage");
        mainPanel.add(createSignUpPage(), "SignUpPage");
        mainPanel.add(createHomePage(), "HomePage");
        mainPanel.add(createChatPage("group 1"), "ChatPage");
        mainPanel.add(createGroupDetailsPage("group 1"), "GroupDetailsPage");
        mainPanel.add(createInvitationPage(), "InvitationPage");
        mainPanel.add(createJoinRequestPage(), "JoinRequestPage");
        mainPanel.add(createCreateGroupPage(), "CreateGroupPage");
        mainPanel.add(createPrivateChatPage("chat@1"), "PrivateChatPage");
        mainPanel.add(createAddPrivateChatPage(), "AddPrivateChatPage");

        // Add the main page to the frame
        frame.add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // application close when page is closed
        frame.setSize(400, 300); // check if the size are good
        frame.setVisible(true); // so the page is visible

        showLoginPage(); // display the page

        // Initialize and start the refresh timer
        refreshTimer = new Timer(2000, e -> refreshPages());
        refreshTimer.start();

    }

    private void refreshPages() {
        if (mainPanel.isVisible()) {
            Component visibleComponent = getVisibleComponent(mainPanel);
            if (visibleComponent instanceof JPanel) {
                JPanel panel = (JPanel) visibleComponent;
                if (panel.getName().equals("HomePage")) {
                    panel.removeAll();
                    panel.add(createHomePage());
                } else if (panel.getName().equals("ChatPage")) {
                    // panel.removeAll();
                    // panel.add(createChatPage(chatName)); // Adjust according to the actual group
                }
                panel.revalidate();
                panel.repaint();

            }
        }
    }

    private Component getVisibleComponent(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Controller controller = new Controller();
        controller.createUser("Admin", 200, "somepassword");
        SwingUtilities.invokeLater(() -> new UserInterface(controller));
    }

    private JPanel createLoginPage() {
        JPanel panel = new JPanel(); // create a panel
        panel.setName("LoginPage");
        panel.setLayout(new GridLayout(3, 2)); // 3 lines and 2 columns for each layout/composants

        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField(); // user can give his id
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordText = new JPasswordField(); // user can give his password
        JButton loginButton = new JButton("Login");
        JButton signUpButton = new JButton("Sign Up");

        // Add them all to the page
        panel.add(userLabel);
        panel.add(userText);
        panel.add(passwordLabel);
        panel.add(passwordText);
        panel.add(loginButton);
        panel.add(signUpButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int loginSuccessful = currentUser.login(userText.getText(), new String(passwordText.getPassword()));
                // move to user's profile page
                if (loginSuccessful == 0) {
                    passwordText.setText("");
                    userText.setText("");
                    showHomePage();
                } else {
                    JOptionPane.showMessageDialog(panel, "Wrong username or password!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }); // if the user is logged go to home page
        signUpButton.addActionListener(e -> showSignUpPage()); // if the user doesn't have an account go to sign up page

        return panel;
    }

    private JPanel createSignUpPage() {
        JPanel panel = new JPanel(); // create the page
        panel.setName("SignUp");
        panel.setLayout(new GridLayout(4, 2));

        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField(); // user can create an id
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordText = new JPasswordField(); // user can create a password
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        JPasswordField confirmPasswordText = new JPasswordField(); // user confirm password
        JButton createAccountButton = new JButton("Create Account");
        JButton backButton = new JButton("Back");

        // Add them all to the page
        panel.add(userLabel);
        panel.add(userText);
        panel.add(passwordLabel);
        panel.add(passwordText);
        panel.add(confirmPasswordLabel);
        panel.add(confirmPasswordText);
        panel.add(createAccountButton);
        panel.add(backButton);

        // when user click on "Create Account" we display the login page
        createAccountButton.addActionListener(e -> {
            // Get the password and confirm password strings
            String password = new String(passwordText.getPassword());
            String confirmPassword = new String(confirmPasswordText.getPassword());
            // Check if passwords match
            if (password.equals(confirmPassword)) {
                int createSuccess = currentUser.createUser(userText.getText(), 20, password);
                if (createSuccess == 0) {
                    JOptionPane.showMessageDialog(panel, "New user created.");
                    passwordText.setText("");
                    confirmPasswordText.setText("");
                    userText.setText("");
                    showLoginPage();
                } else if (createSuccess == 2) {
                    JOptionPane.showMessageDialog(panel, "Username or password cannot be empty!");
                } else {
                    JOptionPane.showMessageDialog(panel, "User couldn't created.");
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Passwords do not match. Please try again.");
                passwordText.setText("");
                confirmPasswordText.setText("");
            }
        });

        backButton.addActionListener(e -> showLoginPage()); // If the user click on "Back" we go back to login page

        return panel;
    }

    private JPanel createHomePage() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setName("HomePage");

        JLabel welcomeLabel = new JLabel("Welcome to the Chat Application", SwingConstants.CENTER);
        String[] groups = currentUser.getGroupNames().toArray(new String[0]);
        JComboBox<String> displayGroups = new JComboBox<>(groups);
        JButton chatButton = new JButton("Group Chat");
        JButton invitationButton = new JButton("Invitation Requests");
        JButton createGroupButton = new JButton("Create Group");
        JButton logoutButton = new JButton("Logout");
        JButton joinRequestButton = new JButton("Send Join Request");
        JButton addPrivateChatButton = new JButton("Add Private Chat");
        JComboBox<String> displayPrivateContacts = new JComboBox<>(
                currentUser.getPrivateContacts().toArray(new String[0]));
        JButton privateChatButton = new JButton("Private Chat");

        panel.add(welcomeLabel, BorderLayout.NORTH);

        // a new panel for the WEST position, add the combo box and chat
        // button to it
        JPanel westPanel = new JPanel(new GridLayout(4, 1));
        westPanel.add(displayGroups);
        westPanel.add(chatButton);
        westPanel.add(displayPrivateContacts);
        westPanel.add(privateChatButton);
        panel.add(westPanel, BorderLayout.WEST);

        // a new panel for the CENTER position, add create group button and
        // add private chat button to it
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.add(createGroupButton);
        centerPanel.add(addPrivateChatButton);
        panel.add(centerPanel, BorderLayout.CENTER);

        // a new panel for the EAST position, add invitation button, logout
        // button, join request button, and add private chat button to it
        JPanel eastPanel = new JPanel(new GridLayout(4, 1));
        eastPanel.add(invitationButton);
        eastPanel.add(logoutButton);
        eastPanel.add(joinRequestButton);
        panel.add(eastPanel, BorderLayout.EAST);

        chatButton.addActionListener(e -> {
            String selectedGroup = (String) displayGroups.getSelectedItem();
            if (selectedGroup == null) {
                JOptionPane.showMessageDialog(panel, "Please select a group.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                showChatPage(selectedGroup);
            }

        });
        createGroupButton.addActionListener(e -> showCreateGroupPage());
        invitationButton.addActionListener(e -> showInvitationPage());
        logoutButton.addActionListener(e -> showLoginPage());
        joinRequestButton.addActionListener(e -> showJoinRequestPage());
        addPrivateChatButton.addActionListener(e -> showAddPrivateChatPage());
        privateChatButton.addActionListener(e -> {
            String selectedUser = (String) displayPrivateContacts.getSelectedItem();
            if (selectedUser == null) {
                JOptionPane.showMessageDialog(panel, "Please select a person.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                showPrivateChatPage(selectedUser);
            }
        });
        return panel;
    }

    private JPanel createChatPage(String groupName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setName("ChatPage");

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        List<Message> currentChat = currentUser.getMessageHistory(groupName);
        if (currentChat != null) {
            for (Message msg : currentChat) {
                chatArea.append(currentUser.getSenderName(msg) + ": " + currentUser.getContent(msg) + "\n");
            }
        }

        JTextField chatInput = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton detailsButton = new JButton("Details");
        JButton backButton = new JButton("Back");
        JButton exitGroupButton = new JButton("Exit Group");

        // Panel for the top row containing back button and exit group button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(backButton, BorderLayout.WEST); // Back button on the left
        topPanel.add(exitGroupButton, BorderLayout.EAST); // Exit Group button on the right

        panel.add(topPanel, BorderLayout.NORTH);

        panel.add(new JScrollPane(chatArea), BorderLayout.WEST);
        panel.add(detailsButton, BorderLayout.EAST);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(chatInput, BorderLayout.CENTER);
        southPanel.add(sendButton, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> showHomePage());
        detailsButton.addActionListener(e -> showGroupDetailsPage(groupName));
        sendButton.addActionListener(e -> {
            String message = chatInput.getText();
            currentUser.sendGroupChatMessage(groupName, message);
            chatInput.setText("");
        });
        exitGroupButton.addActionListener(e -> {
            currentUser.exitGroup(groupName);
            showHomePage();
        });

        return panel;
    }

    private JPanel createPrivateChatPage(String receiverAddress) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setName("PrivateChatPage");
        String receiverName = receiverAddress.split("@")[0];

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        List<Message> currentChat = currentUser.getMessageHistory(receiverName);
        if (currentChat != null) {
            for (Message msg : currentChat) {
                chatArea.append(currentUser.getSenderName(msg) + ": " + currentUser.getContent(msg) + "\n");
            }
        }

        JTextField chatInput = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton backButton = new JButton("Back");

        panel.add(new JScrollPane(chatArea), BorderLayout.WEST);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(chatInput, BorderLayout.CENTER);
        southPanel.add(sendButton, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);
        panel.add(backButton, BorderLayout.NORTH);

        backButton.addActionListener(e -> showHomePage());
        sendButton.addActionListener(e -> {
            String message = chatInput.getText();
            currentUser.sendPrivateChatMessage(receiverAddress, message);
            chatInput.setText("");
        });
        return panel;
    }

    private JPanel createJoinRequestPage() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        panel.setName("JoinRequestPage");

        JLabel groupNameLabel = new JLabel("Group Name:");
        JTextField groupNameField = new JTextField();
        JLabel requestAddressLabel = new JLabel("Request to:");
        JTextField requestAddressField = new JTextField();
        JButton sendRequestButton = new JButton("Send Join Request");
        JButton backButton = new JButton("Back");

        panel.add(groupNameLabel);
        panel.add(groupNameField);
        panel.add(requestAddressLabel);
        panel.add(requestAddressField);
        panel.add(sendRequestButton);
        panel.add(backButton);

        sendRequestButton.addActionListener(e -> {
            String groupName = groupNameField.getText();
            String requestAddress = requestAddressField.getText();
            currentUser.sendJoinRequest(groupName, requestAddress);
            JOptionPane.showMessageDialog(panel, "Join request sent to " + groupName);
        });

        backButton.addActionListener(e -> showHomePage());

        return panel;
    }

    private JPanel createInvitationPage() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setName("InvitationPage");

        JLabel label = new JLabel("Invitation Requests", SwingConstants.CENTER);
        JTextArea invitationListArea = new JTextArea();
        invitationListArea.setEditable(false);
        JScrollPane invitationListScrollPane = new JScrollPane(invitationListArea);

        // Load and display formatted requests
        List<String[]> formattedRequests = currentUser.fetchRequests();
        for (String[] request : formattedRequests) {
            invitationListArea.append("Group: " + request[0] + ", User: " + request[1] + "\n");
        }

        JLabel groupNameLabel = new JLabel("Group Name:");
        JTextField groupNameField = new JTextField();
        JLabel userAddressLabel = new JLabel("User Address:");
        JTextField userAddressField = new JTextField();
        JButton acceptButton = new JButton("Accept");
        JButton rejectButton = new JButton("Reject");
        JButton backButton = new JButton("Back");

        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(groupNameLabel);
        inputPanel.add(groupNameField);
        inputPanel.add(userAddressLabel);
        inputPanel.add(userAddressField);
        inputPanel.add(acceptButton);
        inputPanel.add(rejectButton);
        inputPanel.add(backButton);

        panel.add(label, BorderLayout.NORTH);
        panel.add(invitationListScrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        acceptButton.addActionListener(e -> {
            String groupName = groupNameField.getText();
            String userAddress = userAddressField.getText();
            if (groupName.isEmpty() || userAddress.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Both fields must be filled out", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                currentUser.acceptJoinRequest(groupName, userAddress);
                JOptionPane.showMessageDialog(panel,
                        "Accepted join request from " + userAddress + " for group " + groupName);
                groupNameField.setText("");
                userAddressField.setText("");
                showInvitationPage(); // Refresh the page to reflect the change
            }
        });

        rejectButton.addActionListener(e -> {
            String groupName = groupNameField.getText();
            String userAddress = userAddressField.getText();
            if (groupName.isEmpty() || userAddress.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Both fields must be filled out", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                currentUser.rejectJoinRequest(groupName, userAddress);
                JOptionPane.showMessageDialog(panel,
                        "Rejected join request from " + userAddress + " for group " + groupName);
                groupNameField.setText("");
                userAddressField.setText("");
                showInvitationPage(); // Refresh the page to reflect the change
            }
        });

        backButton.addActionListener(e -> showHomePage());

        return panel;
    }

    // All the following primitives are to show a page in the principal panel
    private void showLoginPage() {
        cardLayout.show(mainPanel, "LoginPage");
    }

    private void showSignUpPage() {
        cardLayout.show(mainPanel, "SignUpPage");
    }

    private void showHomePage() {
        cardLayout.show(mainPanel, "HomePage");
    }

    private void showChatPage(String Groupname) {
        chatName = Groupname;
        removePanelByName("ChatPage");
        JPanel chatPage = createChatPage(Groupname);
        mainPanel.add(chatPage, "ChatPage");
        cardLayout.show(mainPanel, "ChatPage");
    }

    private void showPrivateChatPage(String receiverAddress) {
        removePanelByName("PrivateChatPage");
        JPanel chatPage = createPrivateChatPage(receiverAddress);
        mainPanel.add(chatPage, "PrivateChatPage");
        cardLayout.show(mainPanel, "PrivateChatPage");
    }

    private void showInvitationPage() {
        removePanelByName("InvitationPage"); // Remove the old InvitationPage panel
        JPanel invitationPage = createInvitationPage();
        mainPanel.add(invitationPage, "InvitationPage");
        cardLayout.show(mainPanel, "InvitationPage");
    }

    private void showJoinRequestPage() {
        cardLayout.show(mainPanel, "JoinRequestPage");
    }

    private void showGroupDetailsPage(String groupName) {
        removePanelByName("GroupDetailsPage");
        JPanel DetailsPage = createGroupDetailsPage(groupName);
        mainPanel.add(DetailsPage, "GroupDetailsPage");
        cardLayout.show(mainPanel, "GroupDetailsPage");
    }

    private void showCreateGroupPage() {
        cardLayout.show(mainPanel, "CreateGroupPage");
    }

    private void showAddPrivateChatPage() {
        cardLayout.show(mainPanel, "AddPrivateChatPage");
    }

    private JPanel createGroupDetailsPage(String groupName) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setName("GroupDetails");

        JLabel groupNameLabel = new JLabel(groupName, SwingConstants.CENTER);
        JTextArea groupMembersArea = new JTextArea();
        groupMembersArea.setEditable(false);

        StringBuilder members = new StringBuilder();
        List<String> currentChat = currentUser.getGroupMembers(groupName);
        if (currentChat != null) {
            for (String member : currentUser.getGroupMembers(groupName)) {
                members.append(member).append("\n");
            }
        }
        groupMembersArea.setText(members.toString());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> showHomePage());

        panel.add(groupNameLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(groupMembersArea), BorderLayout.CENTER);
        panel.add(backButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCreateGroupPage() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setName("CreateGroup");

        JTextField groupNameField = new JTextField();
        JButton createButton = new JButton("Create");
        JButton backButton = new JButton("Back");

        panel.add(groupNameField, BorderLayout.CENTER);
        panel.add(createButton, BorderLayout.SOUTH);
        panel.add(backButton, BorderLayout.NORTH);

        backButton.addActionListener(e -> showHomePage());

        createButton.addActionListener(e -> {
            int success = currentUser.createGroup(groupNameField.getText());
            if (success == 0) {
                JOptionPane.showMessageDialog(panel, "Group created.", "Success", JOptionPane.INFORMATION_MESSAGE);
                groupNameField.setText("");
                showHomePage();
            } else if (success == 1) {
                JOptionPane.showMessageDialog(panel, "Group already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                groupNameField.setText("");
            } else {
                JOptionPane.showMessageDialog(panel, "Unknown error.", "Error", JOptionPane.ERROR_MESSAGE);
                groupNameField.setText("");
            }
        });

        return panel;
    }

    private JPanel createAddPrivateChatPage() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setName("AddPrivateChatPage");

        JTextField receiverAddressField = new JTextField();
        JButton createButton = new JButton("Create");
        JButton backButton = new JButton("Back");

        panel.add(receiverAddressField, BorderLayout.CENTER);
        panel.add(createButton, BorderLayout.SOUTH);
        panel.add(backButton, BorderLayout.NORTH);

        backButton.addActionListener(e -> showHomePage());

        createButton.addActionListener(e -> {
            int success = currentUser.addPrivateChat(receiverAddressField.getText());
            if (success == 0) {
                JOptionPane.showMessageDialog(panel, "Contact added.", "Success", JOptionPane.INFORMATION_MESSAGE);
                receiverAddressField.setText("");
                showHomePage();
            } else if (success == 1) {
                JOptionPane.showMessageDialog(panel, "Invalid address.", "Error", JOptionPane.ERROR_MESSAGE);
                receiverAddressField.setText("");
            } else {
                JOptionPane.showMessageDialog(panel, "User already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                receiverAddressField.setText("");
            }
        });

        return panel;
    }

    private void removePanelByName(String panelName) {
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel && panelName.equals(comp.getName())) {
                mainPanel.remove(comp);
                break;
            }
        }
    }

}