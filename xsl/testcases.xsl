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
        <table>
          <xsl:apply-templates select="case"/>
        </table>
      </body>
      <footer>
      </footer>
    </html>
  </xsl:template>

  <xsl:template match="case">
    <tr>      
      <td colspan="1"><xsl:value-of select="number"/></td>
      <td colspan="2"><xsl:value-of select="short_description"/></td>
    </tr>
    <tr>
      <td>&#160;</td>
      <th>Test</th>
      <th>Expected Result</th>
    </tr>
    <xsl:variable name="casenumber" select="@number" />
    <xsl:apply-templates select="/data/test[tm_test_case=$casenumber]">
      <xsl:sort select="order"/>
    </xsl:apply-templates>
    <tr>
      <td colspan="3">&#160;</td>
    </tr>
  </xsl:template>
  
  <xsl:template match="test">
    <tr>
      <td style="text-align: right;"><xsl:value-of select="order"/></td>
      <td><xsl:value-of select="f:markdown(test)" disable-output-escaping="yes"/></td>
      <td><xsl:value-of select="f:markdown(expected_result)" disable-output-escaping="yes"/></td>
    </tr>
    <xsl:if test="test_description != 'null'">
      <tr>
        <td colspan="1">&#160;</td>
        <td colspan="2"><xsl:value-of select="test_description" disable-output-escaping="yes"/></td>
      </tr>    
    </xsl:if>
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
