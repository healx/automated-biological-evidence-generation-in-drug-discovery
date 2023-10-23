package de.unima.ki.anyburl.structure.compare;

import de.unima.ki.anyburl.structure.Body;
import java.util.Comparator;

public class BodyLexicalComparator implements Comparator<Body> {

  public int compare(Body b1, Body b2) {
    return b1.toString().compareTo(b2.toString());
  }
}
