import java.net.ServerSocket;
import java.util.ArrayList;
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
    List<ServerThread> client;

    // Constructor Declaration of Class
    public room(String name, String[] messages, ServerThread client)
    {
        this.name = name;
        this.messages = messages;
        this.client.add(client);
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
    public ServerThread getClient(int x)
    {
        return client.get(x);
    }

}
