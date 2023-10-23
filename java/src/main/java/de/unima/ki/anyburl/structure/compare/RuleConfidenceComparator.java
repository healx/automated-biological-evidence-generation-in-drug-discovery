package de.unima.ki.anyburl.structure.compare;

import de.unima.ki.anyburl.structure.Rule;
import java.util.Comparator;

public class RuleConfidenceComparator implements Comparator<Rule> {

  public RuleConfidenceComparator() {}

  public int compare(Rule o1, Rule o2) {
    // double prob1;
    // double prob2;
    double prob1 = o1.getAppliedConfidence();
    double prob2 = o2.getAppliedConfidence();

    if (prob1 < prob2) return 1;
    else if (prob1 > prob2) return -1;
    return 0;
  }
}
