package de.unima.ki.anyburl.data;

public class AnnotatedTriple extends Triple implements Comparable<AnnotatedTriple> {

  private double confidence;

  public AnnotatedTriple(String head, String relation, String tail) {
    super(head, relation, tail);
  }

  public void setConfidence(double confidence) {
    this.confidence = confidence;
  }

  public double getConfidence() {
    return this.confidence;
  }

  public int compareTo(AnnotatedTriple that) {
    if (this.confidence > that.confidence) {
      return 1;
    } else {
      if (this.confidence == that.confidence) return 0;
      return -1;
    }
  }

  public String toString() {
    return super.toString() + " " + this.confidence;
  }

  public int hashCode() {
    return super.hashCode();
  }

  public boolean equals(Object that) {
    return super.equals(that);
  }
}
