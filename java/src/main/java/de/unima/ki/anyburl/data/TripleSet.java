package de.unima.ki.anyburl.data;

import de.unima.ki.anyburl.Settings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TripleSet {

  private ArrayList<Triple> triples;
  private HashMap<AnnotatedTriple, Double> atriples = new HashMap<AnnotatedTriple, Double>();
  private Random rand;

  HashMap<String, ArrayList<Triple>> headToList;
  HashMap<String, ArrayList<Triple>> tailToList;
  HashMap<String, ArrayList<Triple>> relationToList;

  HashMap<String, HashMap<String, HashSet<String>>> headRelation2Tail;
  HashMap<String, HashMap<String, HashSet<String>>> headTail2Relation;
  HashMap<String, HashMap<String, HashSet<String>>> tailRelation2Head;

  HashMap<String, HashMap<String, ArrayList<String>>> headRelation2TailList;
  // HashMap<String, HashMap<String, ArrayList<String>>> headTail2RelationList;
  HashMap<String, HashMap<String, ArrayList<String>>> tailRelation2HeadList;

  HashSet<String> frequentRelations = new HashSet<String>();

  HashMap<String, ArrayList<String>> relation2HeadSample = new HashMap<String, ArrayList<String>>();
  HashMap<String, ArrayList<String>> relation2TailSample = new HashMap<String, ArrayList<String>>();

  public static void main(String[] args) {

    // TripleSet ts = new TripleSet("data/DB500/ftest.txt");

  }

  public TripleSet(String filepath) {
    this();
    this.readTriples(filepath);
    this.indexTriples();
    this.setupListStructure();
  }

  public TripleSet() {

    this.rand = new Random();

    this.triples = new ArrayList<Triple>();
    this.headToList = new HashMap<String, ArrayList<Triple>>();
    this.tailToList = new HashMap<String, ArrayList<Triple>>();
    this.relationToList = new HashMap<String, ArrayList<Triple>>();

    this.headRelation2Tail = new HashMap<String, HashMap<String, HashSet<String>>>();
    this.headTail2Relation = new HashMap<String, HashMap<String, HashSet<String>>>();
    this.tailRelation2Head = new HashMap<String, HashMap<String, HashSet<String>>>();

    this.headRelation2TailList = new HashMap<String, HashMap<String, ArrayList<String>>>();
    this.tailRelation2HeadList = new HashMap<String, HashMap<String, ArrayList<String>>>();
  }

  public void addTripleSet(TripleSet ts) {
    for (Triple t : ts.triples) {
      this.addTriple(t);
    }
    for (AnnotatedTriple at : ts.atriples.keySet()) {
      this.addTriple(at);
    }
  }

  public void addTriple(AnnotatedTriple t) {
    if (this.isTrue(t) && !this.atriples.containsKey(t)) {
      return;
    } else {
      if (this.atriples.containsKey(t)) {
        double storedConfidence = this.atriples.get(t);
        double newConfidence = t.getConfidence();
        if (newConfidence > storedConfidence) {
          this.atriples.put(t, t.getConfidence());
        }
      } else {
        // System.out.println(" adding new annotated triple " + t);
        this.atriples.put(t, t.getConfidence());
        this.addTripleToIndex(t);
      }
    }
  }

  // fix stuff here
  public void addTriple(Triple t) {
    if (this.isTrue(t) && !this.atriples.containsKey(t)) {
      return;
    } else {
      this.triples.add(t);
      if (this.atriples.containsKey(t)) {
        this.atriples.remove(t);
      } else {
        this.addTripleToIndex(t);
      }
    }
  }

  private void indexTriples() {
    long tCounter = 0;
    long divisor = 10000;
    for (Triple t : triples) {
      tCounter++;
      if (tCounter % divisor == 0) {
        System.out.println("* indexed " + tCounter + " triples");
        divisor *= 2;
      }
      addTripleToIndex(t);
    }
    System.out.println(
        "* set up index for "
            + this.relationToList.keySet().size()
            + " relations, "
            + this.headToList.keySet().size()
            + " head entities, and "
            + this.tailToList.keySet().size()
            + " tail entities");
  }

  private void setupListStructure() {
    System.out.print("* set up list structure ... ");
    // head -> relation -> tails
    for (String head : this.headRelation2Tail.keySet()) {
      this.headRelation2TailList.put(head, new HashMap<String, ArrayList<String>>());
      for (String relation : this.headRelation2Tail.get(head).keySet()) {
        this.headRelation2TailList.get(head).put(relation, new ArrayList<String>());
        this.headRelation2TailList
            .get(head)
            .get(relation)
            .addAll(this.headRelation2Tail.get(head).get(relation));
      }
    }
    // tail -> relation -> head
    for (String tail : this.tailRelation2Head.keySet()) {
      this.tailRelation2HeadList.put(tail, new HashMap<String, ArrayList<String>>());
      for (String relation : this.tailRelation2Head.get(tail).keySet()) {
        this.tailRelation2HeadList.get(tail).put(relation, new ArrayList<String>());
        this.tailRelation2HeadList
            .get(tail)
            .get(relation)
            .addAll(this.tailRelation2Head.get(tail).get(relation));
      }
    }
    System.out.println(" done");
  }

  private void addTripleToIndex(Triple t) {
    String head = t.getHead();
    String tail = t.getTail();
    String relation = t.getRelation();
    // index head
    if (!this.headToList.containsKey(head)) {
      this.headToList.put(head, new ArrayList<Triple>());
    }
    this.headToList.get(head).add(t);
    // index tail
    if (!this.tailToList.containsKey(tail)) {
      this.tailToList.put(tail, new ArrayList<Triple>());
    }
    this.tailToList.get(tail).add(t);
    // index relation
    if (!this.relationToList.containsKey(relation)) {
      this.relationToList.put(relation, new ArrayList<Triple>());
    }
    this.relationToList.get(relation).add(t);
    // index head-relation => tail
    if (!this.headRelation2Tail.containsKey(head)) {
      this.headRelation2Tail.put(head, new HashMap<String, HashSet<String>>());
    }
    if (!this.headRelation2Tail.get(head).containsKey(relation)) {
      this.headRelation2Tail.get(head).put(relation, new HashSet<String>());
    }
    this.headRelation2Tail.get(head).get(relation).add(tail);
    // index tail-relation => head
    if (!this.tailRelation2Head.containsKey(tail)) {
      this.tailRelation2Head.put(tail, new HashMap<String, HashSet<String>>());
    }
    if (!this.tailRelation2Head.get(tail).containsKey(relation)) {
      this.tailRelation2Head.get(tail).put(relation, new HashSet<String>());
    }
    this.tailRelation2Head.get(tail).get(relation).add(head);
    // index headTail => relation
    if (!this.headTail2Relation.containsKey(head)) {
      this.headTail2Relation.put(head, new HashMap<String, HashSet<String>>());
    }
    if (!this.headTail2Relation.get(head).containsKey(tail)) {
      this.headTail2Relation.get(head).put(tail, new HashSet<String>());
    }
    this.headTail2Relation.get(head).get(tail).add(relation);
  }

  private void readTriples(String filepath) {
    Path file = (new File(filepath)).toPath();
    // Charset charset = Charset.forName("US-ASCII");
    Charset charset = Charset.forName("UTF8");
    String line = null;
    long lineCounter = 0;

    if(!file.toFile().exists()) {
        return;
    }

    try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
      while ((line = reader.readLine()) != null) {
        // System.out.println(line);
        lineCounter++;
        if (lineCounter % 1000000 == 0) {
          System.out.println(">>> parsed " + lineCounter + " lines");
        }
        if (line.length() <= 2) continue;
        String[] token = line.split("\t");
        if (token.length < 3) token = line.split(" ");
        Triple t = null;
        if (token.length == 3) t = new Triple(token[0], token[1], token[2]);

        if (token.length == 4) {
          if (token[3].equals(".")) {
            t = new Triple(token[0], token[1], token[2]);
          } else {

            try {
              t = new AnnotatedTriple(token[0], token[1], token[2]);
              ((AnnotatedTriple) t).setConfidence(Double.parseDouble(token[3]));
            } catch (NumberFormatException nfe) {
              System.err.println("could not parse line " + line);
              t = null;
            }
          }
        }
        if (t == null) {
        } else {

          this.triples.add(t);
          if (Settings.REWRITE_REFLEXIV && t.getTail().equals(Settings.REWRITE_REFLEXIV_TOKEN)) {
            Triple trev;
            if (!(t instanceof AnnotatedTriple))
              trev = new Triple(t.getTail(), t.getRelation(), t.getHead());
            else {
              trev = new AnnotatedTriple(t.getTail(), t.getRelation(), t.getHead());
              ((AnnotatedTriple) trev).setConfidence(t.getConfidence());
            }
            this.triples.add(trev);
          }
        }
      }
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
      System.err.format("Error occurred for line: " + line + " LINE END");
      System.exit(1);
    }
    // Collections.shuffle(this.triples);
    System.out.println("* read " + this.triples.size() + " triples");
  }

  public ArrayList<Triple> getTriples() {
    return this.triples;
  }

  public HashMap<AnnotatedTriple, Double> getAnnotatedTriples() {
    return this.atriples;
  }

  public ArrayList<Triple> getTriplesByHead(String head) {
    if (this.headToList.containsKey(head)) {
      return this.headToList.get(head);
    } else {
      return new ArrayList<Triple>();
    }
  }

  public ArrayList<Triple> getNTriplesByHead(String head, int n) {
    if (this.headToList.containsKey(head)) {
      if (this.headToList.get(head).size() <= n) return this.headToList.get(head);
      else {
        ArrayList<Triple> chosen = new ArrayList<Triple>();
        for (int i = 0; i < n; i++) {
          int index = this.rand.nextInt(this.headToList.get(head).size());
          chosen.add(this.headToList.get(head).get(index));
        }
        return chosen;
      }
    } else return new ArrayList<Triple>();
  }

  public ArrayList<Triple> getTriplesByTail(String tail) {
    if (this.tailToList.containsKey(tail)) {
      return this.tailToList.get(tail);
    } else {
      return new ArrayList<Triple>();
    }
  }

  public ArrayList<Triple> getNTriplesByTail(String tail, int n) {

    if (this.tailToList.containsKey(tail)) {
      if (this.tailToList.get(tail).size() <= n) return this.tailToList.get(tail);
      else {
        ArrayList<Triple> chosen = new ArrayList<Triple>();
        for (int i = 0; i < n; i++) {
          int index = this.rand.nextInt(this.tailToList.get(tail).size());
          chosen.add(this.tailToList.get(tail).get(index));
        }
        return chosen;
      }
    } else return new ArrayList<Triple>();
  }

  public ArrayList<Triple> getTriplesByRelation(String relation) {
    if (this.relationToList.containsKey(relation)) {
      return this.relationToList.get(relation);
    } else {
      return new ArrayList<Triple>();
    }
  }

  public Triple getRandomTripleByRelation(String relation) {
    if (this.relationToList.containsKey(relation)) {
      return this.relationToList
          .get(relation)
          .get(this.rand.nextInt(this.relationToList.get(relation).size()));
    }
    return null;
  }

  public ArrayList<String> getNRandomEntitiesByRelation(
      String relation, boolean headNotTail, int n) {
    if (headNotTail) {
      if (relation2HeadSample.containsKey(relation)) return relation2HeadSample.get(relation);
    } else {
      if (relation2TailSample.containsKey(relation)) return relation2TailSample.get(relation);
    }
    return computeNRandomEntitiesByRelation(relation, headNotTail, n);
  }

  private synchronized ArrayList<String> computeNRandomEntitiesByRelation(
      String relation, boolean headNotTail, int n) {

    if (this.relationToList.containsKey(relation)) {
      ArrayList<String> entities = new ArrayList<String>();
      HashSet<String> entitiesAsSet = new HashSet<String>();
      for (Triple triple : this.relationToList.get(relation)) {
        String value = triple.getValue(headNotTail);
        if (!entitiesAsSet.contains(value)) {
          entitiesAsSet.add(value);
          entities.add(value);
        }
      }
      ArrayList<String> sampledEntities = new ArrayList<String>();
      for (int i = 0; i < n; i++) {

        String entity = entities.get(rand.nextInt(entities.size()));
        // System.out.println("i=" + i + "  e=" + e);
        sampledEntities.add(entity);
      }
      if (headNotTail) this.relation2HeadSample.put(relation, sampledEntities);
      else this.relation2TailSample.put(relation, sampledEntities);
      return sampledEntities;
    } else {
      System.err.println(
          "something is strange, internal reference to relation "
              + relation
              + ", which is not indexed");
      System.err.println("check if rule set and triple set fit together");
      return null;
    }
  }

  public Set<String> getRelations() {
    return this.relationToList.keySet();
  }

  public Set<String> getHeadEntities(String relation, String tail) {
    if (tailRelation2Head.get(tail) != null) {
      if (tailRelation2Head.get(tail).get(relation) != null) {
        return tailRelation2Head.get(tail).get(relation);
      }
    }
    return new HashSet<String>();
  }

  public Set<String> getTailEntities(String relation, String head) {
    if (headRelation2Tail.get(head) != null) {
      if (headRelation2Tail.get(head).get(relation) != null) {
        return headRelation2Tail.get(head).get(relation);
      }
    }
    return new HashSet<String>();
  }

  /**
   * Returns those values for which the relation holds for a given value. If the headNotTail is set
   * to true, the value is interpreted as head value and the corresponding tails are returned.
   * Otherwise, the corresponding heads are returned.
   *
   * @param relation The specified relation.
   * @param value The value interpreted as given head or tail.
   * @param headNotTail Whether to interpret the value as head and not as tail (false interprets as
   *     tail).
   * @return The resulting values.
   */
  public Set<String> getEntities(String relation, String value, boolean headNotTail) {
    if (headNotTail) return this.getTailEntities(relation, value);
    else return this.getHeadEntities(relation, value);
  }

  /**
   * Returns a random value for which the relation holds for a given value. If the headNotTail is
   * set to true, the value is interpreted as head value and the corresponding tails are returned.
   * Otherwise, the corresponding heads are returned.
   *
   * @param relation The specified relation.
   * @param value The value interpreted as given head or tail.
   * @param headNotTail Whether to interpret the value as head and not as tail (false interprets as
   *     tail).
   * @return The resulting value or null if no such value exists.
   */
  public String getRandomEntity(String relation, String value, boolean headNotTail) {
    if (headNotTail) return this.getRandomTailEntity(relation, value);
    else return this.getRandomHeadEntity(relation, value);
  }

  private String getRandomHeadEntity(String relation, String tail) {
    if (!tailRelation2HeadList.containsKey(tail)) return null;
    ArrayList<String> list = this.tailRelation2HeadList.get(tail).get(relation);
    if (list == null) return null;
    return list.get(this.rand.nextInt(list.size()));
  }

  private String getRandomTailEntity(String relation, String head) {
    if (!headRelation2TailList.containsKey(head)) return null;
    ArrayList<String> list = this.headRelation2TailList.get(head).get(relation);
    if (list == null) return null;
    return list.get(this.rand.nextInt(list.size()));
  }

  public Set<String> getRelations(String head, String tail) {
    if (headTail2Relation.get(head) != null) {
      if (headTail2Relation.get(head).get(tail) != null) {
        return headTail2Relation.get(head).get(tail);
      }
    }
    return new HashSet<String>();
  }

  public boolean isTrue(String head, String relation, String tail) {
    if (tailRelation2Head.get(tail) != null) {
      if (tailRelation2Head.get(tail).get(relation) != null) {
        return tailRelation2Head.get(tail).get(relation).contains(head);
      }
    }
    return false;
  }

  public boolean isTrue(Triple triple) {
    return this.isTrue(triple.getHead(), triple.getRelation(), triple.getTail());
  }

  public double getConfidence(Triple t) {
    if (this.isTrue(t)) return 1.0;
    if (this.atriples.containsKey(t)) return this.atriples.get(t);
    return 0.0;
  }

  public void compareTo(TripleSet that, String thisId, String thatId) {
    System.out.println("* Comparing two triple sets");
    int counter = 0;
    for (Triple t : triples) {
      if (that.isTrue(t)) {
        counter++;
      }
    }

    System.out.println("* size of " + thisId + ": " + this.triples.size());
    System.out.println("* size of " + thatId + ": " + that.triples.size());
    System.out.println("* size of intersection: " + counter);
  }

  public TripleSet getIntersectionWith(TripleSet that) {
    TripleSet ts = new TripleSet();
    for (Triple t : triples) {
      if (that.isTrue(t)) {
        ts.addTriple(t);
      }
    }
    return ts;
  }

  public TripleSet minus(TripleSet that) {
    TripleSet ts = new TripleSet();
    for (Triple t : triples) {
      if (!that.isTrue(t)) {
        ts.addTriple(t);
      }
    }
    return ts;
  }

  public int getNumOfEntities() {
    return headToList.keySet().size() + tailToList.keySet().size();
  }

  public void determineFrequentRelations(double coverage) {
    HashMap<String, Integer> relationCounter = new HashMap<String, Integer>();
    int allCounter = 0;
    for (Triple t : this.triples) {
      allCounter++;
      String r = t.getRelation();
      if (relationCounter.containsKey(r)) {
        int count = relationCounter.get(r);
        relationCounter.put(r, count + 1);
      } else {
        relationCounter.put(r, 1);
      }
    }

    ArrayList<Integer> counts = new ArrayList<Integer>();
    counts.addAll(relationCounter.values());
    Collections.sort(counts);
    int countUp = 0;
    int border = 0;
    for (Integer c : counts) {
      countUp = countUp + c;
      // System.out.println("countUp: " + countUp);
      // System.out.println("c: " + c);
      if (((double) (allCounter - countUp) / (double) allCounter) < coverage) {
        border = c;
        break;
      }
    }

    // System.out.println("Number of all relations: " + relationCounter.size());
    // System.out.println("Relations covering " + coverage + " of all triples");
    for (String r : relationCounter.keySet()) {

      if (relationCounter.get(r) > border) {
        frequentRelations.add(r);
        // System.out.println(r + " (used in " + relationCounter.get(r) + " triples)");
      }
    }
    // System.out.println("Number of frequent (covering " + coverage+ " of all) relations: " +
    // frequentRelations.size());
  }

  public boolean isFrequentRelation(String relation) {
    return this.frequentRelations.contains(relation);
  }

  public boolean existsPath(String x, String y, int pathLength) {
    if (pathLength == 1) {
      if (this.getRelations(x, y).size() > 0) {
        return true;
      }
      if (this.getRelations(y, x).size() > 0) {
        return true;
      }
      return false;
    }
    if (pathLength == 2) {
      Set<String> hop1x = new HashSet<String>();
      for (Triple hx : this.getTriplesByHead(x)) {
        hop1x.add(hx.getTail());
      }
      for (Triple tx : this.getTriplesByTail(x)) {
        hop1x.add(tx.getHead());
      }

      for (Triple hy : this.getTriplesByHead(y)) {
        if (hop1x.contains(hy.getTail())) return true;
      }
      for (Triple ty : this.getTriplesByTail(y)) {
        if (hop1x.contains(ty.getHead())) return true;
      }
      return false;
    }
    if (pathLength > 2) {
      System.err.println("checking the existence of a path longer than 2 is so far not supported");
      System.exit(1);
    }
    return false;
  }

  public Set<String> getEntities() {
    HashSet<String> entities = new HashSet<String>();
    entities.addAll(headToList.keySet());
    entities.addAll(tailToList.keySet());
    return entities;
  }

  public void write(String filepath) throws FileNotFoundException {
    PrintWriter pw = new PrintWriter(filepath);

    for (Triple t : this.triples) {
      pw.println(t);
    }
    for (Triple t : this.atriples.keySet()) {
      pw.println(t);
    }
    pw.flush();
    pw.close();
  }

  public int size() {
    return this.triples.size() + this.atriples.size();
  }
}
