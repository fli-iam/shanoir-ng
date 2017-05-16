<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ut="urn:jboss:domain:undertow:3.0"
                xmlns:dm="urn:jboss:domain:4.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="//ut:http-listener">
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
        <xsl:attribute name="redirect-socket">proxy-https</xsl:attribute>   
        <xsl:attribute name="proxy-address-forwarding">true</xsl:attribute>
        <xsl:apply-templates select="node()"/>
      </xsl:copy>
    </xsl:template>

    <xsl:template match="//dm:socket-binding-group">
        <xsl:copy>
          <xsl:copy-of select="@*|node()"/>
          <socket-binding xmlns="urn:jboss:domain:4.0" name="proxy-https" port="443"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>