import java.util.*;

public class RoutingAlgorithm {

	private int[][] graph;
	private int src;
	private int size;
	private Map<Integer, ArrayList<Integer>> adjList = new HashMap<Integer, ArrayList<Integer>>();
	
	public RoutingAlgorithm(int[][] graph, int src) {
		this.graph = graph;
		this.src = src;
		this.size = graph.length;
	}
	
	public void shortestPath() {
		
		int[] visited = new int[size];
		int dest = 0;
		while(dest < size) {
			
			ArrayList<Integer> neighbours = new ArrayList<Integer>();
			neighbours.add(src);
			neighbours.add(dest);
			adjList.put(dest, neighbours);
			dest++;
		}

		visited[src] = 1;
		
		int counter = 1; 
		while(counter < size) {
			
			int tmp = -1;
			int weight = Integer.MAX_VALUE/2;
			dest = 0;
			
			while(dest < size) {
				if(visited[dest] == 0 && graph[src][dest] < weight) {
					
					weight = graph[src][dest];
					tmp = dest;
					
				}
				dest++;
			}
			visited[tmp] = 1;
			
			dest = 0;
			while(dest < size) {
				
				if(visited[dest] == 0 && graph[src][tmp] + graph[tmp][dest] < graph[src][dest]) {
					
					graph[src][dest] = graph[src][tmp] + graph[tmp][dest];
					ArrayList<Integer> neighbours = adjList.get(dest);
					ArrayList<Integer> tmpList = adjList.get(tmp);
					neighbours.clear();
					neighbours.addAll(tmpList);
					neighbours.add(dest);
				}
				dest++;
			}
			counter++;
		}
	}
	
	public Map<Integer, ArrayList<Integer>> getAdjList() {
		return adjList;
	}
}
