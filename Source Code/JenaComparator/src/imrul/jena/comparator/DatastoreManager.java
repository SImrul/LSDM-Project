package imrul.jena.comparator;

import java.awt.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

import com.hp.hpl.jena.query.* ;

//TODO: use global URL for dataset, current one is relative to workspace.
public class DatastoreManager {
	
	private static String url = "file:uniprot.nt/uniprot.n3";
	private Dataset dataset = null;
	
	public DatastoreManager() {
		System.out.println("Loading dataset...");
		LoadDataset();
		System.out.println("Dataset loaded successfully.");
	}
	
	private void LoadDataset() {
		//String dftGraphURI = "file:default-graph.ttl" ;
		try {
			String dftGraphURI = url;
			this.dataset = DatasetFactory.create(dftGraphURI);
		}catch(Exception e) {
			System.out.println("Error occured while loading dataset. Error message: " + e.getMessage());
		}		
	}
	
	private void ExecuteQuery(String queryString) {
		this.ExecuteQuery(queryString, System.out);		
	}
	
	private void ExecuteQuery(String queryString, OutputStream out) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qExec = QueryExecutionFactory.create(query, dataset);
		try {
			   ResultSet results = qExec.execSelect() ;
			   ResultSetFormatter.out(out, results);
		} finally { qExec.close() ; }
	}
	
	public static void main(String[] args) throws Exception{
		DatastoreManager dm = new DatastoreManager();
		File context = new File("results");
		if(!context.exists()){ 
			context.mkdirs(); 
		}
		
		String[] querySet = new String[]{QuerySet.Q1, QuerySet.Q2, QuerySet.Q3, 
			QuerySet.Q4, QuerySet.Q5, QuerySet.Q6, QuerySet.Q7, QuerySet.Q8 } 
		;
		for(int i= 1; i <= querySet.length; i++) {
			File output = new File("results/Q" + i + ".txt");
			output.createNewFile();
			FileOutputStream fos = new FileOutputStream(output);
			String queryString = querySet[i-1];
			dm.ExecuteQuery(queryString, fos);
		}
		
	}
}
