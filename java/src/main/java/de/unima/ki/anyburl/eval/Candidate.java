package de.unima.ki.anyburl.eval;

public class Candidate implements Comparable<Candidate> {

  public String value;
  public double confidence;

  public Candidate(String value, double confidence) {
    this.value = value;
    this.confidence = confidence;
  }

  @Override
  public int compareTo(Candidate that) {
    if (this.confidence > that.confidence) return -1;
    if (this.confidence < that.confidence) return 1;
    return 0;
  }

  public int hashCode() {
    return this.value.hashCode();
  }

  public boolean equals(Object that) {
    if (that instanceof Candidate) {
      Candidate thatCand = (Candidate) that;
      return this.value.equals(thatCand.value);
    }
    return false;
  }
}
