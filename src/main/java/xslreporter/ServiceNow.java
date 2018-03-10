package xslreporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ServiceNow {

	private final URL url;
	private final HttpHost host;
	private final AuthScope authScope;
	private UsernamePasswordCredentials userPassCreds;
	private CredentialsProvider credsProvider = null;
	final private BasicCookieStore cookieStore = new BasicCookieStore();
	
	CloseableHttpClient client;

	ServiceNow(File propfile) throws IOException {
		this(getProperties(propfile));
	}
	
	static Properties getProperties(File propfile) throws IOException {
		Properties props = new Properties();
		props.load(new InputStreamReader(new FileInputStream(propfile)));
		return props;
	}
	
	ServiceNow(Properties props) {
		this(props.getProperty("servicenow.instance"), 
				props.getProperty("servicenow.username"), 
				props.getProperty("servicenow.password"));
	}
	
	ServiceNow(String instance, String username, String password) {
		this(getURL(instance), username, password);		
	}
		
	ServiceNow(URL url, String username, String password) {
		assert url != null;
		assert username != null;
		assert password != null;
		assert url.toString().endsWith("/");
		this.url = url;
		this.host = new HttpHost(url.getHost());
		this.authScope = new AuthScope(host);
		setCredentials(username, password);
	}

	private void setCredentials(String username, String password) {
		assert username != null;
		this.credsProvider = new BasicCredentialsProvider();
		this.userPassCreds = new UsernamePasswordCredentials(username, password);
		this.credsProvider.setCredentials(this.authScope, this.userPassCreds);
	}
		
	CloseableHttpClient getClient() {
		assert this.credsProvider != null;
		assert this.cookieStore != null;
		if (this.client == null) {
			this.client = HttpClients.custom().
					setDefaultCredentialsProvider(credsProvider).
					setDefaultCookieStore(cookieStore).
					build();			
		}
		return this.client;
	}

	URI getURI(String path) {
		return getURI(path, null);
	}
		
	URI getURI(String path, List<NameValuePair> params) {
		assert path != null;
		assert path.length() > 0;
		URI result;
		try {
			String base = url.toString() + path;
			URIBuilder builder = new URIBuilder(base);
			if (params != null) builder.addParameters(params);
			result = builder.build();			
		}
		catch (URISyntaxException e) {
			throw new AssertionError(e);
		}
		return result;
	}
		
	private static URL getURL(String name) {
		if (name == null || name.length() == 0)
			throw new AssertionError("Instance URL or name not provided");
		if (name.matches("[\\w-]+")) {
			// name is the instance name; build the URL
			try {
				return new URL("https://" + name + ".service-now.com/");
			} catch (MalformedURLException e) {
				throw new AssertionError(e);
			}			
		}
		if (name.startsWith("https://")) {
			// name is the the full URL
			// make sure it ends with a slash
			if (!name.endsWith("/")) name += "/";
			try {
				return new URL(name);
			} catch (MalformedURLException e) {
				throw new AssertionError(e);
			}			
		}
		throw new AssertionError("Instance URL not valid: " + name);
	}
	
	
}
