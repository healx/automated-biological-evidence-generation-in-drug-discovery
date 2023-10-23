package de.unima.ki.anyburl.threads;

import de.unima.ki.anyburl.LearnReinforced;
import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.algorithm.PathSampler;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.exceptions.TimeOutException;
import de.unima.ki.anyburl.structure.Path;
import de.unima.ki.anyburl.structure.Rule;
import de.unima.ki.anyburl.structure.RuleFactory;
import java.util.ArrayList;

/** The worker thread responsible for learning rules in the reinforced learning setting. */
public class ScorerReinforced extends Thread {

  private TripleSet triples;
  private PathSampler sampler;

  // private int entailedCounter = 1;

  private int createdRules = 0;
  private int storedRules = 0;
  private double producedScore = 0.0;

  private int id = 0;

  private boolean mineParamCyclic = true; // possible values are true and false
  private int mineParamLength =
      1; // possible values are 1 and 2 (if non-cyclic), or 1, 2, 3, 4, 5 if (cyclic)

  private boolean ready = false;

  private boolean onlyXY = false;

  // ***** lets go ******

  public ScorerReinforced(TripleSet triples, int id) {
    this.triples = triples;
    this.sampler = new PathSampler(triples);
    this.id = id;
  }

  public void setSearchParameters(boolean cyclic, int len) {
    this.mineParamCyclic = cyclic;
    this.mineParamLength = len;
    this.ready = true;
    this.onlyXY = false;
    if (this.mineParamCyclic) {
      if (this.mineParamLength > Settings.MAX_LENGTH_GROUNDED_CYCLIC) {
        this.onlyXY = true;
      }
    }
    // System.out.println("THREAD-" + this.id + " using parameters C=" + this.mineParamCyclic + "
    // L=" + this.mineParamLength);
  }

  public void run() {

    while (!LearnReinforced.areAllThere()) {
      LearnReinforced.heyYouImHere(this.id);
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      // System.out.println("THREAD-" + this.id + " waiting for the others");
    }
    System.out.println(
        "THREAD-"
            + this.id
            + " starts to work with L="
            + this.mineParamLength
            + " C="
            + this.mineParamCyclic
            + " ");

    // outer loop is missing

    boolean done = false;
    while (done == false) {
      if (!LearnReinforced.active(
              this.id,
              this.storedRules,
              this.createdRules,
              this.producedScore,
              this.mineParamCyclic,
              this.mineParamLength)
          || !ready) {
        this.createdRules = 0;
        this.storedRules = 0;
        this.producedScore = 0.0;
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        // search for cyclic rules
        if (mineParamCyclic) {
          Path path = sampler.samplePath(this.mineParamLength + 1, true);
          if (path != null && path.isValid()) {
            // System.out.println(path);
            ArrayList<Rule> learnedRules = RuleFactory.getGeneralizations(path, this.onlyXY);
            // System.out.println(learnedRules.size());
            if (!LearnReinforced.active) {
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            } else {
              for (Rule learnedRule : learnedRules) {
                this.createdRules++;
                if (learnedRule.isTrivial()) continue;
                if (learnedRule.isRedundantACRule(triples)) continue;
                // long l2;
                // long l1 = System.currentTimeMillis();
                if (LearnReinforced.isStored(learnedRule)) {
                  // l2 = System.currentTimeMillis();

                  learnedRule.computeScores(this.triples);

                  if (learnedRule.getConfidence() >= Settings.THRESHOLD_CONFIDENCE
                      && learnedRule.getCorrectlyPredicted()
                          >= Settings.THRESHOLD_CORRECT_PREDICTIONS) {
                    if (LearnReinforced.active) {
                      LearnReinforced.storeRule(learnedRule);
                      // this.producedScore +=
                      // getScoringGain(learnedRule.getCorrectlyPredictedMax(),
                      // learnedRule.getConfidenceMax());
                      this.producedScore +=
                          getScoringGain(
                              learnedRule,
                              learnedRule.getCorrectlyPredicted(),
                              learnedRule.getConfidence(),
                              learnedRule.getAppliedConfidence());
                      this.storedRules++;
                    }
                  }
                } else {
                  // l2 = System.currentTimeMillis();
                }

                // if (l2 - l1 > 100) System.out.println("uppps");
              }
            }
          }
        }
        // search for acyclic rules
        if (!mineParamCyclic) {
          Path path = sampler.samplePath(mineParamLength + 1, false);
          if (path != null && path.isValid()) {
            ArrayList<Rule> learnedRules = RuleFactory.getGeneralizations(path, false);
            if (!LearnReinforced.active) {
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            } else {
              for (Rule learnedRule : learnedRules) {
                this.createdRules++;
                if (learnedRule.isTrivial()) continue;
                // long l2;
                // long l1 = System.currentTimeMillis();
                if (LearnReinforced.isStored(learnedRule)) {
                  // l2 = System.currentTimeMillis();
                  try {
                    learnedRule.computeScores(this.triples);
                    // System.out.println("-----> thread-" + this.id + " created (L=" +
                    // this.mineParamLength + " C=" + this.mineParamCyclic + "): " + learnedRule);
                  } catch (TimeOutException e) {
                    continue;
                  }

                  if (learnedRule.getConfidence() >= Settings.THRESHOLD_CONFIDENCE
                      && learnedRule.getCorrectlyPredicted()
                          >= Settings.THRESHOLD_CORRECT_PREDICTIONS) {
                    if (LearnReinforced.active) {
                      LearnReinforced.storeRule(learnedRule);
                      this.producedScore +=
                          getScoringGain(
                              learnedRule,
                              learnedRule.getCorrectlyPredicted(),
                              learnedRule.getConfidence(),
                              learnedRule.getAppliedConfidence());
                      this.storedRules++;
                    }
                  }
                } else {
                  // l2 = System.currentTimeMillis();
                }
                // if (l2 - l1 > 200) System.out.println("uppps");
              }
            }
          }
        }
      }
    }
  }

  public double getScoringGain(
      Rule rule, int correctlyPredicted, double confidence, double appliedConfidence) {
    if (Settings.REWARD == 1) return (double) correctlyPredicted;
    if (Settings.REWARD == 2) return (double) correctlyPredicted * confidence;
    if (Settings.REWARD == 3) return (double) correctlyPredicted * appliedConfidence;
    if (Settings.REWARD == 4)
      return (double) correctlyPredicted * appliedConfidence * appliedConfidence;
    if (Settings.REWARD == 5)
      return (double) correctlyPredicted * appliedConfidence / Math.pow(2, (rule.bodysize() - 1));
    return 0.0;
  }
}
