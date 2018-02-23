package MFClient.Wrappers;

import java.util.HashMap;
import java.util.Set;

public class RestValidator {

	private RestResponse response;

	public RestValidator(RestResponse response) {
		this.response = response;
	}

	public Boolean verifyResponse(String strExpectedCode, String header, String message, String content) throws Exception {
		
		try {
			
			
			HashMap<String, String> headers = new HashMap<String, String>();
			String headerArray[] = header.split( "\n" );
			
			for (int i=0; i<headerArray.length; i++)
				headers.put( headerArray[i].split("::")[0], headerArray[i].split("::")[1] );

			Set<String> keys = headers.keySet();
			String status = "";
			
			for (String key : keys) {
				if (headers.get(key).equals( response.getHeader( key ) ))
					status += "true";
				else
					status += "false";
			}
			
			int expectedCode = Integer.parseInt( strExpectedCode );
			
			return (
					(expectedCode == response.getResponseCode() ? true :  false) &&
					(message.equals( response.getResponseMessage() ) ? true :  false) &&
					(status.contains( "false" ) ? false :  true) &&
					(response.getResponseBody().contains(content) ? true :  false)
					);
		} //End try
		
		catch (Exception e) {
			 throw new Exception("Exception at RestValidator.response : " + e);
		} //End catch
		
	} //End verifyResponse
	
	public String response() throws Exception {
	
		try {
			
			String restResponseValue = "<br /><br />Response Code : " + response.getResponseCode();
			restResponseValue = restResponseValue + "<br />Response Message : " + response.getResponseMessage();
			restResponseValue = restResponseValue + "<br />Response Headers : " + response.getHeaders().toString();
			restResponseValue = restResponseValue + "<br />Response Body : " + response.getResponseBody();
			
			return restResponseValue;
		 } //End try
		
		 catch (Exception e) {
			 throw new Exception("Exception at RestValidator.response : " + e);
		 } //End catch
		
	} //End response
}