//source: http://www.cs.fiu.edu/~weiss/dsj2/code/Graph.java
package lsdm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import com.mongodb.BasicDBObject;

class GraphException extends RuntimeException
// Used to signal violations of preconditions for
// various shortest path algorithms.
{
	public GraphException(String name) {
		super(name);
	}
}

// Represents an edge in the graph.
class Edge {
	public Vertex dest; // Second vertex in Edge
	public double cost; // Edge cost
	public String edge;

	public Edge(Vertex d, double c, String e) {
		dest = d;
		cost = c;
		edge = e;

	}
}

// Graph class: evaluate shortest paths.
//
// CONSTRUCTION: with no parameters.
//
// ******************PUBLIC OPERATIONS**********************
// void addEdge( String v, String w, double cvw )
// --> Add additional edge
// void printPath( String w ) --> Print path after alg is run
// void unweighted( String s ) --> Single-source unweighted
// void dijkstra( String s ) --> Single-source weighted
// void negative( String s ) --> Single-source negative weighted
// void acyclic( String s ) --> Single-source acyclic
// ******************ERRORS*********************************
// Some error checking is performed to make sure graph is ok,
// and to make sure graph satisfies properties needed by each
// algorithm. Exceptions are thrown if errors are detected.

public class Graph {
	public static final double INFINITY = Double.MAX_VALUE;
	public Map vertexMap = new HashMap(); // Maps String to Vertex

	/**
	 * Add a new edge to the graph.
	 */
	public void addEdge(String sourceName, String destName, double cost,
			String edgeName) {
		Vertex v = getVertex(sourceName, true);
		Vertex w = getVertex(destName, true);
		v.adj.add(new Edge(w, cost, edgeName));
	}

	public Boolean removeEdge(String sourceName, String destName, double cost,
			String edgeName) {
		Vertex v = getVertex(sourceName, false);
		Vertex w = getVertex(destName, false);
		for (int a = 0; a < v.adj.size(); a++) {
			Edge e = (Edge) v.adj.get(a);
			if (e.edge == edgeName)
				return v.adj.remove(e);
		}
		return false;
	}

	/**
	 * Driver routine to handle unreachables and print total cost. It calls
	 * recursive routine to print shortest path to destNode after a shortest
	 * path algorithm has run.
	 */
	public void printPath(String destName) {
		Vertex w = (Vertex) vertexMap.get(destName);
		if (w == null)
			throw new NoSuchElementException("Destination vertex not found");
		else if (w.dist == INFINITY)
			System.out.println(destName + " is unreachable");
		else {
			System.out.print("(Cost is: " + w.dist + ") ");
			printPath(w);
			System.out.println();
		}
	}

	/**
	 * If vertexName is not present, add it to vertexMap. In either case, return
	 * the Vertex.
	 */
	public Vertex getVertex(String vertexName, Boolean check) {
		Vertex v = (Vertex) vertexMap.get(vertexName);
		if (v == null && check == true) {
			v = new Vertex(vertexName);
			vertexMap.put(vertexName, v);
		}
		return v;
	}

	/**
	 * Recursive routine to print shortest path to dest after running shortest
	 * path algorithm. The path is known to exist.
	 */
	private void printPath(Vertex dest) {
		if (dest.prev != null) {
			printPath(dest.prev);
			System.out.print(" to ");
		}
		System.out.print(dest.name);
	}

	/**
	 * Initializes the vertex output info prior to running any shortest path
	 * algorithm.
	 */
	public void clearAll() {
		for (Iterator itr = vertexMap.values().iterator(); itr.hasNext();)
			((Vertex) itr.next()).reset();
	}

	/**
	 * Single-source unweighted shortest-path algorithm.
	 */
	public void unweighted(String startName) {
		clearAll();

		Vertex start = (Vertex) vertexMap.get(startName);
		if (start == null)
			throw new NoSuchElementException("Start vertex not found");

		LinkedList q = new LinkedList();
		q.addLast(start);
		start.dist = 0;

		while (!q.isEmpty()) {
			Vertex v = (Vertex) q.removeFirst();
			Iterator itr = v.adj.iterator();
			if (itr.hasNext() || v.name.equals("root"))
				System.out.print(v.name);

			int adjCount = 0;
			while (itr != null) {
				if (itr.hasNext()) {
					adjCount++;
					Edge e = (Edge) itr.next();
					Vertex w = e.dest;

					if (adjCount == 1)
						System.out.println(" -----" + e.edge + "-------->"
								+ w.name);
					else
						System.out.println("   -----" + e.edge + "-------->"
								+ w.name);
					if (w.dist == INFINITY) {
						w.dist = v.dist + 1;
						w.prev = v;
						q.addLast(w);
					}
				} else
					itr = null;
			}

		}
	}

	public void preOrderProc(String nodeName) {
		Vertex node = (Vertex) vertexMap.get(nodeName);
		if (node == null)
			throw new NoSuchElementException("Start vertex not found");

		if (node.visited == true)
			return;
		node.visited = true;
		vertexMap.remove(nodeName);
		vertexMap.put(nodeName, node);

		Iterator itr = node.adj.iterator();
		while (itr.hasNext()) {
			Edge e = (Edge) itr.next();
			Vertex to = e.dest;
			if (to.visited == false) {
				if(node.name!="root")
					System.out.println(node.name + "------" + e.edge + "-------->"+ to.name);
				preOrderProc(to.name);

			}
		}

	}

	public boolean contains(String vrtx) {
		if (vertexMap.containsKey(vrtx))
			return true;
		return false;
	}

}