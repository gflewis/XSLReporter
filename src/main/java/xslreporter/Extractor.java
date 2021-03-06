package xslreporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

public class Extractor {

	public enum Format {REST, XML, UNLOAD};
	
	static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	static TransformerFactory transformerFactory = TransformerFactory.newInstance();
	
	final ServiceNow sn;
	Format format = Format.REST;
	
	final CloseableHttpClient client;
	final javax.xml.parsers.DocumentBuilder builder; 
	final javax.xml.transform.Transformer transformer;	

	HttpRequestBase request;
	CloseableHttpResponse response;	
	
	Extractor(ServiceNow sn) throws ParserConfigurationException, TransformerException {
		this.sn = sn;
		this.client = sn.getClient();
		builder = builderFactory.newDocumentBuilder();
		transformer = transformerFactory.newTransformer();
	}
	
	void setFormat(Format format) {
		this.format = format;
	}

	URI getURI(String tablename, String query) {
		String path;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		switch (this.format) {
		case REST:
			// REST Table API
			path = "api/now/table/" + tablename;
			params.add(new BasicNameValuePair("sysparm_display_value", "all"));
			if (query != null) 
				params.add(new BasicNameValuePair("sysparm_query", query));			
			break;
		case XML:
			// XML API
			path = tablename + ".do?XML";
			if (query != null) 
				params.add(new BasicNameValuePair("sysparm_query", query));			
			break;
		case UNLOAD:
			// Unload Format
			path = tablename + ".do?XML";
			params.add(new BasicNameValuePair("useUnloadFormat", "true"));
			if (query != null)	
				params.add(new BasicNameValuePair("sysparm_query", query));			
			break;
		default:
			throw new AssertionError();
		}
		return sn.getURI(path, params);
		
	}
	
	void extract(String tablename, String query, File output) 
			throws IOException, SAXException {
		URI uri = getURI(tablename, query);
		request = new HttpGet(uri);			
		request.setHeader("Accept", "application/xml");
		// System.out.println("extract " + uri.toString());
		response = client.execute(request);		
		// System.out.println("execute complete");
		StatusLine statusLine = response.getStatusLine();
		// System.out.println(statusLine);
		int statusCode = statusLine.getStatusCode();
		assert statusCode == 200;
		HttpEntity responseEntity = response.getEntity();
		InputStream inStream = responseEntity.getContent();
		FileOutputStream outStream = new FileOutputStream(output);
		IOUtils.copy(inStream,  outStream);
		response.close();
	}
	
	/*
	void closeRequest() throws IOException {
		response.close();
		request.releaseConnection();
	}
	
	String asString(Document doc) throws TransformerException  {
		DOMSource domSource = new DOMSource(doc);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(domSource, result);
		return writer.toString();
	}
	
	public static void main(String[] args) throws Exception {
		File profile = new File(args[0]);
		String tablename  = args[1];
		String query = args.length > 2 ? args[2] : null;
		ServiceNow sn = new ServiceNow(profile);
		Extractor ex = new Extractor(sn);
		Document doc = ex.extract(tablename,  query);
		ex.transformer.transform(new DOMSource(doc), new StreamResult(System.out));
	}
	*/
	
}
