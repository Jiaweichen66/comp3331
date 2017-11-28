import java.util.*;
import java.net.*;
import java.io.*;

public class WebServer {

    public static void main(String[] args) {
        
        try {
            //read port number from command line
            int portNumber = Integer.parseInt(args[0]);
            //set up server 
            ServerSocket server = new ServerSocket(portNumber);
            while (true) {
                //set up client
                Socket client = server.accept();
                //create new thread to process requests
                Runnable thread = new ProcessRequest(client.getInputStream(), client.getOutputStream());
                thread.run();   
            }
        }
        catch (IOException e) {
            
            System.out.println(e);
        }
    }
}
