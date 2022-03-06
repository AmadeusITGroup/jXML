package com.amadeus.et.jxml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static com.amadeus.et.jxml.XmlToJsonTest.readResource;

public class JsonToXmlTest {

  private static final String XML_RESOURCE_FOLDER = "xml/conversion/";

  private static final Logger logger = LoggerFactory.getLogger(JsonToXmlTest.class);

  @InjectMocks
  private XmlToJson xmlToJsonConverter;

  @InjectMocks
  private JsonToXml jsonToXmlconverter;

  @BeforeEach
  private void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void nominalCase() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                            TransformerException {
    final String xmlFileName = "xmlInputValidBasic.xml";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString(xmlFileName));
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson);

    final String expectedXML = getXmlFileContentAsString(xmlFileName);

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void nominalCaseWithChild() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                                     TransformerException {
    final String xmlFileName = "xmlInputValidBasicWithChild.xml";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString(xmlFileName));
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson);

    final String expectedXML = getXmlFileContentAsString(xmlFileName);

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void nominalCaseWithChildren() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                                     TransformerException {
    final String xmlFileName = "xmlInputValidBasicWithChildren.xml";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString(xmlFileName));
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson);

    final String expectedXML = getXmlFileContentAsString(xmlFileName);

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void nominalCaseWithArray() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                                     TransformerException {
    final String xmlFileName = "xmlInputValidBasicWithArray.xml";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString(xmlFileName));
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson);

    final String expectedXML = getXmlFileContentAsString(xmlFileName);

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void nominalCaseWithComplexMessage() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                                     TransformerException {
    final String xmlFileName = "xmlInputValidSOAPLike.xml";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString(xmlFileName));
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson);

    final String expectedXML = getXmlFileContentAsString(xmlFileName);

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void differentSeparator() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                                     TransformerException {
    final String xmlFileName = "xmlInputValidBasicWithArray.xml";
    final String separator = "#";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString(xmlFileName), separator);
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson, separator);

    final String expectedXML = getXmlFileContentAsString(xmlFileName);

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void noNamespace() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                                   TransformerException {
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString("xmlInputValidBasicWithArray.xml"), true);
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson, false);

    final String expectedXML = getXmlFileContentAsString("xmlInputValidBasicWithArrayNoNamespace.xml");

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void differentSeparatorNoNamespace() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                            TransformerException {
    final String separator = "#";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString("xmlInputValidBasicWithArray.xml"), true, separator);
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson, false, separator);

    final String expectedXML = getXmlFileContentAsString("xmlInputValidBasicWithArrayNoNamespace.xml");

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void complexXMLDifferentSeparatorNoNamespace() throws IOException, ParserConfigurationException, SAXException, JxmlException,
                                              TransformerException {
    final String separator = "#";
    final String xmlToJson = xmlToJsonConverter.execute(getXmlFileContentAsString("xmlInputValidSOAPLike.xml"), true, separator);
    final String jsonToXML = jsonToXmlconverter.execute(xmlToJson, false, separator);

    final String expectedXML = getXmlFileContentAsString("xmlInputValidSOAPLikeNoNamespace.xml");

    compareExpectedAndObtained(expectedXML, jsonToXML);
  }

  @Test
  void nullInput() {
    final JxmlException exception = Assertions.assertThrows(JxmlException.class, () ->
        jsonToXmlconverter.execute(null));

    Assertions.assertEquals(
        "'json' should not be null.",
        exception.getMessage());
  }

  @Test
  void wrongElementDelimiter() {
    final JxmlException exception = Assertions.assertThrows(JxmlException.class, () ->
        jsonToXmlconverter.execute("{\"Example\":\"Value\"}", "S"));
    Assertions.assertEquals(
        "'xmlElementDelimiter' must follows this pattern: '[._=!@#~%&*^?,-]'.",
        exception.getMessage());
  }

  @Test
  void emptyJsonShouldRaiseError() {
    final JxmlException exception = Assertions.assertThrows(JxmlException.class, () ->
        jsonToXmlconverter.execute("{}"));
    Assertions.assertEquals(
        "'json' should contain a root element to be converted to XML.",
        exception.getMessage());
  }

  @Test
  void jsonWithSeveralFirstLevelElementsShouldRaiseError() {
    final JxmlException exception = Assertions.assertThrows(JxmlException.class, () ->
        jsonToXmlconverter.execute("{\"FirstElement\":\"Value1\", \"SecondElement\":\"Value2\"}"));
    Assertions.assertEquals(
        "'json' should contain only a single root element to be converted to XML.",
        exception.getMessage());
  }

  @Test
  void readmeExample() throws IOException, ParserConfigurationException, SAXException, TransformerException, JxmlException {
    final String json = "{\"Element\":{\"__text\":\"ElementValue\"}}";
    final String jsonToXML = jsonToXmlconverter.execute(json);

    final String expectedXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Element>ElementValue</Element>";
    compareExpectedAndObtained(expectedXML, jsonToXML);
  }


  private void compareExpectedAndObtained(final String expectedXML, final String obtainedXML)
      throws ParserConfigurationException, SAXException, IOException, TransformerException {

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setCoalescing(true);
    dbf.setIgnoringElementContentWhitespace(true);
    dbf.setIgnoringComments(true);
    final DocumentBuilder db = dbf.newDocumentBuilder();

    final Document doc1 = db.parse(new ByteArrayInputStream(expectedXML.getBytes()));
    doc1.setXmlStandalone(true);
    doc1.normalizeDocument();
    String expectedNormalized = convertDomToString(doc1);
    expectedNormalized = normalizeXml(expectedNormalized);
    logger.info("Expected normalized:\n" + expectedNormalized);

    final Document doc2 = db.parse(new ByteArrayInputStream(obtainedXML.getBytes()));
    doc2.setXmlStandalone(true);
    doc2.normalizeDocument();
    String obtainedNormalized = convertDomToString(doc2);
    obtainedNormalized = normalizeXml(obtainedNormalized);
    logger.info("Obtained normalized:\n" + obtainedNormalized);

    Assertions.assertEquals(expectedNormalized, obtainedNormalized);
  }

  private static String getXmlFileContentAsString(final String fileName) throws IOException {
    return readResource(XML_RESOURCE_FOLDER + fileName);
  }

  private static String normalizeXml(final String xml) {
    final String[] inputs = xml.split("\n");
    final StringBuilder in = new StringBuilder();
    for (final String inp : inputs) {
      in.append(inp.trim());
    }
    return in.toString();
  }

  private String convertDomToString(final Document doc) throws TransformerException {
    final DOMSource domSource = new DOMSource(doc);
    final StringWriter writer = new StringWriter();
    final StreamResult result = new StreamResult(writer);
    final TransformerFactory tf = TransformerFactory.newInstance();
    final Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    return writer.toString();
  }

}
