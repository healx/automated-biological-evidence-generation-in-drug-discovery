package de.unima.ki.anyburl.util;

import de.unima.ki.anyburl.structure.ScoreTree;
import java.util.HashSet;

public class ScoreTreeTest {

  public static void main(String[] args) {
    ScoreTree tree = new ScoreTree();

    HashSet<String> s1 = new HashSet<String>();
    s1.add("a1");
    s1.add("a2");
    s1.add("a3");
    s1.add("a4");
    s1.add("a5");
    s1.add("a6");
    s1.add("a7");
    s1.add("a8");
    s1.add("a9");
    s1.add("a10");
    s1.add("a11");
    s1.add("a12");
    tree.addValues(0.9, s1, null);

    HashSet<String> s2 = new HashSet<String>();
    s2.add("b1");
    tree.addValues(0.8, s2, null);

    // LinkedHashMap<String> results = new LinkedList<String>();
    // tree.getAsLinkedList(results);

  }
}
