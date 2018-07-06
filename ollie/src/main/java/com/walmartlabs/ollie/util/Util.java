package com.walmartlabs.ollie.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Junk drawer of utility methods. */
public final class Util {

  public static final Comparator<String> NATURAL_ORDER = new Comparator<String>() {
    @Override public int compare(String a, String b) {
      return a.compareTo(b);
    }
  };

  private Util() {
  }

  /**
   * Returns an array containing only elements found in {@code first} and also in {@code
   * second}. The returned elements are in the same order as in {@code first}.
   */
  @SuppressWarnings("unchecked")
  public static String[] intersect(
      Comparator<? super String> comparator, String[] first, String[] second) {
    List<String> result = new ArrayList<>();
    for (String a : first) {
      for (String b : second) {
        if (comparator.compare(a, b) == 0) {
          result.add(a);
          break;
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns true if there is an element in {@code first} that is also in {@code second}. This
   * method terminates if any intersection is found. The sizes of both arguments are assumed to be
   * so small, and the likelihood of an intersection so great, that it is not worth the CPU cost of
   * sorting or the memory cost of hashing.
   */
  public static boolean nonEmptyIntersection(
      Comparator<String> comparator, String[] first, String[] second) {
    if (first == null || second == null || first.length == 0 || second.length == 0) {
      return false;
    }
    for (String a : first) {
      for (String b : second) {
        if (comparator.compare(a, b) == 0) {
          return true;
        }
      }
    }
    return false;
  }

  public static int indexOf(Comparator<String> comparator, String[] array, String value) {
    for (int i = 0, size = array.length; i < size; i++) {
      if (comparator.compare(array[i], value) == 0) return i;
    }
    return -1;
  }

  public static String[] concat(String[] array, String value) {
    String[] result = new String[array.length + 1];
    System.arraycopy(array, 0, result, 0, array.length);
    result[result.length - 1] = value;
    return result;
  }
}