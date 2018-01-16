<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:f="java://xslreporter.Functions"
    version="1.0">
  <xsl:output method="html" omit-xml-declaration="no"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <html>
      <body>
        <xsl:apply-templates select="data/story[@number='STRY0024935']"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="story">
    <xsl:variable name="description" select="description"/>
    <description>
	  <xsl:value-of select="f:multiline($description)" disable-output-escaping="yes"/>
    </description>
  </xsl:template>

</xsl:stylesheet>
