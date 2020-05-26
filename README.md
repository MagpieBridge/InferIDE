# InferIDE
This project integrates the static analyzer [Facebook Infer](https://github.com/facebook/infer) into IDEs and editors with the [MagpieBridge](https://github.com/MagpieBridge/MagpieBridge) framework.
InferIDE executes infer in the background and displays analysis results directly in IDEs which support the Language Server Protocol.
Currently, only analyzing Java Projects is supported. For VS Code user, simply install [inferide](https://marketplace.visualstudio.com/items?itemName=LinghuiLuo.inferide&ssr=false) from the VS Code Marktplace.
![infer.gif](https://github.com/MagpieBridge/InferIDE/blob/master/doc/infer.gif)
**Figure 1: Infer warnings in VS Code** 
# Usage
## Install infer
* MacOS `brew install infer`
* Linux 
  - see [infer documentation](https://fbinfer.com/docs/getting-started/) for the latest version.
  - At time of this writing you can use the following command to install version 0.17.0:
  ```bash
  VERSION=0.17.0; \
  curl -sSL "https://github.com/facebook/infer/releases/download/v$VERSION/infer-linux64-v$VERSION.tar.xz" \
  | sudo tar -C /opt -xJ && \
  ln -s "/opt/infer-linux64-v$VERSION/bin/infer" /usr/local/bin/infer
  ```
* Windows
  - Infer does not run natively on windows, but you can use [docker](https://docs.docker.com/docker-for-windows/install/)
* Docker
  - If Docker is installed and infer is not found on the system, InferIDE will use docker to start a container that has infer installed
  - By default, InferIDE will use the docker image [jonasmanuel/inferdocker](https://hub.docker.com/r/jonasmanuel/inferdocker) from dockerhub. It is built from the [dockerfile](inferdocker/Dockerfile) in this repository
  - If you want to use your own image you can supply the image using the command line option -i <dockerImage>
  - if the image is not yet installed on your system, docker will pull it on the first run
    - otherwise you can install it beforehand using `docker pull jonasmanuel/inferdocker` 
 
## Use InferIDE in VS Code
Simply install [inferide](https://marketplace.visualstudio.com/items?itemName=LinghuiLuo.inferide&ssr=false) from the VS Code Marktplace or the latest [InferIDE VS code extension](https://github.com/MagpieBridge/InferIDE/releases/download/0.0.1/inferide-0.0.1.vsix) from the release page. 
## Use InferIDE in the online editor --- GitPod
Configure `.gitpod.yml` for your project as in [this file](https://github.com/MagpieBridge/MagpieBridge/blob/develop/.gitpod.yml
), namely 
```
image: jonasmanuel/inferdocker
vscode:
  extensions:
    - LinghuiLuo.inferide@0.0.1:reNHu3/GfefVx26yvbo/sg==
```
![gitpod](https://github.com/MagpieBridge/InferIDE/blob/master/doc/gitpod.gif)
**Figure 2: Infer warnings in Gitpod** 

## Use InferIDE in Eclipse, IntelliJ/Android Studio, Sublime Text, Vim, Emacs 
- Download the [inferIDE jar file](https://github.com/MagpieBridge/InferIDE/releases/download/0.0.1/inferIDE-0.0.1.jar) from the release page.
- Use the command `java -jar inferIDE-0.0.1.jar -a 5` or simply `java -jar inferIDE-0.0.1.jar` to configure the language server for Java in your desired IDE. Details are explained in [this tuturial](https://github.com/MagpieBridge/MagpieBridge/wiki/Tutorial-11.-Configure-different-IDEs-to-use-your-MagpieBridge-based-server) for all listed IDE and editors. InferIDE supports the following options:
```
 -a,--auto <arg>          enables auto mode and sets the timeout in
                          minutes for starting the automatic tool analysis
                          default timeout is 5
 -i,--dockerImage <arg>   dockerImage to use when running infer in docker
                          default is `jonasmanuel/inferdocker`
 -p,--port <arg>          sets the port for socket mode, standard port is
                          5007
 -s,--socket              run in socket mode, standard port is 5007
 ```
For editors like sublime, make sure you open the root path of your testing Java project at first, then any Java source file. This way the editors can notify InferIDE the project root path and execute infer from there. 
![eclipse](https://github.com/MagpieBridge/InferIDE/blob/master/doc/eclipse.gif)
**Figure 3: Infer warnings in Eclipse** 
![intellij](https://github.com/MagpieBridge/InferIDE/blob/master/doc/intellij.gif)
**Figure 4: Infer warnings in IntelliJ** 

## When is infer running?
These are two ways how infer can be triggered by InferIDE:
- Fully automated (default): The default way is fully based on user interactions in the IDE. Infer will be triggered when the user opens a source file for the first time in a project. Moreover, InferIDE detects if the user is idle in the IDE (not typing for a given amout of time, e.g. 5 minutes) and triggers infer.  The InferIDE command used is `java -jar inferIDE-0.0.1.jar -a 5`
- User-controlled: InferIDE displays a HTML page (see screenshot below) in your web browser which allows you configure commands for infer to run and a click button to trigger infer. The InferIDE command used is `java -jar inferIDE-0.0.1.jar`
Default build-in commands are :
- Maven Project:
  - `infer run --reactive -- mvn clean compile` (first run)
  - `infer run --reactive -- mvn compile`

- Gradle Project: 
  - `infer run --reactive -- ./gradlew clean build` (first run)
  - `infer run --reactive -- ./gradlew build`
 
 <img src="https://github.com/MagpieBridge/InferIDE/blob/master/doc/htmlpage.png" width="600">
 
## How to build InferIDE by yourself?
- Use maven to build, simply `mvn install`
- For VS Code Extension, after build navigate to the directory vscode, execute the following commands:
```
        - npm install #(if the first time)
        - npm install -g vsce #(if the first time)
        - vsce package #(this will create vscode extension under vscode directory)
 ```
 
## Get Involved
- Pull requests are welcome!
- Submit github issues for any feature enhancements, bugs or documentation problems

## Contact 
&#x2709; linghui[at]outlook.de
