package magpiebridge.finfer;

import com.ibm.wala.cast.tree.impl.AbstractSourcePosition;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

public class SourceCodePosition extends AbstractSourcePosition {

  private URL url;
  private int firstLine;
  private int firstCol;
  private int lastLine;
  private int lastCol;

  public SourceCodePosition(URL url, int firstLine, int firstCol, int lastLine, int lastCol) {
    this.url = url;
    this.firstLine = firstLine;
    this.firstCol = firstCol-1;
    this.lastLine = lastLine;
    this.lastCol = lastCol-1;
  }

  @Override
  public URL getURL() {
    return url;
  }

  @Override
  public Reader getReader() throws IOException {
    return null;
  }

  @Override
  public int getFirstLine() {
    return firstLine;
  }

  @Override
  public int getLastLine() {
    return lastLine;
  }

  @Override
  public int getFirstCol() {
    return firstCol;
  }

  @Override
  public int getLastCol() {
    return lastCol;
  }

  @Override
  public int getFirstOffset() {
    return 0;
  }

  @Override
  public int getLastOffset() {
    return 0;
  }
}
