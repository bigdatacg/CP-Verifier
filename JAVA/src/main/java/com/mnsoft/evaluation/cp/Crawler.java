package com.mnsoft.evaluation.cp;

import java.io.File;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mnsoft.mcp.util.FileUtil;

public class Crawler {

	private final static String KEYWORD		= "fuel";
	private final static String JSON_PATH 	= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + ".json";
	@SuppressWarnings("unused")
	private final static String DEST_PATH 	= "C:\\www\\localsearch-evaludation\\germany\\keyword\\" + KEYWORD + "-result.json";
	
	
	private GoogleAPI 	ga = new GoogleAPI();
	private HereAPI		ha = new HereAPI();
	private TomTomAPI	ta = new TomTomAPI();
	
	@SuppressWarnings("unchecked") 
	public JSONObject crawling(JSONObject query) {
		JSONObject	result 		= new JSONObject();
		
		result.put("query", 	query);
		result.put("google", 	this.ga.getGoogleAPI(query));
		result.put("here", 		this.ha.getHereAPI(query));
		result.put("tomtom", 	this.ta.getTomTomAPI(query));
		
		return result; 
	}
	@SuppressWarnings("unchecked")
	public JSONArray crawling(JSONArray queryList) {
		JSONArray	resultList	= new JSONArray();
		for(int i=0; i<queryList.size(); i++) {
			resultList.add(this.crawling((JSONObject)queryList.get(i)));
		}
		return resultList;
	}
	
	public static void main(String[] args) {
		JSONArray 	queryList 	= FileUtil.readJSONFile(new File(JSON_PATH));
		Crawler		crawler		= new Crawler();
		JSONArray	resultList	= crawler.crawling(queryList);
		
		System.out.println(resultList.toJSONString().replaceAll("\\\\", ""));
		// FileUtil.writeFile(new File(DEST_PATH), resultList.toJSONString().replaceAll("\\\\", ""));
	}
	

}
