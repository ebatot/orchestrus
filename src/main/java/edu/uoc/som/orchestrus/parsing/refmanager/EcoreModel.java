package edu.uoc.som.orchestrus.parsing.refmanager;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.uoc.som.orchestrus.parsing.utils.DomUtil;

public class EcoreModel {

	private String name;
	private String nsURI;
	private String nsPrefix;
	
	Element rootNode;
	List<Element> eStructuralFeatures;
	
	
	public EcoreModel(Element rootNode, List<Element> eStructuralFeatures) {
		this((rootNode).getAttribute("name"), 
			(rootNode).getAttribute("nsURI"), 
			(rootNode).getAttribute("nsPrefix"));
		this.eStructuralFeatures = eStructuralFeatures;
		this.rootNode = rootNode;
	}
	
	
	public EcoreModel(String name, String nsURI, String nsPrefix) {
		this.name = name;
		this.nsURI = nsURI;
		this.nsPrefix = nsPrefix;
	}

	public String getHRefJSon() {
		String res = "";
		res += "\"root\": {"
					+ "\"name\": \""+name+"\", "
					+ "\"nsURI\": \""+nsURI+"\", "
					+ "\"nsPrefix\": \""+nsPrefix+"\""
					+ "}"
				+ ",\n";
		
		String resFMs = getJSonForEStructuralFeatures();
		
		res = res + resFMs;
		return "{" + res + "}";
	}


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
					+ "\n \"xpathNamed\": \""+DomUtil.getAbsolutePathNamed(e)+"\", "
				+ "\"xpath\": \""+DomUtil.getAbsolutePath((Element)e)+"\", "
				+ "\"name\": \""+((Element)e).getAttribute("name")+"\", "
				+ "\"xsi:type\": \""+((Element)e).getAttribute("xsi:type")+"\", "
				+ "\"eTypePath\": \""+eTypePath+"\", "
				+ "\"eTypeType\": \""+eTypeType+"\"},";

		}
		resFMs = resFMs.trim();
		if (!resFMs.isBlank())
			resFMs = resFMs.substring(0, resFMs.length() - 1);
		
		resFMs = "\"eStructuralFeatures\": [" + resFMs + "] ";
		return resFMs;
	}
}
