import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class Sender {

    public static final int ISN = 300;

    private static String hostName;
    private static int port;
    private static String fileName;
    private static InetAddress host;
    private BufferedReader br;
    private LinkedList<Packet> packets = new LinkedList<Packet>();
    private Packet currPacket = null;
    private boolean done = false;
    private FileInputStream f;
    private DataInputStream d;
    public static int mss;
    public static int mws;
    public static int timeout;
    public static double pdrop;
    public static int seed;

    private static PrintWriter log;
    private static FileWriter logFile;
    private static BufferedWriter logBuf;

    public static DatagramSocket clientSocket;

    public static int lastReceived = 0;
    public static int expected = 0;
    public static boolean rcvdSynAck = false;


    public Sender(String hostName, int port, String fileName) {
        try {
            this.port = port;
            this.fileName = fileName;
            f = new FileInputStream(fileName);
            d = new DataInputStream(f);
            br = new BufferedReader(new InputStreamReader(d));
            host = InetAddress.getByName(hostName);
            clientSocket = new DatagramSocket();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length != 8) {
            System.err.println("Usage: java sender <receiver ip> <receiver port> <file.txt> <MWS> <MSS> <timeout> <pdrop> <seed>");
            System.exit(1);
        }
        try {
            hostName = args[0];
            port = Integer.parseInt(args[1]);
            fileName = args[2];
            mws = Integer.parseInt(args[3]);
            mss = Integer.parseInt(args[4]);
            timeout = Integer.parseInt(args[5]);
            pdrop = Double.parseDouble(args[6]);
            seed = Integer.parseInt(args[7]);

            logFile = new FileWriter("sender.txt", true);
            logBuf = new BufferedWriter(logFile);
            log = new PrintWriter(logBuf);

            Sender s = new Sender(hostName, port, fileName);
            s.start();
            Thread.sleep(1000);
            clientSocket.close();
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    private void transfer() throws Exception {

        while(!done) {

            String input = br.readLine();
            //System.out.println(input);
            if (input != null) {
                Packet p = Packet.createPacket(expected, new String(input));
                currPacket = p;

                //if window is full, wait a while and try sending again
                boolean sent = false;
                while(!sent) {
                    if(packets.size() < mws) {
                        sendPackets(false);
                        sent = true;
                    } else {
                        Thread.sleep(5);
                    }
                }

                //add the packet just sent to packets buffer
                synchronized(packets) {
                    packets.add(p);
                }
                //start timer if not already started
                //if(!scheduled) {
                //    scheduleTask();
                //}
            } else {
                done = true;
            }
        
        }
    }

    private void listen() throws Exception {

        byte[] receiveData = new byte[mss];
        byte[] sendData = new byte[mss];

        //loop until all packets are sent
        while(!done) {
            

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            receiveData = receivePacket.getData();
            Packet ack = Packet.parseUDPdata(receiveData);
            //System.out.println(readObj(receiveData));

            //Packet ack = readObj(receiveData);
            //System.out.println(ack);

            System.out.println("Expected = " + expected + "rcvd seq no = " + ack.getSequenceNo());
            //received ACK
            if(ack.getId() == 2 && ack.getSequenceNo() == expected) {
                //print to log
                System.out.println("Received ACK");
                log.println("rcv  " + ack.getSymbol() + "  " + ack.getSequenceNo());
                lastReceived = ack.getSequenceNo();
                expected = ack.getSequenceNo() + 1;

                synchronized(packets) {
                    Packet acked = null;
                    int expectedSeq = expected;
                    for(Packet p: packets) {
                        if(ack.getSequenceNo() == expectedSeq) {
                            acked = p;
                        }
                    }
                    if(acked != null) {
                        Packet p = packets.removeFirst();
                        while(p != acked) {
                            p = packets.removeFirst();
                        }
                    }
                }
            }
            //received SYN-ACK
            else if(ack.getId() == 1 && ack.getSequenceNo() == expected) {
                
                System.out.println("Received SYN-ACK");
                log.println("rcv  " + ack.getSymbol() + "  " + ack.getSequenceNo());


                lastReceived = ack.getSequenceNo();
                expected = ack.getSequenceNo() + 1;
                Packet ackn = Packet.createAck(expected);
                sendData = ackn.getUDPdata();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getHost(), getPort());
                clientSocket.send(sendPacket);

                //sendObject(ackn, sendData);
                System.out.println("Sent ACK");
                log.println("snd  " + ackn.getSymbol() + "  " + ackn.getSequenceNo());
            }
            else if(ack.getId() == 5 && ack.getSequenceNo() == expected) {

                System.out.println("Received FIN-ACK");
                lastReceived = ack.getSequenceNo();
                expected = ack.getSequenceNo() + 1;
                log.println("rcv  " + "A  " + ack.getSequenceNo());
                System.out.println("Terminating session!");
            }

            //Packet ack = readObject(receiveData);


       }     
    }

    private void sendPackets(boolean sentAll) throws Exception {
        byte[] sendData = new byte[mss];
        synchronized(packets) {
            //if sendAll is true then send all unacked packets in the buffer
            Random rand = new Random(seed);
            double r = rand.nextDouble();
            /*
            if(sentAll) {
                for(Packet p: packets) {
                    sendData = p.getUDPdata();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getHost(), getPort());
                    clientSocket.send(sendPacket);
                    log.println("snd  " + p.getSymbol() + "  " + p.getSequenceNo());

                    //System.out.println("Sent Packet");
                    //print data to log

                }*/
        //  } else {
                if(r > pdrop) {
                    //sendObject(currPacket, sendData);
                    sendData = currPacket.getUDPdata();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getHost(), getPort());
                    clientSocket.send(sendPacket);

                    log.println("snd  " + currPacket.getSymbol() + "  " + currPacket.getSequenceNo());
                    //Thread.sleep(1000);
                }
        //    }
        }
    }

    public void start() throws Exception {

        Thread listener = new Thread() {
            public void run() {
                try {
                    listen();
                } catch (Exception e) {
                    System.err.println("Error:");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        };
         listener.start();
       // while(!rcvdSynAck) {
            //send SYN packet
        Packet syn = Packet.createSyn(ISN);
        expected = syn.getSequenceNo() + 1;
        
        byte[] sendData = syn.getUDPdata();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getHost(), getPort());
        clientSocket.send(sendPacket);
        //sendObject(syn, sendData);
        System.out.println("Sent SYN");
        log.println("snd  " + syn.getSymbol() + "  " + syn.getSequenceNo());
   // }

       

        transfer();

        //send FIN packet
        int i = 0;
        while(i < 2) {
            Packet fin = Packet.createFin(expected);
            //sendObject(fin, sendData);
            sendData = fin.getUDPdata();
            sendPacket = new DatagramPacket(sendData, sendData.length, getHost(), getPort());
            clientSocket.send(sendPacket);
            System.out.println("Sent FIN");
            log.println("snd  " + fin.getSymbol() + "  " + fin.getSequenceNo());
            i++;
        }  

        log.close();

    }

    private int getPort() {
        return port;
    }

    private InetAddress getHost() {
        return host;
    }

    public String toString() {
        return port + " " + fileName + " " + host;
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
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getHost(), getPort());
            clientSocket.send(sendPacket);
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
