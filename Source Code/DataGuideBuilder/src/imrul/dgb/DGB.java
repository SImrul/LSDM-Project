package imrul.dgb;

import java.util.HashMap;

public class DGB {

	public static NodeElement BuildGuideGraph(String[] conditions) {
		NodeElement root = new NodeElement(null, NodeType.VARIABLE, "1");
		HashMap<String, NodeElement> visitedVarMap = new HashMap<String, NodeElement>();
		NodeElement parent = root;
		for (String statement : conditions) {
			NodeElement subject, object;
			String terms[] = statement.split(" ");
			NodeType type;
			// Subject is Variable/URI?
			type = (terms[0].startsWith("?"))? NodeType.VARIABLE : NodeType.CONSTANT;
			if(visitedVarMap.containsKey(terms[0])) { subject = visitedVarMap.get(terms[0]); }
			else {
				String dIndex = parent.getDeweyIndex() + "." + (parent.getChildern().size() + 1);
				subject = new NodeElement(parent, type, dIndex);
				visitedVarMap.put(terms[0], subject);
				parent.addChild(subject);
			}
			//Process Object + predicate
			type = (terms[2].startsWith("?"))? NodeType.VARIABLE : NodeType.CONSTANT;
			// If a object is already entered in variableMap, then it means the object was 
			// a subject previously. So, have to cut that sub-tree and put it under current subject.
			if(visitedVarMap.containsKey(terms[2])) { 
				object = TransferSubTree(subject, visitedVarMap.get(terms[2])); 
			}
			else {
				String dIndex = subject.getDeweyIndex() + "." + (subject.getChildern().size() + 1);
				object = new NodeElement(subject, type, dIndex);
				visitedVarMap.put(terms[2], object);
				subject.addChild(object);
			}
			object.setPredicate(terms[1]);
			
		}
		return root;
	}
	
	private static NodeElement TransferSubTree(NodeElement newParent, NodeElement oldChild){
		//Note: Should we create a new object or use the old child by updating it's properties.
		NodeElement oldParent = oldChild.getParent();
		// Cut the link between old parent and the child (oldChild)
		// Remove will update the dIndex of following children and subtree.
		oldParent.removeChild(oldChild);

		oldChild.setParent(newParent);
		newParent.addChild(oldChild);
		// Issue an update event for updated Child and it's sub tree.
		oldChild.update();
		return oldChild;
	}
	
	public static void main(String[] args) {
		String q1[] = new String[] {"<http://purl.uniprot.org/citations/7934828> <http://purl.uniprot.org/core/author> ?a"};
		String q2[] = new String[] {"<http://purl.uniprot.org/uniprot/Q6GZX4> ?p ?o"};
		String q3[] = new String[] {"?x <http://purl.uniprot.org/core/name> \"Virology\"", "?x <http://purl.uniprot.org/core/volume> ?y"};
		String q4[] = new String[] {"?x <http://purl.uniprot.org/core/name> ?y", "?x <http://purl.uniprot.org/core/volume> ?z", 
				"?x <http://purl.uniprot.org/core/pages> \"176-186\"" };
		String q5[] = new String[] { "?x <http://purl.uniprot.org/core/name> \"Science\"",
				"?x <http://purl.uniprot.org/core/author> ?y", "?z <http://purl.uniprot.org/core/citation> ?x" 		};
		String q6[] = new String[] {"?x ?y \"Israni S.\"", "<http://purl.uniprot.org/citations/15372022> ?y \"Gomez M.\""};
		String q7[] = new String[] {"?x ?y <http://purl.uniprot.org/citations/15165820>", "?a ?b ?y"};
		String q8[] = new String[] { "?x <http://purl.uniprot.org/core/reviewed> ?y", 
				"?x <http://purl.uniprot.org/core/created> ?b",
				"?x <http://purl.uniprot.org/core/mnemonic> \"003L_IIV3\"", 
				"?x <http://purl.uniprot.org/core/citation> ?z",
				"?z <http://purl.uniprot.org/core/author> ?a" 				};
		NodeElement root = BuildGuideGraph(q5);
	}
}
