package xslreporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

public class Reporter {

	final HashMap<String,String> parameters = new HashMap<String,String>();
	final ServiceNow sn;
	final Extractor extractor;
	final TransformerFactory transformerFactory;
	final File reportFile;
	final StringSubstitutor substitutor;
	final Document reportDoc;
	final Element rootNode;
	File workDir;
	File baseDir;
	
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(Option.builder("r").longOpt("report").required(true).hasArg(true).
				desc("Report file (XML)").build());
		options.addOption(Option.builder("p").longOpt("profile").required(true).hasArg(true).
				desc("Connection profile (Java properties)").build());
		options.addOption(Option.builder("w").longOpt("work").required(false).hasArg(true).
				desc("Working directory").build());
		options.addOption(Option.builder("b").longOpt("base").required(false).hasArg(true).
				desc("Base directory").build());
		DefaultParser parser = new DefaultParser();
		CommandLine cmdline = parser.parse(options, args);
		String profilename = cmdline.getOptionValue("p");
		String reportfilename = cmdline.getOptionValue("r");
		System.out.println("profilename=" + profilename);
		assert profilename != null;
		File profile = new File(profilename);
		assert reportfilename != null;
		List<String> reportArgs = cmdline.getArgList();
		Reporter reporter = new Reporter(profile, reportfilename, reportArgs);
		reporter.processNodes();
	}
	
	Reporter(File profile, String reportName, List<String> args) throws Exception {		
		reportFile = new File(reportName);
		parameters.put("report", reportName);
		sn = new ServiceNow(profile);
		extractor = new Extractor(sn);
		transformerFactory = TransformerFactory.newInstance();
		
		SAXBuilder builder = new SAXBuilder();
		System.out.println("loading " + reportFile.getAbsolutePath());
		reportDoc = builder.build(reportFile);
		rootNode = reportDoc.getRootElement();

		for (int i = 0; i < args.size(); ++i) {
			String[] parts = args.get(i).split("=",  2);
			assert parts.length == 2;
			String name = parts[0];
			String value = parts[1];
			System.out.println(name + "=" + value);
			parameters.put(name,  value);
		}
		substitutor = new StringSubstitutor(parameters);
	}
	

	void processNodes() throws Exception {
		List<Element> nodes = rootNode.getChildren();
		for (int i = 0; i < nodes.size(); ++i) {
			processNode(nodes.get(i));
		}
	}
	
	void processNode(Element node) throws Exception {
		String nodeName = node.getName();
		if ("parameter".equals(nodeName)) {
			String paramName = node.getAttributeValue("name");
			String paramValue = substitutor.replace(node.getAttributeValue("value"));
			parameters.put(paramName,  paramValue);
		}
		else if ("extract".equals(nodeName)) 
			extract(node);
		else if ("transform".equals(nodeName)) 
			transform(node);
		else 
			throw new IllegalArgumentException(nodeName);
	}

	String getAttr(Element node, String name) {
		return substitutor.replace(node.getAttributeValue(name));
	}
	
	void extract(Element node) throws Exception  {
		Boolean skip = false;
		String skipAttr  = node.getAttributeValue("skip");
		if (skipAttr != null && skipAttr.length() > 0)
			skip = new Boolean(substitutor.replace(skipAttr));
		String tag        = substitutor.replace(node.getAttributeValue("tag"));
		String tableName  = substitutor.replace(node.getAttributeValue("table"));
		String query      = substitutor.replace(node.getAttributeValue("query"));
		String outputName = substitutor.replace(node.getAttributeValue("output"));
		System.out.println(
			String.format("extracting %s table=%s query=%s output=%s", 
				tag, tableName, query, outputName) +	(skip ? " (skipped)" : ""));
		if (skip) return;
		assert tag.length() > 0;
		assert tableName.length() > 0;
		assert query.length() > 0;
		assert outputName.length() > 0;
		File outputFile = new File(outputName);
		FileOutputStream outStream = new FileOutputStream(outputFile);
		InputStream inStream = extractor.extract(tableName, query);
		IOUtils.copy(inStream,  outStream);
	}
	
	void transform(Element node) throws Exception {
		String xslName     = substitutor.replace(node.getAttributeValue("xsl"));
		String inputName   = substitutor.replace(node.getAttributeValue("input"));
		String outputName  = substitutor.replace(node.getAttributeValue("output"));
		System.out.println(
			String.format("transforming xsl=%s input=%s output=%s", 
					xslName, inputName, outputName));
		assert xslName.length() > 0;
		assert inputName.length() > 0;
		assert outputName.length() > 0;
		File xslFile = new File(xslName);
		File inFile = new File(inputName);
		File outFile = new File(outputName);
		StreamSource xslSource = new StreamSource(xslFile);		
		Transformer transformer = transformerFactory.newTransformer(xslSource);
		for (Entry<String,String> entry : parameters.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			System.out.print(name + "=" + value + " ");
			transformer.setParameter(name,  value);
		}
		System.out.println();
		StreamSource inStream = new StreamSource(inFile);
		StreamResult outStream = new StreamResult(outFile);
		transformer.transform(inStream,  outStream);
	}
	
}
