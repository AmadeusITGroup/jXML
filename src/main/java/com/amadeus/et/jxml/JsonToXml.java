package com.amadeus.et.jxml;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonToXml {

  public JsonToXml() {}

  /**
   * Returns a String object that represents the XML conversion
   * of the input argument. The json argument must be a valid JSON.
   *
   * @param  json  a JSON, presented as string, to be converted to XML format
   * @return      the converted object
   */
  public String execute(final String json)
      throws JxmlException, ParserConfigurationException, TransformerException {

    return buildXml(json, true, null);
  }

  /**
   * Returns a String object that represents the XML conversion
   * of the input argument. The json argument must be a valid JSON.
   * The keepNamespaces argument is used to specify if the conversion has
   * to keep namespace information.
   *
   * @param  json            a JSON, presented as string, to be converted to XML format
   * @param  keepNamespaces  a boolean used to specify if the conversion has to keep Namespace information
   * @return                 the converted object
   */
  public String execute(final String json, final boolean keepNamespaces)
      throws JxmlException, ParserConfigurationException, TransformerException {

    return buildXml(json, keepNamespaces, null);
  }

  /**
   * Returns a String object that represents the XML conversion
   * of the input argument. The json argument must be a valid JSON.
   * The inputSpecialAttributePrefix argument is used to specify
   * the input special attribute prefix, by default '_', used
   * to identify uniquely the conversion of attributes and text elements.
   *
   * @param  json                         a JSON, presented as string, to be converted to XML format
   * @param  inputSpecialAttributePrefix  a string used to specify the input special attribute prefix
   * @return                              the converted object
   */
  public String execute(final String json, final String inputSpecialAttributePrefix)
      throws JxmlException, ParserConfigurationException, TransformerException {

    return buildXml(json, true, inputSpecialAttributePrefix);
  }

  /**
   * Returns a String object that represents the XML conversion
   * of the input argument. The xml argument must be a valid JSON.
   * The keepNamespaces argument is used to specify if the conversion has
   * to keep namespace information. The inputSpecialAttributePrefix argument
   * is used to specify the input special attribute prefix, by default '_', used
   * to identify uniquely the conversion of attributes and text elements.
   *
   * @param  json                         a JSON, presented as string, to be converted to XML format
   * @param  keepNamespaces               a boolean used to specify if the conversion has to keep Namespace information
   * @param  inputSpecialAttributePrefix  a string used to specify the input special attribute prefix
   * @return                              the converted object
   */
  public String execute(final String json, final boolean keepNamespaces, final String inputSpecialAttributePrefix)
      throws JxmlException, ParserConfigurationException, TransformerException {

    return buildXml(json, keepNamespaces, inputSpecialAttributePrefix);
  }

  private String buildXml(final String json, final boolean keepNamespaces, final String inputSpecialAttributePrefix)
      throws JxmlException, ParserConfigurationException, TransformerException {
    checkInputParameters(json, inputSpecialAttributePrefix);
    final String specialAttributePrefix;
    if (inputSpecialAttributePrefix != null) {
      specialAttributePrefix = inputSpecialAttributePrefix;
    } else {
      specialAttributePrefix = JxmlConstants.DEFAULT_SPECIAL_ATTRIBUTE_PREFIX;
    }

    return convert(json, keepNamespaces, specialAttributePrefix);
  }

  private void checkInputParameters(final String json, final String specialAttributePrefix)
      throws JxmlException {
    Utilities.checkJson(json);
    if (specialAttributePrefix != null) {
      Utilities.checkDelimiter(specialAttributePrefix);
    }
  }

  private String convert(final String json, final boolean keepNamespace,
                         final String specialAttributePrefix) throws ParserConfigurationException,
                                                                     TransformerException {


    final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    final Document doc = builder.newDocument();
    doc.setXmlStandalone(true);

    final JsonElement jelement = new JsonParser().parse(json);

    final JsonObject rootNode = jelement.getAsJsonObject();

    final Element rootData = doc.createElement(getRootName(rootNode, keepNamespace));
    doc.appendChild(rootData);
    final String attributePrefix = specialAttributePrefix;
    final String textContentPrefix = specialAttributePrefix + specialAttributePrefix +
        JxmlConstants.TEXT_CONTENT_IDENTIFIER_SUFFIX;
    JsonElement jsonEntry = null;
    // if coming from XML conversion, only one entry is expected
    // anyway, XML can have only one root element
    for(final Map.Entry<String, JsonElement> entry : rootNode.entrySet()) {
      jsonEntry = entry.getValue();
    }
    convertRecursively(jsonEntry.getAsJsonObject(), rootData, keepNamespace, attributePrefix, textContentPrefix, doc);

    return convertDomToString(doc);
  }

  private void convertRecursively(final JsonObject node, final Element nodeValue, final boolean keepNamespace,
                                  final String attributePrefix, final String textContentPrefix, final Document doc) {

    for(final Map.Entry<String, JsonElement> entry : node.entrySet()) {
      //text
      if (entry.getKey().length()>=JxmlConstants.TEXT_CONTENT_PREFIX_LENGTH &&
          entry.getKey().substring(0,JxmlConstants.TEXT_CONTENT_PREFIX_LENGTH).equals(textContentPrefix)) {
        nodeValue.appendChild(doc.createTextNode(entry.getValue().toString()
                                                     .substring(1, entry.getValue().toString().length()-1)));
      }
      //attribute
      else if (entry.getKey().length()>=JxmlConstants.ATTRIBUTE_PREFIX_PLUS_CHARACTER_LENGTH &&
          entry.getKey().charAt(0)==attributePrefix.charAt(0)) {
        if (!(!keepNamespace && entry.getKey().contains(":"))) {
          nodeValue.setAttribute(entry.getKey().substring(1), entry.getValue().toString()
              .substring(1, entry.getValue().toString().length()-1));
        }
      }
      //inner object
      else {
        if (entry.getValue().isJsonArray()) {
          for (final JsonElement arrayValue : entry.getValue().getAsJsonArray()) {
            final Element child = doc.createElement(getElementName(entry.getKey(), keepNamespace));
            nodeValue.appendChild(child);
            convertRecursively(arrayValue.getAsJsonObject(), child, keepNamespace, attributePrefix, textContentPrefix, doc);
          }
        }
        else {
          final Element child = doc.createElement(getElementName(entry.getKey(), keepNamespace));
          nodeValue.appendChild(child);
          convertRecursively(entry.getValue().getAsJsonObject(), child, keepNamespace, attributePrefix, textContentPrefix, doc);
        }
      }
    }
  }

  private String getRootName(final JsonObject rootNode, final boolean keepNamespace) {
    final Set<String> keys = rootNode.keySet();
    final Iterator<String> iterator = keys.iterator();
    final String rootName = iterator.next();
    return getElementName(rootName, keepNamespace);
  }

  private String getElementName(final String basicName, final boolean keepNamespace) {
    if (!keepNamespace && basicName.contains(":")) {
      return basicName.split(":", JxmlConstants.ATTRIBUTE_NAME_SPLIT_LIMIT)[1];
    }
    return basicName;
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
