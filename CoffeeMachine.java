//CoffeeMachine is used by Client threads attempting to access the Dispenser resource
//Threads are scheduled using monitor and while loops
//The program lives and dies with the dispWaitLogic class, edit at own risk
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
          //System.out.println(id+" broke out using shortBrew");
          break;
        }
        nextBrewTime = brewTime;
        notifyAll();
      }
      wait();
    }
  }

  //updates machine with client info after they consume a dispenser
  public synchronized void updateMachine(int brewTime, int type, int id) {
    if (!swapType) {
      setLongestBrewFinishTime(brewTime);
    }
    //once all 3 dispenser are taken or no more of type remain, prepare to swap types
    if (activeDisp() == 3 || checkRemaining(type) == activeDisp()) {
      setSwapType(true);
      //System.out.println("swapType = true");
    }
    nextID(type);
    notifyAll();
  }

  //true = wait in dispenser loop. false = continue
  public synchronized Boolean dispWaitLogic(int type,int id) {
    //final thread
    if(checkRemaining(type) == 1)
      return false;
    //common, many threads
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

  //threads wait here for dispensers to be consumed before continuing
  public synchronized void waitForDispensers(int type, int id) throws InterruptedException {
    wait(50); //magic wait for consistency, it just works
    while (nextBrewTime == -1 && swapType && !(checkRemaining(type) <= 3)) {
      //System.out.println(id+ " waiting for nextBrewTime");
      notifyAll();
      wait();
    }
    //wait here for other dispensers to fill
    while (dispWaitLogic(type,id)) {
      if((swapType && compareBrews(type) && activeDisp() < 3)) {
        shortBrew = true;
        notifyAll();
      }
      //System.out.println(id+ " waiting for other dispensers to fill");
      wait();
    }
  }

  //add thread finish times to list to compare in advanceTime
  public synchronized void setOutTimes(int outTime) {
    currentOutTimes.add(outTime);
    //System.out.println("list size = " + currentOutTimes.size());
    notifyAll();
  }

  //threads wait for everyone to add to the time list
  public synchronized void waitForSetTimes(int id) throws InterruptedException {
    while(currentOutTimes.size() < activeDisp() && !currentOutTimes.isEmpty()) {
      //System.out.println(id+ " waiting for setTimes");
      wait();
    }
  }

  //increment time to the lowest number next event
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

  //Could not figure out an efficient thread safe way to wait for thread to die
  //without the looping threads destroying everything.
  public synchronized void waitForEnd() throws InterruptedException {
    wait(500);
  }

  //Thread has reached the end, updates machine, returns dispenser
  public synchronized void coffeeFinished(int type,int id) {
    //if final thread at the end of a swap cycle, change mode for other types
    if(activeDisp() == 1 && swapType) {
      setMode(type);
      swapType = false;
    }
    deleteClient(type);
    returnDisp(id);
    currentOutTimes.clear();
    nextEvent = -1;
    shortBrew = false;
    //System.out.println(id + " finished. Hot = "+hotRemain+" Cold = "+coldRemain);
    notifyAll();
  }

  //checks if new brew will be the longest brew for that cycle
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

  //sets swap type, not sure why i made this a method
  public synchronized void setSwapType(Boolean set) {
    swapType = set;
  }

  //changes between hot and cold modes
  public void setMode(int type) {
    if (type == 0) {
      mode = 1;
    } else {
      mode = 0;
    }
  }

  //increments id so the next client can exit the wait pool
  public synchronized void nextID(int type) {
    if (type == 0) {
      nextColdID++;
    } else {
      nextHotID++;
    }
  }

  //returns id of next client out of the wait pool
  public int getNextID(int type) {
    if (type == 0) {
      return nextColdID;
    } else {
      return nextHotID;
    }
  }

  //returns simulation time
  public int getTime() {
    return time;
  }

  //sets up dispenser array, -1 = not in use
  public void initArrays() {
    for (int i = 0; i < dispensers.length; i++) {
      dispensers[i] = -1;
    }
  }

  //returns consumed dispensers
  public int activeDisp() {
    int count = 0;
    for (Integer i : dispensers) {
      if (i != -1) {
        count++;
      }
    }
    return count;
  }

  //searchs for a -1 cell and overwrites it with clientID
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

  //return clients still in simulation
  public int checkRemaining(int type) {
    if (type == 0) {
      return coldRemain;
    } else {
      return hotRemain;
    }
  }

  //removes client from count
  public void deleteClient(int type) {
    if (type == 0) {
      coldRemain--;
    } else {
      hotRemain--;
    }
  }

  //set amount of hot type clients
  public void addHot(int hotClients) {
    hotRemain = hotClients;
  }

  //set amount of cold type clients
  public void addCold(int coldClients) {
    coldRemain = coldClients;
  }
}
