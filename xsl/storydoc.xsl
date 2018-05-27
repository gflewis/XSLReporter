<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:f="java://xslreporter.Functions"
    version="1.0">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="no"/>

  <xsl:param name="css">css/storydoc.css</xsl:param>
  <xsl:param name="servicenow-url" />

  <xsl:template match="/data">
    <html>
      <head>
        <style>
          <xsl:value-of select="f:include($css)" />
        </style>
      </head>
      <body id="content">
        <xsl:apply-templates select="release"/>
        <xsl:apply-templates select="story">
          <xsl:sort select="priority" />
          <xsl:sort select="number" />
        </xsl:apply-templates>
      </body>
      <footer>
      </footer>
    </html>
  </xsl:template>

  <xsl:template match="release">
    <div class="release">
      <table class="form">
        <tr class="title">
          <td class="title" colspan="5"><xsl:value-of select="short_description"/></td>
        </tr>
        <tr class="row-2col">
          <th class="labl">Release</th>
          <td class="data">
            <xsl:call-template name="tasklink">
              <xsl:with-param name="number" select="number"/>
              <xsl:with-param name="sys_id" select="sys_id"/>
            </xsl:call-template>
          </td>
          <td class="gully" />
          <th class="labl">Status</th>
          <td class="data"><xsl:value-of select="state" /></td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="story">
    <div class="story">
      <table class="form">
        <tr class="title">
          <td class="title" colspan="5"><xsl:value-of select="short_description" /></td>
        </tr>
        <tr class="row-2col">
	        <th class="col-1h">Number</th>
          <td class="col-1d">
            <xsl:call-template name="tasklink">
              <xsl:with-param name="number" select="number"/>
              <xsl:with-param name="sys_id" select="sys_id"/>
            </xsl:call-template>
          </td>
	        <td class="gully"/>
	        <th class="col-2h">Status</th>
	        <td class="col-2d"><xsl:value-of select="state" /></td>
        </tr>
        <tr class="row-2col">
          <th class="col-1h">Theme</th>
          <td class="col-1d"><xsl:value-of select="theme" /></td>
          <td class="gully"/>
          <th class="col-2h">Priority</th>
          <td class="col-2d"><xsl:value-of select="priority" /></td>
        </tr>
        <tr class="row-2col">
          <th class="col-1h">Requested by</th>
          <td class="col-1d"><xsl:value-of select="u_requested_by" /></td>
          <td class="gully"/>
          <th class="col-2h">Size</th>
          <td class="col-2d"><xsl:value-of select="u_complexity" /></td>
        </tr>
        <tr class="row-1col">
          <th class="col1h">Description</th>
          <xsl:variable name="description" select="description"/>
          <td class="col-1d" colspan="4">
            <xsl:value-of select="f:markdown(description)" disable-output-escaping="yes"/>
          </td>
        </tr>
        <tr class="row-1col">
          <th class="col-1h">Acceptance Criteria</th>
          <td class="col-1d" colspan="4">
            <xsl:value-of select="f:markdown(acceptance_criteria)" disable-output-escaping="yes"/>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="*">
    <xsl:message terminate="no">
     WARNING: Unmatched element: <xsl:value-of select="name()"/>
    </xsl:message>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template name="tasklink">
    <xsl:param name="number"/>
    <xsl:param name="sys_id"/>
    <xsl:variable name="addr">
      <xsl:value-of select="$servicenow-url"/>
      <xsl:text>task.do?sys_id=</xsl:text>
      <xsl:value-of select="$sys_id"/>
    </xsl:variable>
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:value-of select="$addr" />
      </xsl:attribute>
      <xsl:value-of select="$number" />
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
