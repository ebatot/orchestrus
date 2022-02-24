package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.HashSet;

import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.LinkType;

public class Trace extends TracingElement {
	ArrayList<TraceLink> traceLinks = new ArrayList<TraceLink>();
	
	public Trace() {
	}

	public Trace(String name) {
		super(name);
	}

	public String printJSon() {
		
		String trace = "\"trace\": { \"init\":[";
		for (TraceLink traceLink : traceLinks) 
			trace += " \""+traceLink.getID()+ "\",";
		trace = trace.substring(0, trace.length()-1)+"]}";
		
		String links = "\"links\": [" ;
		for (TraceLink tl : getAllTraceLinks()) 
			links += tl.getJSon()+",\n";
		links = links.substring(0, links.length()-2)+ "]";
		
		// TODO check that ancestry is completly rebuilt - if necessary
		String artefacts = "\"artefacts\": [" ;
		for (Artefact a : getAllArtefacts()) 
			artefacts += a.getJSon()+",\n";
		artefacts = artefacts.substring(0, artefacts.length()-2)+ "]";
		
		String fragments = "\"fragments\": [" ;
		for (Artefact a : getAllArtefacts()) 
			for (Artefact af : a.getFragments().values()) 
				fragments += af.getJSon()+",\n";
		fragments = fragments.substring(0, fragments.length()-2)+ "]";
		
		String artefactTypes = "\"artefactTypes\": [" ;
		for (ArtefactType at : getAllArtefactTypes()) 
			artefactTypes += at.getJSon()+",\n";
		artefactTypes = artefactTypes.substring(0, artefactTypes.length()-2)+ "]";
		
		String tracelinkTypes = "\"tracelinkTypes\": [" ;
		for (LinkType lt : getAllTraceLinkTypes()) 
			tracelinkTypes += lt.getJSon()+",\n";
		tracelinkTypes = tracelinkTypes.substring(0, tracelinkTypes.length()-2)+ "]";
		
		
		return "{\n"+
			trace+",\n"+
			links+",\n"+
			artefacts+",\n"+
			fragments+",\n"+
			artefactTypes+",\n"+
			tracelinkTypes+"\n"+
			"}";
	}
	
	public void addTraceLink(TraceLink tl) {
		traceLinks.add(tl);
	}
	
	public ArrayList<TraceLink> getTraceLinks() {
		return traceLinks;
	}
	
	/**
	 * Transitive closure on trace links targets.
	 * @return
	 */
	public HashSet<TraceLink> getAllTraceLinks() {
		HashSet<TraceLink> tls = new HashSet<>();
		for (TraceLink tl : traceLinks) {
			tls.add(tl);
			tls.addAll(tl.getClosure());
		}
		return tls;
	}
	
	public HashSet<Artefact> getAllArtefacts() {
		HashSet<Artefact> as = new HashSet<>();
		for (TraceLink tl : getAllTraceLinks()) {
			as.addAll(tl.getSourceArtefacts());
			as.addAll(tl.getTargetArtefacts());
		}
		return as;
	}
	
	private HashSet<ArtefactType> getAllArtefactTypes() {	
		HashSet<ArtefactType> ats = new HashSet<>();
		for (Artefact a : getAllArtefacts()) {
			ats.add(a.getType());
		}
		return ats;
	}
	
	private HashSet<LinkType> getAllTraceLinkTypes() {	
		HashSet<LinkType> lts = new HashSet<>();
		for (TraceLink tl : getAllTraceLinks()) {
			lts.add(tl.getType());
		}
		return lts;
	}


}
