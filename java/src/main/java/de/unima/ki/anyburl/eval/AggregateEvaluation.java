package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import java.io.IOException;
import java.util.ArrayList;

public class AggregateEvaluation {

  public static String[] categories =
      new String[] {"Symmetry", "Equivalence", "Subsumption", "Path", "Not covered"};

  // WN18 WN18RR FB15 FB15-237 YAGO
  public static String target = "FB15";

  // 0 = ALL, 1 = HEAD ONLY, 2 = TAIL ONLY
  public static int ALL_HEAD_TAIL = 0;

  public static void main(String[] args) throws IOException {

    TripleSet trainingSet = null, validationSet = null, testSet = null;
    GoldStandard gold = null;

    if (target.equals("WN18")) {
      trainingSet = new TripleSet("data/WN18/train.txt");
      validationSet = new TripleSet("data/WN18/valid.txt");
      testSet = new TripleSet("data/WN18/test.txt");
      gold = new GoldStandard("data/WN18/gold.txt");
    }
    if (target.equals("FB15")) {
      trainingSet = new TripleSet("data/FB15k/train.txt");
      validationSet = new TripleSet("data/FB15k/valid.txt");
      testSet = new TripleSet("data/FB15k/test.txt");
      gold = new GoldStandard("data/FB15k/gold.txt");
    }

    if (target.equals("FB15-237")) {
      trainingSet = new TripleSet("data/FB15-237/train.txt");
      validationSet = new TripleSet("data/FB15-237/valid.txt");
      testSet = new TripleSet("data/FB15-237/test.txt");
      gold = new GoldStandard("data/FB15-237/gold.txt");
    }

    if (target.equals("WN18RR")) {
      trainingSet = new TripleSet("data/WN18RR/train.txt");
      validationSet = new TripleSet("data/WN18RR/valid.txt");
      testSet = new TripleSet("data/WN18RR/test.txt");
      // gold = new GoldStandard("../RuleN18/data/WN18RR/gold.txt");
    }
    if (target.equals("YAGO")) {
      trainingSet = new TripleSet("data/YAGO03-10/train.txt");
      validationSet = new TripleSet("data/YAGO03-10/valid.txt");
      testSet = new TripleSet("data/YAGO03-10/test.txt");
      gold = null;
    }
    if (target.equals("DB500")) {
      trainingSet = new TripleSet("data/DB500/train.txt");
      validationSet = new TripleSet("data/DB500/valid.txt");
      testSet = new TripleSet("data/DB500/test.txt");
      gold = null;
    }
    if (target.equals("ASS")) {
      trainingSet = new TripleSet("experiments/SemAssocs/data/empty.txt");
      validationSet = new TripleSet("experiments/SemAssocs/data/empty.txt");
      testSet = new TripleSet("experiments/SemAssocs/data/assoc_test.nt");
      gold = null;
    }

    if (target.equals("MOB")) {
      trainingSet = new TripleSet("data/mob/train.txt");
      validationSet = new TripleSet("data/mob/valid.txt");
      testSet = new TripleSet("data/mob/test.txt");
      gold = null;
    }

    if (target.equals("WD")) {
      trainingSet = new TripleSet("data/WIKIDATA/empty.txt");
      validationSet = new TripleSet("data/WIKIDATA/empty.txt");
      testSet = new TripleSet("data/WIKIDATA/test.txt");
      gold = null;
    }

    ResultSet[] results = null;

    boolean html = false;
    if (target.equals("ASS")) {

      results =
          new ResultSet[] {
            new ResultSet("RuleN", "experiments/SemAssocs/predictions/p12-s200.txt", true, 100),
            new ResultSet("RuleN", "experiments/SemAssocs/predictions/p12-s200-mul.txt", true, 100),
            new ResultSet("RuleN", "experiments/SemAssocs/predictions/p123-s200.txt", true, 100),
            new ResultSet("RuleN", "experiments/SemAssocs/predictions/p123-s500.txt", true, 100),
          };
    }

    if (target.equals("WD")) {

      results =
          new ResultSet[] {
            new ResultSet(
                "500      ",
                "exp/february/final/rg/wikidata-rt-c2a1g1-p1s1-predictionsZ-AVG-500",
                true,
                10),
            new ResultSet(
                "1000     ",
                "exp/february/final/rg/wikidata-rt-c2a1g1-p1s1-predictionsZ-AVG-1000",
                true,
                10),
            new ResultSet(
                "5000     ",
                "exp/february/final/rg/wikidata-rt-c2a1g1-p1s1-predictionsZ-AVG-5000",
                true,
                10),
            new ResultSet(
                "10000    ",
                "exp/february/final/rg/wikidata-rt-c2a1g1-p1s1-predictionsZ-AVG-10000",
                true,
                10),
          };
    }

    if (target.equals("MOB")) {

      results =
          new ResultSet[] {
            new ResultSet("RuleN", "exp/summer/mob/mob-rules-pred-100", true, 100),
          };
    }

    if (target.equals("WN18")) {

      results =
          new ResultSet[] {
            new ResultSet(
                "100    5    ", "exp/february/final/free/wn18-c5a1g1-p1s1-preds-100", true, 10),
            new ResultSet(
                "500    5    ", "exp/february/final/free/wn18-c5a1g1-p1s1-preds-500", true, 10),
            new ResultSet(
                "1000   5    ", "exp/february/final/free/wn18-c5a1g1-p1s1-preds-1000", true, 10),
            new ResultSet(
                "5000   5    ", "exp/february/final/free/wn18-c5a1g1-p1s1-preds-5000", true, 10),
            new ResultSet(
                "10000  5    ", "exp/february/final/free/wn18-c5a1g1-p1s1-preds-10000", true, 10),
          };
    }

    if (target.equals("WN18RR")) {

      results =
          new ResultSet[] {
            new ResultSet(
                "100   5     ", "exp/february/final/free/wn18rr-c5a1g1-p1s1-preds-100", true, 10),
            new ResultSet(
                "500   5     ", "exp/february/final/free/wn18rr-c5a1g1-p1s1-preds-500", true, 10),
            new ResultSet(
                "1000  5     ", "exp/february/final/free/wn18rr-c5a1g1-p1s1-preds-1000", true, 10),
            new ResultSet(
                "5000  5     ", "exp/february/final/free/wn18rr-c5a1g1-p1s1-preds-5000", true, 10),
            new ResultSet(
                "10000 5     ", "exp/february/final/free/wn18rr-c5a1g1-p1s1-preds-10000", true, 10),
            null,
          };
    }

    if (target.equals("YAGO")) {

      results =
          new ResultSet[] {
            new ResultSet(
                "p2s5   49", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-49", true, 10),
            new ResultSet(
                "p2s5   51", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-51", true, 10),
            new ResultSet(
                "p2s5   50", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-50", true, 10),
            null,
            new ResultSet(
                "p2s5   99", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-99", true, 10),
            new ResultSet(
                "p2s5  101", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-101", true, 10),
            new ResultSet(
                "p2s5  100", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-100", true, 10),
            null,
            new ResultSet(
                "p2s5  449", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-499", true, 10),
            new ResultSet(
                "p2s5  501", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-501", true, 10),
            new ResultSet(
                "p2s5  500", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-500", true, 10),
            null,
            new ResultSet(
                "p2s5  999", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-999", true, 10),
            new ResultSet(
                "p2s5 1001", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-1001", true, 10),
            new ResultSet(
                "p2s5 1000", "exp/february/ext/yagoxxx-c3a1-p2s5-predictions-1000", true, 10),
            null,
          };
    }

    if (target.equals("DB500")) {

      results =
          new ResultSet[] {
            new ResultSet("100     ", "exp/january/reinforced/db500-predictions-100", true, 10),
          };
    }

    if (target.equals("FB15-237")) {

      results =
          new ResultSet[] {
            new ResultSet(
                "p2s5   49", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-49", true, 10),
            new ResultSet(
                "p2s5   51", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-51", true, 10),
            new ResultSet(
                "p2s5   50", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-50", true, 10),
            null,
            new ResultSet(
                "p2s5   99", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-99", true, 10),
            new ResultSet(
                "p2s5  101", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-101", true, 10),
            new ResultSet(
                "p2s5  100", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-100", true, 10),
            null,
            new ResultSet(
                "p2s5  449", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-499", true, 10),
            new ResultSet(
                "p2s5  501", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-501", true, 10),
            new ResultSet(
                "p2s5  500", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-500", true, 10),
            null,
            new ResultSet(
                "p2s5  999", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-999", true, 10),
            new ResultSet(
                "p2s5 1001", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-1001", true, 10),
            new ResultSet(
                "p2s5 1000", "exp/february/ext/fb237xxx-c3a1-p2s5-predictions-1000", true, 10),
            null,
          };
    }

    if (target.equals("FB15")) {

      results =
          new ResultSet[] {
            new ResultSet(
                "p2s5   49", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-49", true, 10),
            new ResultSet(
                "p2s5   51", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-51", true, 10),
            new ResultSet(
                "p2s5   50", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-50", true, 10),
            new ResultSet(
                "p2s5   49", "exp/february/ext/fb15yyy-c3a1-p2s5-predictions-49", true, 10),
            new ResultSet(
                "p2s5   51", "exp/february/ext/fb15yyy-c3a1-p2s5-predictions-51", true, 10),
            new ResultSet(
                "p2s5   50", "exp/february/ext/fb15yyy-c3a1-p2s5-predictions-50", true, 10),
            null,
            new ResultSet(
                "p2s5   99", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-99", true, 10),
            new ResultSet(
                "p2s5  101", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-101", true, 10),
            new ResultSet(
                "p2s5  100", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-100", true, 10),
            new ResultSet(
                "p2s5   99", "exp/february/ext/fb15yyy-c3a1-p2s5-predictions-99", true, 10),
            new ResultSet(
                "p2s5  101", "exp/february/ext/fb15yyy-c3a1-p2s5-predictions-101", true, 10),
            new ResultSet(
                "p2s5  100", "exp/february/ext/fb15yyy-c3a1-p2s5-predictions-100", true, 10),
            null,
            new ResultSet(
                "p2s5  449", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-499", true, 10),
            new ResultSet(
                "p2s5  501", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-501", true, 10),
            new ResultSet(
                "p2s5  500", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-500", true, 10),
            null,
            new ResultSet(
                "p2s5  999", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-999", true, 10),
            new ResultSet(
                "p2s5 1001", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-1001", true, 10),
            new ResultSet(
                "p2s5 1000", "exp/february/ext/fb15xxx-c3a1-p2s5-predictions-1000", true, 10),
            null,
          };
    }

    HitsAtK hitsAtK = new HitsAtK();

    hitsAtK.addFilterTripleSet(trainingSet);
    hitsAtK.addFilterTripleSet(validationSet);
    hitsAtK.addFilterTripleSet(testSet);

    GoldStandard goldSymmetry = null;
    GoldStandard goldEquivalence = null;
    GoldStandard goldSubsumption = null;
    GoldStandard goldPath = null;
    GoldStandard goldUncovered = null;

    if (gold != null) {
      goldSymmetry = gold.getSubset("Symmetry");
      goldEquivalence = gold.getSubset("Equivalence");
      goldSubsumption = gold.getSubset("Subsumption");
      goldPath = gold.getSubset("Path");
      goldUncovered = gold.getSubset("Not covered");
    }

    // symmetry
    double hitsAT1All = 0.0;
    double hitsAT10All = 0.0;
    String resultName = "";
    int resultCounter = 0;

    for (ResultSet rs : results) {

      if (rs == null) {
        double hitsAT1 = hitsAT1All / (double) resultCounter;
        double hitsAT10 = hitsAT10All / (double) resultCounter;
        System.out.println(resultName + "\t" + HitsAtK.f(hitsAT1) + "\t" + HitsAtK.f(hitsAT10));
        hitsAT1All = 0.0;
        hitsAT10All = 0.0;
        resultCounter = 0;

      } else {
        computeScores(rs, testSet, hitsAtK);
        // System.out.println(rs.getName() + "   " + hitsAtK.getHitsAtKDouble(0) + "   " +
        // hitsAtK.getHitsAtKDouble(9));
        hitsAT1All += hitsAtK.getHitsAtKDouble(0);
        hitsAT10All += hitsAtK.getHitsAtKDouble(9);
        resultName = rs.getName();
        resultCounter++;
        hitsAtK.reset();
      }
      // computeScores(rs, testSet, hitsAtK);
      // System.out.println(hitsAtK);

      // computeScores(rs, testSet, hitsAtK);

      /*
      if (html == true) {
      	computeScores(rs, testSet, hitsAtK);
      	System.out.print("<tr><td><span class=\"important\">" + rs.getName() + "</span></td><td>" + hitsAtK.getHitsAtK(0) + "</td> <td>" + hitsAtK.getHitsAtK(9) + "</td> <td></td> <td></td> <td></td></tr>");
      	hitsAtK.reset();
      }

      else {
      	computeScores(rs, gold, hitsAtK);
      	// System.out.println(hitsAtK);
      	System.out.print(rs.getName() + "   " + hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "   " + hitsAtK.getApproxMRR() + "   ");

      	hitsAtK.reset();
      }
      /*

      	computeScores(rs, gold, hitsAtK);
      	System.out.print(rs.getName() + "   " + hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "   " + hitsAtK.getApproxMRR() + "   ");

      	System.out.println(hitsAtK);
      	hitsAtK.reset();


      	// symmetry
      	computeScores(rs, goldSymmetry, hitsAtK);
      	System.out.print( hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "   ");
      	hitsAtK.reset();


      	// equivalence
      	computeScores(rs, goldEquivalence, hitsAtK);
      	System.out.print( hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "   ");
      	hitsAtK.reset();
      	// subsumption
      	computeScores(rs, goldSubsumption, hitsAtK);
      	System.out.print( hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "   ");
      	hitsAtK.reset();
      	// Path
      	computeScores(rs, goldPath, hitsAtK);
      	System.out.print( hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "   ");
      	hitsAtK.reset();
      	// not covered
      	computeScores(rs, goldUncovered, hitsAtK);
      	System.out.print( hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "");
      	hitsAtK.reset();

      }
      */

    }
  }

  private static void computeScores(ResultSet rs, GoldStandard gold, HitsAtK hitsAtK) {
    int counter = 21;
    for (String triple : gold.triples) {
      String[] tt = triple.split(" ");
      Triple t = new Triple(tt[0], tt[1], tt[2]);
      // System.out.print(t+ "\t");
      if (gold.getCategory(triple, true) != null) {
        counter++;
        ArrayList<String> cand = rs.getHeadCandidates(triple);
        // System.out.print(cand.size() + "\t");
        // String c = cand.size() > 0 ? cand.get(0) : "-";
        hitsAtK.evaluateHead(cand, t);
      }
      if (gold.getCategory(triple, false) != null) {
        counter++;
        ArrayList<String> cand = rs.getTailCandidates(triple);
        // System.out.print(cand.size() + "\t");
        // String c = cand.size() > 0 ? cand.get(0) : "-";
        hitsAtK.evaluateTail(cand, t);
        // if (rank == -1) System.out.println("NOT FOUND: " + triple);
      }
      // System.out.println();
    }
  }

  private static void computeScores(ResultSet rs, TripleSet gold, HitsAtK hitsAtK) {
    for (Triple t : gold.getTriples()) {
      // System.out.print(t+ "\t");

      if (!target.equals("ASS")) {
        ArrayList<String> cand1 = rs.getHeadCandidates(t.toString());
        // System.out.print(cand.size() + "\t");
        String c1 = cand1.size() > 0 ? cand1.get(0) : "-";
        // if (ALL_HEAD_TAIL == 0 || ALL_HEAD_TAIL == 1)
        hitsAtK.evaluateHead(cand1, t);
      }

      ArrayList<String> cand2 = rs.getTailCandidates(t.toString());
      // System.out.print(cand.size() + "\t");
      // String c2 = cand2.size() > 0 ? cand2.get(0) : "-";
      // if (ALL_HEAD_TAIL == 0 || ALL_HEAD_TAIL == 2)
      hitsAtK.evaluateTail(cand2, t);
      // System.out.println();
    }
  }

  private static void printAndMarkUnfoundTriples(ResultSet rs, GoldStandard gold, HitsAtK hitsAtK) {
    for (String triple : gold.triples) {
      String[] tt = triple.split(" ");
      Triple t = new Triple(tt[0], tt[1], tt[2]);
      if (gold.getCategory(triple, true) != null) {
        ArrayList<String> cand = rs.getHeadCandidates(triple);
        String c = cand.size() > 0 ? cand.get(0) : "-";
        int foundAt = hitsAtK.evaluateHead(cand, t);
        if (foundAt < 0)
          System.out.println(t.getHead() + " headX" + t.getRelation() + " " + t.getTail());
      }
      if (gold.getCategory(triple, false) != null) {
        ArrayList<String> cand = rs.getTailCandidates(triple);
        String c = cand.size() > 0 ? cand.get(0) : "-";
        int foundAt = hitsAtK.evaluateTail(cand, t);
        if (foundAt < 0)
          System.out.println(t.getHead() + " tailX" + t.getRelation() + " " + t.getTail());
      }
    }
  }

  private static void compareResultSets(
      ResultSet rs1, ResultSet rs2, GoldStandard gold, HitsAtK hitsAtK) {
    for (String triple : gold.triples) {
      String[] tt = triple.split(" ");
      Triple t = new Triple(tt[0], tt[1], tt[2]);
      if (gold.getCategory(triple, true) != null) {
        ArrayList<String> cand1 = rs1.getHeadCandidates(triple);
        ArrayList<String> cand2 = rs2.getHeadCandidates(triple);
        boolean foundBy1 = false;
        for (String c1 : cand1) {
          if (t.getHead().equals(c1)) foundBy1 = true;
        }
        boolean foundBy2 = false;
        for (String c2 : cand2) {
          if (t.getHead().equals(c2)) foundBy2 = true;
        }
        if (foundBy1 != foundBy2) {
          System.out.println(
              "H "
                  + rs1.getName()
                  + "="
                  + foundBy1
                  + " "
                  + rs2.getName()
                  + "="
                  + foundBy2
                  + " "
                  + triple);
        }
      }
      if (gold.getCategory(triple, false) != null) {
        ArrayList<String> cand1 = rs1.getTailCandidates(triple);
        ArrayList<String> cand2 = rs2.getTailCandidates(triple);
        ;
        boolean foundBy1 = false;
        for (String c1 : cand1) {
          if (t.getTail().equals(c1)) foundBy1 = true;
        }
        boolean foundBy2 = false;
        for (String c2 : cand2) {
          if (t.getTail().equals(c2)) foundBy2 = true;
        }
        if (foundBy1 != foundBy2) {
          System.out.println(
              "T "
                  + rs1.getName()
                  + "="
                  + foundBy1
                  + " "
                  + rs2.getName()
                  + "="
                  + foundBy2
                  + " "
                  + triple);
        }
      }
    }
  }
}
