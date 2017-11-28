import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Client to process ping requests over UDP. 
 */

public class PingClient
{
   private static final int MAX_TIMEOUT = 1000;
   private static final double LOSS_RATE = 0.3;
   private static final int AVERAGE_DELAY = 100;  // milliseconds

   public static void main(String[] args) throws Exception
   {
      // Get command line argument.
      if (args.length != 2) {
         System.out.println("Required arguments: Sever port");
         return;
      }
      int port = Integer.parseInt(args[1]);
      InetAddress server = InetAddress.getByName(args[0]);

      // Create random number generator for use in simulating 
      // packet loss and network delay.
      Random random = new Random();
      // Create a datagram socket for receiving and sending UDP packets
      // through the port specified on the command line.
      DatagramSocket socket = new DatagramSocket(port);
      
      int numPingRequests = 0;

      // Processing loop.
      while (numPingRequests < 10) {
      	// Timestamp in milliseconds when sent
     	  Date nowSend = new Date();
     	  long timeStampSend = nowSend.getTime();
     	
        // Set up message format
     	  String str = "seq = "  + numPingRequests + " \n";
     	  byte[] buf = new byte[1024];
     	  buf = str.getBytes();
     	 
        // Create a datagram packet to send outgoing UDP packet.
        DatagramPacket ping = new DatagramPacket(buf, buf.length, server, port);
         
        socket.send(ping);

         // Simulate network delay.
         Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
         // Sleep 1 second
         Thread.sleep(1000);
         
        try{
         	
          socket.setSoTimeout(MAX_TIMEOUT);
          // Receive incoming UDP packet, set new timestamp
          // and print message to output stream
         	DatagramPacket response = new DatagramPacket(new byte[1024], 1024);
         	socket.receive(response);
         	Date nowReceived = new Date();
         	long timeStampReceived = nowReceived.getTime();
         	printData(response, timeStampReceived - timeStampSend - 1000);
         
          } catch (IOException e) {
         
          }
		    numPingRequests++;
      }
   }

   /* 
    * Print ping data to the standard output stream.
    */
   private static void printData(DatagramPacket request, long delayTime) throws Exception
   {
      // Obtain references to the packet's array of bytes.
      byte[] buf = request.getData();

      // Wrap the bytes in a byte array input stream,
      // so that you can read the data as a stream of bytes.
      ByteArrayInputStream bais = new ByteArrayInputStream(buf);

      // Wrap the byte array output stream in an input stream reader,
      // so you can read the data as a stream of characters.
      InputStreamReader isr = new InputStreamReader(bais);

      // Wrap the input stream reader in a bufferred reader,
      // so you can read the character data a line at a time.
      // (A line is a sequence of chars terminated by any combination of \r and \n.) 
      BufferedReader br = new BufferedReader(isr);

      // The message data is contained in a single line, so read this line.
      String line = br.readLine();

      // Print host address, data received from it and delay time
      if(delayTime == 1000){
        System.out.println(
         "Ping to " + 
         request.getAddress().getHostAddress() + 
         ": " +
         new String(line) + " rtt = time out");
      } else {
        System.out.println(
           "Ping to " + 
          request.getAddress().getHostAddress() + 
          ": " +
          new String(line) + " rtt = " + delayTime + "ms");
      }
   }
}
