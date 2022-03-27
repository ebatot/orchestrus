package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;
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
	
	HashMap<Artefact, Integer> artefactsIndex;

	double[][] adjacencyMatrix;

	private List<Artefact> artefactsOrdered;
	
	public void computeLinksSize(boolean normalize) {
		computeArtefactsOrdered();
		computeArtefactsIndex();
		computeAdgacencyMatrix();
		
		if(normalize) {
			normalizeAdjacencymatrix();
		}
		assignLinksSize();
	}
	
	private void computeArtefactsOrdered() {
		Set<Artefact> set = new HashSet<>();
		for (TraceLink tl : traceLinks) {
			set.addAll(tl.getSources());
			set.addAll(tl.getTargets());
		}
		artefactsOrdered = new ArrayList<>();
		artefactsOrdered.addAll(set);
		Collections.sort(artefactsOrdered, new Comparator<Artefact>() {
			@Override
			public int compare(Artefact o1, Artefact o2) {
				return o1.getID().compareTo(o2.getID());
			}
		});
	}

	private void computeArtefactsIndex() {
		artefactsIndex = new HashMap<>();
		Artefact[] arts = (Artefact[]) artefactsOrdered.toArray(new Artefact[artefactsOrdered.size()]);
		for (int i = 0; i < arts.length; i++) {
			artefactsIndex.put(arts[i], i);
		}
	}
	
	private void computeAdgacencyMatrix() {
		Artefact[] arts = (Artefact[]) artefactsOrdered.toArray(new Artefact[artefactsOrdered.size()]);
		adjacencyMatrix = new double[arts.length][arts.length];
		for (TraceLink tl : traceLinks) {
			for (int i = 0; i < arts.length; i++) {
				Artefact a1 = arts[i];
				for (int j = 0; j < arts.length; j++) {
					Artefact a2 = arts[j];
					if (tl.getSources().contains(a1) && tl.getTargets().contains(a2))
						adjacencyMatrix[i][j]++;
					if (tl.getTargets().contains(a1) && tl.getSources().contains(a2))
						adjacencyMatrix[i][j]++;
				}
			}
		}
	}
	
	private void assignLinksSize() {
		setTraceConfidence(0);
		for (TraceLink tl : traceLinks) {
			double counter = 0;
			for (Artefact as : tl.getSources()) {
				for (Artefact at : tl.getTargets()) {
					counter += adjacencyMatrix[artefactsIndex.get(as)][artefactsIndex.get(at)];
				}
			}
			tl.setConfidence(counter);
		}
	}
	
	@SuppressWarnings("unused")
	private int countTraceLink(Artefact source, Artefact target, boolean oriented) {
		int res = 0;
		if(oriented)
			for (TraceLink tl : traceLinks) {
				if(tl.getSources().contains(source) && tl.getTargets().contains(target))
					res++;
			}
		else
			for (TraceLink tl : traceLinks) {
				if(tl.getSources().contains(source) && tl.getTargets().contains(target) ||
						tl.getSources().contains(target) && tl.getTargets().contains(source))
					res++;
			}
		return res;
	}
	
	public void normalizeAdjacencymatrix() {
		double[][] n = adjacencyMatrix; 
		double min = n[0][0];
		double max = n[0][0];
		for(int i = 0; i < n.length; i++){
		    for(int j = 0; j < n[0].length; j++){
		        if(n[i][j] < min){
		            min = n[i][j];
		        }
		        if(n[i][j] > max){
		            max = n[i][j];
		        }
		    }
		}  
		double tessiture = max - min;
		if(tessiture == 0)
			return;
		
		for(int i = 0; i < n.length; i++){
		    for(int j = 0; j < n[0].length; j++){
		    	double v = n[i][j] - min;
				v = (v / tessiture);
				adjacencyMatrix[i][j] = v;
		    }
		}
	}

	/**
	 * Replace all current individual trace link confidence values.
	 * @param d
	 */
	private void setTraceConfidence(double d) {
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
	
	public HashSet<LinkType> getAllTraceLinkTypes() {	
		HashSet<LinkType> lts = new HashSet<>();
		for (LinkType tl : LinkType.getTypes().values()) {
			lts.add(tl);
		}
		return lts;
	}

	public String renderHTMLMatrix() {
		return renderHTMLMatrix(Config.MATRIX_DEFAULT_THRESHOLD);
	}
	
	public String renderHTMLMatrix(double acceptanceThreshold) {
		LOGGER.info("Printing "+getName() + " ("+Config.getInstance().getProjectName()+") matrix adjacency in HTML.");
		
		computeLinksSize(true);

		List<Artefact> compressedArtefacts = compressedArtefacts(acceptanceThreshold);
		boolean printEltIDs = true;
		String res = "\t<tr>\n\t\t<th></th>\n";
		for (Artefact a : compressedArtefacts) 
			res += "\t\t<th class=\"linkName\">"+edu.uoc.som.orchestrus.utils.Utils.limitStringSize(a.getName(), 20)+(printEltIDs?"<br/>"+a.getID():"")+"</th>\n";
		res += "\t</tr>\n";
		
		String res2 = "";
		int i = 0;
		for (Artefact a : compressedArtefacts) {
			res2 += "\t<tr>\n";
			res2 += "\t\t<td class=\"linkName\" width=\"150px\">"+i++ + "   "+edu.uoc.som.orchestrus.utils.Utils.limitStringSize(a.getName(), 20)+(printEltIDs?"<br/>"+a.getID():"")+ "</td>\n";
			for (Artefact a2 : compressedArtefacts) {
				double value = adjacencyMatrix[artefactsIndex.get(a)][artefactsIndex.get(a2)];
				double color = 255- (value*255);
				res2 += "\t\t<td class=\"linkCell\" width=\"150px\" "
						+ "style=\"background-color:rgb("+color+",250,250); font-size:'1em'\">";
				res2 += value + ": "+color;
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

	private List<Artefact> compressedArtefacts(double threshold) {
		List<Artefact> res = new ArrayList<>();
		for (Artefact a : artefactsOrdered) {
			boolean keep = false;
			for (int i = 0; i < adjacencyMatrix.length; i++) 
				if(adjacencyMatrix[artefactsIndex.get(a)][i] > threshold) 
					keep = true;
			if(keep)
				res.add(a);
		}
		return res;
	}

	public String renderD3JSon(boolean showElements) {
		return renderD3JSon(showElements, false);
	}

	public String renderD3JSon(boolean showElements, boolean printUnreferencedArtefacts) {
		LOGGER.info("Printing "+getName() + " ("+Config.getInstance().getProjectName()+") in Tracea D3 JSon");
		Set<Artefact> artCollect = collectArtefacts();
		if(!showElements) {
			Artefact[] artCollectTmp = (Artefact[]) artCollect.toArray(new Artefact[artCollect.size()]);
			for (Artefact a : artCollectTmp) {
				if(a.isOfType(ArtefactTypeFactory.ELEMENT_ARTEFACT))
					artCollect.remove(a);
			}
		}
		
		String links = "" ;
		
		//Link sizes attribution
		computeLinksSize(false);
		
		
		for (TraceLink tl : getTraceLinks()) 
				if(showElements || (!showElements && !tl.touchElementOfType(ArtefactTypeFactory.ELEMENT_ARTEFACT)))
					links += tl.getD3Json()+",\n";
			
		if(!links.isBlank())
			links = links.substring(0, links.length()-2);
		links = "\"links\": [" + links + "]";
		
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

	private Set<Artefact> collectArtefacts() {
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
		return artCollect;
	}


}
