package edu.uoc.som.orchestrus.config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;

public class Config {
	public final static Logger LOGGER = Logger.getLogger(Config.class.getName());

	
//	String projectRoot = "R:\\Coding\\Git\\orchestrus\\data\\GlossaryML-ReferenceML";
//	String project = "com.cea.papyrus.glossary";
//	String projectName = "GlossaryML";
//	List<String> projectDependencies = Arrays.asList("com.cea.papyrus.referencemanagement");

	String projectRoot = "R:\\Coding\\Git\\orchestrus\\data\\GlossaryML-ReferenceML";
	String project = "com.cea.papyrus.referencemanagement";
	String projectName = "ReferencesML";
	List<String> projectDependencies = Collections.emptyList();//Arrays.asList("com.cea.papyrus.referencemanagement");

	/*
	 * Hard coded config files (in project root folder)
	 */
	public static final String PLUGIN_XML_FILENAME = "plugin.xml";
	public static final String PROJECT_FILENAME = ".project";
	public static final String CLASSPATH_FILENAME = ".classpath";
	public static final String MANIFEST_FILENAME = "META-INF"+File.separator+"MANIFEST.MF";
	public static final String BUILD_PROPERTIES_FILENAME = "build.properties";
	List<String> configFiles = Arrays.asList(
			PLUGIN_XML_FILENAME, 
			PROJECT_FILENAME,
			CLASSPATH_FILENAME, 
			MANIFEST_FILENAME, 
			BUILD_PROPERTIES_FILENAME
		);

	/*
	 * Hard coded folders names
	 */
	static final String architectureFramework = "architecture-framework";
	static final String elementTypeConfiguration = "element-type-configurations";
	static final String paletteConfigurationsFolder = "palette-configurations";
	static final String propertiesEditorConfiguration = "properties-editor-configurations";
	static final String specificationModelsFolder = "specification-models";
	static final String tabularEditorConfiguration = "tabular-editors-configurations";
	static final String umlProfilesFolder = "uml-profiles";
	static final String rootFolder = ".";
	List<String> contentFolders = Arrays.asList(
			architectureFramework, 
			elementTypeConfiguration,
			paletteConfigurationsFolder, 
			propertiesEditorConfiguration, 
			specificationModelsFolder,
			tabularEditorConfiguration, 
			umlProfilesFolder,
			rootFolder
		);
	
	static String TOOL_REQ_MODEL_SUFFIX = "_ToolReqModel";
	static String DOMAIN_MODEL_SUFFIX = ".domainmodel";
	static String LANGUAGE_REQ_SUFFIX = ".LanguageReqModel";
	
	public static final double MATRIX_DEFAULT_THRESHOLD = 0.5;

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
			LOGGER.severe("Some folders were not found. Please check configuration.");
//			System.exit(1);
		} else {
			LOGGER.fine("Folders correct.");
		}
		

		check = checkConfigFilesNames();
		if(!check) {
			LOGGER.severe("Some config files were not found. Please check configuration.\nExit with errors.");
			System.exit(1);
		} else {
			LOGGER.fine("Config files correct.");
		}
		/*
		 * Artefacts typing
		 */
//		initArtefactTypes();

		/*
		 * Link typing
		 */
//		initLinkTypes();
		
		/*
		 * Fragments bags: elements (nodes), labels (leafs)
		 */
//		HashMap<String, Artefact> xmlElts = new HashMap<>();
//		HashMap<String, Artefact> labels = new HashMap<>();
		
		
		
		
		// Fragments instanciation 
		//  -> rebuild from XPath patterns
		//  -> contextualizes with file/artefact location
//		Artefact eltXmiType = new Artefact("xmiType", atFactory.getType("xmlElt"));
//		Artefact labelHref = new Artefact("labelHref", atFactory.getType("label"));
		
		// Fragments connexion (Links instanciation)
		//  - Which fragment connects -directly- with who, and -derively- with who
		/*
		 *   F1 - F2 - F3 
		 *    -> F1 connects directly to F2
		 *    -> F1 connects derively to F3 (derived attribute, transitive closure with sub fragments)
		 */
	}

	private boolean checkConfigFilesNames() {
		boolean res = true;
		
		for (String sf : configFiles) {
			boolean tmp = getConfigFile(sf).exists();
			if(!tmp)
				LOGGER.warning("Could not find file: "+getConfigFile(sf).getAbsolutePath());
			res &= tmp;
		}
		return res;
	}

	private boolean checkFolderNames() {
		boolean res = true;
		//Main /
		res &= checkExistsAndDirectory(getProjectRoot());
		//Project folder
		res &= checkExistsAndDirectory(getProjectFullPath());
		
		//Project denpendencies
		for (String pd : getProjectDependenciesFull()) 
			res &= checkExistsAndDirectory(pd);
		
		//sub folders
		for (String sf : contentFolders) 
			res &= checkExistsAndDirectory(getProjectFullPath() + File.separator + sf);
		
		
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

	public String getValue(String key) {
		return values.get(key);
	}
	
	public List<String> getContentFoldersName() {
		return contentFolders;
	}
	
	public List<String> getContentFoldersFull() {
		ArrayList<String> res = new ArrayList<>(getContentFoldersName().size());
		for (String s : getContentFoldersName()) {
			res.add(getProjectFullPath() + File.separator + s);
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
	
	public String getProjectFullPath() {
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

	public static String getPropertiesEditorConfiguration() {
		return propertiesEditorConfiguration;
	}
	public String getPropertiesEditorConfigurationContext() {
		return getProjectFullPath() + File.separator + propertiesEditorConfiguration + File.separator + getProjectName()+".ctx";
	}

	public String getSpecificationModelsFolderFull() {
		return getProjectFullPath() + File.separator + specificationModelsFolder;
	}
	
	public String getSpecificationModelsFolder() {
		return specificationModelsFolder;
	}

	public String getUmlProfilesFolderFull() {
		return getProjectFullPath() + File.separator + umlProfilesFolder;
	}

	public static String getUmlprofilesfolder() {
		return umlProfilesFolder;
	}
	
	public String getPaletteConfigurationsFolderFull() {
		return getProjectFullPath() + File.separator + paletteConfigurationsFolder;
	}
	
	public String getEcoreFilePath() {
		File fEcore = new File(getUmlProfilesFolderFull()+File.separator+projectName+".ecore");
		if(fEcore.exists())
			return fEcore.getAbsolutePath();
		else {
			File folder = new File(getUmlProfilesFolderFull());
			File[] fs = folder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".ecore");
				}
			});
			LOGGER.warning("Ecore file '"+fEcore.getName()+"' not found.");
			if(fs.length > 0) {
				LOGGER.warning("Ecore file '"+fs[0]+"' used instead.");
				return fs[0].getAbsolutePath();
			}
			return "";
		}
	}

	/**
	 * WARNING. "to lower case" as for the GlossaryML project, genmodel in lower case.
	 * @return
	 */
	public String getGenmodelFilePath() {
		File fEcore = new File(getUmlProfilesFolderFull()+File.separator+projectName.toLowerCase()+".genmodel");
		return fEcore.getAbsolutePath();
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

	public File getConfigFile(String fileName) {
		File f = new File(projectRoot + File.separator+project+File.separator + fileName);
		return f;
	}
}
