# InferIDE
This project integrates the static analyzer [Facebook Infer](https://github.com/facebook/infer) into IDEs and editors with the [MagpieBridge](https://github.com/MagpieBridge/MagpieBridge) framework.
InferIDE executes infer in the background and displays analysis results directly in IDEs which support the Language Server Protocol.
Currently, only analyzing Java Projects is supported.  
![infer.gif](https://github.com/MagpieBridge/InferIDE/blob/master/doc/infer.gif)
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
  - By default, InferIDE will use the docker image [jonasmanuel/inferdocker](https://hub.docker.com/r/jonasmanuel/inferdocker) from dockerhub. It is built from the (dockerfile)[inferdocker/Dockerfile] in this repository
  - If you want to use your own image you can supply the image using the command line option -i <dockerImage>
  - if the image is not yet installed on your system, docker will pull it on the first run
 
## Use InferIDE in VS Code

## Use InferIDE in GitPod

## Use InferIDE in Eclipse

## Use InferIDE in Sublime Text

## When is infer running?
These are two ways how infer can be triggered by InferIDE:
- Fully automated (default): The default way is fully based on user interactions in the IDE. Infer will be triggered when the user opens a source file for the first time in a project. Moreover, InferIDE detects if the user is idle in the IDE (not typing for a given amout of time, e.g. 5 minutes) and triggers infer. 
- User-controlled: InferIDE displays a HTML page in your web browser which allows you configure commands for infer to run and a click button to trigger infer.

Default build-in commands are :
- Maven Project:
 - `infer run --reactive -- mvn clean compile` (first run)
 - `infer run --reactive -- mvn compile`

- Gradle Project: 
 - `infer run --reactive -- ./gradlew clean build` (first run)
 - `infer run --reactive -- ./gradlew build`
