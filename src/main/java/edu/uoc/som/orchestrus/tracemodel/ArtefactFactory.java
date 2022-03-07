package edu.uoc.som.orchestrus.tracemodel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.uoc.som.orchestrus.parsing.StaticExplorer;
import edu.uoc.som.orchestrus.parsing.refmanager.Reference;
import edu.uoc.som.orchestrus.parsing.refmanager.ReferenceFactory;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;

public class ArtefactFactory {
	public final static Logger LOGGER = Logger.getLogger(ArtefactFactory.class.getName());

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


	public List<Artefact> subsetsArtefactsByType(ArtefactType type) {
		return artefacts.values().stream().filter(a -> a.isOfType(type)).collect(Collectors.toList());
	}

	public List<Artefact> subsetsArtefactsByTypeName(String typeName) {
		return artefacts.values().stream().filter(a -> a.isOfType(typeName)).collect(Collectors.toList());
	}
	
	public Artefact getArtefact(File f) {
		String locationName = f.getParent() + f.getName();
		return getArtefact(locationName);
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
			locationName = r.getTargetFileArtefact() + r.getTargetFileArtefact();
		}
		return getArtefact(locationName);
	}

	public Artefact getArtefact(String locationName) {
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

		/* build Artefact from source files found in project folder */
		buildSourceFileArtefacts();

		/* build Artefact from source files' parent folders */
		buildSourceFolderArtefacts();

		/* build Artefact from source files found in project folder */
		buildLocalFilesArtefacts();

		/* build Artefact from source files found in project folder */
		buildExternalFilesArtefacts();

	}

	/**
	 * Browse {@link ReferenceFactory#getSourceFiles()} and create SourceFiles
	 * artefacts accordingly ({@link ArtefactTypeFactory#SOURCE_FILE_ARTEFACT}).
	 * <br/>
	 * These are the files found in the project repository.
	 */
	private void buildSourceFileArtefacts() {
		for (String sFile : StaticExplorer.getSourceFiles()) {
			File f = new File(sFile);
			Artefact a = newSourceFileArtefact(f);
			addArtefact(a);
		}
		LOGGER.fine(getArtefacts().size() + " SourceFile artefacts found in " + StaticExplorer.getSourceFiles().size()
				+ " files.");
	}

	/**
	 * Browse {@link ArtefactTypeFactory#SOURCE_FILE_ARTEFACT}s and allocate for
	 * each of them its parent folder. <br/>
	 * 
	 * Warning. If one parent does not resolve, the system will send an exception
	 * and stop.
	 */
	private void buildSourceFolderArtefacts() {
		List<Artefact> sourceArts = subsetsArtefactsByType(ArtefactTypeFactory.SOURCE_FILE_ARTEFACT);
		for (Artefact a : sourceArts) {
			String location = "-location-";
			File f = new File(a.getLocation());
			try {
				location = f.getParentFile().getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();// NO REASON we get there, sources were got from same env.
				LOGGER.severe("Location (of parent folder) not found: " + f.getAbsolutePath());
				System.exit(1);
			}

			String locationname = location + f.getName();
			Artefact res = getArtefact(locationname);
			if (res == null) {
				addArtefact(
						new Artefact(f.getName(), ArtefactTypeFactory.SOURCE_FOLDER_ARTEFACT, location, null, true));
			}
		}
	}

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
			LOGGER.finer("new: " + a);
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
		for (Reference r : ReferenceFactory.getLocalReferences()) {
			Artefact a = newLocalFileArtefact(r);
			boolean success = addArtefact(a);
			if (success)
				artefactsAdded++;
		}
		LOGGER.fine(artefactsAdded + " LocalFile artefacts found in " + ReferenceFactory.getReferences().size()
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
		ArtefactType atSource = ArtefactTypeFactory.SOURCE_FILE_ARTEFACT;

		String parentLocation = "-location-";
		try {
			parentLocation = f.getParentFile().getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();// NO REASON we get there, sources were got from same env.
		}

		Artefact res = getArtefact(f.getName() + parentLocation);
		if (res == null) {
			res = new Artefact(f.getName(), atSource, parentLocation, null, true);
			LOGGER.finer("new " + res);
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
		ArtefactType atLocals = ArtefactTypeFactory.LOCAL_FILE_ARTEFACT;

		Artefact res = null;

		File f = new File(r.getTargetFileArtefact());
		String name = f.getName();
		if (r.isResolved()) {
			// Resolved means that the ancestry of the file exists, we can use the parent to
			// precise the location
			String parentLocation = f.getParentFile().getAbsolutePath();
			res = getArtefact(parentLocation + name);
			if (res == null) {
				res = new Artefact(name + "", atLocals, parentLocation, null, true);
				LOGGER.finest("newR " + res);
			} else {
				LOGGER.finest("Artefact '" + res + "' already exists.");
			}
		} else { // R is not resolved
			String location = r.getTargetFileArtefact(); // Location is the target - we keep it full.

			res = getArtefact(location + name);
			if (res == null) {
				// location and name are redundant -> because there is a quack (no resolution)!
				res = new Artefact(name + "", atLocals, location, null, false);
				LOGGER.finest("new " + res);
			} else {
				LOGGER.finest("Artefact '" + res + "' already exists.");
			}
		}
		return res;
	}

	/**
	 * Creates a new Artefact of type
	 * {@link ArtefactTypeFactory#EXTERNAL_FILE_ARTEFACT} named from the complete
	 * target path of {@link Reference#getTargetFileArtefact()} and located at the
	 * same {@link Reference#getTargetFileArtefact()}. <br/>
	 * 
	 * @param r
	 * @return a new Artefact of type
	 *         {@link ArtefactTypeFactory#EXTERNAL_FILE_ARTEFACT}
	 */
	private Artefact newExternalFileArtefact(Reference r) {
		ArtefactType atExternals = ArtefactTypeFactory.EXTERNAL_FILE_ARTEFACT;

		String artKey = r.getTargetFileArtefact() + r.getTargetFileArtefact(); // Correspond to "name+location", see few
																				// lines bellow.
		Artefact res = null;
		if (artefacts.containsKey(artKey)) {
			res = getArtefact(artKey);
		} else {
			// WARNING Location and name are redundant -> because there is a quack (no
			// resolution) !
			res = new Artefact(r.getTargetFileArtefact() + "", atExternals, r.getTargetFileArtefact(), null);
			LOGGER.finest("new " + res);
		}
		return res;
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

	public Artefact[] sortArtefactsByLocation(Collection<Artefact> artefacts) {
		Artefact[] arts = (Artefact[]) artefacts.toArray(new Artefact[artefacts.size()]);
		Arrays.sort(arts, new Comparator<Artefact>() {
			@Override
			public int compare(Artefact o1, Artefact o2) {
				return o1.getLocation().compareTo(o2.getLocation());
			}
		});
		return arts;
	}

	public boolean addArtefact(Artefact a) {
		int size = artefacts.size();
		artefacts.put(a.getLocation() + a.getName(), a);
		return size == artefacts.size() - 1;
	}

	public void printArtefactsByType() {
		System.out.println();
		System.out.println(getArtefacts().size() + " artefacts built.");
		for (ArtefactType aType : ArtefactTypeFactory.getInstance().getTypesValues()) {
			Artefact[] arts = sortArtefactsByLocation(subsetsArtefactsByType(aType));
			System.out.println(aType.getName());
			for (Artefact a : arts)
				System.out.println(" - " + a);// a.getLocation() + " " + a.getJSon());
		}
	}

}
