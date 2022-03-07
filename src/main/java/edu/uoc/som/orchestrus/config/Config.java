package edu.uoc.som.orchestrus.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;

public class Config {
	public final static Logger LOGGER = Logger.getLogger(Config.class.getName());

	
	
	/*
	 * Hard coded build.properties-like
	 */
	static String architectureFramework 	= "architecture-framework";
	static String elementTypeConfiguration 		= "element-type-configurations";
	static String paletteConfigurationsFolder 	= "palette-configurations";
	static String propertiesEditorConfiguration = "properties-editor-configurations";
	static String specificationModelsFolder 	= "specification-models";
	static String tabularEditorConfiguration 	= "tabular-editors-configurations";
	static String umlProfilesFolder 			= "uml-profiles";
	static String rootFolder 					= ".";
	
	
	List<String> contentFolders = Arrays.asList(
			architectureFramework, 
			elementTypeConfiguration,
			paletteConfigurationsFolder, 
			propertiesEditorConfiguration, 
			specificationModelsFolder,
			tabularEditorConfiguration, 
			umlProfilesFolder,
			rootFolder);
	
	static String TOOL_REQ_MODEL_SUFFIX = "_ToolReqModel";
	static String DOMAIN_MODEL_SUFFIX = ".domainmodel";
	static String LANGUAGE_REQ_SUFFIX = ".LanguageReqModel";
	
	
	static Config instance;
	public static Config getInstance() {
		if(instance == null)
			instance = new Config();
		return instance;
	}
	
	ArtefactTypeFactory atFactory;
	
	HashMap<String, String> values = new HashMap<>();
	/** Name -> Artefact */
	HashMap<String, Artefact> artefacts = new HashMap<>();
	
	String projectRoot = "R:\\Coding\\Git\\orchestrus\\data\\GlossaryML-ReferenceML";
	String project = "com.cea.papyrus.glossary";
	String projectName = "GlossaryML";
	List<String> projectDependencies = Arrays.asList("com.cea.papyrus.referencemanagement");

	
	private Config() {
		atFactory = ArtefactTypeFactory.getInstance();
		
		values.put("projectRoot", projectRoot);
		values.put("project", project);
		values.put("projectDependencies", projectDependencies.toString());
		values.put("projectName", projectName);

		
		LOGGER.info(""
				+ projectName +"\n"
				+ "  - project     : " + project + "\n"
	  		  	+ "  - dependencies: "+ projectDependencies.toString());
		
		boolean check = checkFolderNames();
		if(!check) {
			LOGGER.severe("Some folders were not found. Please check configuration.\nExit with errors.");
			System.exit(1);
		} else {
			LOGGER.fine("Folders correct.");
		}
		
		initArtefactTypes();

		/*
		 * Artefacts typing
		 */
		artefacts.put("notationFile", new Artefact("notationFile", atFactory.getType("xmiFile")));
		artefacts.put("umlFile", new Artefact("umlFile", atFactory.getType("xmiFile")));
		artefacts.put("diFile", new Artefact("diFile", atFactory.getType("xmiFile")));
		
		/*
		 * Fragments bags: elements (nodes), labels (leafs)
		 */
		HashMap<String, Artefact> xmlElts = new HashMap<>();
		HashMap<String, Artefact> labels = new HashMap<>();
		
		
		/*
		 * 
		 * BELOW - MOVE - To ArtefactFactory and LinkFactory ?!
		 * 
		 */
		
		
		// Fragments instanciation 
		//  -> rebuild from XPath patterns
		//  -> contextualizes with file/artefact location
		Artefact eltXmiType = new Artefact("xmiType", atFactory.getType("xmlElt"));
		Artefact labelHref = new Artefact("labelHref", atFactory.getType("label"));
		
		// Fragments connexion (Links instanciation)
		//  - Which fragment connects -directly- with who, and -derively- with who
		/*
		 *   F1 - F2 - F3 
		 *    -> F1 connects directly to F2
		 *    -> F1 connects derively to F3 (derived attribute, transitive closure with sub fragments)
		 */
	}

	private boolean checkFolderNames() {
		boolean res = true;
		//Main /
		res &= checkExistsAndDirectory(getProjectRoot());
		//Project folder
		res &= checkExistsAndDirectory(getProjectFull());
		
		//Project denpendencies
		for (String pd : getProjectDependenciesFull()) 
			res &= checkExistsAndDirectory(pd);
		
		//sub folders
		for (String sf : contentFolders) 
			res &= checkExistsAndDirectory(getProjectFull() + File.separator + sf);
		
		
		return res;
	}

	private boolean checkExistsAndDirectory(String path) {
		boolean res = true;
		File f = new File(path);
		if(!f.exists() || !f.isDirectory()) {
			res = false;
			LOGGER.warning("Could not find folder: "+f.getAbsolutePath());
		}
		return res;
	}

	private void initArtefactTypes() {
		atFactory.addType("xmiFile");
		atFactory.addType("xmlElt");
		atFactory.addType("label");
		LOGGER.fine(""+atFactory.getTypesValues());
	}
	
	public String getValue(String key) {
		return values.get(key);
	}
	
	public List<String> getContentFoldersName() {
		return contentFolders;
	}
	
	public List<String> getContentFoldersFull() {
		ArrayList<String> res = new ArrayList<>(getContentFoldersName().size());
		for (String s : getContentFoldersName()) {
			res.add(getProjectFull() + File.separator + s);
		}
		return res;
	}
	
	
	public HashMap<String, Artefact> getArtefacts(){
		return artefacts;
	}
	
	
	public String getProjectRoot() {
		return projectRoot;
	}
	
	public String getProject() {
		return project;
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getProjectFull() {
		return projectRoot+ File.separator+ project;
	}
	
	public List<String> getProjectDependencies() {
		return projectDependencies;
	}

	public List<String> getProjectDependenciesFull() {
		List<String> res = new ArrayList<>(projectDependencies.size());
		for (String pd : projectDependencies) {
			res.add(projectRoot + File.separator + pd);
		}
		return res;
	}

	public String getPropertiesEditorConfiguration() {
		return propertiesEditorConfiguration;
	}
	public String getPropertiesEditorConfigurationContext() {
		return getProjectFull() + File.separator + propertiesEditorConfiguration + File.separator + getProjectName()+".ctx";
	}

	public String getSpecificationModelsFolderFull() {
		return getProjectFull() + File.separator + specificationModelsFolder;
	}
	
	public String getSpecificationModelsFolder() {
		return specificationModelsFolder;
	}

	public String getUmlProfilesFolderFull() {
		return getProjectFull() + File.separator + umlProfilesFolder;
	}

	public String getPaletteConfigurationsFolderFull() {
		return getProjectFull() + File.separator + paletteConfigurationsFolder;
	}

	/**
	 * @deprecated
	 * @return
	 */
	public HashMap<String, String> getUmlProfileFiles() {
		HashMap<String, String> res = new HashMap<>(3);
		res.put("uml", getUmlProfilesFolderFull() + File.separator + getProjectName() + ".profile.uml");
		res.put("di", getUmlProfilesFolderFull() + File.separator + getProjectName() + ".profile.di");
		res.put("notation", getUmlProfilesFolderFull() + File.separator + getProjectName() +  ".profile.notation");
		res.put("ecore", getUmlProfilesFolderFull() + File.separator + getProjectName() + ".ecore");
		res.put("genmodel", getUmlProfilesFolderFull() + File.separator + getProjectName().toLowerCase() + ".genmodel");
		return res;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public HashMap<String, String> getToolReqModelFiles(){
		HashMap<String, String> res = new HashMap<>(3);
		res.put("uml", getSpecificationModelsFolderFull() + File.separator + getProjectName() + TOOL_REQ_MODEL_SUFFIX + ".uml");
		res.put("di", getSpecificationModelsFolderFull() + File.separator + getProjectName() + TOOL_REQ_MODEL_SUFFIX + ".di");
		res.put("notation", getSpecificationModelsFolderFull() + File.separator + getProjectName() + TOOL_REQ_MODEL_SUFFIX + ".notation");
		return res;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public HashMap<String, String> getDomainModelFiles(){
		HashMap<String, String> res = new HashMap<>(3);
		res.put("uml", getSpecificationModelsFolderFull() + File.separator + getProjectName() + DOMAIN_MODEL_SUFFIX + ".uml");
		res.put("di", getSpecificationModelsFolderFull() + File.separator + getProjectName() + DOMAIN_MODEL_SUFFIX + ".di");
		res.put("notation", getSpecificationModelsFolderFull() + File.separator + getProjectName() + DOMAIN_MODEL_SUFFIX + ".notation");
		return res;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public HashMap<String, String> getLanguageReqModelFiles(){
		HashMap<String, String> res = new HashMap<>(3);
		res.put("uml", getSpecificationModelsFolderFull() + File.separator + getProjectName() + LANGUAGE_REQ_SUFFIX + ".uml");
		res.put("di", getSpecificationModelsFolderFull() + File.separator + getProjectName() + LANGUAGE_REQ_SUFFIX + ".di");
		res.put("notation", getSpecificationModelsFolderFull() + File.separator + getProjectName() + LANGUAGE_REQ_SUFFIX + ".notation");
		return res;
		
	}
}
