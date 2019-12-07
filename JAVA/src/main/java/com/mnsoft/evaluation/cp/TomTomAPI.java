package com.mnsoft.evaluation.cp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URLEncoder;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mnsoft.mcp.util.FileUtil;
import com.mnsoft.mcp.util.HTTPClient;

public class TomTomAPI {
	
	/*
	 * TomTom API
	 * 
	 * [Reference]
	 * https://developer.tomtom.com/search-api/search-api-documentation-search/points-interest-search
	 * 
	 * [Optional]
	 * limit
	 * - default: 10, max: 100
	 * 
	 * radius
	 * - max: 50000m
	 */
	private final static Header[] 	TOMTOM_HEADER 	= new Header[] { };
	private final static String 	API_KEY 		= "key=M5FatcZumFU8JLnwCQ5XqlVzc8UA6EwB";
	private final static String 	TOMTOM_API 		= "https://api.tomtom.com/search/2/poiSearch/api.json?" + API_KEY;
	
	
	@SuppressWarnings("deprecation")
	public String getTomTomAPI(double lat, double lng, long radius, String keyword) {
		return TOMTOM_API + "&limit=100&lat=" + lat + "&lon=" + lng + "&radius=" + radius + "&query=" + URLEncoder.encode(keyword);
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getTomTomPOI(String json) {
		
		JSONArray	list	= new JSONArray();
		
		JSONParser parser 	= new JSONParser();
		JSONObject root		= null;
		try {
			root = (JSONObject)parser.parse(json);
		}catch(Exception e) {
			e.printStackTrace();
		}
		JSONArray results = (JSONArray)root.get("results");
		if(results == null) {
			return null;
		}
		
		JSONObject 	item 	= null;
		String		tmp 	= null;
		for(int i=0; i<results.size(); i++) {
			JSONObject data = new JSONObject();
			
			item = (JSONObject)results.get(i); 
			JSONObject location = ((JSONObject)item.get("position"));
			if(location != null) {
				data.put("location", 	location.get("lat") + "," + location.get("lon"));
			}
			data.put("name", 	((JSONObject)item.get("poi")).get("name"));
			data.put("id",		 item.get("id"));
			data.put("sequence", (i+1));
			
			tmp = (String)((JSONObject)item.get("poi")).get("phone");
			if(tmp != null) {
				tmp = tmp.replaceAll("\\(","").replaceAll("\\)","").replaceAll("-", "");
			}
			data.put("phone", tmp);
			data.put("website", ((JSONObject)item.get("poi")).get("url"));
			data.put("type", (JSONArray)((JSONObject)item.get("poi")).get("categories"));
			
			JSONObject 	addrNode 	= (JSONObject)item.get("address");
			String 		addr 		= (addrNode.get("streetNumber") 	== null ? "" : (String)addrNode.get("streetNumber") + " ") 
									+ (addrNode.get("streetName") 	== null ? "" : (String)addrNode.get("streetName") 		+ " ") 
									+ (data.get("name") 				== null ? "" : (String)data.get("name") 			+ " ")
									+ (addrNode.get("postalCode") 	== null ? "" : (String)addrNode.get("postalCode") 		+ " ")
									+ (addrNode.get("municipality") 	== null ? "" : (String)addrNode.get("municipality") + " " )
									+ (addrNode.get("country") 		== null ? "" : (String)addrNode.get("country"));
			
			data.put("address", addr);

			list.add(data);
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getTomTomAPI(JSONObject root) {
		if(root == null || 
				root.get("keyword") 	== null ||
				root.get("location") 	== null) {
			System.err.println("crawler policy is not well-formed.");
			return null;
		}
		
		JSONObject 	location	= (JSONObject)root.get("location");
		double 		lat 		= (Double)location.get("lat");
		double 		lng 		= (Double)location.get("lon");
		long		radius 		= (Long)location.get("radius");
		String		keyword		= (String)root.get("keyword");
		String 		url			= this.getTomTomAPI(lat,lng,radius,keyword);
		HTTPClient 	client 		= new HTTPClient();
		String		json		= null;
		JSONObject	urlNode		= (JSONObject)root.get("url");
		if(urlNode == null) {
			urlNode = new JSONObject();
			root.put("url", urlNode);
		}
		urlNode.put("tomtom", url);
		
		json 	= client.get(url, TOMTOM_HEADER);
		
		if(json == null) {
			return null;
		}
		return getTomTomPOI(json);
	}
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		
		String baseFiltPath = "";	
		
		if(args.length > 0)
		{
			baseFiltPath = args[0] + "\\";
		}
				
		JSONArray  queryList = FileUtil.readJSONFile(new File(baseFiltPath + "InputKeyword.json"));
		
		JSONObject 	queryItem 	= null;
		for( int iQueryIdx = 0 ; iQueryIdx < queryList.size() ; iQueryIdx++ )
		{
			TomTomAPI  ta		= new TomTomAPI();
			
			queryItem  = (JSONObject)queryList.get(iQueryIdx);
			JSONArray arr = ta.getTomTomAPI(queryItem);
			
			String keyworyd = (String)queryItem.get("keyword");
			FileUtil.writeFile(new File(baseFiltPath+keyworyd+"_tomtom.json"), arr.toJSONString());
			
		}
		
	}
}
