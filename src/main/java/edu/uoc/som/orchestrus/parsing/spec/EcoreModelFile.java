package edu.uoc.som.orchestrus.parsing.spec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.parsing.Reference;
import edu.uoc.som.orchestrus.parsing.ReferenceFactory;
import edu.uoc.som.orchestrus.parsing.Source;
import edu.uoc.som.orchestrus.parsing.SpecificFileReferenceExtractor;
import edu.uoc.som.orchestrus.parsing.utils.DomUtil;
import edu.uoc.som.orchestrus.utils.Utils;

public class EcoreModelFile extends SpecificFileReferenceExtractor{
	
	public final static Logger LOGGER = Logger.getLogger(EcoreModelFile.class.getName());

	public String getFilePath() {
		return Config.getInstance().getEcoreFilePath();
	}

	private String ecoreName;
	private String ecoreNsURI;
	private String ecoreNsPrefix;
	
	Element rootNode;
	List<Element> eStructuralFeatures;
	
	File f;
	List<Element> elements;
	public EcoreModelFile(File ecoreFile) {
		this.f = ecoreFile;
		elements = getElementsForEcoreRefs();
		affectRootValues(rootNode);
	}
	
	private void affectRootValues(Element rootNode) {
		this.ecoreName = (rootNode).getAttribute("name");
		this.ecoreNsURI = (rootNode).getAttribute("nsURI");
		this.ecoreNsPrefix = (rootNode).getAttribute("nsPrefix");
	}

	private List<Element> getElementsForEcoreRefs() {
		
		List<Element> res = new ArrayList<>();
		try {
			Document doc = builder.parse(f);
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			String expression = "//*[@name and @nsURI and @nsPrefix]";
			NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				rootNode = (Element)nNode;
				if(i >= 1)
					throw new IllegalAccessError("Ecore file should only have one root node.");
				res.add(rootNode);
			}
			
			expression = "//eStructuralFeatures";
			eStructuralFeatures = new ArrayList<Element>();
			nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				res.add((Element)nNode);
				eStructuralFeatures.add((Element)nNode);
			}
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return res;
	}

	public String getHRefJSon() {
		String res = "";
		res += "\"root\": {"
					+ "\"name\": \""+ecoreName+"\", "
					+ "\"nsURI\": \""+ecoreNsURI+"\", "
					+ "\"nsPrefix\": \""+ecoreNsPrefix+"\""
					+ "}"
				+ ",\n";
		
		String resFMs = getJSonForEStructuralFeatures();
		
		res = res + resFMs;
		return "{" + res + "}";
	}

	/**
	 * Return a JSon array with all estructural feature in the EcoreModel.<br/>
	 * Create a reference for each estructuralfeature with its 'eTypePath' - to be discussed.
	 * @return
	 */
	private String getJSonForEStructuralFeatures() {
		String resFMs = "";
		for (Element e : eStructuralFeatures) {
			
			String[] eTypeArray = ((Element)e).getAttribute("eType").split(" ");
			String eTypeType = "";
			String eTypePath = "";					
			if(eTypeArray.length > 1) {
				eTypeType = (e).getAttribute("eType").split(" ")[0];
				eTypePath = (e).getAttribute("eType").split(" ")[1];	
			} else {
				eTypeType = "local";
				eTypePath = (e).getAttribute("eType").split(" ")[0];
			}
			
			resFMs += "{\"type\": \"eStructuralFeature\", "
					+ "\"xpath\": \""+DomUtil.getAbsolutePath(e)+"\", "
					+ "\"xpathNamed\": \""+DomUtil.getAbsolutePathNamed(e)+"\", "
					+ "\"name\": \""+((Element)e).getAttribute("name")+"\", "
					+ "\"xsi:type\": \""+((Element)e).getAttribute("xsi:type")+"\", "
					+ "\"eTypePath\": \""+eTypePath+"\", "
					+ "\"eTypeType\": \""+eTypeType+"\""
					+ "},";
			
			// TODO Information required to resolve ??
			Source source = new Source(getFilePath(), DomUtil.getAbsolutePath(e), DomUtil.getAbsolutePathNamed(e));
			Reference r = ReferenceFactory.getReference(Utils.cleanUrlsForJson(eTypePath), source);
			addReference(r);
		}
		resFMs = resFMs.trim();
		if (!resFMs.isBlank())
			resFMs = resFMs.substring(0, resFMs.length() - 1);
		
		resFMs = "\"eStructuralFeatures\": [" + resFMs + "] ";
		return resFMs;
	}
	
	public String getEcoreName() {
		return ecoreName;
	}
	
	public String getEcoreNsPrefix() {
		return ecoreNsPrefix;
	}
	
	public String getEcoreNsURI() {
		return ecoreNsURI;
	}
}
