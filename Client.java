public class Client implements Runnable {

  private int clientID;
  private int processID;
  private int type;   //0 = cold, 1 = hot
  private int brewTime;
  private int dispenser;
  private int timeOut;
  private CoffeeMachine machine;

  public Client(int processID, String type, int clientID, int brewTime, CoffeeMachine machine) {
    this.processID = processID;
    this.clientID = clientID;
    this.brewTime = brewTime;
    this.machine = machine;
    convertTypeString(type);
  }

  public void convertTypeString(String type) {
    if (type.equals("H")) {
      this.type = 1;
    } else {
      this.type = 0;
    }
  }

  //Getters

  public int getClientID() {
    return clientID;
  }

  public int getProcessID() {
    return processID;
  }

  public int getType() {
    return type;
  }

  public int getBrewTime() {
    return brewTime;
  }

  public String output(int disp) {
    String stype;
    if (type == 0) {
      stype = "C";
    } else {
      stype = "H";
    }
    return "(" + machine.getTime() + ") " + stype + clientID + " uses dispenser " +
        disp + " (time: " + brewTime + ")";
  }

  @Override
  public void run() {
    try {
      machine.checkInit(type);
      //wait for turn
      machine.enterWaitPool(type,clientID,brewTime);
      //get dispenser + output status
      dispenser = machine.takeDisp(clientID);
      System.out.println(output(dispenser));
      //update machine
      timeOut = machine.getTime() + brewTime;
      machine.updateMachine(brewTime,type,clientID);
      //the hard shit
      while(machine.getTime() < timeOut) {
        machine.waitForDispensers(type,clientID);
        machine.setOutTimes(timeOut);
        machine.waitForSetTimes(clientID);
        machine.advanceTime();
        if(machine.getTime() != timeOut)
          machine.waitForEnd(clientID);
      }
      //die
      machine.coffeeFinished(type,clientID);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
