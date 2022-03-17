package edu.uoc.som.orchestrus.tracemodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.parsing.Reference;
import edu.uoc.som.orchestrus.parsing.ReferenceFactory;
import edu.uoc.som.orchestrus.parsing.StaticExplorer;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;

public class TraceFactory {
	public final static Logger LOGGER = Logger.getLogger(TraceFactory.class.getName());

	static TraceFactory instance;

	public static TraceFactory getInstance() {
		if (instance == null)
			instance = new TraceFactory();
		return instance;
	}

	public TraceFactory() {
		links = new HashSet<TraceLink>();
	}

	Set<TraceLink> links;
	
	public void fragmentSourcesAndFolders() {
		Collection<Artefact> sourceArts = ArtefactFactory.subsetsArtefactsByType(ArtefactTypeFactory.SOURCE_FILE_ARTEFACT);
		for (Artefact sArt : sourceArts) {
			File f = new File(sArt.getLocation());
			Artefact parentArt = ArtefactFactory.getInstance().getArtefact(f);
			parentArt.addFragment(sArt);
		}
		LOGGER.fine("Done: Each file has its parent folder as parent.");
	}

	public Trace buildBaseTrace() {
		Trace t = new Trace();
		LOGGER.info(ReferenceFactory.getReferences().size() + " references.");
		for (Reference r : ReferenceFactory.getReferences().values()) {
			TraceLink tl = new TraceLink();
			HashMap<String, ArrayList<Reference>> sourcesToRef = StaticExplorer.getReferencesSourcesReversed();
			for (String sSource : sourcesToRef.keySet()) {
				// Pour chaque source, chercher les references qui la contiennent
				if(r.containsSource(sSource)) {
					// l'ajouter au lien
					Artefact a = ArtefactFactory.getInstance().getArtefact(new File(sSource));
					if(a == null) {
						throw new IllegalAccessError("Should not get there. Artefact not recognized.");
					}
					tl.addSource(a);
				} 
			}
			
			Artefact target = ArtefactFactory.getInstance().getArtefact(r);
			if(target == null) {
				throw new IllegalAccessError("Should not get there. Artefact not recognized from ref: "+r.getHREF());
			}
			tl.addTarget(target);
			LOGGER.finer("Link added:" + tl + " sources:"+tl.getSources().size()+ " targets:"+tl.getTargets().size());
			t.addTraceLink(tl);
		}
		LOGGER.fine(t.getTraceLinks().size()+" trace link added.");
		return t;
	}


}
