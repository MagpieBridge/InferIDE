package magpiebridge.finfer;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.function.Supplier;
import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.ServerConfiguration;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Main class of the InferIDE language server.
 *
 * @author Linghui Luo
 */
public class InferIDE {

  private static final int DEFAULT_TIMEOUT = 5;
  private static final String DEFAULT_DOCKER_IMAGE = "jonasmanuel/inferdocker";
  private static final String DEFAULT_PORT = "5007";

  private static CommandLine cmd = null;

  public static void main(String... args) throws IOException, InterruptedException {
    Options cliOptions =
        new Options()
            .addOption(
                "a",
                "auto",
                true,
                MessageFormat.format(
                    "enables auto mode and sets the timeout in minutes for starting the automatic tool analysis\n default timeout is {0}",
                    DEFAULT_TIMEOUT))
            .addOption(
                "i",
                "dockerImage",
                true,
                MessageFormat.format(
                    "dockerImage to use when running infer in docker\ndefault is `{0}`",
                    DEFAULT_DOCKER_IMAGE))
            .addOption(
                "s",
                "socket",
                false,
                MessageFormat.format("run in socket mode, standard port is {0}", DEFAULT_PORT))
            .addOption(
                "p",
                "port",
                true,
                MessageFormat.format(
                    "sets the port for socket mode, standard port is {0}", DEFAULT_PORT));

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();

    try {
      cmd = parser.parse(cliOptions, args);
    } catch (ParseException e) {
      formatter.printHelp("inferIDE", cliOptions, true);

      System.exit(1);
    }
    Supplier<MagpieServer> createServer =
        () -> {
          boolean auto = false;
          int timeout = 5;
          if (cmd.hasOption("auto")) {
            auto = true;
            try {
              timeout = Integer.parseInt(cmd.getOptionValue("auto"));
            } catch (NumberFormatException e) {
              formatter.printHelp("inferIDE", cliOptions, true);
              System.exit(1);
            }
          }
          String dockerImage = cmd.getOptionValue("dockerImage", DEFAULT_DOCKER_IMAGE);

          ServerConfiguration config = new ServerConfiguration();
          config.setDoAnalysisBySave(false);
          if (!auto) {
            config.setDoAnalysisByOpen(false);
            config.setShowConfigurationPage(true, true);
          } else {
            config.setDoAnalysisByOpen(true);
            config.setDoAnalysisByIdle(true, timeout * 60 * 1000);
          }
          MagpieServer server = new MagpieServer(config);
          String language = "java";
          IProjectService javaProjectService = new JavaProjectService();
          server.addProjectService(language, javaProjectService);
          ToolAnalysis analysis = new InferServerAnalysis(dockerImage);
          Either<ServerAnalysis, ToolAnalysis> either = Either.forRight(analysis);
          server.addAnalysis(either, language);
          return server;
        };
    if (cmd.hasOption("socket")) {
      int port = Integer.parseInt(cmd.getOptionValue("port", DEFAULT_PORT));
      MagpieServer.launchOnSocketPort(port, createServer);
    } else {
      createServer.get().launchOnStdio();
    }
  }
}
