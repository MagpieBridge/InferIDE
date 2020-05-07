package magpiebridge.finfer;

import org.eclipse.lsp4j.jsonrpc.messages.Either;

import magpiebridge.core.IProjectService;
import magpiebridge.core.MagpieServer;
import magpiebridge.core.ServerAnalysis;
import magpiebridge.core.ServerConfiguration;
import magpiebridge.core.ToolAnalysis;
import magpiebridge.projectservice.java.JavaProjectService;

public class InferIDE {
  public static void main(String... args) {
    ServerConfiguration config = new ServerConfiguration();
    MagpieServer server = new MagpieServer(config);
    String language = "java";
    IProjectService javaProjectService = new JavaProjectService();
    server.addProjectService(language, javaProjectService);
    ServerAnalysis analysis = new InferServerAnalysis();
    Either<ServerAnalysis, ToolAnalysis> either = Either.forLeft(analysis);
    server.addAnalysis(either, language);
    //server.launchOnStdio();
    server.launchOnSocketPort(5007);
  }
}
