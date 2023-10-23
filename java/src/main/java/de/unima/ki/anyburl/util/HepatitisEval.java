package de.unima.ki.anyburl.util;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.eval.HitsAtK;
import de.unima.ki.anyburl.eval.ResultSet;
import java.io.IOException;
import java.util.ArrayList;

public class HepatitisEval {

  public static void main(String[] args) throws IOException {

    TripleSet trainingSet = new TripleSet("data/CL/Hepatitis/fine/train5.txt");
    TripleSet validationSet = new TripleSet();
    TripleSet testSet = new TripleSet("data/CL/Hepatitis/fine/test5.txt");

    ResultSet rs = new ResultSet("50s-conf50", "exp/hepatitis/predictions5-50", true, 2);

    HitsAtK hitsAtK = new HitsAtK();

    // hitsAtK.addFilterTripleSet(trainingSet);
    // hitsAtK.addFilterTripleSet(validationSet);
    //  hitsAtK.addFilterTripleSet(testSet);

    computeScores(rs, testSet, hitsAtK);

    System.out.print(rs.getName() + "   " + hitsAtK.getHitsAtK(0));
    hitsAtK.reset();
  }

  private static void computeScores(ResultSet rs, TripleSet gold, HitsAtK hitsAtK) {
    for (Triple t : gold.getTriples()) {
      ArrayList<String> cand2 = rs.getTailCandidates(t.toString());
      // System.out.print(cand.size() + "\t");
      String c2 = cand2.size() > 0 ? cand2.get(0) : "-";
      hitsAtK.evaluateTail(cand2, t);
      // System.out.println();
    }
  }
}
