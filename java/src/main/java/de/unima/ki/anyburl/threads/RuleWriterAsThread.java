package de.unima.ki.anyburl.threads;

import de.unima.ki.anyburl.structure.Rule;
import de.unima.ki.anyburl.structure.RuleCyclic;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class RuleWriterAsThread extends Thread {

  private LinkedList<Rule> rules;
  private String filepath;
  private int elapsedSeconds;
  private PrintWriter log;
  private int snapshotCounter;

  public RuleWriterAsThread(
      String filepath,
      int snapshotCounter,
      HashSet<Rule>[] rules307,
      PrintWriter log,
      int elapsedSeconds) {
    this.rules = new LinkedList<Rule>();
    for (int i = 0; i < 307; i++) {
      HashSet<Rule> ruleSet = rules307[i];
      for (Rule r : ruleSet) {
        this.rules.add(r);
      }
    }
    this.filepath = filepath;
    this.elapsedSeconds = elapsedSeconds;
    this.log = log;
    this.snapshotCounter = snapshotCounter;
  }

  public RuleWriterAsThread(
      String filepath,
      int snapshotCounter,
      ArrayList<HashSet<Rule>> ruless,
      PrintWriter log,
      int elapsedSeconds) {
    this.rules = new LinkedList<Rule>();
    for (HashSet<Rule> ruleSet : ruless) {

      for (Rule r : ruleSet) {
        this.rules.add(r);
      }
    }
    this.filepath = filepath;
    this.elapsedSeconds = elapsedSeconds;
    this.log = log;
    this.snapshotCounter = snapshotCounter;
  }

  public void run() {
    this.storeRules();
  }

  /*
  private void write() throws FileNotFoundException {
  	System.out.println("* starting to write rules to " + filepath);
  	long start = System.currentTimeMillis();

  	int i = 0;
  	PrintWriter pw = new PrintWriter(filepath);
  	for (Rule rule : rules) {
  		pw.println(rule);
  		i++;
  	}
  	pw.flush();
  	pw.close();
  	long stop = System.currentTimeMillis();
  	long elapsed = stop - start;
  	System.out.println("* wrote " + i + " rules to " + filepath + " within " + elapsed +  " ms");
  }
  */

  private void storeRules() {

    long startWriting = System.currentTimeMillis();
    File ruleFile = new File(this.filepath + "-" + this.snapshotCounter);
    int maxBodySize = 10;
    int[] acyclicCounter = new int[maxBodySize];
    int[] cyclicCounter = new int[maxBodySize];

    try {
      if (log != null) log.println();
      if (log != null) log.println("rule file: " + ruleFile.getPath());
      System.out.println(">>> storing rules in file " + ruleFile.getPath());

      PrintWriter pw = new PrintWriter(ruleFile);
      long numOfRules = 0;
      for (Rule r : rules) {
        if (r.bodysize() < maxBodySize)
          if (r instanceof RuleCyclic) cyclicCounter[r.bodysize() - 1]++;
          else acyclicCounter[r.bodysize() - 1]++;
        pw.println(r);
        numOfRules++;
      }
      // }
      pw.flush();
      pw.close();
      if (log != null) log.print("cyclic: ");
      for (int i = 0; i < maxBodySize; i++) {
        if (cyclicCounter[i] == 0) break;
        if (log != null) log.print(cyclicCounter[i] + " | ");
      }
      if (log != null) log.print("\nacyclic: ");
      for (int i = 0; i < maxBodySize; i++) {
        if (acyclicCounter[i] == 0) break;
        if (log != null) log.print(acyclicCounter[i] + " | ");
      }
      // log.println("\nfinally reached coverage: " + df.format(lastC * 100) + "%");
      long stopWriting = System.currentTimeMillis();
      if (log != null) log.println("time planned: " + snapshotCounter + "s");
      if (log != null) log.println("time elapsed: " + elapsedSeconds + "s");
      System.out.println(
          ">>> stored " + numOfRules + " rules in " + (stopWriting - startWriting) + "ms");
      if (log != null) log.println("");
      if (log != null) log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
