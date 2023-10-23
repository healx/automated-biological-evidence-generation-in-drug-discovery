package de.unima.ki.anyburl.threads;

import de.unima.ki.anyburl.RuleEngine;
import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.structure.Rule;
import java.util.ArrayList;
import java.util.HashMap;

public class Predictor extends Thread {

  private TripleSet testSet;
  private TripleSet trainingSet;
  private TripleSet filterSet;
  private int k;
  private HashMap<String, ArrayList<Rule>> relation2Rules4Prediction;

  public Predictor(
      TripleSet testSet,
      TripleSet trainingSet,
      TripleSet filterSet,
      int k,
      HashMap<String, ArrayList<Rule>> relation2Rules4Prediction) {
    this.testSet = testSet;
    this.trainingSet = trainingSet;
    this.filterSet = filterSet;
    this.k = k;
    this.relation2Rules4Prediction = relation2Rules4Prediction;
  }

  public void run() {
    Triple triple = RuleEngine.getNextPredictionTask();
    // Rule rule = null;
    while (triple != null) {
      // System.out.println(this.getName() + " making prediction for " + triple);
      if (Settings.AGGREGATION_ID == 1) {
        RuleEngine.predictMax(
            testSet, trainingSet, filterSet, k, relation2Rules4Prediction, triple);
      } else if (Settings.AGGREGATION_ID == 2) {
        RuleEngine.predictNoisyOr(
            testSet, trainingSet, filterSet, k, relation2Rules4Prediction, triple, true);
        RuleEngine.predictNoisyOr(
            testSet, trainingSet, filterSet, k, relation2Rules4Prediction, triple, false);
      } else if (Settings.AGGREGATION_ID == 3) {
        RuleEngine.predictHarmonicSum(
            testSet, trainingSet, filterSet, k, relation2Rules4Prediction, triple, true);
        RuleEngine.predictHarmonicSum(
            testSet, trainingSet, filterSet, k, relation2Rules4Prediction, triple, false);
      }
      // System.out.println(this.getName() + " going for next prediction");
      triple = RuleEngine.getNextPredictionTask();
      // System.out.println(this.getName() + " going for next prediction");
      // rule = RuleEngine.getNextRuleMaterializationTask();
      // if (rule != null) RuleEngine.materializeRule(rule, trainingSet);
      // Thread.yield();
    }
  }
}
