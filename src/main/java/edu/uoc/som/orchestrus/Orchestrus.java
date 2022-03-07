package edu.uoc.som.orchestrus;

public class Orchestrus {
	/*
	 *  Arguments:
	 *  - Config file
	 *  - Resolved conflictual FileArtefact paths
	 *  - Link type "translations"
	 */
	
	/*
	 * Run:
	 *  - Extract References (hrefs)
	 *  	o List folders (Config.sourceFiles)
	 *  	o Separate source/local/external (protocol?!)
	 *  	o Resolve source/local that can be solved
	 *  	o Sort out unresolvable -> UX for typing alternative ? (Storage ?)
	 *  - Extract Trace
	 *  	o Extract Source/Local/External File artefact
	 *  	  - Hardcoded typing ?
	 *  	o Build (multiended) links between Source/Local/External artefact
	 *  	  - Source and Local: solve and use Xpath to recover specific elements path
	 *   	  - What about externals ? UX alternative ?
	 *   	o Solve specific elements path 
	 *   	  - Directly where possible: source, resolvable File artefact.
	 *   	  - With UX for external ? 
	 *  - Store Trace in JSon
	 *  	o Trace init links (IDs)
	 *  	o Artefacts
	 * 		o Links
	 *  	o Typing: artefacts & links -> EngineeringDomain !! ("Translations" to ApplicationDomain ?!)
	 *  	o Fragmentation: cluster Paths to shows dependency nests - like common ancestor in the tree (X)path.
	 *  
	 *  	  - WOT ELSE broo ?!!?
	 *  
	 *  - Load trace
	 *  	o Types
	 */
	
}
