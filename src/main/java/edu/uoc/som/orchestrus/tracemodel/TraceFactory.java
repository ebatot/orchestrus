package edu.uoc.som.orchestrus.tracemodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import edu.uoc.som.orchestrus.parsing.StaticExplorer;
import edu.uoc.som.orchestrus.parsing.refmanager.Reference;
import edu.uoc.som.orchestrus.parsing.refmanager.ReferenceFactory;
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
	
	public void buildLinks() {
		fragmentSourcesAndFolders();
		linkReferencesSourceToTargets();

	}

	private void fragmentSourcesAndFolders() {
		Collection<Artefact> sourceArts = ArtefactFactory.getInstance()
				.subsetsArtefactsByType(ArtefactTypeFactory.SOURCE_FILE_ARTEFACT);
		for (Artefact sArt : sourceArts) {
			File f = new File(sArt.getLocation());
			String location = f.getParent();
			Artefact parentArt = ArtefactFactory.getInstance().getArtefact(location + f.getName());
			parentArt.addFragment(sArt);
		}
	}

	private void linkReferencesSourceToTargets() {
		Trace t = new Trace();
		for (Reference r : ReferenceFactory.getReferences().values()) {
			TraceLink tl = new TraceLink();
			HashMap<String, ArrayList<Reference>> sourcesToRef = StaticExplorer.getReferencesSourcesReversed();
			for (String sSource : sourcesToRef.keySet()) {
				// Pour chaque source, chercher les references qui la contiennent
				if(r.containsSource(sSource)) {
					// l'ajouter au lien
					Artefact a = ArtefactFactory.getInstance().getArtefact(new File(sSource));
					if(a == null) {
						throw new IllegalAccessError("Should not get there. Artefact not recognized");
					}
					tl.addSource(a);
				} 
			}
			
			Artefact target = ArtefactFactory.getInstance().getArtefact(r);
			if(target == null) {
				throw new IllegalAccessError("Should not get there. Artefact not recognized");
			}
			tl.addTarget(target);
			t.addTraceLink(tl);
//			System.out.println("tl:\n  "+tl.getSources()+"\n  "+tl.getTargets());
		}
		
		File f = new File("R:\\Coding\\Git\\orchestrus\\data\\GlossaryML-ReferenceML\\traceSample.json");
		try {
			FileUtils.write(f, t.printJSon());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Pour chaque reference, chercher les href qui 
		
		
		
//		Collection<Artefact> artefacts = ArtefactFactory.getInstance().getArtefacts().values();
//		
//		Trace t = new Trace();
//		HashMap<String, ArrayList<Reference>> sourcesToRef = StaticExplorer.getReferencesSourcesReversed();
//		for (String sSource : sourcesToRef.keySet()) {
//			ArrayList<Reference> refs = sourcesToRef.get(sSource);
//			System.out.println(refs);
//		}
//		
//		
//		
//		
//		for (String sSource : sourcesToRef.keySet()) {
//			File fSource = new File(sSource);
//			String location = fSource.getParent();
//			String name = fSource.getName();
//			ArrayList<Reference> refs = sourcesToRef.get(sSource);
//			System.out.println("TraceFactory.linkReferencesSourceToTargets("+sSource+")");
//			List<Artefact> targets = new ArrayList<Artefact>(refs.size());
//			for (Reference r : refs) {
////				Artefact ar = ArtefactFactory.getInstance().getArtefact(r.getTargetFileArtefact()+r.getTargetFileArtefact());
//				
//				for (String ss : r.getSources()) {
//					Artefact ssArt = ArtefactFactory.getInstance().getArtefact(new File(ss));
//				}
//				
//				Artefact ar = ArtefactFactory.getInstance().getArtefact(r);
//				System.out.println("   "+ar);
//				targets.add(ar);
//			}
//			
//			Artefact source = ArtefactFactory.getInstance().getArtefact(fSource);
//			System.out.println(source);
//			if(source == null)
//				System.exit(1);
//			for (Artefact a : targets) {
//				System.out.println("  - "+a);
//				if(a == null)
//					System.exit(1);
//				
//			}
//			TraceLink tl2 = new TraceLink();
//			tl2.addEnds(Arrays.asList(new Artefact[] {source}), targets);
//			t.addTraceLink(tl2);
//		}
//
//		
//		System.out.println(t.printJSon());
//		
//		File f = new File("R:\\Coding\\Git\\orchestrus\\data\\GlossaryML-ReferenceML\\traceSAmple.json");
//		try {
//			FileUtils.write(f, t.printJSon());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		for (Reference r : references) {
//			System.out.println(r.getTargetFileArtefact());
//			if(r.isLocal()) {
//				System.out.println("local");
//			}
//		}
	}


}
