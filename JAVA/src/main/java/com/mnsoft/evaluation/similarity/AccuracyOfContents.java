package com.mnsoft.evaluation.similarity;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mnsoft.mcp.util.FileUtil;

import info.debatty.java.stringsimilarity.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotoh;

public class AccuracyOfContents {
	
	@SuppressWarnings("unchecked")
	public JSONArray getReferenceCategoryList(JSONArray refNode) {
		JSONArray 	refCategories 		= new JSONArray();
		Set<String> refCategoriesSet 	= new HashSet<String>();				/* well-known cp's categories */
		for(int j=0; j<refNode.size(); j++) {
			refCategoriesSet.addAll((JSONArray)((JSONObject)refNode.get(j)).get("type"));
		}
		for(String category : refCategoriesSet) {
			refCategories.add(category);
		}
		return refCategories;
	}
	@SuppressWarnings({ "unchecked" })
	public void executeAOC(JSONObject data, JSONArray refCategories, String keyword, String cpName) {
		
		JSONObject 	content		 	= data;

		double		score			= 0.0;
		double		nameScore		= 0.0;
		double		typeScore 		= 0.0;
		double 		keywordWeight	= 0.3;
		double 		categoryWeight	= 0.7;
		double		swg				= 0;
		double		jw				= 0;
		String 		name  			= (String)content.get("name");
		JSONArray 	type  			= (JSONArray)content.get("type");
		JSONObject	scoreNode 		= new JSONObject(); 
		
		jw  		= this.jw.similarity(keyword.toLowerCase(), name.toLowerCase()) * keywordWeight;
		swg			= this.swg.getSimilarity(keyword.toLowerCase(), name.toLowerCase()) * keywordWeight;
		nameScore 	= jw > swg ? jw : swg;
		
		type		= this.getCategoriesMapper(type,cpName, refCategories);
		typeScore	= this.similarityOfCategory(type, refCategories) * categoryWeight;
		
		score = nameScore + typeScore;
		scoreNode.put("swg", 	swg);
		scoreNode.put("jw", 	jw);
		scoreNode.put("name", 	nameScore);
		scoreNode.put("type", 	typeScore);
		scoreNode.put("total", 	score);
		
		content.put("aoc", 	scoreNode);
	}
	
	private final static double	MIN_SCORE = 0.6;
	@SuppressWarnings({ "unchecked", "unused" })
	private JSONArray getFailDetails(JSONObject contentNode, String cpName) {
		JSONArray	data		= (JSONArray)contentNode.get(cpName);
		JSONArray	details		= new JSONArray();
		JSONObject 	content 	= null;
		JSONObject	scoreNode	= null;
		double		score		= 0.0;
		for(int i=0; i<data.size(); i++) {
			content 	= (JSONObject)data.get(i);
			scoreNode 	= (JSONObject)content.get("score");
			
			score = (Double)scoreNode.get("total");
			if(MIN_SCORE > score) {
				details.add(content);
			}
		}
		return details;
	}
	private JaroWinkler 		jw 	= new JaroWinkler();
	private SmithWatermanGotoh 	swg = new SmithWatermanGotoh();
	
	/* 
	 * AOC, SOC common module.
	 * TODO: implement 
	 * 
	 */
	private double similarityOfCategory(JSONArray target, JSONArray reference) {
		double similarity 	= 0.0;
		double tmp			= 0.0;
		
		String targetCategory 	= "";
		String refCategory		= "";
		for(int i=0; i<target.size(); i++) {
			targetCategory = (String)target.get(i);
			for(int j=0; j<reference.size(); j++) {
				refCategory = (String)reference.get(j);
				tmp	= this.swg.getSimilarity(targetCategory.toLowerCase(), refCategory.toLowerCase());
				
				if(similarity < tmp) {
					similarity = tmp;
				}
			}
		}
		return similarity;
	}
	@SuppressWarnings({ "unchecked", "unused"})
	private int getAOC(JSONObject contentNode, String cpName) {
		JSONObject 	query			= (JSONObject)contentNode.get("query");
		JSONArray	refCategories	= (JSONArray)contentNode.get("categories");
		JSONArray	data			= (JSONArray)contentNode.get(cpName);
		
		JSONObject 	content 		= null;
		String		keyword			= (String)query.get("keyword");
		int 		sum 			= 0;
		double		score			= 0.0;
		double		nameScore		= 0.0;
		double		typeScore 		= 0.0;
		
		
		double 		keywordWeight	= 0.3;
		double 		categoryWeight	= 0.7;
		double		swg				= 0;
		double		jw				= 0;
		
		for(int i=0; i<data.size(); i++) {
			content = (JSONObject)data.get(i);
			
			String 		name  		= (String)content.get("name");
			JSONArray 	type  		= (JSONArray)content.get("type");
			JSONObject	scoreNode 	= new JSONObject(); 
			
			jw  		= this.jw.similarity(keyword.toLowerCase(), name.toLowerCase()) * keywordWeight;
			swg			= this.swg.getSimilarity(keyword.toLowerCase(), name.toLowerCase()) * keywordWeight;
			nameScore 	= jw > swg ? jw : swg;
			
			type		= this.getCategoriesMapper(type,cpName, refCategories);
			if(type == null) {
				continue;
			}
			typeScore	= this.similarityOfCategory(type, refCategories) * categoryWeight;
			
			score = nameScore + typeScore;
			if(score > MIN_SCORE) {
				sum ++;
			}
			scoreNode.put("SWG", 	swg);
			scoreNode.put("JW", 	jw);
			scoreNode.put("type", 	typeScore);
			scoreNode.put("total", 	score);
			content.put("score", 	scoreNode);
			if(score < MIN_SCORE) {
				System.out.println("[CHECK][" + cpName + "][" + keyword + "]" + content.toJSONString().replaceAll("\\\\", ""));
			}
		}
		return sum;
	}
	
	private final static String 	CATEGORY_PATH 	= "./conf/category.json";
	private final static JSONObject	POLICY_CATEGORY = FileUtil.readJSONObject(new File(CATEGORY_PATH));
	@SuppressWarnings("unchecked")
	public JSONArray getCategoriesMapper(JSONArray categories, String cpName, JSONArray refCategories) {
		JSONObject 	policy = (JSONObject)POLICY_CATEGORY.get(cpName);
		JSONArray	mapper = new JSONArray();
		
		if(policy == null) {
			return categories;
		}
		
		for(int i=0; i<categories.size(); i++) {
			if(policy.get(categories.get(i)) == null) {
				System.out.println("[CHECK-CATEGORY][CP] " + cpName + " [CATEGORY]" + (String)categories.get(i) + "[REF-CATEGORY] " + refCategories.toJSONString());		/* non-registeredcategory */
				mapper.add(categories.get(i));
			}else {
				mapper.addAll((JSONArray)policy.get((String)categories.get(i)));
			}
		}
		return mapper;
	}
}
 

