package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.TypedArtefact;

public class Artefact extends TypedArtefact {
	
	public final static Logger LOGGER = Logger.getLogger(Artefact.class.getName());

	private String location;
	private Artefact parent;
	private String definition;

	private boolean resolves;

	private HashMap<String, Artefact> fragments = new HashMap<>();
	private ArrayList<TraceLink> sourceOf;
	private ArrayList<TraceLink> targetOf;

	public Artefact(String name, ArtefactType type, String location, Artefact parent, boolean resolved) {
		super(name, type);
		this.parent = parent;
		this.location = location;
		sourceOf = new ArrayList<>();
		targetOf = new ArrayList<>();
	}

	public Artefact(String name, ArtefactType type, String location, Artefact parent) {
		this(name, type, location, parent, false);
	}

	public Artefact(String name, ArtefactType type) {
		this(name, type, null, null, false);
	}
	
	public String getJSon() {
		String res = "{";
		res += "\"id\": \"" + getID() + "\",";
		res += "\"name\": \"" + getName() + "\",";
		res += getParent() == null ? "" : "\"parent\": \"" + getParent().getID() + "\",";
		res += "\"fragments\": " + Utils.getElementsIDsAsJsonCollection(fragments.values()) + ",";
		res += "\"type\": \"" + getTypeUID() + "\"";
		return res + "}";
	}
	
	@Override
	public String toString() {
		return this.getType().getName()+"["+this.getName()+"]";
	}


	public boolean isResolves() {
		return resolves;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public void addFragment(Artefact af) {
		fragments.put(af.getID(), af);
		af.setParent(this);
		LOGGER.finest("Fragment: " + af + " ADDED to: " + this);
	}

	public String getLocation() {
		return location;
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

	public boolean isOfType(ArtefactType at) {
		return getType().getName().equals(at.getName());
	}

	public boolean isOfType(String typeName) {
		return getType().getName().equals(typeName);
	}
}
