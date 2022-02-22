package edu.uoc.som.tracemodel.typing;

import java.util.HashMap;

public class ArtefactTypeFactory {
	static ArtefactTypeFactory instance;
	
	public static ArtefactTypeFactory getInstance() {
		if(instance == null)
			instance = new ArtefactTypeFactory();
		return instance;
	}
	
	HashMap<String, ArtefactType> types = new HashMap<>();
	
	public ArtefactType addType(String typeName) {
		ArtefactType add = new ArtefactType(typeName);
		types.put(typeName, add);
		return add;
	}
}
