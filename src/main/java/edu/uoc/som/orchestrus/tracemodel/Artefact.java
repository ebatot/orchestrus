package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.TypedArtefact;

public class Artefact extends TypedArtefact {
	private static final String FRAG_SUFIX = "_frag";
	Artefact parent;
	String definition;
	HashMap<String,Artefact> fragments = new HashMap<>();
	
	ArrayList<TraceLink> sourceOf;
	ArrayList<TraceLink> targetOf;

	public Artefact(Artefact parent, String name) {
		this(name, parent.getType());
		setParent(parent);
	}
	
	/**
	 * Automated naming parentname+"_frag"
	 * @param parent
	 */
	public Artefact(Artefact parent) {
		this(parent.getName()+FRAG_SUFIX, parent.getType());
		parent.addFragment(this);
	}
	public Artefact(String name, ArtefactType type) {
		super(name, type);
		setType(type);
		sourceOf = new ArrayList<>();
		targetOf = new ArrayList<>();
	}
	
	
	public void addFragment(Artefact af) {
		fragments.put(af.getID(), af);
		af.setParent(this);
	}
	
	public HashMap<String, Artefact> getFragments() {
		return fragments;
	}
	
	public Artefact getParent() {
		return parent;
	}
	
	public void setParent(Artefact artefact) {
		this.parent = artefact;
	}

	public ArrayList<TraceLink> getTargetOf() {
		return targetOf;
	}
	
	public ArrayList<TraceLink> getSourceOf() {
		return sourceOf;
	}
	
	public boolean removeTargetOf(TraceLink tl) {
		return targetOf.remove(tl);
	}
	public boolean removeTargetsOf(Collection<TraceLink> tls) {
		return targetOf.removeAll(tls);
	}
	
	public boolean removeSourceOf(TraceLink tl) {
		return sourceOf.remove(tl);
	}
	public boolean removeSourcesOf(Collection<TraceLink> tls) {
		return sourceOf.removeAll(tls);
	}
	
	
	public boolean addTargetOf(TraceLink tl) {
		return targetOf.add(tl);
	}
	
	public boolean addSourceOf(TraceLink tl) {
		return sourceOf.add(tl);
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}
	
	public String getDefinition() {
		return definition;
	}

	public String getJSon() {
		String res = "{";
		res += "\"id\": \""+getID()+"\",";
		res += "\"name\": \""+getName()+"\",";
		res += getParent()==null?"":"\"parent\": \""+getParent().getID()+"\",";
		res += "\"fragments\": "+Utils.getElementsIDsAsJsonCollection(fragments.values())+",";
		res += "\"type\": \""+getTypeUID()+"\"";
		return res +"}";
	}
}
