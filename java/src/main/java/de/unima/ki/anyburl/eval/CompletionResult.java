package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.TripleSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class CompletionResult {

  private ArrayList<String> headResults;
  private ArrayList<String> tailResults;

  private ArrayList<Double> headConfidences;
  private ArrayList<Double> tailConfidences;

  private String triple;

  public CompletionResult(String triple) {
    this.triple = triple;
    this.headResults = new ArrayList<String>();
    this.tailResults = new ArrayList<String>();

    this.headConfidences = new ArrayList<Double>();
    this.tailConfidences = new ArrayList<Double>();
  }

  public void addHeadResults(String[] heads, int k) {
    if (k > 0) addResults(heads, this.headResults, k);
    else addResults(heads, this.headResults);
  }

  public void addTailResults(String[] tails, int k) {
    if (k > 0) addResults(tails, this.tailResults, k);
    else addResults(tails, this.tailResults);
  }

  private void addResults(String[] candidates, ArrayList<String> results, int k) {
    for (String c : candidates) {
      if (!c.equals("")) {
        results.add(c);
        k--;
        if (k == 0) return;
      }
    }
  }

  private void addConfidences(Double[] confs, ArrayList<Double> confidences) {
    for (Double d : confs) {
      confidences.add(d);
    }
  }

  private void addConfidences(Double[] confs, ArrayList<Double> confidences, int k) {
    for (Double d : confs) {
      confidences.add(d);
      k--;
      if (k == 0) return;
    }
  }

  private void addResults(String[] candidates, ArrayList<String> results) {
    for (String c : candidates) {
      if (!c.equals("")) {
        results.add(c);
      }
    }
  }

  public ArrayList<String> getHeads() {
    return this.headResults;
  }

  public ArrayList<String> getTails() {
    return this.tailResults;
  }

  public ArrayList<Double> getHeadConfidences() {
    return this.headConfidences;
  }

  public ArrayList<Double> getTailConfidences() {
    return this.tailConfidences;
  }

  public void addHeadConfidences(Double[] confidences, int k) {
    if (k > 0) this.addConfidences(confidences, this.headConfidences, k);
    else this.addConfidences(confidences, this.headConfidences);
  }

  public void addTailConfidences(Double[] confidences, int k) {
    if (k > 0) this.addConfidences(confidences, this.tailConfidences, k);
    else this.addConfidences(confidences, this.tailConfidences);
  }

  public void extendWith(CompletionResult thatResult, int k, double factor) {

    PriorityQueue<Candidate> qHeads = new PriorityQueue<Candidate>();
    HashMap<String, Double> headsC = new HashMap<String, Double>();
    for (int i = 0; i < this.headConfidences.size(); i++) {
      Candidate hc = new Candidate(this.getHeads().get(i), this.headConfidences.get(i));
      qHeads.add(hc);
      headsC.put(this.getHeads().get(i), this.headConfidences.get(i));
    }

    for (int i = 0; i < thatResult.headConfidences.size(); i++) {
      Candidate hc =
          new Candidate(thatResult.getHeads().get(i), thatResult.headConfidences.get(i) * factor);
      if (headsC.containsKey(thatResult.getHeads().get(i))) {
        if (hc.confidence > headsC.get(thatResult.getHeads().get(i))) {
          System.out.println(hc.confidence);
          qHeads.remove(hc); // looks crazy, is not crazy
          qHeads.add(hc);
        }
      } else qHeads.add(hc);
    }

    this.headResults.clear();
    this.headConfidences.clear();
    int j = 0;
    while (qHeads.size() > 0) {
      Candidate c = qHeads.poll();
      this.headResults.add(c.value);
      this.headConfidences.add(c.confidence);
      j++;
      if (j == k) break;
    }

    PriorityQueue<Candidate> qTails = new PriorityQueue<Candidate>();
    HashMap<String, Double> tailsC = new HashMap<String, Double>();
    for (int i = 0; i < this.tailConfidences.size(); i++) {
      Candidate hc = new Candidate(this.getTails().get(i), this.tailConfidences.get(i));
      qTails.add(hc);
      tailsC.put(this.getTails().get(i), this.tailConfidences.get(i));
    }

    for (int i = 0; i < thatResult.tailConfidences.size(); i++) {
      Candidate hc =
          new Candidate(thatResult.getTails().get(i), thatResult.tailConfidences.get(i) * factor);
      if (tailsC.containsKey(thatResult.getTails().get(i))) {
        if (hc.confidence > tailsC.get(thatResult.getTails().get(i))) {
          qTails.remove(hc); // looks crazy, is not crazy
          qTails.add(hc);
        }
      } else qTails.add(hc);
    }

    this.tailResults.clear();
    this.tailConfidences.clear();
    j = 0;
    while (qTails.size() > 0) {
      Candidate c = qTails.poll();
      this.tailResults.add(c.value);
      this.tailConfidences.add(c.confidence);
      j++;
      if (j == k) break;
    }
  }

  public void supressConnected(TripleSet triples) {

    String[] token = triple.split(" ");
    String head = token[0];
    String relation = token[1];
    String tail = token[2];

    for (int i = 0; i < this.tailResults.size(); i++) {
      if (triples.getRelations(head, this.tailResults.get(i)).size() > 0) {
        System.out.println("remove head candidate " + this.tailResults.get(i));
        this.tailResults.remove(i);
        this.tailConfidences.remove(i);
      }
    }

    for (int i = 0; i < this.headResults.size(); i++) {
      if (triples.getRelations(this.headResults.get(i), tail).size() > 0) {
        System.out.println("remove tail candidate " + this.headResults.get(i));
        this.headResults.remove(i);
        this.headConfidences.remove(i);
      }
    }
  }
}
