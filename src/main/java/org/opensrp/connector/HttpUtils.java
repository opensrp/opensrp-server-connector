/**
 * 
 */
package org.opensrp.connector;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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
