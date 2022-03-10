package edu.uoc.som.orchestrus;

import edu.uoc.som.orchestrus.parsing.StaticExplorer;
import edu.uoc.som.orchestrus.tracemodel.ArtefactFactory;
import edu.uoc.som.orchestrus.tracemodel.Trace;
import edu.uoc.som.orchestrus.tracemodel.TraceFactory;
import edu.uoc.som.orchestrus.utils.Utils;

public class Orchestrus {
	
	
	public static void main(String[] args) {
		System.out.println("    --  o· o - O ~ o - o ~ o · O ·--");
		System.out.println("    --                            --");
		System.out.println("    -- --      Orchestrus      -- --");
		System.out.println("    --                            --");
		System.out.println("    --------------------------------\n");

//		Config config = Config.getInstance();

		
		StaticExplorer ppse = new StaticExplorer();
		String interArtDependencies_JSON = ppse.getInterArtefactReferences_Json();
		Utils.storeDependencies_HC(interArtDependencies_JSON);

		// Build artefacts from Sources and References
		ArtefactFactory aFactory = ArtefactFactory.getInstance();
		aFactory.buildArtefacts();
		
		
		TraceFactory tFactory = TraceFactory.getInstance();
		// TODONE Connect fragments.
		tFactory.fragmentSourcesAndFolders();
		// TODONE connect artefacts with links.
		Trace t = tFactory.buildBaseTrace();
		
		
		Utils.storeTrace_HC(t);

		// TODO Decompose artefacts with XPath patterns.




		/*
		 * Check which files are left - Javas - Manifest & Pom - Class path &
		 * .properties - plugin.xml
		 */

		System.err.flush();
		System.out.println("\n\n-- Safe Exit o·~ !¡");
	}
	/*
	 *  Arguments:
	 *  - Config file
	 *    - project = com.cea.papyrus.glossary
	 * 	  - projectRoot = R:\Coding\Git\orchestrus\data\GlossaryML-ReferenceML
	 * 	  - projectName = GlossaryML
	 * 	  - projectDependencies = com.cea.papyrus.referencemanagement
	 *    - File extensions ?

	 *  - Resolved conflictual FileArtefact paths
	 *  - Link type "translations"
	 *  
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
