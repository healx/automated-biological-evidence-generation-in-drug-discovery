package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;

public class ResultSetStats {

  public static void main(String[] args) {

    ResultSet rs =
        new ResultSet("results", "exp/summer/analysis/YAGO/C3AC20995-all-10000-k10-ALL", true, 10);
    TripleSet test = new TripleSet("data/YAGO03-10/test.txt");

    int counterHeadLow = 0;
    int counterHeadMedium = 0;
    int counterHeadHigh = 0;

    int counterTailLow = 0;
    int counterTailMedium = 0;
    int counterTailHigh = 0;

    int counter = 0;
    for (Triple t : test.getTriples()) {
      counter++;
      double headR10conf = 0.0;
      double tailR10conf = 0.0;
      try {
        headR10conf = rs.getHeadConfidences(t.toString()).get(9);
      } catch (Exception e) {
      }
      try {
        tailR10conf = rs.getTailConfidences(t.toString()).get(9);
      } catch (Exception e) {
      }
      if (headR10conf > 0.5) counterHeadHigh++;
      if (headR10conf > 0.1 && headR10conf <= 0.5) counterHeadMedium++;
      if (headR10conf <= 0.1 && headR10conf > 0) counterHeadLow++;

      if (tailR10conf > 0.5) counterTailHigh++;
      if (tailR10conf > 0.1 && tailR10conf <= 0.5) counterTailMedium++;
      if (tailR10conf <= 0.1 && tailR10conf > 0) counterTailLow++;
    }

    System.out.println("Testcases: " + counter);
    System.out.println(
        "Head: "
            + counterHeadLow
            + " | "
            + counterHeadMedium
            + " | "
            + counterHeadHigh
            + " | "
            + (counterHeadLow + counterHeadMedium + counterHeadHigh));
    System.out.println(
        "Tail: "
            + counterTailLow
            + " | "
            + counterTailMedium
            + " | "
            + counterTailHigh
            + " | "
            + (counterHeadLow + counterHeadMedium + counterHeadHigh));
  }
}
