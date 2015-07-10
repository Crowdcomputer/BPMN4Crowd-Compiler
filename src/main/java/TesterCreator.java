import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.apache.commons.io.FileUtils;
import org.crowdcomputer.bpmn.utils.ModelReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by stefano on 1/30/15.
 */
public class TesterCreator {

    public static void main(String[] args) {
//
        TesterCreator compiler = new TesterCreator();
        compiler.createProcess("test_empty.bpmn");
        System.exit(0);
        // response.getOutputStream().print(s_model);

    }

    public void createProcess(String filename) {
        BpmnModel model = new ModelReader().read(filename);
        Process process = model.getMainProcess();
        StartEvent start = createStart("start");
        EndEvent end = creteEnd("end");
        process.addFlowElement(start);
        process.addFlowElement(end);
        for (int i = 0; i < 10; i++) {
            ScriptTask script = createScript("script" + i, "script" + i);
            process.addFlowElement(script);
            process.addFlowElement(createSequenceFlow(start.getId(),script.getId()));
            process.addFlowElement(createSequenceFlow(script.getId(),end.getId()));
        }
        store(model);


    }
    private SequenceFlow createSequenceFlow(String from, String to) {
//        log.debug("create flow {} {}", from, to);
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    private StartEvent createStart(String id) {
        StartEvent ret = new StartEvent();
        ret.setId(id);
        return ret;
    }

    private EndEvent creteEnd(String id) {
        EndEvent ret = new EndEvent();
        ret.setId(id);
        return ret;
    }

    private ScriptTask createScript(String id, String name) {
        ScriptTask ret = new ScriptTask();
        ret.setId(id + "pick");
        ret.setName(name + " pick");
        ret.setScript("println " + "\""+name+ "\"");
        ret.setScriptFormat("groovy");
        return ret;
    }

    private void store(BpmnModel model) {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        try {
            FileUtils.copyInputStreamToFile(
                    new ByteArrayInputStream(converter.convertToXML(model)),
                    new File("out.bpmn"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

