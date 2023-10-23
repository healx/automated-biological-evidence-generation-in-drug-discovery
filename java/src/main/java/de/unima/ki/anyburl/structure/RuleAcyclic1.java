package de.unima.ki.anyburl.structure;

import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.data.TripleSet;

public class RuleAcyclic1 extends RuleAcyclic {

  public RuleAcyclic1(RuleUntyped r) {
    super(r);
  }

  protected String getUnboundVariable() {
    return null;
  }

  // public double getAppliedConfidence() {
  // 	return (double)this.getCorrectlyPredictedHeads() / ((double)this.getPredictedHeads() +
  // Settings.UNSEEN_NEGATIVE_EXAMPLES + Settings.UNSEEN_NEGATIVE_EXAMPLES_ATYPED[2]);
  // }

  /*
  public double getAppliedConfidence() {
  	return (double)this.getCorrectlyPredicted() / ((double)this.getPredicted() + Math.pow(Settings.UNSEEN_NEGATIVE_EXAMPLES, this.bodysize()));
  }
  */

  public boolean isRedundantACRule(TripleSet triples) {
    Atom last = this.body.getLast();
    if (last.isRightC()) {
      if (triples.getHeadEntities(last.getRelation(), last.getRight()).size()
          < Settings.AC_MIN_NUM_OF_LAST_ATOM_GROUNDINGS) {
        return true;
      }
    } else {
      if (triples.getTailEntities(last.getRelation(), last.getLeft()).size()
          < Settings.AC_MIN_NUM_OF_LAST_ATOM_GROUNDINGS) {
        return true;
      }
    }
    return false;
  }

  public boolean isSingleton(TripleSet triples) {
    // return false;

    if (this.body.get(0).getRight().equals("X") && this.body.get(0).getRight().equals("Y")) {
      String head = this.body.get(0).getLeft();
      String relation = this.body.get(0).getRelation();
      if (triples.getTailEntities(relation, head).size() > 1) return false;
      else return true;
    } else {
      String tail = this.body.get(0).getRight();
      String relation = this.body.get(0).getRelation();
      if (triples.getHeadEntities(relation, tail).size() > 1) return false;
      else return true;
    }
  }

  public boolean isMoreSpecific(Rule general) {
    if (general instanceof RuleCyclic && this.isCyclic()) {
      RuleCyclic rg = (RuleCyclic) general;
      if (!this.head.getXYGeneralization().equals(rg.head)) return false;
      if (this.bodysize() == rg.bodysize()) {
        if (this.body.get(0).contains("X")) {
          for (int i = 0; i < this.bodysize() - 1; i++) {
            if (!this.body.get(i).equals(rg.body.get(i))) return false;
          }
          if (this.body.get(this.bodysize() - 1).moreSpecial(rg.body.get(this.bodysize() - 1)))
            return true;
          return false;
        } else {
          String vSpecific = "Y";
          String vGeneral = "Y";
          for (int i = 0; i < this.bodysize() - 1; i++) {
            Atom sAtom = this.body.get(i);
            Atom gAtom = rg.body.get(this.bodysize() - 1 - i);
            if (!sAtom.equals(gAtom, vSpecific, vGeneral)) return false;
            vSpecific = sAtom.getOtherTerm(vSpecific);
            vGeneral = gAtom.getOtherTerm(vGeneral);
          }
          if (this.body.get(this.bodysize() - 1).moreSpecial(rg.body.get(0), vSpecific, vGeneral))
            return true;
        }
      }
    }
    if (general instanceof RuleAcyclic2) {
      // System.out.println("this:     " + this);
      // System.out.println("general:  " + general);

      RuleAcyclic2 rg = (RuleAcyclic2) general;
      if (!this.head.equals(rg.head)) return false;
      if (this.bodysize() > rg.bodysize()) return false;
      for (int i = 0; i < this.bodysize() - 1; i++) {
        if (!this.body.get(i).equals(rg.body.get(i))) {
          return false;
        }
      }
      if (this.body.get(this.bodysize() - 1).moreSpecial(rg.body.get(this.bodysize() - 1)))
        return true;
    }
    return false;
  }

  public boolean isCyclic() {
    if (this.getHead().getConstant().equals(this.body.getLast().getConstant())) return true;
    return false;
  }

  public String toXYString() {
    if (this.head.getLeft().equals("X")) {
      String c = this.head.getRight();
      StringBuffer sb = new StringBuffer();
      sb.append(this.getHead().toString(c, "Y"));
      for (int i = 0; i < this.bodysize(); i++) {
        sb.append(this.getBodyAtom(i).toString(c, "Y"));
      }
      String rs = sb.toString();
      return rs;
    }
    if (this.head.getRight().equals("Y")) {
      String c = this.head.getLeft();
      StringBuffer sb = new StringBuffer();
      sb.append(this.getHead().toString(c, "X"));
      for (int i = this.bodysize() - 1; i >= 0; i--) {
        sb.append(this.getBodyAtom(i).toString(c, "X"));
      }
      String rs = sb.toString();
      return rs;
    }
    System.err.println("toXYString of the following rule not implemented: " + this);
    System.exit(1);
    return null;
  }

  /* XXX
  public String getSignature() {
  	StringBuffer sb = new StringBuffer();
  	for (Atom atom : this.body.literals) {
  		sb.append(atom.getRelation());
  		atom.getXYGeneralization()
  	}
  	return null;
  }
  XXX */

}
