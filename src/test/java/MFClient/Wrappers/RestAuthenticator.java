package MFClient.Wrappers;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.testng.Reporter;
import org.testng.xml.XmlTest;

import com.fasterxml.jackson.databind.ObjectMapper;

//Data needed for login request to server
class LoginRequest {
	public String Username;
	public String Password;
	public String WindowsUser;
}

//Data needed for login request to vault
class VaultLoginRequest {
	public String Username;
	public String Password;
	public String WindowsUser;
	public String VaultGuid;
}

//Data for authentication token returned from server
class Token{
	
	public String Value;

}

//Data for vault information returned from server
class Vault{
	
	public String Name;
	public String GUID;
	public String Authentication;
}

public class RestAuthenticator {
	
	private String webHost;
	private String webProtocol;
	private String userName;  
	private String password;
	private Boolean isWindowsUser; 
	private String url;
	
	public RestAuthenticator() {
	
		//Gets the parameters from xml for authenticating server
		XmlTest xmlParameters = Reporter.getCurrentTestResult().getTestContext().getCurrentXmlTest();
		this.webHost = xmlParameters.getParameter("WebHost");
		this.webProtocol = xmlParameters.getParameter("WebProtocol");
		this.userName = xmlParameters.getParameter("UserName");
		this.password = xmlParameters.getParameter("Password");
		this.isWindowsUser = (xmlParameters.getParameter("WindowsUser").equalsIgnoreCase( "yes" ) ? true : false) ;
		this.url = this.webProtocol + "://" + this.webHost;
	}
	
	/** 
	 * getAuthenticationTokenFromMFServer : Logs in to the server with username and password from XML to get authentication token.
	 * @param None
	 * @return Object of RestProcessor
	 */
	public RestProcessor getAuthenticationTokenFromMFServer() throws Exception {
		
		try {
		
			return new RestProcessor(this.url, 
					this.getAuthenticationTokenFromMFServer( 
							this.webHost, this.userName, this.password, this.isWindowsUser ));
			
		} //End try
		
	 	catch(Exception e) {
	 		throw new Exception("Exception at RestAuthenticator.getAuthenticationTokenMFServer : " + e);
		} //End catch
	
	} //End getAuthenticationTokenMFServer
	
	/** 
	 * getAuthenticationTokenFromMFServer : Logs in to the server with provided username and password to get authentication token.
	 * @param username - Username of the user that requests information
	 * @param password - Password of the user
	 * @param webHost - IP address or domain name of the computer hosting M-Files Web
	 * @param isWindowsUser - Boolean if the user is Windows user
	 * @return Authentication token string
	 */
	public String getAuthenticationTokenFromMFServer(String webHost, String username, String password, Boolean isWindowsUser) throws Exception {
		
		CloseableHttpResponse response = null;
		
		try {
		
			// HttpClient to communicate with REST API
			CloseableHttpClient httpclient = HttpClients.createDefault();
						
			// Initializing POST method to connect to the server and get the authentication token
			HttpPost httppost = new HttpPost("http://" + webHost + "/REST/server/authenticationtokens");
			
			// Mapper used for serializing/deserializing objects/json
			ObjectMapper mapper = new ObjectMapper();
			
			// Object for login request
			LoginRequest requestObject = new LoginRequest();

			// Object for login request
			//LoginRequest requestObject = new LoginRequest();
			requestObject.Username = username;
			requestObject.Password = password;
			requestObject.WindowsUser = isWindowsUser ?  "true" : "false";
			
			// Serializing object to json
			String json = mapper.writeValueAsString(requestObject);
			StringEntity params = new StringEntity(json);
		
			httppost.addHeader("content-type", "application/json");
		
			// Setting json parameter containing login data as the body of http POST
			httppost.setEntity(params);
		
			// Sending the request and getting the response
			response = httpclient.execute(httppost);
		
			// Response as an InputStream
			InputStream responseBody = response.getEntity().getContent();
			
			// Constructing a string out of the response
			StringWriter writer = new StringWriter();
			Charset charset = Charset.forName("UTF-8");
			IOUtils.copy(responseBody, writer, charset);
			String token = writer.toString();
	
			// Deserializing the response authentication token json string as an object
			Token tokenObject = mapper.readValue(token, Token.class);
			
			// Return the authentication token string
			return tokenObject.Value;
			//return new RestProcessor (url, tokenObject.Value);
	
		} //End try
		
	 	catch(Exception e) {
	 		throw new Exception("Exception at RestAuthenticator.getAuthenticationTokenMFServer : " + e);
		} //End catch
		
		finally {
			response.close();
		}
	
	} //End getAuthenticationTokenMFServer
	
	/**
	 * getAuthenticationTokenFromVault : Logs in to the vault with username and password from XML to get authentication token.
	 * @param vaultGuid - GUID of the vault with {braces}
	 * @return Object of RestProcessor
	 */
	public RestProcessor getAuthenticationTokenFromVault(String vaultGuid) throws Exception {
				
		try{
			
			return new RestProcessor(this.url, 
					this.getAuthenticationTokenFromVault( 
							this.webHost, this.userName, this.password, this.isWindowsUser, vaultGuid ));
			
		} //End try
		
	 	catch(Exception e) {
	 		throw new Exception("Exception at RestAuthenticator.getAuthenticationTokenFromVault : " + e);
		} //End catch
		
	} //End getAuthenticationTokenFromVault
	
	/**
	 * getAuthenticationTokenFromVault : Logs in to the vault with provided username and password to get authentication token.
	 * @param webHost - IP address or domain name of the computer hosting M-Files Web
	 * @param username - Username of the user that requests information
	 * @param password - Password of the user
	 * @param isWindowsUser - Boolean if the user is Windows user
	 * @param vaultGuid - GUID of the vault with {braces}
	 * @return Authentication token string
	 */
	public String getAuthenticationTokenFromVault(String webHost, String username, String password, Boolean isWindowsUser, String vaultGuid) throws Exception {
				
		CloseableHttpResponse response = null;
		
		try {
			
			// HttpClient to communicate with REST API
			CloseableHttpClient httpclient = HttpClients.createDefault();
								
			// Initializing POST method to connect to the vault and get the authentication token
			HttpPost httppost = new HttpPost("http://" + webHost + "/REST/server/authenticationtokens");
					
			// Mapper used for serializing/deserializing objects/json
			ObjectMapper mapper = new ObjectMapper();
					
			// Object for login request
			VaultLoginRequest requestObject = new VaultLoginRequest();

			// Object for login request
			//LoginRequest requestObject = new LoginRequest();
			requestObject.Username = username;
			requestObject.Password = password;
			requestObject.VaultGuid = vaultGuid;
			requestObject.WindowsUser = isWindowsUser ?  "true" : "false";
			
			// Serializing object to json
			String json = mapper.writeValueAsString(requestObject);
			StringEntity params = new StringEntity(json);
				
			httppost.addHeader("content-type", "application/json");
				
			// Setting json parameter containing login data as the body of http POST
			httppost.setEntity(params);
				
			// Sending the request and getting the response
			response = httpclient.execute(httppost);
				
			// Response as an InputStream
			InputStream responseBody = response.getEntity().getContent();
	
			// Constructing a string out of the response
			StringWriter writer = new StringWriter();
			Charset charset = Charset.forName("UTF-8");
			IOUtils.copy(responseBody, writer, charset);
			String token = writer.toString();
			
			// Deserializing the response authentication token json string as an object
			Token tokenObject = mapper.readValue(token, Token.class);
					
			// Return the authentication token string
			return tokenObject.Value;
			
		} //End try
		
	 	catch(Exception e) {
	 		throw new Exception("Exception at RestAuthenticator.getAuthenticationTokenFromVault : " + e);
		} //End catch

		finally {
			response.close();
		}
	
		
	} //End getAuthenticationTokenFromVault
	
} //End RestAuthenticator
