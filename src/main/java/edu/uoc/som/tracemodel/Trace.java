package edu.uoc.som.tracemodel;

import java.util.ArrayList;
import java.util.HashSet;

public class Trace extends TracingElement {
	ArrayList<TraceLink> traceLinks = new ArrayList<TraceLink>();
	
	public Trace() {
	}

	public Trace(String name) {
		super(name);
	}

	public String printJSon() {
		String json = "";
		for (TraceLink traceLink : traceLinks) {
			
		}
		return null;
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
	
}
