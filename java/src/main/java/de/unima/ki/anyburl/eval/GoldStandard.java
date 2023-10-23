package de.unima.ki.anyburl.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GoldStandard {

  private HashMap<String, String> headTriplesToCat;
  private HashMap<String, String> tailTriplesToCat;

  public ArrayList<String> triples;

  public GoldStandard() {
    this.headTriplesToCat = new HashMap<String, String>();
    this.tailTriplesToCat = new HashMap<String, String>();
    this.triples = new ArrayList<String>();
  }

  public GoldStandard(String filePath) {

    this.headTriplesToCat = new HashMap<String, String>();
    this.tailTriplesToCat = new HashMap<String, String>();
    this.triples = new ArrayList<String>();
    try {
      File file = new File(filePath);
      FileReader fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String tripleLine;
      String previousTriple = "";
      while ((tripleLine = bufferedReader.readLine()) != null) {
        if (tripleLine.length() < 3) continue;
        String[] token = tripleLine.split("\t");
        if (token[1].equals("head")) {
          this.headTriplesToCat.put(token[0], token[2]);
        }
        if (token[1].equals("tail")) {
          this.tailTriplesToCat.put(token[0], token[2]);
        }
        if (!token[0].equals(previousTriple)) {
          this.triples.add(token[0]);
        }
        previousTriple = token[0];
      }
      fileReader.close();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public String getCategory(String triple, boolean headNotTail) {
    if (headNotTail) {
      return this.headTriplesToCat.get(triple);
    } else {
      return this.tailTriplesToCat.get(triple);
    }
  }

  public GoldStandard getSubset(String category) {
    GoldStandard gs = new GoldStandard();
    for (String t : this.triples) {
      boolean addedTriple = false;
      if (this.headTriplesToCat.get(t).equals(category)) {
        addedTriple = true;
        gs.headTriplesToCat.put(t, category);
      } else {
        gs.headTriplesToCat.put(t, null);
      }
      if (this.tailTriplesToCat.get(t).equals(category)) {
        addedTriple = true;
        gs.tailTriplesToCat.put(t, category);
      } else {
        gs.tailTriplesToCat.put(t, null);
      }
      if (addedTriple) {
        gs.triples.add(t);
      }
    }
    return gs;
  }
}
