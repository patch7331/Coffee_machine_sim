//CoffeeSim handles the creation of objects and threads. It assigns them data and
//starts the threads
//Created by Adam Crocker for COMP2240
//Last Edited 12/10/2018
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoffeeSim {

  private List<Client> cObjs; //used to call final time after threads have died
  private List<Thread> syncedClients;
  private CoffeeMachine machine;
  private int hotClients = 0;
  private int coldClients = 0;

  public CoffeeSim(ArrayDeque<String> data) {
    machine = new CoffeeMachine(3);
    cObjs = new ArrayList<>();
    syncedClients = Collections.synchronizedList(new ArrayList<>());
    addClients(data);
    checkTypes();
  }

  //Creates client objs and threads out of those objects
  public void addClients(ArrayDeque<String> data) {
    int clients = Integer.parseInt(data.poll());
    for (int i = 0; i < clients; i++) {
      Client nc = new Client(i, data.poll(), Integer.parseInt(data.poll()),
          Integer.parseInt(data.poll()),machine);
      Thread nt = new Thread(nc);
      cObjs.add(nc);
      syncedClients.add(nt);
    }
  }

  //counts how many hot/cold clients are in the simulation and sends them to CoffeeMachine
  public void checkTypes() {
    for(Client c : cObjs) {
      if(c.getType() == 0)
        coldClients++;
      else
        hotClients++;
    }
    machine.addHot(hotClients);
    machine.addCold(coldClients);
  }

  //Starts threads, waits for them to die then prints result
  public void runSim() throws InterruptedException {
    for(Thread c : syncedClients) {
      c.start();
    }
    for(Thread c : syncedClients) {
      c.join();
    }
    System.out.println("("+machine.getTime()+") DONE");
  }

}
