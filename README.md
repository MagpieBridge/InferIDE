# InferIDE
This project integrates the static analyzer [Facebook Infer](https://github.com/facebook/infer) into IDEs and editors with the [MagpieBridge](https://github.com/MagpieBridge/MagpieBridge) framework.
InferIDE executes infer in the background and displays analysis results directly in IDEs which support the Language Server Protocol.
Currently, only analyzing Java Projects is supported.  
![infer.gif](https://github.com/MagpieBridge/InferIDE/blob/master/doc/infer.gif)

## When is infer running?
These are two ways how infer can be triggered by InferIDE:
- Fully automated (default): The default way is fully based on user interactions in the IDE. Infer will be triggered when the user opens a source file for the first time in a project. Moreover, InferIDE detects if the user is idle in the IDE (not typing for a given time period) and triggers infer. 
- User-controlled: InferIDE displays a HTML page in your browser which allows you configure commands for infer to run and a click button to trigger infer.

## Use InferIDE in VS Code

## Use InferIDE in GitPod

## Use InferIDE in Eclipse

## Use InferIDE in Sublime Text
