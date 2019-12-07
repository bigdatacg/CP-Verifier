package com.mnsoft.evaluation.similarity;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mnsoft.mcp.util.DistanceUtil;
import com.mnsoft.mcp.util.FileUtil;
import com.mnsoft.mcp.util.LookupUtil;

import info.debatty.java.stringsimilarity.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWatermanGotoh;

public class SimilarityOfContents {
	
	private final static String SOURCE_PATH = "C:\\www\\localsearch-evaludation\\germany\\keyword\\20190527-result.json";
	
	@SuppressWarnings("unchecked")
	public JSONObject getSOC(JSONObject data) {
		JSONObject 	contentNode = data;
		JSONObject	result		= null;
		JSONObject	cp			= null;
		
		result			= new JSONObject();
		cp 				= new JSONObject();
		
		result.put("query", (JSONObject)contentNode.get("query"));
		try {
			cp.put("report", 	this.getSOC(contentNode, "here", "google"));
			cp.put("size", (contentNode.get("here") == null ? 0 : ((JSONArray)contentNode.get("here")).size()));
			result.put("here", cp);
		} catch (Exception e) {
			e.printStackTrace();
			cp.put("report", null);
		}
		
		result.put("google", ((JSONArray)contentNode.get("google")).size());

		cp 				= new JSONObject();
		try {
			cp.put("report", 	this.getSOC(contentNode, "tomtom", "google"));
			cp.put("size", (contentNode.get("tomtom") == null ? 0 : ((JSONArray)contentNode.get("tomtom")).size()));
			result.put("tomtom", cp);
		} catch (Exception e) {
			e.printStackTrace();
			cp.put("report", null);
		}
		
		return result;
	}
	@SuppressWarnings({ "unchecked", "unused" })
	private JSONArray getFailDetails(JSONObject contentNode, String cpName) {
		JSONObject 	query		= (JSONObject)contentNode.get("query");
		JSONArray	data		= (JSONArray)contentNode.get(cpName);
		JSONObject	policy		= (JSONObject)query.get("policy");
		JSONObject	aocPolicy	= (JSONObject)policy.get("aoc");
		long 		minLimit	= (Long)((JSONObject)aocPolicy.get("score")).get("min");
		
		JSONArray	details		= new JSONArray();
		JSONObject 	content 	= null;
		JSONObject	scoreNode	= null;
		double		score		= 0.0;
		for(int i=0; i<data.size(); i++) {
			content 	= (JSONObject)data.get(i);
			scoreNode 	= (JSONObject)content.get("score");
			
			score = (Double)scoreNode.get("total");
			if(minLimit > score) {
				details.add(content);
			}
		}
		return details;
	}
	
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
	
	/*
	 * [score]
	 * 1. phone: 	TRUE  (priority 1)
	 * 2. name:     40%
	 * 3. address:  50%
	 * 4. location: FAIL Condition, 50m
	 * 5. website:  10%
	 * 
	 * [policy]
	 * PASS: 70% ~ 100%
	 * FAIL: 0%	 ~ 70%
	 */
	private JaroWinkler 		jw 	= new JaroWinkler();
	private SmithWatermanGotoh 	swg = new SmithWatermanGotoh();
	@SuppressWarnings("unused")
	private Levenshtein 		ll 	= new Levenshtein();
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
	@SuppressWarnings("unchecked")
	private JSONArray getSOC(JSONObject contentNode, String targetCPName, String refCPName) throws Exception {
		if(contentNode.get(targetCPName) == null) {
			return null;
		}
		JSONArray 	targetArr 	= (JSONArray)((JSONArray)contentNode.get(targetCPName)).clone();
		JSONArray 	refArr		= (JSONArray)contentNode.get(refCPName);
		
		JSONObject	targetNode 	= null;
		JSONObject	refNode		= null;
		
		String		targetName	= null;
		String		targetPhone = null;
		String		targetAddr	= null;
		String 		targetWeb	= null;
		double		targetLat	= 0.0;
		double		targetLon	= 0.0;
		String		targetType	= null;
		
		String		refName		= null;
		String		refPhone 	= null;
		String		refAddr		= null;
		String 		refWeb		= null;
		double		refLat		= 0.0;
		double		refLon		= 0.0;
		String		refType		= null;
		
		/* temp score */
		double		tmp			= 0.0;
		@SuppressWarnings("unused")
		double		tmp2		= 0.0;
		double		scoreName	= 0.0;
		double		scoreAddr	= 0.0;
		double		scoreWeb	= 0.0;
		double		scorePhone	= 0.0;
		double		scoreType 	= 0.0;
		double		scoreTotal	= 0.0;
		
		final double	POLICY_NAME		= 0.30;		// 0.20
		final double	POLICY_ADDR		= 0.40;		// 0.50
		final double	POLICY_PHONE 	= 0.15;		// 0.10
		final double	POLICY_TYPE		= 0.10;		// 0.15
		final double	POLICY_WEB		= 0.05;		// 0.05
		final int		POLICY_DISTANCE	= 100;		// 1000
		
		double 	distance 		= 0.0;
		
		if(targetArr == null) {
			return null;
		}
		
		JSONArray 	resultList 		= null;
		JSONObject	result			= null;
		JSONObject	score			= null;
		
		for(int i=0; i<targetArr.size(); i++) {
			resultList 	= new JSONArray();
			targetNode 	= (JSONObject)targetArr.get(i);
			targetName 	= (String)targetNode.get("name");
			targetPhone	= (String)targetNode.get("phone");
			targetAddr	= (String)targetNode.get("address");
			targetWeb	= (String)targetNode.get("website");
			targetPhone	= (String)targetNode.get("phone");
			targetLat	= Double.parseDouble(((String)targetNode.get("location")).split(",")[0]);
			targetLon	= Double.parseDouble(((String)targetNode.get("location")).split(",")[1]);
			targetType	= (String)((JSONArray)targetNode.get("type")).get(0);
			
			for(int j=0; j<refArr.size(); j++) {
				refNode 	= (JSONObject)refArr.get(j);
				refName 	= (String)refNode.get("name");
				refPhone	= (String)refNode.get("phone");
				refAddr		= (String)refNode.get("address");
				refWeb		= (String)refNode.get("website");
				refPhone	= (String)refNode.get("phone");
				refLat		= Double.parseDouble(((String)refNode.get("location")).split(",")[0]);
				refLon		= Double.parseDouble(((String)refNode.get("location")).split(",")[1]);
				refType		= (String)((JSONArray)refNode.get("type")).get(0);
				
				/* unconditional false */
				distance = DistanceUtil.distance(targetLat, targetLon, refLat, refLon);
				if(distance > POLICY_DISTANCE) {
					continue;
				}
				/* unconditional true */
				scorePhone = 0.0;
				if(targetPhone != null && refPhone != null && targetPhone.equals(refPhone) == true) {
					scorePhone = POLICY_PHONE;
				}
				
				/* score of name */
				scoreName = tmp = 0.0;
				if(targetName != null && refName != null) {
					scoreName 	= this.jw.similarity(targetName.toLowerCase(), refName.toLowerCase());
					tmp			= this.swg.getSimilarity(targetName.toLowerCase(), refName.toLowerCase());
					scoreName 	= ((scoreName * 0.7) + (tmp * 0.3))  * POLICY_NAME;
				}
				
				/* unconditional false */
				if(scoreName < POLICY_NAME / 2) {
					continue;
				}
				
				/* score of address */
				scoreAddr = tmp = 0.0;
				if(targetAddr != null && refAddr != null) {
					scoreAddr 	= this.jw.similarity(targetAddr.toLowerCase(), refAddr.toLowerCase());
					tmp			= this.swg.getSimilarity(targetAddr.toLowerCase(), refAddr.toLowerCase());
					scoreAddr 	= ((scoreAddr * 0.3) + (tmp * 0.7))  * POLICY_ADDR;
				}
				
				/* unconditional false */
				if(scoreAddr < POLICY_ADDR / 2) {
					continue;
				}
				
				/* score of website */
				scoreWeb = tmp = 0.0;
				if(targetWeb != null && refWeb != null && LookupUtil.isEquals(targetWeb, refWeb)) {
					scoreWeb = POLICY_WEB;
				}
				
				/* score of category */
				scoreType = tmp = 0.0;
				
				
				if(targetType != null && refType != null) {
					scoreType	= this.similarityOfCategory((JSONArray)targetNode.get("type"), (JSONArray)refNode.get("type")) * POLICY_TYPE;
				}
				
				scoreTotal = scoreName + scoreAddr + scoreWeb + scorePhone + scoreType;
				
				result 	= new JSONObject();
				score	= new JSONObject();
				
				score.put("total", 			scoreTotal);
				score.put("name", 			scoreName);
				score.put("address", 		scoreAddr);
				score.put("phone", 			scorePhone);
				score.put("web", 			scoreWeb);
				score.put("type", 			scoreType);
				score.put("distance", 		distance);
				result.put("soc", 			score);
				result.put("reference", 	refNode);
				
				resultList.add(result);
				
				if(scoreTotal > 0.65) {
//					System.out.println("[PASS][" + i + "][" + j + "] target:    " 	+ targetNode.toJSONString().replaceAll("\\\\", ""));
//					System.out.println("[PASS][" + i + "][" + j + "] reference: " 	+ refNode.toJSONString().replaceAll("\\\\", ""));
//					System.out.println("[PASS][" + i + "][" + j + "] total: " + scoreTotal + ", name: " + scoreName + ", addr: " + scoreAddr + ", web: " + scoreWeb + ", phone: " + scorePhone + ", type:" + scoreType + " distance: " + distance);
				} else {
//					System.out.println("[FAIL][" + i + "][" + j + "] target:    " 	+ targetNode.toJSONString().replaceAll("\\\\", ""));
//					System.out.println("[FAIL][" + i + "][" + j + "] reference: " 	+ refNode.toJSONString().replaceAll("\\\\", ""));
//					System.out.println("[FAIL][" + i + "][" + j + "] total: " + scoreTotal + ", name: " + scoreName + ", addr: " + scoreAddr + ", web: " + scoreWeb + ", phone: " + scorePhone + ", type:" + scoreType + " distance: " + distance);
				}
			}
			if(resultList.size() != 0) {
				targetNode.put("references", resultList);
			}else {
				targetNode.put("distance-filtered", true);
			}
		}
		return this.sort(targetArr.toArray());
	}
	@SuppressWarnings("unchecked")
	private JSONArray sort(Object[] resultList) {
		JSONObject 	tmp 		= null;
		JSONObject 	tmp2 		= null;
		JSONArray	tmpArr		= null;
		JSONArray	tmpArr2		= null;
		JSONArray	sorted		= new JSONArray();
		double		tmpScore 	= 0.0;
		double		tmpScore2	= 0.0;
		for(int i=0; i<resultList.length; i++) {

			for(int j=0; j<resultList.length; j++) {
				
				/* search max total score */
				tmpArr 			= (JSONArray)((JSONObject)resultList[i]).get("references");
				tmpScore		= 0.0;
				if(tmpArr != null) {
					for(int k=0; k<tmpArr.size(); k++) {
						tmp = (JSONObject)tmpArr.get(k);
						if(tmpScore < (Double)((JSONObject)tmp.get("soc")).get("total")) {
							tmpScore = (Double)((JSONObject)tmp.get("soc")).get("total");
						}
					}
				}
				
				/* search max total score */
				tmpArr2 		= (JSONArray)((JSONObject)resultList[j]).get("references");
				tmpScore2		= 0.0;
				if(tmpArr2 == null) {
					continue;
				}
				for(int k=0; k<tmpArr2.size(); k++) {
					tmp = (JSONObject)tmpArr2.get(k);
					if(tmpScore2 < (Double)((JSONObject)tmp.get("soc")).get("total")) {
						tmpScore2 = (Double)((JSONObject)tmp.get("soc")).get("total");
					}
				}
				
				
				if(tmpScore > tmpScore2) {
					tmp2			= (JSONObject)resultList[j]; 
					resultList[j] 	= (JSONObject)resultList[i];
					resultList[i] 	= tmp2;
				}
			}
		}
		
		for(int i=0; i<resultList.length; i++) {
			if((JSONArray)((JSONObject)resultList[i]).get("references") != null) {
				sorted.add((JSONObject)resultList[i]);
			}
		}
		return sorted;
	}
	public static void main(String[] args) {
		JSONArray 				data 	= FileUtil.readJSONFile(new File(SOURCE_PATH));
		SimilarityOfContents	soc		= new SimilarityOfContents();
		
		for(int i=0; i<data.size(); i++) {
			System.out.println(soc.getSOC((JSONObject)data.get(i)).toJSONString());
		}
	}
}
