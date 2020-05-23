package magpiebridge.finfer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StreamGobbler extends Thread {
  private InputStream in;
  private List<String> output = new ArrayList<>();
  private Consumer<IOException> errorHandler;

  public StreamGobbler(InputStream in, Consumer<IOException> errorHandler) {
    this.in = in;
    this.errorHandler = errorHandler;
  }

  @Override
  public void run() {
    try {
      InputStreamReader isr = new InputStreamReader(in);
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null) {
        output.add(line);
      }
    } catch (IOException e) {
      errorHandler.accept(e);
    }
  }

  public List<String> getOutput() {
    return output;
  }
}
