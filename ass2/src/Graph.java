public class Graph {

	public int[][] packetHops;
	public int[][] packetDelay;
	public int[][] capacity;
	public float[][] occupied;

	public Graph(int dimensions) {
		packetHops = new int[dimensions][dimensions];
		packetDelay = new int[dimensions][dimensions];
		capacity = new int[dimensions][dimensions];
		occupied = new float[dimensions][dimensions];
	}
}
