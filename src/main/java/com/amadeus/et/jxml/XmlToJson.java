package com.amadeus.et.jxml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class XmlToJson {

  public XmlToJson() {}

  /**
   * Returns a String object that represents the json conversion
   * of the input argument. The xml argument must be a valid XML.
   *
   * @param  xml  an XML, presented as string, to be converted to JSON format
   * @return      the converted object
   */
  public String execute(final String xml)
      throws JxmlException, ParserConfigurationException, SAXException, IOException {

    return buildJson(xml, true, null);
  }

  /**
   * Returns a String object that represents the json conversion
   * of the xml input argument. The xml argument must be a valid XML.
   * The keepNamespaces argument is used to specify if the conversion has
   * to keep or not XML namespaces.
   *
   * @param  xml             an XML, presented as string, to be converted to JSON format
   * @param  keepNamespaces  a boolean used to specify if the conversion has to keep Namespace information
   * @return                 the converted object
   */
  public String execute(final String xml, final boolean keepNamespaces)
      throws JxmlException, ParserConfigurationException, SAXException, IOException {

    return buildJson(xml, keepNamespaces, null);
  }

  /**
   * Returns a String object that represents the json conversion
   * of the xml input argument. The xml argument must be a valid XML.
   * The inputSpecialAttributePrefix argument is used to specify
   * the input special attribute prefix, by default '_', used to
   * identify uniquely the conversion of attributes and text elements.
   *
   * @param  xml                          an XML, presented as string, to be converted to JSON format
   * @param  inputSpecialAttributePrefix  a string used to specify the input special attribute prefix
   * @return                              the converted object
   */
  public String execute(final String xml, final String inputSpecialAttributePrefix)
      throws JxmlException, ParserConfigurationException, SAXException, IOException {

    return buildJson(xml, true, inputSpecialAttributePrefix);
  }

  /**
   * Returns a String object that represents the json conversion
   * of the xml input argument. The xml argument must be a valid XML.
   * The keepNamespaces argument is used to specify if the conversion has
   * to keep or not XML namespaces. The inputSpecialAttributePrefix argument
   * is used to specify the input special attribute prefix, by default '_', used
   * to identify uniquely the conversion of attributes and text elements.
   *
   * @param  xml                          an XML, presented as string, to be converted to JSON format
   * @param  keepNamespaces               a boolean used to specify if the conversion has to keep Namespace information
   * @param  inputSpecialAttributePrefix  a string used to specify the input special attribute prefix
   * @return                              the converted object
   */
  public String execute(final String xml, final boolean keepNamespaces, final String inputSpecialAttributePrefix)
      throws JxmlException, ParserConfigurationException, SAXException, IOException {

    return buildJson(xml, keepNamespaces, inputSpecialAttributePrefix);
  }

  private String buildJson(final String xml, final boolean keepNamespaces, final String inputSpecialAttributePrefix)
      throws JxmlException, ParserConfigurationException,
             SAXException, IOException {
    checkInputParameters(xml, inputSpecialAttributePrefix);
    final String specialAttributePrefix;
    if (inputSpecialAttributePrefix != null) {
      specialAttributePrefix = inputSpecialAttributePrefix;
    } else {
      specialAttributePrefix = JxmlConstants.DEFAULT_SPECIAL_ATTRIBUTE_PREFIX;
    }

    return convert(xml, keepNamespaces, specialAttributePrefix);
  }

  private void checkInputParameters(final String xml, final String specialAttributePrefix)
      throws JxmlException {
    Utilities.checkXml(xml);
    if (specialAttributePrefix != null) {
      Utilities.checkDelimiter(specialAttributePrefix);
    }
  }

  private String convert(final String xml, final boolean keepNamespace,
                         final String specialAttributePrefix) throws ParserConfigurationException,
                                                                     SAXException, IOException {

    final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    final DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    final Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
    doc.normalizeDocument();

    final Node rootNode = doc.getDocumentElement();
    final JsonObject rootData = new JsonObject();
    final String attributePrefix = specialAttributePrefix;
    final String textContentPrefix = specialAttributePrefix + specialAttributePrefix +
        JxmlConstants.TEXT_CONTENT_IDENTIFIER_SUFFIX;
    convertRecursively(rootNode, rootData, keepNamespace, attributePrefix, textContentPrefix);

    return gson.toJson(rootData);
  }

  private void convertRecursively(final Node node, final JsonObject nodeValue, final boolean keepNamespace,
                                  final String attributePrefix, final String textContentPrefix) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      final JsonObject innerJsonObject = new JsonObject();

      parseAttributes(node, keepNamespace, attributePrefix, innerJsonObject);
      iterateOnInnerNodes(node, keepNamespace, attributePrefix, textContentPrefix, innerJsonObject);
      addTextContent(node, textContentPrefix, innerJsonObject);

      nodeValue.add(getNodeName(node, keepNamespace), innerJsonObject);
    }
  }

  private void parseAttributes(final Node node, final boolean keepNamespace, final String attributePrefix,
                               final JsonObject jsonObject) {
    final NamedNodeMap nodeAttributes = node.getAttributes();
    for (int j = 0; j < nodeAttributes.getLength(); j++) {
      final Node attributes = nodeAttributes.item(j);
      if (keepNamespace) {
        jsonObject.addProperty(attributePrefix + attributes.getNodeName(), attributes.getNodeValue());
      } else {
        if (!attributes.getNodeValue().contains(":")) {
          jsonObject.addProperty(attributePrefix + attributes.getNodeName(), attributes.getNodeValue());
        }
      }
    }
  }

  private void iterateOnInnerNodes(final Node node, final boolean keepNamespace, final String attributePrefix,
                                   final String textContentPrefix, final JsonObject jsonObject) {
    final NodeList nodeList = node.getChildNodes();

    final Map<String, ArrayList<Integer>> nodeMap = extractNodeMap(nodeList, keepNamespace);

    final Map<String, JsonArray> arrayMap = new HashMap<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Node innerNode = nodeList.item(i);
      final String innerNodeName = getNodeName(innerNode, keepNamespace);

      if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
        if (nodeMap.get(innerNodeName).size() == 1) {
          convertRecursively(innerNode, jsonObject, keepNamespace, attributePrefix, textContentPrefix);
        } else {
          if (arrayMap.containsKey(innerNodeName)) {
            final JsonArray jsonArray = arrayMap.get(innerNodeName);
            iterateOnArray(innerNode, keepNamespace, attributePrefix, textContentPrefix, jsonArray);
          } else {
            final JsonArray jsonArray = new JsonArray();
            iterateOnArray(innerNode, keepNamespace, attributePrefix, textContentPrefix, jsonArray);
            arrayMap.put(innerNodeName, jsonArray);
          }
        }
      }
    }
    final Iterator<Map.Entry<String, JsonArray>> iterator = arrayMap.entrySet().iterator();
    while (iterator.hasNext()) {
      final Map.Entry<String, JsonArray> mapPair = iterator.next();
      jsonObject.add(mapPair.getKey(), mapPair.getValue());
      iterator.remove();
    }
  }

  private void iterateOnArray(final Node node, final boolean keepNamespace, final String attributePrefix,
                              final String textContentPrefix, final JsonArray jsonArray) {
    final JsonObject iterationJsonObject = new JsonObject();
    convertRecursively(node, iterationJsonObject, keepNamespace, attributePrefix, textContentPrefix);
    jsonArray.add(iterationJsonObject.get(getNodeName(node, keepNamespace)));
  }

  private void addTextContent(final Node node, final String textContentPrefix, final JsonObject jsonObject) {
    if (node.hasChildNodes() &&  node.getFirstChild().getNodeValue() != null
        && !"".equals(node.getFirstChild().getNodeValue()
                          .replaceAll("\n|\t","")
                          .replace(" ",""))) {
      jsonObject.addProperty(textContentPrefix, normalizeText(node.getFirstChild().getNodeValue()));
    }
  }

  private Map<String, ArrayList<Integer>> extractNodeMap(final NodeList nodeList, final boolean keepNamespace) {
    final Map<String, ArrayList<Integer>> nodeMap = new HashMap<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      final Node node = nodeList.item(i);
      final String nodeName = getNodeName(node, keepNamespace);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (nodeMap.containsKey(nodeName)) {
          nodeMap.get(nodeName).add(i);
        } else {
          final ArrayList<Integer> nodeIndexes = new ArrayList<>(); // we need a not fixed-size list
          nodeIndexes.add(i);
          nodeMap.put(nodeName, nodeIndexes);
        }
      }
    }
    return nodeMap;
  }

  private String getNodeName(final Node node, final boolean keepNamespace) {
    String nodeName = node.getNodeName();
    if (!keepNamespace && nodeName.contains(":")) {
      nodeName = nodeName.split(":", JxmlConstants.ATTRIBUTE_NAME_SPLIT_LIMIT)[1];
    }
    return nodeName;
  }

  private String normalizeText(final String text) {
    return text.replaceAll("\n|\t"," ")
        .replaceAll(" +"," ")
        .trim();
  }

}
