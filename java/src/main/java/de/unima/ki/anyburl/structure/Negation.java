package de.unima.ki.anyburl.structure;

public class Negation {

  private Atom atom;

  public Negation(Atom atom) {
    this.atom = atom;
  }

  public String toString() {
    return "!" + this.atom;
  }

  public boolean equals(Object o) {
    if (o instanceof Negation) {
      Negation n = (Negation) o;
      return this.atom.equals(n.atom);
    }
    return false;
  }

  public int hashCode() {
    return -1 * this.atom.hashCode();
  }

  public Atom getAtom() {
    return this.atom;
  }

  public String getRight() {
    return this.atom.getRight();
  }

  public String getLeft() {
    return this.atom.getLeft();
  }

  public String getRelation() {
    return this.atom.getRelation();
  }

  public boolean isVariableRight() {
    return this.atom.getRight().length() <= 1;
  }

  public boolean isVariableLeft() {
    return this.atom.getLeft().length() <= 1;
  }
}
