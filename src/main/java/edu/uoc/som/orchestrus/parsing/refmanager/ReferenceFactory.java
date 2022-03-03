package edu.uoc.som.orchestrus.parsing.refmanager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

public class ReferenceFactory {
	public final static Logger LOGGER = Logger.getLogger(ReferenceFactory.class.getName());
	
	/*
	 * INFO - regex pattern for urls :
	 * ^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]
	 * 
	 */
	
	
	/**
	 * HREF -> Reference
	 */
	private static HashMap<String, Reference> references = new HashMap<>();
	
	/**
	 * Where did the references where found
	 */
	private static HashMap<Reference, ArrayList<String>> referencesSources = new HashMap<>();
	
	/**
	 * Source to reference
	 */
	private static HashMap<String, ArrayList<Reference>> referencesSourcesReversed = new HashMap<>();
	
	/**
	 * Locations on this computer. (No protocol, resolved or not)
	 */
	private static HashSet<String> locationsLocal = new HashSet<>();
	
	/**
	 * Locations external to this computer. (Protocol)
	 */
	private static HashSet<String> locationsExternal = new HashSet<>();
	
	public static String extractInnerPath(String rawReference) {
		String res = "";
		if (rawReference.contains("#"))
			res = rawReference.substring(rawReference.indexOf("#") + 1);
		return res;
	}

	public static String extractLocation(String rawReference) {
		String res = Protocol.removeProtocol(rawReference);
		if (res.contains("#"))
			res = res.substring(0, res.indexOf("#"));
		return res;
	}

	public static Protocol extractProtocol(String rawReference) {
		return Protocol.getProtocol(rawReference);
	}

	enum Protocol {
		http, pathmap, platform, ppe, bundleclass, no_protocol;

		static Protocol getProtocol(String raw) {
			if (raw.startsWith("http"))
				return http;
			if (raw.startsWith("pathmap"))
				return pathmap;
			if (raw.startsWith("platform"))
				return platform;
			if (raw.startsWith("ppe"))
				return ppe;
			if (raw.startsWith("bundleclass"))
				return bundleclass;
			else
				return no_protocol;
		}

		static String removeProtocol(String rawReference) {
			Protocol p = getProtocol(rawReference);
			switch (p) {
			case http:
			case pathmap:
			case platform:
			case bundleclass:
			case ppe:
				return rawReference.substring(p.toString().length() + 2);

			case no_protocol:
				return rawReference;
			default:
				throw new IllegalArgumentException("'" + p + " unknown.");
			}
		}
	}
	
	/**
	 * Resolve the location of a reference (if it is "no_protocol").
	 * Direction uses '/' instead of '\' for JSon compatibility.
	 * @param sourceFile
	 * @param r
	 * @return
	 */
	public static boolean resolveLocation(String sourceFile, Reference r) {
		if (r.hasNoProtocol()) {
			boolean res = false;
			String resolvedLocation = "-resolvedLocation-";
			try {
				File source = new File(sourceFile);
				File folder = source.getParentFile();

				File f = Path.of(folder.getAbsolutePath(), r.getTargetFileArtefact()).toFile();
				resolvedLocation = f.getCanonicalPath();
				if (f.exists()) {
					r.setLocation(resolvedLocation);
					r.setResolved(true);
					res = true;
				} else {
					LOGGER.warning("Location '" + r.getTargetFileArtefact() + "' in '" + source.getAbsolutePath()
							+ "' did not resolve.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (res) {
				LOGGER.finest(" Resolved -  " + r.getTargetFileArtefact() + " -> " + resolvedLocation);
			} else {
				LOGGER.finest(" !Resolved - " + r.getTargetFileArtefact() + " -> " + resolvedLocation);
			}
			return res;
		}
		return false;
	}

	
	
	public static Reference getReference(String href) {
		return references.get(href);
	}

	public static Reference getReference(String href, String sourceFile) {

		Reference rr = new Reference(href);
		ReferenceFactory.resolveLocation(sourceFile, rr);

		Reference r = references.get(rr.getHREF());
		if (r == null) {
			references.put(rr.getHREF(), rr);
			LOGGER.finest("New ref: " + rr.getHREF());
		} else {
			LOGGER.finest("again: " + rr.getHREF());
		}
		r = references.get(rr.getHREF());

		// Use resolved locations to register references
		if (r.hasProtocol()) {
			locationsExternal.add(r.protocol + "://" + r.getTargetFileArtefact());
		} else {
			locationsLocal.add(r.getTargetFileArtefact());
		}

		if (!referencesSources.keySet().contains(r))
			referencesSources.put(r, new ArrayList<String>());
		referencesSources.get(r).add(sourceFile);

		if (!referencesSourcesReversed.keySet().contains(sourceFile))
			referencesSourcesReversed.put(sourceFile, new ArrayList<Reference>());
		referencesSourcesReversed.get(sourceFile).add(r);

		return r;
	}
	
	public static Collection<Reference> getReferencesValues() {
		return references.values();
	}
	public static HashMap<String, Reference> getReferences() {
		return references;
	}
	
	public static List<Reference> getLocalReferences() {
		return references.values().stream().filter(r -> r.isLocal()).collect(Collectors.toList());
	}
	public static List<Reference> getExternalReferences() {
		return references.values().stream().filter(r -> !r.isLocal()).collect(Collectors.toList());
	}
	
	public static Collection<String> getSourceFiles() {
		return referencesSourcesReversed.keySet();
	}
	
	public static boolean isLocal(Reference r) {
		return locationsLocal.contains(r.getTargetFileArtefact());
	}
	
	/**
	 * 
	 * @return Locations on this computer. (No protocol, resolved or not)
	 */
	public static HashSet<String> getLocationsLocal() {
		return locationsLocal;
	}
	
	/**
	 * 
	 * @return Locations external to this computer (Protocol)
	 */
	public static HashSet<String> getLocationsExternal() {
		return locationsExternal;
	}
	
	public static HashMap<Reference, ArrayList<String>> getReferencesSources() {
		return referencesSources;
	}
	
	public static HashMap<String, ArrayList<Reference>> getReferencesSourcesReversed() {
		return referencesSourcesReversed;
	}
	
	/**
	 * @deprecated
	 * @param interArtDependencies_JSON
	 * @return
	 */
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
			LOGGER.finest(r.toString());
		}
		LOGGER.fine(hrefReferences.size() + " href references found");
		
		eltsRef = (JSONArray)JsonPath.read(
				interArtDependencies_JSON, 
				"$..[?(@.value && @.key)]");
		
		Set<Reference> ctxReferences = new HashSet<>(eltsRef.size());
		for (Object eltRef : eltsRef) {
			@SuppressWarnings("unchecked")
			String href = ((HashMap<String, String>)eltRef).get("value");
			Reference r = new Reference(href);
			ctxReferences.add(r);
			LOGGER.finest(r.toString());
		}
		LOGGER.fine(ctxReferences.size() + " ctx references found");
	
		return hrefReferences;
	}
	

}
