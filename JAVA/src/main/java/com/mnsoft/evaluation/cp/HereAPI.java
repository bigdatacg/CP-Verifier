package com.mnsoft.evaluation.cp;

import java.net.URLEncoder;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.mnsoft.mcp.util.HTTPClient;

public class HereAPI {
	/*
	 * Here API
	 */
	private final static Header[] 	HERE_HEADER			= new Header[] {
	};
	private final static String		HERE_APP_ID			= "m8oovwJN7hiIpRyJpKlf";
	private final static String		HERE_APP_CODE		= "OMHTd6S4vIU3Nxr46ShH3w";
	private final static String		HERE_DEFAULT_PARAMS = "&size=10000&X-Mobility-Mode=drive";
	private final static String		HERE_API			= "http://places.api.here.com/places/v1/discover/search?app_id=" + HERE_APP_ID  + "&app_code=" + HERE_APP_CODE;
	
	@SuppressWarnings("deprecation")
	public String getHereAPI(double lat, double lng, long radius,String keyword) {
		return HERE_API + "&" + HERE_DEFAULT_PARAMS + "&in=" + lat + "," + lng + ";r=" + radius + "&q=" + URLEncoder.encode(keyword);
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getHerePOI(String json) {
		
		JSONArray	list	= new JSONArray();
		
		JSONParser parser 	= new JSONParser();
		JSONObject root		= null;
		try {
			root = (JSONObject)parser.parse(json);
		}catch(Exception e) {
			e.printStackTrace();
		}
		JSONObject results = (JSONObject)root.get("results");
		if(results == null) {
			return null;
		}
		JSONArray items = (JSONArray)results.get("items");
		if(items == null || items.size() == 0) {
			return null;
		}
		JSONObject 	item 	= null;
		String 		tmp 	= null;
		
		for(int i=0; i<items.size(); i++) {
			JSONObject data = new JSONObject();
			item = (JSONObject)items.get(i); 
			JSONArray location = (JSONArray)item.get("position");
			if(location != null) {
				data.put("location", 	location.get(0) + "," + location.get(1));
			}
			tmp = (String)item.get("title");
			
			JSONArray typeArr = new JSONArray();
			typeArr.add(((JSONObject)item.get("category")).get("title"));
			data.put("name", 	tmp);
			data.put("type", 	typeArr);
			data.put("id", 		item.get("id"));
			data.put("sequence", (i+1));
			
			tmp = (String)((JSONObject)item.get("address")).get("text");
			tmp = tmp.replaceAll("<br/>", " ");
			data.put("address", tmp);

			JSONObject contacts = (JSONObject)item.get("contacts");
			if(contacts != null) {
				JSONArray phoneList = (JSONArray)contacts.get("phone");
				if(phoneList != null) {
					tmp = (String)((JSONObject)phoneList.get(0)).get("value");
					data.put("phone", tmp);
				}else {
					data.put("phone", null);
				}
				JSONArray websiteList = (JSONArray)contacts.get("website");
				if(websiteList != null) {
					tmp = (String)((JSONObject)websiteList.get(0)).get("value");
					data.put("website", tmp);
				}else {
					data.put("website", null);
				}
			}
			list.add(data);
		}
		return list;
	}
	@SuppressWarnings("unchecked")
	public JSONArray getHereAPI(JSONObject root) {
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
		String 		url			= this.getHereAPI(lat,lng,radius,keyword);
		HTTPClient 	client 		= new HTTPClient();
		String		json		= null;
		JSONObject	urlNode		= (JSONObject)root.get("url");
		if(urlNode == null) {
			urlNode = new JSONObject();
			root.put("url", urlNode);
		}
		urlNode.put("here", url);
		
		json 	= client.get(url, HERE_HEADER);
		
		return getHerePOI(json);
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		HereAPI 	ha		= new HereAPI();
		JSONObject 	query 	= new JSONObject();
		
		query.put("lat", 		48.8588377);
		query.put("lng", 		2.2770207);
		query.put("radius", 	500);
		query.put("keyword", 	"coffee");
		
		JSONArray arr 		= ha.getHereAPI(query);
		
		if(arr == null) {
			return;
		}
		System.out.println(arr.toString().replaceAll("\\\\", ""));
	}
}

