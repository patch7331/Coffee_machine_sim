public class Client {

  private int clientID;
  private int processID;
  private String type;
  private int brewTime;

  public Client(int processID, String type, int clientID, int brewTime) {
    this.processID = processID;
    this.clientID = clientID;
    this.brewTime = brewTime;
    this.type = type;
  }

  //Getters

  public int getClientID() {
    return clientID;
  }

  public int getProcessID() {
    return processID;
  }

  public String getType() {
    return type;
  }

  public int getBrewTime() {
    return brewTime;
  }
}
