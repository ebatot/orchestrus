package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.LinkType;

public class Trace extends TracingElement {
	public final static Logger LOGGER = Logger.getLogger(Trace.class.getName());

	HashSet<TraceLink> traceLinks = new HashSet<TraceLink>();
	
	public Trace() {
	}

	public Trace(String name) {
		super(name);
	}

	public String printTraceaJSon() {
		
		String trace = "";
		for (TraceLink traceLink : traceLinks) 
			trace += " \""+traceLink.getID()+ "\",";
		if(!trace.isBlank())
			trace = trace.substring(0, trace.length()-1);
		trace = "\"trace\": { \"init\":[" + trace+"]}";
		
		String links = "" ;
		for (TraceLink tl : getTraceLinks()) 
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
	
	
	
	public int[][] getAdgacencyMatrix() {
		int[][] matrix;
		Artefact[] arts = (Artefact[]) getArtefactsOrdered().toArray(new Artefact[getArtefactsOrdered().size()]);
		matrix = new int[arts.length][arts.length];
		for (TraceLink tl : traceLinks) {
			for (int i = 0; i < arts.length; i++) {
				Artefact a1 = arts[i];
				for (int j = 0; j < arts.length; j++) {
					Artefact a2 = arts[j];
					if (tl.getSources().contains(a1) && tl.getTargets().contains(a2))
						matrix[i][j]++;
					if (tl.getTargets().contains(a1) && tl.getSources().contains(a2))
						matrix[i][j]++;
				}
			}
		}
		return matrix;
	}
	
	public HashMap<Artefact, Integer> getArtefactIndexes() {
		HashMap<Artefact, Integer> artefactIndexes = new HashMap<>();
		Artefact[] arts = (Artefact[]) getArtefactsOrdered().toArray(new Artefact[getArtefactsOrdered().size()]);
		for (int i = 0; i < arts.length; i++) {
			artefactIndexes.put(arts[i], i);
		}
		return artefactIndexes;
	}
	
	
	public void computeLinksSize() {
		int[][] matrix = getAdgacencyMatrix();
		HashMap<Artefact, Integer> artefactIndexes = getArtefactIndexes();
		
		setTraceConfidence(0);
		for (TraceLink tl : traceLinks) {
			int counter = 0;
			for (Artefact as : tl.getSources()) {
				for (Artefact at : tl.getTargets()) {
					counter += matrix[artefactIndexes.get(as)][artefactIndexes.get(at)];
				}
			}
			tl.setConfidence(counter);
		}
		normalizeLinksSize();
	}
	
	public void normalizeLinksSize() {
		int max = Collections.max(traceLinks, new Comparator<TraceLink>() {
			@Override
			public int compare(TraceLink o1, TraceLink o2) {
				return Double.compare(o1.getConfidence(), o2.getConfidence());
			}
		}).getConfidence();
		int min = Collections.min(traceLinks, new Comparator<TraceLink>() {
			@Override
			public int compare(TraceLink o1, TraceLink o2) {
				return Double.compare(o1.getConfidence(), o2.getConfidence());
			}
		}).getConfidence();
		int tessiture = max - min;
		if(tessiture == 0)
			return;
		
		for (TraceLink tl : traceLinks) {
			int confidence = tl.getConfidence();
			confidence = confidence - min;
			confidence = confidence * (1 / tessiture);
			tl.setConfidence(confidence);
		}
		
	}
	
	public int[][] normalizedAdjacencymatrix(int[][] m) {
		int[][] n = new int[m.length][m.length]; // SQUARE MAtRIX !
		int min = m[0][0];
		int max = m[0][0];
		for(int i = 0; i < m.length; i++){
		    for(int j = 0; j < m[0].length; j++){
		        if(m[i][j] < min){
		            min = m[i][j];
		        }
		        if(m[i][j] > max){
		            max = m[i][j];
		        }
		    }
		}  
		double tessiture = max - min;
		if(tessiture == 0)
			return m;
		
		for(int i = 0; i < m.length; i++){
		    for(int j = 0; j < m[0].length; j++){
		    	int v = m[i][j];
				v = v - min;
				v = (int) (v * Math.ceil(1 / tessiture));
				n[i][j] = v;
		    }
		}
		return n;
	}

	/**
	 * Replace all current individual trace link confidence values.
	 * @param d
	 */
	private void setTraceConfidence(int d) {
		for (TraceLink tl : traceLinks) {
			tl.setConfidence(d);
		}
	}

	public void addTraceLink(TraceLink tl) {
		traceLinks.add(tl);
	}
	
	public HashSet<TraceLink> getTraceLinks() {
		return traceLinks;
	}
	
	public List<Artefact> getArtefactsOrdered() {
		HashSet<Artefact> res = new HashSet<>();
		for (TraceLink tl : traceLinks) {
			res.addAll(tl.getSources());
			res.addAll(tl.getTargets());
		}
		ArrayList<Artefact> arts = new ArrayList<>(res.size());
		arts.addAll(res);
		Collections.sort(arts, new Comparator<Artefact>() {
			@Override
			public int compare(Artefact o1, Artefact o2) {
				return o1.getID().compareTo(o2.getID());
			}
		});
		return arts;
	}
	
	public HashSet<LinkType> getAllTraceLinkTypes() {	
		HashSet<LinkType> lts = new HashSet<>();
		for (LinkType tl : LinkType.getTypes().values()) {
			lts.add(tl);
		}
		return lts;
	}

	public String printHTMLMatrix() {
		int[][] matrix = getAdgacencyMatrix();
		List<Artefact> arts = getArtefactsOrdered();
		HashMap<Artefact, Integer> artsIdx = getArtefactIndexes();
		int[][] m = normalizedAdjacencymatrix(matrix);
		
		boolean printEltIDs = true;
		String res = "\t<tr>\n\t\t<th></th>\n";
		for (Artefact a : arts) 
			res += "\t\t<th class=\"linkName\">"+edu.uoc.som.orchestrus.utils.Utils.limitStringSize(a.getName(), 20)+(printEltIDs?"<br/>"+a.getID():"")+"</th>\n";
		res += "\t</tr>\n";
		
		String res2 = "";
		for (Artefact a : arts) {
			res2 += "\t<tr>\n";
			res2 += "\t\t<td class=\"linkName\" width=\"150px\">"+edu.uoc.som.orchestrus.utils.Utils.limitStringSize(a.getName(), 20)+(printEltIDs?"<br/>"+a.getID():"")+ "</td>\n";
			for (Artefact a2 : arts) {
				double color = 255- (((m[artsIdx.get(a)][artsIdx.get(a2)]*255)/100));
				res2 += "\t\t<td class=\"linkCell\" width=\"150px\" style=\"background-color:rgb("+color+",250,250); font-size:1em\">";
				res2 += m[artsIdx.get(a)][artsIdx.get(a2)] + ": "+color;
				res2 += "</td>\n";
			}
			res2 += "\t</tr>\n";
		}
		
		res2 += "\n";
		String table = "<table border=1 style=\"border-collapse: collapse;\">\n" + res + res2 + "</table>";
		String HEADER  = "<html>\r\n"
				+ "<head>\r\n"
				+ "<style>\r\n"
				+ "table {\r\n"
				+ "  width: 100%;\r\n"
				+ "  border: 1px solid black;\r\n"
				+ "  border-collapse: collapse;\r\n"
				+ "}\r\n"
				+ "th {\r\n"
				+ "  background-color: #04AA6D;\r\n"
				+ "  color: white;\r\n"
				+ "}\r\n"
				+ "tr { width:100px; }\r\n"
				+ "tr:hover {background-color: yellow;}\r\n"
				+ "tr:nth-child(even) {background-color: #f2f2f2;}\r\n"
				+ ".linkName{\r\n"
				+ "  font-family: verdana;\r\n"
				+ "  font-size: 15px;\r\n"
				+ "  font-style: bold;\r\n"
				+ "}\r\n"
				+ "\r\n"
				+ "</style>\r\n"
				+ "</head>\r\n"
				+ "<body>\n"
				+ "<h1>Trace matrix</h1>\n"
				+ "\t<div style=\"overflow-x:auto;\">\n";
		return  HEADER + table + "\n\t</div>\n</body>" ;
	}

	public String printD3JSon() {
		return printD3JSon(false);
	}

	public String printD3JSon(boolean printUnreferencedArtefacts) {
		Set<Artefact> artCollect = new HashSet<>();
		for (TraceLink tl : getTraceLinks()) {
			artCollect.addAll(tl.getSources());
			artCollect.addAll(tl.getTargets());
		}
		LOGGER.fine(artCollect.size() + "/"+ArtefactFactory.getArtefacts().values()+ " artefacts referenced.");
		
		for (Artefact a : ArtefactFactory.sortArtefactsByLocation(ArtefactFactory.getArtefacts().values())) {
			if(!artCollect.contains(a)) {
				LOGGER.finest("[DEV] !! missing: "+a + " in collection list.");
			}
		}
		for (Artefact a : ArtefactFactory.sortArtefactsByLocation(artCollect)) {
			if(!ArtefactFactory.getArtefacts().values().contains(a)) {
				LOGGER.finest("[DEV] !! missing: "+a + " in complete list.");
			}
		}
		
		String links = "" ;
		
		//TODO Link sizes attribution
		computeLinksSize();
		
		for (TraceLink tl : getTraceLinks()) 
			links += tl.getD3Json()+",\n";
		if(!links.isBlank())
			links = links.substring(0, links.length()-2);
		links = "\"links\": [" + links + "]";
		
		// print ALL artefacts, IN THE UNIVERSE !
		String nodes = "";
		if(printUnreferencedArtefacts)
			artCollect.addAll(ArtefactFactory.getArtefacts().values());
		for (Artefact a : artCollect) 
			nodes += a.getD3JSon()+",\n";
		if(!nodes.isBlank())
			nodes = nodes.substring(0, nodes.length()-2);
		nodes = "\"nodes\": [" + nodes + "]";
		
		return "{\n"+
					links+",\n"+
					nodes+"\n"+
				"}";
	}


}
