import java.util.concurrent.Semaphore;

public class CoffeeMachine extends Semaphore {

  private int mode; //0 = cold, 1 = hot
  private int heads;
  private int time;
  private int longestBrewFinishTime;
  private int prevColdID;
  private int prevHotID;
  private boolean init = false; //used to set mode triggered by first thread
  private boolean swapType = false; //stops one coffee type from dominating

  public CoffeeMachine(int i, boolean b) {
    super(i, b);
    heads = i;
    time = 0;
  }

  public void getCoffee(int type, int id, int brew) throws InterruptedException {
    synchronized (this) {
      if(!init) {
        mode = type;
        init = true;
      }
    }
    if(type == 0)
      getCold(id,brew);
    else
      getHot(id,brew);
  }

  //Requirements to acquire:
  //Machine must be in correct mode
  //Customer is next in line
  //if swaptype is true, can only acquire if brew will finish before or
  //at same time as longestBrew
  public void getHot(int id, int brew) throws InterruptedException {
    while(mode == 0 || id != prevHotID+1 || (swapType && !compareBrews(brew))) {
      wait();
    }
    acquire();
    //critical

  }

  public void getCold(int id, int brew) throws InterruptedException {
    while(mode == 1 || id != prevColdID+1) {
      wait();
    }
  }

  public synchronized void setLongestBrewFinishTime(int brewTime) {
    if(brewTime + time > longestBrewFinishTime)
      longestBrewFinishTime = brewTime + time;
  }

  public synchronized Boolean compareBrews(int brewTime) {
    return brewTime + time <= longestBrewFinishTime;
  }

  public synchronized void setSwapType(Boolean x) {
    swapType = x;
  }

  public synchronized void nextClient(int type) {
    if(type == 0)
      prevColdID++;
    else
      prevHotID++;
  }

  public int getTime() {
    return time;
  }

}
