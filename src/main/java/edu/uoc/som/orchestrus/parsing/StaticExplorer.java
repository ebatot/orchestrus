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
	 * Get XMI elements with an HREF attribute (see {@link #getImportHrefs_Json()}) <br/>
	 * and other elements with interdependencies (ctx.values) (see {@link #getCtxValues_Json()}
	 * 
	 * Collects on the fly references for later processing.
	 * 
	 * (Note: ctx values are added in properties-editor-configurations under suffixed file name "Project.ctx-value")
	 * @return JSON
	 */
	@SuppressWarnings("deprecation")
	public String getInterArtefactReferences_Json() {
		
		String hrefs = getImportHrefs_Json();
		String ctxValues = getCtxValues_Json();

		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		JsonElement el = parser.parse(hrefs);
		JsonElement el2 = parser.parse(ctxValues);
		JsonObject ob = el.getAsJsonObject();
		String contextFileName = config.getProjectName() + ".ctx";
		JsonObject obEditorProperties = ob.getAsJsonObject(config.getPropertiesEditorConfiguration());
		obEditorProperties.add(contextFileName+"-values", el2);
		return gson.toJson(el);
	}
	
	
	public String getImportHrefs_Json() {
		String res ="{";
		int isf = 0;
		for (String sf : Config.getInstance().getContentFoldersFull()) {
			File f = new File(sf);
			res += "\"" + f.getName() + "\": \n";
			
			File[] files = f.listFiles(XMILikeFileFilter.getFilter());
			String domainModelHrefs = "";
			try {
				if (files != null)
					domainModelHrefs = getHrefs_Json(Arrays.asList(files));
				res += domainModelHrefs;
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (++isf < Config.getInstance().getContentFoldersName().size())
				res += ",\n";
		}

		return res + "}";
	}
	
	

	
	public String getCtxValues_Json() {
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
		LOGGER.fine(elts.size() + " elements found in '" + fContext +"'");
		return res;
	}

	private String getHrefs_Json(Collection<File> files) throws SAXException, IOException {
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
		LOGGER.fine(countElts + " elements found in " + files.size() +" files");
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
}
