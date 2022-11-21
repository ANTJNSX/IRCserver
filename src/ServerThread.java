import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerThread extends Thread {
    public boolean activeThread = true;
    public String cliInput;
    private Socket socket;
    public ArrayList<ServerThread> threadList;
    public ArrayList<room> roomList;
    private PrintWriter output;
    room currentRoom = null;
    public ServerThread(Socket socket, ArrayList<ServerThread> threads, room currentRoom, ArrayList<room> roomList) {
        this.setName("anon");
        this.socket = socket;
        this.threadList = threads;
        this.roomList = roomList;
    }

    @Override
    public void run() {

        try {
            //Reading the input from Client
            BufferedReader input = new BufferedReader( new InputStreamReader(socket.getInputStream()));

            //returning the output to the client : true statement is to flush the buffer otherwise
            //we have to do it manually
            output = new PrintWriter(socket.getOutputStream(),true);

            // Prints "Welcome" to the client when they join
            output.println(" _       __     __                        \n" +
                    "| |     / /__  / /________  ____ ___  ___ \n" +
                    "| | /| / / _ \\/ / ___/ __ \\/ __ `__ \\/ _ \\\n" +
                    "| |/ |/ /  __/ / /__/ /_/ / / / / / /  __/\n" +
                    "|__/|__/\\___/_/\\___/\\____/_/ /_/ /_/\\___/");


            //Infinite loop for input/output between server and client
            while(activeThread) {
                String inputString = input.readLine();
                String[] cmdStr = inputString.split(" ", 3);

                /*
                 * if output string = [Command] ex. /join
                 * else output to the rest of the users
                 *
                 * /JOIN x
                 * /CREATE x
                 * /NICK x
                 * /QUIT x
                 * /LIST x
                 * /WHOIS x
                 * /WHO  x
                 *
                 */

                //Turns clients command into lowercase if it starts with a backslash
                //that way it doesn't need to care about uppercase letters etc


                //Checks if the message is a command or not
                //Default send to users in the same room
                switch (cmdStr[0]){
                    case "/join":
                        // [COMMAND] /join
                        // join a specific room
                        if(cmdStr.length < 3){
                            join(cmdStr[1], null);
                        }else{
                            join(cmdStr[1], cmdStr[2]);
                        }
                        break;

                    case "/create":
                        // [COMMAND] /CREATE
                        // Create a new room
                        try {
                            createRoom(cmdStr[1], null, cmdStr[2]);
                        }catch (Exception e){
                            createRoom(cmdStr[1], null, null);

                            System.out.println(e.getLocalizedMessage());
                        }
                        break;

                    case "/nick":
                        // [COMMAND] /NICK
                        // set your nickname for other users to see
                        try {
                            nick(cmdStr[1]);
                        }catch (Exception e){
                            this.setName("anon");
                            this.output.println("Hello anon");
                            System.out.println(e.getLocalizedMessage());
                        }
                        break;

                    case "/whois":
                        // [COMMAND] /WHOIS
                        // get info about a user
                        try {
                            whois(cmdStr[1]);
                        }catch (Exception e){
                            this.output.println("Please add Who you want to see");
                            System.out.println(e.getLocalizedMessage());
                        }
                        break;

                    case "/who":
                        // [COMMAND] /WHO
                        // get info clients in room
                        who();
                        break;

                    case "/list":
                        // [COMMAND] /LIST
                        // print a list of all opened rooms
                        List();
                        break;

                    case "/help":
                        help();
                        break;

                    case "/quit":
                        // [COMMAND] /QUIT
                        // exit the server
                        System.out.println("user has quit" + this.getName());

                        //remove thread from list
                        this.threadList.remove(this);

                        //close socket
                        this.socket.close();

                        //stop loop
                        activeThread = false;
                        break;

                    default:
                        //output to all clients
                        printToALlClients(this.getName() + " Said: " +inputString, cmdStr);
                        System.out.println("Server received " + "'" + inputString + "'" + " from " + socket.getLocalAddress() + "-" + this.getName());
                        break;
                } //END OF SWITCH



            }
        } catch (Exception e) {
            System.out.println("Error occured " +e.getStackTrace());
            System.out.println(e.getLocalizedMessage());
        }
    }

    //--------------------------------------------------------------

        //Methods for all the commands

    //Creates new room with passed variables
    private void createRoom(String name, String[] messages, String password){
        try {
            boolean nameExists = false;
            for (room crntRoom: roomList){
                if (crntRoom.name.equals(name)){
                    nameExists = true;
                }
            }

            // Checks if there is a room with the same name
            if (!nameExists){
                try {
                    room newRoom = new room(name, messages, password);
                    roomList.add(newRoom);
                    newRoom.addClient(this);
                    join(name,password);
                    this.currentRoom = newRoom;

                }catch (Exception e){
                    this.output.println(e.getLocalizedMessage());
                    this.output.println(e.getMessage());
                }
            }else {
                this.output.println("Room with that name already exists");
            }
        }catch (Exception e){
            this.output.println("Create Failed: " + e.getLocalizedMessage());
        }
    }

    //Client joins room with same name
    public void join(String name, String password){
        try {
            for (room crntRoom: roomList){
                if (crntRoom.name.equals(name)){
                    this.currentRoom = crntRoom;
                    //checks password
                    if (crntRoom.password.equals(password)){

                        crntRoom.addClient(this);

                        this.output.println("-----");

                        for (String message: this.currentRoom.messages){
                            this.output.println(message);
                        }

                        //Send welcome message into room
                        String joinMessage = "Welcome to " + this.currentRoom.getName() + " " + this.getName() + "!";
                        this.output.println(joinMessage);
                    }else{
                        this.output.println("Incorrect Password");
                        this.output.println(crntRoom.password);
                    }

                }else {
                    this.output.println("No room with that name");
                }
            }
        } catch (Exception e){
            this.output.println(e.getLocalizedMessage());
        }
    }

    //List all the open rooms
    private void List(){
        try {
            // Prints a list of the rooms
            this.output.println("-----");
            this.output.println(roomList.size());

            for (room crntRoom: roomList){
                this.output.println(crntRoom.getName() + " " + crntRoom.clientList.size() + " Connected");
            }
            this.output.println("-----");

        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }

    public void nick(String name){
        //set nick
        this.setName(name);
        output.println("Nytt nick: " + this.getName());
    }


    //Lists all the connected clients
    private void who(){
        try {
            for (ServerThread sT: threadList){
                if(sT.currentRoom.equals(this.currentRoom)){
                    try {
                        this.output.println(sT.getName() + '\t' + sT.socket.getLocalAddress());
                    }catch (Exception e){
                        System.out.println(e.getLocalizedMessage());
                    }
                }
            }
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }

    //Gives information about specific client
    private void whois(String clientName){
        for (ServerThread sT: threadList){
            if(sT.getName().equals(clientName)){
                output.println(sT.getName() + '\t' + sT.currentRoom.getName() +  '\t' + sT.socket.getLocalAddress());
            }
        }
    }

    //Prints an explanation for each command
    public void help(){
        this.output.println("-----HELP-----");
        this.output.println("/JOIN []" + '\t' + "Join room with given name ");
        this.output.println("/create []" + '\t' + "creates room with given name ");
        this.output.println("/nick []" + '\t' + "Set the name that will show when you send messages, Default is Anon");
        this.output.println("/QUIT" + '\t' + "Quit the server");
        this.output.println("/list" + '\t' + "Prints a list of all the rooms");
        this.output.println("/whois []" + '\t' + "Gives information about user with given name");
        this.output.println("/who" + '\t' + "Prints a list of all the users connected to the server");
        this.output.println("/help []" + '\t' + "Prints this help text");

        this.output.println("-----END-----");
    }

    //prints twice for some odd reason
    private void printToALlClients(String outputString, String[] cmdStr) {
        try {
            // Finds users that are in the same room
            for (ServerThread sT: threadList){
                if (sT.currentRoom.equals(this.currentRoom)){
                    sT.output.println(outputString);
                    sT.currentRoom.addMessage(outputString);
                }
            }

        }catch (Exception e){
            output.println(e.getLocalizedMessage());
        }

    }
}