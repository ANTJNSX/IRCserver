import java.util.*;

public class room {
    /*
    create room class
    name
    list of messages
    list of users in the room?

     */
    String name;
    String[] messages;
    ArrayList<ServerThread> clientList = new ArrayList<ServerThread>();

    // Constructor Declaration of Class
    public room(String name, String[] messages)
    {
        this.name = name;
        this.messages = messages;
    }


    // method 1
    public String getName()
    {
        return name;
    }

    // method 2
    public String[] getMessages()
    {
        return messages;
    }

    // method 3
    public int getClient()
    {
        return clientList.size();
    }

    public void addClient(ServerThread client)
    {
        clientList.add(client);
    }

    public void removeClient(ServerThread client)
    {
        clientList.remove(client);
    }

}
