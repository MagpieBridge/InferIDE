package magpiebridge.finfer;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.util.collections.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import magpiebridge.core.AnalysisConsumer;
import magpiebridge.core.AnalysisResult;
import magpiebridge.core.Kind;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.core.analysis.configuration.ConfigurationAction;
import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;
import magpiebridge.projectservice.java.JavaProjectService;
import magpiebridge.projectservice.java.JavaProjectType;
import magpiebridge.util.SourceCodePositionFinder;
import org.eclipse.lsp4j.DiagnosticSeverity;

public class InferServerAnalysis implements ToolAnalysis {
  private String rootPath;
  private String reportPath;
  private String projectType;
  private boolean firstTime = true;
  private static boolean showTrace = false;
  private String command;

  public InferServerAnalysis() {
    super();
    this.firstTime = true;
    this.command = "infer run --reactive --";
  }

  @Override
  public String source() {
    return "infer";
  }

  @Override
  public void analyze(Collection<? extends Module> files, AnalysisConsumer consumer, boolean rerun) {
    if (consumer instanceof MagpieServer) {
      MagpieServer server = (MagpieServer) consumer;
      if (this.rootPath == null) {
        JavaProjectService ps = (JavaProjectService) server.getProjectService("java").get();
        if (ps.getRootPath().isPresent()) {
          this.rootPath = ps.getRootPath().get().toString();
          this.projectType = ps.getProjectType();
          this.reportPath = this.rootPath + File.separator + "infer-out" + File.separator + "report.json";
        }
      }
      if (rerun && this.rootPath != null) {
        server.submittNewTask(() -> {
          try {
            File report = new File(InferServerAnalysis.this.reportPath);
            if (report.exists())
              report.delete();
            Process runInfer = this.runCommand(new File(InferServerAnalysis.this.rootPath));
            if (runInfer.waitFor() == 0) {
              File file = new File(InferServerAnalysis.this.reportPath);
              if (file.exists()) {
                Collection<AnalysisResult> results = convertToolOutput();
                server.consume(results, source());
              }
            }
          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
          }
        });
      }
    }
  }

  private String getToolBuildCmdWithClean() {
    if (this.projectType.equals(JavaProjectType.Maven.toString()))
      return "mvn clean compile";
    if (this.projectType.equals(JavaProjectType.Gradle.toString()))
      return "";
    return "";
  }

  private String getToolBuildCmd() {
    if (this.projectType.equals(JavaProjectType.Maven.toString()))
      return "mvn compile";
    if (this.projectType.equals(JavaProjectType.Gradle.toString()))
      return "";
    return "";
  }

  @Override
  public String[] getCommand() {
    String cmd = "infer run --reactive -- ";
    if (firstTime) {
      firstTime = false;
      cmd = cmd + getToolBuildCmdWithClean();
    } else
      cmd = cmd + getToolBuildCmd();
    return cmd.split(" ");
  }

  @Override
  public Collection<AnalysisResult> convertToolOutput() {
    Collection<AnalysisResult> res = new ArrayList<AnalysisResult>();
    try {
      JsonParser parser = new JsonParser();
      JsonArray bugs = parser.parse(new FileReader(new File(this.reportPath))).getAsJsonArray();
      for (int i = 0; i < bugs.size(); i++) {
        JsonObject bug = bugs.get(i).getAsJsonObject();
        String bugType = bug.get("bug_type").getAsString();
        String qualifier = bug.get("qualifier").getAsString();
        int line = bug.get("line").getAsInt();
        String file = this.rootPath + File.separator + bug.get("file").getAsString();
        Position pos = SourceCodePositionFinder.findCode(new File(file), line).toPosition();
        String msg = bugType + ": " + qualifier;
        JsonArray trace = bug.get("bug_trace").getAsJsonArray();
        ArrayList<Pair<Position, String>> traceList = new ArrayList<Pair<Position, String>>();
        if (showTrace) {
          for (int j = 0; j < trace.size(); j++) {
            JsonObject step = trace.get(j).getAsJsonObject();
            String stepFile = this.rootPath + File.separator + step.get("filename").getAsString();
            if (new File(stepFile).exists()) {
              int stepLine = step.get("line_number").getAsInt();
              String stepDescription = step.get("description").getAsString();
              Position stepPos = SourceCodePositionFinder.findCode(new File(stepFile), stepLine).toPosition();
              Pair<Position, String> pair = Pair.make(stepPos, stepDescription);
              traceList.add(pair);
            }
          }
        }
        AnalysisResult rbug = new InferResult(Kind.Diagnostic, pos, msg, traceList, DiagnosticSeverity.Error, null, null);
        res.add(rbug);
      }
    } catch (JsonIOException e) {
      e.printStackTrace();
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return res;
  }

  @Override
  public List<ConfigurationOption> getConfigurationOptions() {
    List<ConfigurationOption> commands = new ArrayList<>();
    ConfigurationOption command1 = new ConfigurationOption("run command: infer run", OptionType.checkbox);
    ConfigurationOption command2
        = new ConfigurationOption( "run command: infer run --reactive", OptionType.checkbox);
    ConfigurationOption command3 = new ConfigurationOption("run command: ", OptionType.text);
    commands.add(command1);
    commands.add(command2);
    commands.add(command3);
    return commands;
  }

  @Override
  public List<ConfigurationAction> getConfiguredActions() {
    return Collections.emptyList();
  }

  @Override
  public void configure(List<ConfigurationOption> configuration) {
    for (ConfigurationOption o : configuration)
      if (o.getValueAsBoolean())
        command = o.getName().split(": ")[1];
  }
}
