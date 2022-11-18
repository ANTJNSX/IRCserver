import java.util.*;

public class room {
    /*
    create room class
    name
    list of messages
    list of users in the room?

     */
    String name;
    String password;
    ArrayList<String> messages = new ArrayList<>();
    ArrayList<ServerThread> clientList = new ArrayList<ServerThread>();

    // Constructor Declaration of Class
    public room(String name, String[] messages, String password)
    {
        this.name = name;
        this.password = password;
    }


    //gets name
    public String getName()
    {
        return name;
    }

    //gets password
    public String getPassword()
    {
        return password;
    }

    // method 2
    public void getMessages(int x)
    {
        messages.get(x);
    }

    public void addMessage(String message){
        messages.add(message);
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
