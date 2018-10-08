import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayDeque;
import java.util.Arrays;

public class C3192396A2P3 {

  public static void main(String[] args) {
    if(args.length == 1) {
      ArrayDeque<String> data = read(args);
      for(String s : data) {
        System.out.println(s);
      }
    }
    else
      System.out.println("Argument error");
  }

  //Turns file data into a manageable queue on integers
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
    }
    catch (Exception e) {
      System.err.format("Exception occurred trying to read '%s'.", args[0]);
      e.printStackTrace();
      return null;
    }
  }

}
