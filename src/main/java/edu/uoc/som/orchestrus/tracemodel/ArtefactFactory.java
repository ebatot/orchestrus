package edu.uoc.som.orchestrus.tracemodel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
	Map<String, Artefact> artefacts;

	public ArtefactFactory() {
		artefacts = new HashMap<>();
	}

	public Map<String, Artefact> getArtefacts() {
		return artefacts;
	}

	public List<Artefact> subsetsArtefactsByType(ArtefactType type) {
		return artefacts.values().stream().filter(a -> a.isOfType(type)).collect(Collectors.toList());
	}

	public List<Artefact> subsetsArtefactsByTypeName(String typeName) {
		return artefacts.values().stream().filter(a -> a.isOfType(typeName)).collect(Collectors.toList());
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

	public void buildArtefacts() {
		System.out.println("ArtefactFactory.buildArtefacts()");
		System.out.println();

//		for (String s : ReferenceFactory.getReferences().keySet()) {
//			System.out.println(s+":"+ReferenceFactory.getReference(s));
//		}

//		for (Reference r : ReferenceFactory.getReferencesSources().keySet()) {
//			System.out.println(r+":"+ReferenceFactory.getReferencesSources().get(r));
//		}

//		for (String href : ReferenceFactory.getReferencesSourcesReversed().keySet()) {
//			System.out.println(href + ":" + ReferenceFactory.getReferencesSourcesReversed().get(href));
//			Artefact a = getArtefact(href);
//			for (Reference r : ReferenceFactory.getReferencesSourcesReversed().get(href)) {
//				
//			}
//		}

		buildSourceFileArtefacts();

		buildLocalFilesArtefacts();

		buildExternalFilesArtefacts();

		Artefact[] artsSource = sortArtefactsByLocation(subsetsArtefactsByTypeName("SourceFileArtefact"));
		Artefact[] artsLocal = sortArtefactsByLocation(subsetsArtefactsByTypeName("LocalFileArtefact"));
		Artefact[] artsExternal = sortArtefactsByLocation(subsetsArtefactsByTypeName("ExternalFileArtefact"));

		System.out.println();
		System.out.println(getArtefacts().size() + " artefacts built.");
		System.out.println("Sources:");
		for (Artefact a : artsSource)
			System.out.println(" - " + a);// a.getLocation() + " " + a.getJSon());
		System.out.println("Locals:");
		for (Artefact a : artsLocal)
			System.out.println(" - " + a);// a.getLocation() + " " + a.getJSon());
		System.out.println("Externals:");
		for (Artefact a : artsExternal)
			System.out.println(" - " + a);// a.getLocation() + " " + a.getJSon());

	}

	/**
	 * Browse {@link ReferenceFactory#getExternalReferences()} and create
	 * ExternalFiles artefacts accordingly. These are the files found as targets of
	 * references from the SourceFile that are not local WARNING: for now, the
	 * "external" are the ones that have a protocol). (i.e., files in the project
	 * repository target other local files (LocalFile artefacts).
	 */
	private void buildExternalFilesArtefacts() {
		int artefactsAdded = 0;
		ArtefactType atExternals = ArtefactTypeFactory.getInstance()
				.getType(ArtefactTypeFactory.EXTERNAL_FILE_ARTEFACT);
		for (Reference r : ReferenceFactory.getExternalReferences()) {
			Artefact a = new Artefact(r.getTargetFileArtefact() + "", atExternals, r.getTargetFileArtefact(), null);
			addArtefact(a);
			artefactsAdded++;
//			LOGGER.finer("ADDED: "+a);
		}
		LOGGER.fine(artefactsAdded + " ExternalFile artefacts found in " + ReferenceFactory.getReferences().size()
				+ " references.");
	}

	/**
	 * Browse {@link ReferenceFactory#getLocalReferences()} and create LocalFiles
	 * artefacts accordingly. These are the files found as targets of references
	 * from the SourceFile. (i.e., files in the project repository target other
	 * local files (LocalFile artefacts).
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
	 * Browse {@link ReferenceFactory#getSourceFiles()} and create SourceFiles
	 * artefacts accordingly. These are the files found ni the project repository.
	 */
	private void buildSourceFileArtefacts() {
		for (String sFile : ReferenceFactory.getSourceFiles()) {
			File f = new File(sFile);
			Artefact a = newSourceFileArtefact(f);
			addArtefact(a);
		}
		LOGGER.fine(getArtefacts().size() + " SourceFile artefacts found in " + ReferenceFactory.getSourceFiles().size()
				+ " files.");
	}

	public Artefact newSourceFileArtefact(File f) {
		ArtefactType atSource = ArtefactTypeFactory.getInstance().getType(ArtefactTypeFactory.SOURCE_FILE_ARTEFACT);

		String location = "-location-";
		try {
			location = f.getParentFile().getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();// NO REASON we get there, sources were got from same env.
		}
		Artefact a = new Artefact(f.getName(), atSource, location, null, true);
		LOGGER.finer("new " + a);
		return a;
	}

	public Artefact newLocalFileArtefact(Reference r) {
		ArtefactType atLocals = ArtefactTypeFactory.getInstance().getType(ArtefactTypeFactory.LOCAL_FILE_ARTEFACT);

		Artefact res = null;

		File f = new File(r.getTargetFileArtefact());
		String name = f.getName();
		if (r.isResolved()) {
			// Resolved means that the ancestry of the file exists, we can use the parent to
			// precise the location
			String location = f.getParentFile().getAbsolutePath();
			if (artefacts.containsKey(location + name)) {
				res = getArtefact(location + name);
			} else {
				// Prefix name with type ? atLocals.getName() +"_"+
				res = new Artefact(name + "", atLocals, location, null, true);
				LOGGER.finest("newR " + res);
			}
		} else {
			String location = r.getTargetFileArtefact(); // Location is the target - we keep it full.
			if (artefacts.containsKey(location + name)) {
				res = getArtefact(location + name);
			} else {
				res = new Artefact(name + "", atLocals, location, null, false); // location and name are redundant ->
																				// because there is a quack (no
																				// resolution)!
				LOGGER.finest("new " + res);
			}
		}
		return res;
	}

	@SuppressWarnings("unused")
	private Artefact[] sortArtefactsByType(Collection<Artefact> artefacts) {
		Artefact[] arts = (Artefact[]) artefacts.toArray(new Artefact[artefacts.size()]);
		Arrays.sort(arts, new Comparator<Artefact>() {
			@Override
			public int compare(Artefact o1, Artefact o2) {
				return o1.getType().getName().compareTo(o2.getType().getName());
			}
		});
		return arts;
	}

	private Artefact[] sortArtefactsByLocation(Collection<Artefact> artefacts) {
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

}
