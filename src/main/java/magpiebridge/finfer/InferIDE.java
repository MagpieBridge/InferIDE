package magpiebridge.finfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.ServerConfiguration;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.core.analysis.configuration.ConfigurationOption;
import magpiebridge.core.analysis.configuration.OptionType;
import magpiebridge.projectservice.java.JavaProjectService;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class InferIDE {
  public static void main(String... args) throws IOException, InterruptedException {
    Supplier<MagpieServer> createServer = () -> {
      ServerConfiguration config = new ServerConfiguration();
      config.setDoAnalysisByOpen(false);
      //config.setDoAnalysisByIdle(true, 5 * 60 * 1000);
      config.setReportFalsePositive(true);
      config.setReportConfusion(true);
      config.setShowConfigurationPage(true);
      MagpieServer server = new MagpieServer(config);
      String language = "java";
      IProjectService javaProjectService = new JavaProjectService();
      server.addProjectService(language, javaProjectService);
      ToolAnalysis analysis = new InferServerAnalysis();
      Either<ServerAnalysis, ToolAnalysis> either = Either.forRight(analysis);
      server.addAnalysis(either, language);

      ToolAnalysis analysis2 = new InferServerAnalysis() {
        @Override
        public String source() {
          return "infer2";
        }
        @Override
        public List<ConfigurationOption> getConfigurationOptions() {
          List<ConfigurationOption> commands = new ArrayList<>();
          ConfigurationOption command1 =
              new ConfigurationOption("run command: infer capture", OptionType.checkbox);
          ConfigurationOption command2 =
              new ConfigurationOption("run command: infer capture --reactive", OptionType.checkbox);
          ConfigurationOption command3 = new ConfigurationOption("run command: ", OptionType.text);
          commands.add(command1);
          commands.add(command2);
          commands.add(command3);
          return commands;
        }
      };
      Either<ServerAnalysis, ToolAnalysis> either2 = Either.forRight(analysis2);
      server.addAnalysis(either2, "javascript");
      return server;
    };
    // createServer.get().launchOnStdio();
    MagpieServer.launchOnSocketPort(5007, createServer);
  }
}
