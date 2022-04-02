package edu.uoc.som.orchestrus.graph;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.clustering.GirvanNewmanClustering;
import org.jgrapht.alg.clustering.KSpanningTreeClustering;
import org.jgrapht.alg.clustering.LabelPropagationClustering;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.alg.interfaces.CycleBasisAlgorithm.CycleBasis;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.json.JSONExporter;

import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.ArtefactFactory;
import edu.uoc.som.orchestrus.tracemodel.Trace;
import edu.uoc.som.orchestrus.tracemodel.TraceLink;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;

public class TraceGraph {
	public final static Logger LOGGER = Logger.getLogger(TraceGraph.class.getName());

	Trace trace;
	Graph<Artefact, WeightedEdge> graph;
	Set<List<WeightedEdge>> cycles;

	List<Trace> KSpanClusters;
	List<Trace> GirvanNewmanClusters;
	List<Trace> labelPropagationClusters;
	

	public TraceGraph(Trace t) {
		this.trace = t;
		this.graph = buildGraph(t, false);
		detectCycles(true);
		
//		-4787022384348844445
//		-2221988569368117268
//		-94187287130004140

//		Artefact a3 = ArtefactFactory.getInstance().getArtefactWithID("-94187287130004140");

//		renderAsJSon(true);
		
//		clusterKSpan(2, true);
//		clusterLabelPropagation(50, true);
	}
	
	public boolean testPaths() {
		Artefact a1 = ArtefactFactory.getInstance().getArtefactWithID("-4787022384348844445");
		Artefact a2 = ArtefactFactory.getInstance().getArtefactWithID("5392703102889984549");
		getPath( a1,  a2);
		return true;
	}
	
	List<Trace> clusters = new ArrayList<>();
	
	public List<Set<Artefact>> clusterLabelPropagation(int maxIterations, boolean forceUndirected) {
		LabelPropagationClustering<Artefact, WeightedEdge> ks = null;
		if (forceUndirected)
			ks = new LabelPropagationClustering<>(asUndirectedGraph(), maxIterations);
		else if (graph.getType().isUndirected())
			ks = new LabelPropagationClustering<>(graph, maxIterations);
		else {
			LOGGER.warning("Graph must be undirected (try to force?).");
			return Collections.emptyList();
		}

		return ks.getClustering().getClusters();
	}

	public List<Set<Artefact>> clusterGirvanNewman(int k, boolean forceUndirected) {
		GirvanNewmanClustering<Artefact, WeightedEdge> ks = null;
		if (forceUndirected)
			ks = new GirvanNewmanClustering<>(asUndirectedGraph(), k);
		else if (graph.getType().isUndirected())
			ks = new GirvanNewmanClustering<>(graph, k);
		else {
			LOGGER.warning("Graph must be undirected (try to force?).");
			return Collections.emptyList();
		}

		return ks.getClustering().getClusters();
	}

	public List<Set<Artefact>> clusterKSpan(int kNumber, boolean forceUndirected) {
			KSpanningTreeClustering<Artefact, WeightedEdge> ks = null;
			if (forceUndirected)
				ks = new KSpanningTreeClustering<>(asUndirectedGraph(), kNumber);
			else if (graph.getType().isUndirected())
				ks = new KSpanningTreeClustering<>(graph, kNumber);
			else {
				LOGGER.warning("Graph must be undirected (try to force?).");
				return Collections.emptyList();
			}
	
			return ks.getClustering().getClusters();
		}

	public List<Trace> getLabelPropagationClusters(int maxIterations) {
		if (labelPropagationClusters == null) {
			String prefix = "LabelPropagation_";
			List<Set<Artefact>> clusters = clusterLabelPropagation(maxIterations, true);
			labelPropagationClusters = getClustersAsTraces(clusters, prefix);
			LOGGER.info("LabelPropagation: " + labelPropagationClusters.size() + " clusters found with maxIteration: "
					+ maxIterations);
		}
		return labelPropagationClusters;
	}

	public List<Trace> getGirvanNewmanClusters(int k) {
		if (GirvanNewmanClusters == null) {
			String prefix = "GirvanNewman_";
			List<Set<Artefact>> clusters = clusterGirvanNewman(k, true);
			GirvanNewmanClusters = getClustersAsTraces(clusters, prefix);
			LOGGER.info("GirvanNewman: " + GirvanNewmanClusters.size()+" clusters found with k: "+k);
		}
		return GirvanNewmanClusters;
	}

	public List<Trace> getKSpanClusters(int kNumber) {
		if (KSpanClusters == null) {
			String prefix = "KSpan_";
			List<Set<Artefact>> clusters = clusterKSpan(kNumber, true);
			KSpanClusters = getClustersAsTraces(clusters, prefix);
			LOGGER.info("KSpan: "+KSpanClusters.size()+" clusters found with k: "+kNumber);
		}
		return KSpanClusters;
	}


	private List<Trace> getClustersAsTraces(List<Set<Artefact>> clusters, String prefix) {
		ArrayList<Trace> res = new ArrayList<Trace>();
		int i = 0;
		for (Set<Artefact> c : clusters) {
			Trace t = getTraceFromArtefactSet(c);
			t.setName(prefix+i++);
			res.add(t);
		}
		return res;
	}
	
	
	public Trace getTraceFromArtefactSet(Set<Artefact> cluster) {
		Trace tc = new Trace();
		for (Artefact as : cluster) {
			for (Artefact at : cluster) {
				Set<WeightedEdge> clusterEdges = graph.getAllEdges(as, at);
				for (WeightedEdge edge : clusterEdges) {
					tc.addTraceLink((TraceLink) Trace.getElement(edge.getID()));
				}
			}
		}
		return tc;
	}
	
	
	
	
	public Graph<Artefact, WeightedEdge> getGraph() {
		return graph;
	}

	public String renderAsJSon(boolean printFile) {

		// Define a vertex attribute provider

		// Create a JSON graph exporter with a vertexIdProvider which tells
		// the exporter how to name each vertex
		JSONExporter<Artefact, WeightedEdge> exporter = new JSONExporter<>(v -> v.getID());
		exporter.setVertexAttributeProvider(getArtefactVertexAttributeProvider());
		exporter.setEdgeAttributeProvider(WeightedEdge.getAttributeProvider());

		if (printFile) {
			// Export the graph
			File f = new File("data/out/tmp/jgrapht.json");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Graph written in: " + f.getAbsolutePath());
			exporter.exportGraph(graph, f);
		}

		StringWriter sw = new StringWriter();
		exporter.exportGraph(graph, sw);
		return sw.getBuffer().toString();
	}

	public static Function<Artefact, Map<String, Attribute>> getArtefactVertexAttributeProvider() {
		Function<Artefact, Map<String, Attribute>> vertexAttributeProvider = v -> {
		    Map<String, Attribute> map = new LinkedHashMap<>();
		    map.put("name", DefaultAttribute.createAttribute(v.getName()));
		    map.put("location", DefaultAttribute.createAttribute(v.getLocation()));
		    map.put("type", DefaultAttribute.createAttribute(v.getType().getName()));
		    return map;
		};
		return vertexAttributeProvider;
	}

	public Graph<Artefact, WeightedEdge> buildGraph(Trace t, boolean includeElements) {

		Graph<Artefact, WeightedEdge> g = GraphTypeBuilder.<Artefact, WeightedEdge>directed()
				.allowingMultipleEdges(false).allowingSelfLoops(false).edgeClass(WeightedEdge.class).weighted(true)
				.buildGraph();

		for (Artefact a : ArtefactFactory.getArtefacts().values()) {
			if (includeElements || !a.isOfType(ArtefactTypeFactory.ELEMENT_ARTEFACT))
				g.addVertex(a);
		}

		for (TraceLink tl : trace.getTraceLinks()) {
			for (Artefact at : tl.getTargets()) {
				if (includeElements || !at.isOfType(ArtefactTypeFactory.ELEMENT_ARTEFACT)) {
					for (Artefact as : tl.getSources()) {
						if (includeElements || !as.isOfType(ArtefactTypeFactory.ELEMENT_ARTEFACT)) {
							boolean b = g.addEdge(as, at, new WeightedEdge(tl.getID()));
							if (!b) // The link already exists, increment weight
								g.setEdgeWeight(as, at, g.getEdgeWeight(g.getEdge(as, at)) + 1);
						}
					}
				}
			}
		}

		double weightTotal = 0;
		for (WeightedEdge e : g.edgeSet()) {
			LOGGER.finest("e: " + e);
			weightTotal += e.getWeight();
		}
		LOGGER.info("Verteces: " + g.vertexSet().size() + ", Edges: " + g.edgeSet().size() + " (total weight: "
				+ weightTotal + ")");
		return g;
	}
	
	public boolean detectCycles(boolean forceUndirected) {
		if (forceUndirected || graph.getType().isUndirected()) {
			
			
			boolean change = false;
			if(forceUndirected && graph.getType().isDirected()) {
				graph.getType().asUndirected();
				change = true;
			}
			CycleBasis<Artefact, WeightedEdge> cb = null;
			if (graph.getType().isUndirected()) {
				cb = new PatonCycleBase<>(graph).getCycleBasis();
			} else if (forceUndirected) {
				LOGGER.warning("FORCE UNDIRECTED.");
				cb = new PatonCycleBase<>(asUndirectedGraph()).getCycleBasis();
			} else {
				LOGGER.warning("Graph is directed - undirected required.");
				return false;
			}
			
			cycles = cb.getCycles();
			LOGGER.fine(cycles.size() + " loops found.");
			for (List<WeightedEdge> cycle : cycles) {
				LOGGER.finer(cycle.size() + " edges loop:");
				for (WeightedEdge e : cycle) {
					LOGGER.finer("  " + e);
				}
			}
			if(change)
				graph.getType().asDirected();
			return !cycles.isEmpty();
		} else {
			HawickJamesSimpleCycles<Artefact, WeightedEdge> hjsc = new HawickJamesSimpleCycles<>(graph);
			hjsc.printSimpleCycles();
			return hjsc.countSimpleCycles() > 0;
		}
	}
	
	public  AsUndirectedGraph<Artefact, WeightedEdge> asUndirectedGraph() {
		return  new AsUndirectedGraph<Artefact, WeightedEdge>(graph);
	}

	/**
	 * DEV method, used to show Algorithms results in Sysout
	 */
	public void getPath(Artefact a1, Artefact a2) {
		// computes all the strongly connected components of the directed graph
//        StrongConnectivityAlgorithm<Artefact, WeightedEdge> scAlg =
//            new KosarajuStrongConnectivityInspector<>(graph);
//        List<Graph<Artefact, WeightedEdge>> stronglyConnectedSubgraphs =
//            scAlg.getStronglyConnectedComponents();

        // prints the strongly connected components
//        System.out.println("Strongly connected components:");
//        for (int i = 0; i < stronglyConnectedSubgraphs.size(); i++) {
//            System.out.println(stronglyConnectedSubgraphs.get(i));
//        }
//        System.out.println();

        // Prints the shortest path from vertex i to vertex c. This certainly
        // exists for our particular directed graph.
        System.out.println("Shortest path from "+a1+" to "+a2+":");
        DijkstraShortestPath<Artefact, WeightedEdge> dijkstraAlg =
            new DijkstraShortestPath<Artefact, WeightedEdge>(graph, a1, a2);
        List<WeightedEdge> iPaths = dijkstraAlg.getPathEdgeList();
        if(iPaths != null)
        	System.out.println(iPaths.toString().replace(", ", ",\n "));
        else 
        	System.out.println("No path.");
	}

}
