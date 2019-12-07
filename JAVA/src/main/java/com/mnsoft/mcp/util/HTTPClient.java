package com.mnsoft.mcp.util;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPClient {
	private final static Logger logger = LoggerFactory.getLogger(HTTPClient.class);
	private final static int	DEFAULT_TIMEOUT	= 10 * 1000;	/* 10 sec. */
	
	private int requestTimeOut 	= DEFAULT_TIMEOUT;
	private int socketTimeOut	= DEFAULT_TIMEOUT;
	private int	connTimeOut		= DEFAULT_TIMEOUT;
	public HTTPClient() {
	}
	/**
	 * get HTTP response via HTTP get method
	 * @param url
	 * @param headers
	 * @return response entity
	 */
	public String get(String url, Header[] headers) {
		/* request */
		CloseableHttpClient httpClient 	= null; 
		HttpGet 			req 		= null;
		
		/* response */
		HttpResponse 		res			= null; 
		String				result		= null;
		
		RequestConfig config = RequestConfig.custom()
				  .setConnectTimeout(this.connTimeOut)
				  .setConnectionRequestTimeout(this.requestTimeOut)
				  .setSocketTimeout(this.socketTimeOut).build();
		
		httpClient 	= HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		req 		= new HttpGet(url);
		/* appointed headers */
		req.setHeaders(headers);
		
		try  {
			res    = httpClient.execute(req);
			result = EntityUtils.toString(res.getEntity(), "UTF-8");
		}catch(IOException e) {
			logger.error(e.getMessage());
			for (StackTraceElement element : e.getStackTrace()) {
				logger.error(element.toString());
			}
			return null;
		}catch(Exception e) {
			logger.error(e.getMessage());
			for (StackTraceElement element : e.getStackTrace()) {
				logger.error(element.toString());
			}
			return null;
		}finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
				for (StackTraceElement element : e.getStackTrace()) {
					logger.error(element.toString());
				}
			}
		}
		logger.info("[URL] " + url + "[SIZE] " + result.length() + " bytes." + "[SHA1] " + CryptoUtil.sha1sum(result.getBytes()));
		if(res.getStatusLine().getStatusCode() != 200) {
			return null;
		}
		return result;
	}
}