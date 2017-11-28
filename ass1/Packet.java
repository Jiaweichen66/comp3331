import java.io.*;
import java.net.*;
import java.nio.*;

public class Packet implements Serializable{

	private int sequenceNo;
	private int id;
	public String data;
	private static final long serialVersionUID = 1L;


	public Packet(int id, int sequenceNo, String data) {
		this.id = id;
		this.sequenceNo = sequenceNo;
		this.data = data;
		
	}

	public static Packet createPacket(int sequenceNo, String data) {
		return new Packet(3, sequenceNo, data);
	}

	public static Packet createSyn(int sequenceNo) {
		return new Packet(0, sequenceNo, new String());
	}

	public static Packet createSynAck(int sequenceNo) {
		return new Packet(1, sequenceNo, new String());
	}

	public static Packet createAck(int sequenceNo) {
		return new Packet(2, sequenceNo, new String());
	}

	public static Packet createFin(int sequenceNo) {
		return new Packet(4, sequenceNo, new String());
	}

	public static Packet createFinAck(int sequenceNo) {
		return new Packet(5, sequenceNo, new String());
	}

	public int getId() {
		return id;
	}

	public String getSymbol() {
		if(id == 0) {
			return "S";
		}
		else if (id == 1) {
			return "SA";
		}
		else if (id == 2) {
			return "A";
		}
		else if (id == 3) {
			return "D";
		}
		else if (id == 4) {
			return "F";
		}
		else {
			return "FA";
		}
	}
	public int getSequenceNo() {
		return sequenceNo;
	}

	public byte[] getData() {
		return data.getBytes();
	}

	public byte[] getUDPdata() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.putInt(id);
		buffer.putInt(sequenceNo);
		buffer.putInt(data.length());
		buffer.put(data.getBytes(), 0, data.length());
		return buffer.array();
	}

	public static Packet parseUDPdata(byte[] udpData) {
		ByteBuffer buffer = ByteBuffer.wrap(udpData);
		int id = buffer.getInt();
		int sequenceNo = buffer.getInt();
		int length = buffer.getInt();
		byte[] data = new byte[length];
		buffer.get(data, 0, length);
		return new Packet(id, sequenceNo, new String(data));
	}
/*
	private void writeObject(ObjectOutputStream out) throws IOException{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
	}	
*/
	public String toString() {
		return "ID = " + id + " " + "Sequence # = " + sequenceNo + " " + "Data = " + data;
	}

}