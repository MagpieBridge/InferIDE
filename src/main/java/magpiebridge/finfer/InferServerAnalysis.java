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
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;

public class InferServerAnalysis implements ToolAnalysis {
  private String rootPath;
  private String reportPath;
  private String projectType;
  private boolean firstTime;
  private static boolean showTrace = false;
  private String userDefinedCommand;
  private boolean useDefaultCommand;
  private String defaultCommand;

  public InferServerAnalysis() {
    this.firstTime = true;
    this.useDefaultCommand = true;
  }

  private String getDefaultCommand() {
    String command = "infer run --reactive -- ";
    String buildCmd = null;
    if (firstTime) {
      firstTime = false;
      buildCmd = getToolBuildCmdWithClean();
    } else {
      buildCmd = getToolBuildCmd();
    }
    if (buildCmd == null)
      command = "infer run";
    else {
      command = command + buildCmd;
    }
    return command;
  }

  @Override
  public String source() {
    return "infer";
  }

  @Override
  public void analyze(Collection<? extends Module> files, AnalysisConsumer consumer,
      boolean rerun) {
    if (consumer instanceof MagpieServer) {
      MagpieServer server = (MagpieServer) consumer;
      if (this.rootPath == null) {
        JavaProjectService ps = (JavaProjectService) server.getProjectService("java").get();
        if (ps.getRootPath().isPresent()) {
          this.rootPath = ps.getRootPath().get().toString();
          this.reportPath =
              this.rootPath + File.separator + "infer-out" + File.separator + "report.json";
          this.projectType = ps.getProjectType();
          this.defaultCommand = getDefaultCommand();
        }
      }
      if (rerun && this.rootPath != null) {
        server.submittNewTask(() -> {
          try {
            File report = new File(InferServerAnalysis.this.reportPath);
            if (report.exists())
              report.delete();
            server.forwardMessageToClient(new MessageParams(MessageType.Info,
                "Running command: " + (useDefaultCommand ? defaultCommand : userDefinedCommand)));
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
    if (JavaProjectType.Maven.toString().equals(this.projectType))
      return "mvn clean compile";
    if (JavaProjectType.Gradle.toString().equals(this.projectType))
      return "./gradlew clean build";
    return null;
  }

  private String getToolBuildCmd() {
    if (this.projectType.equals(JavaProjectType.Maven.toString()))
      return "mvn compile";
    if (this.projectType.equals(JavaProjectType.Gradle.toString()))
      return "./gradlew build";
    return null;
  }

  @Override
  public String[] getCommand() {
    if (!useDefaultCommand)
      return userDefinedCommand.split(" ");
    else {
      return defaultCommand.split(" ");
    }
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
              Position stepPos =
                  SourceCodePositionFinder.findCode(new File(stepFile), stepLine).toPosition();
              Pair<Position, String> pair = Pair.make(stepPos, stepDescription);
              traceList.add(pair);
            }
          }
        }
        AnalysisResult rbug = new InferResult(Kind.Diagnostic, pos, msg, traceList,
            DiagnosticSeverity.Error, null, null);
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
    ConfigurationOption defaultCommand =
        new ConfigurationOption("run default command", OptionType.checkbox, "true");
    ConfigurationOption command = new ConfigurationOption("run command: ", OptionType.text);
    commands.add(defaultCommand);
    commands.add(command);
    return commands;
  }

  @Override
  public List<ConfigurationAction> getConfiguredActions() {
    return Collections.emptyList();
  }

  @Override
  public void configure(List<ConfigurationOption> configuration) {
    for (ConfigurationOption o : configuration) {
      if (o.getType().equals(OptionType.checkbox)) {
        this.useDefaultCommand = o.getValueAsBoolean();
      } else if (o.getType().equals(OptionType.text) && o.getValue() != null) {
        this.userDefinedCommand = o.getValue();
        this.useDefaultCommand = false;
      }
    }
  }
}
