package magpiebridge.finfer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.wala.classLoader.Module;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;

import magpiebridge.core.AnalysisResult;
import magpiebridge.core.Kind;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;
import magpiebridge.util.SourceCodeInfo;
import magpiebridge.util.SourceCodePositionFinder;

public class InferServerAnalysis implements ServerAnalysis {
  private String rootPath;

  @Override
  public String source() {
    return "infer";
  }

  @Override
  public void analyze(Collection<? extends Module> files, MagpieServer server, boolean rerun) {
    JavaProjectService ps = (JavaProjectService) server.getProjectService("java").get();
    if (ps.getRootPath().isPresent()) {
      this.rootPath = ps.getRootPath().get().toString();
      Collection<AnalysisResult> results = read(new File("report.json"));
      server.consume(results, source());
    }
  }

  public Collection<AnalysisResult> read(File report) {
    Collection<AnalysisResult> res = new ArrayList<AnalysisResult>();

    try {
      JsonParser parser = new JsonParser();
      JsonArray bugs =parser.parse(new FileReader(report)).getAsJsonArray();
      for (int i = 0; i < bugs.size(); i++) {
        JsonObject bug = bugs.get(i).getAsJsonObject();
        String bugType = bug.get("bug_type").getAsString();
        String qualifier = bug.get("qualifier").getAsString();
        int line = bug.get("line").getAsInt();
        String file = this.rootPath + File.separator + bug.get("file").getAsString();
        SourceCodeInfo info = SourceCodePositionFinder.findCode(new File(file), line);
        URL url = new URL("file://"+ file);
        SourceCodePosition pos = new SourceCodePosition(url, line, info.range.getStart().getCharacter(), line,
            info.range.getEnd().getCharacter());
        String msg = bugType + ": " + qualifier;
        AnalysisResult rbug
            = new InferResult(Kind.Diagnostic, pos, msg, new ArrayList<>(), DiagnosticSeverity.Error, null, null);
        res.add(rbug);
      }
    } catch (JsonIOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (JsonSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return res;
  }

}
