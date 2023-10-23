package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.io.RuleReader;
import de.unima.ki.anyburl.structure.Rule;
import java.io.IOException;
import java.util.LinkedList;

public class RandomPrediction {

  public static void main(String[] args) throws IOException {

    String filepathRules = "exp/kiril/yago-rules-refined-ci0999-x-ci0999-x-50";
    // String filepathRules = "exp/kiril/yago-rules-50";
    String filepathTriples = "data/YAGO03-10/train.txt";

    RuleReader rr = new RuleReader();

    TripleSet ts = new TripleSet(filepathTriples);

    LinkedList<Rule> rules = rr.read(filepathRules);

    // make random prediction for the first 10 rules
    for (int i = 0; i < 10; i++) {
      Rule rule = rules.get(i);
      Triple predicted = rule.getRandomValidPrediction(ts);
      System.out.println("Rule: " + rule);
      System.out.println("Predicted: " + predicted);
    }
  }
}
