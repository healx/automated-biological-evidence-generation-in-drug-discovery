package de.unima.ki.anyburl.data;

import de.unima.ki.anyburl.Settings;

/** A triple represents a labeled edge a knowledge graph. */
public class Triple {

  private String head; // subject
  private String tail; // object
  private String relation;

  private int h = 0;
  private boolean h_set = false;

  public Triple(String head, String relation, String tail) {
    if (head.length() < 2 || tail.length() < 2) {
      System.err.println("the triple set you are trying to load contains constants of length 1");
      System.err.println("a constant (entity) needs to be described by at least two letters");
      System.exit(1);
    }
    this.head = head;
    this.relation = relation;
    if (Settings.REWRITE_REFLEXIV && head.equals(tail)) {
      this.tail = Settings.REWRITE_REFLEXIV_TOKEN;
    } else {
      this.tail = tail;
    }
  }

  public String getHead() {
    return this.head;
  }

  public String getTail() {
    return this.tail;
  }

  public String getValue(boolean headNotTail) {
    if (headNotTail) return this.head;
    else return this.tail;
  }

  public String getRelation() {
    return relation;
  }

  public String toString() {
    return this.head + " " + this.relation + " " + this.tail;
  }

  public boolean equals(Object that) {
    if (that instanceof Triple || that instanceof AnnotatedTriple) {
      Triple thatTriple = (Triple) that;
      if (this.head.equals(thatTriple.head)
          && this.tail.equals(thatTriple.tail)
          && this.relation.equals(thatTriple.relation)) {
        return true;
      }
    }
    return false;
  }

  public int hashCode() {
    if (!h_set) {
      h = this.head.hashCode() + this.tail.hashCode() + this.relation.hashCode();
    }
    return h;
  }

  public boolean equals(boolean headNotTail, String subject, String rel, String object) {
    if (headNotTail) {
      return (this.head.equals(subject) && this.tail.equals(object) && this.relation.equals(rel));
    } else {
      return (this.head.equals(object) && this.tail.equals(subject) && this.relation.equals(rel));
    }
  }

  public double getConfidence() {
    return 1.0;
  }
}
