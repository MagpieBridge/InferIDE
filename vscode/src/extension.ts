'use strict';
import * as net from 'net';
import { workspace, ExtensionContext, window } from 'vscode';
import { LanguageClient, LanguageClientOptions, ServerOptions, StreamInfo } from 'vscode-languageclient';


export async function activate(context: ExtensionContext) {
	// Startup options for the language server
	const settings = workspace.getConfiguration("InferIDE");
	const lspTransport: string = settings.get("lspTransport");
	const auto: boolean = settings.get("auto");
	const timeout = settings.get("timeout");
	const dockerImage: string = settings.get("dockerImage");
	let script = 'java';
	let relativePath = "inferIDE-0.0.1.jar";
	let args = ['-jar', context.asAbsolutePath(relativePath)];
	if (auto)
		args.push("--auto", timeout.toString())
	if (dockerImage)
		args.push("-i", dockerImage);
	const serverOptionsStdio = {
		run: { command: script, args: args },
		debug: { command: script, args: args }
	}

	const serverOptionsSocket = () => {
		const socket = net.connect({ port: 5007 })
		const result: StreamInfo = {
			writer: socket,
			reader: socket
		}
		return new Promise<StreamInfo>((resolve) => {
			socket.on("connect", () => resolve(result))
			socket.on("error", _ =>
				window.showErrorMessage(
					"Failed to connect to InferIDE language server. Make sure that the language server is running " +
					"-or- configure the extension to connect via standard IO."))
		})
	}

	const serverOptions: ServerOptions =
		(lspTransport === "stdio") ? serverOptionsStdio : (lspTransport === "socket") ? serverOptionsSocket : null

	let clientOptions: LanguageClientOptions = {
		documentSelector: [{ scheme: 'file', language: 'java' }],
		synchronize: {
			configurationSection: 'java',
			fileEvents: [workspace.createFileSystemWatcher('**/*.java')]
		}
	};

	// Create the language client and start the client.
	let client: LanguageClient = new LanguageClient('InferIDE', 'InferIDE', serverOptions, clientOptions);
	client.start();
	await client.onReady();
}

