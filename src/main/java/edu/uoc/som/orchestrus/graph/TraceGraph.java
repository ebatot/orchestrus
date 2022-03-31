package edu.uoc.som.orchestrus.graph;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.alg.interfaces.CycleBasisAlgorithm.CycleBasis;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;
import org.jgrapht.graph.DefaultWeightedEdge;
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

	public TraceGraph(Trace t) {
		this.trace = t;
		this.graph = buildGraph(t, false);
		detectCycles();
		
//		-4787022384348844445
//		-2221988569368117268
//		-94187287130004140

		Artefact a1 = ArtefactFactory.getInstance().getArtefactWithID("-4787022384348844445");
		Artefact a2 = ArtefactFactory.getInstance().getArtefactWithID("5392703102889984549");
//		Artefact a3 = ArtefactFactory.getInstance().getArtefactWithID("-94187287130004140");


		getPath( a1,  a2);
		
		renderAsJSon(true);
	}
	
	public Graph<Artefact, WeightedEdge> getGraph() {
		return graph;
	}
	
	public String renderAsJSon(boolean printFile) {

		//Define a vertex attribute provider



				//Create a JSON graph exporter with a vertexIdProvider which tells
				//the exporter how to name each vertex
				JSONExporter<Artefact, WeightedEdge> exporter = new JSONExporter<>(v -> v.getID());
				exporter.setVertexAttributeProvider(getArtefactVertexAttributeProvider());
				exporter.setEdgeAttributeProvider(WeightedEdge.getAttributeProvider());
				
				
				if(printFile) {
					//Export the graph
					File f =  new File("data/out/tmp/jgrapht.json");
					try {
						f.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Graph written in: "+f.getAbsolutePath());
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
							boolean b = g.addEdge(as, at, new WeightedEdge());
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

	public boolean detectCycles() {
		if (graph.getType().isUndirected()) {
			CycleBasis<Artefact, WeightedEdge> cb = new PatonCycleBase<>(graph).getCycleBasis();
			cycles = cb.getCycles();
			LOGGER.fine(cycles.size() + " loops found.");
			if (LOGGER.getLevel() != null && LOGGER.getLevel().intValue() > Level.FINER.intValue())
				for (List<WeightedEdge> cycle : cycles) {
					LOGGER.finer(cycle.size() + " edges loop:");
					for (WeightedEdge e : cycle) {
						LOGGER.finer("  " + e);
					}
				}
			return !cycles.isEmpty();
		} else {
			HawickJamesSimpleCycles<Artefact, WeightedEdge> hjsc = new HawickJamesSimpleCycles<>(graph);
			hjsc.printSimpleCycles();
			return hjsc.countSimpleCycles() > 0;
		}
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
