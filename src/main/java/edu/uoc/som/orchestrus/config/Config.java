package edu.uoc.som.orchestrus.config;

import java.util.HashMap;

import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;

public abstract class Config {
	
	
	HashMap<String, ArtefactType> artefactTypes;
	
	public Config() {
		
	}
	
	public abstract HashMap<String, ArtefactType> getArtefactTypes() ;
	public abstract HashMap<String, ArtefactType> getArtefacts() ;
	
	public abstract HashMap<String, ArtefactType> getMainName() ;
	
}
