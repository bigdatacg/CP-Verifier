package com.mnsoft.evaluation.application;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mnsoft.evaluation.cp.Crawler;
import com.mnsoft.evaluation.similarity.AccuracyOfContents;
import com.mnsoft.evaluation.similarity.SimilarityOfContents;
import com.mnsoft.mcp.util.FileUtil;

public class Evaluation {
	
	public final static String KEYWORD				= "subway";
	public final static String JSON_PATH 			= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + ".json";
	public final static String CRAWL_DEST_PATH 		= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + "-crawl.json";
	public final static String REPORT_DEST_PATH 	= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + "-report.json";

	private final static double POLICY_PASS = 0.65;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		JSONArray 				queryList 		= FileUtil.readJSONFile(new File(JSON_PATH));
		Crawler					crawler			= new Crawler();
//		JSONArray				resultList		= crawler.crawling(queryList);		
		JSONArray				resultList		= FileUtil.readJSONFile(new File(CRAWL_DEST_PATH));				/* for test */
		SimilarityOfContents	soc				= new SimilarityOfContents();
		AccuracyOfContents		aoc				= new AccuracyOfContents();
		JSONObject				report			= null;
		JSONArray				reportList		= new JSONArray();
		
		JSONArray				hereReport		= null;
		JSONArray				tomtomReport	= null;
		JSONObject				tmp				= null;
		JSONArray				tmpArr			= null;
		double					score			= 0.0; 
		int						iCount			= 0;
		int						loopSize		= 0;
		
		JSONArray				refCategories	= null;
		
		FileUtil.writeFile(new File(CRAWL_DEST_PATH), resultList.toJSONString());
		
		for(int i=0; i<resultList.size(); i++) {
			/* reference categories */
			refCategories	= aoc.getReferenceCategoryList((JSONArray)((JSONObject)resultList.get(i)).get("google"));
			
			/* SOC */
			report			= soc.getSOC((JSONObject)resultList.get(i));
			hereReport		= (JSONArray)((JSONObject)report.get("here")).get("report");
			tomtomReport	= (JSONArray)((JSONObject)report.get("tomtom")).get("report");
			tmp				= null;
			tmpArr			= null;
			score			= 0.0;
			iCount			= 0;
			loopSize		= hereReport == null ? 0 : hereReport.size();
			
			for(int j=0; j<loopSize; j++) {
				
				/* SOC-REPORTING(HERE) */
				tmp 	= (JSONObject)hereReport.get(j);
				tmpArr	= (JSONArray)tmp.get("references");
				for(int k=0; k<tmpArr.size(); k++) {
					tmp 	= (JSONObject)tmpArr.get(k);
					score 	= (Double)((JSONObject)tmp.get("soc")).get("total");
					if(score > POLICY_PASS) {
						iCount++;
						break;
					}
				}
				
				/* AOC-REPORTING(HERE) */
				aoc.executeAOC((JSONObject)hereReport.get(j), refCategories,(String)((JSONObject)((JSONObject)resultList.get(i)).get("query")).get("keyword"), "here");
			}
			((JSONObject)report.get("here")).put("correct", iCount);
			
			/* SOC-REPORTING(TOMTOM) */
			iCount = 0;
			loopSize		= tomtomReport == null ? 0 : tomtomReport.size();
			for(int j=0; j<loopSize; j++) {
				tmp 	= (JSONObject)tomtomReport.get(j);
				tmpArr	= (JSONArray)tmp.get("references");
				for(int k=0; k<tmpArr.size(); k++) {
					tmp 	= (JSONObject)tmpArr.get(k);
					score 	= (Double)((JSONObject)tmp.get("soc")).get("total");
					if(score > POLICY_PASS) {
						iCount++;
						break;
					}
				}
				
				/* AOC-REPORTING(HERE) */
				aoc.executeAOC((JSONObject)tomtomReport.get(j), refCategories,(String)((JSONObject)((JSONObject)resultList.get(i)).get("query")).get("keyword"), "tomtom");
			}
			((JSONObject)report.get("tomtom")).put("correct", iCount);
			reportList.add(report);
		}
		System.out.println(reportList.toJSONString());
		FileUtil.writeFile(new File(REPORT_DEST_PATH), reportList.toJSONString());
	}

}
