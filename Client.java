//Client objects are threads compete for resources in CoffeeMachine
//clients are hot or cold, they have a set amount of time they spend brewing
//Created by Adam Crocker for COMP2240
//Last Edited 12/10/2018
public class Client implements Runnable {

  private int clientID; //used for ordering threads and output
  private int processID;  //unused, numbering of threads as they were created in CoffeeSim
  private int type;   //0 = cold, 1 = hot
  private int brewTime; //how long a client consumes a dispenser
  private int dispenser;  //used for output
  private int timeOut;  //used for output
  private CoffeeMachine machine;

  public Client(int processID, String type, int clientID, int brewTime, CoffeeMachine machine) {
    this.processID = processID;
    this.clientID = clientID;
    this.brewTime = brewTime;
    this.machine = machine;
    convertTypeString(type);
  }

  //Converts stinky string data into delicious int
  public void convertTypeString(String type) {
    if (type.equals("H")) {
      this.type = 1;
    } else {
      this.type = 0;
    }
  }

  //Getters

  //used to count how many clients of a type were created
  public int getType() {
    return type;
  }

  //output information to terminal
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
      //loop until brewing ends
      while(machine.getTime() < timeOut) {
        machine.waitForDispensers(type,clientID);
        machine.setOutTimes(timeOut);
        machine.waitForSetTimes(clientID);
        machine.advanceTime();
        if(machine.getTime() != timeOut)
          machine.waitForEnd();
      }
      //thread prepares to die
      machine.coffeeFinished(type,clientID);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
