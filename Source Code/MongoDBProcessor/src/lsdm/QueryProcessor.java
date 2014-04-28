package lsdm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import dnl.utils.text.table.TextTable;

public class QueryProcessor {
	private Map elements;
	private Graph guideGraph;
	private Query query;

	public QueryProcessor(String queryStr, int queryNumber) {
		
		long start = new Date().getTime();
		String time = "Started: "+getTime(start)+"\n";
		
		elements = new HashMap();
		buildGuideGraph(queryStr);
		guideGraph.clearAll();
		System.out.println("==================  Plan Processing  ==================");
		processQueryPlan("root");
		System.out.println("=======================================================\n");
		int numRows = printStats();
		long end = new Date().getTime();

    	time += "Ended: "+getTime(end)+"\n";
    	String duration =  getTimeString(end-start);
    	time += "Duration: "+duration+"\n";
    	printOutput(true,numRows,queryNumber,time);
    	System.out.println( time);
	}
	private String getTime(long millis){
		Date date=new Date(millis);
	    return date.toString();
	}

	//http://stackoverflow.com/questions/7049009/getting-millisecond-format
	private String getTimeString(long millis) {
	    int minutes = (int) (millis / (1000 * 60));
	    int seconds = (int) ((millis / 1000) % 60);
	    int milliseconds = (int) (millis % 1000);
	    return String.format("%d:%02d.%03d", minutes, seconds, milliseconds);
	}
	private void processQueryPlan(String nodeName) {
		Vertex node = (Vertex) guideGraph.vertexMap.get(nodeName);
		if (node == null)
			throw new NoSuchElementException("Start vertex not found");

		if (node.visited == true)
			return;
		node.visited = true;
		if(nodeName != "root"){
			guideGraph.vertexMap.remove(nodeName);
			guideGraph.vertexMap.put(nodeName, node);
		}

		Iterator itr = node.adj.iterator();
		itr = sortByValue(itr);

		while (itr.hasNext()) {
			Edge e = (Edge) itr.next();
			Vertex to = e.dest;
			if (to.visited == false) {
				if(nodeName!="root"){
				System.out.println("Processing:");
				System.out.println(node.name + "------" + e.edge + "-------->"+ to.name);
				String sub = node.name;
				String pred = e.edge;
				String obj = to.name;
				processSparqlQuery(sub, pred, obj);
				}
				processQueryPlan(to.name);
			}
		}
	}

	private Iterator sortByValue(Iterator itr) {
		if (!itr.hasNext())
			return itr;
		LinkedList sortedList = new LinkedList();
		while (itr.hasNext()) {
			Edge e = (Edge) itr.next();
			if (e.dest.name.startsWith("?"))
				sortedList.addLast(e);
			else
				sortedList.addFirst(e);
		}
		return sortedList.iterator();
	}

	private int printStats() {
		Set keys = elements.keySet();
		Iterator itr = keys.iterator();
		int numRows = -1;
		System.out.println("=======================  Stats  =======================");
		while (itr.hasNext()) {
			String key = itr.next().toString();
			ArrayList<String> list = (ArrayList<String>) elements.get(key);
			System.out.println(key + ": " + list.size());
			if(numRows< list.size())
				 numRows=list.size();
		}
		System.out.println("=======================================================");
		return numRows;
	}

	private void processSparqlQuery(String sub, String pred, String obj) {

		List<Object> matches = null;
		List<BasicDBObject> queries = null;
		JSONParser parser = new JSONParser();
		queries = translateQueryPattern(sub, pred, obj);// get queries for this
														// pattern
		matches = executeQuery(queries);

		System.out.println("Number of Matches: " + matches.size());

		// Store Matches
		ArrayList<String> subMatchList = new ArrayList<String>();
		ArrayList<String> predMatchList = new ArrayList<String>();
		ArrayList<String> objMatchList = new ArrayList<String>();
		JSONObject jsonObject = null;
		for (int m = 0; m < matches.size(); m++) {
			try {
				jsonObject = (JSONObject) parser.parse(matches.get(m).toString());
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

			subMatchList.add(jsonObject.get("subject").toString());
			predMatchList.add(jsonObject.get("property").toString());
			objMatchList.add(jsonObject.get("object").toString());
		}

		// store value if isVar==true
		if (sub.startsWith("?"))
			storeMatches(sub, subMatchList);
		if (pred.startsWith("?"))
			storeMatches(pred, predMatchList);
		if (obj.startsWith("?"))
			storeMatches(obj, objMatchList);
	}

	private List<BasicDBObject> translateQueryPattern(String sub, String pred,
			String obj) {
		Boolean[] vars = areVariabls(sub, pred, obj);
		List<BasicDBObject> queries = new ArrayList<BasicDBObject>();
		if (vars[0] == false) {
			BasicDBObject query = new BasicDBObject();
			query.put("subject", sub);
			queries.add(query);
		} else if (elements.containsKey(sub)) {
			ArrayList<String> subs = (ArrayList<String>) elements.get(sub);
			for (int q = 0; q < subs.size(); q++) {
				BasicDBObject query = new BasicDBObject();
				query.put("subject", subs.get(q));
				queries.add(query);
			}
		}
		if (vars[1] == false) {
			if (queries.size() == 0) {
				BasicDBObject query = new BasicDBObject();
				query.put("property", pred);
				queries.add(query);
			} else {
				List<BasicDBObject> newQueries = new ArrayList<BasicDBObject>();
				for (int q = 0; q < queries.size(); q++) {
					BasicDBObject query = queries.get(q);
					query.put("property", pred);
					newQueries.add(query);
				}
				if (newQueries.size() > 0)
					queries = newQueries;
			}
		} else if (elements.containsKey(pred)) {
			ArrayList<String> preds =  (ArrayList<String>) elements.get(pred);
			if (queries.size() == 0) {
				for (int q = 0; q < preds.size(); q++) {
					BasicDBObject query = new BasicDBObject();
					query.put("property", preds.get(q));
					queries.add(query);
				}
			} else {
				List<BasicDBObject> newQueries = new ArrayList<BasicDBObject>();
				for (int q = 0; q < queries.size(); q++) {
					BasicDBObject query = queries.get(q);
					query.put("property", preds.get(q));
					newQueries.add(query);
				}
				if (newQueries.size() > 0)
					queries = newQueries;
			}

		}
		if (vars[2] == false) {
			if (queries.size() == 0) {
				BasicDBObject query = new BasicDBObject();
				query.put("object", obj);
				queries.add(query);
			} else {
				List<BasicDBObject> newQueries = new ArrayList<BasicDBObject>();
				for (int q = 0; q < queries.size(); q++) {
					BasicDBObject query = queries.get(q);
					query.put("object", obj);
					newQueries.add(query);
				}
				if (newQueries.size() > 0)
					queries = newQueries;
			}
		} else if (elements.containsKey(obj)) {
			ArrayList<String> objs = (ArrayList<String>) elements.get(obj);
			if (queries.size() == 0) {
				for (int q = 0; q < objs.size(); q++) {
					BasicDBObject query = new BasicDBObject();
					query.put("object", objs.get(q));
					queries.add(query);
				}
			} else {
				List<BasicDBObject> newQueries = new ArrayList<BasicDBObject>();
				for (int q = 0; q < queries.size(); q++) {
					BasicDBObject query = queries.get(q);
					query.put("object", objs.get(q));
					newQueries.add(query);
				}
				if (newQueries.size() > 0)
					queries = newQueries;
			}

		}
		return queries;
	}

	private void printOutput(boolean lineNumbers, int numRows, int queryNumber, String timing) {

		String[] outputVars = toArray(query.getResultVars());
		Object[][] data = new Object[numRows][outputVars.length];
		String[][] columns = getColumns(numRows, outputVars);
		for (int k = 0; k < numRows; k++) {
			Object[] row = new Object[outputVars.length];
			for (int j = 0; j < outputVars.length; j++) {
				row[j] = columns[k][j];
			}
			data[k] = row;
		}

		
		outputVars = formatRVar(outputVars);
		TextTable tt = new TextTable(outputVars, data);
		tt.setSort(0);
		tt.setAddRowNumbering(lineNumbers);
		//System.out.println("\nOUTPUT:\n");
		PrintStream ps = null;
		try {
			ps = new PrintStream(new File ("output/q"+queryNumber+"_output.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		int indent = 0;
		try {
			ps.write(timing.getBytes(Charset.forName("UTF-8")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tt.printTable(ps, indent );
	}

	private String[] formatRVar(String[] outputVars) {
		for (int v = 0; v < outputVars.length; v++) {
			outputVars[v] = outputVars[v].substring(1);
		}
		return outputVars;
	}

	private String[][] getColumns(int rowcount, String[] outputVars) {
		int numCols = outputVars.length;
		String[][] cols = new String[rowcount][numCols];
		ArrayList<String> col;
		for (int c = 0; c < numCols; c++) {
			col = (ArrayList<String>) elements.get(outputVars[c]);
			for (int r = 0; r < rowcount; r++) {
				String val = col.get(r);
				if (val.startsWith("<") == false)
					cols[r][c] = "\"" + val + "\"";
				else
					cols[r][c] = val;
			}
		}
		return cols;
	}

	private String[] toArray(List<String> resultVars) {
		String[] vars = new String[resultVars.size()];
		for (int v = 0; v < vars.length; v++)
			vars[v] = "?" + resultVars.get(v);

		return vars;
	}

	private void storeMatches(String e, ArrayList<String> valueList) {
		//System.out.println("Proc: " + e);
		if (elements.containsKey(e) == false) {// if new element
			elements.put(e,valueList);
		}
	}

	private void reduce(int i) {
		Iterator k = elements.keySet().iterator();
		List keys = new ArrayList();
		while (k.hasNext()) {
			keys.add(k.next());
		}
		for (int p = 0; p < keys.size(); p++) {
			String key = keys.get(p).toString();
			removeEle(key,i);

		}

	}

	private void expand(int i, int amount) {
		Iterator k = elements.keySet().iterator();
		List keys = new ArrayList();
		while (k.hasNext()) 
			keys.add(k.next());
		int numkeys =keys.size();
		for (int p = 0; p <numkeys ; p++) {
			String key = keys.get(p).toString();
			duplicateEle(key,i,amount);
		}
	}
	private void removeEle(String eleKey,int index) {
		ArrayList<String> eleList = (ArrayList<String>) elements.get(eleKey);
		eleList.remove(index);
		elements.remove(eleKey);
		elements.put(eleKey, eleList);
		
	}

	private void duplicateEle(String eleKey,int index, int amount){
		ArrayList<String> eleList = (ArrayList<String>) elements.get(eleKey);
		String ele = eleList.get(index);
		for(int i=0; i<amount;i++)
			eleList.add(index, ele);
		elements.remove(eleKey);
		elements.put(eleKey, eleList);
		
	}

	private List<Object> executeQuery(List<BasicDBObject> queries) {
		List<Object> docs = new ArrayList<Object>();
		// To connect to mongodb server
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient("localhost", 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		// Now connect to your databases
		DB db = mongoClient.getDB("ntdocs");
		// System.out.println("Connected to: " + db.getName());
		DBCollection col = db.getCollection("col");
		//System.out.println("Issuing " + queries.size() + " queries..");
		int eleIndex = 0;
		int numQueries = queries.size();
		for (int k = 0; k <numQueries ; k++) {
			//System.out.println("Query: "+k);
			// System.out.println(queries.get(k).toString());
			DBCursor matches = col.find(queries.get(k));
			int numMatches = matches.count();
			while (matches.hasNext())
				docs.add(matches.next());
			matches.close();
			//System.out.println("Q Match: "+numMatches);
			if(numMatches==1)
				eleIndex++;	
			else if (numMatches > 1) {
				if (elements.size() >0) {
					expand(eleIndex,numMatches-1);
					eleIndex+=numMatches;
				}
			}
			else if (numMatches < 1) {
				//System.out.println("Shrinking PathArray..");
				reduce(eleIndex);
			}

			// System.out.println("Number of docs: "+docs.size());
		}
		return docs;

	}

	private Boolean[] areVariabls(String e1, String e2, String e3) {
		Boolean[] state = new Boolean[3];
		if (e1.startsWith("?"))
			state[0] = true;
		else
			state[0] = false;
		if (e2.startsWith("?"))
			state[1] = true;
		else
			state[1] = false;
		if (e3.startsWith("?"))
			state[2] = true;
		else
			state[2] = false;

		return state;
	}

	// build query plan
	private void buildGuideGraph(String queryStr) {

		// SPARQL Parsing
		query = QueryFactory.create(queryStr);
		String[][] qpMat = new QueryPatternMatrix(query).getQPMatrix();

		// Query Planning
		guideGraph = new Graph();
		System.out
				.println("\n\n~~~~~~~~~~~~~~~~~~~~~~ Guide Graph ~~~~~~~~~~~~~~~~~~~~~~");
		for (int k = 0; k < qpMat.length; k++) {
			String sub = qpMat[k][0];
			String pred = qpMat[k][1];
			String obj = qpMat[k][2];

			if (guideGraph.contains(sub) == false) {
				guideGraph.addEdge("root", sub, 0, "ggLink");
				guideGraph.addEdge(sub, obj, 0, pred);
				guideGraph.getVertex(sub, false).mark();

				Vertex v = guideGraph.getVertex(obj, false);
				if (v != null) {
					if (v.scratch == 1) {
						guideGraph.removeEdge("root", obj, 0, "ggLink");
					}
				}
			} else {
				guideGraph.addEdge(sub, obj, 0, pred);
				Vertex v = guideGraph.getVertex(obj, false);
				if (v != null) {
					if (v.scratch == 1) {
						guideGraph.removeEdge("root", obj, 0, "ggLink");
					}
				}

			}
		}
		// return first element
		Vertex root = guideGraph.getVertex("root", false);
		Edge first = (Edge) root.getNeighbor();
		guideGraph.unweighted("root");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~ Query Plan ~~~~~~~~~~~~~~~~~~~~~~~");
		guideGraph.clearAll();
		guideGraph.preOrderProc("root");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
	}
}
