import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    public static void main(String[] args) {
        //using serversocket as argument to automatically close the socket
        //the port number is unique for each server

        //list to add all the clients thread
        ArrayList<ServerThread> threadList = new ArrayList<>();
        ArrayList<room> roomList = new ArrayList<>();
        try (ServerSocket serversocket = new ServerSocket(5000)) {
            while (true) {
                Socket socket = serversocket.accept();
                System.out.println("Client Connected");
                ServerThread serverThread = new ServerThread(socket, threadList, roomList);
                //starting the thread
                threadList.add(serverThread);

                serverThread.start();

                //get all the list of currently running thread

            }
        } catch (Exception e) {
            System.out.println("Error occured in main: " + e.getStackTrace());
        }
    }
}