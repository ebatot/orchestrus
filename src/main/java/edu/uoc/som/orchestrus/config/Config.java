package edu.uoc.som.orchestrus.config;

import java.util.HashMap;

import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;

public class Config {
	
	
	HashMap<String, ArtefactType> artefactTypes;
	
	public Config() {
		
	}
	
	public HashMap<String, ArtefactType> getArtefactTypes() {
		return artefactTypes;
	}
	
	public void setArtefactTypes(HashMap<String, ArtefactType> artefactTypes) {
		this.artefactTypes = artefactTypes;
	}
	
	public ArtefactType addArtefactType(ArtefactType at) {
		return artefactTypes.put(at.getID(), at);
	}
}
