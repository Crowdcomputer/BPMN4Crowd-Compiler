#How to use it
Place all your files within the folder where the compiler is.

`java -cp BpmnCompiler-XXX.jar org.crowdcomputer.bpmn.Compiler $1 $2`
(`XXX` is the version of the current relase)
where
- `$1` is the main BPMN file (e.g., test.bpmn)
- `$2` is the name of the resulting zip file, without extension (e.g., test -> test.zip)

`java -cp BpmnCompiler-XXX.jar org.crowdcomputer.bpmn.Compiler test.bpmn test` will compile the `test.bpmn` file and relative validation process, storing the result in `test.zip` file.
