<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xalan="http://xml.apache.org/xslt"
    xmlns:xp="http://www.w3.org/2005/xpath-functions"     
    xmlns:f="java://xslreporter.Functions"
    version="1.0">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="no"
    xalan:indent-amount="2" />
  <xsl:strip-space elements="*" />

  <xsl:template match="/report">
    <xsl:element name="data">
      <xsl:apply-templates select="extract" />
    </xsl:element>
  </xsl:template>

  <xsl:template match="extract">
    <xsl:variable name="recordtype" select="@id" />
    <xsl:variable name="filename" select="f:substitute(output)" />
    <xsl:variable name="recordset" select="document($filename)/response" />
    <xsl:call-template name="generate-record-set">
      <xsl:with-param name="recordtype" select="@id" />
      <xsl:with-param name="recordset" select="$recordset" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*" />

  <xsl:template name="generate-record-set">
    <xsl:param name="recordtype" />
    <xsl:param name="recordset" />
    <xsl:for-each select="$recordset/result">
      <xsl:call-template name="generate-record">
        <xsl:with-param name="recordtype" select="$recordtype" />
        <xsl:with-param name="recordnode" select="." />
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="generate-record">
    <xsl:param name="recordtype" />
    <xsl:param name="recordnode" />
    <xsl:element name="{$recordtype}">
      <xsl:attribute name="number">
         <xsl:value-of select="$recordnode/number/value" />
       </xsl:attribute>
      <xsl:attribute name="sys_id">
          <xsl:value-of select="$recordnode/sys_id/value" />
        </xsl:attribute>
      <xsl:for-each select="$recordnode/*">
        <xsl:sort select="name()" />
        <xsl:call-template name="generate-element">
          <xsl:with-param name="elementnode" select="." />
        </xsl:call-template>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>

  <xsl:template name="generate-element">
    <xsl:param name="elementnode" />
    <xsl:element name="{name($elementnode)}">
      <xsl:value-of select="$elementnode/display_value" />
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
