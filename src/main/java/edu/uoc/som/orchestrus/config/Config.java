package edu.uoc.som.orchestrus.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.parsing.Tests;
import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;

public class Config {
	public final static Logger LOGGER = Logger.getLogger(Config.class.getName());

	static String specificationModelsFolder 	= "specification-models";
	static String umlProfilesFolder 				= "uml-profiles";
	static String paletteConfigurationsFolder 	= "palette-configurations";
	static List<String> subFolders = Arrays.asList(
			specificationModelsFolder, 
			umlProfilesFolder, 
			paletteConfigurationsFolder);

	
	static String TOOL_REQ_MODEL_SUFFIX = "_ToolReqModel";
	static String DOMAIN_MODEL_SUFFIX = ".domainmodel";
	static String LANGUAGE_REQ_SUFFIX = ".LanguageReqModel";
	
	
	
	
	ArtefactTypeFactory atFactory;
	
	HashMap<String, String> values = new HashMap<>();
	/** Name -> Artefact */
	HashMap<String, Artefact> artefacts = new HashMap<>();
	
	String projectRoot = "R:\\Coding\\Git\\orchestrus\\data\\GlossaryML-ReferenceML";
	String project = "com.cea.papyrus.glossary";
	String projectName = "GlossaryML";
	List<String> projectDependencies = Arrays.asList("com.cea.papyrus.referencemanagement");

	
	public Config() {
		atFactory = ArtefactTypeFactory.getInstance();
		
		values.put("projectRoot", projectRoot);
		values.put("project", project);
		values.put("projectDependencies", projectDependencies.toString());
		values.put("projectName", projectName);
		
		boolean check = checkFolderNames();
		if(!check) {
			LOGGER.severe("Some folders were not found. Please check configuration.\nExit with errors.");
			System.exit(1);
		} else {
			LOGGER.info("Folders correct.");
		}
		
		initArtefactTypes();

		artefacts.put("notationFile", new Artefact("notationFile", atFactory.getType("xmiFile")));
		artefacts.put("umlFile", new Artefact("umlFile", atFactory.getType("xmiFile")));
		artefacts.put("diFile", new Artefact("diFile", atFactory.getType("xmiFile")));
		
		HashMap<String, Artefact> xmlElts = new HashMap<>();
		Artefact eltXmiType = new Artefact("xmiType", atFactory.getType("xmlElt"));
		
		HashMap<String, Artefact> labels = new HashMap<>();
		Artefact labelHref = new Artefact("labelHref", atFactory.getType("label"));
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
		for (String sf : subFolders) 
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
	}
	
	public String getValue(String key) {
		return values.get(key);
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
			res.add( projectRoot+ File.separator+ pd);
		}
		return res;
	}
	
	public String getSpecificationModelsFolderFull() {
		return getProjectFull()+ File.separator+specificationModelsFolder;
	}
	
	public String getUmlProfilesFolderFull() {
		return getProjectFull()+ File.separator+umlProfilesFolder;
	}
	
	public String getPaletteConfigurationsFolderFull() {
		return getProjectFull()+ File.separator+paletteConfigurationsFolder;
	}
	
	public HashMap<String, String> getToolReqModelFiles(){
		HashMap<String, String> res = new HashMap<>(3);
		res.put("uml", getSpecificationModelsFolderFull() + File.separator + getProjectName() + TOOL_REQ_MODEL_SUFFIX + ".uml");
		res.put("di", getSpecificationModelsFolderFull() + File.separator + getProjectName() + TOOL_REQ_MODEL_SUFFIX + ".di");
		res.put("notation", getSpecificationModelsFolderFull() + File.separator + getProjectName() + TOOL_REQ_MODEL_SUFFIX + ".notation");
		return res;
	}
	
	public HashMap<String, String> getDomainModelFiles(){
		HashMap<String, String> res = new HashMap<>(3);
		res.put("uml", getSpecificationModelsFolderFull() + File.separator + getProjectName() + DOMAIN_MODEL_SUFFIX + ".uml");
		res.put("di", getSpecificationModelsFolderFull() + File.separator + getProjectName() + DOMAIN_MODEL_SUFFIX + ".di");
		res.put("notation", getSpecificationModelsFolderFull() + File.separator + getProjectName() + DOMAIN_MODEL_SUFFIX + ".notation");
		return res;
	}
	
	public HashMap<String, String> getLanguageReqModelFiles(){
		HashMap<String, String> res = new HashMap<>(3);
		res.put("uml", getSpecificationModelsFolderFull() + File.separator + getProjectName() + LANGUAGE_REQ_SUFFIX + ".uml");
		res.put("di", getSpecificationModelsFolderFull() + File.separator + getProjectName() + LANGUAGE_REQ_SUFFIX + ".di");
		res.put("notation", getSpecificationModelsFolderFull() + File.separator + getProjectName() + LANGUAGE_REQ_SUFFIX + ".notation");
		return res;
		
	}
}
