package de.unima.ki.anyburl.structure;

import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.data.TripleSet;
import java.util.HashMap;

public class RuleAcyclic2 extends RuleAcyclic {

  private String unboundVariable = null;

  public RuleAcyclic2(RuleUntyped r) {
    super(r);
  }

  protected String getUnboundVariable() {
    if (this.unboundVariable != null) return this.unboundVariable;
    // if (this.body.get(this.body.size()-1).isLeftC() ||
    // this.body.get(this.body.size()-1).isRightC()) return null;
    HashMap<String, Integer> counter = new HashMap<String, Integer>();
    for (Atom atom : this.body) {
      if (!atom.getLeft().equals("X") && !atom.getLeft().equals("Y")) {
        if (counter.containsKey(atom.getLeft())) counter.put(atom.getLeft(), 2);
        else counter.put(atom.getLeft(), 1);
      }
      if (!atom.getRight().equals("X") && !atom.getRight().equals("Y")) {
        if (counter.containsKey(atom.getRight())) counter.put(atom.getRight(), 2);
        else counter.put(atom.getRight(), 1);
      }
    }
    for (String variable : counter.keySet()) {
      if (counter.get(variable) == 1) {
        this.unboundVariable = variable;
        return variable;
      }
    }
    // this can never happen
    return this.unboundVariable;
  }

  /*
  public double getAppliedConfidence() {
  	return (double)this.getCorrectlyPredictedHeads() / ((double)this.getPredictedHeads() + Settings.UNSEEN_NEGATIVE_EXAMPLES + Settings.UNSEEN_NEGATIVE_EXAMPLES_ATYPED[3]);
  }
  */

  public boolean isSingleton(TripleSet triples) {
    return false;
  }

  public boolean isMoreSpecific(Rule general) {
    if (general instanceof RuleAcyclic2) {
      RuleAcyclic2 rg = (RuleAcyclic2) general;
      if (this.getHead().equals(rg.getHead())) {
        if (this.bodysize() <= rg.bodysize()) {
          for (int i = 0; i < this.bodysize(); i++) {
            Atom a = this.getBodyAtom(i);
            if (!rg.body.contains(a)) return false;
          }
          return true;
        }
      }
      return false;
    }
    return false;
  }

  public boolean isRedundantACRule(TripleSet triples) {
    Atom last = this.body.getLast();
    if (last.isRightC()) {
      if (triples.getTriplesByRelation(last.getRelation()).size()
          < Settings.AC_MIN_NUM_OF_LAST_ATOM_GROUNDINGS) {
        return true;
      }
    } else {
      if (triples.getTriplesByRelation(last.getRelation()).size()
          < Settings.AC_MIN_NUM_OF_LAST_ATOM_GROUNDINGS) {
        return true;
      }
    }
    return false;
  }
}
