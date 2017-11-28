import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class Receiver {
	
   // private static final int PORT = Integer.parseInt(args[0]);
   // private static final String FILE = args[1];
    private static final String HOSTNAME = "localhost";

    private static int port;
    private static String fileName;
    private static PrintWriter log;
    private static PrintWriter writeFile;
    private static InetAddress host;
    private static DatagramSocket serverSocket;
    private static FileWriter file;
    private static FileWriter logFile;
    private static BufferedWriter fileBuf;
    private static BufferedWriter logBuf;
    private static InetAddress sendHost;
    private static int sendPort;

    private int i = 0;


    public Receiver(int port, String fileName) {
        try {
            this.port = port;
            host = InetAddress.getByName(HOSTNAME);
            serverSocket = new DatagramSocket(port);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        if(args.length != 2) {
            System.err.println("Usage: java receiver <receiver port> <file.txt>");
            System.exit(1);
        }
        try {
            port = Integer.parseInt(args[0]);
            fileName = args[1];
            
            file = new FileWriter(fileName, true);
            logFile = new FileWriter("receiver.txt", true);
            fileBuf = new BufferedWriter(file);
            logBuf = new BufferedWriter(logFile);
            writeFile = new PrintWriter(fileBuf);
            log = new PrintWriter(logBuf);

            Receiver r = new Receiver(port, fileName);

            r.start();
        } catch (Exception e) {
            System.out.println("Error:");
            e.printStackTrace();
        }


    }

    public void start() throws Exception {
        
        boolean done = false;
        int expected = -1;
        int lastReceived = -1;
        //two sockets, one for sending data and one for receiving data
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while(!done) {
            //create a packet to receive data
            
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            receiveData = receivePacket.getData();
            Packet p = Packet.parseUDPdata(receiveData);

            sendHost = receivePacket.getAddress();
            sendPort = receivePacket.getPort();

            System.out.println("Received Packet");
            
            //Packet p = readObj(receiveData);

            if(p.getId() == 0) {

                System.out.println("Received SYN");
                log.println("rcv  " + p.getSymbol() + "  " + p.getSequenceNo());
                sendData = p.getUDPdata();
                lastReceived = p.getSequenceNo();
                expected = p.getSequenceNo() + 1;

                Packet synAck = Packet.createSynAck(expected);
                sendData = synAck.getUDPdata();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendHost, sendPort);
                serverSocket.send(sendPacket);

                //sendObject(synAck, sendData);
                log.println("snd  " + synAck.getSymbol() + "  " + synAck.getSequenceNo());
                System.out.println("Sent SYN-ACK");
            }
            else if(p.getId() == 2) {  //&& p.getSequenceNo() == expected) {
                
                System.out.println("Received ACK");
                log.println("rcv  " + p.getSymbol() + "  " + p.getSequenceNo());
                lastReceived = p.getSequenceNo();
                expected = p.getSequenceNo() + 1;

            }
            else if(p.getId() == 4) {
                
                log.println("rcv  " + p.getSymbol() + "  " + p.getSequenceNo());
                lastReceived = p.getSequenceNo();
                expected = p.getSequenceNo() + 1;

                System.out.println("Received FIN");
                Packet ack = Packet.createFinAck(expected);
                //sendData = ack.getUDPdata();

                sendData = ack.getUDPdata();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendHost, sendPort);
                serverSocket.send(sendPacket);
                //sendObject(ack, sendData);
                System.out.println("Sent FIN-ACK");
                log.println("snd  " + ack.getSymbol() + "  " + ack.getSequenceNo());

                i++;
                if(i == 2) {
                    done = true;
                }

            }  
            else if (p.getId() == 3) { //&& p.getSequenceNo() == expected) {
                
                log.println("rcv  " + p.getSymbol() + "  " + p.getSequenceNo());

                lastReceived = p.getSequenceNo();
                expected = p.getSequenceNo() + 1;

                Packet ack = Packet.createAck(expected);
                sendData = ack.getUDPdata();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendHost, sendPort);
                serverSocket.send(sendPacket);

                //sendObject(ack, sendData);
                System.out.println("Sent ACK");

                //write data to file
                writeFile.println(new String(p.getData()));
                log.println("snd  " + ack.getSymbol() + "  " + ack.getSequenceNo());
                System.out.println("Wrote data to file");

            }
            else {
                
                log.println("rcv  " + p.getSymbol() + "  " + p.getSequenceNo());

                //received a packed that was not expected;
                lastReceived = p.getSequenceNo();
                expected = p.getSequenceNo() + 1;

                Packet wrongPack = Packet.createAck(expected);
                sendData = wrongPack.getUDPdata();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendHost, sendPort);
                serverSocket.send(sendPacket);

                //sendObject(p, sendData);
                log.println("snd  " + wrongPack.getSymbol() + "  " + wrongPack.getSequenceNo());

            //Packet p = readObject(receiveData);

            }

        }
        writeFile.close();
        log.close();
        serverSocket.close();
    }
/*
    public void sendObject(Packet p, byte[] sendData) throws Exception{
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            os.flush();
            os.writeObject(p);
            os.flush();
            sendData = outputStream.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendHost, sendPort);
            serverSocket.send(sendPacket);
            os.reset();
            outputStream.reset();
            os.close();
            outputStream.close();
        } catch(UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Packet readObj(byte[] receivedData) throws Exception{
        
        try{
            ByteArrayInputStream in = new ByteArrayInputStream(receivedData);
            ObjectInputStream is = new ObjectInputStream(in);
            Packet p = (Packet) is.readObject();
            is.close();
            in.close();
            return p;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;    
    }

}
*/
}