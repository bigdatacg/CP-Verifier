package com.mnsoft.mcp.util;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class LookupUtil {

	public static List<String> lookup(String url) {
		List<String>	list		= new ArrayList<String>();
		URL 			aURL 		= null;
		InetAddress[] 	machines 	= null;

		if(url.contains("http://") == false && url.contains("https://") == false) {
			url = "http://" + url;
		}
		
		try {
			aURL 		= new URL(url);
			machines 	= InetAddress.getAllByName(aURL.getHost());

			for (InetAddress address : machines) {
				list.add(address.getHostAddress());
			}
		} catch (MalformedURLException e) {
			list.add(url);
			e.printStackTrace();
		} catch (UnknownHostException e) {
			list.add(url);
			e.printStackTrace();
		}
		return list;
	}
	
	public static boolean isEquals(String targetUrl, String refUrl) {
		List<String> targetIpList 	= LookupUtil.lookup(targetUrl);
		List<String> refIpList		= LookupUtil.lookup(refUrl);
		return targetIpList.containsAll(refIpList);
	}

	public static void main(String[] args) {
		System.out.println(LookupUtil.isEquals("www.starbucks.com/site-selector", "www.starbucks.de"));
	}

}
