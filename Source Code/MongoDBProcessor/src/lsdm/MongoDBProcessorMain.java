package lsdm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class MongoDBProcessorMain {
	
	public static void main(String[] args) throws IOException {
		//RDFtoMongo builder = new RDFtoMongo();
		//builder.buildDB();
		String queryString = "";
		int queryNumber=8;
		try {
			String workingdirectory=System.getProperty("user.dir"); 
			String file = workingdirectory+"/queries/q"+queryNumber+".txt";
			queryString = new Scanner(new File(file), "UTF-8").useDelimiter("\\A").next();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		QueryProcessor qp = new QueryProcessor(queryString,queryNumber);
		//System.out.println(java.lang.Runtime.getRuntime().maxMemory());
		//129957888


	}
}
