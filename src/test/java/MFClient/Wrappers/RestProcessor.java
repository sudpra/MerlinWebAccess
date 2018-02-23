package MFClient.Wrappers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

public class RestProcessor {

	private HttpClient client;
	private String url;
	private String authToken;
	
	/**
	 * Constructor for RestExecutor
	 * @param url
	 */
	public RestProcessor(String url, String authToken) {
		
		this.client = HttpClientBuilder.create().build();
		this.url = url;
		this.authToken = authToken;
	}

	/**
	 * get : Executes HttpGet request and returns response json
	 * 
	 * @param restURL Rest URL to be get response
	 * @return RestResponse Response as object
	 * @throws Exception
	 */
	public RestResponse get(String restURL) throws Exception {
		
		RestResponse restResponse = new RestResponse();
		
		try {
			
			//Creates HttpGet request for the URL and stores the Http response
			HttpGet httpGet = new HttpGet(url + restURL);
			
			//Adds Authentication token & content type headers to get request
			httpGet.addHeader( "X-Authentication", this.authToken );
			httpGet.addHeader("content-type", "application/json");
			
			//Executes the HTTP GET request and stores its response
			HttpResponse httpResponse = client.execute(httpGet);
			
			//Converts Http get request content into a buffer reader
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String line = "";
			StringBuffer responseString = new StringBuffer();
			
			//Stores response content as a string
			while ((line = rd.readLine()) != null) {
				responseString.append(line);
			}
			
			//Sets Http get response to the Rest Response object that helps easy reterival of rest response
			restResponse.setResponseBody(responseString.toString());
			restResponse.setResponseCode(httpResponse.getStatusLine().getStatusCode());
			restResponse.setResponseMessage(httpResponse.getStatusLine().getReasonPhrase());
			Header[] rheaders = httpResponse.getAllHeaders();
		
			for (Header header : rheaders) {
				restResponse.setHeader(header.getName(), header.getValue());
			}
				
		} //End try
		
		catch (Exception e) {
			throw new Exception("Exception at RestProcessor.get : " + e);
		} //End catch
		
		return restResponse;
		
	} //End get

	/**
	 * post : Executes HttpPost request and returns response json
	 * 
	 * @param restURL Rest URL to be post content
	 * @param xmlContent XML content to be posted on HttpPost
	 * @return RestResponse Response as object
	 * @throws Exception
	 */
	public RestResponse post(String restURL, String xmlContent) throws Exception {
		
		RestResponse restResponse = new RestResponse();
		
		try {
			
			//Creates HttpPost request for the URL and stores the Http response
			HttpPost httpPost = new HttpPost(url + restURL);
			
			//Adds Authentication token & content type headers to post request
			String contentType = "application/json";
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put( "X-Authentication", this.authToken );
			headers.put("content-type", "application/json");
			httpPost.setEntity(getEntities(headers));
			
			//Setting the xml content and content type.
			StringEntity input = new StringEntity(xmlContent);
			input.setContentType(contentType);
			httpPost.setEntity(input);

			//Executes HTTP POST request and stores its response
			HttpResponse httpResponse = client.execute(httpPost);
			
			//Stores Http Response as buffer reader to have formatted output
			BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String line = "";
			StringBuffer responseString = new StringBuffer();
			
			//Stores http response body as a string
			while ((line = rd.readLine()) != null) {
				responseString.append(line);
			}
			
			//Sets Http post response to the Rest Response object that helps easy reterival of rest response
			restResponse.setResponseBody(responseString.toString());
			restResponse.setResponseCode(httpResponse.getStatusLine().getStatusCode());
			restResponse.setResponseMessage(httpResponse.getStatusLine().getReasonPhrase());
			Header[] rheaders = httpResponse.getAllHeaders();

			for (Header header : rheaders) {
				restResponse.setHeader(header.getName(), header.getValue());
			}
		
		} //End try 
		
		catch (Exception e) {
			throw new Exception("Exception at RestProcessor.post : " + e);
		} //End catch
		
		return restResponse;
		
	} //End post

	/**
	 * delete : Executes HttpDelete request and returns response json
	 * 
	 * @param restURL Rest URL to be perform Delete request
	 * @return RestResponse Response as object
	 * @throws Exception
	 */
	public RestResponse delete(String restURL) throws Exception {
		
		RestResponse restResponse = new RestResponse();
		
		try {
			
			//Creates HttpDelete request for the URL and stores the Http response
			HttpDelete httpDelete = new HttpDelete(url + restURL);
			
			//Adds Authentication token & content type headers to get request
			httpDelete.addHeader( "X-Authentication", this.authToken );
			httpDelete.addHeader("content-type", "application/json");
			
			//Executes the HTTP GET request and stores its response
			HttpResponse httpResponse = client.execute(httpDelete);
			
			//Converts Http get request content into a buffer reader
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String line = "";
			StringBuffer responseString = new StringBuffer();
			
			//Stores response content as a string
			while ((line = rd.readLine()) != null) {
				responseString.append(line);
			}
			
			//Sets Http get response to the Rest Response object that helps easy reterival of rest response
			restResponse.setResponseBody(responseString.toString());
			restResponse.setResponseCode(httpResponse.getStatusLine().getStatusCode());
			restResponse.setResponseMessage(httpResponse.getStatusLine().getReasonPhrase());
			Header[] rheaders = httpResponse.getAllHeaders();
		
			for (Header header : rheaders) {
				restResponse.setHeader(header.getName(), header.getValue());
			}
				
		} //End try
		
		catch (Exception e) {
			throw new Exception("Exception at RestProcessor.delete : " + e);
		} //End catch
		
		return restResponse;
		
	}
	
	/**
	 * put : Executes HttpPut request and returns response json
	 * 
	 * @param restURL Rest URL to be perform put request
	 * @param xmlContent XML content to be posted on HttpPut
	 * @return RestResponse Response as object
	 * @throws Exception
	 */
	public RestResponse put(String restURL, String xmlContent) throws Exception {
		
		RestResponse restResponse = new RestResponse();
		
		try {
			
			//Creates HttpPut request for the URL and stores the Http response
			HttpPut httpPut = new HttpPut(url + restURL);
			
			//Adds Authentication token & content type headers to post request
			String contentType = "application/json";
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put( "X-Authentication", this.authToken );
			headers.put("content-type", "application/json");
			httpPut.setEntity(getEntities(headers));
			
			//Setting the xml content and content type.
			StringEntity input = new StringEntity(xmlContent);
			input.setContentType(contentType);
			httpPut.setEntity(input);

			//Executes HTTP PUT request and stores its response
			HttpResponse httpResponse = client.execute(httpPut);
			
			//Stores Http Response as buffer reader to have formatted output
			BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			
			String line = "";
			StringBuffer responseString = new StringBuffer();
			
			//Stores http response body as a string
			while ((line = rd.readLine()) != null) {
				responseString.append(line);
			}
			
			//Sets Http put response to the Rest Response object that helps easy reterival of rest response
			restResponse.setResponseBody(responseString.toString());
			restResponse.setResponseCode(httpResponse.getStatusLine().getStatusCode());
			restResponse.setResponseMessage(httpResponse.getStatusLine().getReasonPhrase());
			Header[] rheaders = httpResponse.getAllHeaders();

			for (Header header : rheaders) {
				restResponse.setHeader(header.getName(), header.getValue());
			}
		
		} //End try 
		
		catch (Exception e) {
			throw new Exception("Exception at RestProcessor.put : " + e);
		} //End catch
		
		return restResponse;
		
	} //End put

	/**
	 * getEntities : Gets the hashmap turns it in HttpEntity nameValuePair.
	 * 
	 * @param inputEntities Input entity headers
	 * @return HttpEntity to set for Put/Post methods
	 * @throws Exception 
	 */
	private HttpEntity getEntities(HashMap<String, String> inputEntities) throws Exception {
		
		try {
		
			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(inputEntities.size());
			Set<String> keys = inputEntities.keySet();
			
			for (String key : keys)
				nameValuePairs.add(new BasicNameValuePair(key, inputEntities.get(key)));
			
			return new UrlEncodedFormEntity(nameValuePairs);
			
		} //End try
		
		catch (Exception e) {
			throw new Exception("Exception at RestProcessor.put : " + e);
		} //End catch

	} //End getEntities

} //End RestProcessor