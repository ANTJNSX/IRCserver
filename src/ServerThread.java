import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
    private Socket socket;
    public ArrayList<ServerThread> threadList;
    public ArrayList<room> rooms = new ArrayList<room>();

    private PrintWriter output;

    public ServerThread(Socket socket, ArrayList<ServerThread> threads) {
        this.setName("anon");
        this.socket = socket;
        this.threadList = threads;
    }

    @Override
    public void run() {
        room Room = new room("Main",null);
        rooms.add(Room);
        Room.addClient(this);
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

            //Adds the client to main room
            Room.addClient(this);
            room newRoom = null;

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
                 * /JOIN
                 * /CREATE
                 * /WHO  x
                 * /NICK x
                 * /QUIT x
                 * /LIST x
                 *
                 */

                System.out.println(rooms.size());

                // [COMMAND] /join
                // join a specific room
                boolean roomNameTaken = false;
                String currentRoom;
                if(cmdStr[0].equals("/JOIN")) {
                    try {
                        currentRoom = Room.name;
                        // Find the old room and leave it
                        for (room crntRoom: rooms){
                            if (crntRoom.getName().equals(currentRoom)){
                                crntRoom.removeClient(this);
                            }
                        }
                        // Find the new room and join it
                        for (room crntRoom: rooms) {
                            if (crntRoom.getName().equals(cmdStr[1]))
                                crntRoom.addClient(this);
                        }
                    }catch (Exception e){
                        this.output.println(e.getLocalizedMessage());
                    }
                }


                                                //DOESNT WORK
                // [COMMAND] /CREATE
                // Create a new room
                else if (cmdStr[0].equals("/CREATE")){
                    for (room thisRoom: rooms){
                        // If room name isn't taken, create room
                        if (!thisRoom.name.equals(cmdStr[1])){
                            try {
                                newRoom = new room(cmdStr[1], null);
                                rooms.add(newRoom);
                            }catch (Exception e ){
                                this.output.println(e.getLocalizedMessage());
                            }
                            this.output.println(newRoom);
                            this.output.println(newRoom.name);
                        }
                    }
                }


                // [COMMAND] /WHO
                // get info about a user
                else if(cmdStr[0].equals("/WHO")) {
                    String username = cmdStr[1];
                    for (ServerThread sT: threadList){
                        if(sT.getName().equals(username)){
                            output.println(sT.getName() + '\t' + sT.socket.getLocalAddress() + '\t' + '\t' + sT.getState());
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
                // print a list of all the current users
                else if (cmdStr[0].equals("/LIST")){
                    for( ServerThread sT: threadList) {
                        output.println(sT.getName() + '\t' + sT.socket.getLocalAddress());
                    }

                    for (room crntRoom: rooms){
                        this.output.println(crntRoom.getName());
                    }
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

    //prints twice for some odd reason
    private void printToALlClients(String outputString, String[] cmdStr) {
        try {
            for (room currentRoom: rooms){
                for (ServerThread roomCli: currentRoom.clientList){
                    if (roomCli.getName().equals(this.getName())){
                        for (ServerThread sT: threadList){
                            if (!sT.getName().equals(this.getName()))
                                sT.output.println(outputString);
                        }
                    }
                }
            }

        }catch (Exception e){
            output.println(e.getLocalizedMessage());
        }

    }
}