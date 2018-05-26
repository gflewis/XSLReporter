package xslreporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class Functions {

	static Parser parser;
	static HtmlRenderer renderer;

	static public String upper(String value) {
		return value.toUpperCase();
	}
	
	static public String lower(String value) {
		return value.toLowerCase();
	}
	
	static public Integer length(String value) {
		return value.length();
	}

	static public String include(String filename) throws IOException {
		System.out.println("include " + filename);
		assert filename != null;
		assert filename.length() > 0;
		StringBuffer content = new StringBuffer();
		File inputFile = new File(filename);		
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		String line = null;
		while ((line = reader.readLine()) != null) {
			content.append(line);
			content.append('\n');			
		}
		reader.close();
		return content.toString();
	}
	
	static public String markdown(String value) {
		assert value != null;		
		if (parser == null) parser = Parser.builder().build();
		if (renderer == null) renderer = HtmlRenderer.builder().build();			
		Node document = parser.parse(value);
		String result = renderer.render(document);
		return result;
	}
	
	enum TextMode {NORMAL, LIST, CODE};

	static public String multiline(String value) {
		TextMode mode = TextMode.NORMAL;
		int indent = 0;
		StringBuffer buffer = new StringBuffer();
		buffer.append("<p>");
		String[] lines = value.split("\n");
		for (String line : lines) {
			// trim trailing apaces
			line = line.replaceAll("\\s$", "");
			// count leading spaces
			String trimmed = line.trim();
			int lead = line.length() - trimmed.length();
			if (lead > 0) {
				if (mode == TextMode.LIST) buffer.append("</ul>\n");
				if (mode != TextMode.CODE) {
					buffer.append("<pre>\n");
					mode = TextMode.CODE;				
					indent = lead;
				}
				trimmed = line.substring(indent);
				buffer.append(line + "\n");
			}
			else if (line.startsWith("* ")) {
				if (mode == TextMode.CODE) buffer.append("</pre>\n");
				if (mode != TextMode.LIST) {
					buffer.append("<ul>\n");
					mode = TextMode.LIST;
				}
				trimmed = line.substring(2);
				buffer.append("<li>");
				buffer.append(trimmed);
				buffer.append("</li>\n");
			}
			else {
				if (mode == TextMode.CODE) buffer.append("</pre>\n");
				if (mode == TextMode.LIST) buffer.append("</ul>\n");
				mode = TextMode.NORMAL;
				buffer.append(trimmed);
				buffer.append(line.length() == 0 ? "</p>\n<p>" : "\n");
			}
		}
		if (mode == TextMode.CODE) buffer.append("</pre>\n");
		if (mode == TextMode.LIST) buffer.append("</ul>\n");
		buffer.append("</p>");
		return buffer.toString();
	}
}
