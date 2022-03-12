package edu.uoc.som.orchestrus.parsing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.parsing.refmanager.Reference;
import edu.uoc.som.orchestrus.parsing.refmanager.ReferenceFactory;
import edu.uoc.som.orchestrus.parsing.utils.DomUtil;
import edu.uoc.som.orchestrus.parsing.utils.XmlException;
import edu.uoc.som.orchestrus.utils.Utils;

public class StaticExplorer {
	public final static Logger LOGGER = Logger.getLogger(StaticExplorer.class.getName());
	public final static Logger LOGGER2 = Logger.getLogger(StaticExplorer.class.getName()+2);
	private static final String XMI_SOURCE_PATH = "refSource";
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private Config config;

	public StaticExplorer() {
		this.config = Config.getInstance();
		try {
			factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (ParserConfigurationException e) {
			throw new XmlException("Could not load the XML factory. (DEV)");
		}
	}
	
	
	/**
	 * Get XMI elements with an HREF attribute (see {@link #getHrefs_Json()}) <br/>
	 * and other elements with interdependencies (ctx.values) (see {@link #getCtxValues_Json()}
	 * 
	 * Collects on the fly references for later processing.
	 * 
	 * (Note: ctx values are added in properties-editor-configurations under suffixed file name "Project.ctx-value")
	 * @return JSON
	 */
	@SuppressWarnings("deprecation")
	public String getInterArtefactReferences_Json() {
		JsonParser parser = new JsonParser();
		
		// Root created with HREFS references
		String hrefs = getHrefs_Json();
		JsonElement elHrefs = parser.parse(hrefs);
		
		// TODO Check uml-profile/*.ecore, *.genmodel
		//TODO
		
		
		// Added extra context references
		String ctxValues = getCtxValues_Json();
		JsonElement elCtx = parser.parse(ctxValues);
		JsonObject ob = elHrefs.getAsJsonObject();
		String contextFileName = config.getProjectName() + ".ctx";
		JsonObject obEditorProperties = ob.getAsJsonObject(config.getPropertiesEditorConfiguration());
		obEditorProperties.add(contextFileName+"-values", elCtx);
		
		// Plugin.xml references
		String pluginXmlRefs = extractPluginXMLRefs();
		JsonElement elPlugin = parser.parse(pluginXmlRefs);
		ob.add(Config.PLUGIN_XML_FILENAME, elPlugin);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		
		
		return gson.toJson(elHrefs);
	}
	
	
	private String getHrefs_Json() {
		String res ="{";
		int isf = 0;
		for (String sf : Config.getInstance().getContentFoldersFull()) {
			File f = new File(sf);
			res += "\"" + f.getName() + "\": \n";
			
			File[] files = f.listFiles(XMILikeFileFilter.getFilter());
			String domainModelHrefs = "";
			try {
				if (files != null)
					domainModelHrefs = getHrefsFromFiles_Json(Arrays.asList(files));
				res += domainModelHrefs;
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (++isf < Config.getInstance().getContentFoldersName().size())
				res += ",\n";
		}

		return res + "}";
	}
	
	private String getHrefsFromFiles_Json(Collection<File> files) throws SAXException, IOException {
		String res = "{";
		int i = 0;
		int countElts  = 0;
		for (File f : files) {
			List<Element> elts = getHREFElementsFromFile(builder, f);
			countElts += elts.size();
			String hrefs = getJSonForHrefs(elts) ;
			res += "\n\""+f.getName()+"\": "+hrefs + (++i < files.size()?",":"");
		}
		res += "}";
		LOGGER.fine(countElts + " references found in " + files.size() +" files");
		return res;
	}
	
	private String getCtxValues_Json() {
		String res = "";
		File fContext = new File(config.getPropertiesEditorConfigurationContext());
		List<Element> elts = Collections.emptyList();
		try {
			elts = getContextValuElementsFromFile(builder, fContext);
			String ctxvalues = getJSonForCtxValues(elts);
			res += ctxvalues ;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		res = Utils.cleanJSon(res);
		LOGGER.fine(elts.size() + " references found in '" + fContext +"'");
		return res;
	}
	

	/**
	 * 
	 * @return JSON containing references found in /plugin.xml file
	 */
	private  String extractPluginXMLRefs() {
		File f = Config.getInstance().getConfigFile(Config.PLUGIN_XML_FILENAME);
		try {
			Document doc = builder.parse(f);
			XPath xPath = XPathFactory.newInstance().newXPath();
			return getProfileExtensionPoints(doc, xPath, f);
			
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private String getProfileExtensionPoints(Document doc, XPath xPath, File f) throws XPathExpressionException {
		String resElt = "{";

		String expression = "//extension[@point]";
		NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
		int i = 0;
		for (i = 0; i < nodeList2.getLength(); i++) {
			Node nNode = nodeList2.item(i);

			/*
			 * 
			 * 
			 * 
			 * TODO
			 * Here we are. One case for each extension point type.
			 *  -> references "profile-extension-points"
			 *     - element name, attributes path, model, etc.
			 * 
			 */

			
			String point = ((Element)nNode).getAttribute("point");
			String value = "";
			String name = ((Element)nNode).getAttribute("name");
			String path = ((Element)nNode).getAttribute("path");
			
			
			value = nNode.toString();
			
			switch (ExtensionPoint.getExtensionPointFromName(point)) {
			case profile:
				value = getProfileExtension(doc, xPath, f);
				break;
			case generated_package:
			case architecture:
			case infra:
			case conetxt:
			case factory:
				value = "{\"TODO\": \"TODO\"}";
				//TODO
				break;
			default:
				throw new IllegalArgumentException("Should never get there, unrecognized extension point: "+point);
			}
			
			
			LOGGER2.finest("\""+point+"\": "+value+"");
			resElt += "\""+point+"\": "+value+",\n";
		}
		if(i>0) 
			resElt = resElt.substring(0, resElt.trim().length()-1);
		return resElt + "}";
	}
	static enum ExtensionPoint {
		profile("org.eclipse.papyrus.uml.extensionpoints.UMLProfile"),
		generated_package("org.eclipse.emf.ecore.generated_package"),
		architecture("org.eclipse.papyrus.infra.architecture.models"),
		infra("org.eclipse.papyrus.infra.newchild"),
		conetxt("org.eclipse.papyrus.infra.properties.contexts"),
		factory("org.eclipse.emf.ecore.factory_override");

		ExtensionPoint(String name) {
			this.name = name;
		}
		
		static ExtensionPoint getExtensionPointFromName(String name) {
			for (ExtensionPoint ep : ExtensionPoint.values()) {
				if(ep.getName().equals(name))
					return ep;
			}
			return null;
		}
		
		
		String name;
		public String getName() {
			return name;
		}
	}

	private String getProfileExtension(Document doc, XPath xPath, File f) {
		String res = "";
		String expression = "/plugin/extension/profile";
		try {
			NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				if ( i > 1 ) {
					throw new IllegalArgumentException("Should never get there, only one extension profile by "+Config.PLUGIN_XML_FILENAME+" file.");
				}
				Node nNode = nodeList2.item(i);
				LOGGER2.finest("{ \"name\": \""+((Element)nNode).getAttribute("name")+"\", \"path\": \""+((Element)nNode).getAttribute("path")+"\"}");
				res += "{ \"name\": \""+((Element)nNode).getAttribute("name")+"\", \"path\": \""+((Element)nNode).getAttribute("path")+"\"}";
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Source to reference
	 */
	private static HashMap<String, ArrayList<Reference>> referencesSourcesReversed = new HashMap<>();
	
	/**
	 * Adds a source-reference to the stock.
	 * @param sourceFile
	 * @param r
	 */
	private static void addReferenceSourceReversed(String sourceFile, Reference r) {
		if (!referencesSourcesReversed.keySet().contains(sourceFile))
			referencesSourcesReversed.put(sourceFile, new ArrayList<Reference>());
		referencesSourcesReversed.get(sourceFile).add(r);
	}
	
	/**
	 * For each element found with a "href" attribute, extract the following information (JSON syntax)
	 * [ elt1: { XPath-to-Elt ; XPath neamed ; xmi:type ; href }
	 * 
	 * @param elts
	 * @return JSON
	 */
	private static String getJSonForHrefs(List<Element> elts) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		int i = 0;
		for (Element element : elts) {
			String sourceFile = element.getAttributes().getNamedItem(XMI_SOURCE_PATH).getTextContent();
			String cleanhref = element.getAttributes().getNamedItem("href").getTextContent();
			
			/*
			 * Build and resolve references
			 */
			Reference r = ReferenceFactory.getReference(cleanhref, sourceFile);
			addReferenceSourceReversed(sourceFile, r);
			
			cleanhref = r.getHREF().replaceAll("'", "\\\\'");
			cleanhref = cleanhref.replaceAll("\"", "\\\\\"");
			
			cleanhref = cleanhref.replace("\\", "/");
			
			Node elt = element.getAttributes().getNamedItem("xmi:type");
			String xmitype = elt != null ? "\n \"xmi:type\": \""+elt.getTextContent()+"\", ":"";

			sb.append("{"
					+ "\"xpath\": \""+DomUtil.getAbsolutePath((Element)element)+"\", "
					+ "\n \"xpathNamed\": \""+DomUtil.getAbsolutePathNamed((Element)element)+"\", "
					+ xmitype
					+ "\n \"href\": \"" + cleanhref +"\""
					+ "}");
			if(++i < elts.size()) sb.append(",");
			sb.append("\n");
		}
		sb.append("]");
		return sb.toString();
	}


	
	/**
	 * For each element, extract the following information (JSON syntax)
	 * [ elt1: {XPath, Xpath with @names, [id,] key, value }]
	 * <br/>
	 * 
	 * Location are put with '/' in Json.
	 * 
	 * @param elts
	 * @return JSON
	 */
	private static String getJSonForCtxValues(List<Element> elts) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		int i = 0;
		for (Element element : elts) {
			String sourceFile = element.getAttributes().getNamedItem(XMI_SOURCE_PATH).getTextContent();
			String cleanvalue = element.getAttributes().getNamedItem("value").getTextContent();
			
			Reference r = ReferenceFactory.getReference(cleanvalue, sourceFile);
			addReferenceSourceReversed(sourceFile, r);

			cleanvalue = r.getHREF().replaceAll("'", "\\\\'");
			cleanvalue = cleanvalue.replaceAll("\"", "\\\\\"");
			
			cleanvalue = cleanvalue.replace("\\", "/");
			
			String key = element.getAttributes().getNamedItem("key").getTextContent();
			
			Node elt = element.getAttributes().getNamedItem("xmi:id");
			String xmiid = elt != null ? "\n \"xmi:id\": \""+elt.getTextContent()+"\", ":"";
			sb.append("{"
					+ "\"xpath\": \""+DomUtil.getAbsolutePath((Element)element)+"\", "
					+ "\n \"xpathNamed\": \""+DomUtil.getAbsolutePathNamed((Element)element)+"\", "
					+ xmiid
					+ "\n \"key\": \"" + key +"\","
					+ "\n \"value\": \"" + cleanvalue +"\""
					+ "}");
			if(++i < elts.size()) sb.append(",");
			sb.append("\n");
		}
		sb.append("]");
		return sb.toString();
	}


	private static List<Element> getHREFElementsFromFile(DocumentBuilder builder, File xmlFile)
			throws SAXException, IOException {
		LOGGER.finest("XMI file: "+xmlFile.getAbsolutePath());
		Document doc = builder.parse(xmlFile);
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//*[@href]";
		List<Element> elts = new ArrayList<>();
		try {
			NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
			   Node nNode = nodeList2.item(i);
			   elts.add((Element)nNode);
			   //Injects sourceFile in Element.
			   ((Element)nNode).setAttribute(XMI_SOURCE_PATH, xmlFile.getAbsolutePath());
			   LOGGER.finest(" ->  "+((Element)nNode).getAttribute("href"));
			   
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return elts;
	}
	
	private static List<Element> getContextValuElementsFromFile(DocumentBuilder builder, File xmlFile)
			throws SAXException, IOException {
		LOGGER.finest("XMI file: "+xmlFile.getAbsolutePath());
		Document doc = builder.parse(xmlFile);
		
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//details[@key and @value]";
		List<Element> elts = new ArrayList<>();
		try {
			NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
			   Node nNode = nodeList2.item(i);
			   elts.add((Element)nNode);
			   ((Element)nNode).setAttribute(XMI_SOURCE_PATH, xmlFile.getAbsolutePath());
			   LOGGER.finest(" ->  "+((Element)nNode).getAttribute("value"));
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return elts;
	}


	public static Set<String> getSourceFiles() {
		return referencesSourcesReversed.keySet();
	}
	
	/**
	 * 
	 * @return For each source file, its references.
	 */
	public static HashMap<String, ArrayList<Reference>> getReferencesSourcesReversed() {
		return referencesSourcesReversed;
	}
}
