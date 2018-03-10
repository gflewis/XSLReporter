package xslreporter;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class Transformer {

	static Document document;

	public static void main(String[] args) throws Exception {
		
		File stylesheet = new File(args[0]);
		File datafile = new File(args[1]);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(datafile);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		StreamSource stylesource = new StreamSource(stylesheet);
		javax.xml.transform.Transformer transformer = tFactory.newTransformer(stylesource);
		StreamSource datasource = new StreamSource(datafile);
		StreamResult result = new StreamResult(System.out);
		transformer.transform(datasource, result);
	}

}
