package edu.uoc.som.orchestrus.tracemodel.typing;

import java.util.Collection;
import java.util.HashMap;

public class ArtefactTypeFactory {

	public static final ArtefactType EXTERNAL_FILE_ARTEFACT = addType("ExternalFileArtefact");
	public static final ArtefactType EXTERNAL_LOCATION_ARTEFACT = addType("ExternalLocationArtefact");
	public static final ArtefactType LOCAL_FILE_ARTEFACT = addType("LocalFileArtefact");
	public static final ArtefactType SOURCE_FILE_ARTEFACT = addType("SourceFileArtefact");
	public static final ArtefactType LOCAL_FOLDER_ARTEFACT = addType("LocalFolderArtefact");
	public static final ArtefactType LOCAL_ROOT_ARTEFACT = addType("LocalRootArtefact");

	static ArtefactTypeFactory instance;

	public static ArtefactTypeFactory getInstance() {
		if (instance == null)
			instance = new ArtefactTypeFactory();
		return instance;
	}

	HashMap<String, ArtefactType> types = new HashMap<>();

	public static ArtefactType addType(String typeName) {
		ArtefactType add = new ArtefactType(typeName);
		getInstance().getTypes().put(typeName, add);
		return add;
	}

	public static ArtefactType getType(String typeName) {
		if (getInstance().getTypes().get(typeName) == null)
			addType(typeName);
		return getInstance().getTypes().get(typeName);
	}

	public Collection<ArtefactType> getTypesValues() {
		return types.values();
	}

	public HashMap<String, ArtefactType> getTypes() {
		return types;
	}
}
