package MFClient.Wrappers;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestUtils {

	/** 
	 * getVaultGuidFromMFServer : Gets Vault GUID for the specified vault name using REST call after authenticating MFServer
	 * @param vaultName - Name of the vault whose GUID is looked for
	 * @return The GUID of the vault without braces
	 */
	public static String getVaultGuidFromMFServer(String vaultName) throws Exception {
		
		try {
		
			//Gets authentication token from MFServer to admit access to the server
			RestAuthenticator restAuthenticator = new RestAuthenticator();
			RestProcessor restProcessor = restAuthenticator.getAuthenticationTokenFromMFServer();
			
			//Reads the REST URL to get the response of server vaults 
			RestResponse restResponse = restProcessor.get("/REST/server/vaults");
			
			// Getting defails of vaults from response as json string
			String vaultsJson =  restResponse.getResponseBody(); //getResponseFromGetRequestToMFServer(url, authToken);
			
			// Mapper used for serializing/deserializing objects/json
			ObjectMapper mapper = new ObjectMapper();
			
			// Deserializing the received json as a list of vault objects
			List<Vault> vaults = mapper.readValue(vaultsJson, new TypeReference<List<Vault>>(){});
			
			// Variable for the vault GUID to be found
			String testVaultGuid = "";
			
			// Going through the vaults to find the correct one
			for(Vault vault : vaults) {
				
				// Name and GUID of a vault in the list
				String name = vault.Name;
				String guid = vault.GUID;
				
				if(vaultName.equals(name)) 	{ // The vault is the one that is looked for
					testVaultGuid = guid; // Save the vault GUID
					break;
				}
			}
			
			if (testVaultGuid.length() > 1) { // Remove braces from the guid to enable using it in vault urls
				
				String testVaultGuidForUrl = testVaultGuid.substring(1, testVaultGuid.length() - 1);
				return testVaultGuidForUrl;
			}
			else { // The vault does not exist -> test class run should be aborted?
				throw new Exception("GUID for vault (" + vaultName + ") is not found");
			}
			
		} //End try
		catch(Exception e) {
			throw new Exception ("Exception at RestUtils.getVaultGuidFromMFServer : " + e);
		} //End catch
		
	} //getVaultGuidFromMFServer
	
} //RestUtils
