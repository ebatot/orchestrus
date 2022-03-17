package edu.uoc.som.orchestrus.parsing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.parsing.utils.DomUtil;
import edu.uoc.som.orchestrus.utils.Utils;

public class GenModel {

	public static String getFilePath() {
		return Config.getInstance().getGenmodelFilePath();
	}
	
	private String modelDirectory;
	private String modelPluginID;
	private String modelName;
	private String rootExtendsClass;
	private String importerID;
	private String[] usedGenPackages;
	
	Element rootNode;
	List<Element> foreignModels;
	
	Set<Reference> references;
	
	
	public GenModel(Element rootNode, List<Element> foreignModels) {
		this(((Element)rootNode).getAttribute("modelDirectory"), 
			((Element)rootNode).getAttribute("modelPluginID"), 
			((Element)rootNode).getAttribute("modelName"), 
			((Element)rootNode).getAttribute("rootExtendsClass"), 
			((Element)rootNode).getAttribute("importerID"), 
			((Element) rootNode).getAttribute("usedGenPackages").split(" "));
		this.foreignModels = foreignModels;
		this.rootNode = rootNode;
	}
	
	
	private GenModel(String modelDirectory, String modelPluginID, String modelName, String rootExtendsClass,
			String importerID, String[] usedGenPackages) {
		this.modelDirectory = modelDirectory;
		this.modelPluginID = modelPluginID;
		this.modelName = modelName;
		this.rootExtendsClass = rootExtendsClass;
		this.importerID = importerID;
		this.usedGenPackages = usedGenPackages;
		
		references = new HashSet<>();
	}

	public String getHRefJSon() {
		String res = "";
		res += "\"root\": {"
					+ "\"modelDirectory\": \""+modelDirectory+"\", "
					+ "\"modelPluginID\": \""+modelPluginID+"\", "
					+ "\"modelName\": \""+modelName+"\", "
					+ "\"rootExtendsClass\": \""+rootExtendsClass+"\", "
					+ "\"importerID\": \""+importerID+"\", "
					+ "\"usedGenPackages\": "+getUsegGenPackagesAsJSonArray() + "}"
				+ ",\n";
		
		
		Source source = new Source(getFilePath(), DomUtil.getAbsolutePath(rootNode), DomUtil.getAbsolutePathNamed(rootNode));
		
		Reference r = ReferenceFactory.getReference(Utils.cleanUrlsForJson(modelDirectory), source);
		Reference r2 = ReferenceFactory.getReference(Utils.cleanUrlsForJson(modelPluginID), source);
		Reference r3 = ReferenceFactory.getReference(Utils.cleanUrlsForJson(rootExtendsClass), source);
		references.add(r);
		references.add(r2);
		references.add(r3);
		String resFMs = getJSonForForeignModels();
		
		res = res + resFMs;
		return "{" + res + "}";
	}


	/**
	 * Return a JSon array with foreign model references. <br/>
	 * @return
	 */
	private String getJSonForForeignModels() {
		String resFMs = "";
		for (Element e : foreignModels) {
			resFMs += "\"" + ((Element) e).getTextContent() + "\",\n";
			Source source = new Source(getFilePath(), DomUtil.getAbsolutePath(e), DomUtil.getAbsolutePathNamed(e));
			// TODO Information required to resolve ??
			Reference r = ReferenceFactory.getReference(Utils.cleanUrlsForJson(e.getTextContent()), source);
			System.out.println(r.isResolved() + ": "+r+":"+r.getHREF()+"    "+r.getSources());
			references.add(r);
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
