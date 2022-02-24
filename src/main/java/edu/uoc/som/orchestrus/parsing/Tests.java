package edu.uoc.som.orchestrus.parsing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.Trace;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;
import edu.uoc.som.orchestrus.tracemodel.typing.LinkTypeFactory;
import edu.uoc.som.orchestrus.utils.BasicFormatter;

public class Tests {
	
	public final static Logger LOGGER = Logger.getLogger(Tests.class.getName());

	 

	public static void main(String[] args) throws IOException, ParserConfigurationException {
		System.out.println("    --  o· o - O ~ o - o ~ o · O ·--");
		System.out.println("    --                            --");
		System.out.println("    -- --      Orchestrus      -- --");
		System.out.println("    --       Parsing  tests       --");
		System.out.println("    --------------------------------\n");
		
		testDesignTypesExtraction();
		
		System.out.println("\n\n-- Safe Exit o·~ !¡");
	}
	
	public static void testDesignTypesExtraction() throws ParserConfigurationException {
		LOGGER.info("");

		//Factories
		ArtefactTypeFactory atFactory = ArtefactTypeFactory.getInstance();
		LinkTypeFactory ltFactory = LinkTypeFactory.getInstance();

		//Trace
		Trace t = new Trace("testDesignTypesExtraction");
		
		
		//Config
		Config config = new Config();
		
		
		DocumentBuilderFactory factory =
		DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		FileInputStream input;
		try {
			File umlProfile = new File(config.getDomainModelFiles().get("uml"));
			LOGGER.info("UML Domain model file: "+umlProfile.getAbsolutePath());
			input = new FileInputStream(umlProfile);

			
			input = new FileInputStream(new File(config.getDomainModelFiles().get("di")));
			
			Document doc = builder.parse(input);
			
			
			System.out.println(doc.toString());
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			String expression = "/uml:Model";	       
			System.out.println("Tests.testDesignTypesExtraction() +++");
			try {
				NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(  doc, XPathConstants.NODESET);
				for (int i = 0; i < nodeList.getLength(); i++) {
					   Node nNode = nodeList.item(i);
					   System.out.println(nNode);
					}
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		/*
//		 * Link types from design to code and palette and EltTypeConfig + internal c2c: code to code.
//		 */
//		LinkType d2c = ltFactory.addType("Design2Code");
//		LinkType d2p = ltFactory.addType("Design2Palette");
//		LinkType p2c = ltFactory.addType("Palette2Code");
//		LinkType c2et = ltFactory.addType("Code2EltType");
//		LinkType c2c = ltFactory.addType("Code2");
//		LinkType p2p = ltFactory.addType("Palette2");
//		LinkType et2et = ltFactory.addType("EltType2");
//		
//		/*
//		 * Actual trace : 
//		 * src - name - tgt - type
//		 *  0  -  l1  -  3  -  d2c
//		 *  1  -  l2  -  5  -  d2p
//		 *  2  -  l3  -  5  -  d2p
//		 *  5  -  l4  -  6  -  p2p
//		 *  3  -  l5  -  4  -  c2c
//		 *  6  -  l6  -  4  -  p2c
//		 *  4  -  l7  -  7  -  c2et
//		 *  7  -  l8  -  8  -  et2et
//		 */
//		TraceLink l1 = new TraceLink("l1", d2c);
//		TraceLink l2 = new TraceLink("l2", d2p);
//		TraceLink l3 = new TraceLink("l3", d2p);
//		l1.setEnds(afs[0], afs[3]);
//		l2.setEnds(afs[1], afs[5]);
//		l3.setEnds(afs[2], afs[5]);
//		
//		TraceLink l4 = new TraceLink("l4", p2p);
//		l4.setEnds(afs[5], afs[6]);
//		
//		TraceLink l5 = new TraceLink("l5", c2c);
//		l5.setEnds(afs[3], afs[4]);
//
//		TraceLink l6 = new TraceLink("l6", p2c);
//		l6.setEnds(afs[6], afs[4]);
//
//		TraceLink l7 = new TraceLink("l7", c2et);
//		TraceLink l8 = new TraceLink("l8", et2et);
//		l7.setEnds(afs[4], afs[7]);
//		l8.setEnds(afs[7], afs[8]);
//		
//
//		t.addTraceLink(l1);
//		t.addTraceLink(l2);
//		t.addTraceLink(l3);
//		System.out.println("Closure of l1: (" +  l1.getClosure().size() + "/3) " + l1.getClosure());
//		System.out.println("Closure of t: (" +  t.getAllTraceLinks().size() + "/8) " + t.getAllTraceLinks());
//		
//		System.out.println(t.printJSon());
	}
	


}
