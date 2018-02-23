package genericLibrary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;


public final class Shutdown {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException {
		
		if (args[0].trim().equalsIgnoreCase("-selenium")) shutdownSelenium(args.length > 1?args[1]:"localhost");
		if (args[0].trim().equalsIgnoreCase("-genie")) shutdownGenie(args.length > 1?args[1]:"localhost");
		if (args[0].trim().equalsIgnoreCase("-browser")) killBrowsers(args.length > 1?args[1]:"");

	}
	
	public static void shutdownSelenium(String hub) throws InterruptedException, ClientProtocolException, IOException {

		HttpClient client = HttpClientBuilder.create().build();
		@SuppressWarnings("deprecation")
		RequestConfig defaultRequestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.BEST_MATCH).setExpectContinueEnabled(true).setStaleConnectionCheckEnabled(true).setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST)).setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC)).build();
		RequestConfig rc = RequestConfig.copy(defaultRequestConfig).setConnectionRequestTimeout(5000).setConnectTimeout(5000).setSocketTimeout(5000).build(); // to create timeout http requet
		
		if (!hub.contains(":"))
			hub = hub + ":4444";
		
		HttpGet h = new HttpGet("http://" + hub + "/grid/console");
		HttpResponse hr = null;
		
		try {
			 hr = client.execute(h);
		}
		catch(HttpHostConnectException e) {
			if (e.getMessage().toLowerCase().contains("connection refused")) {
				System.out.println("Hub @ " + hub + " is not up at");
				return;
			}
		}
		
		//System.out.println(hr.getStatusLine());
		ResponseHandler <String> r = new BasicResponseHandler();
		String content = r.handleResponse(hr);
		content.matches("<p class='proxyid'>id : (.*), OS");
		String[] nodes = content.split("<p class='proxyid'>id :"); //identify the nodes from grid console
		int reattempt = 10;
		
		for (int i=1; i<nodes.length; i++) { //identify each node and shutdown
			
			client = HttpClientBuilder.create().build();
			String node = nodes[i];
			node = node.substring(0, node.indexOf(",")).trim().toLowerCase();
			
			if (node.contains("localhost"))
				node = node.replace("localhost", hub.substring(0, hub.indexOf(":")));
			
			//shutdown each node
			System.out.println(node);
			h = new HttpGet(node + "/selenium-server/driver/?cmd=shutDownSeleniumServer");
			h.setConfig(rc);
			
			//Find name of the host (system)
			String systemName = node.replace(Integer.toString(h.getURI().getPort()), "");
			systemName = systemName.replace(h.getProtocolVersion().getProtocol().toLowerCase() + "://", "");
			systemName = systemName.replace(":", "");
			killBrowsers(systemName);
		
			try {
				hr = client.execute(h);
				reattempt = 10;
			}
			catch(ConnectionPoolTimeoutException e) { //incase of response time by node, re-attempt 10 times
				Thread.sleep(10000);
				if (reattempt > 0) { i--; reattempt = 10; } 
				reattempt--;
				continue;
			}
			
			catch(HttpHostConnectException e) {
				if (e.getMessage().toLowerCase().contains("connection refused")) {
					System.out.println("Node @ " + node + " is not configured properly");
					continue;
				}
			}
			
			System.out.println("Node - " + node + " shutdown status (" + hr.getStatusLine() + ")");
		}
		
		client = HttpClientBuilder.create().build();
		h = new HttpGet("http://" + hub + "/lifecycle-manager?action=shutdown"); //shutdown hub finally
		h.setConfig(rc);
	
		hr = client.execute(h);
		System.out.println("Hub - " + hub + " shutdown status (" + hr.getStatusLine() + ")");

	}
	
	public static void shutdownGenie(String systemName) {

		String line;
	
		try {
		    Process proc = Runtime.getRuntime().exec("wmic.exe /node:\"" + systemName + "\" process where \"commandLine like '%geniesocketserver.jar%' and executablePath like '%java.exe'\" get ProcessID");
		    BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		    BufferedWriter oStream = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
		    oStream.flush();
		    oStream.close();
		    String processID = "";
		    
		    while ((line = input.readLine()) != null) {
		    	
		    	line = line.trim();
		    	if (line != null && line.matches("[0-9]+"))
		    		processID = line;
		    }
		    input.close();
		    
		    if (processID.isEmpty())
		    	System.out.println("Genie Server is not running in this box");
		    
		    proc.destroy();
		    Runtime.getRuntime().exec("taskkill.exe /s " + systemName + " /PID " + processID + " /T /F");
		    
		} 
		catch (IOException ioe) {
		    ioe.printStackTrace();
		}
		
	}
	
	public static void killBrowsers(String host) throws IOException {
		
		if (host.isEmpty()) host = "localhost";
		List<String> hosts = new ArrayList<String>();
		hosts.add(host);
		
		if (isHub(host)) {
			try {
				hosts.addAll(getNodes(host));
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		for (String hostName : hosts) {
			Runtime.getRuntime().exec("taskkill.exe /s " + hostName + " /IM chromedriver* /T /F");
			Runtime.getRuntime().exec("taskkill.exe /s " + hostName + " /IM chrome* /T /F");
			Runtime.getRuntime().exec("taskkill.exe /s " + hostName + " /IM firefox* /T /F");
			Runtime.getRuntime().exec("taskkill.exe /s " + hostName + " /IM IEDriver* /T /F");
			System.out.println("Terminated the firefox/ie/chrome browsers @ " + hostName);
		}
		
	}
	
	public static boolean isHub(String hub) {
		
		HttpClient client = HttpClientBuilder.create().build();
		
		if (!hub.contains(":"))
			hub = hub + ":4444";
		
		HttpGet h = new HttpGet("http://" + hub + "/grid/console");
		
		try {
			 client.execute(h);
			 return true;
		}
		catch(Exception e) {
			return false;
		}

	}
	
	public static List<String> getNodes(String hub) throws ClientProtocolException, IOException {
		
		List<String> activeNodes = new ArrayList<String>();
		HttpClient client = HttpClientBuilder.create().build();
		
		if (!hub.contains(":"))
			hub = hub + ":4444";
		
		HttpGet h = new HttpGet("http://" + hub + "/grid/console");
		HttpResponse hr = null;
		
		try {
			 hr = client.execute(h);
		}
		catch(HttpHostConnectException e) {
			if (e.getMessage().toLowerCase().contains("connection refused")) {
				System.out.println("Hub @ " + hub + " is not up at");
				return activeNodes;
			}
		}
		
		ResponseHandler <String> r = new BasicResponseHandler();
		String content = r.handleResponse(hr);
		content.matches("<p class='proxyid'>id : (.*), OS");
		String[] nodes = content.split("<p class='proxyid'>id :"); //identify the nodes from grid console
		
		for (int i=1; i<nodes.length; i++) { //identify each node
			
			client = HttpClientBuilder.create().build();
			String node = nodes[i];
			node = node.substring(0, node.indexOf(",")).trim().toLowerCase();
			
			if (node.contains("localhost"))
				node = node.replace("localhost", hub.substring(0, hub.indexOf(":")));

			URL url = new URL(node);
			activeNodes.add(url.getHost());
			
		}
		
		return activeNodes;
		
	}
	
}