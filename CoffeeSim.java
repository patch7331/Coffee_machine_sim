import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class CoffeeSim {

  private List<Client> cObjs;

  public CoffeeSim(ArrayDeque<String> data) {
    cObjs = new ArrayList<>();
    addClients(data);
  }

  public void addClients(ArrayDeque<String> data) {
    int clients = Integer.parseInt(data.poll());
    for (int i = 0; i < clients; i++) {
      Client nc = new Client(i, data.poll(), Integer.parseInt(data.poll()),
          Integer.parseInt(data.poll()));
      cObjs.add(nc);
    }
  }
  
}
