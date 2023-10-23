package de.unima.ki.anyburl.io;

import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.structure.Atom;
import de.unima.ki.anyburl.structure.Rule;
import de.unima.ki.anyburl.structure.RuleAcyclic1;
import de.unima.ki.anyburl.structure.RuleAcyclic2;
import de.unima.ki.anyburl.structure.RuleCyclic;
import de.unima.ki.anyburl.structure.RuleUntyped;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class RuleReader {

  public static final int TYPE_UNDEFINED = 0;

  public static final int TYPE_CYCLIC = 1;
  public static final int TYPE_ACYCLIC = 2;
  public static final int TYPE_REFINED = 3;

  public static void main(String[] args) throws IOException {
    RuleReader rr = new RuleReader();
    LinkedList<Rule> r1 = rr.read("exp/neg/yago-rules-10-neg-10");

    int i = 0;
    for (Rule r : r1) {
      System.out.println(r);
      System.out.println(r.hasNegation());
      i++;
      if (i > 20) break;
    }
  }

  /**
   * @param filepath The file to read the rules from.
   * @returnA list of rules.
   * @throws IOException
   */
  public LinkedList<Rule> read(String filepath) throws IOException {
    System.out.print("* reading rules from " + filepath + "");
    // int i = 0;
    LinkedList<Rule> rules = new LinkedList<Rule>();
    // HashMap<Long, Rule> ids2Rules = new HashMap<Long,Rule>();
    File file = new File(filepath);
    BufferedReader br =
        new BufferedReader(
            (new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
    long counter = 0;
    try {
      String line = br.readLine();
      while (line != null) {
        if (line == null || line.equals("")) break;
        Rule r = this.getRule(line);
        if (r != null
            && r.getConfidence() >= Settings.RR_THRESHOLD_CONFIDENCE
            && r.getCorrectlyPredicted() >= Settings.RR_THRESHOLD_CORRECT_PREDICTIONS) {
          rules.add(r);
          counter++;
          if (counter % 1000000 == 0) System.out.print(" ~");
        }
        line = br.readLine();
      }
    } finally {
      br.close();
    }
    System.out.println(", read " + rules.size() + " rules");
    return rules;
  }

  /**
   * @param filepath The file to read the rules from.
   * @returnA list of rules.
   * @throws IOException
   */
  public LinkedList<Rule> readRefinable(String filepath) throws IOException {
    // int i = 0;
    LinkedList<Rule> rules = new LinkedList<Rule>();
    // HashMap<Long, Rule> ids2Rules = new HashMap<Long,Rule>();
    File file = new File(filepath);
    BufferedReader br =
        new BufferedReader(
            (new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)));
    try {
      String line = br.readLine();
      while (line != null) {
        if (line == null || line.equals("")) break;
        // System.out.println(line);
        Rule r = this.getRule(line);
        if (r != null) {
          if (r.isRefinable()) rules.add(r);
        }
        line = br.readLine();
      }
    } finally {
      br.close();
    }
    return rules;
  }

  public Rule getRule(String line) {
    if (line.startsWith("#")) return null;
    String token[] = line.split("\t");
    // rule with constant in head
    RuleUntyped r = null;
    if (token.length == 4) {
      r =
          new RuleUntyped(
              Integer.parseInt(token[0]), Integer.parseInt(token[1]), Double.parseDouble(token[2]));
    }
    if (token.length == 7) {
      System.err.println(
          "you are trying to read am old rule set which is based on head/tail distiction not yet"
              + " supported anymore");
      System.exit(1);
    }
    r = (RuleUntyped) r;
    String atomsS[] = token[token.length - 1].split(" ");
    r.setHead(new Atom(atomsS[0]));
    for (int i = 2; i < atomsS.length; i++) {
      Atom lit = new Atom(atomsS[i]);
      r.addBodyAtom(lit);
    }
    if (r.isCyclic()) {
      if (Settings.READ_CYCLIC_RULES == 1) return new RuleCyclic(r);
    } else {
      if (r.isAcyclic1()) {
        if (Settings.READ_ACYCLIC1_RULES == 1) return new RuleAcyclic1(r);
      } else {
        if (Settings.READ_ACYCLIC2_RULES == 1) return new RuleAcyclic2(r);
      }
    }
    return null;
  }
}
