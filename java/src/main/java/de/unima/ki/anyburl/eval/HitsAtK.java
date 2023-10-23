package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class HitsAtK {

  private ArrayList<TripleSet> filterSets = new ArrayList<TripleSet>();

  private static final int ATKMAX = 10;

  private int[] hitsADnTail = new int[ATKMAX];
  private int[] hitsADnTailFiltered = new int[ATKMAX];
  private int counterTail = 0;
  private int counterTailCovered = 0;

  private ArrayList<Integer> headRanks = new ArrayList<Integer>();
  private ArrayList<Integer> tailRanks = new ArrayList<Integer>();

  private int[] hitsADnHead = new int[ATKMAX];
  private int[] hitsADnHeadFiltered = new int[ATKMAX];
  private int counterHead = 0;
  private int counterHeadCovered = 0;

  private static NumberFormat nf = NumberFormat.getInstance(Locale.US);
  private static DecimalFormat df = (DecimalFormat) nf;

  // private StringBuffer storedResult = new StringBuffer("");

  public HitsAtK() throws IOException {
    df.applyPattern("0.0000");
  }

  public void reset() {
    // reset head
    this.hitsADnHead = new int[ATKMAX];
    this.hitsADnHeadFiltered = new int[ATKMAX];
    this.counterHead = 0;
    this.counterHeadCovered = 0;
    // reset tail
    this.hitsADnTail = new int[ATKMAX];
    this.hitsADnTailFiltered = new int[ATKMAX];
    this.counterTail = 0;
    this.counterTailCovered = 0;

    this.headRanks = new ArrayList<Integer>();
    this.tailRanks = new ArrayList<Integer>();
  }

  public String getApproxMRR() {
    double mrr = 0.0;
    double hk = 0.0;
    double hk_prev = 0.0;
    double hk_diff = 0.0;
    for (int k = 0; k < 10; k++) {
      hk =
          ((double) (hitsADnHeadFiltered[k] + hitsADnTailFiltered[k])
              / (double) (counterHead + counterTail));
      hk_diff = hk - hk_prev;
      mrr += hk_diff * (1.0 / (k + 1));
      // System.out.println("k=" + (k+1) + " hk=" + hk);
      hk_prev = hk;
    }
    double mrr_up = mrr + (1.0 - hk_prev) * (1.0 / 11.0);
    double mrr_low = mrr;
    return f(mrr_low) + ".." + f(mrr_up);
  }

  public String getHitsAtK(int k) {

    String hitsAtK =
        f(
            (double) (hitsADnHeadFiltered[k] + hitsADnTailFiltered[k])
                / (double) (counterHead + counterTail));
    return hitsAtK;
  }

  public double getHitsAtKDouble(int k) {
    return ((double) (hitsADnHeadFiltered[k] + hitsADnTailFiltered[k])
        / (double) (counterHead + counterTail));
  }

  // call this method when no head candidate has been found
  public void evaluateHead() {
    counterHead++;
  }

  // call this method when no tail candidate has been found
  public void evaluateTail() {
    counterTail++;
  }

  public int evaluateHead(ArrayList<String> candidates, Triple triple) {
    int foundAt = -1;
    counterHead++;
    if (candidates.size() > 0) counterHeadCovered++;

    int filterCount = 0;
    for (int rank = 0; rank < candidates.size() && rank < ATKMAX; rank++) {
      String candidate = candidates.get(rank);

      if (candidate.equals(triple.getHead())) {
        for (int index = rank; index < ATKMAX; index++) {
          hitsADnHead[index]++;
          hitsADnHeadFiltered[index - filterCount]++;
        }
        foundAt = rank + 1;
        break;
      } else {
        for (TripleSet filterSet : filterSets) {
          if (filterSet.isTrue(candidate, triple.getRelation(), triple.getTail())) {
            filterCount++;
            break;
          }
        }
      }
    }

    int counter = 0;
    boolean ranked = false;
    for (String candidate : candidates) {
      counter++;
      if (candidate.equals(triple.getHead())) {
        this.headRanks.add(counter);
        ranked = true;
        break;
      }
    }
    if (!ranked) this.headRanks.add(-1);

    return foundAt;
  }

  public int evaluateTail(ArrayList<String> candidates, Triple triple) {
    int foundAt = -1;
    counterTail++;
    if (candidates.size() > 0) counterTailCovered++;

    int filterCount = 0;
    for (int rank = 0; rank < candidates.size() && rank < ATKMAX; rank++) {
      String candidate = candidates.get(rank);

      if (candidate.equals(triple.getTail())) {
        for (int index = rank; index < ATKMAX; index++) {
          hitsADnTail[index]++;
          hitsADnTailFiltered[index - filterCount]++;
        }
        foundAt = rank + 1;
        break;
      } else {
        for (TripleSet filterSet : filterSets) {
          if (filterSet.isTrue(triple.getHead(), triple.getRelation(), candidate)) {
            filterCount++;
            break;
          }
        }
      }
    }

    int counter = 0;
    boolean ranked = false;
    for (String candidate : candidates) {
      counter++;
      if (candidate.equals(triple.getTail())) {
        this.tailRanks.add(counter);
        ranked = true;
        break;
      }
    }
    if (!ranked) this.tailRanks.add(-1);

    return foundAt;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("evaluation result\n");
    sb.append("hits@k\traw\t\t\tfilter\n");
    sb.append("hits@k\ttail\thead\ttotal\ttail\thead\ttotal\n");
    for (int i = 0; i < ATKMAX; i++) {
      sb.append(i + 1);
      sb.append("\t");
      sb.append(f((double) hitsADnTail[i] / (double) counterTail));
      sb.append("\t");
      sb.append(f((double) hitsADnHead[i] / (double) counterHead));
      sb.append("\t");
      sb.append(
          f((double) (hitsADnHead[i] + hitsADnTail[i]) / (double) (counterHead + counterTail)));
      sb.append("\t");
      sb.append(f((double) hitsADnTailFiltered[i] / (double) counterTail));
      sb.append("\t");
      sb.append(f((double) hitsADnHeadFiltered[i] / (double) counterHead));
      sb.append("\t");
      sb.append(
          f(
              (double) (hitsADnHeadFiltered[i] + hitsADnTailFiltered[i])
                  / (double) (counterHead + counterTail)));
      sb.append("\n");
    }
    sb.append(
        "counterHead="
            + counterHead
            + " counterTail="
            + counterTail
            + " hits@10Tail="
            + hitsADnTail[ATKMAX - 1]
            + " hits@10Head="
            + hitsADnHead[ATKMAX - 1]
            + "\n");
    sb.append(
        "counterHead="
            + counterHead
            + " counterTail="
            + counterTail
            + " hits@10TailFiltered="
            + hitsADnTailFiltered[ATKMAX - 1]
            + " hits@10HeadFiltered="
            + hitsADnHeadFiltered[ATKMAX - 1]
            + "\n");
    sb.append(
        "fraction of head covered by rules  = "
            + ((double) counterHeadCovered / (double) counterHead)
            + "\n");
    sb.append(
        "fraction of tails covered by rules = "
            + ((double) counterTailCovered / (double) counterTail)
            + "\n");
    return sb.toString();
  }

  public static String f(double v) {
    return df.format(v);
  }

  /*
  public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue2222(Map<K, V> map) {
  	return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
  			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }
  */

  public void addFilterTripleSet(TripleSet tripleSet) {
    this.filterSets.add(tripleSet);
  }

  public String getMRR(int numOfInstances) {
    double headMrr = this.getMRR(numOfInstances, this.headRanks);
    double tailMrr = this.getMRR(numOfInstances, this.tailRanks);
    return f((headMrr + tailMrr) / 2.0);
  }

  private double getMRR(int numOfInstances, ArrayList<Integer> numbers) {
    double mrr = 0.0;
    for (int i = 0; i < numbers.size(); i++) {
      if (numbers.get(i) > 0) {
        mrr += 1.0 / (double) numbers.get(i);
      } else {
        mrr += 2.0 / (double) numOfInstances;
      }
    }
    mrr = mrr / (double) numbers.size();
    return mrr;
  }
}
