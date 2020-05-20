# InferIDE
This project integrates the static analyzer [Facebook Infer](https://github.com/facebook/infer) into IDEs and editors with the [MagpieBridge](https://github.com/MagpieBridge/MagpieBridge) framework.
InferIDE executes infer in the background and displays analysis results directly in IDEs which support the Language Server Protocol.
Currently, only analyzing Java Projects is supported.  
![infer.gif](https://github.com/MagpieBridge/InferIDE/blob/master/doc/infer.gif)

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
 
## Use InferIDE in VS Code

## Use InferIDE in GitPod

## Use InferIDE in Eclipse

## Use InferIDE in Sublime Text
