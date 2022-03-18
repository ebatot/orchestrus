package edu.uoc.som.orchestrus.parsing.spec;

import java.util.List;

import org.w3c.dom.Element;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.parsing.Reference;
import edu.uoc.som.orchestrus.parsing.ReferenceFactory;
import edu.uoc.som.orchestrus.parsing.Source;
import edu.uoc.som.orchestrus.parsing.SpecificFileReferenceExtractor;
import edu.uoc.som.orchestrus.parsing.utils.DomUtil;
import edu.uoc.som.orchestrus.utils.Utils;

public class EcoreModel extends SpecificFileReferenceExtractor{
	public String getFilePath() {
		return Config.getInstance().getEcoreFilePath();
	}

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
	
	
	private EcoreModel(String name, String nsURI, String nsPrefix) {
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
}
