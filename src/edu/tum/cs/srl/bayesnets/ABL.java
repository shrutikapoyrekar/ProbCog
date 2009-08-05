package edu.tum.cs.srl.bayesnets;

import java.io.PrintStream;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.tum.cs.srl.Database;
import edu.tum.cs.srl.RelationKey;
import edu.tum.cs.srl.bayesnets.learning.CPTLearner;
import edu.tum.cs.srl.bayesnets.learning.DomainLearner;

public class ABL extends BLOGModel {
	
	public ABL(String[] blogFiles, String networkFile) throws Exception {
		super(blogFiles, networkFile);
		
		// read functional dependencies among relation arguments
		Pattern pat = Pattern.compile("RelationKey\\s+(\\w+)\\s*\\((.*)\\)");
		Matcher matcher = pat.matcher(this.blogContents);
		while(matcher.find()) {
			String relation = matcher.group(1);
			String[] arguments = matcher.group(2).trim().split("\\s*,\\s*");
			addRelationKey(new RelationKey(relation, arguments));
		}	
	}
	
	public ABL(String blogFile, String networkFile) throws Exception {
		this(new String[]{blogFile}, networkFile);
	}
	
	@Override
	protected void writeDeclarations(PrintStream out) {
		super.writeDeclarations(out);
		
		// write relation keys
		for(Collection<RelationKey> ckey : this.relationKeys.values()) {
			for(RelationKey key : ckey) {
				out.println("RelationKey " + key.toString());
			}
		}
		out.println();
	}
	
	public static void main(String[] args) {
		try {
			String bifFile = "abl/kitchen-places/actseq.xml";
			ABL bn = new ABL(new String[]{"abl/kitchen-places/actseq.abl"}, bifFile);
			String dbFile = "abl/kitchen-places/train.blogdb";
			// read the training database
			System.out.println("Reading data...");
			Database db = new Database(bn);
			db.readBLOGDB(dbFile);		
			System.out.println("  " + db.getEntries().size() + " variables read.");
			// learn domains
			if(true) {
				System.out.println("Learning domains...");
				DomainLearner domLearner = new DomainLearner(bn);
				domLearner.learn(db);
				domLearner.finish();
			}
			// learn parameters
			System.out.println("Learning parameters...");
			CPTLearner cptLearner = new CPTLearner(bn);
			cptLearner.learnTyped(db, true, true);
			cptLearner.finish();	
			System.out.println("Writing XML-BIF output...");
			bn.saveXMLBIF(bifFile);
			if(true) {
				System.out.println("Showing Bayesian network...");
				bn.show();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
