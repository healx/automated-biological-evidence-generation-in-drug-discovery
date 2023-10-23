package de.unima.ki.anyburl.exceptions;

public class Timer {

  private long attempts = 0;
  private boolean done = false;
  private long maxTime = 0;
  private long interval = 0;

  public Timer() {
    this.interval = 1000;
    // long currentTime = System.currentTimeMillis();
    // this.maxTime = currentTime + Settings.RULE_SCORING_TIME;
  }

  public boolean timeOut() {
    return false;
    /*
    if (this.done) return true;
    this.attempts++;
    if (this.attempts % this.interval == 0) {
    	long now = System.currentTimeMillis();
    	if (now > this.maxTime) {
    		this.done = true;
    		return true;
    	}
    }
    return false;
    */
  }
}
