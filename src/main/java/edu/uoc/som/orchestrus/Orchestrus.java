package edu.uoc.som.orchestrus;

import java.util.Arrays;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.parsing.StaticExplorer;
import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.ArtefactFactory;
import edu.uoc.som.orchestrus.tracemodel.Trace;
import edu.uoc.som.orchestrus.tracemodel.TraceFactory;
import edu.uoc.som.orchestrus.utils.Utils;

public class Orchestrus {
	public final static Logger LOGGER = Logger.getLogger(Orchestrus.class.getName());

	
	public static void main(String[] args) {
		System.out.println("    --  o· o - O ~ o - o ~ o · O ·--");
		System.out.println("    --                            --");
		System.out.println("    -- --      Orchestrus      -- --");
		System.out.println("    --                            --");
		System.out.println("    --------------------------------\n");


/*
 * Init.
 * builds references from source files, 
 * then builds artefacts as sources and targets of these references.		
 */
		Config config = Config.getInstance();
		StaticExplorer ppse = new StaticExplorer(config);
		String interArtDependencies_JSON = ppse.getInterArtefactReferences_Json();
		Utils.storeDependencies_HC(interArtDependencies_JSON);

		// Build artefacts from Sources and References
		ArtefactFactory aFactory = ArtefactFactory.getInstance();
		aFactory.buildArtefacts();
		aFactory.clusterExternalLocations(); 
		// Relates artefacts with fragmentation links.
		TraceFactory.fragmentSourcesAndFolders();
		LOGGER.info(ArtefactFactory.getArtefacts().size()+ " artefacts found.");
		
		Trace tFrag = TraceFactory.buildFragmentationTrace();
		Utils.storeD3Tracea(tFrag, false, true);
		
		// TODO resolve IDs from target file.
//		ppse.resolveElementIDs();
		// TODO Decompose artefacts with XPath patterns.


		Trace t = TraceFactory.buildReferencesTrace();
		Utils.storeD3Tracea(t, false, true, "R:\\Coding\\Git\\orchestrus\\meta\\d3viewer\\data\\input_trace_data.json");
		Utils.storeMatrixTracea(t, false, 4/21);
		Utils.storeSetupJSon(t, true);
		
		
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

	public static void printArtefactSignatures() {
		System.out.println("Orchestrus.printArtefactSignatures()");
		String[] keys = (String[])  ArtefactFactory.getArtefacts().keySet().toArray(new String[ ArtefactFactory.getArtefacts().keySet().size()]);
		
		Arrays.sort(keys);
		
		for (String key : keys) {
			Artefact a = ArtefactFactory.getArtefacts().get(key);
			System.out.println(key + ":\t\t "+a);
		}
	}
	
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
