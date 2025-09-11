package com.zl.mjga.utils;

public class StringCaseUtils {
  public static String convertCamelCaseToSnake(String input) {
    StringBuilder result = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isUpperCase(c)) {
        result.append("_").append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
}
