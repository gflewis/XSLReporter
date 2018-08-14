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

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

public class Reporter {

	static final HashMap<String,String> variables = new HashMap<String,String>();

	final ServiceNow sn;	
	final Extractor extractor;
	final TransformerFactory transformerFactory;
	final File reportFile;
	final Document reportDoc;
	final Element rootNode;
	
	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(Option.builder("r").longOpt("report").required(true).hasArg(true).
				desc("Report file (XML)").build());
		options.addOption(Option.builder("p").longOpt("profile").required(true).hasArg(true).
				desc("Connection profile (Java properties)").build());
		options.addOption(Option.builder("x").longOpt("xml").required(false).hasArg(false).
				desc("XML Web Service format").build());
		options.addOption(Option.builder("u").longOpt("unload").required(false).hasArg(false).
				desc("XML Unload Format").build());
		DefaultParser parser = new DefaultParser();
		CommandLine cmdline = parser.parse(options, args);
		String profilename = cmdline.getOptionValue("p");
		String reportfilename = cmdline.getOptionValue("r");
		Extractor.Format format = Extractor.Format.REST;
		if (cmdline.hasOption("x")) format = Extractor.Format.XML;
		if (cmdline.hasOption("u")) format = Extractor.Format.UNLOAD;
		
		System.out.println("profilename=" + profilename + " format=" + format);
		assert profilename != null;
		File profile = new File(profilename);
		assert reportfilename != null;
		List<String> reportArgs = cmdline.getArgList();
		Reporter reporter = new Reporter(profile, reportfilename, reportArgs, format);
		reporter.processNodes();
	}
	
	Reporter(File profile, String reportName, 
				List<String> args, 
				Extractor.Format format) 
			throws Exception {		
		reportFile = new File(reportName);
		setVariable("user.dir", System.getProperty("user.dir"));
		setVariable("user.home", System.getProperty("user.home"));
		setVariable("user.name", System.getProperty("user.name"));
		setVariable("report-file", reportName);
		sn = new ServiceNow(profile);
		String instance = sn.getURI("").toString();
		setVariable("servicenow-url", instance);
		
		extractor = new Extractor(sn, format);
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
			setVariable(name,  value);
		}
	}
	
	static HashMap<String,String> getVariables() {
		return variables;
	}
	
	void setVariable(String name, String value) {
		System.out.println(name + "=" + value);
		variables.put(name,  value);
	}
	
	private String substitute(String text) {
		StringSubstitutor sub = new StringSubstitutor(variables);
		return sub.replace(text);
	}
	
	void processNodes() throws Exception {
		List<Element> nodes = rootNode.getChildren();
		for (int i = 0; i < nodes.size(); ++i) {
			processNode(nodes.get(i));
		}
	}
	
	void processNode(Element node) throws Exception {
		String nodeName = node.getName();
		if ("variable".equals(nodeName)) 
			variable(node);
		else if ("extract".equals(nodeName)) 
			extract(node);
		else if ("transform".equals(nodeName)) 
			transform(node);
		else if ("html-to-pdf".equals(nodeName))
			html2pdf(node);
		else 
			throw new IllegalArgumentException(nodeName);
	}
	
	void variable(Element node) {
		String name = node.getAttributeValue("name");
		String value = null;
		String txtValue = node.getAttributeValue("value");
		if (txtValue != null && txtValue.length() > 0) {
			value = substitute(txtValue);
			setVariable(name, value);
		}
		else {
			value = variables.get(name);
		}
		String txtRequired = node.getAttributeValue("required");
		if (txtRequired != null && txtRequired.length() > 0) {
			Boolean required = new Boolean(substitute(txtRequired));
			if (required) {
				if (value==null || value.length()==0)
					throw new IllegalArgumentException("Not initialized: " + name);
			}
		}
	}
	
	void extract(Element node) throws Exception  {
		Boolean skip = false;
		String skipAttr  = node.getAttributeValue("skip");
		if (skipAttr != null && skipAttr.length() > 0)
			skip = new Boolean(substitute(skipAttr));
		String id         = substitute(node.getAttributeValue("id"));
		String tableName  = substitute(node.getChildText("table"));
		String query      = substitute(node.getChildText("query"));
		String outputName = substitute(node.getChildText("output"));
		System.out.println(
			String.format("extracting %s table=%s query=%s output=%s", 
				id, tableName, query, outputName) +	(skip ? " (skipped)" : ""));
		if (skip) return;
		assert id.length() > 0;
		assert tableName.length() > 0;
		assert query.length() > 0;
		assert outputName.length() > 0;
		File outputFile = new File(outputName);
		FileOutputStream outStream = new FileOutputStream(outputFile);
		InputStream inStream = extractor.extract(tableName, query);
		IOUtils.copy(inStream,  outStream);
		extractor.closeRequest();
	}
	
	void transform(Element node) throws Exception {
		String xslName     = substitute(node.getChildText("xsl"));
		String inputName   = substitute(node.getChildText("input"));
		String outputName  = substitute(node.getChildText("output"));
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
		for (Entry<String,String> entry : variables.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			transformer.setParameter(name,  value);
		}
		StreamSource inStream = new StreamSource(inFile);
		StreamResult outStream = new StreamResult(outFile);
		transformer.transform(inStream,  outStream);
	}
	
	void html2pdf(Element node) throws Exception {
		String inputName   = substitute(node.getChildText("input"));
		String outputName  = substitute(node.getChildText("output"));
		FileOutputStream outStream = new FileOutputStream(outputName);
        PdfRendererBuilder builder = new PdfRendererBuilder();
		System.out.println(String.format("building PDF input=%s output=%s", inputName, outputName));
        builder.withFile(new File(inputName));
        builder.toStream(outStream);
        builder.run();		
	}
}
