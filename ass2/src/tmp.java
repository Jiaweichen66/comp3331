
///////////////////////////////////////////////////////////
/*	public static class Graph {

		private Set<Node> nodes;
		private Set<Edge> edges;
		private Map<Node, Set<Edge>> adjList;
		
		public Graph() {
			nodes = new HashSet<>();
			edges = new HashSet<>();
			adjList = new HashMap<>();
		}
		
		public String toString() {
			String s = "";
			for(Node item : nodes)
	        {
				if(item != null){
					s += item.getLabel() + "\n"; 
				}
	        }

	        return s;
			
		}
		public Node addNode(String key) {
			int temp = 0;
			Node n1 = null;
			for(Node item: nodes) {
				if(item.key.equals(key)){
					temp = 1;
					n1 = item;
					return n1;
				}
			}
			if(temp == 0) {
				Node n = new Node(key);
				nodes.add(n);
				return n;
			}
			return null;
		}

		public boolean addEdge(Edge e) {

			if (!edges.add(e)) {
            	return false;
            }

            adjList.putIfAbsent(e.n1, new HashSet<>());
            adjList.putIfAbsent(e.n2, new HashSet<>());

            adjList.get(e.n1).add(e);
            Edge d = new Edge(e.n2, e.n1, e.propDelay, e.maxVc);
            adjList.get(e.n2).add(d);
            
            return true;

		}

		public boolean addEdge(String key1, String key2, int propDelay, int maxVc) {
            
			return addEdge(new Edge(addNode(key1), addNode(key2), propDelay, maxVc));

        }

        public Set<Node> getNodes() {
            return Collections.unmodifiableSet(nodes);
        }

        public Set<Edge> getEdges() {
            return Collections.unmodifiableSet(edges);
        }

        public Map<Node, Set<Edge>> getAdjList() {
            return Collections.unmodifiableMap(adjList);
        }

        public Set<Node> getAdjVertices(Node n) {
            return adjList.get(n).stream()
                          .map(e -> e.n1.equals(n) ? e.n2 : e.n1)
                          .collect(Collectors.toSet());
        }

	}

	public static class Node {

		private String key;
		private ArrayList<Node> neighbours;
		private LinkedList<Node> path;
		int distance;

		public Node(String key) {
			this.key = key;
			neighbours = new ArrayList<Node>();
			path = new LinkedList<Node>();
			distance = 1;
			
		}

		@Override
		public boolean equals(Object o) {
			return (o instanceof Node) && (((Node) o).getLabel()).equals(this.getLabel());
		}
		
		public String toString() {
			return this.key;
		}

		public String getLabel() {
			return key;
		}

		public void setLabel(String key) {
			this.key = key;
		}

		public ArrayList<Node> getNeighbours() {
			return neighbours;
		}

		public LinkedList<Node> getPath() {
			return path;
		}
		
		public ArrayList<Node> findNeighbours(Graph g) {
			
			for(Node item: g.adjList.keySet()) {
				if(item.key.equals(this.key)) {	
					Set<Edge> set = g.adjList.get(item);
					Iterator<Edge> iter = set.iterator();
					while(iter.hasNext()) {
						this.neighbours.add(iter.next().n2);
					}	
				}
				
 			}
			return this.neighbours;
		}
	}

	public static class Edge {

		Node n1, n2;
		int propDelay;
		int maxVc;

		public Edge(Node n1, Node n2, int propDelay, int maxVc) {
			this.n1 = n1;
			this.n2 = n2;
			this.propDelay = propDelay;
			this.maxVc = maxVc;
			
		}
		
		public String toString() {
			return n1 + " " + n2;
		}
		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			if(!(o instanceof Edge)) {
				return false;
			}
			Edge _o = (Edge) o;
			return _o.n1.equals(n1) && _o.n2.equals(n2) &&
                   _o.propDelay == propDelay && _o.maxVc == maxVc;
		}

		@Override
		public int hashCode() {
			int result = n1.hashCode();
			result = 31 * result + n2.hashCode();
            result = 31 * result + propDelay;
            result = 31 * result + maxVc;
            return result;
		}
	}

	public static void main(String[] args) {
		
		RoutingPerformance r = new RoutingPerformance();
		Scanner sc = null;
		Scanner sc1 = null;
		String workload = args[1];
		String topology = args[0];
		Graph g = new Graph();
		ArrayList<Connection> requests = new ArrayList<Connection>();
		try {
			sc = new Scanner(new FileReader(topology)); 
			sc1 = new Scanner(new FileReader(workload));
			while(sc.hasNextLine()) {
				String node1 = sc.next();
				String node2 = sc.next();
				int prop = sc.nextInt();
				int vc = sc.nextInt();
				g.addEdge(node1, node2, prop, vc);
				
			}
			
			while(sc1.hasNextLine()) {
			
				double st = sc1.nextDouble();
				String s = sc1.next();
				String d = sc1.next();
				double et = sc1.nextDouble();
				Connection c = new Connection(st, et, s, d);
				requests.add(c);
				
			}
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file");  
            ex.printStackTrace();
        }
		Node dest = null;
		Node src = null;
		
		
		
		for(Connection c: requests) {
			for(Iterator<Node> it = g.nodes.iterator(); it.hasNext();) {
				Node n = it.next();
				if(c.getSource().equals(n.key)){
					src = n;
				}
				if(c.getDestination().equals(n.key)) {
					dest = n;
				}
			}
			//System.out.println(requests.size());
			r.sph(g, src, dest);
			
			for(Iterator<Node> it = g.nodes.iterator(); it.hasNext();){
				Node y = it.next();
				if(y.key.equals(dest.key)) {
					System.out.print("Source - " + src.key + ", Destination - "+y+" , Dist - "+ y.distance+" , Path - ");
					for(Node pathvert:y.path) {
						System.out.print(pathvert+" ");
					}
					
					System.out.println(""+y);
				}
				while(!y.path.isEmpty()) {
					y.path.remove();
					y.distance = 1;
				}
			}
			

		}
	}
	
	public boolean sph(Graph g, Node source, Node destination) {
		
		//System.out.println(source.key + " - " + destination.key);
		Comparator<Node> comparator = new Compare();
		PriorityQueue<Node> queue = new PriorityQueue<Node>(comparator);
		Set<String> visited = new HashSet<String>();

		queue.add(source);
		visited.add(source.key);
		source.distance = 0;
		while(!queue.isEmpty()) {
			
			Node n = queue.peek();
			
			for(Map.Entry<Node, Set<Edge>> e: g.adjList.entrySet()) {
				for(Iterator<Edge> it = e.getValue().iterator(); it.hasNext();) {
					Edge w = it.next();
					if(isNeighbour(g, source, destination)) {
						//System.out.println("HI");
						destination.distance = 1;
						destination.path.add(source);
						return true;
					}
					
					if(e.getKey().key.equals(n.key)) {
						System.out.println(" Node = " + e.getKey().key + " - " + w.n2.key);
						if(visited.contains(w.n2.key)) {
							//System.out.println("HI");
							queue.remove(w.n2);
						}
						else {
							//change distance
							w.n2.distance += e.getKey().distance;
							if(!queue.contains(w.n2)) {
								queue.add(w.n2);
								if(!w.n2.path.contains(e.getKey())){
									w.n2.path = new LinkedList<Node>(e.getKey().path);
									w.n2.path.add(e.getKey());
								}
							}
							
						}
						if(w.n2.key.equals(destination.key)) {
							System.out.println("HIII " + w.n2.distance + " " + source.key + " - " + w.n2.key);
							return true;
						}
					}
					//System.out.println(queue);
				}

			}
			visited.add(n.key);
			queue.remove(n);

		}
		return false;
	}
	
	public boolean isNeighbour(Graph g, Node node1, Node node2) {
    	
		for(Map.Entry<Node, Set<Edge>> e: g.adjList.entrySet()) {	
			for(Iterator<Edge> it = e.getValue().iterator(); it.hasNext();) {
				Edge w = it.next();
				
				if(e.getKey().key.equals(node1.key) && w.n2.key.equals(node2.key)) {
					return true;
				}
			}
		}
		
		return false;
    }
	
	public static class Compare implements Comparator<Node> {

		@Override
		public int compare(Node arg0, Node arg1) {
			if(arg0.distance < arg1.distance) {
				return -1;
			}
			else if(arg0.distance > arg1.distance) {
				return 1;
			}
			return 0;
		}
	}
	
*/