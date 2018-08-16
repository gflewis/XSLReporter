<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:f="java://xslreporter.Functions" version="1.0">
  <xsl:output method="xml" indent="yes" omit-xml-declaration="no" />

  <xsl:param name="servicenow-url" />
  <xsl:param name="css-file" />

  <xsl:template match="/data">
    <html>
      <head>
        <link rel="stylesheet" type="text/css">
          <xsl:attribute name="href">
            <xsl:value-of select="$css-file" />
          </xsl:attribute>
        </link>
      </head>
      <body id="content">
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
    <div class="form">
        <div class="title">
          <xsl:value-of select="short_description" />
        </div>
        <div class="number">
          <xsl:value-of select="number" />
        </div>
        <div class="status">
          <xsl:value-of select="state" />
        </div>
    </div>
  </xsl:template>

  <xsl:template match="story">
    <div class="form">
      <div class="row head">
        <div class="seq">
          <xsl:number value="position()" format="(1)" />
        </div>
        <div class="title">
          <xsl:value-of select="short_description" />
        </div>
      </div>
      <div class="row">
        <div class="labl col1">Number</div>
        <div class="text col1">
          <xsl:value-of select="number" />
        </div>
        <div class="labl col2">Status</div>
        <div class="text col2">
          <xsl:value-of select="state" />
        </div>
      </div>
      <div class="row">
        <div class="labl full">Description</div>
        <div class="text full">
          <xsl:value-of select="f:markdown(description)"
            disable-output-escaping="yes" />
        </div>
      </div>
    </div>
  </xsl:template>
            
  <xsl:template match="*">
    <xsl:message terminate="no">
      WARNING: Unmatched element:
      <xsl:value-of select="name()" />
    </xsl:message>
    <xsl:apply-templates />
  </xsl:template>

</xsl:stylesheet>
