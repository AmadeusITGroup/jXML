package com.amadeus.et.jxml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class XmlToJsonTest {

  private static final String XML_RESOURCE_FOLDER = "xml/conversion/";
  private static final String JSON_RESOURCE_FOLDER = "json/conversion/";

  @InjectMocks
  private XmlToJson converter;

  @BeforeEach
  private void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void nominalCase() throws IOException, ParserConfigurationException, SAXException, JxmlException {
    final String result = converter.execute(getXmlFileContentAsString("xmlInputValidBasic.xml"));

    final String expectedJson = getJsonFileContentAsString("jsonBasicExpectedConversion.json");
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void basicXmlWithArray()
      throws IOException, ParserConfigurationException, SAXException, JxmlException {
    final String result = converter.execute(getXmlFileContentAsString("xmlInputValidBasicWithArray.xml"));

    final String expectedJson = getJsonFileContentAsString("jsonBasicWithArrayExpectedConversion.json");
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void basicXmlWithArrayNoNamespace()
      throws IOException, ParserConfigurationException, SAXException, JxmlException {
    final String result = converter.execute(getXmlFileContentAsString("xmlInputValidBasicWithArray.xml"), false);

    final String expectedJson = getJsonFileContentAsString("jsonBasicWithArrayExpectedConversionNoNamespace.json");
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void complexXmlWithArray()
      throws IOException, ParserConfigurationException, SAXException, JxmlException {
    final String result = converter.execute(getXmlFileContentAsString("xmlInputValidSOAPLike.xml"));

    final String expectedJson = getJsonFileContentAsString("jsonExpectedComplexConversion.json");
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void specialCharactersCaseAndCarriageReturn()
      throws IOException, ParserConfigurationException, SAXException, JxmlException {
    final String result = converter.execute(getXmlFileContentAsString("xmlInputValidSpecialCharactersAndCarriageReturn.xml"));

    final String expectedJson = getJsonFileContentAsString("jsonSpecialCharactersAndCarriageReturnConversion.json");
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void doNotKeepNamespace()
      throws IOException, ParserConfigurationException, SAXException, JxmlException {
    final String result = converter.execute(getXmlFileContentAsString("xmlInputValidSOAPLike.xml"), false);

    final String expectedJson = getJsonFileContentAsString("jsonExpectedConversionNoNamespace.json");
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void changingElementDelimiter()
      throws IOException, ParserConfigurationException, SAXException, JxmlException {
    final String result = converter.execute(getXmlFileContentAsString("xmlInputValidSOAPLike.xml"), true, "#");

    final String expectedJson = getJsonFileContentAsString("jsonExpectedConversionDifferentDelimiter.json");
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void wrongElementDelimiter() {
    final JxmlException exception = Assertions.assertThrows(JxmlException.class, () ->
        converter.execute(getXmlFileContentAsString("xmlInputInvalidSOAPLike.xml"), true, "<"));
    Assertions.assertEquals(
        "'xmlElementDelimiter' must follows this pattern: '[._=!@#~%&*^?,-]'.",
        exception.getMessage());
  }

  @Test
  void nullXmlData() {
    final JxmlException exception = Assertions.assertThrows(JxmlException.class, () ->
        converter.execute(null));
    Assertions.assertEquals(
        "'xml' should not be null.",
        exception.getMessage());
  }

  @Test
  void wrongXml() {
    final SAXParseException exception = Assertions.assertThrows(SAXParseException.class, () ->
        converter.execute(getXmlFileContentAsString("xmlInputInvalidSOAPLike.xml"), true, "#"));
    Assertions.assertEquals(
        "The element type \"soap:Header\" must be terminated by the matching end-tag \"</soap:Header>\".",
        exception.getMessage());
  }

  @Test
  void emptyXmlFile() {
    final SAXParseException exception = Assertions.assertThrows(SAXParseException.class, () ->
      converter.execute(getXmlFileContentAsString("emptyFile.xml")));
    Assertions.assertEquals("Premature end of file.",
                            exception.getMessage());
  }

  @Test
  void multipleRootElements() {
    final SAXParseException exception = Assertions.assertThrows(SAXParseException.class, () ->
        converter.execute(getXmlFileContentAsString("xmlInputInvalidMultipleRootElement.xml"), true, "#"));
    Assertions.assertEquals(
        "The markup in the document following the root element must be well-formed.",
        exception.getMessage());
  }

  @Test
  void flatVerySimpleXmlInputData()
      throws ParserConfigurationException, IOException, SAXException, JxmlException {
    final String inputXML =
        "<TransactionFlowLink link=\"Pippo\"></TransactionFlowLink>";

    final String result = converter.execute(inputXML);

    final String expectedJson =
        "{\"TransactionFlowLink\":{\"_link\":\"Pippo\"}}";
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void flatSimpleXmlInputData()
      throws ParserConfigurationException, IOException, SAXException, JxmlException {
    final String inputXML =
        "<TransactionFlowLink link=\"Pippo\">Ciccio</TransactionFlowLink>";

    final String result = converter.execute(inputXML);

    final String expectedJson =
        "{\"TransactionFlowLink\":{\"_link\":\"Pippo\",\"__text\":\"Ciccio\"}}";
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void flatXmlInputData()
      throws ParserConfigurationException, IOException, SAXException, JxmlException {
    final String inputXML =
        "<TransactionFlowLink link=\"Pippo\"><T1><T2>Ciccio1</T2><T2>Ciccio2</T2></T1></TransactionFlowLink>";
    final String result = converter.execute(inputXML);

    final String expectedJson =
        "{\"TransactionFlowLink\":{\"_link\":\"Pippo\",\"T1\":{\"T2\":[{\"__text\":\"Ciccio1\"},{\"__text\":\"Ciccio2\"}]}}}";
    Assertions.assertEquals(expectedJson, result);
  }

  @Test
  void commentsAreDropped()
      throws ParserConfigurationException, IOException, SAXException, JxmlException {

    final String xml =
        "<?xml version=\"1.1\" encoding=\"UTF-16\"?><!--This is a comment--><Element>ElementValue</Element>";
    final String XmltoJson = converter.execute(xml);

    final String expectedJson =
        "{\"Element\":{\"__text\":\"ElementValue\"}}";
    Assertions.assertEquals(expectedJson, XmltoJson);
  }

  @Test
  void readmeExample()
      throws ParserConfigurationException, IOException, SAXException, JxmlException {

    final String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Element>ElementValue</Element>";
    final String XmltoJson = converter.execute(xml);

    final String expectedJson =
        "{\"Element\":{\"__text\":\"ElementValue\"}}";
    Assertions.assertEquals(expectedJson, XmltoJson);
  }

  private static String getXmlFileContentAsString(final String fileName) throws IOException {
    return readResource(XML_RESOURCE_FOLDER + fileName);
  }

  private static String getJsonFileContentAsString(final String fileName) throws IOException {
    final String input = readResource(JSON_RESOURCE_FOLDER + fileName);
    final String[] inputs = input.split("\n");
    final StringBuilder in = new StringBuilder();
    for (final String inp : inputs) {
      in.append(inp.trim());
    }
    return in.toString();
  }

  public static String readResource(final String fileName) throws IOException {
    final ClassLoader classLoader = XmlToJsonTest.class.getClassLoader();
    final InputStream stream = classLoader.getResourceAsStream(fileName);
    Objects.requireNonNull(stream, "File not found: " + fileName + "\nFYI: Filename is case sensitive.");
    return IOUtils.toString(stream, Charset.defaultCharset());
  }

}
