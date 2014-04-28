package lsdm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class RDFtoMongo {
	public void buildDB() {
		try {
			// set up connection to mongodb server
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			DB db = mongoClient.getDB("ntdocs");
			DBCollection col = db.getCollection("col");

			// set WC to normal since all operations are performed on localhost
			mongoClient.setWriteConcern(WriteConcern.NORMAL);

			// read nt file
			File f = new File("uniprot.n3");
			InputStream is = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String l;

			// read line (ntriple)
			while ((l = br.readLine()) != null) {
					// store nt as a single document
					String[] q = l.split(" ");
					q[2] = q[2].replace("\"", "");
					int x = q.length;
					if (x > 4) {
						String s = "";
						for (int i = 2; i < x - 1; i++)
							s = s + q[i] + " ";
						s = s.replace("\"", "");
						BasicDBObject bob = new BasicDBObject("subject", q[0])
								.append("property", q[1]).append("object",
										s.trim());
						//check if it exists
						//DBCursor match = col.find(bob);
						//if(match.hasNext()==false)
							col.insert(bob);
					} else {
						BasicDBObject bob = new BasicDBObject("subject", q[0])
								.append("property", q[1])
								.append("object", q[2]);
						//check if it exists
						//DBCursor match = col.find(bob);
						//if(match.hasNext()==false)
							col.insert(bob);
					}
				}
			is.close();
			mongoClient.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}