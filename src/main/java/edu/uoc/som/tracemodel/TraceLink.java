package edu.uoc.som.tracemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import edu.uoc.som.tracemodel.typing.LinkType;
import edu.uoc.som.tracemodel.typing.LinkTypeFactory;
import edu.uoc.som.tracemodel.typing.TypedLink;

public class TraceLink extends TypedLink {
	
	public TraceLink(String name, LinkType type) {
		super(name);
		setType(type);
	}
	public TraceLink() {
		setType(LinkTypeFactory.getUntyped());
	}
	
	
	public TraceLink(LinkType type) {
		setType(type);
	}
	
	ArrayList<ArtefactFragment> sources = new ArrayList<>();
	ArrayList<ArtefactFragment> targets = new ArrayList<>();
	
	public ArrayList<ArtefactFragment> getSources() {
		return sources;
	}
	
	public ArrayList<ArtefactFragment> getTargets() {
		return targets;
	}
	
	public Collection<Artefact> getSourceArtefacts() {
		HashSet<Artefact> res = new HashSet<>(sources.size());
		for (ArtefactFragment af : sources) 
			res.add(af.getArtefact());
		return res;
	}
	
	public Collection<Artefact> getTargetArtefacts() {
		HashSet<Artefact> res = new HashSet<>(targets.size());
		for (ArtefactFragment af : targets) 
			res.add(af.getArtefact());
		return res;
	}
	
	public Collection<TraceLink> getSuccessors() {
		HashSet<TraceLink> res = new HashSet<>();
		for (ArtefactFragment af : targets) 
			res.addAll(af.getSourceOf());
		return res;
	}
	
	public Collection<TraceLink> getPredecessors() {
		HashSet<TraceLink> res = new HashSet<>();
		for (ArtefactFragment af : sources) 
			res.addAll(af.getTargetOf());
		return res;
	}
	
	public void addSourceArtefact(ArtefactFragment af) {
		sources.add(af);
	}
	public void addTargetArtefact(ArtefactFragment af) {
		targets.add(af);
	}
	
	public HashSet<TraceLink> getClosure() {
		HashSet<TraceLink> tls = new HashSet<>();
		tls.addAll(getSuccessors());
		TraceLink[] tlsArray = (TraceLink[]) tls.toArray(new TraceLink[tls.size()]);
		for (TraceLink traceLink : tlsArray) 
			tls.addAll(traceLink.getClosure());
		return tls;
	}
	
	public void setEnds(List<ArtefactFragment> sources, List<ArtefactFragment> targets) {
		this.sources = new ArrayList<>(sources.size());
		this.targets = new ArrayList<>(targets.size());
		addEnds(sources, targets);
	}
	
	public void addEnds(List<ArtefactFragment> sources, List<ArtefactFragment> targets) {
		this.sources.addAll(sources);
		this.targets.addAll(targets);
	}
	
	/**
	 * Set new ends to the link : prior source and target will be removed.
	 * @param sources
	 * @param targets
	 */
	public void setEnds(ArtefactFragment sources, ArtefactFragment targets) {
		
		for (ArtefactFragment af : this.sources) 
			af.removeSourceOf(this);
		for (ArtefactFragment af : this.targets) 
			af.removeTargetOf(this);
		
		this.sources = new ArrayList<>(1);
		this.targets = new ArrayList<>(1);
		addEnds(sources, targets);
	}
	
	public void addEnds(ArtefactFragment source, ArtefactFragment target) {
		this.sources.add(source);
		source.addSourceOf(this);
		this.targets.add(target);
		target.addTargetOf(this);
	}
	
	public boolean addSource(ArtefactFragment newSource) {
		boolean res = this.sources.add(newSource);
		if(res)
			newSource.addSourceOf(this);
		return res;
	}
	
	public boolean addTarget(ArtefactFragment newTarget) {
		boolean res = this.sources.add(newTarget);
		if(res)
			newTarget.addTargetOf(this);
		return res;
	}
	
	public String getJSon() {
		String res = "{";
		res += "\"id\": \""+getID()+"\",";
		res += "\"name\": \""+getName()+"\",";
		res += "\"sources\": "+Utils.getElementsIDsAsJsonCollection(sources)+",";
		res += "\"targets\": "+Utils.getElementsIDsAsJsonCollection(targets)+",";
		res += "\"type\": \""+getTypeUID()+"\"";
		return res +"}";
	}
}

