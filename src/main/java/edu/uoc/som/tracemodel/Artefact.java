package edu.uoc.som.tracemodel;

import java.util.HashMap;

import edu.uoc.som.tracemodel.typing.ArtefactType;
import edu.uoc.som.tracemodel.typing.LinkType;
import edu.uoc.som.tracemodel.typing.TypedArtefact;

public class Artefact extends TypedArtefact {
	
	public Artefact(String name, ArtefactType type) {
		super(name, type);
		setType(type);
	}
	
	HashMap<String,ArtefactFragment> fragments = new HashMap<>();
	
	public void addFragment(String label, ArtefactFragment af) {
		fragments.put(label, af);
		af.setArtefact(this);
	}
}
