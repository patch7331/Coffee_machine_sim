public class Client implements Runnable {

  private int clientID;
  private int processID;
  private int type;   //0 = cold, 1 = hot
  private int brewTime;

  public Client(int processID, String type, int clientID, int brewTime) {
    this.processID = processID;
    this.clientID = clientID;
    this.brewTime = brewTime;
    convertTypeString(type);
  }

  public void convertTypeString(String type) {
    if(type.equals("H"))
      this.type = 1;
    else
      this.type = 0;
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

  @Override
  public void run() {

  }
}
