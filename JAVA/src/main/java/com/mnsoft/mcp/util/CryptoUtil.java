package com.mnsoft.mcp.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoUtil {
	private final static Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
	
	/**
	 * make sha1sum hash value
	 * @param data
	 * @return
	 */
	public static String sha1sum(byte[] data) {
		MessageDigest 	sha1 	= null;
		String			hex 	= "";
		try {
			sha1 = MessageDigest.getInstance("SHA1");
			sha1.update(data);
			hex = HexUtil.bytesToHexString(sha1.digest());
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			for (StackTraceElement element : e.getStackTrace()) {
				logger.error(element.toString());
			}
		}
		return hex;
	}
}
