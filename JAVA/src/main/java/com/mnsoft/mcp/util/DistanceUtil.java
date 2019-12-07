package com.mnsoft.mcp.util;

public class DistanceUtil {
	public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta    = lon1 - lon2;
        double dist     = Math.sin(deg2rad(lat1)) 
                * Math.sin(deg2rad(lat2)) 
                + Math.cos(deg2rad(lat1)) 
                * Math.cos(deg2rad(lat2)) 
                * Math.cos(deg2rad(theta));
        
        dist = Math.acos(dist);
        dist = dist * 180/Math.PI;
        dist = dist * 60 * 1.1515 * 1609.344;
        return dist;
    }
	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}
	public static void main(String[] args) {
		System.out.println(DistanceUtil.distance(49.00439,2.57703, 49.0057827,2.5847271));
	}
}
