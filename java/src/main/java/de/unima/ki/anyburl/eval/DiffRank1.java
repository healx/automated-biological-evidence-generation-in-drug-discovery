package de.unima.ki.anyburl.eval;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;

public class DiffRank1 {

  public static void main(String[] args) {

    ResultSet rs1 = new ResultSet("tree", "exp/beam3/fb237-pred-both-tscored", true, 10);
    ResultSet rs2 = new ResultSet("beam", "exp/beam3/fb237-pred-both-bscored", true, 10);

    TripleSet triples = new TripleSet("data/FB15-237/test.txt");

    // GoldStandard goldSymmetry    = gold.getSubset("Subsumption");

    int deltaPosTail = 0;
    int deltaNegTail = 0;

    int deltaPosHead = 0;
    int deltaNegHead = 0;

    double distance = -1.0;

    for (Triple t : triples.getTriples()) {
      String triple = t.toString();
      String[] tt = triple.split(" ");
      // Triple t = new Triple(tt[0], tt[1], tt[2]);
      String proposedHead1 =
          (rs1.getHeadCandidates(triple).size() > 0) ? rs1.getHeadCandidates(triple).get(0) : "-";
      String proposedHead2 =
          (rs2.getHeadCandidates(triple).size() > 0) ? rs2.getHeadCandidates(triple).get(0) : "-";
      if (!proposedHead2.equals("-")
          && !proposedHead1.equals("-")
          && !proposedHead1.equals(proposedHead2)) {

        if (!proposedHead1.equals(t.getHead()) && proposedHead2.equals(t.getHead())) {
          // if (rs2.getHeadConfidences(triple).get(0) -  rs1.getHeadConfidences(triple).get(0) >
          // distance) {

          System.out.println(triple);
          System.out.println(
              ">>> head 1: " + proposedHead1 + ", " + (proposedHead1.equals(t.getHead())));
          System.out.println(
              ">>> head 2: " + proposedHead2 + ", " + (proposedHead2.equals(t.getHead())));
          deltaPosHead++;
          System.out.println(
              rs2.getName()
                  + ": "
                  + rs2.getHeadConfidences(triple).get(0)
                  + " <- "
                  + rs1.getName()
                  + ": "
                  + rs1.getHeadConfidences(triple).get(0)
                  + "\n");
          // }

        }

        if (proposedHead1.equals(t.getHead()) && !proposedHead2.equals(t.getHead())) {
          // change to the worse
          // if (rs2.getHeadConfidences(triple).get(0) -  rs1.getHeadConfidences(triple).get(0) >
          // distance) {
          System.out.println(triple);
          System.out.println(
              ">>> head 1: " + proposedHead1 + ", " + (proposedHead1.equals(t.getHead())));
          System.out.println(
              ">>> head 2: " + proposedHead2 + ", " + (proposedHead2.equals(t.getHead())));
          System.out.println(
              "    position of hit = "
                  + rs2.getHeadCandidates(triple).indexOf(t.getHead())
                  + " size = "
                  + rs2.getHeadCandidates(triple).size());
          deltaNegHead++;
          System.out.println(
              rs2.getName()
                  + ": "
                  + rs2.getHeadConfidences(triple).get(0)
                  + " <- "
                  + rs1.getName()
                  + ": "
                  + rs1.getHeadConfidences(triple).get(0)
                  + "\n");

          // }
        }
      }
      String proposedTail1 =
          (rs1.getTailCandidates(triple).size() > 0) ? rs1.getTailCandidates(triple).get(0) : "-";
      String proposedTail2 =
          (rs2.getTailCandidates(triple).size() > 0) ? rs2.getTailCandidates(triple).get(0) : "-";
      if (!proposedTail2.equals("-")
          && !proposedTail1.equals("-")
          && !proposedTail1.equals(proposedTail2)) {

        if (!proposedTail1.equals(t.getTail()) && proposedTail2.equals(t.getTail())) {
          // if (rs2.getTailConfidences(triple).get(0) -  rs1.getTailConfidences(triple).get(0) >
          // distance) {
          System.out.println(triple);
          System.out.println(
              ">>> tail 1: " + proposedTail1 + ", " + (proposedTail1.equals(t.getTail())));
          System.out.println(
              ">>> tail 2: " + proposedTail2 + ", " + (proposedTail2.equals(t.getTail())));
          deltaPosTail++;
          System.out.println(
              rs2.getTailConfidences(triple).get(0)
                  + " <- "
                  + rs1.getTailConfidences(triple).get(0)
                  + "\n");
          // }
        }

        if (proposedTail1.equals(t.getTail()) && !proposedTail2.equals(t.getTail())) {
          // if (rs2.getTailConfidences(triple).get(0) -  rs1.getTailConfidences(triple).get(0) >
          // distance) {
          System.out.println(triple);
          System.out.println(
              ">>> tail 1: " + proposedTail1 + ", " + (proposedTail1.equals(t.getTail())));
          System.out.println(
              ">>> tail 2: " + proposedTail2 + ", " + (proposedTail2.equals(t.getTail())));
          System.out.println(
              "    position of hit = "
                  + rs2.getTailCandidates(triple).indexOf(t.getTail())
                  + " size = "
                  + rs2.getTailCandidates(triple).size());
          deltaNegTail++;
          System.out.println(
              rs2.getTailConfidences(triple).get(0)
                  + " <- "
                  + rs1.getTailConfidences(triple).get(0)
                  + "\n");
          // }
        }
      }
    }
    System.out.println("Delta Head : pos=" + deltaPosHead + " neg=" + deltaNegHead);
    System.out.println("Delta Tail : pos=" + deltaPosTail + " neg=" + deltaNegTail);
    System.out.println(
        "Delta All  : pos="
            + (deltaPosHead + deltaPosTail)
            + " neg="
            + (deltaNegHead + deltaNegTail));
    System.out.println(
        "threshold of "
            + distance
            + " = "
            + (100.0
                * (((deltaPosHead + deltaPosTail) - (deltaNegHead + deltaNegTail))
                    / ((double) triples.getTriples().size() * 2.0)))
            + "%");
  }
}
