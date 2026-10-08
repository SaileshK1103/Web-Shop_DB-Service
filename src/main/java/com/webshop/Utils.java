package com.webshop;

import java.math.BigDecimal;

public class Utils {
    public static boolean isNonEmptyString(String value) {
      return value != null && !value.isBlank();
    }

  public static boolean isGreater(BigDecimal value1, BigDecimal value2) {
    return value1.compareTo(value2) > 0;
  }
}
