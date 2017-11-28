
public class Route {

	double start;
	double end;
	int source;
	int destination;
	boolean occupied;

	public Route(double s, double d, int src, int dest, boolean occupied) {
		this.start = s;
		this.end = d;
		this.source = src;
		this.destination = dest;
		this.occupied = occupied;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}

	public int getSource() {
		return source;
	}

	public int getDestination() {
		return destination;
	}
	
	public boolean isOccupied() {
		return occupied;
	}

	
}
