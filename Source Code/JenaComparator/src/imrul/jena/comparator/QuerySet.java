package imrul.jena.comparator;

public final class QuerySet {

	public static final String Q1 = "select ?a where {  <http://purl.uniprot.org/citations/7934828> <http://purl.uniprot.org/core/author> ?a . }";
	public static final String Q2 = "select ?p ?o where { <http://purl.uniprot.org/uniprot/Q6GZX4> ?p ?o . }";
	public static final String Q3 = "select ?x ?y where " +
			"{	?x <http://purl.uniprot.org/core/name> \"Virology\" . " +
			"	?x <http://purl.uniprot.org/core/volume> ?y . " +
			"}";
	public static final String Q4 = "select ?x ?z where " +
			"{  ?x <http://purl.uniprot.org/core/name> ?y ." +
			" 	?x <http://purl.uniprot.org/core/volume> ?z ." +
			" 	?x <http://purl.uniprot.org/core/pages> \"176-186\" . " +
			"}";
	public static final String Q5 = "select ?x ?y ?z where " +
			"{  ?x <http://purl.uniprot.org/core/name> \"Science\" . " +
			"	?x <http://purl.uniprot.org/core/author> ?y .  " +
			"	?z <http://purl.uniprot.org/core/citation> ?x . " +
			"}";
	public static final String Q6 = "select ?x ?y where " +
			"{  ?x ?y \"Israni S.\" .  " +
			"	<http://purl.uniprot.org/citations/15372022> ?y \"Gomez M.\" . " +
			"}";
	public static final String Q7 = "select ?a ?b where " +
			"{  ?x ?y <http://purl.uniprot.org/citations/15165820> . " +
			" 	?a ?b ?y . } ";
	public static final String Q8 = "select ?x ?z ?a where " +
			"{  ?x <http://purl.uniprot.org/core/reviewed> ?y . " +
			"	?x <http://purl.uniprot.org/core/created> ?b .  " +
			"	?x <http://purl.uniprot.org/core/mnemonic> \"003L_IIV3\" ." +
			"	?x <http://purl.uniprot.org/core/citation> ?z .  " +
			"	?z <http://purl.uniprot.org/core/author> ?a . " +
			"} ";
	
}
