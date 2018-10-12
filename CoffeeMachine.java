//CoffeeMachine is used by Client threads attempting to access the Dispenser resource
//Threads are scheduled using monitor and while loops
//The program lives and dies with the dispWaitLogic class, edit at own risk
//Created by Adam Crocker for COMP2240
//Last Edited 12/10/2018
import java.util.ArrayList;
import java.util.List;

public class CoffeeMachine {

  private int mode; //0 = cold, 1 = hot
  private int time; //sim time
  private int longestBrewFinishTime = 0;  //longest brewing time of client for batch
  private int nextHotID = 1;  //used to order the client threads
  private int nextColdID = 1;
  private int hotRemain = 0;  //how many hot/cold clients are in the sim
  private int coldRemain = 0;
  private boolean init = false; //used to set mode triggered by first thread
  private boolean swapType = false; //stops one coffee type from dominating
  private int nextEvent = -1; //used to increment time discretely
  private Integer[] dispensers; //3 dispensers to be consumed
  private int nextBrewTime = -1;  //used to check if another coffee can brew before longestFinishTime
  private Boolean shortBrew = false;  //breaks client out of wait pool if they can brew
  private List<Integer> currentOutTimes;  //used to compare smallest time increment


  public CoffeeMachine(int i) {
    time = 0;
    dispensers = new Integer[i];
    currentOutTimes = new ArrayList<>();
    initArrays();
  }

  //Logic For Threads

  //threads come here first to check if they are the first thread
  public synchronized void checkInit(int type) {
    if (!init) {
      mode = type;
      init = true;
    }
  }

  //wait here if its not your turn, type, or dispensers are full or swapType is true
  public synchronized void enterWaitPool(int type, int id, int brewTime)
      throws InterruptedException {
    while (mode != type || id != getNextID(type) || activeDisp() == 3 || swapType) {
      //checks to see if other clients of same type can brew before current longest is done
      if (id == getNextID(type) && swapType && mode == type ) {
        //breaks out of loop if client can use machine
        if (shortBrew && activeDisp() != 3) {
          System.out.println(id+" broke out using shortBrew");
          break;
        }
        nextBrewTime = brewTime;
        notifyAll();
      }
      wait();
    }
  }

  public synchronized void updateMachine(int brewTime, int type, int id) {
    if (!swapType) {
      setLongestBrewFinishTime(brewTime);
    }
    if (activeDisp() == 3 || checkRemaining(type) == activeDisp()) {
      setSwapType(true);
      System.out.println("swapType = true");
    }
    nextID(type);
    notifyAll();
  }

  //true, must wait. false, continue
  public synchronized Boolean dispWaitLogic(int type,int id) {
    //final thread
    if(checkRemaining(type) == 1)
      return false;
    //+3 threads
    if(activeDisp() < 3 && checkRemaining(type) > 2 && !swapType)
      return true;
    //1 thread + typeChange + nextBrew is too long
    if(activeDisp() < 3 && swapType && !compareBrews(type))
      return false;
    //1 thread + typeChange + shortBrew
    if(activeDisp() < 3 && swapType && compareBrews(type))
      return true;
    //full dispensers
    if(activeDisp() == 3)
      return false;
    //System.out.println(id+" logic error");
    return false;
  }

  public synchronized void waitForDispensers(int type, int id) throws InterruptedException {
    wait(50); //magic wait for consistency, it just works
    while (nextBrewTime == -1 && swapType && !(checkRemaining(type) <= 3)) {
      System.out.println(id+ " waiting for nextBrewTime");
      notifyAll();
      wait();
    }
    //wait here for other dispensers to fill
    while (dispWaitLogic(type,id)) {
      if((swapType && compareBrews(type) && activeDisp() < 3)) {
        shortBrew = true;
        notifyAll();
      }
      System.out.println(id+ " waiting for other dispensers to fill");
      wait();
    }
  }

  //set time to shortest next event
  public synchronized void setOutTimes(int outTime) {
    currentOutTimes.add(outTime);
    //System.out.println("list size = " + currentOutTimes.size());
    notifyAll();
  }

  public synchronized void waitForSetTimes(int id) throws InterruptedException {
    while(currentOutTimes.size() < activeDisp() && !currentOutTimes.isEmpty()) {
      System.out.println(id+ " waiting for setTimes");
      wait();
    }
  }

  public synchronized void advanceTime() {
    for(Integer times : currentOutTimes) {
      if(times < nextEvent || nextEvent == -1)
        nextEvent = times;
    }
    if(time < nextEvent) {
      time = nextEvent;
      //System.out.println("Time = " +time);
    }
    notifyAll();
  }

  //need to figure out a way to signal other thread has finished
  public synchronized void waitForEnd(int id) throws InterruptedException {
    /*while(!threadFinished) {
      System.out.println(id+ " waiting for thread to die");
      wait();
    }*/
    wait(500);
  }

  public synchronized void coffeeFinished(int type,int id) {
    if(activeDisp() == 1 && swapType) {
      setMode(type);
      swapType = false;
    }
    deleteClient(type);
    returnDisp(id);
    currentOutTimes.clear();
    nextEvent = -1;
    shortBrew = false;
    System.out.println(id + " finished. Hot = "+hotRemain+" Cold = "+coldRemain);
    notifyAll();
  }


  public synchronized void setLongestBrewFinishTime(int brewTime) {
    if (brewTime + time > longestBrewFinishTime) {
      longestBrewFinishTime = brewTime + time;
    }
  }

  //compares the brewtime of new clients in swaptype to see if they can fit in before
  //the longest brew finishes
  public synchronized Boolean compareBrews(int type) {
    if(activeDisp() == checkRemaining(type))
      nextBrewTime = -1;
    if(nextBrewTime == -1)
      return false;
    else
      return nextBrewTime + time <= longestBrewFinishTime;
  }

  public synchronized void setSwapType(Boolean set) {
    swapType = set;
  }

  public int getMode() {
    return mode;
  }

  public void setMode(int type) {
    if (type == 0) {
      mode = 1;
    } else {
      mode = 0;
    }
  }

  public synchronized void nextID(int type) {
    if (type == 0) {
      nextColdID++;
    } else {
      nextHotID++;
    }
  }

  public int getNextID(int type) {
    if (type == 0) {
      return nextColdID;
    } else {
      return nextHotID;
    }
  }

  public int getTime() {
    return time;
  }

  //Dispenser logic
  public void initArrays() {
    for (int i = 0; i < dispensers.length; i++) {
      dispensers[i] = -1;
    }
  }

  public int activeDisp() {
    int count = 0;
    for (Integer i : dispensers) {
      if (i != -1) {
        count++;
      }
    }
    return count;
  }

  public synchronized int takeDisp(int id) {
    for (int i = 0; i < dispensers.length; i++) {
      if (dispensers[i] == -1) {
        dispensers[i] = id;
        if (activeDisp() == 3) {
          setSwapType(true);
        }
        notifyAll();
        return i + 1;
      }
    }
    return -1;
  }

  public synchronized void returnDisp(int id) {
    for (int i = 0; i < dispensers.length; i++) {
      if (dispensers[i] == id) {
        dispensers[i] = -1;
        break;
      }
    }
  }

  //Client count logic
  public int checkRemaining(int type) {
    if (type == 0) {
      return coldRemain;
    } else {
      return hotRemain;
    }
  }

  public void deleteClient(int type) {
    if (type == 0) {
      coldRemain--;
    } else {
      hotRemain--;
    }
  }

  public void addHot(int hotClients) {
    hotRemain = hotClients;
  }

  public void addCold(int coldClients) {
    coldRemain = coldClients;
  }
}
