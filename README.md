Contents

Server.java

this is the server class that is required to be ran before any clients can
connect to the chatrooms. It will start up a server on localhost at port
1234 and allow multiple connections from clients. if something goes wrong
it can shut itself down.

ClientHandler.java

this is the thread that the server will create for every connection that the 
server recives. they have functionality that allws them to read the clients
console and share that message with all the other ClientHandler classes. if
an error occurs here it will close its buffered reader and writer and socket
but wont shot down the server

Client.java

this is the client side of the system. after running this class it will attempt
to connect to localhost 1234 and then let the user input text. it starts out 
asking for a username and then goes onto the menus. in the menus clients can
go to public chat which is the main chat or decide to join other topic chats.
after joing topic groups they can then join the topic chats. if they wish to 
leave a group they must type 'leave group name' in the section that lists their
groups.

Message.java

class that represents a message object

Room.java

class that represents a room object

messages.json

this JSON file contains all the messages that different clients have sent. JSON
message objects contain all information about a message such as what group it was
in, who sent it, what date it was sent.
