package de.unima.ki.anyburl.util;

import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.io.RuleReader;
import de.unima.ki.anyburl.io.RuleWriter;
import de.unima.ki.anyburl.structure.Rule;
import java.io.IOException;
import java.util.LinkedList;

/** Loads a rule set computed without OI and checks how much is removed if OI is activated. */
public class ObjectIdentity {

  public static void main(String[] args) throws IOException {

    Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS = 1000;
    Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS = 100000;
    Settings.BEAM_SAMPLING_MAX_REPETITIONS = 5;

    String trainingPath = "data/WN18/train.txt";
    String rulePath = "exp/january/oi/wn18-rules-c5-s10c01-off-1000";
    String rulePathOut = "exp/january/oi/wn18-rules-c5-s10c01-on-1000";

    TripleSet ts = new TripleSet(trainingPath);

    RuleReader rr = new RuleReader();
    LinkedList<Rule> rulesAll = rr.read(rulePath);
    LinkedList<Rule> rulesOI = new LinkedList<Rule>();

    int[] counterAll = new int[5];
    int[] counterOI = new int[5];

    int c = 0;
    int oi = 0;
    for (Rule r : rulesAll) {
      // if (r.bodysize() > 1) continue;
      c++;
      if (c % 100 == 0) System.out.println("iterated over " + c + " rules (" + oi + "/" + c + ")");
      // System.out.println();
      int l = r.bodysize();
      counterAll[l - 1]++;

      // System.out.println("off: " + r);

      r.computeScores(ts);

      // System.out.println("on:  " + r);
      // System.out.println();

      if (r.getConfidence() >= 0.05 && r.getCorrectlyPredicted() >= 5) {
        counterOI[l - 1]++;
        // System.out.println(r);
        oi++;
        rulesOI.add(r);
      }
    }

    int sumAll = 0;
    int sumOI = 0;
    for (int i = 0; i < 5; i++) {
      sumAll += counterAll[i];
      sumOI += counterOI[i];
      System.out.println(i + ": " + counterAll[i] + " -> " + counterOI[i]);
    }

    System.out.println("total: " + sumAll + " -> " + sumOI);

    RuleWriter rw = new RuleWriter();
    rw.write(rulesOI, rulePathOut);
  }
}
