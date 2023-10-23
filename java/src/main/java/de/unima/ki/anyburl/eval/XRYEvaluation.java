package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.AnnotatedTriple;
import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class XRYEvaluation {

  public static void main(String[] args) throws IOException {

    /*
    TripleSet resultSet = new TripleSet("experiments/WN18/predictions/rl-p123-500.txt");
    TripleSet testSet = new TripleSet("data/WN18/test.txt");
    */

    TripleSet resultSet = new TripleSet("experiments/WN18/predictions/rl/p123-500.txt");

    // TripleSet resultSet = new TripleSet("experiments/WN18RR/predictions/rl/p12345-200.txt");

    // TripleSet resultSet = new TripleSet("experiments/FB15k/predictions/rl/p12-500.txt");

    // System.exit(1);

    TripleSet testSet = new TripleSet("data/WN18/test.txt");

    int K = 100;

    double weightedHitsATK = 0.0;
    double above = 0.0;
    double below = 0.0;
    double unweightedHitsATK = 0.0;
    Set<String> relations = resultSet.getRelations();

    int denominator = 0;

    for (String relation : relations) {
      int d = Math.min(K, testSet.getTriplesByRelation(relation).size());
      denominator += d;
    }

    int i = 0;
    double weightedMapATK = 0.0;
    double unweightedMapATK = 0.0;

    int tripleCounter = 0;
    double confidenceTotal = 0.0;

    for (String relation : relations) {
      System.out.print(relation);

      TripleSet tTriples = new TripleSet();
      for (Triple t : testSet.getTriplesByRelation(relation)) tTriples.addTriple(t);

      if (tTriples.getTriples().size() == 0) {
        System.out.println(" ...");
        continue;
      }

      TripleSet rTriples = new TripleSet();
      for (Triple t : resultSet.getTriplesByRelation(relation)) rTriples.addTriple(t);

      for (Triple t : rTriples.getTriples()) {
        AnnotatedTriple at = (AnnotatedTriple) t;
        confidenceTotal += at.getConfidence();
        tripleCounter++;
      }

      // System.out.println(" xxxx=" + resultSet.getTriplesByRelation(relation).size());
      // System.out.println(" XXXX=" + rTriples.getTriples().size());

      TripleSet iTriples = tTriples.getIntersectionWith(rTriples);

      double hitsATK =
          (double) iTriples.getTriples().size()
              / (double) Math.min(K, tTriples.getTriples().size());

      double apATK = 0.0;
      // if (relation.equals("/food/food/nutrients./food/nutrition_fact/nutrient")) {
      apATK = getAveragePrecision(tTriples, rTriples);
      // }
      // else {
      //	continue;
      // }
      unweightedMapATK += apATK;
      weightedMapATK += apATK * ((double) Math.min(K, tTriples.getTriples().size()) / denominator);

      System.out.println(
          " hitsAtk="
              + hitsATK
              + " apAtk="
              + apATK
              + " test="
              + tTriples.getTriples().size()
              + " result="
              + rTriples.getTriples().size()
              + " intersection="
              + iTriples.getTriples().size());
      // System.out.println(":" + hitsATK);

      above += (double) iTriples.getTriples().size();
      below += (double) Math.min(K, tTriples.getTriples().size());
      unweightedHitsATK += hitsATK;
      i++;
    }

    unweightedHitsATK /= (double) i;
    weightedHitsATK = above / below;

    unweightedMapATK /= (double) i;

    System.out.println("=============================================");
    System.out.println("Weighted Mean Hits@K:   " + weightedHitsATK);
    System.out.println("Unweighted Mean Hits@K: " + unweightedHitsATK);
    System.out.println("Weighted MAP@K:         " + weightedMapATK);
    System.out.println("Unweighted MAP@K:       " + unweightedMapATK);
    System.out.println("Confidence Average:     " + (confidenceTotal / (double) tripleCounter));
  }

  private static double getAveragePrecision(TripleSet tTriples, TripleSet rTriples) {

    ArrayList<AnnotatedTriple> rTriplesAnnotated = new ArrayList<AnnotatedTriple>();
    for (Triple t : rTriples.getTriples()) {
      rTriplesAnnotated.add((AnnotatedTriple) t);
    }
    Collections.sort(rTriplesAnnotated);
    Collections.reverse(rTriplesAnnotated);

    int created = 1;
    int correct = 1;
    double p;
    double ap = 0.0;
    for (AnnotatedTriple t : rTriplesAnnotated) {

      // System.out.println("p=" + p);
      if (tTriples.isTrue(t)) {
        p = (double) correct / (double) created;
        ap += p;
        // System.out.println("ap*=" + ap);
        correct++;
        // System.out.println("x ");
      } else {
        // System.out.println("0 ");
      }
      created++;
    }
    // System.out.println("ap=" + (ap / correct));

    return (ap / (double) correct);
  }
}
