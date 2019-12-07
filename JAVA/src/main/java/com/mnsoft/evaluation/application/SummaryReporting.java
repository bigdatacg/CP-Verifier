package com.mnsoft.evaluation.application;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mnsoft.mcp.util.FileUtil;

public class SummaryReporting {
	
	public final static String KEYWORD				= "subway";
	public final static String REPORT_DEST_PATH 	= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + "-report.json";

	private final static double POLICY_SOC_PASS = 0.65;
	private final static double POLICY_AOC_PASS = 0.70;
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		JSONArray 	reportList 		= FileUtil.readJSONFile(new File(REPORT_DEST_PATH));
		JSONObject	report			= null;
		JSONArray	hereReport		= null;
		JSONArray	tomtomReport	= null;
		JSONObject	tmp				= null;
		JSONArray	tmpArr			= null;
		double		soc				= 0.0;
		double		aoc				= 0.0;
		int			iCount			= 0;
		double		iSOCTotal		= 0;
		double		iAOCTotal		= 0;
		int			referenceCount	= 0;
		int			loopSize		= 0;
		
		boolean		isSOCPass		= false;
		
		System.out.println("keyword, city, google, here, , , , , , , , , , tomtom, , , , , , , , , ");
		System.out.println("keyword, city, google, size, target, filtered, corrects, referencing, coverage, soc-avg, soc-total, aoc-avg, aoc-total, "
												+ "size, target, filtered, corrects, referencing, coverage, soc-avg, soc-total, aoc-avg, aoc-total");
		
		for(int i=0; i<reportList.size(); i++) {
			report = (JSONObject)reportList.get(i);
			
			hereReport		= (JSONArray)((JSONObject)report.get("here")).get("report");
			tomtomReport	= (JSONArray)((JSONObject)report.get("tomtom")).get("report");
			tmp				= null;
			tmpArr			= null;
			soc				= 0.0;
			aoc				= 0.0;
			iCount			= 0;
			iSOCTotal		= 0;
			iAOCTotal		= 0;
			referenceCount 	= 0;
			loopSize		= hereReport == null ? 0 : hereReport.size();
			
			for(int j=0; j<loopSize; j++) {
				
				isSOCPass 	= false;
				tmp 		= (JSONObject)hereReport.get(j);
				tmpArr		= (JSONArray)tmp.get("references");
				for(int k=0; k<tmpArr.size(); k++, referenceCount++) {
					tmp 	= (JSONObject)tmpArr.get(k);
					soc 	= (Double)((JSONObject)tmp.get("soc")).get("total");
					if(soc > POLICY_SOC_PASS) {
						isSOCPass = true;
					}
					iSOCTotal += soc;
				}
				
				tmp 		= (JSONObject)hereReport.get(j);
				aoc 		= (Double)((JSONObject)tmp.get("aoc")).get("total");
				iAOCTotal 	+= aoc;
				if(aoc > POLICY_AOC_PASS && isSOCPass == true) {
					iCount++;
				}
				
			}
			((JSONObject)report.get("here")).put("soc-total", 	(float)iSOCTotal);
			((JSONObject)report.get("here")).put("soc-avg", 	(referenceCount == 0 ? 0 : ((float)iSOCTotal / (float)referenceCount)));
			((JSONObject)report.get("here")).put("aoc-total", 	(float)iAOCTotal);
			((JSONObject)report.get("here")).put("aoc-avg", 	(referenceCount == 0 ? 0 : ((float)iAOCTotal / (float)referenceCount)));
			((JSONObject)report.get("here")).put("references", 	referenceCount);
			((JSONObject)report.get("here")).put("target", 		hereReport.size());
			((JSONObject)report.get("here")).put("correct", 	iCount);
			
			/* SOC-REPORTING(TOMTOM) */
			tmp				= null;
			tmpArr			= null;
			soc				= 0.0;
			aoc				= 0.0;
			iCount			= 0;
			iSOCTotal		= 0;
			iAOCTotal		= 0;
			referenceCount 	= 0;
			
			loopSize		= tomtomReport == null ? 0 : tomtomReport.size();
			
			for(int j=0; j<loopSize; j++, referenceCount++) {
				
				isSOCPass 	= false;
				tmp 		= (JSONObject)tomtomReport.get(j);
				tmpArr		= (JSONArray)tmp.get("references");
				for(int k=0; k<tmpArr.size(); k++) {
					tmp 	= (JSONObject)tmpArr.get(k);
					soc 	= (Double)((JSONObject)tmp.get("soc")).get("total");
					if(soc > POLICY_SOC_PASS) {
						isSOCPass = true;
					}
					iSOCTotal += soc;
					
				}
				
				tmp 		= (JSONObject)tomtomReport.get(j);
				aoc 		= (Double)((JSONObject)tmp.get("aoc")).get("total");
				iAOCTotal 	+= aoc;
				if(aoc > POLICY_AOC_PASS && isSOCPass == true) {
					iCount++;
				}
			}
			((JSONObject)report.get("tomtom")).put("soc-total", 	(float)iSOCTotal);
			((JSONObject)report.get("tomtom")).put("soc-avg",		(referenceCount == 0 ? 0 : ((float)iSOCTotal / (float)referenceCount)));
			((JSONObject)report.get("tomtom")).put("aoc-total", 	(float)iAOCTotal);
			((JSONObject)report.get("tomtom")).put("aoc-avg", 		(referenceCount == 0 ? 0 : ((float)iAOCTotal / (float)referenceCount)));
			((JSONObject)report.get("tomtom")).put("references", 	referenceCount);
			((JSONObject)report.get("tomtom")).put("target", 		tomtomReport.size());
			((JSONObject)report.get("tomtom")).put("correct", 		iCount);
			
			printReport(report);
		}
	}
	public static void printReport(JSONObject report) {
		String 	keyword				= (String)((JSONObject)report.get("query")).get("keyword");
		String	description			= (String)((JSONObject)report.get("query")).get("description");
		long	googleSize			= (Long)report.get("google");
		
		long 	hereSize 			= (Long)((JSONObject)report.get("here")).get("size");
		long	hereCorrect 		= (Integer)((JSONObject)report.get("here")).get("correct");
		long	hereReferences		= (Integer)((JSONObject)report.get("here")).get("references");
		long	hereTarget			= (Integer)((JSONObject)report.get("here")).get("target");
		long	hereDistFailure		= hereSize - hereTarget; 
		double	hereCoverage		= ((float)hereCorrect / (float)googleSize); 
		double	hereSOCAVG			= (Float)((JSONObject)report.get("here")).get("soc-avg");
		double	hereSOCTotal		= (Float)((JSONObject)report.get("here")).get("soc-total");
		double	hereAOCAVG			= (Float)((JSONObject)report.get("here")).get("aoc-avg");
		double	hereAOCTotal		= (Float)((JSONObject)report.get("here")).get("aoc-total");
		
		long 	tomtomSize 			= (Long)((JSONObject)report.get("tomtom")).get("size");
		long 	tomtomCorrect 		= (Integer)((JSONObject)report.get("tomtom")).get("correct");
		long	tomtomReferences	= (Integer)((JSONObject)report.get("tomtom")).get("references");
		long	tomtomTarget		= (Integer)((JSONObject)report.get("tomtom")).get("target");
		long	tomtomDistFailure	= tomtomSize - tomtomTarget;
		double	tomtomCoverage		= ((float)tomtomCorrect / (float)googleSize); 
		double	tomtomSOCAVG		= (Float)((JSONObject)report.get("tomtom")).get("soc-avg");
		double	tomtomSOCTotal		= (Float)((JSONObject)report.get("tomtom")).get("soc-total");
		double	tomtomAOCAVG		= (Float)((JSONObject)report.get("tomtom")).get("aoc-avg");
		double	tomtomAOCTotal		= (Float)((JSONObject)report.get("tomtom")).get("aoc-total");
		
		System.out.print(keyword + "," + description + "," + googleSize + "," + hereSize + "," + hereTarget + "," + hereDistFailure + "," + hereCorrect + "," + hereReferences + "," + hereCoverage + "," + hereSOCAVG + "," + hereSOCTotal + "," + hereAOCAVG + "," + hereAOCTotal + ",");
		System.out.println(tomtomSize + "," + tomtomTarget + "," + tomtomDistFailure + "," + tomtomCorrect + "," + tomtomReferences + "," + tomtomCoverage + "," + tomtomSOCAVG + "," + tomtomSOCTotal + "," + tomtomAOCAVG + "," + tomtomAOCTotal);
	}
}
