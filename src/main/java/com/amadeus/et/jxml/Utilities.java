package com.amadeus.et.jxml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class Utilities {
  private static final String ELEMENT_DELIMITER_PATTERN = "[._=!@#~%&*^?,-]";
  private static final String ERROR_DELIMITER_NOT_MATCHING_EXPECTED_PATTERN =
      "'xmlElementDelimiter' must follows this pattern: '[._=!@#~%&*^?,-]'.";
  private static final String ERROR_NULL_XML =
      "'xml' should not be null.";
  private static final String ERROR_NULL_JSON =
      "'json' should not be null.";
  private static final String ERROR_NO_ROOT_IN_JSON =
      "'json' should contain a root element to be converted to XML.";
  private static final String ERROR_NO_SINGLE_ROOT_IN_JSON =
      "'json' should contain only a single root element to be converted to XML.";

  static void checkDelimiter(final String delimiter) throws JxmlException {
    final Pattern pattern = Pattern.compile(ELEMENT_DELIMITER_PATTERN);
    final Matcher matcher = pattern.matcher(delimiter);
    if (!matcher.matches()) {
      throw new JxmlException(ERROR_DELIMITER_NOT_MATCHING_EXPECTED_PATTERN);
    }
  }

  static void checkXml(final String xml) throws JxmlException {
    if (xml == null) {
      throw new JxmlException(ERROR_NULL_XML);
    }
  }

  static void checkJson(final String json) throws JxmlException {
    if (json == null) {
      throw new JxmlException(ERROR_NULL_JSON);
    }

    final JsonElement jelement = new JsonParser().parse(json);
    final JsonObject rootNode = jelement.getAsJsonObject();
    if (rootNode.entrySet().size() == 0) {
      throw new JxmlException(ERROR_NO_ROOT_IN_JSON);
    }
    if (rootNode.entrySet().size() > 1) {
      throw new JxmlException(ERROR_NO_SINGLE_ROOT_IN_JSON);
    }
  }

}
