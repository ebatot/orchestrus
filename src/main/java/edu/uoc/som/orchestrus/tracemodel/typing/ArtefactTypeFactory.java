package edu.uoc.som.orchestrus.tracemodel.typing;

import java.util.Collection;
import java.util.HashMap;

public class ArtefactTypeFactory {

	public static final String EXTERNAL_FILE_ARTEFACT = "ExternalFileArtefact";
	public static final String LOCAL_FILE_ARTEFACT = "LocalFileArtefact";
	public static final String SOURCE_FILE_ARTEFACT = "SourceFileArtefact";
	

	static ArtefactTypeFactory instance;

	public static ArtefactTypeFactory getInstance() {
		if (instance == null)
			instance = new ArtefactTypeFactory();
		return instance;
	}

	HashMap<String, ArtefactType> types = new HashMap<>();

	public ArtefactType addType(String typeName) {
		ArtefactType add = new ArtefactType(typeName);
		types.put(typeName, add);
		return add;
	}

	public ArtefactType getType(String typeName) {
		if (types.get(typeName) == null)
			addType(typeName);
		return types.get(typeName);
	}

	public Collection<ArtefactType> getTypesValues() {
		return types.values();
	}

}
