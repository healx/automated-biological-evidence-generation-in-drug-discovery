package de.unima.ki.anyburl.structure;

import de.unima.ki.anyburl.Settings;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class Dice {

  private String OUTPUT_PATH = null;

  private static final int SUPPORTED_TYPES = 12;

  private static final int SUPPORTED_TYPES_CYCLIC = 10;

  // a small number larger than 0, which is saved as score, if there has been an attempt to mine
  // that type
  // however, it has achieved a score of 0
  private static final double GAMMA = 0.0001;

  private static final double INITIAL_SCORE = Double.MAX_VALUE / (SUPPORTED_TYPES + 1.0);

  // [batch][type]

  private ArrayList<Long> timestamps = new ArrayList<Long>();

  private ArrayList<Double[]> scores = new ArrayList<Double[]>();
  private ArrayList<Integer[]> freqs = new ArrayList<Integer[]>();

  private double[] currentScores = new double[SUPPORTED_TYPES];
  private int[] currentFreqs = new int[SUPPORTED_TYPES];

  private double[] relevantScores = new double[SUPPORTED_TYPES];
  private boolean relevantScoresComputed = false;

  private static Random rand = new Random();

  // types listing, well, pretty awful way of doing this :-)
  // 0 = cyclic 1
  // 1 = cyclic 2
  // ...
  // 9 = cyclic 9
  // 10 = acyclic 1
  // 11 = acyclic 2

  public static void main(String[] args) {
    // a simulated run;

    int numOfThreads = 10;
    Dice dice = new Dice();

    for (int round = 0; round < 40; round++) {
      System.out.println("ROUND " + round);

      dice.computeRelevenatScores();
      dice.saveScores();
      System.out.println("DICE: " + dice);

      int thread[] = new int[numOfThreads];
      for (int i = 0; i < numOfThreads; i++) {
        int type = dice.ask(0);
        thread[i] = type;
        // System.out.print("dice=" + i + "=>" + type + "   ");
      }

      dice.resetScores();

      for (int t = 0; t < thread.length; t++) {
        {
          double s = simulateScore(thread[t]);
          dice.addScore(thread[t], s);
        }
      }
    }
  }

  public static double simulateScore(int type) {

    if (type == 3) return 100 + rand.nextDouble() * 10;
    if (type == 5) return 500 + rand.nextDouble() * 10;
    return 10 + rand.nextDouble() * 10;
  }

  public Dice() {
    this(null);
  }

  public Dice(String filePath) {
    this.OUTPUT_PATH = filePath;
    for (int i = 0; i < SUPPORTED_TYPES; i++) {
      this.currentScores[i] = INITIAL_SCORE;
      this.currentFreqs[i] = 1;
    }
    if (Settings.MAX_LENGTH_ACYCLIC == 0) {
      this.currentScores[SUPPORTED_TYPES_CYCLIC] = 0.0;
      this.currentScores[SUPPORTED_TYPES_CYCLIC + 1] = 0.0;
    }
    if (Settings.MAX_LENGTH_ACYCLIC == 1) this.currentScores[SUPPORTED_TYPES_CYCLIC + 1] = 0.0;
    for (int j = Settings.MAX_LENGTH_CYCLIC; j < SUPPORTED_TYPES_CYCLIC; j++)
      this.currentScores[j] = 0.0;
  }

  /**
   * Throws a dice for the rule types which is weighted according to the scores collected the last
   * time this type was mined.
   *
   * @return The type that was chosen by the dice.
   */
  public int ask(int batchCounter) {

    double r =
        ((double) Settings.RANDOMIZED_DECISIONS_ANNEALING - (double) batchCounter)
            / (double) Settings.RANDOMIZED_DECISIONS_ANNEALING;
    if (r < Settings.EPSILON) r = Settings.EPSILON;

    if (rand.nextDouble() < r) {
      int i;
      do {
        i = rand.nextInt(SUPPORTED_TYPES);
      } while (this.scores.get(0)[i] == 0);
      return i;
    }

    if (Settings.POLICY == 1) {
      double score;
      double max = -100.0;
      int maxIndex = -1;
      for (int i = 0; i < SUPPORTED_TYPES; i++) {
        score = relevantScores[i];
        if (score > max) {
          maxIndex = i;
          max = score;
        }
      }
      return maxIndex;
    } else if (Settings.POLICY == 2) {
      if (this.relevantScoresComputed == false)
        throw new RuntimeException(
            "before asking the dice you have to compute the relevant scores");
      double total = 0.0;
      for (int i = 0; i < SUPPORTED_TYPES; i++) {
        total += relevantScores[i];
      }
      double d = rand.nextDouble() * total;

      for (int i = 0; i < SUPPORTED_TYPES; i++) {
        if (d < relevantScores[i]) return i;
        d -= relevantScores[i];
      }
    }
    return 0; // should never happen
  }

  public void computeRelevenatScores() {

    for (int i = 0; i < SUPPORTED_TYPES; i++) {
      if (this.currentScores[i] > 0) {
        this.relevantScores[i] = this.currentScores[i] / (double) this.currentFreqs[i];
        // System.out.print(i + "<=" + this.relevantScores[i] + ", ");
      } else {
        // System.out.print(i + "==" + this.relevantScores[i] + ", ");
        // do nothing, keep relevant score from previous run
        // this might be the place to compute the last time this type has been tried
        // the more time is gone, the more probable it should be that this is used again
      }
    }
    this.relevantScoresComputed = true;
    // System.out.println();

  }

  public void resetScores() {
    for (int i = 0; i < SUPPORTED_TYPES; i++) {
      this.currentScores[i] = 0.0;
      this.currentFreqs[i] = 0;
    }
    this.relevantScoresComputed = false;
  }

  public synchronized void addScore(int index, double score) {
    // System.out.println("type=" + index + " scored:" + score);
    this.currentScores[index] += score;
    this.currentFreqs[index] += 1;
    if (score == 0.0) this.currentScores[index] += GAMMA;
  }

  public void saveScores() {
    this.scores.add(new Double[SUPPORTED_TYPES]);
    this.freqs.add(new Integer[SUPPORTED_TYPES]);
    this.timestamps.add(System.currentTimeMillis());
    Double[] lastScores = this.scores.get(this.scores.size() - 1);
    Integer[] lastFreqs = this.freqs.get(this.scores.size() - 1);
    for (int i = 0; i < this.currentScores.length; i++) {
      lastFreqs[i] = this.currentFreqs[i];
      lastScores[i] = this.currentScores[i] / (double) ((lastFreqs[i] > 0) ? lastFreqs[i] : 1);
    }
    // this.resetScores();
  }

  // decode and encode

  public static boolean decodedDiceCyclic(int dice) {
    if (dice >= 0 && dice < SUPPORTED_TYPES_CYCLIC) return true;
    else return false;
  }

  public static int decodedDiceLength(int dice) {
    if (decodedDiceCyclic(dice)) return dice + 1;
    else return dice - (SUPPORTED_TYPES_CYCLIC - 1);
  }

  public static int encode(boolean cyclic, int len) {
    if (cyclic) return len - 1;
    else return (SUPPORTED_TYPES_CYCLIC - 1) + len;
  }

  public void write(String suffix) {
    // System.out.println("writing to " + this.OUTPUT_PATH + suffix);
    if (this.OUTPUT_PATH == null) return;
    try {
      PrintWriter pw = new PrintWriter(this.OUTPUT_PATH + "_" + suffix);

      for (int n = 0; n < this.scores.size(); n++) {
        pw.print(this.timestamps.get(n));
        for (int i = 0; i < SUPPORTED_TYPES; i++) {
          pw.print("\t" + scores.get(n)[i]);
          // sb.append("\t" + scores.get(n)[i]);
        }
        for (int i = 0; i < SUPPORTED_TYPES; i++) {
          pw.print("\t" + freqs.get(n)[i]);
        }
        pw.print("\n");
      }

      pw.flush();
      pw.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public String toString() {
    DecimalFormat df = new DecimalFormat("000000");
    StringBuffer sb = new StringBuffer("");
    // sb.append("history:\n");
    /*
    for (int n = 0; n < this.scores.size(); n++) {
    	sb.append("n=" + n);
    	for (int i = 0; i < SUPPORTED_TYPES; i++) {
    		sb.append("\t" + df.format(scores.get(n)[i]));
    		// sb.append("\t" + scores.get(n)[i]);
    	}
    	sb.append("\n");
    	sb.append("    ");
    	for (int i = 0; i < SUPPORTED_TYPES; i++) {
    		sb.append("\t" + freqs.get(n)[i]);
    	}
    	sb.append("\n");
    }
    */
    for (int i = 0; i < SUPPORTED_TYPES; i++) {

      if (i >= Settings.MAX_LENGTH_CYCLIC && i < SUPPORTED_TYPES_CYCLIC) continue;
      if (i - SUPPORTED_TYPES_CYCLIC >= Settings.MAX_LENGTH_ACYCLIC) continue;
      if (i == SUPPORTED_TYPES_CYCLIC) sb.append(" |");
      double s = this.relevantScores[i];
      sb.append(" " + ((s > 999999) ? " > 99k" : df.format(s)));
    }

    // sb.append("\n");

    return sb.toString();
  }
}
