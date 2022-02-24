package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.Collection;

public class ArtefactFragment extends TracingElement {
	Artefact artefact;
	String definition;
//	ArrayList<ArtefactFragment> subFragments;
	
	ArrayList<TraceLink> sourceOf;
	ArrayList<TraceLink> targetOf;
	
	
	public ArtefactFragment(Artefact artefact, String definition) {
		this(artefact);
		this.definition = definition;
	}
	
	public ArtefactFragment(Artefact artefact) {
		this.artefact = artefact;
		sourceOf = new ArrayList<>();
		targetOf = new ArrayList<>();
	}
	
	public Artefact getArtefact() {
		return artefact;
	}
	
	public void setArtefact(Artefact artefact) {
		this.artefact = artefact;
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
		res += "\"artefact\": \""+getArtefact().getID()+"\",";
		res += "\"definition\": "+definition+"";
		return res +"}";
	}
}
