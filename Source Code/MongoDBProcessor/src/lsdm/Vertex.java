//source: http://www.cs.fiu.edu/~weiss/dsj2/code/Graph.java
package lsdm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.DBCursor;

//Represents a vertex in the graph.
class Vertex {
	public String name; // Vertex name
	public List adj; // Adjacent vertices
	public double dist; // Cost
	public Vertex prev; // Previous vertex on shortest path
	public int scratch;// Extra variable used in algorithm
	public Boolean isVar;
	public boolean visited;// Extra variable used in algorithm
	public boolean expand;

	public Vertex(String nm) {
		name = nm;
		visited=false;
		adj = new LinkedList();
		expand=false;
		reset();
		if (nm.startsWith("?"))
			isVar = true;
		else
			isVar = false;

	}

	public Object getNeighbor() {
		return adj.get(0);
	}

	public void reset() {
		dist = Graph.INFINITY;
		prev = null;
		scratch = 0;
		visited=false;
	}

	public void mark() {
		scratch = 1;
	}

}