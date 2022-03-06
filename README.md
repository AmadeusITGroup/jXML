# JXML
ET - Easy Transform.

JXML provides lossless bi-directional XML-JSON conversion, without any static information requested about 
the data to be converted (no XSD, binding classes or other), and with proper namespace support.


JXML compiles to the JVM 8, and so is compatible with JVM 8 and up.

## How it works

XML and JSON are not equivalent, in order to enable lossless conversions some conventions
have been adopted:
- XML elements: converted into a json object;
- Attributes: converted into a json member, identified using a predefined (and customizable) prefix;
- Namespaces: converted into a json member; XML namespace prefixes by default are kept as part of the json object name;
- Text: converted into a json member, identified using a double predefined (and customizable) prefix, plus 'text';
- XML arrays: there is no XML array, XML arrays are normally identified using XSD and so on; in this project, if (and only if) multiple XML elements with the same name are found under the same element, they are converted into a json array;
- Numbers, integers and so on: in order to avoid losing digits (example of possible loss: 2.0000 -> 2), currently everything is converted into a string;
- XML comments: currently they are dropped;
- XML declaration: currently it is dropped, so json -> XML conversion will assume version '1.0', encoding 'UTF-8' and standalone 'no';


In our conversion, a prefix is a customizable character (pattern: [._=!@#~%&*^?,-]). By default, it is set to '_'.
In order to execute lossless bi-direction conversions, both XML -> JSON and JSON -> XML conversions
MUST use the same prefix.

JXML provides the possibility to skip namespace conversion. Skipping namespace conversion,
also namespace prefix will be skipped. Obviously, this will not allow the possibility to go back to the complete original XML.

Conversion example:
```starting xml
<?xml version="1.0" encoding="UTF-8"?>
<ext:Envelope attr="test" xmlns:ext="http://schemas.xmltest.org/test/ext/" xmlns:in="http://schemas.xmltest.org/test/in/">
  <in:element attr="test2" newattr="otherAttribute">
    Header 1
  </in:element>
</ext:Envelope>
```
```obtained json
{
  "ext:Envelope":{
    "_attr":"test",
    "_xmlns:ext":"http://schemas.xmltest.org/test/ext/",
    "_xmlns:in":"http://schemas.xmltest.org/test/in/",
    "in:element":{
      "_attr":"test2",
      "_newattr":"otherAttribute",
      "__text":"Header 1"
    }
  }
}
```

## How to use it

### API
XML -> json conversion
```
com.amadeus.et.jxml

Class XmlToJson
public String execute(final String xml)
public String execute(final String xml, final boolean keepNamespaces)
public String execute(final String xml, final String inputSpecialAttributePrefix)
public String execute(final String xml, final boolean keepNamespaces, final String inputSpecialAttributePrefix)
```
json -> XML conversion
```
com.amadeus.et.jxml

Class JsonToXml
public String execute(final String json)
public String execute(final String json, final boolean keepNamespaces)
public String execute(final String json, final String inputSpecialAttributePrefix)
public String execute(final String json, final boolean keepNamespaces, final String inputSpecialAttributePrefix)
```
Parameter detailed description is provided in the associated JavaDoc.


### Dependency

Add it as a dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>com.amadeus.et</groupId>
  <artifactId>jxml</artifactId>
  <version>0.1</version>
</dependency>
```
### XML to JSON conversion

```java
final String xml = "<?xml version="1.0" encoding="UTF-8"?><Element>ElementValue</Element>";
final XmltoJson converter = new XmlToJson();
final String json = converter.execute(xml);
```

### JSON to XML conversion

```java
final String json = "{"Element":{"__text":"ElementValue"}}";
final JsonToXml converter = new JsonToXml();
final String xml = converter.execute(json);
```

## Contributions

We welcome all contributions!

At all times, ensure that your unit-tests correctly work by doing a double conversion.
For example, json -> xml -> json, and compare both input and output json are semantically the same.

Other lossless conversion may be added to ET package in the future, expecting they will follow the library philosophy:
no static information have to be requested to the user, just the input data and the expected output format.

## How to build & test

`docker build .`

OR

`mvn -T1C clean install -P coverage-per-test`
