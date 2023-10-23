package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import java.util.Set;

public class TestHardnessEstimator {

  public static void main(String[] args) {

    TripleSet training = new TripleSet("data/FB15-237/train.txt");
    TripleSet test = new TripleSet("data/FB15-237/test.txt");
    TripleSet valid = new TripleSet("data/FB15-237/valid.txt");

    /*
    TripleSet training = new TripleSet("data/FB15k/train.txt");
    TripleSet test = new TripleSet("data/FB15k/test.txt");
    TripleSet valid = new TripleSet("data/FB15k/valid.txt");
    */

    TripleSet all = new TripleSet();
    all.addTripleSet(training);
    all.addTripleSet(test);
    all.addTripleSet(valid);

    int[] headsCounter = new int[5];
    int[] tailsCounter = new int[5];

    double unknownRateHead = 0.1;
    double unknownRateTail = 0.1;

    double hitsAT1RateHead = 0.0;
    double hitsAT1RateTail = 0.0;

    for (Triple t : test.getTriples()) {

      // System.out.print(t);

      String head = t.getHead();
      String tail = t.getTail();
      String relation = t.getRelation();

      Set<String> heads = all.getHeadEntities(relation, tail);
      Set<String> tails = all.getTailEntities(relation, head);

      double d1 = 1.0 / (heads.size() * unknownRateHead);
      if (d1 >= 1.0) d1 = 1.0;
      hitsAT1RateHead += d1;

      double d2 = 1.0 / (tails.size() * unknownRateTail);
      if (d2 >= 1.0) d2 = 1.0;
      hitsAT1RateTail += d2;

      // System.out.println(d);

      // System.out.println(tails.size() + "|" + heads.size() + " |" +  t);

      if (heads.size() == 1) headsCounter[0]++;
      if (heads.size() > 1 && heads.size() <= 10) headsCounter[1]++;
      if (heads.size() > 10 && heads.size() <= 50) headsCounter[2]++;
      if (heads.size() > 50 && heads.size() <= 200) headsCounter[3]++;
      if (heads.size() > 200) headsCounter[4]++;

      if (tails.size() == 1) tailsCounter[0]++;
      if (tails.size() > 1 && tails.size() <= 10) tailsCounter[1]++;
      if (tails.size() > 10 && tails.size() <= 50) tailsCounter[2]++;
      if (tails.size() > 50 && tails.size() <= 200) tailsCounter[3]++;
      if (tails.size() > 200) tailsCounter[4]++;
    }

    int allHeads = 0;
    int allTails = 0;
    for (int i = 0; i < 5; i++) {
      allHeads += headsCounter[i];
      allTails += tailsCounter[i];
    }

    for (int i = 0; i < 5; i++) {
      System.out.print((headsCounter[i] / (double) allHeads) + "\t");
    }
    System.out.println();

    for (int i = 0; i < 5; i++) {
      System.out.print((tailsCounter[i] / (double) allTails) + "\t");
    }
    System.out.println();

    System.out.println(
        headsCounter[0]
            + "\t"
            + headsCounter[1]
            + "\t"
            + headsCounter[2]
            + "\t"
            + headsCounter[3]
            + "\t"
            + ((double) headsCounter[4] / allTails));
    System.out.println(
        tailsCounter[0]
            + "\t"
            + tailsCounter[1]
            + "\t"
            + tailsCounter[2]
            + "\t"
            + tailsCounter[3]
            + "\t"
            + tailsCounter[4]);

    System.out.println("hitsAT1RateHead =     " + hitsAT1RateHead / test.getTriples().size());
    System.out.println("hitsAT1RateTail =     " + hitsAT1RateTail / test.getTriples().size());
    System.out.println(
        "average hitsAT1Rate = "
            + ((hitsAT1RateTail / test.getTriples().size())
                    + (hitsAT1RateHead / test.getTriples().size()))
                / 2.0);
    // System.out.println("hitsAT1RateTail = " + hitsAT1RateTail / test.getTriples().size());

  }
}
