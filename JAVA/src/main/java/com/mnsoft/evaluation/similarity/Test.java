package com.mnsoft.evaluation.similarity;

import info.debatty.java.stringsimilarity.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotoh;

public class Test {
	
	private final static String str1 = "TACGGGCCCGCTAC";
	private final static String str2 = "TAGCCCTATCGGTCA dsgfd";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JaroWinkler 		jw 	= new JaroWinkler();
		SmithWatermanGotoh 	swg = new SmithWatermanGotoh();
		SmithWaterman		sw	= new SmithWaterman();
		Levenshtein 		ll 	= new Levenshtein();
		
		double score1 = jw.similarity(str1, str2);
		double score2 = swg.getSimilarity(str1, str2);
		double score3 = ll.getSimilarity(str1, str2);
		double score4 = sw.getSimilarity(str1, str2);
		
		System.out.println("jw: " + score1 + ",sw: " + score4 +  ", swg: " + score2 + ", ll: " + score3);
		System.out.println("jw: " + (score1*0.3) + ", swg: " + (score2*0.4) + ", ll: " + (score3*0.3));
		System.out.println("total: " + (score1) * 0.1);
	}

}
