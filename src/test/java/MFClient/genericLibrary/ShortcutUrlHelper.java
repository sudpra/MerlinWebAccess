package genericLibrary;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;



// Data needed for login request to server
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

// Data for authentication token returned from server
class Token{
	
	public String Value;

}

// Data for vault information returned from server
class Vault{
	
	public String Name;
	public String GUID;
	public String Authentication;
}

// Data for object type information returned from server
class ObjType{
	public String AllowAdding;
	public String CanHaveFiles;
	public String DefaultPropertyDef;
	public String External;
	public String ID;
	public String NamePlural;
	public String Name;
	public String Owner;
	public String OwnerPropertyDef;
	public List<String> ReadOnlyPropertiesDuringInsert;
	public List<String> ReadOnlyPropertiesDuringUpdate;
	public String RealObjectType;
	public String ShowInTaskPane;
	public List<String> objectTypeTargetsForBrowsing;
	public String HasOwner;
	
}

public class ShortcutUrlHelper {

	/**
	 * extractSearchOnlyObjectType : Extracts the plural name of the object type from the string
	 * @param searchOnlyString - String in format "Search only: Documents"
	 * @return The plural name of the object type in string parameter. In the case of example: "Documents"
	 */
	public static String extractSearchOnlyObjectType(String searchOnlyString)
	{
		// searchOnlyString should be in format "Search only: Documents"
		String[] parts = searchOnlyString.split(":");
		
		String searchingFor = null;
		
		// Checking that the split succeeded and that the second half has something in it
		if(parts.length > 1 && parts[1].length() > 1)
		{
			// Getting rid of the space character in front of second part
			searchingFor = parts[1].substring(1);
		}
		
		return searchingFor;
	}

	/**
	 * convertSearchString : Converts the parameter string to hexadecimal values of its bytes.
	 * In short, it encodes the search string to another format that can be used in M-Files Web URL
	 * @param searchString - A search word or phrase in string format
	 * @return The hexadecimal encoded byte format of the parameter string
	 */
	public static String convertSearchString(String searchString) throws UnsupportedEncodingException
	{
		// Converting searchString to a byte array
		byte[] bytes =  searchString.getBytes("UTF-8");
		
		System.out.println("Here are the bytes: " + bytes);
		
		// Getting the hexadecimal values of the bytes as a single string
		String test = Hex.encodeHexString(bytes);
		String stringToReturn = "";
		
		// Adding "_p" before each byte to form up the search string
		// For example string "test_" will be in format: _p74_p65_p73_p74_p5f
		for(int i = 0; i < bytes.length; ++i)
		{
			// Hex bytes have length of 2 characters each -> bytes starting from indices 0, 2, 4,...
			int start = i * 2;
			int end = start + 2;
			stringToReturn = stringToReturn + "_p" + test.substring(start, end);	
		}
		
		return stringToReturn;
	}
	
	/**
	 * makeSearchUrl : Forms an URL to make a search in M-Files Web
	 * @param webHost - IP address or domain name of the computer hosting M-Files Web
	 * @param searchString - Search word or phrase as a string
	 * @param testVaultGuid - GUID of the vault without the braces
	 * @param searchOnlyObjectTypeString - Search only object type in format: "Search only: Documents".
	 * Empty means searching for all object types.
	 * @param objectTypeIdsByName - A ConcurrentHashMap containing object type plural names as keys and their ids as values.
	 * @return Fully usable URL to get search results from M-Files Web
	 */
	public static String makeSearchUrl(String webHost, String searchString, String testVaultGuid, String searchOnlyObjectTypeString, ConcurrentHashMap <String, String> objectTypeIdsByName) throws Exception
	{
		try
		{
			String webHostAddress = webHost;
			// A Finished quicksearch URL should be in this format:
			// http://webhost/Default.aspx?#83808D0B-1B3D-43A0-BE81-6E29065214FC/views/_tempsearch?limit=50&0_qba=searchforthis&0_o=136
			
			String url = "";
			
			// Quicksearch url requires the parameters to not be null
			if(webHostAddress != null && testVaultGuid != null)
			{
			
			// The beginning of the quicksearch url
				url = "http://" + webHostAddress + "/Default.aspx?#" + testVaultGuid + "/views/_tempsearch?limit=50"; 
			}
			else
			{
				throw new NullPointerException("ShortcutUrlHelper can not form URL because of a null parameter.");
			}

			try
			{
				// Checking if the search string contains characters
				if(searchString.length() > 0)
				{
					// Adding search string parameter to the url encoded as utf-8 hexadecimal values 
					// (that's how MFWA understands special characters)
					url = url + "&0_qba=" + convertSearchString(searchString);
				}
			
			}
			catch(NullPointerException e1){
				
				// searchString is null -> it is regarded as empty
			}
			
			try
			{
				
				// Checking if the string has any content
				if(searchOnlyObjectTypeString != null && searchOnlyObjectTypeString.length() > 0)
				{
					// Extracting the plural name of the object type, that is searched for, from the test data string
					String searchOnlyObjectType = extractSearchOnlyObjectType(searchOnlyObjectTypeString);
					
					// Finding out the id of the object type from the map using the name as key
					String objectTypeId = objectTypeIdsByName.get(searchOnlyObjectType);
					
					// Checking if the object type was present in the map
					if(objectTypeId != null)
					{
						// Adding the object type parameter to the url
						url = url + "&0_o=" + objectTypeId;
					}
				}
			}
			catch(NullPointerException e2){
				
				// objectTypeIdsByName doesn't have key for the object type.
				throw e2;
			}
				
			return url;
				
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	/**
	 * getObjectTypeIdsByPluralNameFromVault : Logs in to the vault with provided username and password. Then requests
	 * information of object types in the vault by using REST API.
	 * @param vaultGuid - GUID of the vault with {braces}
	 * @param username - Username of the user that requests information
	 * @param password - Password of the user
	 * @param webHost - IP address or domain name of the computer hosting M-Files Web
	 * @param isWindowsUser - Boolean if the user is Windows user
	 * @return A ConcurrentHashMap containing object types of the vault with plural names as keys and their ids as values.
	 */
	public static ConcurrentHashMap<String, String> getObjectTypeIdsByPluralNameFromVault(String vaultGuid, String username, String password, String webHost, Boolean isWindowsUser) throws Exception
	{
		try{
			
			// Logging in to to vault and getting authentication token
			String vaultAuthToken = getAuthenticationTokenFromVault(username, password, isWindowsUser, webHost, vaultGuid);
			
			// Url to get information from object types in vault
			String url = "http://" + webHost + "/REST/structure/objecttypes";
			
			String objectTypesJson = getResponseFromGetRequestToMFServer(url, vaultAuthToken);
			
			// Mapper used for serializing/deserializing objects/json
			ObjectMapper mapper = new ObjectMapper();
			
			// Deserializing the object types from json to a list of objects
			List<ObjType> objectTypes = mapper.readValue(objectTypesJson, new TypeReference<List<ObjType>>(){});
			
			// Map for saving the ids of object types using the plural name of the object type as key
			ConcurrentHashMap <String, String> objectTypeIdsByName = new ConcurrentHashMap <String, String>();
			
			// Going through all object types
			for(ObjType objectType : objectTypes)
			{
				// Plural name as key
				String nameToAdd = objectType.NamePlural;
				
				// Id as value
				String idToAdd = objectType.ID;

				objectTypeIdsByName.put(nameToAdd, idToAdd);
			}
			
			return objectTypeIdsByName;
			
		}
		catch(Exception e)
		{
			throw e;
		}
		
	}
	
	/**
	 * getVaultGuidFromMFServer : Logs in to the server with provided username and password. Then requests
	 * information of vaults using REST API.
	 * @param vaultName - Name of the vault whose GUID is looked for
	 * @param username - Username of the user that requests information
	 * @param password - Password of the user
	 * @param webHost - IP address or domain name of the computer hosting M-Files Web
	 * @param isWindowsUser - Boolean if the user is Windows user
	 * @return The GUID of the vault without braces
	 */
	public static String getVaultGuidFromMFServer(String vaultName, String username, String password, Boolean isWindowsUser, String webHost) throws Exception
	{
		
		try{
			// Logging in to server to get the authentication token
			String authToken = getAuthenticationTokenFromMFServer(webHost, username, password, isWindowsUser);
			
			// Url for getting vault related data
			String url = "http://" + webHost + "/REST/server/vaults";
			
			// Getting defails of vaults from response as json string
			String vaultsJson = getResponseFromGetRequestToMFServer(url, authToken);
			
			// Mapper used for serializing/deserializing objects/json
			ObjectMapper mapper = new ObjectMapper();
			
			// Deserializing the received json as a list of vault objects
			List<Vault> vaults = mapper.readValue(vaultsJson, new TypeReference<List<Vault>>(){});
			
			// Looking for the GUID of the vault used in this test
			String lookingForVault = vaultName;
			
			// Variable for the vault GUID to be found
			String testVaultGuid = "";
			
			// Going through the vaults to find the correct one
			for(Vault vault : vaults)
			{
				// Name and GUID of a vault in the list
				String name = vault.Name;
				String guid = vault.GUID;
				
				// The vault is the one that is looked for
				if(lookingForVault.equals(name))
				{
					// Save the vault GUID
					testVaultGuid = guid;
					Log.message("The GUID for the test vault on this test class is " + guid);
				}
			}
			
			// Checking if a guid has been found
			if(testVaultGuid.length() > 1)
			{
				// Remove braces from the guid to enable using it in vault urls
				String testVaultGuidForUrl = testVaultGuid.substring(1, testVaultGuid.length() - 1);
				return testVaultGuidForUrl;
			}
			else
			{
				// The vault does not exist -> test class run should be aborted?
				throw new Exception("ShortcutUrlHelper was not able to find GUID for the test vault " + vaultName);
			}
			
		}
		catch(Exception e)
		{
			throw e;
		}
		
	}
	
	/**
	 * getAuthenticationTokenFromMFServer : Logs in to the server with provided username and password to get authentication token.
	 * @param username - Username of the user that requests information
	 * @param password - Password of the user
	 * @param webHost - IP address or domain name of the computer hosting M-Files Web
	 * @param isWindowsUser - Boolean if the user is Windows user
	 * @return Authentication token string
	 */
	public static String getAuthenticationTokenFromMFServer(String webHost, String username, String password, Boolean isWindowsUser) throws Exception
	{
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
		
		if(isWindowsUser)
		{
			requestObject.WindowsUser = "true";
		}
		else
		{
			requestObject.WindowsUser = "false";
		}
		
		try{
		
			// Serializing object to json
			String json = mapper.writeValueAsString(requestObject);
			StringEntity params = new StringEntity(json);
		
			httppost.addHeader("content-type", "application/json");
		
			// Setting json parameter containing login data as the body of http POST
			httppost.setEntity(params);
		
			// Sending the request and getting the response
			CloseableHttpResponse response = httpclient.execute(httppost);
		
			try{
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
			}
			catch(Exception e1)
			{
				throw e1;
			}
			finally
			{
				// Closing open response before exiting method
				response.close();
			}
		}
		catch(Exception e)
		{
			throw e;
		}
	
	}
	
	/**
	 * getAuthenticationTokenFromVault : Logs in to the vault with provided username and password to get authentication token.
	 * @param username - Username of the user that requests information
	 * @param password - Password of the user
	 * @param webHost - IP address or domain name of the computer hosting M-Files Web
	 * @param isWindowsUser - Boolean if the user is Windows user
	 * @param vaultGuid - GUID of the vault with {braces}
	 * @return Authentication token string
	 */
	public static String getAuthenticationTokenFromVault(String username, String password, Boolean isWindowsUser, String webHost, String vaultGuid) throws Exception
	{
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
				
		if(isWindowsUser)
		{
			requestObject.WindowsUser = "true";
		}
		else
		{
			requestObject.WindowsUser = "false";
		}
				
		try{
				
			// Serializing object to json
			String json = mapper.writeValueAsString(requestObject);
			StringEntity params = new StringEntity(json);
				
			httppost.addHeader("content-type", "application/json");
				
			// Setting json parameter containing login data as the body of http POST
			httppost.setEntity(params);
				
			// Sending the request and getting the response
			CloseableHttpResponse response = httpclient.execute(httppost);
				
			try{
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
			}
			catch(Exception e1)
			{
				throw e1;
			}
			finally
			{
				// Closing open response before exiting method
				response.close();
			}
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	/**
	 * getResponseFromGetRequestToMFServer : Make a HTTP GET request to the server or vault.
	 * @param url - URL of the request
	 * @param authenticationToken - Authentication token string
	 * @return Response (usually json) as a string
	 */
	public static String getResponseFromGetRequestToMFServer(String url, String authenticationToken) throws Exception
	{
		
		try{
			// HttpClient to communicate with REST API
			CloseableHttpClient httpclient = HttpClients.createDefault();
		
			// Initializing a new GET request for vaults on the server
			HttpGet httpget = new HttpGet(url);
		
			// Adding authentication token to the request
			httpget.addHeader("X-Authentication", authenticationToken);
			httpget.addHeader("content-type", "application/json");
		
			// Sending the GET request for the vaults and getting the response
			CloseableHttpResponse response = httpclient.execute(httpget);
			//responsesToBeClosed.add(response);
			
			try{
				// Response as an InputStream
				InputStream responseBody = response.getEntity().getContent();
			
				// Constructing a string out of the response
				StringWriter writer = new StringWriter();
				Charset charset = Charset.forName("UTF-8");
				IOUtils.copy(responseBody, writer, charset);
				String jsonString = writer.toString();
				return jsonString;
			
			}
			catch(Exception e)
			{
				throw e;
			}
			finally
			{
				// Closing open response before exiting method
				response.close();
			}
			
		}
		catch(Exception e1)
		{
			throw e1;
		}
	}
	
}
