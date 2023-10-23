package de.unima.ki.anyburl.structure;

import de.unima.ki.anyburl.Settings;
import de.unima.ki.anyburl.data.SampledPairedResultSet;
import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import de.unima.ki.anyburl.exceptions.TimeOutException;
import de.unima.ki.anyburl.exceptions.Timer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RuleCyclic extends Rule {

  public RuleCyclic(RuleUntyped r) {
    super(r);
    // modify it to its canonical form

    if (this.body.get(0).contains("Y") && this.bodysize() > 1) {
      // if (this.bodysize() > 3) System.out.println("before: " + this);
      for (int i = 0; i <= (this.bodysize() / 2) - 1; i++) {
        int j = (this.bodysize() - i) - 1;
        Atom atom_i = this.body.get(i);
        Atom atom_j = this.body.get(j);
        this.body.set(i, atom_j);
        this.body.set(j, atom_i);
      }
      this.body.normalizeVariableNames();
      // if (this.bodysize() > 3) System.out.println("after: " + this);
    }
  }

  public HashSet<String> computeTailResults(String head, TripleSet ts) {
    HashSet<String> results = new HashSet<String>();
    Timer count = new Timer();
    // if (Settings.BEAM_NOT_DFS) {
    //	results = this.beamPGBodyCyclic("X", "Y", head, 0, true, ts);
    // }
    // else {
    this.getCyclic("X", "Y", head, 0, true, ts, new HashSet<String>(), results, count);
    // }
    return results;
  }

  public HashSet<String> computeHeadResults(String tail, TripleSet ts) {
    HashSet<String> results = new HashSet<String>();
    Timer count = new Timer();
    // if (Settings.BEAM_NOT_DFS) {
    //	results = this.beamPGBodyCyclic("Y", "X", tail, this.bodysize() - 1, false, ts);
    // }
    // else {
    this.getCyclic(
        "Y", "X", tail, this.bodysize() - 1, false, ts, new HashSet<String>(), results, count);
    // }
    return results;
  }

  @Override
  public void computeScores(TripleSet triples) {
    // X is given in first body atom
    SampledPairedResultSet xypairs;
    SampledPairedResultSet xypairsReverse;

    if (this.body.get(0).contains("X")) {
      if (Settings.BEAM_NOT_DFS) {
        if (Settings.BEAM_TYPE_EDIS) {
          xypairs = beamBodyCyclicEDIS("X", "Y", triples);
          xypairsReverse = beamBodyCyclicReverseEDIS("X", "Y", triples);
        } else {
          xypairs = beamBodyCyclic("X", "Y", triples);
          xypairsReverse = beamBodyCyclicReverse("X", "Y", triples);
        }
      } else {
        try {
          xypairs = groundBodyCyclic("X", "Y", triples);
        } catch (TimeOutException e) {
          xypairs = new SampledPairedResultSet();
        }
        xypairsReverse = xypairs;
      }
    } else {
      if (Settings.BEAM_NOT_DFS) {
        if (Settings.BEAM_TYPE_EDIS) {
          xypairs = beamBodyCyclicEDIS("Y", "X", triples);
          xypairsReverse = beamBodyCyclicReverseEDIS("Y", "X", triples);
        } else {
          xypairs = beamBodyCyclic("Y", "X", triples);
          xypairsReverse = beamBodyCyclicReverse("Y", "X", triples);
        }
      } else {
        try {
          xypairs = groundBodyCyclic("Y", "X", triples);
        } catch (TimeOutException e) {
          xypairs = new SampledPairedResultSet();
        }
        xypairsReverse = xypairs;
      }
    }

    int predictedAll = 0;
    int correctlyPredictedAll = 0;
    // body groundings for head prediction
    int correctlyPredicted = 0;
    int predicted = 0;
    for (String key : xypairsReverse.getValues().keySet()) {
      for (String value : xypairsReverse.getValues().get(key)) {
        predicted++;
        if (triples.isTrue(key, this.head.getRelation(), value)) correctlyPredicted++;
      }
    }

    // double f;
    // if (predicted == 0) { f = 1.0; }
    // else { f = xypairsReverse.getChaoEstimate() / predicted; }

    predictedAll += predicted;
    correctlyPredictedAll += correctlyPredicted;

    // body groundings for head prediction
    correctlyPredicted = 0;
    predicted = 0;
    for (String key : xypairs.getValues().keySet()) {
      for (String value : xypairs.getValues().get(key)) {
        predicted++;
        if (triples.isTrue(key, this.head.getRelation(), value)) correctlyPredicted++;
      }
    }

    predictedAll += predicted;
    correctlyPredictedAll += correctlyPredicted;

    this.predicted = predictedAll;
    this.correctlyPredicted = correctlyPredictedAll;
    this.confidence = (double) this.correctlyPredicted / (double) this.predicted;
  }

  /*
  public int estimateAllBodyGroundings() {
  	for (int i = 0; i < this.bodysize(); i++) {

  		Atom atom = this.getBodyAtom(i);
  		atom.get




  	}

  }
  */

  /**
   * The new implementation if the sample based computation of the scores. Samples completely random
   * attempts to create a beam over the body.
   *
   * @param triples
   */
  /*
  public void beamScores(TripleSet triples) {
  	long startScoring = System.currentTimeMillis();
  	// X is given in first body atom
  	SampledPairedResultSet xypairs;
  	if (this.body.get(0).contains("X")) {
  		xypairs = beamBodyCyclic("X", "Y", triples);
  	}
  	else {
  		xypairs = beamBodyCyclic("Y", "X", triples);
  	}
  	// body groundings
  	int correctlyPredicted = 0;
  	int predicted = 0;
  	for (String key : xypairs.getValues().keySet()) {
  		for (String value : xypairs.getValues().get(key)) {
  			if (Settings.PREDICT_ONLY_UNCONNECTED) {
  				Set<String> links = triples.getRelations(key, value);
  				Set<String> invLinks = triples.getRelations(value, key);
  				if (invLinks.size() > 0) continue;
  				if (!links.contains(this.head.getRelation()) && links.size() > 0) continue;
  				if (links.contains(this.head.getRelation()) && links.size() > 1) continue;
  			}
  			predicted++;
  			if (triples.isTrue(key, this.head.getRelation(), value)) {
  				correctlyPredicted++;
  			}
  		}
  	}
  	this.predicted = predicted;
  	this.correctlyPredicted = correctlyPredicted;
  	this.confidence = (double)correctlyPredicted / (double)predicted;

  }
  */

  public Triple getRandomValidPrediction(TripleSet triples) {
    ArrayList<Triple> validPredictions = this.getPredictions(triples, 1);
    if (validPredictions == null || validPredictions.size() == 0) return null;
    if (validPredictions.size() == 0) return null;
    int index = rand.nextInt(validPredictions.size());
    return validPredictions.get(index);
  }

  public Triple getRandomInvalidPrediction(TripleSet triples) {
    ArrayList<Triple> validPredictions = this.getPredictions(triples, -1);
    if (validPredictions == null || validPredictions.size() == 0) return null;
    if (validPredictions.size() == 0) return null;
    int index = rand.nextInt(validPredictions.size());
    return validPredictions.get(index);
  }

  public ArrayList<Triple> getPredictions(TripleSet triples) {
    return this.getPredictions(triples, 0);
  }

  /**
   * @param triples
   * @param valid 1= must be valid; -1 = must be invalid; 0 = valid and invalid is okay
   * @return
   */
  protected ArrayList<Triple> getPredictions(TripleSet triples, int valid) {
    SampledPairedResultSet xypairs;
    if (this.body.get(0).contains("X")) xypairs = groundBodyCyclic("X", "Y", triples);
    else xypairs = groundBodyCyclic("Y", "X", triples);
    ArrayList<Triple> predictions = new ArrayList<Triple>();
    for (String key : xypairs.getValues().keySet()) {
      for (String value : xypairs.getValues().get(key)) {
        if (valid == 1) {
          if (triples.isTrue(key, this.head.getRelation(), value)) {
            Triple validPrediction = new Triple(key, this.head.getRelation(), value);
            predictions.add(validPrediction);
          }
        } else if (valid == -1) {
          if (!triples.isTrue(key, this.head.getRelation(), value)) {
            Triple invalidPrediction = new Triple(key, this.head.getRelation(), value);
            predictions.add(invalidPrediction);
          }
        } else {
          Triple validPrediction = new Triple(key, this.head.getRelation(), value);
          predictions.add(validPrediction);
        }
      }
    }
    return predictions;
  }

  public boolean isPredictedX(String leftValue, String rightValue, Triple forbidden, TripleSet ts) {
    System.err.println("method not YET available for an extended/refinde rule");
    return false;
  }

  // *** PRIVATE PLAYGROUND ****

  private void getCyclic(
      String currentVariable,
      String lastVariable,
      String value,
      int bodyIndex,
      boolean direction,
      TripleSet triples,
      HashSet<String> previousValues,
      HashSet<String> finalResults,
      Timer count) {

    if (this.hasNegation()) {
      if (this.negation.getLeft().equals(currentVariable)) {
        Set<String> tails = triples.getTailEntities(this.negation.getRelation(), value);
        if (this.negation.isVariableRight()) {
          if (tails.size() > 0) return;
        } else {
          if (tails.contains(this.negation.getRight())) return;
        }
      } else if (this.negation.getRight().equals(currentVariable)) {
        Set<String> heads = triples.getHeadEntities(this.negation.getRelation(), value);
        if (this.negation.isVariableLeft()) {
          if (heads.size() > 0) return;
        } else {
          if (heads.contains(this.negation.getLeft())) return;
        }
      }
    }
    if (Rule.APPLICATION_MODE && finalResults.size() >= Settings.DISCRIMINATION_BOUND) {
      finalResults.clear();
      return;
    }
    // XXX if (!Rule.APPLICATION_MODE && finalResults.size() >= Settings.SAMPLE_SIZE) return;
    // check if the value has been seen before as grounding of another variable
    Atom atom = this.body.get(bodyIndex);
    boolean headNotTail = atom.getLeft().equals(currentVariable);
    if (previousValues.contains(value)) return;
    // the current atom is the last
    if ((direction == true && this.body.size() - 1 == bodyIndex)
        || (direction == false && bodyIndex == 0)) {
      // get groundings
      for (String v : triples.getEntities(atom.getRelation(), value, headNotTail)) {
        if (!Rule.APPLICATION_MODE && count.timeOut()) throw new TimeOutException();
        if (!previousValues.contains(v) && !value.equals(v)) finalResults.add(v);
      }
      return;
    }
    // the current atom is not the last
    else {
      Set<String> results = triples.getEntities(atom.getRelation(), value, headNotTail);
      String nextVariable = headNotTail ? atom.getRight() : atom.getLeft();
      HashSet<String> currentValues = new HashSet<String>();
      currentValues.addAll(previousValues);
      currentValues.add(value);
      int i = 0;

      for (String nextValue : results) {
        if (!Rule.APPLICATION_MODE && count.timeOut()) throw new TimeOutException();
        // XXX if (!Rule.APPLICATION_MODE && i >= Settings.SAMPLE_SIZE) break;
        int updatedBodyIndex = (direction) ? bodyIndex + 1 : bodyIndex - 1;
        this.getCyclic(
            nextVariable,
            lastVariable,
            nextValue,
            updatedBodyIndex,
            direction,
            triples,
            currentValues,
            finalResults,
            count);
        i++;
      }
      return;
    }
  }

  private SampledPairedResultSet groundBodyCyclic(
      String firstVariable, String lastVariable, TripleSet triples) {
    return groundBodyCyclic(firstVariable, lastVariable, triples, true);
  }

  private SampledPairedResultSet groundBodyCyclic(
      String firstVariable, String lastVariable, TripleSet triples, boolean samplingOn) {
    SampledPairedResultSet groundings = new SampledPairedResultSet();
    Atom atom = this.body.get(0);
    boolean headNotTail = atom.getLeft().equals(firstVariable);
    ArrayList<Triple> rtriples = triples.getTriplesByRelation(atom.getRelation());
    int counter = 0;
    Timer count = new Timer();
    for (Triple t : rtriples) {
      counter++;
      HashSet<String> lastVariableGroundings = new HashSet<String>();
      // the call itself
      this.getCyclic(
          firstVariable,
          lastVariable,
          t.getValue(headNotTail),
          0,
          true,
          triples,
          new HashSet<String>(),
          lastVariableGroundings,
          count);
      if (lastVariableGroundings.size() > 0) {
        if (firstVariable.equals("X")) {
          groundings.addKey(t.getValue(headNotTail));
          for (String lastVariableValue : lastVariableGroundings) {
            groundings.addValue(lastVariableValue);
          }
        } else {
          for (String lastVariableValue : lastVariableGroundings) {
            groundings.addKey(lastVariableValue);
            groundings.addValue(t.getValue(headNotTail));
          }
        }
      }
      if ((counter > Settings.SAMPLE_SIZE || groundings.size() > Settings.SAMPLE_SIZE)
          && samplingOn) {
        break;
      }
      if (!Rule.APPLICATION_MODE && count.timeOut()) throw new TimeOutException();
    }
    return groundings;
  }

  private SampledPairedResultSet beamBodyCyclic(
      String firstVariable, String lastVariable, TripleSet triples) {
    SampledPairedResultSet groundings = new SampledPairedResultSet();
    Atom atom = this.body.get(0);
    boolean headNotTail = atom.getLeft().equals(firstVariable);
    Triple t;
    int attempts = 0;
    int repetitions = 0;
    while ((t = triples.getRandomTripleByRelation(atom.getRelation())) != null) {
      attempts++;
      String lastVarGrounding =
          this.beamCyclic(
              firstVariable, t.getValue(headNotTail), 0, true, triples, new HashSet<String>());
      // String lastVarGrounding = this.beamCyclic(firstVariable, t.getValue(headNotTail), 0, true,
      // triples, new HashSet<String>());
      if (lastVarGrounding != null) {
        if (firstVariable.equals("X")) {
          groundings.addKey(t.getValue(headNotTail));
          if (groundings.addValue(lastVarGrounding)) repetitions = 0;
          else repetitions++;
        } else {
          groundings.addKey(lastVarGrounding);
          if (groundings.addValue(t.getValue(headNotTail))) repetitions = 0;
          else repetitions++;
        }
      }
      if (Settings.BEAM_SAMPLING_MAX_REPETITIONS <= repetitions) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS <= attempts) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS <= groundings.size()) break;
    }
    return groundings;
  }

  private SampledPairedResultSet beamBodyCyclicEDIS(
      String firstVariable, String lastVariable, TripleSet triples) {
    SampledPairedResultSet groundings = new SampledPairedResultSet();
    Atom atom = this.body.get(0);
    boolean headNotTail = atom.getLeft().equals(firstVariable);
    int repetitions = 0;
    ArrayList<String> entities =
        triples.getNRandomEntitiesByRelation(
            atom.getRelation(), headNotTail, Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS);
    for (String e : entities) {
      String lastVarGrounding =
          this.beamCyclic(firstVariable, e, 0, true, triples, new HashSet<String>());
      if (lastVarGrounding != null) {
        if (firstVariable.equals("X")) {
          groundings.addKey(e);
          if (groundings.addValue(lastVarGrounding)) repetitions = 0;
          else repetitions++;

          // if (!(groundings.addValue(lastVarGrounding))) repetitions++;
        } else {
          groundings.addKey(lastVarGrounding);
          if (groundings.addValue(e)) repetitions = 0;
          else repetitions++;

          // if (!(groundings.addValue(e))) repetitions++;
        }
      }
      if (Settings.BEAM_SAMPLING_MAX_REPETITIONS <= repetitions) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS <= groundings.size()) break;
    }
    groundings.setChaoEstimate(repetitions);
    return groundings;
  }

  // see http://www.vldb.org/conf/1995/P311.PDF

  public int getChaoEstimate(int f1, int f2, int d) {
    return (int) (d + ((double) (f1 * f1) / (double) (2.0 * f2)));
  }

  private SampledPairedResultSet beamBodyCyclicReverse(
      String firstVariable, String lastVariable, TripleSet triples) {
    SampledPairedResultSet groundings = new SampledPairedResultSet();
    Atom atom = this.body.getLast();
    boolean headNotTail = atom.getLeft().equals(lastVariable);
    Triple t;
    int attempts = 0;
    int repetitions = 0;
    while ((t = triples.getRandomTripleByRelation(atom.getRelation())) != null) {
      attempts++;
      String firstVarGrounding =
          this.beamCyclic(
              lastVariable,
              t.getValue(headNotTail),
              this.bodysize() - 1,
              false,
              triples,
              new HashSet<String>());
      // until here
      if (firstVarGrounding != null) {
        if (firstVariable.equals("X")) {
          groundings.addKey(firstVarGrounding);
          if (groundings.addValue(t.getValue(headNotTail))) repetitions = 0;
          else repetitions++;
        } else {
          groundings.addKey(t.getValue(headNotTail));
          if (groundings.addValue(firstVarGrounding)) repetitions = 0;
          else repetitions++;
        }
      }
      if (Settings.BEAM_SAMPLING_MAX_REPETITIONS <= repetitions) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS <= attempts) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS <= groundings.size()) break;
    }
    return groundings;
  }

  private SampledPairedResultSet beamBodyCyclicReverseEDIS(
      String firstVariable, String lastVariable, TripleSet triples) {
    SampledPairedResultSet groundings = new SampledPairedResultSet();
    Atom atom = this.body.getLast();
    boolean headNotTail = atom.getLeft().equals(lastVariable);
    int repetitions = 0;
    ArrayList<String> entities =
        triples.getNRandomEntitiesByRelation(
            atom.getRelation(), headNotTail, Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS);
    for (String e : entities) {
      // System.out.println("e="+ e);
      String firstVarGrounding =
          this.beamCyclic(
              lastVariable, e, this.bodysize() - 1, false, triples, new HashSet<String>());
      if (firstVarGrounding != null) {
        if (firstVariable.equals("X")) {
          groundings.addKey(firstVarGrounding);
          if (groundings.addValue(e)) repetitions = 0;
          else repetitions++;
        } else {
          groundings.addKey(e);
          if (groundings.addValue(firstVarGrounding)) repetitions = 0;
          else repetitions++;
        }
      }
      if (Settings.BEAM_SAMPLING_MAX_REPETITIONS <= repetitions) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS <= groundings.size()) break;
    }
    return groundings;
  }

  // (String currentVariable, String lastVariable, TripleSet triples,) {
  private HashSet<String> beamPGBodyCyclic(
      String firstVariable,
      String lastVariable,
      String value,
      int bodyIndex,
      boolean direction,
      TripleSet triples) {
    HashSet<String> groundings = new HashSet<String>();
    Atom atom = this.body.get(bodyIndex);
    boolean headNotTail = atom.getLeft().equals(firstVariable);
    int attempts = 0;
    int repetitions = 0;
    // System.out.println("startsFine: " + atom.getRelation() + " - " + value + " - " +
    // headNotTail);
    boolean startFine = !triples.getEntities(atom.getRelation(), value, headNotTail).isEmpty();
    // System.out.println("startsFine=" + startFine);
    while (startFine) {
      attempts++;
      String grounding =
          this.beamCyclic(
              firstVariable, value, bodyIndex, direction, triples, new HashSet<String>());
      if (grounding != null) {
        if (groundings.add(grounding)) repetitions = 0;
        else repetitions++;
      }
      if (Settings.BEAM_SAMPLING_MAX_REPETITIONS <= repetitions) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS <= attempts) break;
      if (Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS <= groundings.size()) break;
    }
    // System.out.println(this);
    // System.out.println("  => r=" + repetitions + " a=" + attempts + " g=" + groundings.size());
    // System.out.println(Settings.BEAM_SAMPLING_MAX_BODY_GROUNDING_ATTEMPTS);
    // System.out.println(Settings.BEAM_SAMPLING_MAX_BODY_GROUNDINGS);

    return groundings;
  }

  /**
   * Tries to create a random grounding for a partially grounded body.
   *
   * @param currentVariable The name of the current variable for which a value is given.
   * @param value The value of the current variable.
   * @param bodyIndex The index of the body atom that we are currently concerned with.
   * @param direction The direction to search. True means to search from first to last atom, false
   *     the opposite direction.
   * @param triples The data set used for grounding the body
   * @param previousValues The values that were used as groundings for variables visited already.
   * @return A grounding for the last variable (or constants). Null if not a full grounding of the
   *     body has been constructed.
   */
  protected String beamCyclic(
      String currentVariable,
      String value,
      int bodyIndex,
      boolean direction,
      TripleSet triples,
      HashSet<String> previousValues) {
    // System.out.println(currentVariable + ", " + value + ", " + bodyIndex +", " +direction + ", "
    // + previousValues.size());
    if (value == null) return null;
    // check if the value has been seen before as grounding of another variable
    Atom atom = this.body.get(bodyIndex);
    boolean headNotTail = atom.getLeft().equals(currentVariable);
    // OI-OFF
    if (previousValues.contains(value)) return null;
    // the current atom is the last
    if ((direction == true && this.body.size() - 1 == bodyIndex)
        || (direction == false && bodyIndex == 0)) {
      String finalValue = triples.getRandomEntity(atom.getRelation(), value, headNotTail);
      // System.out.println("Y = " + finalValue + " out of " +
      // triples.getEntities(atom.getRelation(), value, headNotTail).size());

      // OI-OFF
      if (previousValues.contains(finalValue)) return null;
      // OI-OFF
      if (value.equals(finalValue)) return null;
      return finalValue;
    }
    // the current atom is not the last
    else {
      String nextValue = triples.getRandomEntity(atom.getRelation(), value, headNotTail);
      String nextVariable = headNotTail ? atom.getRight() : atom.getLeft();
      // OI-OFF
      previousValues.add(value);
      int updatedBodyIndex = (direction) ? bodyIndex + 1 : bodyIndex - 1;
      return this.beamCyclic(
          nextVariable, nextValue, updatedBodyIndex, direction, triples, previousValues);
    }
  }

  public boolean isRefinable() {
    return true;
  }

  /*
  public double getAppliedConfidence() {
  	return (double)this.getCorrectlyPredicted() / ((double)this.getPredicted() + Math.pow(Settings.UNSEEN_NEGATIVE_EXAMPLES, this.bodysize()));
  }
  */

  public boolean isSingleton(TripleSet triples) {
    return false;
  }
}
