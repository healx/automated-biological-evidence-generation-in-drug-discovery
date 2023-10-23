package de.unima.ki.anyburl.io;

public class Test {

  public static void main(String[] args) {

    String alpha = "akjhksdjf,hfdj,kjads;kjsdfhfh;ksdfkhs,sdf";

    String[] token = alpha.split(",|;");
    for (String t : token) {
      System.out.println(t);
    }
  }
}
