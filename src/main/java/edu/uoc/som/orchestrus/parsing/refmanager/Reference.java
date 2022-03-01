package edu.uoc.som.orchestrus.parsing.refmanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.jayway.jsonpath.JsonPath;

import edu.uoc.som.orchestrus.parsing.Tests;
import net.minidev.json.JSONArray;

public class Reference {
	
	
	/*
	 * INFO - regex pattern for urls :
	 * ^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]
	 * 
	 */

	String raw;
	Protocol protocol;
	String location;
	String innerLocation;
	
	public Reference(String strRef) {
		this.raw = strRef;
		this.protocol = extractProtocol();
		this.location = extractLocation();
		this.innerLocation = extractInnerPath();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(!obj.getClass().equals(this.getClass())) return false;
		return this.getRaw().equals(((Reference)obj).getRaw());
	}
	
	public String getRaw() {
		return raw;
	}
	
	private String extractInnerPath() {
		String res = "";
		if(this.raw.contains("#"))
			this.raw.substring(this.raw.indexOf("#")+1);
		return res;
	}

	private String extractLocation() {
		String res = Protocol.removeProtocol(this);
		if(res.contains("#"))
			res = res.substring(0, res.indexOf("#"));
		return res;
	}

	private Protocol extractProtocol() {
		return Protocol.getProtocol(this.raw);
	}
	
	enum Protocol {
		http, pathmap, platform, ppe, no_protocol;

		static Protocol getProtocol(String raw) {
			if(raw.startsWith("http"))
				return http;
			if(raw.startsWith("pathmap"))
				return pathmap;
			if(raw.startsWith("platform"))
				return platform;
			if(raw.startsWith("ppe"))
				return ppe;
			else
				return no_protocol;
		}
		
		static String removeProtocol(Reference r) {
			switch (r.protocol) {
			case http:
			case pathmap:
			case platform:
			case ppe:
				return r.raw.substring(r.protocol.toString().length() + 3);

			case no_protocol:
				return r.raw;				
			default:
				throw new IllegalArgumentException("'"+r.protocol+" unknown.");
			}
		}
	}

	@Override
	public String toString() {
		return "["+protocol+"]"+location+"#"+innerLocation;
	}

	public static Set<Reference> buildReferences(String interArtDependencies_JSON) {
		JSONArray eltsRef = (JSONArray)JsonPath.read(
				interArtDependencies_JSON, 
				"$..[?(@.href)]");
		
		Set<Reference> hrefReferences = new HashSet<>(eltsRef.size());
		for (Object eltRef : eltsRef) {
			@SuppressWarnings("unchecked")
			String href = ((HashMap<String, String>)eltRef).get("href");
			Reference r = new Reference(href);
			hrefReferences.add(r);
			Tests.LOGGER.finest(r.toString());
		}
		Tests.LOGGER.fine(hrefReferences.size() + " href references found");
		
		eltsRef = (JSONArray)JsonPath.read(
				interArtDependencies_JSON, 
				"$..[?(@.value && @.key)]");
		
		Set<Reference> ctxReferences = new HashSet<>(eltsRef.size());
		for (Object eltRef : eltsRef) {
			@SuppressWarnings("unchecked")
			String href = ((HashMap<String, String>)eltRef).get("value");
			Reference r = new Reference(href);
			ctxReferences.add(r);
			Tests.LOGGER.finest(r.toString());
		}
		Tests.LOGGER.fine(ctxReferences.size() + " ctx references found");
	
		return hrefReferences;
	}
}
