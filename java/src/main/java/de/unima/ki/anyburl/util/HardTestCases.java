package de.unima.ki.anyburl.util;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.eval.ResultSet;
import java.util.ArrayList;

public class HardTestCases {

  public static void main(String[] args) {

    TripleSet test = new TripleSet("data/YAGO03-10/test.txt");

    ResultSet rs = new ResultSet("results", "exp/powerslave/YAGO03-10/beta2-10000", true, 10);

    int hardCounter = 0;

    for (Triple t : test.getTriples()) {
      boolean headSolved = false;
      boolean tailSolved = false;
      ArrayList<String> heads = rs.getHeadCandidates(t.toString());
      if (heads.contains(t.getHead())) {
        headSolved = true;
      }

      ArrayList<String> tails = rs.getTailCandidates(t.toString());
      if (tails.contains(t.getTail())) {
        tailSolved = true;
      }
      if (!tailSolved) {
        hardCounter++;
        System.out.println(hardCounter + ": " + t);
      }
    }
  }
}
