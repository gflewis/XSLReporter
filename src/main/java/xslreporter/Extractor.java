package xslreporter;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Extractor {

	static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	static TransformerFactory transformerFactory = TransformerFactory.newInstance();
	
	final ServiceNow sn;
	final CloseableHttpClient client;
	final javax.xml.parsers.DocumentBuilder builder; 
	final javax.xml.transform.Transformer transformer;	

	Extractor(ServiceNow sn) throws ParserConfigurationException, TransformerException {
		this.sn = sn;
		this.client = sn.getClient();
		builder = builderFactory.newDocumentBuilder();
		transformer = transformerFactory.newTransformer();
	}
	
	Document extract(String tablename, String query) throws IOException, SAXException {
		String path = "api/now/table/" + tablename;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (query != null) 
			params.add(new BasicNameValuePair("sysparm_query", query));
		URI uri = sn.getURI(path, params);
		HttpGet request = new HttpGet(uri);
		request.setHeader("Accept", "application/xml");
		CloseableHttpResponse response = client.execute(request);		
		StatusLine statusLine = response.getStatusLine();		
		int statusCode = statusLine.getStatusCode();
		assert statusCode == 200;
		HttpEntity responseEntity = response.getEntity();
		InputStream content = responseEntity.getContent();
		Document responseDoc = builder.parse(content);
		return responseDoc;
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
	
}
