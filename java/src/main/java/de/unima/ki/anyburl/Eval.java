package de.unima.ki.anyburl;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.eval.HitsAtK;
import de.unima.ki.anyburl.eval.ResultSet;
import de.unima.ki.anyburl.io.IOHelper;
import de.unima.ki.anyburl.structure.Rule;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class Eval {

  private static String CONFIG_FILE = "config-eval.properties";

  /** Path to the file that contains the triple set used for learning the rules. */
  public static String PATH_TRAINING = "";

  /** Path to the file that contains the triple set used for to test the rules. */
  public static String PATH_TEST = "";

  /** Path to the file that contains the triple set used for validation purpose. */
  public static String PATH_VALID = "";

  /** Path to the output file where the predictions are stored. */
  public static String PATH_PREDICTIONS = "";

  public static void main(String[] args) throws IOException {

    if (args.length == 1) {
      CONFIG_FILE = args[0];
      System.out.println("reading params from file " + CONFIG_FILE);
    }

    Rule.applicationMode();
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = new FileInputStream(CONFIG_FILE);
      prop.load(input);
      PATH_TRAINING = IOHelper.getProperty(prop, "PATH_TRAINING", PATH_TRAINING);
      PATH_TEST = IOHelper.getProperty(prop, "PATH_TEST", PATH_TEST);
      PATH_VALID = IOHelper.getProperty(prop, "PATH_VALID", PATH_VALID);
      PATH_PREDICTIONS = IOHelper.getProperty(prop, "PATH_PREDICTIONS", PATH_PREDICTIONS);
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

    TripleSet trainingSet = new TripleSet(PATH_TRAINING);
    TripleSet validationSet = new TripleSet(PATH_VALID);
    TripleSet testSet = new TripleSet(PATH_TEST);

    int[] values = Apply.getMultiProcessing(PATH_PREDICTIONS);

    HitsAtK hitsAtK = new HitsAtK();
    hitsAtK.addFilterTripleSet(trainingSet);
    hitsAtK.addFilterTripleSet(validationSet);
    hitsAtK.addFilterTripleSet(testSet);

    StringBuffer sb = new StringBuffer();
    if (values.length == 0) {
      ResultSet rs = new ResultSet(PATH_PREDICTIONS, true, 10);
      computeScores(rs, testSet, hitsAtK);
      System.out.println(
          hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(2) + "   " + hitsAtK.getHitsAtK(9));
    } else {
      for (int value : values) {

        String rsPath = PATH_PREDICTIONS.replaceFirst("\\|.*\\|", "" + value);
        ResultSet rs = new ResultSet(rsPath, true, 10);
        computeScores(rs, testSet, hitsAtK);
        System.out.println(
            hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(2) + "   " + hitsAtK.getHitsAtK(9));
        sb.append(value + "   " + hitsAtK.getHitsAtK(0) + "   " + hitsAtK.getHitsAtK(9) + "\n");
      }
    }
    System.out.println("-----");
    System.out.println(sb);
  }

  private static void computeScores(ResultSet rs, TripleSet gold, HitsAtK hitsAtK) {
    for (Triple t : gold.getTriples()) {
      ArrayList<String> cand1 = rs.getHeadCandidates(t.toString());
      // String c1 = cand1.size() > 0 ? cand1.get(0) : "-";
      hitsAtK.evaluateHead(cand1, t);
      ArrayList<String> cand2 = rs.getTailCandidates(t.toString());
      // String c2 = cand2.size() > 0 ? cand2.get(0) : "-";
      hitsAtK.evaluateTail(cand2, t);
    }
  }
}
