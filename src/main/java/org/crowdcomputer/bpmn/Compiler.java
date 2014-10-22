package org.crowdcomputer.bpmn;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.crowdcomputer.bpmn.utils.AppZip;
import org.crowdcomputer.bpmn.utils.ConsoleOutput;
import org.crowdcomputer.bpmn.utils.ModelReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Compiler {
    private org.apache.logging.log4j.Logger log = LogManager.getLogger(this.getClass());

    private static String defaultFolder = "";
    private ConsoleOutput output = new ConsoleOutput(false);
    private ArrayList<String> fileList = new ArrayList<String>();
    private HashSet<String> processed = new HashSet<String>();

    private static String[] bpmn4crowdTasks = {
            "org.crowdcomputer.impl.task.MarketPlaceTask",
            "org.crowdcomputer.impl.task.NewsletterTask",
            "org.crowdcomputer.impl.task.ContestTask",
            "org.crowdcomputer.impl.task.TurkTask"
    };
    private static String[] bpmn4crowdFakeTask = {
            "org.crowdcomputer.impl.tactic.PickTask",
            "org.crowdcomputer.impl.tactic.ReceiveResult"
    };
    private static String[] bpmn4crowdTacticTask = {
            "org.crowdcomputer.impl.tactic.RewardTask",
            "org.crowdcomputer.impl.tactic.ValidationTask"
    };

    private static String[] bpmn4crowdCallProcessTask = {
            "org.crowdcomputer.impl.tactic.RewardTask",
            "org.crowdcomputer.impl.tactic.ValidationTask",
            "org.crowdcomputer.impl.tactic.CreateTask"
    };

    private static String[] int_processes = {
            "validation_process",
            "tactic_process",
            "reward_process",
    };


    private List<ServiceTask> extractTasks(Collection<FlowElement> elements, String[] list) {
//        output.print("Extracting BPMN4Crowd tasks..");
//        Collection<FlowElement> elements = process.getFlowElements();
        List<ServiceTask> ret = new ArrayList<ServiceTask>();
        for (FlowElement flowElement : elements) {


            if (flowElement instanceof ServiceTask) {
                ServiceTask stask = ((ServiceTask) flowElement);
//                all the tasks that are bpm4crowdTasks
                if (Arrays.asList(list).contains(
                        stask.getImplementation())) {
                    ret.add(stask);
//                    log.debug("found service Task {}", stask.getName());
                }
            }
        }
        output.print("done\n");
        return ret;
    }


    private ServiceTask extractTaskCustom(Process process) {
        Collection<FlowElement> elements = process.getFlowElements();
        for (FlowElement flowElement : elements) {
            if (flowElement instanceof ServiceTask) {
                ServiceTask stask = ((ServiceTask) flowElement);
                if (stask.getImplementation().equals("org.crowdcomputer.impl.tactic.CreateTask"))
                    return stask;
            }
        }
        return null;
    }


    private void store(BpmnModel model) {
        makeFolder();
        BpmnXMLConverter converter = new BpmnXMLConverter();
//        output.print("Storing " + model.getMainProcess().getName());
        log.debug("name of process {}", model.getMainProcess().getId());
        // String s_model = new String(converter.convertToXML(model));
        // InputStream s_model = new
        try {
            FileUtils.copyInputStreamToFile(
                    new ByteArrayInputStream(converter.convertToXML(model)),
                    new File(model.getMainProcess().getId() + ".bpmn"));
            if (!fileList.contains(model.getMainProcess().getId() + ".bpmn"))
                fileList.add(model.getMainProcess().getId() + ".bpmn");

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


    private void autoLayout(BpmnModel model) {
        output.print("Rigenerating layout.");
//        new BpmnAutoLayout(model).execute();
        output.print("done\n");
    }

    private void addReceiveTasks(List<ServiceTask> myTasks, Process process) {
        output.print("Modifing BPMN4Crowd Tasks..");
        for (ServiceTask serviceTask : myTasks) {
            log.debug("Adding receiver for {} in {}", serviceTask.getId(), process.getId());
            // List<SequenceFlow> incoming = serviceTask.getIncomingFlows();
            List<SequenceFlow> outgoing = serviceTask.getOutgoingFlows();
            int i = 0;
            for (SequenceFlow out : outgoing) {
                output.print("...");
                log.debug("fixing outgoing {} {}", out.getSourceRef(),
                        out.getTargetRef());
                // create receive task
                ReceiveTask rtask = createReceive(serviceTask.getId() + '-',
                        serviceTask.getName());
                // add it to the process
                process.addFlowElement(rtask);
                // old outgoing flow now points to receive task
//                out.setTargetRef(rtask.getId());
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

    private void addReceiveTasksSub(List<ServiceTask> myTasks, SubProcess process) {
        output.print("Modifing BPMN4Crowd Tasks..");
        for (ServiceTask serviceTask : myTasks) {
            log.debug("Adding receiver for {} in {}", serviceTask.getId(), process.getId());
            // List<SequenceFlow> incoming = serviceTask.getIncomingFlows();
            List<SequenceFlow> outgoing = serviceTask.getOutgoingFlows();
            int i = 0;
            for (SequenceFlow out : outgoing) {
                output.print("...");
                log.debug("fixing outgoing {} {}", out.getSourceRef(),
                        out.getTargetRef());
                // create receive task
                ReceiveTask rtask = createReceive(serviceTask.getId() + '-',
                        serviceTask.getName());
                // add it to the process
                process.addFlowElement(rtask);
                // old outgoing flow now points to receive task
//                out.setTargetRef(rtask.getId());
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

    private ReceiveTask createPick(String id, String name) {
        ReceiveTask ret = new ReceiveTask();
        ret.setId(id + "pick");
        ret.setName(name + " pick");
        return ret;
    }

    private SequenceFlow createSequenceFlow(String from, String to) {
//        log.debug("create flow {} {}", from, to);
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    private void replaceFakeTask(ServiceTask createTask) {
        String file = searchFilename(createTask).getStringValue();
        BpmnModel model;
        if (!file.endsWith(".bpmn"))
            file = file + ".bpmn";
        model = new ModelReader().read(file);
        Process process = model.getMainProcess();
        List<ServiceTask> tasksThatNeedsToBeReplaces = extractTasks(process.getFlowElements(), bpmn4crowdFakeTask);
        log.debug("tasksThatNeedsToBeReplaces {}", tasksThatNeedsToBeReplaces.size());
        replaceTask(tasksThatNeedsToBeReplaces, process, createTask);

        List<SubProcess> subs = new ArrayList<SubProcess>();
        for (FlowElement flowElement : process.getFlowElements())
            if (flowElement instanceof SubProcess) {
                subs.add((SubProcess) flowElement);
            }
        for (SubProcess sub : subs) {
            List<ServiceTask> retSub = extractTasks(sub.getFlowElements(), bpmn4crowdFakeTask);
            replaceTaskSub(retSub, sub, createTask);
        }
        log.debug("adding {} {} ", model.getMainProcess().getId(), model.getMainProcess().getName());
        log.debug("model {} ", model);
        autoLayout(model);
        store(model);
    }

    private String fixProcess(String file, String prefix) {

        String newId = prefix + UUID.randomUUID();

        if (!file.endsWith(".bpmn"))
            file = file + ".bpmn";
        log.debug("modifiying {}->{}", file, newId);
        BpmnModel model = new ModelReader().read(file);
        Process process = model.getMainProcess();
        if (process.getId().startsWith("sid-"))
            return process.getId();
//            newId = process.getId()'';
        log.debug("newId is {}", newId);
        process.setId(newId);
//        create process with new id.
        store(model);
        //get all the task that need a received
//        extract task that needs edits
        List<ServiceTask> tasksThatNeedsAReceiver = extractTasks(process.getFlowElements(), bpmn4crowdTasks);
        log.debug("tasksThatNeedsAReceiver {}", tasksThatNeedsAReceiver.size());

        //add a receiver
//        for all of these tasks add a receivers (we need it )
        addReceiveTasks(tasksThatNeedsAReceiver, process);
//        save edits
        store(model);
        autoLayout(model);

        //for all the tasks, check if it is a customtactic, if so process it
        ServiceTask mainTask = extractTaskCustom(process);
        log.debug("MainTask {}", mainTask);

        if (mainTask != null) {
            FieldExtension field = searchFilename(mainTask);
            String newName = fixProcess(field.getStringValue(), "sid-");
            field.setStringValue(newName);
            replaceFakeTask(mainTask);
        }
// //       process validation and reward tasks (validation,reward,customtactic)
        List<ServiceTask> tasksThatNeedToBeProcessed = extractTasks(process.getFlowElements(), bpmn4crowdCallProcessTask);
        addReceiveTasks(tasksThatNeedToBeProcessed, process);
        store(model);
        log.debug("tasksThatNeedToBeProcessed {}", tasksThatNeedToBeProcessed.size());

        List<SubProcess> subs = new ArrayList<SubProcess>();
        for (FlowElement flowElement : process.getFlowElements())
            if (flowElement instanceof SubProcess) {
                subs.add((SubProcess) flowElement);
            }
        for (SubProcess sub : subs) {
            List<ServiceTask> tasksThatNeedToBeProcessed2 = extractTasks(sub.getFlowElements(), bpmn4crowdCallProcessTask);
            log.debug("sub here");
            log.debug(tasksThatNeedToBeProcessed2.size());
            addReceiveTasksSub(tasksThatNeedToBeProcessed2, sub);
            for (ServiceTask taskProcess2 : tasksThatNeedToBeProcessed2) {
                FieldExtension field2 = searchFilename(taskProcess2);
                log.debug("this file is inside {}  ", field2.getStringValue());
                String newName2 = fixProcess(field2.getStringValue(), "sid-");
                field2.setStringValue(newName2);
                store(model);
            }

        }


        for (ServiceTask taskProcess : tasksThatNeedToBeProcessed) {
//            return the filed, later it have to be change

            FieldExtension field = searchFilename(taskProcess);
            log.debug("this file is inside {}  ", field.getStringValue());
//            if (!processed.contains(field.getStringValue())) {
            String newName = fixProcess(field.getStringValue(), "sid-");
            field.setStringValue(newName);
            //this goes just one level down.. should be recursive


        }
        store(model);
        processed.add(newId);
        return newId;
    }

    private FieldExtension searchFilename(ServiceTask taskProcess) {
        for (FieldExtension field : taskProcess.getFieldExtensions()) {
            if (Arrays.asList(int_processes).contains(field.getFieldName())) {
                return field;
            }
        }
        return null;
    }

    public void Compile(String file, String zipfile) {
        output.colorPrintln(ConsoleOutput.ANSI_GREEN, "[BPMN4Crowd COMPILER]\n");
        output.colorPrintln(ConsoleOutput.ANSI_RESET, "Compiling main file "
                + file);
        log.debug(file);
        if (zipfile.length() == 0)
            zipfile = "out";
        fixProcess(file, "sid-main-");

        output.print("Generating zip file: " + zipfile + "...");
        AppZip appZip = new AppZip(fileList, zipfile);
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


    private void replaceTask(List<ServiceTask> customTasks, Process process, ServiceTask mainTask) {
        for (ServiceTask stask : customTasks) {
            ReceiveTask newtask = null;
            log.debug("replace {}", stask.getImplementation());
            if (stask.getImplementation().equals("org.crowdcomputer.impl.tactic.PickTask")) {
                log.debug("picktask");
                newtask = createPick(mainTask.getId() + '-', mainTask.getName());
                substitute(stask, newtask, process);

            } else if (stask.getImplementation().equals("org.crowdcomputer.impl.tactic.ReceiveResult")) {
                log.debug("receive");
                newtask = createReceive(mainTask.getId() + '-',
                        mainTask.getName());
                substitute(stask, newtask, process);

            }

        }
    }


    private void substitute(ServiceTask remove, ReceiveTask add, Process process) {
        process.removeFlowElement(remove.getId());
        List<SequenceFlow> flow = remove.getIncomingFlows();
        for (SequenceFlow sf : flow) {
            process.addFlowElement(createSequenceFlow(sf.getSourceRef(), add.getId()));
            process.removeFlowElement(sf.getId());
        }
        flow = remove.getOutgoingFlows();
        for (SequenceFlow sf : flow) {
            process.addFlowElement(createSequenceFlow(add.getId(), sf.getTargetRef()));
            process.removeFlowElement(sf.getId());
        }
        process.addFlowElement(add);

    }

    //this is bad, but it's the fastest way i found.
    private void replaceTaskSub(List<ServiceTask> customTasks, SubProcess process, ServiceTask mainTask) {
        for (ServiceTask stask : customTasks) {
            ReceiveTask newtask = null;

            log.debug("replace {}", stask.getImplementation());
            if (stask.getImplementation().equals("org.crowdcomputer.impl.tactic.PickTask")) {
                log.debug("picktask");
                newtask = createPick(mainTask.getId() + '-', mainTask.getName());
                substituteSub(stask, newtask, process);

            } else if (stask.getImplementation().equals("org.crowdcomputer.impl.tactic.ReceiveResult")) {
                log.debug("receive");
                newtask = createReceive(mainTask.getId() + '-',
                        mainTask.getName());
                substituteSub(stask, newtask, process);

            }

        }
    }


    private void substituteSub(ServiceTask remove, ReceiveTask add, SubProcess process) {
        process.removeFlowElement(remove.getId());
        List<SequenceFlow> flow = remove.getIncomingFlows();
        for (SequenceFlow sf : flow) {
            process.addFlowElement(createSequenceFlow(sf.getSourceRef(), add.getId()));
            process.removeFlowElement(sf.getId());
        }
        flow = remove.getOutgoingFlows();
        for (SequenceFlow sf : flow) {
            process.addFlowElement(createSequenceFlow(add.getId(), sf.getTargetRef()));
            process.removeFlowElement(sf.getId());
        }
        process.addFlowElement(add);

    }


    public static void main(String[] args) {
//        sorry, this code is a mess. if i've time i'll make it more clear.
        String file = "";
        String zipfile = "";
        if (args.length > 0) {
            file = args[0];
        } else
            file = "testprocess.bpmn";
        if (args.length > 1) {
            zipfile = args[1];
        } else
            zipfile = "";
        System.out.println("BPMN4Crowd Compiler");
        System.out.println("Starting " + file + " ..");
        Compiler compiler = new Compiler();
        compiler.Compile(file, zipfile);
        System.out.println("end .. check your file " + zipfile + ".zip");
        System.out.println("stefano - stefano.tranquillini@gmail.com");
        System.exit(0);
        // response.getOutputStream().print(s_model);

    }
}
