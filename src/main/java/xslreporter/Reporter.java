package xslreporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.text.StringSubstitutor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Reporter {

	final ServiceNow sn;
	final Extractor extractor;
	final TransformerFactory transformerFactory;
	final DocumentBuilder builder;
	final Element root;
	final File reportFile;
	File workDir;
	File baseDir;
	NodeList extractList;
	
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(Option.builder("r").longOpt("report").required(true).hasArg(true).
				desc("Report file (XML)").build());
		options.addOption(Option.builder("p").longOpt("profile").required(true).hasArg(true).
				desc("Connection profile (Java properties)").build());
		options.addOption(Option.builder("w").longOpt("work").required(false).hasArg(true).
				desc("Working directory").build();
		options.addOption(Option.builder("b").longOpt("base").required(false).hasArg(true).build()
				desc("Base directory").build();
		DefaultParser parser = new DefaultParser();
		CommandLine cmdline = parser.parse(options, args);
		String profilename = cmdline.getOptionValue("p");
		String reportfilename = cmdline.getOptionValue("r");
		String workDirName = cmdline.getOptionValue("w");
		System.out.println("profilename=" + profilename);
		assert profilename != null;
		File profile = new File(profilename);
		assert reportfilename != null;
		File report = new File(reportfilename);
		Reporter reporter = new Reporter(profile, report);
		reporter.process();
	}
	
	Reporter(File profile, File xml) throws Exception {
		reportFile = xml;
		sn = new ServiceNow(profile);
		extractor = new Extractor(sn);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		builder = dbf.newDocumentBuilder();
		FileInputStream stream = new FileInputStream(xml);
		Document doc = builder.parse(stream);
		root = doc.getDocumentElement();
		assert root.getNodeName().contentEquals("report");
		workDir = getRootFile("workDir");
		System.out.println("work=" + workDir);
		assert workDir.isDirectory();
		assert workDir.canWrite();
		this.extractList = root.getElementsByTagName("extract");
		this.transformerFactory = TransformerFactory.newInstance();
	}
	

	void process() throws Exception {
		File cssFile = getRootFile("cssFile");
		File cssFileXml = getRootFile(workDir, "cssXmlFile");
		File xslData = getRootFile("xslData");
		File xslHtml = getRootFile("xslHtml");
		File xmlData = getRootFile(workDir, "dataFile");
		File htmlReport = getRootFile(workDir, "htmlReport");
		writeCSS(cssFile, cssFileXml);
		extract();
		transform(xslData, reportFile, xmlData);
		transform(xslHtml, xmlData, htmlReport);
	}

	String getRootAttr(String name) {
		String value = repl(root.getAttribute(name));
		assert value.length() > 0;
		return value;
	}
	
	File getRootFile(String name) {
		return new File(getRootAttr(name));
	}
	
	File getRootFile(File dir, String name) {
		return new File(dir, getRootAttr(name));
	}
	
	String repl(String text) {
		String result = StringSubstitutor.replaceSystemProperties(text);
		assert !result.contains("$");
		assert !result.contains("{");
		return result;
	}
		
	void extract() throws Exception  {
		Transformer transformer = transformerFactory.newTransformer();
		for (int i = 0; i < extractList.getLength(); ++i) {
			Element extract = (Element) extractList.item(i);
			String tag = repl(extract.getAttribute("tag"));
			String table = repl(extract.getAttribute("table"));
			String query = repl(extract.getAttribute("query"));
			String fileName = repl(extract.getAttribute("file"));
			assert tag.length() > 0;
			assert table.length() > 0;
			assert query.length() > 0;
			assert fileName.length() > 0;
			File filePath = new File(workDir, fileName);
			System.out.println("extracting " + tag + 
					" table=" + table +
					" query=" + query +
					" file=" + filePath);
			FileWriter writer = new FileWriter(filePath);
			Document doc = extractor.extract(table, query);
			DOMSource domSource = new DOMSource(doc);
			transformer.transform(domSource, new StreamResult(writer));
		}	
	}
	
	void transform(File stylesheet, File docFile, File outFile) throws Exception {
		System.out.println("transforming " + docFile + " to " + outFile);
		StreamSource stylesource = new StreamSource(stylesheet);
		Transformer transformer = transformerFactory.newTransformer(stylesource);
		transformer.setParameter("report-name",  reportFile.getName());
		transformer.setParameter("report-path",  reportFile.getAbsolutePath());
		transformer.setParameter("work-dir", workDir.getAbsolutePath());
		transformer.setParameter("base-dir", baseDir.);
		StreamSource docSource = new StreamSource(docFile);
		transformer.transform(docSource, new StreamResult(outFile));		
	}
	
	void writeCSS(File cssFile, File xmlFile) throws IOException {
		System.out.println("copy " + cssFile + " to " + xmlFile);
		BufferedReader reader = new BufferedReader(new FileReader(cssFile));
		PrintWriter writer = new PrintWriter(xmlFile);
		String line;
		writer.println("<?xml version='1.0' encoding='UTF-8'?>");
		writer.println("<content><![CDATA[");
		while ((line = reader.readLine()) != null) {
			writer.println(line);
		}
		reader.close();
		writer.println("]]>");
		writer.println("</content>");
		writer.close();		
	}
	
}
