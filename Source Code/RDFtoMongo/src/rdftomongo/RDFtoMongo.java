import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.io.*;

public class RDFtoMongo {
    public static void main(String[] args) {
        try
        {
        //MongoClient mc= new MongoClient();
        MongoClient mc=new MongoClient(Arrays.asList(new ServerAddress("localhost",27017)));
        DB db=mc.getDB("suresh");
        Set<String> sc=db.getCollectionNames();
        DBCollection c=db.getCollection("col");
        System.out.println(sc);
        mc.setWriteConcern(WriteConcern.JOURNALED);
        File f=new File("C:\\Users\\seshu\\Documents\\LargeScale Project\\uniprot.n3");
        InputStream is=new FileInputStream(f);
        BufferedReader br=new BufferedReader(new InputStreamReader(is)); 
        String l;
        while((l=br.readLine())!=null)
                {
                 System.out.println(l)   ;
                 String[] q=l.split(" ");
                 q[2]=q[2].replace("\"", "");
                 int x=q.length;
                 if(x>4)
                 {
                  String s="";
                  for(int i=2;i<x-1;i++)  
                      s=s+q[i]+" ";
                  s=s.replace("\"", "");
                  BasicDBObject bob=new BasicDBObject("subject",q[0]).append("property", q[1]).append("object", s);
                  c.insert(bob);
                 }
                 else
                 {
                 BasicDBObject bob=new BasicDBObject("subject",q[0]).append("property", q[1]).append("object", q[2]);
                 c.insert(bob);
                 }
                }
        is.close();
        mc.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        } 
    }
    
}
