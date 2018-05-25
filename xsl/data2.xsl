<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xslt"
    version="1.0">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="no" xalan:indent-amount="2"/>
  <xsl:strip-space elements="*"/>

  <xsl:param name="report" select="report/@name" />
  <xsl:variable name="workDir" select="/report/@workDir" />
  <xsl:variable name="cssData" select="/report/@cssXmlFile" />

  <xsl:template match="/report">
    <xsl:element name="data">
      <xsl:attribute name="report">
        <xsl:value-of select="$report"/>
      </xsl:attribute>
      <xsl:attribute name="workDir">
        <xsl:value-of select="$workDir"/>
      </xsl:attribute>
      <xsl:attribute name="cssData">
        <xsl:value-of select="$cssData"/>
      </xsl:attribute>
      <xsl:apply-templates select="extract"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="extract">
    <xsl:variable name="recordtype" select="@tag" />
    <xsl:variable name="filename"
      select="concat($workDir, $report, '.', @tag, '.xml')" />
    <xsl:variable name="recordset" select="document($filename)/response"/>
    <xsl:call-template name="generate-record-set">
      <xsl:with-param name="recordtype" select="@tag"/>
      <xsl:with-param name="recordset" select="$recordset"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*">
  </xsl:template>

  <xsl:template name="generate-record-set">
    <xsl:param name="recordtype"/>
    <xsl:param name="recordset"/>
    <xsl:for-each select="$recordset/result">
      <xsl:call-template name="generate-record">
        <xsl:with-param name="recordtype" select="$recordtype"/>
        <xsl:with-param name="recordnode" select="."/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="generate-record">
    <xsl:param name="recordtype"/>
    <xsl:param name="recordnode"/>
    <xsl:element name="{$recordtype}">
      <xsl:attribute name="number">
         <xsl:value-of select="$recordnode/number/value"/>
       </xsl:attribute>
       <xsl:attribute name="sys_id">
          <xsl:value-of select="$recordnode/sys_id/value"/>
        </xsl:attribute>
      <xsl:for-each select="$recordnode/*">
        <xsl:call-template name="generate-element">
          <xsl:with-param name="elementnode" select="." />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>

  <xsl:template name="generate-element">
    <xsl:param name="elementnode" />
    <xsl:element name="{name($elementnode)}">
      <xsl:value-of select="$elementnode/display_value"/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
