import java.util.*;
import java.net.*;
import java.io.*;

public class ProcessRequest implements Runnable {
    
    InputStream in;
    OutputStream out;

    private String fileName;
    private String fileLocation;

    //set up input and output streams 
    ProcessRequest(InputStream in, OutputStream out) {  
        this.in = in;
        this.out = out;
    }

    public void run() {
        try {
            
            // show request headers and store http header
            String header = readInput(in);
            System.out.println("CONSOLE: Reading request");
            System.out.println(header);

            // get file name from request header
            //if file has a 'H' this doesn't work
            fileLocation = "./" + header.substring(5, header.indexOf("H") - 1);
            System.out.println(fileLocation);

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
            if(header.equals("false")) {
                writer.print("HTTP/1.0 404 NOT FOUND");
            } else {
            //send response header and body
            writer.print("HTTP/1.0 200 OK\nContent-Type: text/html\n\n" + fileReader(fileLocation));
            }
            System.out.println("CONSOLE: Response Sent");

            writer.flush();
            out.close();
            in.close();         
        }
        catch (IOException e) {
            
            System.out.println(e);
        }
    }

    //Returns content of webpage as a string (html)
    public String fileReader(String file) {

        StringBuilder text = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner;
        try {
            
            scanner = new Scanner(new FileInputStream(file));
            while (scanner.hasNextLine()) {
                
                text.append(scanner.nextLine() + NL);
            }
            return text.toString();
        } catch (FileNotFoundException e) {
            String fileExists = "false";
            System.out.println(e);
            return fileExists;
        }
    }

    //Reads bytestream from client and returns a string
    private String readInput(InputStream is) {

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        try {
            
            String current;
            String header = "";
            while (!(current = in.readLine()).isEmpty()) {
                header += current + System.getProperty("line.separator");       
            }
            return header;
        }
        catch (IOException e) {
            
           System.out.println(e);
            return null;
        }
    }
}