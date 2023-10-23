package de.unima.ki.anyburl;

import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.io.IOHelper;
import de.unima.ki.anyburl.structure.Dice;
import de.unima.ki.anyburl.structure.Rule;
import de.unima.ki.anyburl.structure.RuleCyclic;
import de.unima.ki.anyburl.threads.RuleWriterAsThread;
import de.unima.ki.anyburl.threads.ScorerReinforced;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

public class LearnReinforced {

  private static long timeStamp = 0;

  private static String CONFIG_FILE = "config-learn.properties";

  // used at the begging to check if all threads are really available
  private static HashSet<Integer> availableThreads = new HashSet<Integer>();

  private static RuleWriterAsThread rwt = null;

  /** Path to the file that contains the triple set used for learning the rules. */

  /*
   * Lets hope that people will not run AnyBURl with more than 100 cores ... up to these 307 buckets should be sufficient
   * I somehow like the number 307
   */
  @SuppressWarnings("unchecked")
  private static HashSet<Rule>[] rules307 = new HashSet[307];

  static {
    for (int i = 0; i < 307; i++) {
      HashSet<Rule> anonymRuleSet = new HashSet<Rule>();
      rules307[i] = new HashSet<Rule>(Collections.synchronizedSet(anonymRuleSet));
    }
  }

  // private static HashSet<Rule> rules = new HashSet<Rule>();
  // public static Set<Rule> rulesSyn = Collections.synchronizedSet(rules);

  public static HashMap<String, RuleCyclic> indexedXYRules = new HashMap<String, RuleCyclic>();

  // private static HashMap<String, RuleIndex> indexedCL2Rules = new HashMap<String, RuleIndex>();

  public static int[][] stats;

  public static Dice dice;

  public static boolean active = true;
  public static boolean report = false;
  public static boolean[] activeThread;

  public static boolean finished = false;

  public static void main(String[] args) throws FileNotFoundException, InterruptedException {

    if (args.length == 1) {
      CONFIG_FILE = args[0];
      System.out.println("reading params from file " + CONFIG_FILE);
    }

    Properties prop = new Properties();
    InputStream input = null;

    try {
      input = new FileInputStream(CONFIG_FILE);
      prop.load(input);
      Settings.SINGLE_RELATIONS =
          IOHelper.getProperty(prop, "SINGLE_RELATIONS", Settings.SINGLE_RELATIONS);
      Settings.PATH_TRAINING = IOHelper.getProperty(prop, "PATH_TRAINING", Settings.PATH_TRAINING);
      Settings.CONSTANTS_OFF = IOHelper.getProperty(prop, "CONSTANTS_OFF", Settings.CONSTANTS_OFF);
      Settings.PATH_OUTPUT = IOHelper.getProperty(prop, "PATH_OUTPUT", Settings.PATH_OUTPUT);
      Settings.PATH_DICE = IOHelper.getProperty(prop, "PATH_DICE", Settings.PATH_DICE);
      Settings.SNAPSHOTS_AT = IOHelper.getProperty(prop, "SNAPSHOTS_AT", Settings.SNAPSHOTS_AT);
      Settings.SAMPLE_SIZE = IOHelper.getProperty(prop, "SAMPLE_SIZE", Settings.SAMPLE_SIZE);
      Settings.TRIAL_SIZE = IOHelper.getProperty(prop, "TRIAL_SIZE", Settings.TRIAL_SIZE);
      Settings.BATCH_TIME = IOHelper.getProperty(prop, "BATCH_TIME", Settings.BATCH_TIME);
      Settings.WORKER_THREADS =
          IOHelper.getProperty(prop, "WORKER_THREADS", Settings.WORKER_THREADS);
      Settings.MAX_LENGTH_CYCLIC =
          IOHelper.getProperty(prop, "MAX_LENGTH_CYCLIC", Settings.MAX_LENGTH_CYCLIC);
      Settings.MAX_LENGTH_ACYCLIC =
          IOHelper.getProperty(prop, "MAX_LENGTH_ACYCLIC", Settings.MAX_LENGTH_ACYCLIC);
      Settings.THRESHOLD_CORRECT_PREDICTIONS =
          IOHelper.getProperty(
              prop, "THRESHOLD_CORRECT_PREDICTIONS", Settings.THRESHOLD_CORRECT_PREDICTIONS);
      Settings.THRESHOLD_CONFIDENCE =
          IOHelper.getProperty(prop, "THRESHOLD_CONFIDENCE", Settings.THRESHOLD_CONFIDENCE);
      Settings.EPSILON = IOHelper.getProperty(prop, "EPSILON", Settings.EPSILON);
      Settings.SPECIALIZATION_CI =
          IOHelper.getProperty(prop, "SPECIALIZATION_CI", Settings.SPECIALIZATION_CI);
      Settings.REWARD = IOHelper.getProperty(prop, "SCORING_REGIME", Settings.REWARD);
      Settings.SCORING_REGIME_CONFDIFF =
          IOHelper.getProperty(prop, "SCORING_REGIME_CONFDIFF", Settings.SCORING_REGIME_CONFDIFF);
      Settings.POLICY = IOHelper.getProperty(prop, "POLICY", Settings.POLICY);
      Settings.MAX_LENGTH_GROUNDED_CYCLIC =
          IOHelper.getProperty(
              prop, "MAX_LENGTH_GROUNDED_CYCLIC", Settings.MAX_LENGTH_GROUNDED_CYCLIC);
      Settings.AC_MIN_NUM_OF_LAST_ATOM_GROUNDINGS =
          IOHelper.getProperty(
              prop,
              "AC_MIN_NUM_OF_LAST_ATOM_GROUNDINGS",
              Settings.AC_MIN_NUM_OF_LAST_ATOM_GROUNDINGS);

      Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS =
          IOHelper.getProperty(
              prop,
              "BEAM_SAMPLING_MAX_BODY_GROUNDINGS",
              Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS);
      Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS =
          IOHelper.getProperty(
              prop,
              "BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS",
              Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS);
      Settings.BEAM_SAMPLING_MAX_REPETITIONS =
          IOHelper.getProperty(
              prop, "BEAM_SAMPLING_MAX_REPETITIONS", Settings.BEAM_SAMPLING_MAX_REPETITIONS);

      Settings.REWRITE_REFLEXIV =
          IOHelper.getProperty(prop, "REWRITE_REFLEXIV", Settings.REWRITE_REFLEXIV);

    } catch (IOException ex) {
      System.err.println("Could not read relevant parameters from the config file " + CONFIG_FILE);
      ex.printStackTrace();
      System.exit(1);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
        }
      }
    }

    PrintWriter log = new PrintWriter(Settings.PATH_OUTPUT + "_log");
    log.println("Logfile");
    log.println("~~~~~~~\n");

    long indexStartTime = System.currentTimeMillis();

    TripleSet ts = new TripleSet(Settings.PATH_TRAINING);

    long indexEndTime = System.currentTimeMillis();
    log.println("indexing dataset: " + Settings.PATH_TRAINING);
    log.println("time elapsed: " + (indexEndTime - indexStartTime) + "ms");
    log.println();
    log.println(IOHelper.getParams());
    log.flush();

    long now = System.currentTimeMillis();

    // Thread[] scorer = new Thread[Learn.WORKER_THREADS];
    dice = new Dice(Settings.PATH_DICE);
    dice.computeRelevenatScores();
    dice.saveScores();
    // new

    activeThread = new boolean[Settings.WORKER_THREADS];
    stats = new int[Settings.WORKER_THREADS][3];

    ScorerReinforced[] scorer = new ScorerReinforced[Settings.WORKER_THREADS];
    for (int threadCounter = 0; threadCounter < Settings.WORKER_THREADS; threadCounter++) {

      Thread.sleep(50);
      System.out.println("* creating worker thread #" + threadCounter);
      ScorerReinforced s = new ScorerReinforced(ts, threadCounter);

      int type = dice.ask(0);
      boolean cyclic = Dice.decodedDiceCyclic(type);
      int len = Dice.decodedDiceLength(type);
      s.setSearchParameters(cyclic, len);

      scorer[threadCounter] = s;
      scorer[threadCounter].start();
      activeThread[threadCounter] = true;
    }

    dice.resetScores();

    boolean done = false;

    int batchCounter = 0;

    // =================
    // === MAIN LOOP ===
    // =================

    long startTime = System.currentTimeMillis();

    int snapshotIndex = 0;
    long batchStart = System.currentTimeMillis();
    while (done == false) {
      // System.out.println("main thread sleeps for 10 ms");
      Thread.sleep(10);

      now = System.currentTimeMillis();

      // elapsed seconds
      // snapshotIndex
      int elapsedSeconds = (int) (now - startTime) / 1000;
      int currentIndex = snapshotIndex;

      snapshotIndex = checkTimeMaybeStoreRules(log, done, snapshotIndex, elapsedSeconds, dice);

      if (snapshotIndex < 0) {
        done = true;
        break;
      }

      if (snapshotIndex > currentIndex) {
        // this needs t be done to avoid that a zeror time batch conducted because of long rule
        // storage times
        batchStart = System.currentTimeMillis();
        now = System.currentTimeMillis();
        // System.out.println("currentIndex=" +  currentIndex +  " snapshotIndex=" + snapshotIndex);
        // System.out.println("now: " + now);
      }

      if (now - batchStart > Settings.BATCH_TIME) {

        report = true;
        active = false;

        /// System.out.println(">>> set status to inactive");
        do {
          // System.out.println(">>> waiting for threads to report");
          Thread.sleep(10);
        } while (!allThreadsReported());
        // System.out.println(">>> all reports are available:");
        // System.out.println();

        // printStatsOfBatch();
        batchCounter++;
        // saturation = getSaturationOfBatch();

        dice.computeRelevenatScores();
        dice.saveScores();

        System.out.print(">>> Batch #" + batchCounter + " ");
        for (int t = 0; t < scorer.length; t++) {
          int type = dice.ask(batchCounter);
          // System.out.print(type + "|");
          boolean cyclic = Dice.decodedDiceCyclic(type);
          int len = Dice.decodedDiceLength(type);
          scorer[t].setSearchParameters(cyclic, len);
        }
        // System.out.print("   ");

        System.out.println(dice);

        dice.resetScores();

        activateAllThreads(scorer);
        batchStart = System.currentTimeMillis();
        active = true;
        report = false;
      }
    }
    // =================

    log.flush();
    log.close();

    System.exit(0);
  }

  private static int checkTimeMaybeStoreRules(
      PrintWriter log, boolean done, int snapshotIndex, int elapsedSeconds, Dice dice) {

    if (snapshotIndex >= Settings.SNAPSHOTS_AT.length) {
      System.out.println("Finished planned snapshots");
      return -1;
    }

    if (elapsedSeconds > Settings.SNAPSHOTS_AT[snapshotIndex] || done) {
      active = false;
      // this time might be required for letting the other threads go on one line in the code
      try {
        Thread.sleep(50);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      // ArrayList<Set<? extends Rule>> allUsefulRules = new ArrayList<Set<? extends Rule>>();

      // ...

      if (!done)
        System.out.println(
            "\n>>> CREATING SNAPSHOT " + snapshotIndex + " after " + elapsedSeconds + " seconds");
      else
        System.out.println("\n>>> CREATING FINAL SNAPSHOT 0 after " + elapsedSeconds + " seconds");
      String suffix = "" + (done ? 0 : Settings.SNAPSHOTS_AT[snapshotIndex]);
      rwt =
          new RuleWriterAsThread(
              Settings.PATH_OUTPUT,
              done ? 0 : Settings.SNAPSHOTS_AT[snapshotIndex],
              rules307,
              log,
              elapsedSeconds);
      rwt.start();
      // storeRules(Settings.PATH_OUTPUT, done ? 0 : Settings.SNAPSHOTS_AT[snapshotIndex], rulesSyn,
      // log, elapsedSeconds);
      System.out.println();
      dice.write(suffix);
      snapshotIndex++;
      if (snapshotIndex == Settings.SNAPSHOTS_AT.length || done) {
        log.close();
        System.out.println(">>> Bye, bye.");
        LearnReinforced.finished = true;

        while (rwt != null && rwt.isAlive()) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.out.println(">>> waiting for rule writer thread to finish");
        }

        System.exit(0);
      }
      active = true;
    }
    return snapshotIndex;
  }

  public static void printStatsOfBatch() {
    for (int i = 0; i < stats.length; i++) {
      System.out.print("Worker #" + i + ": ");
      for (int j = 0; j < stats[i].length - 1; j++) {
        System.out.print(stats[i][j] + " / ");
      }
      System.out.println(stats[i][stats[i].length - 1]);
    }
  }

  public static double getSaturationOfBatch() {
    int storedTotal = 0;
    int createdTotal = 0;
    for (int i = 0; i < stats.length; i++) {
      storedTotal += stats[i][0];
      createdTotal += stats[i][1];
    }
    return 1.0 - ((double) storedTotal / (double) createdTotal);
  }

  /**
   * Called by a worker thread to see if batch time is over. If this is the case the thread is
   * deactivated and the thread specific statistics of its performance in the current batch are
   * stored.
   *
   * @param threadId Id of the thread reporting.
   * @param storedRules the number of good rule that have been stored.
   * @param createdRules The number of created rules that have been created and checked for novelty
   *     and quality.
   * @return
   */
  public static boolean active(
      int threadId,
      int storedRules,
      int createdRules,
      double producedScore,
      boolean cyclic,
      int len) {
    if (active) return true;
    if (!report) return true;
    else if (activeThread[threadId]) {

      // System.out.println("retrieved message from thread " + threadId + " created=" + createdRules
      // + " stored=" + storedRules + " produced=" +producedScore);
      int type = Dice.encode(cyclic, len);
      // System.out.println("type of thread: " + type + " cyclic=" + cyclic + " len=" + len);
      stats[threadId][0] = storedRules;
      stats[threadId][1] = createdRules;
      // connect to the dice
      // TODO
      // int type = Dice.encode(cyclic, len);
      dice.addScore(type, producedScore);
      activeThread[threadId] = false;
      return false;
    }
    return false;
  }

  /**
   * Checks whether all worker threads have reported and are deactivated.
   *
   * @return True, if all threads have reported and are thus inactive.
   */
  public static boolean allThreadsReported() {
    for (int i = 0; i < activeThread.length; i++) {
      if (activeThread[i] == true) return false;
    }
    return true;
  }

  /**
   * Activates all threads .
   *
   * @param scorer The set of threads to be activated.
   */
  public static void activateAllThreads(Thread[] scorer) {
    for (int i = 0; i < activeThread.length; i++) {
      activeThread[i] = true;
    }
    active = true;
  }

  public static void showElapsedMoreThan(long duration, String message) {
    long now = System.currentTimeMillis();
    long elapsed = now - timeStamp;
    if (elapsed > duration) {
      System.err.println(message + " required " + elapsed + " millis!");
    }
  }

  public static void takeTime() {
    timeStamp = System.currentTimeMillis();
  }

  /**
   * Stores a given rule in a set. If the rule is a cyclic rule it also stores it in a way that is
   * can be checked in constants time for a AC1 rule if the AC1 follows.
   *
   * @param learnedRule
   */
  public static void storeRule(Rule rule) {
    int code307 = Math.abs(rule.hashCode()) % 307;
    rules307[code307].add(rule);
    // rulesSyn.add(rule);
    if (rule instanceof RuleCyclic) {
      indexXYRule((RuleCyclic) rule);
    }
  }

  private static synchronized void indexXYRule(RuleCyclic rule) {
    StringBuffer sb = new StringBuffer();
    sb.append(rule.getHead().toString());
    for (int i = 0; i < rule.bodysize(); i++) {
      sb.append(rule.getBodyAtom(i).toString());
    }
    String rs = sb.toString();
    if (indexedXYRules.containsKey(rs)) {
      // should not happen
    } else {
      indexedXYRules.put(rs, rule);
    }
  }

  /** Checks if the given rule is already stored. */
  public static boolean isStored(Rule rule) {
    int code307 = Math.abs(rule.hashCode()) % 307;
    if (!rules307[code307].contains(rule)) {
      return true;
    }
    return false;
  }

  public static boolean areAllThere() {
    // System.out.println("there are " + availableThreads.size() + " threads here" );
    if (availableThreads.size() == Settings.WORKER_THREADS) return true;
    return false;
  }

  public static void heyYouImHere(int id) {
    availableThreads.add(id);
  }
}
