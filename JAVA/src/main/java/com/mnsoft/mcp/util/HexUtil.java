package com.mnsoft.mcp.util;

public class HexUtil {

	/**
	 * converto byte to hex-string
	 * @param bytes
	 * @return
	 */
	public static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		if(bytes == null) {
			return "";
		}
		for(int i=0; i<bytes.length; i++) {
			sb.append(dumpHex(bytes[i]));
		}
		return sb.toString();
	}
	
	/** Hexadecimal table for fast converting. */
	private static final char[] hexDigits = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	/**
	 * dump hex
	 * @param hex
	 * @return hex
	 */
	public static String dumpHex(byte hex) {
		char[] hexChar = new char[2];
		hexChar[0] = hexDigits[(hex >>> 4) & 0x0F];
		hexChar[1] = hexDigits[hex & 0x0F];
		return new String(hexChar);
	}
	
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
}
