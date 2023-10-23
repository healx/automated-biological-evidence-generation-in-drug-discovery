package de.unima.ki.anyburl.util;

import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.io.RuleReader;
import de.unima.ki.anyburl.structure.Rule;

public class RuleExplorer {

  public static void main(String[] args) {

    TripleSet ts = new TripleSet("data/YAGO03-10/train.txt");

    RuleReader rr = new RuleReader();

    Rule rNeg =
        rr.getRule(
            "10	10	1.0	isLocatedIn(X,United_States) <= isLocatedIn(X,A), livesIn(B,A),"
                + " livesIn(B,United_States), !isLocatedIn(X,United_Kingdom)");
    Rule r =
        rr.getRule(
            "10	10	1.0	isLocatedIn(X,United_States) <= isLocatedIn(X,A), livesIn(B,A),"
                + " livesIn(B,United_States)");

    for (int i = 50; i < 20000; i += 1000) {
      Settings.SAMPLE_SIZE = i;
      r.computeScores(ts);
      rNeg.computeScores(ts);
      // System.out.println("SAMPLE_SIZE=" + i + ": r=" + r.getCorrectlyPredicted() + " " +
      // r.getConfidence() + " rNeg=" + rNeg.getCorrectlyPredicted() + " " + rNeg.getConfidence());
    }

    // r.computeScores(ts);
    // System.out.println(r.getConfidence());

  }
}
