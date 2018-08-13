<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:f="java://xslreporter.Functions" version="1.0">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="no" />

  <xsl:param name="css">
    css/testcases.css
  </xsl:param>
  <xsl:param name="servicenow-url" />

  <xsl:template match="/data">
    <html>
      <head>
        <style>
          <xsl:value-of select="f:include($css)" />
        </style>
      </head>
      <body id="content">
        <table border="1">
          <xsl:apply-templates select="case"/>
        </table>
      </body>
      <footer>
      </footer>
    </html>
  </xsl:template>

  <xsl:template match="case">
    <tr>      
      <td><xsl:value-of select="number"/></td>
      <td><xsl:value-of select="short_description"/></td>
    </tr>
    <xsl:variable name="casenumber" select="@number" />
    <xsl:apply-templates select="/data/test[tm_test_case=$casenumber]"/>
  </xsl:template>
  
  <xsl:template match="test">
    <tr>
      <td><xsl:value-of select="number"/></td>
    </tr>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:message terminate="no">
      WARNING: Unmatched element:
      <xsl:value-of select="name()" />
    </xsl:message>
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template name="tasklink">
    <xsl:param name="number" />
    <xsl:param name="sys_id" />
    <xsl:variable name="addr">
      <xsl:value-of select="$servicenow-url" />
      <xsl:text>task.do?sys_id=</xsl:text>
      <xsl:value-of select="$sys_id" />
    </xsl:variable>
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:value-of select="$addr" />
      </xsl:attribute>
      <xsl:value-of select="$number" />
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
