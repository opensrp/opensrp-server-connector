/**
 * 
 */
package org.opensrp.connector;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Samuel Githengi created on 09/10/20
 */
public class HttpUtils {
	
	private static Logger logger = LogManager.getLogger(HttpUtils.class);
	
	public  static String getURL(String url,String username, String password) {
		Request request = new Request.Builder().url(url)
		        .addHeader("Authorization", Credentials.basic(username, password)).build();
		OkHttpClient client = new OkHttpClient();
		Call call = client.newCall(request);
		Response response;
		try {
			response = call.execute();
			String responseBody = response.body().string();
			if (!StringUtils.isBlank(responseBody)) {
				return responseBody;
			}
		}
		catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
		
	}
}
