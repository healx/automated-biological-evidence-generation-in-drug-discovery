package de.unima.ki.anyburl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts files in format 'subject object relation' to 'subject relation object'
 *
 * @author Christian
 */
public class DB500Rewriter {

  public static void main(String[] args) throws FileNotFoundException {

    String input = "data/DB500/train-origin.txt";
    String output = "data/DB500/train.txt";

    PrintWriter pw = new PrintWriter(output);

    Path file = (new File(input)).toPath();
    Charset charset = Charset.forName("UTF8");
    String line = null;
    long lineCounter = 0;
    try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
      while ((line = reader.readLine()) != null) {
        lineCounter++;
        if (lineCounter % 1000000 == 0) System.out.println("parsed " + lineCounter + " lines");
        String[] token = line.split("\t");
        if (token.length != 3) System.err.println("something's wrong with this line: " + line);
        pw.println(token[0] + "\t" + token[2] + "\t" + token[1]);
      }
    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
      System.err.format("Error occurred for line: " + line + " LINE END");
      System.exit(1);
    }
    System.out.println("done.");
  }
}
