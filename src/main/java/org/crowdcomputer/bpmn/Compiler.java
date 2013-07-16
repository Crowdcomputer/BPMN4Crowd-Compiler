package org.crowdcomputer.bpmn;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.impl.bpmn.diagram.ProcessDiagramGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.crowdcomputer.bpmn.utils.AppZip;
import org.crowdcomputer.bpmn.utils.ConsoleOutput;
import org.crowdcomputer.bpmn.utils.ModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compiler {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String file = "";
	private Set<String> processed = new HashSet<String>();
	private HashMap<String, String> oldAndNew = new HashMap<String, String>();
	private static String defaultFolder = "export";
	private ConsoleOutput output = new ConsoleOutput(true);

	private static String[] bpmn4crowdTasks = {
			"org.crowdcomputer.impl.task.MarketPlaceTask",
			"org.crowdcomputer.impl.task.NewsletterTask",
			"org.crowdcomputer.impl.task.ContestTask",
			"org.crowdcomputer.impl.task.TurkTask" };

	private void fixChildProcesses(List<ServiceTask> myTasks) {
		// output.print("Compiling internal Processes");
		for (ServiceTask serviceTask : myTasks) {
			// log.debug("Checking {}",serviceTask.getId());
			for (FieldExtension field : serviceTask.getFieldExtensions()) {
				// log.debug("Field {}",field.getFieldName());
				if (field.getFieldName().equals("validation_process")) {
					String reward_process = field.getStringValue();
					log.debug("field {}",field.getStringValue());
					if (reward_process.trim().length() > 0) {
						BpmnModel model = new ModelReader()
								.read(reward_process);
						output.println("Compiling " + reward_process);
						Process process = model.getMainProcess();
						String newId = "";
						String oldId = reward_process;
						if (!processed.contains(oldId)) {
							newId = "sid-" + UUID.randomUUID();
							processed.add(oldId);
							processed.add(newId + ".bpmn");
							oldAndNew.put(oldId, newId);
							log.debug("old and new {} {}", oldId, newId
									+ ".bpmn");
							process.setId(newId);
							List<ServiceTask> myTasksChild = extractTasks(process);
							addReceiveTasks(myTasksChild, process);
							autoLayout(model);
							store(model);
							generatePNG(model);
							field.setStringValue(oldAndNew.get(oldId));
						} else {
							field.setStringValue(oldAndNew.get(oldId));

							log.debug("already processed stuff {} ->{}", oldId,
									field.getStringValue());
						}
					}
					output.println(reward_process + " done");
				}
			}
		}
	}

	private List<ServiceTask> extractTasks(Process process) {
		output.print("Extracting BPMN4Crowd tasks..");
		Collection<FlowElement> elements = process.getFlowElements();
		List<ServiceTask> ret = new ArrayList<ServiceTask>();
		for (FlowElement flowElement : elements) {
			if (flowElement instanceof ServiceTask) {
				ServiceTask stask = ((ServiceTask) flowElement);
				if (Arrays.asList(bpmn4crowdTasks).contains(
						stask.getImplementation())) {
					ret.add(stask);
					log.debug("found service Task {}", stask.getName());
				}
			}
		}
		output.print("done\n");
		return ret;
	}

	private void store(BpmnModel model) {
		makeFolder();
		BpmnXMLConverter converter = new BpmnXMLConverter();
		output.print("Storing " + model.getMainProcess().getName());
		// String s_model = new String(converter.convertToXML(model));
		// InputStream s_model = new
		try {
			FileUtils.copyInputStreamToFile(
					new ByteArrayInputStream(converter.convertToXML(model)),
					new File(defaultFolder + File.separator
							+ model.getMainProcess().getId() + ".bpmn"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void makeFolder() {
		File f = new File(defaultFolder);
		try {
			f.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void parseSubProcesse(List<ServiceTask> myTasks) {
		// TODO Auto-generated method stub

	}

	private void generatePNG(BpmnModel model) {
		if (false) {
			try {
				ProcessDiagramGenerator pdg = new ProcessDiagramGenerator();
				InputStream inputStream = pdg.generatePngDiagram(model);
				File f = null;

				FileUtils.copyInputStreamToFile(inputStream, new File(
						defaultFolder + File.separator
								+ model.getMainProcess().getId() + ".png"));
				output.println("Image stored to " + f.getAbsolutePath());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.error("something went wrong");
			}
		} else {
			log.debug("Generation of PNG does not work");
		}

	}

	private void autoLayout(BpmnModel model) {
		output.print("Rigenerating layout.");
		new BpmnAutoLayout(model).execute();
		output.print("done\n");
	}

	private void addReceiveTasks(List<ServiceTask> myTasks, Process process) {
		output.print("Modifing BPMN4Crowd Tasks..");
		for (ServiceTask serviceTask : myTasks) {
			log.debug("Processing {}", serviceTask.getId());
			// List<SequenceFlow> incoming = serviceTask.getIncomingFlows();
			List<SequenceFlow> outgoing = serviceTask.getOutgoingFlows();
			int i = 0;
			for (SequenceFlow out : outgoing) {
				output.print("...");
				log.debug("fixing outgoing {} {}", out.getSourceRef(),
						out.getTargetRef());
				// create receive task
				ReceiveTask rtask = createReceive(serviceTask.getId() + i,
						serviceTask.getName());
				// add it to the process
				process.addFlowElement(rtask);
				// old outgoing flow now points to receive task
				// out.setTargetRef(rtask.getId());
				process.removeFlowElement(out.getId());
				log.debug("removed");
				process.addFlowElement(createSequenceFlow(rtask.getId(),
						out.getTargetRef()));
				process.addFlowElement(createSequenceFlow(out.getSourceRef(),
						rtask.getId()));
				// create new flow from target to old re
				i++;
			}
		}
		output.print("done\n");

	}

	private ReceiveTask createReceive(String id, String name) {
		ReceiveTask ret = new ReceiveTask();
		ret.setId(id + "receive");
		ret.setName(name + " receive");
		return ret;
	}

	private SequenceFlow createSequenceFlow(String from, String to) {
		log.debug("create flow {} {}", from, to);
		SequenceFlow flow = new SequenceFlow();
		flow.setSourceRef(from);
		flow.setTargetRef(to);
		return flow;
	}

	public void compileMain(String file, String zipfile) {
		output.colorPrintln(ConsoleOutput.ANSI_GREEN, "[BPMN4Crowd COMPILER]\n");
		output.colorPrintln(ConsoleOutput.ANSI_RESET, "Compiling main file "
				+ file);
		BpmnModel model = new ModelReader().read(file);
		Process process = model.getMainProcess();
		String newId = "sid-main-" + UUID.randomUUID();
		String oldId = process.getId();
		if (zipfile.length() == 0)
			zipfile = process.getName();
		processed.add(file);
		processed.add(newId + ".bpmn");
		process.setId(newId);
		List<ServiceTask> myTasks = extractTasks(process);
		addReceiveTasks(myTasks, process);
		autoLayout(model);

		fixChildProcesses(myTasks);
		// parseSubProcesse(myTasks);
		store(model);
		generatePNG(model);
		output.print("Generating zip file: " + zipfile + "...");
		AppZip appZip = new AppZip("export", zipfile);
		appZip.zip();
		output.print("done\n");
		output.print(file + " has been compiled.\n");
		output.print("removing junk...");
		try {
			FileUtils.deleteDirectory(new File("export"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		output.print("done\n");
		output.colorPrintln(ConsoleOutput.ANSI_GREEN, "Zip " + zipfile
				+ ".zip create and ready to be deployed");
		output.colorPrintln(ConsoleOutput.ANSI_GREEN, "\n\n Enjoy");
		output.colorPrintln(ConsoleOutput.ANSI_RED,
				"BPMN4Crowd - stefano.tranquillini@gmail.com");

	}

	public static void main(String[] args) {

		String file = "";
		String zipfile = "";
		if (args.length > 0) {
			file = args[0];
		} else
			file = "/Users/stefanotranquillini/sw/CroCO/BpmnCompiler/src/main/resources/test.bpmn";
		if (args.length > 1) {
			zipfile = args[1];
		} else
			zipfile = "";
		Compiler compiler = new Compiler();
		compiler.compileMain(file, zipfile);
		System.exit(0);
		// response.getOutputStream().print(s_model);

	}
}
