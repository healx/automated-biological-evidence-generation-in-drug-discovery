package de.unima.ki.anyburl.structure;

import java.util.HashMap;
import java.util.HashSet;

public class RuleIndex {

  HashMap<String, HashMap<Integer, HashSet<RuleCyclic>>> normal =
      new HashMap<String, HashMap<Integer, HashSet<RuleCyclic>>>();
  HashMap<String, HashMap<Integer, HashSet<RuleCyclic>>> inverted =
      new HashMap<String, HashMap<Integer, HashSet<RuleCyclic>>>();

  public void add(String relation, int pos, boolean direction, RuleCyclic r) {
    if (direction) {
      if (!normal.containsKey(relation))
        normal.put(relation, new HashMap<Integer, HashSet<RuleCyclic>>());
      if (!normal.get(relation).containsKey(pos))
        normal.get(relation).put(pos, new HashSet<RuleCyclic>());
      normal.get(relation).get(pos).add(r);
    } else {
      if (!inverted.containsKey(relation))
        inverted.put(relation, new HashMap<Integer, HashSet<RuleCyclic>>());
      if (!inverted.get(relation).containsKey(pos))
        inverted.get(relation).put(pos, new HashSet<RuleCyclic>());
      inverted.get(relation).get(pos).add(r);
    }
  }

  public HashSet<RuleCyclic> get(String relation, int pos, boolean direction) {
    if (direction) return normal.get(relation).get(pos);
    else return inverted.get(relation).get(pos);
  }

  public boolean containsKey(String relation, int pos, boolean direction) {
    if (direction) {
      if (normal.containsKey(relation)) return normal.get(relation).containsKey(pos);
      else return false;
    } else {
      if (inverted.containsKey(relation)) return inverted.get(relation).containsKey(pos);
      else return false;
    }
  }
}
