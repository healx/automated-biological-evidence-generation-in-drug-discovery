package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.TripleSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class ResultSet {

  private HashMap<String, CompletionResult> results;
  private String name;

  public static boolean applyThreshold = false;
  public static double threshold = 0.0;

  public static void main(String[] args) throws FileNotFoundException {

    ResultSet rs = new ResultSet("exp/january/reinforced/db500-predictions-100", true, 10);
  }

  private boolean containsConfidences = false;

  public ResultSet(String name, String filePath) {
    this(name, filePath, false, 0);
  }

  public ResultSet(String name, boolean containsConfidences, int k) {
    this(name, name, containsConfidences, k);
  }

  public ResultSet(String name, String filePath, boolean containsConfidences, int k) {
    System.out.println("* loading result set at " + filePath);
    this.containsConfidences = containsConfidences;
    this.name = name;
    this.results = new HashMap<String, CompletionResult>();
    long counter = 0;
    long stepsize = 100000;
    File file = null;
    try {
      file = new File(filePath);
      // FileReader fileReader = new FileReader(file);

      // FileInputStream i = null;
      BufferedReader bufferedReader =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
      String tripleLine;
      while ((tripleLine = bufferedReader.readLine()) != null) {
        counter++;
        if (counter % stepsize == 0)
          System.out.println("* parsed " + counter + " lines of results file");
        if (tripleLine.length() < 3) continue;
        CompletionResult cr = new CompletionResult(tripleLine);
        String headLine = bufferedReader.readLine();
        String tailLine = bufferedReader.readLine();
        String tempLine = "";
        if (headLine.startsWith("Tails:")) {
          System.out.println("reversed");
          tempLine = headLine;
          headLine = tailLine;
          tailLine = tempLine;
        }
        if (!applyThreshold) {
          cr.addHeadResults(getResultsFromLine(headLine.substring(7)), k);
          cr.addHeadConfidences(getConfidencesFromLine(headLine.substring(7)), k);
          cr.addTailResults(getResultsFromLine(tailLine.substring(7)), k);
          cr.addTailConfidences(getConfidencesFromLine(tailLine.substring(7)), k);
        } else {
          cr.addHeadResults(getThresholdedResultsFromLine(headLine.substring(7)), k);
          cr.addHeadConfidences(getThresholdedConfidencesFromLine(headLine.substring(7)), k);
          cr.addTailResults(getThresholdedResultsFromLine(tailLine.substring(7)), k);
          cr.addTailConfidences(getThresholdedConfidencesFromLine(tailLine.substring(7)), k);
        }
        this.results.put(tripleLine.split("\t")[0], cr);
      }
      bufferedReader.close();
    } catch (IOException e) {
      System.err.println("problem related to file " + file + ".");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void extendWith(ResultSet rs, int k, double factor) {
    for (String t : this.results.keySet()) {
      CompletionResult thisResult = this.results.get(t);
      CompletionResult thatResult = rs.results.get(t);
      thisResult.extendWith(thatResult, k, factor);
    }
  }

  public void printAsTripleSet(PrintWriter pw) {
    for (String line : this.results.keySet()) {
      // System.out.println(line);
      String[] token = line.split("\\s+");
      CompletionResult cr = this.results.get(line);
      int i = 0;
      for (String h : cr.getHeads()) {
        if (i % 2 == 0) {
          // System.out.println("head: " + h);
          pw.println(h + " " + token[1] + " " + token[2]);
        }
        i++;
      }
      i = 0;
      for (String t : cr.getTails()) {
        if (i % 2 == 0) {
          // System.out.println("tail: " + t);
          pw.println(token[0] + " " + token[1] + " " + t);
        }
        i++;
      }
    }
  }

  private String[] getThresholdedResultsFromLine(String rline) {
    if (!containsConfidences) {
      return rline.split("\t");
    } else {
      String t = "";
      String cS = "";
      String[] token = rline.split("\t");
      // String[] tokenx = new String[token.length / 2];
      ArrayList<String> tokenx = new ArrayList<String>();
      for (int i = 0; i < token.length / 2; i++) {

        t = token[i * 2];
        cS = token[i * 2 + 1];
        double c = Double.parseDouble(cS);
        if (c > threshold) {
          tokenx.add(t);
        } else {
          break;
        }
      }
      String[] tokenxx = (String[]) tokenx.toArray(new String[0]);
      return tokenxx;
    }
  }

  private Double[] getThresholdedConfidencesFromLine(String rline) {
    if (!containsConfidences) {
      System.err.println("there are no confidences, you cannot retrieve them");
      return null;
    } else {
      String t = "";
      String cS = "";
      String[] token = rline.split("\t");
      // String[] tokenx = new String[token.length / 2];
      ArrayList<Double> tokenx = new ArrayList<Double>();
      for (int i = 0; i < token.length / 2; i++) {

        // t = token[i*2];
        cS = token[i * 2 + 1];
        double c = Double.parseDouble(cS);
        if (c > threshold) {
          tokenx.add(c);
        } else {
          break;
        }
      }
      Double[] tokenxx = (Double[]) tokenx.toArray(new Double[0]);
      return tokenxx;
    }
  }

  private String[] getResultsFromLine(String rline) {
    if (!containsConfidences) {
      return rline.split("\t");
    } else {
      String[] token = rline.split("\t");
      String[] tokenx = new String[token.length / 2];
      for (int i = 0; i < tokenx.length; i++) {
        tokenx[i] = token[i * 2];
      }
      return tokenx;
    }
  }

  private Double[] getConfidencesFromLine(String rline) {
    if (!containsConfidences) {
      System.err.println("there are no confidences, you cannot retrieve them");
      return null;
    } else {
      String[] token = rline.split("\t");
      Double[] tokenx = new Double[token.length / 2];

      for (int i = 0; i < tokenx.length; i++) {
        tokenx[i] = Double.parseDouble(token[i * 2 + 1]);
      }
      return tokenx;
    }
  }

  public ArrayList<String> getHeadCandidates(String triple) {
    try {
      // System.out.println("head: " + triple);
      CompletionResult cr = this.results.get(triple);
      return cr.getHeads();
    } catch (RuntimeException e) {
      return new ArrayList<String>();
    }
  }

  public ArrayList<String> getTailCandidates(String triple) {
    // System.out.println("tail: " + triple);
    try {
      CompletionResult cr = this.results.get(triple);
      return cr.getTails();
    } catch (RuntimeException e) {
      return new ArrayList<String>();
    }
  }

  public ArrayList<Double> getHeadConfidences(String triple) {
    try {
      CompletionResult cr = this.results.get(triple);
      return cr.getHeadConfidences();
    } catch (RuntimeException e) {
      return new ArrayList<Double>();
    }
  }

  public ArrayList<Double> getTailConfidences(String triple) {
    try {
      CompletionResult cr = this.results.get(triple);
      return cr.getTailConfidences();
    } catch (RuntimeException e) {
      return new ArrayList<Double>();
    }
  }

  public String getName() {
    return this.name;
  }

  public void supressConnected(TripleSet trainingSet) {
    for (String triple : this.results.keySet()) {
      CompletionResult cr = this.results.get(triple);
      cr.supressConnected(trainingSet);
    }
  }
}
