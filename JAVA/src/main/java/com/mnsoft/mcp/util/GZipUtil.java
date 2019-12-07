package com.mnsoft.mcp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GZipUtil {
	private final static Logger logger = LoggerFactory.getLogger(GZipUtil.class);
	
	/**
	 * unzip data
	 * @param compressedData
	 * @return byte array
	 */
	public static byte[] unzip(byte[] compressedData) {
		ByteArrayInputStream 		bis 	= new java.io.ByteArrayInputStream(compressedData);
		GZIPInputStream 			gzis	= null;
		ByteArrayOutputStream 		bos 	= null;
		try {
			gzis = new java.util.zip.GZIPInputStream(bis);
		} catch (IOException e) {
			logger.error(e.getMessage());
			for (StackTraceElement element : e.getStackTrace()) {
				logger.error(element.toString());
			}
			return null;
		}
		bos = new java.io.ByteArrayOutputStream();

		int res = 0;
		byte buf[] = new byte[1024];
		try {
			while ((res = gzis.read(buf, 0, buf.length)) != -1) {
			    bos.write(buf, 0, res);
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
			for (StackTraceElement element : e.getStackTrace()) {
				logger.error(element.toString());
			}
			return null;
		} finally {
			try {
				if(bos != null) {
					bos.close();
				}
				if(gzis != null) {
					gzis.close();
				}
				if(bis != null) {
					bis.close();
				}
			}catch(IOException e) {
				logger.error(e.getMessage());
				for (StackTraceElement element : e.getStackTrace()) {
					logger.error(element.toString());
				}
				return null;
			}
		}
		return bos.toByteArray();
	}
	
}