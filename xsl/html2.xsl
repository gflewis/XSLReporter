<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:f="java://xslreporter.Functions"
    version="1.0">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="no"/>

  <xsl:variable name="workDir" select="/data/@workDir" />
  <xsl:variable name="cssData" select="/data/@cssData"/>

  <xsl:template match="/data">
    <html>
      <head>
        <style>
          <xsl:value-of select="document($cssData)/content" />
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
          <td class="data"><xsl:value-of select="number" /></td>
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
	        <th class="labl">Number</th>
          <td class="data">
            <xsl:variable name="base">
              <xsl:text>mit.service-now.com/rm_story.do?sys_id=</xsl:text>
            </xsl:variable>
            <xsl:variable name="addr"
              select="concat('https://',$base,sys_id)" />
            <xsl:element name="a">
              <xsl:attribute name="href">
                <xsl:value-of select="$addr" />
              </xsl:attribute>
              <xsl:value-of select="number" />
            </xsl:element>
          </td>
	        <td class="gully"/>
	        <th class="labl">Status</th>
	        <td class="data"><xsl:value-of select="state" /></td>
        </tr>
        <tr class="row-2col">
          <th class="labl">Theme</th>
          <td class="data"><xsl:value-of select="theme" /></td>
          <td class="gully"/>
          <th class="labl">Priority</th>
          <td class="data"><xsl:value-of select="priority" /></td>
        </tr>
        <tr class="row-2col">
          <th class="labl">Requested by</th>
          <td class="data"><xsl:value-of select="u_requested_by" /></td>
          <td class="gully"/>
          <th class="labl">Size</th>
          <td class="data"><xsl:value-of select="u_complexity" /></td>
        </tr>
        <tr class="row-1col">
          <th class="labl">Description</th>
          <xsl:variable name="description" select="description"/>
          <td class="data" colspan="4">
            <xsl:value-of select="f:markdown(description)" disable-output-escaping="yes"/>
          </td>
        </tr>
        <tr class="row-1col">
          <th class="labl">Acceptance Criteria</th>
          <td class="data" colspan="4">
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

</xsl:stylesheet>
