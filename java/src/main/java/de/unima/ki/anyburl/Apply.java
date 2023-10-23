package de.unima.ki.anyburl;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.io.IOHelper;
import de.unima.ki.anyburl.io.RuleReader;
import de.unima.ki.anyburl.structure.Rule;
import de.unima.ki.anyburl.structure.RuleAcyclic1;
import de.unima.ki.anyburl.structure.RuleAcyclic2;
import de.unima.ki.anyburl.structure.RuleCyclic;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Properties;

public class Apply {

  private static String CONFIG_FILE = "config-apply.properties";

  /**
   * Filter the rule set prior to applying it. Removes redundant rules which do not have any impact
   * (or no desired impact).
   */
  // public static boolean FILTER = true;

  /**
   * Always should be set to false. The TILDE results are based on a setting where this is set to
   * true. This parameter is sued to check in how far this setting increases the quality of the
   * results.
   */
  public static boolean USE_VALIDATION_AS_BK = false;

  public static void main(String[] args) throws IOException {

    if (args.length == 1) {
      CONFIG_FILE = args[0];
      System.out.println("* reading params from file " + CONFIG_FILE);
    }

    Rule.applicationMode();
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = new FileInputStream(CONFIG_FILE);
      prop.load(input);
      Settings.PREDICTION_TYPE =
          IOHelper.getProperty(prop, "PREDICTION_TYPE", Settings.PREDICTION_TYPE);
      if (Settings.PREDICTION_TYPE.equals("aRx")) {
        Settings.PATH_TRAINING =
            IOHelper.getProperty(prop, "PATH_TRAINING", Settings.PATH_TRAINING);
        Settings.PATH_MATERIALIZED =
            IOHelper.getProperty(prop, "PATH_MATERIALIZED", Settings.PATH_MATERIALIZED);
        Settings.PATH_TEST = IOHelper.getProperty(prop, "PATH_TEST", Settings.PATH_TEST);
        Settings.PATH_VALID = IOHelper.getProperty(prop, "PATH_VALID", Settings.PATH_VALID);
        Settings.PATH_OUTPUT = IOHelper.getProperty(prop, "PATH_OUTPUT", Settings.PATH_OUTPUT);
        Settings.PATH_EXPLANATION =
            IOHelper.getProperty(prop, "PATH_EXPLANATION", Settings.PATH_EXPLANATION);
        Settings.PATH_RULES = IOHelper.getProperty(prop, "PATH_RULES", Settings.PATH_RULES);
        Settings.PATH_RULES_BASE =
            IOHelper.getProperty(prop, "PATH_RULES_BASE", Settings.PATH_RULES_BASE);
        Settings.TOP_K_OUTPUT = IOHelper.getProperty(prop, "TOP_K_OUTPUT", Settings.TOP_K_OUTPUT);
        Settings.UNSEEN_NEGATIVE_EXAMPLES =
            IOHelper.getProperty(
                prop, "UNSEEN_NEGATIVE_EXAMPLES", Settings.UNSEEN_NEGATIVE_EXAMPLES);
        Settings.UNSEEN_NEGATIVE_EXAMPLES_REFINE =
            IOHelper.getProperty(
                prop, "UNSEEN_NEGATIVE_EXAMPLES_REFINE", Settings.UNSEEN_NEGATIVE_EXAMPLES_REFINE);
        Settings.THRESHOLD_CONFIDENCE =
            IOHelper.getProperty(prop, "THRESHOLD_CONFIDENCE", Settings.THRESHOLD_CONFIDENCE);
        Settings.DISCRIMINATION_BOUND =
            IOHelper.getProperty(prop, "DISCRIMINATION_BOUND", Settings.DISCRIMINATION_BOUND);
        Settings.TRIAL_SIZE = IOHelper.getProperty(prop, "TRIAL_SIZE", Settings.TRIAL_SIZE);
        Settings.WORKER_THREADS =
            IOHelper.getProperty(prop, "WORKER_THREADS", Settings.WORKER_THREADS);
        Settings.READ_CYCLIC_RULES =
            IOHelper.getProperty(prop, "READ_CYCLIC_RULES", Settings.READ_CYCLIC_RULES);
        Settings.READ_ACYCLIC1_RULES =
            IOHelper.getProperty(prop, "READ_ACYCLIC1_RULES", Settings.READ_ACYCLIC1_RULES);
        Settings.READ_ACYCLIC2_RULES =
            IOHelper.getProperty(prop, "READ_ACYCLIC2_RULES", Settings.READ_ACYCLIC2_RULES);
        Settings.UNSEEN_NEGATIVE_EXAMPLES_ATYPED =
            IOHelper.getProperty(
                prop, "UNSEEN_NEGATIVE_EXAMPLES_ATYPED", Settings.UNSEEN_NEGATIVE_EXAMPLES_ATYPED);
        Settings.TYPE_SPLIT_ANALYSIS =
            IOHelper.getProperty(prop, "TYPE_SPLIT_ANALYSIS", Settings.TYPE_SPLIT_ANALYSIS);
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

        // FILTER = IOHelper.getProperty(prop, "FILTER", FILTER);
        Settings.AGGREGATION_TYPE =
            IOHelper.getProperty(prop, "AGGREGATION_TYPE", Settings.AGGREGATION_TYPE);

        if (Settings.AGGREGATION_TYPE.equals("maxplus")) Settings.AGGREGATION_ID = 1;
        if (Settings.AGGREGATION_TYPE.equals("noisyor")) Settings.AGGREGATION_ID = 2;
        if (Settings.AGGREGATION_TYPE.equals("harmonicSum")) Settings.AGGREGATION_ID = 3;

      }
      /*
      else if (PREDICTION_TYPE.equals("xRy")) {
      	TRESHOLD_FUNCTIONAL = IOHelper.getProperty(prop, "TRESHOLD_FUNCTIONAL", TRESHOLD_FUNCTIONAL);
      	System.out.println("TF: " + TRESHOLD_FUNCTIONAL);
      	PATH_TRAINING = IOHelper.getProperty(prop, "PATH_TRAINING",PATH_TRAINING);
      	PATH_TEST = IOHelper.getProperty(prop, "PATH_TEST", PATH_TEST);
      	PATH_VALID = IOHelper.getProperty(prop, "PATH_VALID",PATH_VALID);
      	PATH_OUTPUT = IOHelper.getProperty(prop, "PATH_OUTPUT", PATH_OUTPUT);
      	PATH_RULES = IOHelper.getProperty(prop, "PATH_RULES", PATH_RULES);
      	TOP_K_OUTPUT = IOHelper.getProperty(prop, "TOP_K_OUTPUT", TOP_K_OUTPUT);
      	COMBINATION_RULE = IOHelper.getProperty(prop, "COMBINATION_RULE", COMBINATION_RULE);
      	if (COMBINATION_RULE.equals("multiplication")) RuleEngine.COMBINATION_RULE_ID = 1;
      	if (COMBINATION_RULE.equals("maxplus")) RuleEngine.COMBINATION_RULE_ID = 2;
      	if (COMBINATION_RULE.equals("max")) RuleEngine.COMBINATION_RULE_ID = 3;
      }
      */
      else {
        System.err.println(
            "The prediction type " + Settings.PREDICTION_TYPE + " is not yet supported.");
        System.exit(1);
      }

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

    if (Settings.PREDICTION_TYPE.equals("aRx")) {
      String[] TSA = new String[] {"ALL"};
      if (Settings.TYPE_SPLIT_ANALYSIS) {
        TSA =
            new String[] {
              "ALL", "C-1", "C-2", "C-3", "AC1-1", "AC1-2", "AC2-1", "AC2-2", "N-C-1", "N-C-2",
              "N-C-3", "N-AC1-1", "N-AC1-2", "N-AC2-1", "N-AC2-2"
            };
      }
      for (int TSAindex = 0; TSAindex < TSA.length; TSAindex++) {

        int[] values = getMultiProcessing(Settings.PATH_RULES);

        PrintWriter log = null;

        if (Settings.TYPE_SPLIT_ANALYSIS) {
          if (values[0] == 0) log = new PrintWriter(Settings.PATH_RULES + "_plog_" + TSA[TSAindex]);
          else
            log = new PrintWriter(Settings.PATH_OUTPUT.replace("|", "") + "_plog_" + TSA[TSAindex]);
        } else {
          if (values[0] == 0) log = new PrintWriter(Settings.PATH_RULES + "_plog");
          else log = new PrintWriter(Settings.PATH_OUTPUT.replace("|", "") + "_plog");
        }

        log.println("Logfile");
        log.println("~~~~~~~\n");
        log.println();
        log.println(IOHelper.getParams());
        log.flush();

        RuleReader rr = new RuleReader();
        LinkedList<Rule> base = new LinkedList<Rule>();
        if (!Settings.PATH_RULES_BASE.equals("")) {
          System.out.println("* reading additional rule file as base");
          base = rr.read(Settings.PATH_RULES_BASE);
        }

        for (int value : values) {

          long startTime = System.currentTimeMillis();

          // long indexStartTime = System.currentTimeMillis();
          String path_output_used = null;
          String path_rules_used = null;
          if (value == 0) {
            path_output_used = Settings.PATH_OUTPUT;
            path_rules_used = Settings.PATH_RULES;
          }
          if (value > 0) {
            path_output_used = Settings.PATH_OUTPUT.replaceFirst("\\|.*\\|", "" + value);
            path_rules_used = Settings.PATH_RULES.replaceFirst("\\|.*\\|", "" + value);
          }
          if (Settings.TYPE_SPLIT_ANALYSIS) {
            path_output_used += TSA[TSAindex];
          }
          log.println("rules:   " + path_rules_used);
          log.println("output: " + path_output_used);
          log.flush();

          PrintWriter pw = new PrintWriter(new File(path_output_used));

          if (Settings.PATH_EXPLANATION != null)
            Settings.EXPLANATION_WRITER = new PrintWriter(new File(Settings.PATH_EXPLANATION));
          System.out.println("* writing prediction to " + path_output_used);

          TripleSet trainingSet = new TripleSet(Settings.PATH_TRAINING);
          TripleSet materializedSet;
          if (Settings.PATH_MATERIALIZED == null || Settings.PATH_MATERIALIZED.equals(""))
            materializedSet = null;
          else materializedSet = new TripleSet(Settings.PATH_MATERIALIZED);

          TripleSet testSet = new TripleSet(Settings.PATH_TEST);
          TripleSet validSet = new TripleSet(Settings.PATH_VALID);
          // TripleSet validSet = null;
          // TripleSet testSet = null;
          // System.out.println("read all files");
          // System.exit(0);

          // check if you should predict only unconnected
          checkIfPredictOnlyUnconnected(validSet, trainingSet);

          if (USE_VALIDATION_AS_BK) {
            trainingSet.addTripleSet(validSet);
            validSet = new TripleSet();
          }

          LinkedList<Rule> rules = rr.read(path_rules_used);
          rules.addAll(base);

          int rulesSize = rules.size();
          LinkedList<Rule> rulesThresholded = new LinkedList<Rule>();
          if (Settings.THRESHOLD_CONFIDENCE > 0.0) {
            for (Rule r : rules) {

              // if (r instanceof RuleAcyclic1 && (r.bodysize() == 3 || r.bodysize() == 2) &&
              // r.getHead().getConstant().equals(r.getBodyAtom(r.bodysize()-1).getConstant()))
              // continue;
              if (r.getConfidence() > Settings.THRESHOLD_CONFIDENCE) {
                if (Settings.TYPE_SPLIT_ANALYSIS) {
                  filterTSA(TSA, TSAindex, rulesThresholded, r);
                } else {
                  rulesThresholded.add(r);
                }
              }
            }
            System.out.println(
                "* applied confidence threshold of "
                    + Settings.THRESHOLD_CONFIDENCE
                    + " and reduced from "
                    + rules.size()
                    + " to "
                    + rulesThresholded.size()
                    + " rules");
          }
          rules = rulesThresholded;
          RuleEngine.applyRulesARX(
              rules, testSet, trainingSet, validSet, materializedSet, Settings.TOP_K_OUTPUT, pw);

          long endTime = System.currentTimeMillis();
          System.out.println(
              "* evaluated "
                  + rulesSize
                  + " rules to propose candiates for "
                  + testSet.getTriples().size()
                  + "*2 completion tasks");
          System.out.println("* finished in " + (endTime - startTime) + "ms.");

          System.out.println();

          log.println("finished in " + (endTime - startTime) / 1000 + "s.");
          log.println();
          log.flush();
        }
        log.close();
      }
    }
    /*
    if (PREDICTION_TYPE.equals("xRy")) {


    	TripleSet trainingSet = new TripleSet(PATH_TRAINING);
    	TripleSet testSet = new TripleSet(PATH_TEST);
    	TripleSet validSet = new TripleSet(PATH_VALID);

    	RuleReader rr = new RuleReader();
    	List<Rule> rules = rr.read(PATH_RULES);
    	long startTime = System.currentTimeMillis();
    	RuleEngine.applyRulesXRY(rules, testSet, trainingSet, validSet, TOP_K_OUTPUT);


    	long endTime = System.currentTimeMillis();
    	System.out.println("* evaluated " + rules.size() + " rules to propose candiates for pairwise completion tasks");
    	System.out.println("* finished in " + (endTime - startTime) + "ms.");
    }
    */

  }

  /**
   * Checks whether the validation set has more than 500 triples. If yes, it is checked whether
   * those are only connecting entities that are unconnected in the training set. If this is the
   * case the corresponding parameter is set to true, which results into a filtering out such
   * predictions.
   *
   * @param validSet
   * @param trainingSet
   */
  private static void checkIfPredictOnlyUnconnected(TripleSet validSet, TripleSet trainingSet) {
    if (validSet.size() > 500) {
      for (Triple t : validSet.getTriples()) {
        if (trainingSet.getRelations(t.getHead(), t.getTail()).size() > 0) {
          return;
        }
        if (trainingSet.getRelations(t.getTail(), t.getHead()).size() > 0) {
          return;
        }
      }
      System.out.println(
          "* set param PREDICT_ONLY_UNCONNECTED due to validation set characteristics");
      Settings.PREDICT_ONLY_UNCONNECTED = true;
    }
  }

  private static void filterTSA(
      String[] TSA, int TSAindex, LinkedList<Rule> rulesThresholded, Rule r) {
    switch (TSA[TSAindex]) {
      case "ALL":
        rulesThresholded.add(r);
        break;
      case "C-1":
        if (r instanceof RuleCyclic && r.bodysize() == 1) rulesThresholded.add(r);
        break;
      case "C-2":
        if (r instanceof RuleCyclic && r.bodysize() == 2) rulesThresholded.add(r);
        break;
      case "C-3":
        if (r instanceof RuleCyclic && r.bodysize() == 3) rulesThresholded.add(r);
        break;
      case "AC1-1":
        if (r instanceof RuleAcyclic1 && r.bodysize() == 1) rulesThresholded.add(r);
        break;
      case "AC1-2":
        if (r instanceof RuleAcyclic1 && r.bodysize() == 2) rulesThresholded.add(r);
        break;
      case "AC2-1":
        if (r instanceof RuleAcyclic2 && r.bodysize() == 1) rulesThresholded.add(r);
        break;
      case "AC2-2":
        if (r instanceof RuleAcyclic2 && r.bodysize() == 2) rulesThresholded.add(r);
        break;
      case "N-C-1":
        if (!(r instanceof RuleCyclic && r.bodysize() == 1)) rulesThresholded.add(r);
        break;
      case "N-C-2":
        if (!(r instanceof RuleCyclic && r.bodysize() == 2)) rulesThresholded.add(r);
        break;
      case "N-C-3":
        if (!(r instanceof RuleCyclic && r.bodysize() == 3)) rulesThresholded.add(r);
        break;
      case "N-AC1-1":
        if (!(r instanceof RuleAcyclic1 && r.bodysize() == 1)) rulesThresholded.add(r);
        break;
      case "N-AC1-2":
        if (!(r instanceof RuleAcyclic1 && r.bodysize() == 2)) rulesThresholded.add(r);
        break;
      case "N-AC2-1":
        if (!(r instanceof RuleAcyclic2 && r.bodysize() == 1)) rulesThresholded.add(r);
        break;
      case "N-AC2-2":
        if (!(r instanceof RuleAcyclic2 && r.bodysize() == 2)) rulesThresholded.add(r);
        break;
    }
  }

  /*
  private static void showRulesStats(List<Rule> rules) {
  	int xyCounter = 0;
  	int xCounter = 0;
  	int yCounter = 0;
  	for (Rule rule : rules) {
  		if (rule.isXYRule()) xyCounter++;
  		if (rule.isXRule()) xCounter++;
  		if (rule.isYRule()) yCounter++;
  	}
  	System.out.println("XY=" + xyCounter + " X="+ xCounter + " Y=" + yCounter);

  }
  */

  public static int[] getMultiProcessing(String path1) {
    String token[] = path1.split("\\|");
    if (token.length < 2) {
      return new int[] {0};
    } else {
      String numbers[] = token[1].split(",");
      int[] result = new int[numbers.length];
      int i = 0;
      for (String n : numbers) {
        result[i] = Integer.parseInt(n);
        i++;
      }
      return result;
    }
  }
}
