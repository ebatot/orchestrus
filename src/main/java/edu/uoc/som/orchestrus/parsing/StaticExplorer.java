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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.parsing.spec.ContextFile;
import edu.uoc.som.orchestrus.parsing.spec.EcoreModel;
import edu.uoc.som.orchestrus.parsing.spec.GenModel;
import edu.uoc.som.orchestrus.parsing.spec.PluginFile;
import edu.uoc.som.orchestrus.parsing.utils.DomUtil;
import edu.uoc.som.orchestrus.parsing.utils.XmlException;
import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.ArtefactFactory;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;
import edu.uoc.som.orchestrus.utils.Utils;

public class StaticExplorer {
	public final static Logger LOGGER = Logger.getLogger(StaticExplorer.class.getName());
	public final static Logger LOGGER2 = Logger.getLogger(StaticExplorer.class.getName()+2);
	
	/** Internal temporary name for source file path of references */
	public static final String XMI_SOURCE_PATH_REF = "refSource";
	
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	
	private Config config;

	public StaticExplorer() {
		// TODO Externalize config parameters: project root in CmdLine...
		// TODO Derive config parameters: dependencies, project name, and uris... 
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
	 * and other elements with interdependencies (ctx.values) (see {@link #getJSonForCtxValues()}
	 * 
	 * Collects on the fly references for later processing.
	 * 
	 * (Note: ctx values are added in properties-editor-configurations under suffixed file name "Project.ctx-value")
	 * @return JSON
	 */
	@SuppressWarnings("deprecation")
	public String getInterArtefactReferences_Json() {
		LOGGER.info("Building References - JSon manipulation.");
		JsonParser parser = new JsonParser();
		
		
		// Root created with HREFS references
		String hrefs = getHrefs_Json();
		JsonElement elHrefs = parser.parse(hrefs);
		JsonObject obRoot = elHrefs.getAsJsonObject();
		
		
		// Plugin.xml references
		String pluginXmlRefs = getJSonForPluginXMLRefs();
		JsonElement elPlugin = parser.parse(pluginXmlRefs);
		obRoot.add(Config.PLUGIN_XML_FILENAME, elPlugin);
		
		// uml-profile/*.ecore definition and references
		String ecoreFileName = config.getProjectName() + ".ecore";
		String ecoreRefs = getJSonForEcoreRefs();
		JsonArray obEcore = obRoot.getAsJsonObject(Config.getUmlprofilesfolder()).getAsJsonArray(ecoreFileName);
		JsonElement elEcore = parser.parse(ecoreRefs);
		obEcore.add(elEcore);
		
		// TODO To lower ?! always ? or only for GlossaryML project..
		String genmodelFileName = config.getProjectName().toLowerCase() + ".genmodel";
		String genmodRefs = getJsonForGenmodelRefs();
		JsonArray obGenmod = obRoot.getAsJsonObject(Config.getUmlprofilesfolder()).getAsJsonArray(genmodelFileName);
		JsonElement elGenmod = parser.parse(genmodRefs);
		obGenmod.add(elGenmod);

		
		// Added extra context references
		String contextFileName = config.getProjectName() + ".ctx";
		String ctxValues = getJSonForCtxValues();
		JsonElement elCtx = parser.parse(ctxValues);
		JsonObject obEditorProperties = obRoot.getAsJsonObject(Config.getPropertiesEditorConfiguration());
		obEditorProperties.add(contextFileName+"-values", elCtx);
		
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
					domainModelHrefs = getJSonForHrefsFromFiles(Arrays.asList(files));
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
	
	private String getJSonForHrefsFromFiles(Collection<File> files) throws SAXException, IOException {
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
		LOGGER.fine(countElts + " references found in " + files.size() + " files");
		return res;
	}

	private String getJSonForCtxValues() {
		String res = "";
		File fContext = new File(config.getPropertiesEditorConfigurationContext());
		List<Element> elts = Collections.emptyList();
		try {
			elts = getContextValuElementsFromFile(builder, fContext);
			ContextFile gm = new ContextFile(elts);
			String ctxvalues = gm.getHRefJSon();
			res += ctxvalues;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		res = Utils.cleanJSon(res);
		LOGGER.fine(elts.size() + " references found in '" + fContext + "'");
		return res;
	}

	/**
	 * Extract references from GenModel file. <br/>
	 * 
	 * <ul>
	 * <li>genModelDeclaration</li>
	 * <ul>
	 * <li>modelDirectory</li>
	 * 
	 * <li>modelPluginID</li>
	 * <li>modelName</li>
	 * <li>rootExtendsClass</li>
	 * <li>importerID</li>
	 * <li>usedGenPackages (list)</li>
	 * <ul>
	 * <li>Foreign models</li>
	 * <li>(genFeatures ? not yet implemented)</li>
	 * <ul>
	 * 
	 * @return JSON
	 */
	private String getJsonForGenmodelRefs() {
		File f = new File(config.getGenmodelFilePath());
		String res = "";

		try {
			Document doc = builder.parse(f);
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//*[@modelName and @modelPluginID and @modelDirectory]";
			Node genModelRootNode = null;
			NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				if(genModelRootNode == null)
					genModelRootNode = nNode;
				else
					throw new IllegalAccessError("Only one genModel node (root) expected in genmodel file.");
				LOGGER2.finer(res);
			}

			List<Element> foreignModels = new ArrayList<>();
			expression = "//foreignModel";
			nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				foreignModels.add((Element)nNode);
			}
			
			GenModel gm = new GenModel((Element)genModelRootNode, foreignModels);
			
//			Set<Reference> refs = ReferenceFactory.getReferences(gm);
//			for (Reference reference : refs) {
//				System.out.println(reference.getSources());
//			}
			
			res += gm.getHRefJSon();
			
			return res;
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}



	/**
	 * Extract references from Ecore project file (see {@link Config#getEcoreFilePath()}) <br/>
	 * <ul>
	 * 	<li> epackageDeclaration values, </li>
	 * 	<li> eStructuralFeature types (intra and inter model dependencies) </li>
	 * </ul>
	 * @return
	 */
	private String getJSonForEcoreRefs() {
		File f = new File(config.getEcoreFilePath());
		String res = "";
		try {
			Document doc = builder.parse(f);
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//*[@name and @nsURI and @nsPrefix]";
			Element rootNode = null;
			NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				rootNode = (Element)nNode;
				if(i >= 1)
					throw new IllegalAccessError("Ecore file should only have one root node.");
				LOGGER2.finer(res);
			}
			
			List<Element> esfElts = new ArrayList<>();
			expression = "//eStructuralFeatures";
			nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
			for (int i = 0; i < nodeList2.getLength(); i++) {
				Node nNode = nodeList2.item(i);
				esfElts.add((Element)nNode);
				LOGGER2.finer(res);
			}
			
			EcoreModel ecoreModel = new EcoreModel(rootNode, esfElts);
			res = ecoreModel.getHRefJSon();

			return res;
		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * 
	 * @return JSON containing references found in /plugin.xml file
	 */
	private String getJSonForPluginXMLRefs() {
		File f = Config.getInstance().getConfigFile(Config.PLUGIN_XML_FILENAME);
		try {
			Document doc = builder.parse(f);
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			PluginFile pf = new PluginFile(doc, xPath, f);
			return pf.getHRefJSon();

		} catch (SAXException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return "";
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
			String sourceFile = element.getAttributes().getNamedItem(XMI_SOURCE_PATH_REF).getTextContent();
			String sourceInnerPath = DomUtil.getAbsolutePath(element);
			String sourceInnerPathNamed = DomUtil.getAbsolutePathNamed(element);
			Source source = new Source(sourceFile, sourceInnerPath, sourceInnerPathNamed);
			
			String cleanhref = element.getAttributes().getNamedItem("href").getTextContent();
			
			/*
			 * Build and resolve references
			 */
			Reference r = ReferenceFactory.getReference(cleanhref, source);
//			addReferenceSourceReversed(sourceFile, r);
			
			cleanhref = Utils.cleanUrlsForJson(r.getHREF());
			
			Node elt = element.getAttributes().getNamedItem("xmi:type");
			String xmitype = elt != null ? "\n \"xmi:type\": \""+elt.getTextContent()+"\", ":"";

			sb.append("{"
//					+ "\"sourceFile\": \""+Utils.cleanUrlsForJson(sourceFile)+"\", "
					+ "\"xpath\": \""+sourceInnerPath+"\", "
					+ "\n \"xpathNamed\": \""+sourceInnerPathNamed+"\", "
					+ xmitype
					+ "\n \"href\": \"" + cleanhref +"\""
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
			   ((Element)nNode).setAttribute(XMI_SOURCE_PATH_REF, xmlFile.getAbsolutePath());
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
			   ((Element)nNode).setAttribute(XMI_SOURCE_PATH_REF, xmlFile.getAbsolutePath());
			   LOGGER.finest(" ->  "+((Element)nNode).getAttribute("value"));
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return elts;
	}


	public static Set<Source> getSourceFiles() {
		return ReferenceFactory.getReferencesSourcesReversed().keySet();
	}
	
	/**
	 * 
	 * @return For each source file, its references.
	 */
	public static HashMap<Source, ArrayList<Reference>> getReferencesSourcesReversed() {
		return ReferenceFactory.getReferencesSourcesReversed();
	}


	/**
	 * NOT IMPLEMENTED !
	 * @param f
	 * @param name
	 * @return
	 */
	public Object getElementFromFile(File f, String name) {
		// TODO get element from xmi ID !
	
		try {
			Document doc = builder.parse(f);
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//details[@key and @value]";
			List<Element> elts = new ArrayList<>();
				NodeList nodeList2 = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
				for (int i = 0; i < nodeList2.getLength(); i++) {
				   Node nNode = nodeList2.item(i);
				}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new IllegalAccessError("NOT IMPLEMENTED !");
	}


	/**
	 * Warning. NOT IMPLEMENTED !
	 * Extract elements from XMI files using their xmi:ID.
	 */
	public void resolveElementIDs() {
		for (Artefact a : ArtefactFactory.subsetsArtefactsByType(ArtefactTypeFactory.ELEMENT_ARTEFACT)) {
			String name = a.getName(); 
			if(a.isResolves()) {
				if(name.length() == 23 && name.startsWith("_")) {
					File f = new File(a.getLocation() + File.separator + a.getName());
					
					Object elt = getElementFromFile(f, name);
					
				}
			}
		}
		throw new IllegalAccessError("NOT IMPLEMENTED !");
	}
}
	