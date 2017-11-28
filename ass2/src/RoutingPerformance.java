import java.io.*;
import java.util.*;


public class RoutingPerformance {
	
	private ArrayList<Integer>[][] routingTable;
	private ArrayList<Route> requests = new ArrayList<Route>();
	private Hashtable<String, Integer> nodes = new Hashtable<String, Integer>();
	
	private int totalVcRequests = 0;
	private int totalSuccessfulRoutedPackets = 0;
	private int numNodes = 0;
	private int totalPackets = 0;
	private int totalHops = 0;
	private int totalBlockedPackets = 0;
	private int delayTime = 0;
	

	private void circuitNetwork(Graph g, List<Route> route, String routingAlgo) {
		
		ArrayList<Route> occupiedRoutes = new ArrayList<Route>();
		
			for(Route r: route) {
				totalVcRequests++;
				totalPackets += r.end - r.start + 1;
				ArrayList<Integer> path = new ArrayList<Integer>();
				
				if(routingAlgo.equals("SHP") || routingAlgo.equals("SDP")) {
					path = routingTable[r.getSource()][r.getDestination()];
					
				}
				
				int pathLength = path.size();
				int[] point = new int[pathLength];
				int m = 0;
				for(Iterator<Integer> it = path.iterator(); it.hasNext();){
					int val = it.next();
					point[m++] = val;
				}
				removeOccupiedPath(g, occupiedRoutes, r);
				int lostPackets = 0;
				
				if (routingAlgo.equals("SHP") || routingAlgo.equals("SDP")) {
					int i = 0;
					int j = 1;
					while(i < pathLength - 1 && j < pathLength) {
						if (g.capacity[point[i]][point[j]] == 0 || g.capacity[point[j]][point[i]] == 0) {
							lostPackets = 1;
							break;
						}
						i++;
						j++;
					}
				} else {
					float max = 0;
					int i = 0;
					int j = 1;
					while(i < pathLength - 1 && j < pathLength) {
			
						if (g.occupied[point[i]][point[j]] > max) {
							max = g.occupied[point[i]][point[j]];
						}
						if (max == 1) {
							lostPackets = 1;
							break;
						}
					}
					i++;
					j++;
				}
				
				if (lostPackets == 0) {
					totalHops += (pathLength - 1);
					totalSuccessfulRoutedPackets++;
					int i = 0;
					int j = 1;
					while(i < pathLength - 1 && j < pathLength) {
					
						delayTime += g.packetDelay[point[i]][point[j]];

						g.capacity[point[i]][point[j]] -= 1;
						g.capacity[point[j]][point[i]] -= 1;
						Route tmp1 = new Route(r.start, r.end, point[i], point[j], true);
						Route tmp2 = new Route(r.start, r.end, point[j], point[i], true);
						occupiedRoutes.add(tmp1);
						occupiedRoutes.add(tmp2);
						i++;
						j++;
					}
					
				} else {
					totalBlockedPackets += r.end - r.start + 1;
				}
			}
	}
	
	//remove paths where the starting time is after the ending time of a occupied path.
	private void removeOccupiedPath(Graph g, ArrayList<Route> occupiedPaths, Route route) {
		
		int i = 0;
		while(i < occupiedPaths.size()){
				Route path = occupiedPaths.get(i);
				if(route.start >= occupiedPaths.get(i).getEnd()) {
					g.capacity[occupiedPaths.get(i).getSource()][occupiedPaths.get(i).getDestination()]++;
					i--;
					occupiedPaths.remove(path);
				}
				i++;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		
		RoutingPerformance rp = new RoutingPerformance();
		Graph g = null;
		
		if(args.length != 5) {
			System.out.println("Usage: RoutingPerformance [NETWORK_SCHEME] [ROUTING_SCHEME] [TOPOLOGY_FILE] [WORKLOAD_FILE] [PACKET_RATE]");
			return;
		}
		
		
		//we need to ready the topology file twice
		Scanner scanner = new Scanner(new FileReader(args[2]));
		Scanner scanner1 = new Scanner(new FileReader(args[2]));
		//workload file
		Scanner scanner2 = new Scanner(new FileReader(args[3]));
		
		try{
			//first read topology file to get nodes 
			while(scanner.hasNextLine()) {
				String node1 = scanner.next();
				String node2 = scanner.next();
				scanner.nextInt();
				scanner.nextInt();
				rp.nodes.put(node1, 1);
				rp.nodes.put(node2, 1);
				
			}
			scanner.close();
			Set<String> routers = rp.nodes.keySet();
			
			for(String n: routers) {
				rp.nodes.put(n, rp.numNodes++);
			}
			
			g = new Graph(rp.numNodes);
			
			int i = 0;
			while(i < rp.numNodes) {
				int j = 0;
				while(j < rp.numNodes) {
					g.packetHops[i][j] = Integer.MAX_VALUE/2;
					g.packetDelay[i][j] = Integer.MAX_VALUE/2;
					g.capacity[i][j] = 0;
					g.occupied[i][j] = 0;
					j++;
				}
				i++;
			}
			
			while(scanner1.hasNextLine()) {
				
				String node1 = scanner1.next();
				String node2 = scanner1.next();
				int propDelay = scanner1.nextInt();
				int maxVc = scanner1.nextInt();
				
				g.packetHops[rp.nodes.get(node1)][rp.nodes.get(node2)] = 1;
				g.packetDelay[rp.nodes.get(node1)][rp.nodes.get(node2)] = propDelay;
				g.capacity[rp.nodes.get(node1)][rp.nodes.get(node2)] = maxVc;
				
				g.packetHops[rp.nodes.get(node2)][rp.nodes.get(node1)] = 1;
				g.packetDelay[rp.nodes.get(node2)][rp.nodes.get(node1)] = propDelay;
				g.capacity[rp.nodes.get(node2)][rp.nodes.get(node1)] = maxVc;
			
			}
			scanner1.close();
			
			while(scanner2.hasNextLine()) {
				
				Double startTime = scanner2.nextDouble();
				String node1 = scanner2.next();
				String node2 = scanner2.next();
				Double endTime = scanner2.nextDouble();
				Route route = new Route(startTime, endTime + startTime, rp.nodes.get(node1), rp.nodes.get(node2), false);
				rp.requests.add(route);
			
			}
			
			scanner2.close();
			Comparator<Route> r = new Comparator<Route>() {
				@Override
				public int compare(Route src, Route dest) {
					if(src.start < dest.end) {
						return -1;
					}
					return 1;
				}
			};
			
			Collections.sort(rp.requests, r);
			rp.routingTable = new ArrayList[rp.numNodes][rp.numNodes];
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		int src = 0;
		while(src < rp.numNodes) {
			int[][] tmp = new int[rp.numNodes][rp.numNodes];
			int i = 0;
			while(i < rp.numNodes) {
				int j = 0;
				while(j < rp.numNodes) {
					if(args[1].equals("SHP")) tmp[i][j] = g.packetHops[i][j];
					if(args[1].equals("SDP")) tmp[i][j] = g.packetDelay[i][j];
					j++;
				}
				i++;
			}
			RoutingAlgorithm ra = new RoutingAlgorithm(tmp, src);
			ra.shortestPath();
			for(int dest = 0; dest < rp.numNodes; dest++) {

				if(src == dest) {
					continue;
				}
				Map<Integer, ArrayList<Integer>> pathList = ra.getAdjList();
				rp.routingTable[src][dest] = pathList.get(dest);

			}
			src++;
		} 
		if(args[0].equals("CIRCUIT")) {
			rp.circuitNetwork(g, rp.requests, args[1]);
		}
		
		//Output
		float percentageBlocked = (float) rp.totalBlockedPackets / rp.totalPackets;
		System.out.println("total number of virtual circuit requests: " + rp.totalVcRequests + "\n");
		System.out.println("total number of packets: " + rp.totalPackets + "\n");
		System.out.printf("number of successfully routed packets: " + "%d\n\n", rp.totalPackets-rp.totalBlockedPackets);
		System.out.printf("percentage of successfully routed packets: " + "%.2f\n\n", 100 - percentageBlocked * 100);
		System.out.println("number of blocked packets: " + rp.totalBlockedPackets + "\n");
		System.out.printf("percentage of blocked packets: " + "%.2f\n\n", percentageBlocked * 100);
		System.out.printf("average number of hops per circuit: " + "%.2f\n\n", (float) rp.totalHops / rp.totalSuccessfulRoutedPackets);
		System.out.printf("average cumulative propagation delay per circuit: " + "%.2f\n\n", (float) rp.delayTime / rp.totalSuccessfulRoutedPackets);
		
	}
}	