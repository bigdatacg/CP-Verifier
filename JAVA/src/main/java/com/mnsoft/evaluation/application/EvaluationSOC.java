package com.mnsoft.evaluation.application;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mnsoft.evaluation.cp.Crawler;
import com.mnsoft.evaluation.similarity.SimilarityOfContents;
import com.mnsoft.mcp.util.FileUtil;

public class EvaluationSOC {
	
	private final static String KEYWORD		= "20190528";
	private final static String JSON_PATH 	= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + ".json";
	@SuppressWarnings("unused")
	private final static String DEST_PATH 	= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + "-result.json";

	private final static double POLICY_PASS = 0.65;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		JSONArray 				queryList 	= FileUtil.readJSONFile(new File(JSON_PATH));
		Crawler					crawler		= new Crawler();
		JSONArray				resultList	= crawler.crawling(queryList);
		SimilarityOfContents	soc			= new SimilarityOfContents();
		JSONObject				report		= null;
		
		JSONArray				hereReport		= null;
		JSONArray				tomtomReport	= null;
		JSONObject				tmp				= null;
		JSONArray				tmpArr			= null;
		double					score			= 0.0;
		int						iCount			= 0;
		
		System.out.println(resultList.toJSONString());
		
		for(int i=0; i<resultList.size(); i++) {
			report			= soc.getSOC((JSONObject)resultList.get(i));
			hereReport		= (JSONArray)((JSONObject)report.get("here")).get("report");
			tomtomReport	= (JSONArray)((JSONObject)report.get("tomtom")).get("report");
			tmp				= null;
			tmpArr			= null;
			score			= 0.0;
			iCount			= 0;
			
			for(int j=0; j<hereReport.size(); j++) {
				tmp 	= (JSONObject)hereReport.get(j);
				tmpArr	= (JSONArray)tmp.get("references");
				for(int k=0; k<tmpArr.size(); k++) {
					tmp 	= (JSONObject)tmpArr.get(k);
					score 	= (Double)((JSONObject)tmp.get("soc")).get("total");
					if(score > POLICY_PASS) {
						iCount++;
					}
				}
			}
			((JSONObject)report.get("here")).put("correct", iCount);
			
			iCount = 0;
			for(int j=0; j<tomtomReport.size(); j++) {
				tmp 	= (JSONObject)tomtomReport.get(j);
				tmpArr	= (JSONArray)tmp.get("references");
				for(int k=0; k<tmpArr.size(); k++) {
					tmp 	= (JSONObject)tmpArr.get(k);
					score 	= (Double)((JSONObject)tmp.get("soc")).get("total");
					if(score > POLICY_PASS) {
						iCount++;
					}
				}
			}
			((JSONObject)report.get("tomtom")).put("correct", iCount);
			
			System.out.println(report.toJSONString().replaceAll("\\\\", ""));
//		FileUtil.writeFile(new File(DEST_PATH), report.toJSONString().replaceAll("\\\\", ""));
		}
		
		
		
	}

}
