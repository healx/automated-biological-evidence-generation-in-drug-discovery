package de.unima.ki.anyburl.structure;

import de.unima.ki.anyburl.Settings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

public class ScoreTree {

  public static int LOWER_BOUND = 10;
  public static int UPPER_BOUND = 10;

  public static double EPSILON = 0.00001;

  ArrayList<ScoreTree> children;
  private double score;
  private Rule explanation;
  private HashSet<String> storedValues;
  private int numOfValues;
  private int index;
  private boolean root;
  private boolean closed;

  // private numOf

  public ScoreTree() {
    this.children = new ArrayList<ScoreTree>();
    this.storedValues = null;
    this.closed = false;
    this.numOfValues = 0;
    this.index = 0;
    this.root = true;
    this.explanation = null;
  }

  public ScoreTree(double score, Set<String> values, Rule explanation) {
    this.score = score;
    this.explanation = explanation;
    this.children = new ArrayList<ScoreTree>();
    this.storedValues = new HashSet<String>();
    this.storedValues.addAll(values);
    if (this.storedValues.size() <= 1) this.closed = true;
    else this.closed = false;
    this.numOfValues = values.size();
    this.root = false;
  }

  public void addValues(double score, Set<String> values, Rule explanation) {
    // for(String v : values) {
    //	System.out.println(score + ": " + v);
    // }
    this.addValues(score, values, explanation, 0);
  }

  private void addValues(double score, Set<String> values, Rule explanation, int counter) {
    // go deep first
    for (ScoreTree child : this.children) {
      // int nosv = this.storedValues != null ? this.storedValues.size() : 0;
      child.addValues(score, values, explanation, 0);
    }
    // compare with stored values
    HashSet<String> touched = new HashSet<String>();
    HashSet<String> untouched = new HashSet<String>();
    if (!root) {
      for (String storedValue : this.storedValues) {
        if (values.contains(storedValue)) touched.add(storedValue);
        else untouched.add(storedValue);
      }
      values.removeAll(touched);
    }
    // standard split
    if (touched.size() > 0
        && this.storedValues.size() > 1
        && touched.size() < this.storedValues.size()) {
      int childIndex = this.index - untouched.size();
      if (childIndex >= ScoreTree.LOWER_BOUND) {
        this.storedValues = touched;
        this.index = childIndex;
        this.numOfValues -= untouched.size();
      } else {
        this.storedValues = untouched;
        this.addChild(score, touched, explanation, childIndex);
      }
    }
    // special case of adding new value, which happens only if the maximal number of values is not
    // yet exceeded
    if (this.root && values.size() > 0 && this.numOfValues < LOWER_BOUND) {
      this.addChild(score, values, explanation, this.numOfValues + values.size());
      this.numOfValues += values.size();
    }

    // try to set on closed if only 1 or less values are stored in this and in its children
    if (this.storedValues == null || this.storedValues.size() <= 1) {
      boolean c = true;
      for (ScoreTree child : this.children) {
        if (!child.closed) {
          c = false;
          break;
        }
      }
      this.closed = c;
    }
  }

  private ScoreTree addChild(double score, Set<String> values, Rule explanation, int childIndex) {
    ScoreTree child = new ScoreTree(score, values, explanation);
    child.index = childIndex;
    this.children.add(child);
    return child;
  }

  public String toString() {
    String rep = "";
    for (ScoreTree child : children) {
      rep = rep + child.toString("");
    }
    return rep;
  }

  private String toString(String indent) {
    String rep = "";
    String closingSign = this.closed ? "X" : "O";
    rep +=
        indent
            + closingSign
            + " "
            + this.score
            + " ["
            + this.index
            + "]("
            + this.numOfValues
            + ") -> { ";
    if (storedValues != null) {
      for (String v : this.storedValues) {
        rep += v + " ";
      }
    }
    rep += "} with explanation: " + this.explanation + "\n";
    for (ScoreTree child : children) {
      rep = rep + child.toString(indent + "   ");
    }
    return rep;
  }

  public void print(String ss, HashSet<String> set) {
    System.out.print(ss + ": ");
    for (String s : set) {
      System.out.print(s + ",");
    }
    System.out.println();
  }

  public boolean fine() {
    if (this.root && this.children.size() > 0) {
      int i = this.children.get(children.size() - 1).index;
      // System.out.println(i);
      if (i >= LOWER_BOUND && i <= UPPER_BOUND) {
        return this.isFirstUnique();
      }
    }
    return false;
  }

  private boolean isFirstUnique() {
    ScoreTree tree = this;
    while (tree.children.size() > 0) {
      tree = tree.children.get(0);
    }
    return tree.closed;
  }

  public void getAsLinkedList(
      LinkedHashMap<String, Double> list, double ps, int level, String myself) {
    if (this.children.size() > 0) {
      for (ScoreTree child : children) {
        if (this.root) {
          child.getAsLinkedList(list, ps, level + 1, myself);
        } else {
          double psUpdated = ps + Math.pow(EPSILON, level - 1) * this.score;
          child.getAsLinkedList(list, psUpdated, level + 1, myself);
        }
      }
    }
    if (!this.root) {
      double psUpdated = ps + Math.pow(EPSILON, level - 1) * this.score;
      // print("" + psUpdated, this.storedValues);
      for (String v : this.storedValues) {
        String value = v;
        if (v.equals(Settings.REWRITE_REFLEXIV_TOKEN)) value = myself;
        list.put(value, psUpdated);
      }
    }
  }

  public void getAsLinkedList(LinkedHashMap<String, Double> list, String myself) {
    this.getAsLinkedList(list, 0, 0, myself);
  }

  public static void main(String[] args) {
    ScoreTree tree = new ScoreTree();
    HashSet<String> s1 = new HashSet<String>();
    s1.add("a");
    s1.add("b");
    s1.add("c");
    s1.add("d");
    s1.add("d1");
    s1.add("d2");
    tree.addValues(0.9, s1, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());
    System.out.println("first unique   " + tree.isFirstUnique());

    HashSet<String> s11 = new HashSet<String>();
    s11.add("aaa");
    s11.add("bbb");
    tree.addValues(0.8999, s11, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());
    System.out.println("first unique   " + tree.isFirstUnique());

    HashSet<String> s12 = new HashSet<String>();
    s12.add("a");
    s12.add("b");
    tree.addValues(0.891, s12, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());
    System.out.println("first unique   " + tree.isFirstUnique());

    HashSet<String> s2 = new HashSet<String>();
    s2.add("a");
    s2.add("b");
    s2.add("e1");
    s2.add("e2");
    s2.add("e3");
    s2.add("e4");
    tree.addValues(0.88, s2, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());
    System.out.println("first unique   " + tree.isFirstUnique());

    HashSet<String> s3 = new HashSet<String>();
    s3.add("a");
    s3.add("e1");
    s3.add("e4");
    // s3.add("e5");
    tree.addValues(0.6, s3, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());
    System.out.println("first unique   " + tree.isFirstUnique());

    HashSet<String> s4 = new HashSet<String>();
    s4.add("xa");
    s4.add("xb");
    s4.add("xe1");
    s4.add("xe2");
    s4.add("xe3");
    s4.add("xe4");
    tree.addValues(0.5, s4, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());

    HashSet<String> s5 = new HashSet<String>();
    s5.add("xa");
    s5.add("xb");
    s5.add("ye1");
    s5.add("ye2");
    s5.add("ye3");
    s5.add("ye4");
    tree.addValues(0.41, s5, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());

    HashSet<String> s6 = new HashSet<String>();
    s6.add("xe2");
    s6.add("xa");
    s6.add("xxx");
    // s6.add("xb");
    tree.addValues(0.39, s6, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());

    HashSet<String> s7 = new HashSet<String>();
    s7.add("xe1");
    s7.add("xe2");
    tree.addValues(0.22, s7, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());

    HashSet<String> s8 = new HashSet<String>();
    // s8.add("xe1");
    s8.add("xe4");
    tree.addValues(0.21, s8, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());

    HashSet<String> s9 = new HashSet<String>();
    // s8.add("xe1");
    s9.add("e1");
    s9.add("e2");
    tree.addValues(0.21, s9, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());

    HashSet<String> s19 = new HashSet<String>();
    // s8.add("xe1");
    s19.add("u1");
    s19.add("u2");
    tree.addValues(0.19, s19, null);

    System.out.println("-------");
    System.out.println(tree);
    System.out.println("precise enough " + tree.fine());

    LinkedHashMap<String, Double> list = new LinkedHashMap<String, Double>();
    tree.getAsLinkedList(list, 0, 0, "blubber");

    for (Entry<String, Double> e : list.entrySet()) {
      System.out.println(e.getValue() + ": " + e.getKey());
    }
  }

  public HashMap<String, HashSet<Rule>> getExplainedCandidates() {
    HashMap<String, HashSet<Rule>> explainedCandidates = new HashMap<String, HashSet<Rule>>();
    LinkedList<Rule> explanations = new LinkedList<Rule>();
    this.getExplainedCandidates(explainedCandidates, explanations, 0);
    return explainedCandidates;
  }

  public void getExplainedCandidates(
      HashMap<String, HashSet<Rule>> explainedCandidates,
      LinkedList<Rule> explanations,
      int level) {
    if (this.children.size() > 0) {
      for (ScoreTree child : children) {
        if (this.root) {
          child.getExplainedCandidates(explainedCandidates, explanations, level + 1);
        } else {
          // double psUpdated = ps + Math.pow(EPSILON, level-1) * this.score;
          explanations.add(this.explanation);
          child.getExplainedCandidates(explainedCandidates, explanations, level + 1);
          explanations.removeLast();
        }
      }
    }
    if (!this.root) {
      HashSet<Rule> collectedExplanations = new HashSet<Rule>();
      collectedExplanations.addAll(explanations);
      for (String v : this.storedValues) {
        explainedCandidates.put(v, collectedExplanations);
      }
    }
  }
}
