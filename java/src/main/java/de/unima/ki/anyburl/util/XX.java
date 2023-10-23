package de.unima.ki.anyburl.util;

import de.unima.ki.anyburl.data.Triple;
import de.unima.ki.anyburl.data.TripleSet;
import java.io.IOException;

public class XX {

  /**
   * Can be deleted after problem has been fixed.
   *
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {

    TripleSet ts = new TripleSet("data/FB15-237/test.txt");
    System.out.println(ts.size());

    int i = 0;
    int j = 0;
    for (Triple t : ts.getTriples()) {

      // if (t.getRelation().equals("/location/us_county/county_seat")) {
      j++;
      if (t.getHead().equals(t.getTail())) {
        i++;
        System.out.println(t);
      }
      // }
      // String entity = "/m/0mnwd";
      // if (t.getHead().equals(entity) || t.getTail().equals(entity)) {
      //	System.out.println(t);
      // }

    }

    System.out.println(i);
    System.out.println((100.0 * (double) i / (double) j) + "%");
  }
}
