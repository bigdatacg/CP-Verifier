package com.mnsoft.evaluation.cp;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mnsoft.mcp.util.HTTPClient;

public class GoogleAPI {
	/*
	 * Google API
	 * 
	 * [limit]
	 * radius: 50,000m
	 * 
	 * By default, each Nearby Search or Text Search returns up to 20 establishment results per query; 
	 * however, each search can return as many as 60 results, split across three pages. 
	 * If your search will return more than 20, then the search response will include an additional value — next_page_token. 
	 */
	private final static Header[] 	GOOGLE_HEADER		= new Header[] { };
	private final static String 	GOOGLE_KEY	  		= "AIzaSyD-xd3AhQLymRrThgae5RDkLkJa4m9sSRU";
	private final static String		GOOGLE_API			= "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + GOOGLE_KEY;
	
	private final static String		GOOGLE_DETAIL_API 		= "https://maps.googleapis.com/maps/api/place/details/json?key=" + GOOGLE_KEY;
	private final static String		GOOGLE_DEFAULT_PARAMS 	= "&fields=address_component,adr_address,alt_id,formatted_address,geometry,icon,id,name,permanently_closed,photo,place_id,plus_code,scope,type,url,utc_offset,vicinity,formatted_phone_number,international_phone_number,opening_hours,website";
	
	
	@SuppressWarnings("deprecation")
	private String getGoogleAPI(double lat, double lng, long radius, String keyword) {
		return GOOGLE_API + "&location=" + lat + "," + lng + "&radius=" + radius + "&keyword=" + URLEncoder.encode(keyword);
	}
	

	public class PlaceDetail implements Runnable {

		private JSONObject 	dataNode	= null;
		private HTTPClient 	client 		= new HTTPClient();
		private JSONObject 	result 		= new JSONObject();
		private int			sequence	= 0;
		
		public PlaceDetail(JSONObject dataNode, int sequence) {
			this.dataNode = dataNode;
			this.sequence = sequence;
		}
		
		public void run() {
			this.crawl();
		}
		
		private String getGoogleDetailAPI(String placeId) {
			return GOOGLE_DETAIL_API + GOOGLE_DEFAULT_PARAMS + "&placeid=" + placeId;
		}
		
		@SuppressWarnings("unchecked")
		public void crawl() {
			
			JSONObject 	location 	= (JSONObject) ((JSONObject)this.dataNode.get("geometry")).get("location");
			String 		url 		= null;
			String		detail		= null;
			JSONObject	detailNode	= null;
			
			this.result.put("location", (Double)location.get("lat") + "," + (Double)location.get("lng"));
			this.result.put("name",		this.dataNode.get("name"));
			this.result.put("sequence", this.sequence);
			this.result.put("type", 	(JSONArray)this.dataNode.get("types"));
			
			url 		= getGoogleDetailAPI((String)this.dataNode.get("place_id"));
			detail		= client.get(url, GOOGLE_HEADER);
			detailNode	= getGoogleDetailPOI(detail);
			
			if(detailNode != null) {
				this.result.putAll(detailNode);
			}
		}
		
		@SuppressWarnings({"unchecked" })
		public JSONObject getGoogleDetailPOI(String json) {
			JSONObject object	= new JSONObject();
			JSONParser parser 	= new JSONParser();
			JSONObject root		= null;
			try {
				root = (JSONObject)parser.parse(json);
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			JSONObject result = (JSONObject)root.get("result");
			if(result == null) {
				return null;
			}
			
			String tmp = null;
			if(result.get("formatted_address") != null) {
				object.put("address", ((String)result.get("formatted_address")).replaceAll(",", ""));
			}else {
				object.put("address", null);
			}
			if(result.get("international_phone_number") != null) {
				tmp = (String)result.get("international_phone_number");
				tmp = tmp.replaceAll(" ", "").replaceAll("-", "");
				object.put("phone", tmp);
			}else {
				object.put("phone", null);
			}
			object.put("website", result.get("website"));
			object.put("id", result.get("place_id"));
			return object;
		}
		
		public JSONObject getResult() {
			return this.result;
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getGooglePOI(String json, int index) {
		JSONArray			resultList	= new JSONArray();
		JSONParser 			parser 		= new JSONParser();
		JSONObject 			root		= null;
		List<PlaceDetail> 	thList 		= null;
		ExecutorService 	service 	= null;
		
		try {
			root = (JSONObject)parser.parse(json);
		}catch(Exception e) {
			e.printStackTrace();
		}
		JSONArray results = (JSONArray)root.get("results");
		if(results == null || results.size() == 0) {
			System.out.println(json);
			return null;
		}
		
		/* 
		 * thread: under 20 threads. 
		 * 
		 * [Reference]
		 * By default, each Nearby Search or Text Search returns up to 20
		 */
		thList = new ArrayList<PlaceDetail>(results.size());
		for(int i=0; i<results.size(); i++) {
			int sequence = (i+1) + (index * 20);
			thList.add(new PlaceDetail((JSONObject)results.get(i), sequence));
		}
		
		service = Executors.newFixedThreadPool(thList.size());
		for(Runnable r : thList) {
			service.submit(r);
		}
		service.shutdown();
		while(service.isTerminated() == false) {
			try {
				service.awaitTermination(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for(PlaceDetail th : thList) {
			resultList.add(th.getResult());
		}
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getGoogleAPI(JSONObject root) {
		
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
		String 		url			= getGoogleAPI(lat,lng,radius,keyword);
		HTTPClient 	client 		= new HTTPClient();
		String		json		= null;

		JSONObject	urlNode		= (JSONObject)root.get("url");
		if(urlNode == null) {
			urlNode = new JSONObject();
			root.put("url", urlNode);
		}
		urlNode.put("google", url);
		
		JSONArray	resultList  = new JSONArray();
		JSONObject	object		= null; 
		JSONParser	parser		= new JSONParser();
		String		nextToken	= null;
		
		JSONArray	tmpList		= null;
		int			index		= 0;
		do {
			url		= getGoogleAPI(lat,lng,radius,keyword);
			if(nextToken != null && nextToken.equals("") == false) {
				url += "&pagetoken=" + nextToken;
			}
			json 	= client.get(url, GOOGLE_HEADER);
			
			/*
			 * There is a short delay between when a next_page_token is issued, and when it will become valid. 
			 * Requesting the next page before it is available will return an INVALID_REQUEST response. 
			 * Retrying the request with the same next_page_token will return the next page of results.
			 * 
			 * https://developers.google.com/places/web-service/search
			 */
			if(json.contains("INVALID_REQUEST") == true) {
				try {
					System.err.println("sleep for crawling to google...");
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			
			try {
				object 		= (JSONObject)parser.parse(json);
				nextToken 	= (String)object.get("next_page_token");
			} catch (ParseException e) {
				e.printStackTrace();
			} 
			
			tmpList = getGooglePOI(json, index++);
			if(tmpList != null) {
				resultList.addAll(tmpList);
			}
		}while(nextToken != null);
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		GoogleAPI 	ga			= new GoogleAPI();
		JSONObject 	query 		= new JSONObject();
		JSONObject 	location 	= new JSONObject();
		long		radius		= 500;
		location.put("lat", 	48.8588377);
		location.put("lon", 	2.2770207);
		location.put("radius", 	radius);
		query.put("location", 	location);
		query.put("keyword", 	"coffee");
		
		JSONArray arr = ga.getGoogleAPI(query);
		if(arr == null) {
			return;
		}
		System.out.println(arr.toString());
	}
	
}
