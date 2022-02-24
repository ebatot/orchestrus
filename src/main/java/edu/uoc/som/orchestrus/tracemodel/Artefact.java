package edu.uoc.som.orchestrus.tracemodel;

import java.util.HashMap;

import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.LinkType;
import edu.uoc.som.orchestrus.tracemodel.typing.TypedArtefact;

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
	
	public HashMap<String, ArtefactFragment> getFragments() {
		return fragments;
	}

	public String getJSon() {
		String res = "{";
		res += "\"id\": \""+getID()+"\",";
		res += "\"name\": \""+getName()+"\",";
		res += "\"fragments\": "+Utils.getElementsIDsAsJsonCollection(fragments.values())+",";
		res += "\"type\": \""+getTypeUID()+"\"";
		return res +"}";
	}
}
