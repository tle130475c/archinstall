package com.tle130475c.archinstall.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XMLReader {
    private XPath xPath;
    private Document document;

    public XMLReader(String path, DocumentBuilderFactory dbFactory)
            throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        XPathFactory xPathFactory = XPathFactory.newInstance();
        File configFile = new File(path);

        xPath = xPathFactory.newXPath();
        document = builder.parse(configFile);
    }

    public String getValue(String expression) throws XPathExpressionException {
        return xPath.evaluate(expression, document);
    }
}
