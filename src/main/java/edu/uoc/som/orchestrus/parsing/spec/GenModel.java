package edu.uoc.som.orchestrus.parsing.spec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uoc.som.orchestrus.parsing.Reference;
import edu.uoc.som.orchestrus.parsing.ReferenceFactory;
import edu.uoc.som.orchestrus.parsing.Source;
import edu.uoc.som.orchestrus.parsing.SpecificFileReferenceExtractor;
import edu.uoc.som.orchestrus.parsing.utils.DomUtil;
import edu.uoc.som.orchestrus.utils.Utils;

public class GenModel extends SpecificFileReferenceExtractor {

	private String modelDirectory;
	private String modelPluginID;
	private String modelName;
	private String rootExtendsClass;
	private String importerID;
	private String[] usedGenPackages;
	
	File f;
	
	Element rootNode;
	List<Element> foreignModels;
	
	public GenModel(File f) {
		if(f == null)
			throw new IllegalArgumentException("File is null.");
		this.f = f;
		init();
	}
	
	public void init() {
		try {
			rootNode = null;
			Document doc = builder.parse(f);
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodeList2 = doc.getChildNodes();
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				if(rootNode == null)
					rootNode = (Element)nNode;
				else
					throw new IllegalAccessError("Only one genModel node (root) expected in genmodel file.");
			}
			affectRootValues(rootNode);
			
			foreignModels = new ArrayList<>();
			String expression = "//foreignModel";
			nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				foreignModels.add((Element)nNode);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void affectRootValues(Element rootNode2) {
		this.modelDirectory = ((Element) rootNode).getAttribute("modelDirectory");
		this.modelPluginID = ((Element) rootNode).getAttribute("modelPluginID");
		this.modelName = ((Element) rootNode).getAttribute("modelName");
		this.rootExtendsClass = ((Element) rootNode).getAttribute("rootExtendsClass");
		this.importerID = ((Element) rootNode).getAttribute("importerID");
		this.usedGenPackages = ((Element) rootNode).getAttribute("usedGenPackages").split(" ");
		if(usedGenPackages == null)
			usedGenPackages = new String[0];
	}

	public String getFilePath() {
		return f.getAbsolutePath();
	}

	@Override
	public String getHRefJSon() {
		String res = "";
		res = getJSonForRootValues(res);
		
		// TODO Information required to resolve ??
		Source source = new Source(getFilePath(), DomUtil.getAbsolutePath(rootNode), DomUtil.getAbsolutePathNamed(rootNode));
		Reference r = ReferenceFactory.getReference(Utils.cleanUrlsForJson(modelDirectory), source);
		Reference r2 = ReferenceFactory.getReference(Utils.cleanUrlsForJson(modelPluginID), source);
		Reference r3 = ReferenceFactory.getReference(Utils.cleanUrlsForJson(rootExtendsClass), source);
		addReference(r);
		addReference(r2);
		addReference(r3);
		String resFMs = getJSonForForeignModels();
		
		res = res + resFMs;
		return "{" + res + "}";
	}

	private String getJSonForRootValues(String res) {
		res += "\"root\": {"
					+ "\"modelDirectory\": \""+modelDirectory+"\", "
					+ "\"modelPluginID\": \""+modelPluginID+"\", "
					+ "\"modelName\": \""+modelName+"\", "
					+ "\"rootExtendsClass\": \""+rootExtendsClass+"\", "
					+ "\"importerID\": \""+importerID+"\", "
					+ "\"usedGenPackages\": "+getUsegGenPackagesAsJSonArray() + "}"
				+ ",\n";
		return res;
	}

	/**
	 * Return a JSon array with foreign model references. <br/>
	 * @return
	 */
	private String getJSonForForeignModels() {
		String resFMs = "";
		for (Element e : foreignModels) {
			resFMs += "\"" + ((Element) e).getTextContent() + "\",\n";

			// TODO Information required to resolve ??
			Source source = new Source(getFilePath(), DomUtil.getAbsolutePath(e), DomUtil.getAbsolutePathNamed(e));
			Reference r = ReferenceFactory.getReference(Utils.cleanUrlsForJson(e.getTextContent()), source);
			addReference(r);
		}
		resFMs = resFMs.trim();
		if (!resFMs.isBlank())
			resFMs = resFMs.substring(0, resFMs.length() - 1);
		resFMs = "\"foreignModels\": [" + resFMs + "] ";
		return resFMs;
	}

	/**
	 * Axtrude a list from the node's 'usedGenPackages' attribute.
	 * 
	 * @param nNode
	 * @return
	 */
	private String getUsegGenPackagesAsJSonArray() {
		String ugp = "";
		for (String gp : usedGenPackages)
			ugp += "\"" + gp + "\",";
		if (!ugp.isBlank())
			ugp = ugp.substring(0, ugp.length() - 1);
		ugp = "[" + ugp + "]";
		return ugp;
	}
}
