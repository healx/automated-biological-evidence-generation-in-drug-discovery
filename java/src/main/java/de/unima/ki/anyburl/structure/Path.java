package de.unima.ki.anyburl.structure;

import de.unima.ki.anyburl.data.Triple;
import java.util.HashSet;

public class Path {

  protected String[] nodes;
  protected char[] markers;

  public Path(String[] nodes, char[] markers) {
    this.nodes = nodes;
    this.markers = markers;
  }

  public String toString() {
    StringBuffer p = new StringBuffer("");
    for (int i = 0; i < this.nodes.length - 1; i++) {
      p.append(this.markedNodeToString(i));
      p.append(" -> ");
    }
    p.append(this.markedNodeToString(this.nodes.length - 1));
    return p.toString();
  }

  private String markedNodeToString(int i) {
    if (i % 2 == 1) {
      return this.markers[(i - 1) / 2] + this.nodes[i];
    } else {
      return this.nodes[i];
    }
  }

  // TODO write code
  public boolean equals(Object that) {
    return false;
  }

  // TODO write code
  public int hashCode() {
    return 7;
  }

  /**
   * Checks if a path is valid for strict object identity.
   *
   * @return False, if the x and y values appear at the wrong position in the path, or if the same
   *     entities appears several times in the body part of the path.
   */
  public boolean isValid() {
    String xconst = this.nodes[0];
    String yconst = this.nodes[2];
    HashSet<String> visitedEntities = new HashSet<>();
    for (int i = 4; i < nodes.length - 2; i += 2) {
      if (nodes[i].equals(xconst)) {
        return false;
      }
      if (nodes[i].equals(yconst)) {
        return false;
      }
    }
    for (int i = 2; i < nodes.length; i += 2) {
      if (visitedEntities.contains(nodes[i])) return false;
      visitedEntities.add(nodes[i]);
    }
    return true;
  }

  /** Checks if a path is non cyclic, i.e, does not connect the entities of the given triple. */
  public boolean isNonCyclic(Triple t) {
    // System.out.println("path:   " + this);
    // System.out.println("triple: " + t);
    for (int i = 4; i < nodes.length; i += 2) {
      if (t.getHead().equals(nodes[i])) return false;
      if (t.getTail().equals(nodes[i])) return false;
    }
    return true;
  }

  /**
   * Checks if the path will result in a cyclic rule.
   *
   * @return True, if its a cyclic path.
   */
  public boolean isCyclic() {
    if (this.nodes[this.nodes.length - 1].equals(this.nodes[0])
        || this.nodes[this.nodes.length - 1].equals(this.nodes[2])) return true;
    return false;
  }
}
