package com.mnsoft.mcp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	private final static Logger logger = (Logger) LoggerFactory.getLogger(FileUtil.class);
	
    /**
     * file write
     *
     * @param dest(file object)
     * @param sb(content)
     */
	public static void writeFile(File dest, String str) {
		BufferedWriter 	bw	= null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest.getPath()), "UTF-8")); 
			bw.write(str);	
		} catch (Exception e) {
			logger.error("file write error : " + e.getMessage());
		}finally {
			try {
				if(bw != null) {
					bw.close();
				}
			} catch(Exception e) {
				logger.error("file write error : " + e.getMessage());
			}
		}
	}
	
    /**
     * file read
     *
     * @param dest(file object)
     * @return content
     */
	public static String readFile(File dest) {
		BufferedReader 	br	= null;
		StringBuffer buffer = new StringBuffer();
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(dest), "UTF-8"));
			String s = null;
			while((s = br.readLine()) != null) {
				buffer.append(s);
			}
		} catch (Exception e) {
			logger.error("file read error : " + e.getMessage());
		}finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch(Exception e) {
				logger.error("file read error : " + e.getMessage());
			}
		}
		return buffer.toString().trim();
	}
	/**
     * file read keyword csv file
     * keyword,lat,lng,radius
     * 
     * @return content
     */
	@SuppressWarnings("unchecked")
	public static JSONArray readCSVFile(File dest) {
		JSONArray 		list 		= new JSONArray();
		BufferedReader 	br			= null;
		JSONObject		contents 	= null;
		String 			s 			= null;
		String[]		tmpContents	= null;
		
		try {
			
			br = new BufferedReader(new FileReader(dest));
			
			while((s = br.readLine()) != null) {
				
				contents = new JSONObject();
				tmpContents = s.split(",");
				try {
					contents.put("keyword", tmpContents[0]);
					contents.put("lat", 	Double.parseDouble(tmpContents[1]));
					contents.put("lng", 	Double.parseDouble(tmpContents[2]));
					contents.put("radius", 	Integer.parseInt(tmpContents[3]));
				}catch(Exception e) {
					logger.error("file read error : " + s);
					continue;
				}
				list.add(contents);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("file read error : " + s);
		}finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch(Exception e) {
				logger.error("file read error : " + e.getMessage());
			}
		}
		return list;
	}
	public static JSONArray readJSONFile(File dest) {
		JSONParser		parser		= new JSONParser();
		JSONArray 		list 		= null;
		String			str			= readFile(dest);
		if(str == null || dest.exists() == false) {
			System.err.println("file is not exist.");
			return null;
		}
		
		try {
			list = (JSONArray)parser.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println(e.getMessage() + ": " + str);
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage() + ": " + str);
		}
		
		return list;
	}
	public static JSONObject readJSONObject(File dest) {
		JSONParser		parser		= new JSONParser();
		JSONObject 		obj 		= null;
		String			str			= readFile(dest);
		if(str == null || dest.exists() == false) {
			System.err.println("file is not exist.");
			return null;
		}
		
		try {
			obj = (JSONObject)parser.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println(e.getMessage() + ": " + str);
		} catch(Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage() + ": " + str);
		}
		
		return obj;
	}
	public static void main(String[] args) {
		JSONArray arr = readCSVFile(new File("C:\\Eclipse\\STS\\spring-tool-suite-3.9.0.RELEASE-e4.7.0-win32-x86_64\\sts-bundle\\workspace\\evaluation\\keyword.data"));
		System.out.println(arr.toJSONString());
	}
}
