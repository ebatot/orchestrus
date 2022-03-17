package edu.uoc.som.orchestrus.tracemodel.typing;

import java.util.Collection;
import java.util.HashMap;

public class ArtefactTypeFactory {

	public static final ArtefactType EXTERNAL_FILE_ARTEFACT = addType("ExternalFile");
	public static final ArtefactType EXTERNAL_LOCATION_ARTEFACT = addType("ExternalLocation");
	public static final ArtefactType LOCAL_FILE_ARTEFACT = addType("LocalFile");
	public static final ArtefactType SOURCE_FILE_ARTEFACT = addType("SourceFile");
	public static final ArtefactType LOCAL_FOLDER_ARTEFACT = addType("LocalFolder");
	public static final ArtefactType LOCAL_ROOT_ARTEFACT = addType("LocalRoot");
	public static final ArtefactType ELEMENT_ARTEFACT = addType("Element");

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

	public static int getD3Size(ArtefactType type) {
//		if(type == LOCAL_ROOT_ARTEFACT)
//			return 1;
		return 100;
//		if(type == LOCAL_FOLDER_ARTEFACT)
//			return 34;
//		if(type == LOCAL_FILE_ARTEFACT)
//			return 38;
//		if(type == SOURCE_FILE_ARTEFACT)
//			return 38;
//		if(type == EXTERNAL_LOCATION_ARTEFACT)
//			return 30;
//		if(type == EXTERNAL_FILE_ARTEFACT)
//			return 34;
//		if(type == ELEMENT_ARTEFACT)
//			return 42;
//		throw new IllegalArgumentException("Unrecognized type:" + type);
	}
}
