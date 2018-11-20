//This program simulates a Coffee Machine with 3 dispensers, the machine can do hot or
//cold but only one mode at once. Threads are used to simulate clients using the
//machine for different amounts of time
//Input data is taken from a file, output to terminal
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Arrays;

public class A2P3 {

  public static void main(String[] args) throws InterruptedException {
    if (args.length == 1) {
      ArrayDeque<String> data = read(args);
      CoffeeSim sim = new CoffeeSim(data);
      sim.runSim();
    } else {
      System.out.println("Argument error");
    }
  }

  //Turns file data into a manageable queue of tokens
  public static ArrayDeque<String> read(String[] args) {
    ArrayDeque<String> clientData = new ArrayDeque<>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(args[0]));
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] words = line.split("((?<=H)|(?=H))|((?<=C)|(?=C))|\\s");
        clientData.addAll(Arrays.asList(words));
      }
      reader.close();
      return clientData;
    } catch (Exception e) {
      System.err.format("Exception occurred trying to read '%s'.", args[0]);
      e.printStackTrace();
      return null;
    }
  }

}
