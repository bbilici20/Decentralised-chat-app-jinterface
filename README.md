# Decentralised-chatting-app
A decentralised chat app that supports group communication. The project uses Erlang nodes through Jinterface module for distributed communication. </br>

Start epmd on terminal before running the app.</br>

## Design
### User
The User class represents an individual participating in the communication system. Each user has a username, a password and a communication node.
### Groups
Groups are represented by the name of the group and are mapped to Erlang mailboxes that handle messages between users. Each group has a name, a mailbox, a message history, and a list of addresses of the group members.

## Communication Architecture
### Communication Node
Each user has a Communication Node managing communication activities, including sending/receiving messages, storing message history, keeping private contacts, and managing group details.

### Message Structure
Each message contains the following elements:

pid: The mailbox PID.</br>
senderAddress: The sender’s address.</br>
content: The message content.</br>
timeStamp: The time the message was sent.</br>
type: The type of message (private chat, group chat, join request, etc.).</br>
### Mailboxes and Message History
Mailboxes are used for communication and are stored in a map structure. Each mailbox has a message history, which is stored and managed using an ArrayList.

### Message History Length and Causality
Messages are sorted by their timestamps to ensure causality. If the message history exceeds the maximum length, the oldest message is removed.

### Private and Group Messaging
Users communicate through mailboxes, with each group having a mailbox shared among its members. Group messages are broadcasted to all users in the group, and each user keeps track of group member addresses.

## Features
Signup and login: Users can create accounts and log in.</br>
Create groups: Users can create new groups.</br>
Search for groups and send join group requests: Users can join existing groups.</br>
Search for users and start a private chat: Users can start private chats with other users.</br>
Exit group: Users can leave groups they are a member of.</br>
## Scalability & Failures
The application can scale based on the thread pool size assigned during the creation of the Communication Node. In case of a node failure, the group communication remains unaffected for other users.

## User Interface
### Attributes
JFrame frame: The main window.</br>
CardLayout cardLayout: Manages switching between panels.</br>
JPanel mainPanel: Container for all pages.</br>
Controller currentUser: Manages user operations.</br>
Timer refreshTimer: Periodically refreshes data.</br>
String chatName: Stores the current chat group name.</br>
### Page Creation
Different pages are created for various functionalities:</br>

Login Page: For user login.</br>
Sign-Up Page: For account creation.</br>
Home Page: Displays user options.</br>
Chat Pages: Shows chat interfaces.</br>
### Refresh Method
The refreshPages method updates the content of the currently visible page to reflect new data, ensuring real-time chat updates.
