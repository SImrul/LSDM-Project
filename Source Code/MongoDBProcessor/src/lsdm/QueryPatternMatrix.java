package lsdm;

import com.hp.hpl.jena.query.Query;

public class QueryPatternMatrix {

	private String[][] eleArr;
	

	public String[][] getQPMatrix() {
		return eleArr;
	}


	public QueryPatternMatrix(Query query) {
		String qp = query.getQueryPattern().toString().replaceAll("[{}]", "")
				.trim();
		qp+=" .";
		System.out.println("~~~~~~ Query Patterns ~~~~~");
		System.out.println(qp);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		String[] qArr = qp.split("\n");
		int numQP = qArr.length;
		System.out.println("Creating a "+ numQP + "x3 Elements Matrix...");
		eleArr = new String[numQP][3];

		for (int k = 0; k < numQP; k++) {
			String[] q = qArr[k].trim().split(" ");
			q[2] = q[2].replace("\"", "");
			int numSplits = q.length;
			if (numSplits > 4) {
				String s = "";
				for (int i = 2; i < numSplits - 1; i++)
					s = s + q[i] + " ";
				s = s.replace("\"", "");
				eleArr[k][0]=q[0].trim();
				eleArr[k][1]=q[1].trim();
				eleArr[k][2]=s.trim();
				

			} else {
				
				eleArr[k][0]=q[0].trim();
				eleArr[k][1]=q[1].trim();
				eleArr[k][2]=q[2].trim();
			}
			//System.out.println(eleArr[k][0]+" "+eleArr[k][1]+" "+eleArr[k][2]);
		}

	}

}
