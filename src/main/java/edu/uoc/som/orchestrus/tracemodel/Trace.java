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
		
		String trace = "";
		for (TraceLink traceLink : traceLinks) 
			trace += " \""+traceLink.getID()+ "\",";
		if(!trace.isBlank())
			trace = trace.substring(0, trace.length()-1);
		trace = "\"trace\": { \"init\":[" + trace+"]}";
		
		String links = "" ;
		for (TraceLink tl : getAllTraceLinks()) 
			links += tl.getJSon()+",\n";
		if(!links.isBlank())
			links = links.substring(0, links.length()-2);
		links = "\"links\": [" + links + "]";
		
		// print ALL artefacts, IN THE UNIVERSE !
		String artefacts = "\"artefacts\": [" ;
		for (Artefact a : ArtefactFactory.getArtefacts().values()) 
			artefacts += a.getJSon()+",\n"; 
		artefacts = artefacts.substring(0, artefacts.length()-2)+ "]";
		
		String artefactTypes = "\"artefactTypes\": [" ;
		for (ArtefactType at : ArtefactFactory.getAllArtefactTypes()) 
			artefactTypes += at.getJSon()+",\n";
		artefactTypes = artefactTypes.substring(0, artefactTypes.length()-2)+ "]";
		
		String tracelinkTypes = "" ;
		for (LinkType lt : LinkType.getTypes().values()) 
			tracelinkTypes += lt.getJSon()+",\n";
		if(!tracelinkTypes.isBlank())
			tracelinkTypes = tracelinkTypes.substring(0, tracelinkTypes.length()-2);
		tracelinkTypes = "\"tracelinkTypes\": [" + tracelinkTypes + "]";
		
		return "{\n"+
			trace+",\n"+
			links+",\n"+
			artefacts+",\n"+
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
	
	/**
	 * @deprecated
	 * @return
	 */
	public HashSet<Artefact> getAllArtefactsConnected() {
		HashSet<Artefact> as = new HashSet<>();
		for (TraceLink tl : getAllTraceLinks()) {
			as.addAll(tl.getSources());
			as.addAll(tl.getTargets());
		}
		return as;
	}
	
	public HashSet<LinkType> getAllTraceLinkTypes() {	
		HashSet<LinkType> lts = new HashSet<>();
		for (TraceLink tl : getAllTraceLinks()) {
			lts.add(tl.getType());
		}
		return lts;
	}


}
