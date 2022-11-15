import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
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

            // Prints "Welcome"
            output.println(" _       __     __                        \n" +
                    "| |     / /__  / /________  ____ ___  ___ \n" +
                    "| | /| / / _ \\/ / ___/ __ \\/ __ `__ \\/ _ \\\n" +
                    "| |/ |/ /  __/ / /__/ /_/ / / / / / /  __/\n" +
                    "|__/|__/\\___/_/\\___/\\____/_/ /_/ /_/\\___/");


            boolean activeThread = true;
            //Infinite loop for server
            //To read inputs from client
            while(activeThread) {
                String inputString = input.readLine();
                String[] cmdStr = inputString.split(" ", 2);

                /*
                 * if output string = [Command] ex. /join
                 * else output to the rest of the users
                 *
                 * /JOIN x
                 * /CREATE x
                 * /NICK x
                 * /QUIT x
                 * /LIST x
                 * /WHOIS
                 * /WHO

                 */


                // [COMMAND] /join
                // join a specific room
                boolean roomNameTaken = false;
                String currentRoom;
                if(cmdStr[0].equals("/JOIN")) {
                    try {
                        for (room crntRoom: roomList){
                            if (crntRoom.name.equals(cmdStr[1])){
                                this.currentRoom = crntRoom;
                                crntRoom.addClient(this);
                            }
                        }
                    }catch (Exception e){
                        this.output.println(e.getLocalizedMessage());
                    }
                }



                                                //DOESNT WORK
                // [COMMAND] /CREATE
                // Create a new room
                else if (cmdStr[0].equals("/CREATE")){
                    try {
                        boolean nameExists = false;
                        for (room crntRoom: roomList){
                            if (crntRoom.name.equals(cmdStr[1])){
                                nameExists = true;
                            }
                        }
                        // Checks if there is a room with the same name
                        if (!nameExists){
                            createRoom(cmdStr[1], null);
                        }else {
                            this.output.println("Room with that name already exists");
                        }
                    }catch (Exception e){
                        this.output.println("Create Failed: " + e.getLocalizedMessage());
                    }
                }


                // [COMMAND] /WHOIS
                // get info about a user
                else if(cmdStr[0].equals("/WHOIS")) {
                    String username = cmdStr[1];
                    for (ServerThread sT: threadList){
                        if(sT.getName().equals(username)){
                            output.println(sT.getName() + '\t' + sT.currentRoom +  '\t' + sT.socket.getLocalAddress());
                        }
                    }
                }

                // [COMMAND] /WHO
                // get info clients in room
                else if(cmdStr[0].equals("/WHO")) {
                    for (ServerThread sT: threadList){
                        if(sT.currentRoom.equals(this.currentRoom)){
                            this.output.println(sT.getName() + '\t' + sT.socket.getLocalAddress());
                        }
                    }
                }

                // [COMMANd] /NICK
                // set your nickname for other users to see
                else if (cmdStr[0].equals("/NICK")){
                    //set nick
                    this.setName(cmdStr[1]);
                    output.println("Nytt nick: " + this.getName());
                }

                // [COMMAND] /LIST
                // print a list of all opened rooms
                else if (cmdStr[0].equals("/LIST")){
                    // Prints a list of the rooms
                    this.output.println("-----");
                    this.output.println(roomList.size());
                    for (room crntRoom: roomList){
                        this.output.println(crntRoom.getName() + " " + crntRoom.clientList.size() + " Connected");
                    }
                    this.output.println("-----");

                }

                // [COMMAND] /QUIT
                // exit the server
                else if(cmdStr[0].equals("/QUIT")) {
                    System.out.println("user has quit" + this.getName());

                    //remove thread from list
                    this.threadList.remove(this);

                    //close socket
                    this.socket.close();

                    //stop loop
                    activeThread = false;
                }

                else{

                    //output to all clients
                    printToALlClients(this.getName() + " Said: " +inputString, cmdStr);
                    System.out.println("Server received " + "'" + inputString + "'" + " from " + socket.getLocalAddress() + "-" + this.getName());
                }

            }

        } catch (Exception e) {
            System.out.println("Error occured " +e.getStackTrace());
            System.out.println(e.getLocalizedMessage());
        }
    }

    //Creates new room with passed variables
    private void createRoom(String name, String[] messages){
        try {
            room newRoom = new room(name, messages);
            roomList.add(newRoom);
            newRoom.addClient(this);
            this.currentRoom = newRoom;

        }catch (Exception e){
            this.output.println(e.getLocalizedMessage());
        }
    }


    //prints twice for some odd reason
    private void printToALlClients(String outputString, String[] cmdStr) {
        try {
            // Finds users that are in the same room
            for (ServerThread sT: threadList){
                if (sT.currentRoom.equals(this.currentRoom)){
                    sT.output.println(outputString);
                }
            }

        }catch (Exception e){
            output.println(e.getLocalizedMessage());
        }

    }
}