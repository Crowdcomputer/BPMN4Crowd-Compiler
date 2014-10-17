package org.crowdcomputer.bpmn.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.crowdcomputer.bpmn.Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelReader {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public BpmnModel read(String file) {
//		log.info("Loading process from {}", file);
		BpmnXMLConverter converter = new BpmnXMLConverter();
		XMLInputFactory f = XMLInputFactory.newInstance();
		XMLStreamReader r = null;

//		Reader reader = new FileInputStream(new File(file));

		try {
			r = f.createXMLStreamReader(new FileInputStream(file));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		log.info("Model loaded");

		return converter.convertToBpmnModel(r);

		// model.addProcess(process);
	}
}
