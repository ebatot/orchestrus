package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import edu.uoc.som.orchestrus.tracemodel.typing.LinkType;
import edu.uoc.som.orchestrus.tracemodel.typing.TypedLink;

public class TraceLink extends TypedLink {
	
	private double confidence = 100;

	ArrayList<Artefact> sources = new ArrayList<>();
	ArrayList<Artefact> targets = new ArrayList<>();

	public TraceLink(String name, LinkType type) {
		super(name);
		setType(type);
	}

	public TraceLink() {
		this(newName(), LinkType.getUntyped());
	}

	public TraceLink(LinkType type) {
		this(newName(), type);
	}

	public TraceLink(Artefact a, Artefact aa) {
		this(LinkType.getType(a, aa));
		setEnds(a, aa);
	}

	private static int counter = 0;
	private static String newName() {
		return "L" + counter++;
	}

	public ArrayList<Artefact> getSources() {
		return sources;
	}

	public ArrayList<Artefact> getTargets() {
		return targets;
	}
	
	public double getConfidence() {
		return confidence;
	}
	
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}
	
	@Override
	public String toString() {
		return getName() + sources + targets;
	}

//	public Collection<Artefact> getSourceArtefacts() {
//		HashSet<Artefact> res = new HashSet<>(sources.size());
//		for (Artefact af : sources) 
//			res.add(af.getParent());
//		return res;
//	}
//	
//	public Collection<Artefact> getTargetArtefacts() {
//		HashSet<Artefact> res = new HashSet<>(targets.size());
//		for (Artefact af : targets) 
//			res.add(af.getParent());
//		return res;
//	}

	public Collection<TraceLink> getSuccessors() {
		HashSet<TraceLink> res = new HashSet<>();
		for (Artefact af : targets) {
			if (af != null)
				res.addAll(af.getSourceOf());
		}
		return res;
	}

	public Collection<TraceLink> getPredecessors() {
		HashSet<TraceLink> res = new HashSet<>();
		for (Artefact af : sources)
			res.addAll(af.getTargetOf());
		return res;
	}

	public void setEnds(List<Artefact> sources, List<Artefact> targets) {
		this.sources = new ArrayList<>(sources.size());
		this.targets = new ArrayList<>(targets.size());
		addEnds(sources, targets);
	}

	public void addEnds(List<Artefact> sources, List<Artefact> targets) {
		this.sources.addAll(sources);
		this.targets.addAll(targets);
	}

	/**
	 * Set new ends to the link : prior source and target will be removed.
	 * 
	 * @param sources
	 * @param targets
	 */
	public void setEnds(Artefact sources, Artefact targets) {

		for (Artefact af : this.sources)
			af.removeSourceOf(this);
		for (Artefact af : this.targets)
			af.removeTargetOf(this);

		this.sources = new ArrayList<>(1);
		this.targets = new ArrayList<>(1);
		addEnds(sources, targets);
	}

	public void addEnds(Artefact source, Artefact target) {
		this.sources.add(source);
		source.addSourceOf(this);
		this.targets.add(target);
		target.addTargetOf(this);
	}

	public boolean addSource(Artefact newSource) {
		boolean res = this.sources.add(newSource);
		if (res)
			newSource.addSourceOf(this);
		return res;
	}

	public boolean addTarget(Artefact newTarget) {
		boolean res = this.targets.add(newTarget);
		if (res)
			newTarget.addTargetOf(this);
		return res;
	}

	public String getJSon() {
		String res = "{";
		res += "\"id\": \"" + getID() + "\",";
		res += "\"name\": \"" + getName() + "\",";
		res += "\"sources\": " + Utils.getElementsIDsAsJsonCollection(sources) + ",";
		res += "\"targets\": " + Utils.getElementsIDsAsJsonCollection(targets) + ",";
		res += "\"confidence\": "+confidence+",";
		res += "\"type\": \"" + getTypeUID() + "\"";
		return res + "}";
	}

	/**
	 * ONLY MONO ENDED !! Only first source and first target considered ! WARNING !
	 * @return
	 */
	public String getD3Json() {
		String res = "{";
		res += "\"id\": \"" + getID() + "\",";
		res += "\"name\": \"" + confidence + "\",";
		res += "\"source_id\": \"" + sources.get(0).getID() + "\",";
		res += "\"target_id\": \"" + targets.get(0).getID() + "\",";
		res += "\"type\": \"" + getType().getName() + "\",";
		res += "\"group\": " + getType().getNumber() + ",";
		res += "\"confidence\": " + confidence + ",";
		res += "\"energy\": 100";
		return res + "}";
	}

}
