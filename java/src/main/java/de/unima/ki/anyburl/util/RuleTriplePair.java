package de.unima.ki.anyburl.util;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.structure.Rule;

public class RuleTriplePair {

  public Triple t;
  public Rule r;

  public RuleTriplePair(Rule r, Triple t) {
    this.r = r;
    this.t = t;
  }
}
