package edu.uoc.som.orchestrus.tracemodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.uoc.som.orchestrus.Orchestrus;
import edu.uoc.som.orchestrus.parsing.StaticExplorer;
import edu.uoc.som.orchestrus.parsing.refmanager.Reference;
import edu.uoc.som.orchestrus.parsing.refmanager.ReferenceFactory;
import edu.uoc.som.orchestrus.parsing.refmanager.ReferenceFactory.Protocol;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;

public class ArtefactFactory {
	public final static Logger LOGGER = Logger.getLogger(ArtefactFactory.class.getName());

	private static final String ROOT_LOCATION_NAME = "ROOT";
	private Artefact projectRoot;

	static ArtefactFactory instance;

	public static ArtefactFactory getInstance() {
		if (instance == null)
			instance = new ArtefactFactory();
		return instance;
	}

	/**
	 * (location+name) -> artefact
	 */
	private Map<String, Artefact> artefacts;


	public ArtefactFactory() {
		artefacts = new HashMap<>();
	}

	/**
	 * WARNING missing some ExternalFOlderArtefacts....
	 * @return
	 */
	public static Map<String, Artefact> getArtefacts() {
		return getInstance().artefacts;
	}
	
	
	public static HashSet<ArtefactType> getAllArtefactTypes() {	
		HashSet<ArtefactType> ats = new HashSet<>();
		for (Artefact a : ArtefactFactory.getArtefacts().values()) {
			ats.add(a.getType());
		}
		return ats;
	}


	public Artefact getArtefact(File f) {
		String locationName = f.getParent() + f.getName();
		Protocol p = Protocol.no_protocol;
		if(f.exists())
			p = Protocol.local;
		return getArtefact(p + locationName);
	}

	/**
	 * See <ul><li>{@link #newSourceFileArtefact(File)}</li><li>{@link #newLocalFileArtefact(Reference)}</li><li>{@link #newExternalFileArtefact(Reference)}</li></ul> 
	 * 
	 * for precions/cohesion on keys of the artefacts map.
	 * @param r
	 * @return
	 */
	public Artefact getArtefact(Reference r) {
		String locationName = "";
		if (r.isLocal()) {
			File f = new File(r.getTargetFileArtefact());
			if(r.isResolved())
				locationName = f.getParent() + f.getName();
			else
				locationName = r.getTargetFileArtefact() + f.getName();
		} else {
			//TODO locationName connundrum
//			locationName = r.getTargetFileArtefact() + r.getTargetFileArtefact();
			String location = r.getTargetFileArtefact();
			String name = location.substring(location.lastIndexOf("/") + 1);
			location = location.substring(0, location.length() - name.length());
			locationName = location + name;
		}
		return getArtefact(r.getProtocol() + locationName);
	}
	
	public Artefact getArtefact(Protocol p, String location, String name) {
		return getArtefact(p + location + name);
	}



	private Artefact getArtefact(String locationName) {
		return artefacts.get(locationName);
	}

	public Artefact getArtefactWithID(String ID) {
		for (Artefact a : artefacts.values()) {
			if (a.getID().equals(ID))
				return a;
		}
		return null;
	}

	/**
	 * Builds artefacts from an instanciated static explorer (see {@link StaticExplorer}) 
	 */
	public void buildArtefacts() {
		
		createLocalRootArtefact();
//		Orchestrus.printArtefactSignatures();
		
		/* build Artefact from source files found in project folder */
		buildSourceFileArtefacts();
		System.out.println("\n");
//		Orchestrus.printArtefactSignatures();
		getArtefacts();
		/* build Artefact from source files' parent folders */
		//buildSourceFolderArtefacts();

		/* build Artefact from source files found in project folder */
		buildLocalFilesArtefacts();
		System.out.println("\n");
		getArtefacts();
//		Orchestrus.printArtefactSignatures();

		/* build Artefact from source files found in project folder */
		buildExternalFilesArtefacts();
		getArtefacts();
		System.out.println("\n");
//		Orchestrus.printArtefactSignatures();

	}

	private void createLocalRootArtefact() {
		projectRoot = new Artefact("ProjectRoot", ArtefactTypeFactory.LOCAL_ROOT_ARTEFACT, ROOT_LOCATION_NAME, null, true);
		projectRoot.setProtocol(Protocol.local);
		addArtefact(projectRoot);
	}

	/**
	 * Browse {@link ReferenceFactory#getSourceFiles()} and create SourceFiles
	 * artefacts accordingly ({@link ArtefactTypeFactory#SOURCE_FILE_ARTEFACT}).
	 * <br/>
	 * These are the files found in the project repository.
	 */
	private void buildSourceFileArtefacts() {
		int iFile = 0;
		int iFolder = 0;
		for (String sFile : StaticExplorer.getSourceFiles()) {
			File f = new File(sFile);
			Artefact a = newSourceFileArtefact(f);
			addArtefact(a);
			iFile++;
			boolean added = addLocalFolderArtefact(a);
			if (added)
				iFolder++;
			
			if(a.getParent() != null)
			
			projectRoot.addFragment(a.getParent());
		}
		LOGGER.fine(iFile + " SourceFile and " + iFolder + " LocalFolder found in "
				+ StaticExplorer.getSourceFiles().size() + " files.");
	}

	/**
	 * Allocate its parent folder
	 * ({@link ArtefactTypeFactory#LOCAL_FOLDER_ARTEFACT}) to a Source/Local File
	 * artefact. <br/>
	 * 
	 * Warning. If parent does not resolve, the system will send an exception
	 * and stop.
	 * 
	 * @param a
	 * @param r  (null when calling for a SourceFile artefact)
	 * @return
	 */
	private boolean addLocalFolderArtefact(Artefact a) {
		Artefact res;
		File f = new File(a.getLocation());
		try {
			f.getParentFile().getCanonicalPath();
		} catch (Exception e) {
			e.printStackTrace();// NO REASON we get there, sources were got from same env.
			LOGGER.severe("Location (of parent folder) not found: " + f.getAbsolutePath());
			System.exit(1);
		}

		res = getArtefact(f);
		if (res == null) {
			res = new Artefact(f.getName(), ArtefactTypeFactory.LOCAL_FOLDER_ARTEFACT, f.getParent(), null, true);
			res.addFragment(a);
			addArtefact(res);
			return true;
		}
		return false;
	}

//	/**
//	 * Browse {@link ArtefactTypeFactory#SOURCE_FILE_ARTEFACT}s and allocate for
//	 * each of them its parent folder. <br/>
//	 * 
//	 * Warning. If one parent does not resolve, the system will send an exception
//	 * and stop.
//	 */
//	@SuppressWarnings("unused")
//	private void buildLocalFolderArtefacts() {
//		List<Artefact> sourceArts = subsetsArtefactsByType(ArtefactTypeFactory.SOURCE_FILE_ARTEFACT);
//		for (Artefact a : sourceArts) {
//			String location = "-location-";
//			File f = new File(a.getLocation());
//			try {
//				location = f.getParentFile().getCanonicalPath();
//			} catch (IOException e) {
//				e.printStackTrace();// NO REASON we get there, sources were got from same env.
//				LOGGER.severe("Location (of parent folder) not found: " + f.getAbsolutePath());
//				System.exit(1);
//			}
//
//			Artefact parent = getArtefact(f.getParentFile());
//			if (parent == null) {
//				parent = new Artefact(f.getName(), ArtefactTypeFactory.LOCAL_FOLDER_ARTEFACT, location, null, f.exists());
//				addArtefact(parent);
//			}
//			parent.addFragment(a);
//		}
//	}

	/**
	 * Browse {@link ReferenceFactory#getExternalReferences()} and create
	 * ExternalFiles artefacts accordingly
	 * {@link ArtefactTypeFactory#EXTERNAL_FILE_ARTEFACT}. <br/>
	 * These are the files found as targets of references from the SourceFile that
	 * are not local <br/>
	 * WARNING: for now, the "external" are the ones that have a protocol). (i.e.,
	 * files in the project repository target other source/local files
	 * (Source/LocalFile artefacts).
	 */
	private void buildExternalFilesArtefacts() {
		int artefactsAdded = 0;
		for (Reference r : ReferenceFactory.getExternalReferences()) {
			Artefact a = newExternalFileArtefact(r);
			boolean success = addArtefact(a);
			if (success)
				artefactsAdded++;
//			LOGGER.finer("new: " + a);
		}
		LOGGER.fine(artefactsAdded + " ExternalFile artefacts found in " + ReferenceFactory.getReferences().size()
				+ " references.");
	}

	/**
	 * Browse {@link ReferenceFactory#getLocalReferences()} and create LocalFiles
	 * artefacts accordingly {@link ArtefactTypeFactory#LOCAL_FILE_ARTEFACT}. <br/>
	 * These are the files found as targets of references from the SourceFile.
	 * (i.e., files in the project repository target other local files (LocalFile
	 * artefacts).
	 */
	private void buildLocalFilesArtefacts() {
		int artefactsAdded = 0;
		int folderAdded = 0;
		for (Reference r : ReferenceFactory.getLocalReferences()) {
			Artefact a = newLocalFileArtefact(r);
			boolean success = addArtefact(a);
			if (success)
				artefactsAdded++;
			if(r.isResolved()) {
				boolean added = addLocalFolderArtefact(a);
				if(added) {
					folderAdded++;
					LOGGER.finer("added:" + a.getParent().getLocation()+a.getParent().getName());
				}
			}
		}
		LOGGER.fine(artefactsAdded + " LocalFile and "+folderAdded+" LocalFolder found in " + ReferenceFactory.getReferences().size()
				+ " references.");
	}

	/**
	 * Creates a new Artefact of type
	 * {@link ArtefactTypeFactory#SOURCE_FILE_ARTEFACT}. It is located at the parent
	 * folder of the file passed in parameter
	 * 
	 * @param f
	 * @return a new Artefact of type
	 *         {@link ArtefactTypeFactory#SOURCE_FILE_ARTEFACT}
	 */
	private Artefact newSourceFileArtefact(File f) {
		Artefact res = getArtefact(f);
		if (res == null) {
			res = new Artefact(f.getName(), ArtefactTypeFactory.SOURCE_FILE_ARTEFACT, f.getParent(), null, true);
			LOGGER.finest("newR " + res);
		} else
			LOGGER.finest("Artefact '" + res + "' already exists.");
		return res;
	}

	/**
	 * Creates a new Artefact of type
	 * {@link ArtefactTypeFactory#LOCAL_FILE_ARTEFACT} named from the target file
	 * name extracted from {@link Reference#getTargetFileArtefact()}. <br/>
	 * If R resolves ({@link Reference#isResolved()}) the artefact is gona be
	 * located ({@link Artefact#location}) at the parent folder. (This will help
	 * cluster links' origins and targets) <br/>
	 * If not, the artefact will be located at the reference's target
	 * ({@link Reference#getTargetFileArtefact()}). <br/>
	 * ArtefactType: {@link ArtefactTypeFactory#LOCAL_FILE_ARTEFACT}
	 * 
	 * @param r
	 * @return a new Artefact of type
	 *         {@link ArtefactTypeFactory#LOCAL_FILE_ARTEFACT}
	 */
	private Artefact newLocalFileArtefact(Reference r) {
		Artefact res = null;

		File f = new File(r.getTargetFileArtefact());
		String name = f.getName();
		
		
		if (r.isResolved()) {
			// Resolved means that the ancestry of the file exists, we can use the parent to
			// precise the location
			res = getArtefact(f);
			if (res == null) {
				res = new Artefact(name + "", ArtefactTypeFactory.LOCAL_FILE_ARTEFACT, f.getParent(), null, true);
				LOGGER.finest("newR " + res);
			} else {
				LOGGER.finest("Artefact '" + res + "' already exists.");
			}
			
		} else { // R is not resolved
			String location = r.getTargetFileArtefact(); // Location is the target - we keep it full.

			res = getArtefact(r.getProtocol(), location, name);
			if (res == null) {
				res = new Artefact(name, ArtefactTypeFactory.LOCAL_FILE_ARTEFACT, location, null, false);
				LOGGER.finest("new " + res);
			} else {
				LOGGER.finest("Artefact '" + res + "' already exists.");
			}
		}
		return res;
	}

	/**
	 * Creates a new Artefact of type
	 * {@link ArtefactTypeFactory#EXTERNAL_FILE_ARTEFACT} named from the last path
	 * element of {@link Reference#getTargetFileArtefact()} and located at
	 * {@link Reference#getTargetFileArtefact()}. <br/>
	 * 
	 * @param r
	 * @return a new Artefact of type
	 *         {@link ArtefactTypeFactory#EXTERNAL_FILE_ARTEFACT}
	 */
	private Artefact newExternalFileArtefact(Reference r) {
		String location = r.getTargetFileArtefact();
		String name = location.substring(location.lastIndexOf("/") + 1);
		location = location.substring(0, location.length() - name.length());

		Artefact res = getArtefact(r.getProtocol(), location, name);
		if (res == null) {
			res = new Artefact(name, ArtefactTypeFactory.EXTERNAL_FILE_ARTEFACT, location, null, false);
			res.setProtocol(r.getProtocol());//
			addArtefact(res);
			LOGGER.finest("new " + res);
		}
		Artefact parent = getExternalLocation(location, r.getProtocol(), res);
		parent.addFragment(res);

		return res;
	}

	/**
	 * 
	 * @param location
	 * @param p
	 * @param res fragment 
	 */
	private Artefact getExternalLocation(String location, Protocol p, Artefact res) {
		Artefact resParent = getArtefact(p, location, ROOT_LOCATION_NAME);
		if(resParent == null) {
			resParent = new ExternalLocationArtefact(location, ArtefactTypeFactory.EXTERNAL_LOCATION_ARTEFACT, p);
			addArtefact(resParent);
		}
		return resParent;
	}

	public Artefact[] sortArtefactsByType(Collection<Artefact> artefacts) {
		Artefact[] arts = (Artefact[]) artefacts.toArray(new Artefact[artefacts.size()]);
		Arrays.sort(arts, new Comparator<Artefact>() {
			@Override
			public int compare(Artefact o1, Artefact o2) {
				return o1.getType().getName().compareTo(o2.getType().getName());
			}
		});
		return arts;
	}

	public static Artefact[] sortArtefactsByLocation(Collection<Artefact> artefacts) {
		Artefact[] arts = (Artefact[]) artefacts.toArray(new Artefact[artefacts.size()]);
		Arrays.sort(arts, new Comparator<Artefact>() {
			@Override
			public int compare(Artefact o1, Artefact o2) {
				return o1.getLocation().compareTo(o2.getLocation());
			}
		});
		return arts;
	}

	public static List<Artefact> subsetsArtefactsByType(ArtefactType type) {
		return getArtefacts().values().stream().filter(a -> a.isOfType(type)).collect(Collectors.toList());
	}

	public static List<Artefact> subsetsArtefactsByTypeName(String typeName) {
		return getArtefacts().values().stream().filter(a -> a.isOfType(typeName)).collect(Collectors.toList());
	}
	
	public static List<Artefact> subsetsArtefactsByProtocol(Protocol p) {
		return getArtefacts().values().stream().filter(a -> a.getProtocol() == p).collect(Collectors.toList());
	}

	public static Set<Artefact> getAncestors() {
		HashSet<Artefact> res = new HashSet<>();
		for (Artefact a : getArtefacts().values()) {
			res.add(a.getAncestor());
		}
		return res;
	}

	
	static public Set<Artefact> arts = new HashSet<>();
	
	public boolean addArtefact(Artefact a) {
		int size = artefacts.size();
		artefacts.put(a.getProtocol() + a.getLocation() + a.getName(), a);
		if(size != artefacts.size() - 1) {
			LOGGER.finest(a + " was not added.");
		}
		arts.add(a);
		return size == artefacts.size() - 1;
	}

	public static void printArtefactsByType() {
		System.out.println();
		System.out.println(getArtefacts().size() + " artefacts built.");
		for (ArtefactType aType : ArtefactTypeFactory.getInstance().getTypesValues()) {
			Artefact[] arts = sortArtefactsByLocation(subsetsArtefactsByType(aType));
			System.out.println(aType.getName());
			for (Artefact a : arts)
				System.out.println(" - " + a.getJSon());// a.getLocation() + " " + a.getJSon());
		}
	}

	public static List<TraceLink> getFragmentLinks(Artefact a) {
		ArrayList<TraceLink> tls = new ArrayList<>();
		for (Artefact aa : a.getFragments()) {
			TraceLink tl = new TraceLink(a, aa);
			tls.add(tl);
			tls.addAll(getFragmentLinks(aa));
		}
		return tls;
	}



}
