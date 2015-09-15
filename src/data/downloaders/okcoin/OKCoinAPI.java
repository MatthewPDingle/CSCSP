package data.downloaders.okcoin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;

public class OKCoinAPI {

	private static OKCoinAPI instance = new OKCoinAPI();
	private static HttpClient client;

	private OKCoinAPI() {
		client = HttpClientBuilder.create().build();
	}

	public static OKCoinAPI getInstance() {
		return instance;
	}

	public HttpClient getHttpClient() {
		return client;
	}

	private HttpPost httpPostMethod(String url) {
		return new HttpPost(url);
	}

	private HttpRequestBase httpGetMethod(String url) {
		return new HttpGet(url);
	}

	public String requestHttpGet(String url_prex, String url, String param) throws HttpException, IOException {
		String r = "";
		try {
			boolean success = false;
			int attempt = 0;
			while (!success) {
				try {
					attempt++;
					url = url_prex + url;
					if (param != null && !param.equals("")) {
						url = url + "?" + param;
					}
					HttpRequestBase method = this.httpGetMethod(url);
					HttpResponse response = client.execute(method);
					HttpEntity entity = response.getEntity();
					if (entity == null) {
						return "";
					}
					InputStream is = null;
					try {
						is = entity.getContent();
						r = IOUtils.toString(is, "UTF-8");
					} 
					finally {
						if (is != null) {
							is.close();
						}
					}
					success = true;
				}
				catch (HttpHostConnectException he) {
					if (attempt > 10) {
						LogManager.getLogger("data.downloaders.okcoin").error("Connection to OKCoin failed.  Aborting.");
						throw he;
					}
					LogManager.getLogger("data.downloaders.okcoin").error(he.getStackTrace().toString());
					Thread.sleep(1000);
				}
			}
		}
		
		catch (Exception e) {
			e.printStackTrace();
		}
		return r;
	}

	public String requestHttpPost(String url_prex, String url, Map<String, String> params) throws HttpException, IOException {
		url = url_prex + url;
		HttpPost method = this.httpPostMethod(url);
		List<NameValuePair> valuePairs = this.convertMap2PostParams(params);
		UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(valuePairs, Consts.UTF_8);
		method.setEntity(urlEncodedFormEntity);
		HttpResponse response = client.execute(method);
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return "";
		}
		InputStream is = null;
		String responseData = "";
		try {
			is = entity.getContent();
			responseData = IOUtils.toString(is, "UTF-8");
		} 
		finally {
			if (is != null) {
				is.close();
			}
		}
		return responseData;
	}

	private List<NameValuePair> convertMap2PostParams(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		if (keys.isEmpty()) {
			return null;
		}
		int keySize = keys.size();
		List<NameValuePair> data = new LinkedList<NameValuePair>();
		for (int i = 0; i < keySize; i++) {
			String key = keys.get(i);
			String value = params.get(key);
			data.add(new BasicNameValuePair(key, value));
		}
		return data;
	}
}
