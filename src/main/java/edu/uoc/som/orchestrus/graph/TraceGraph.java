package edu.uoc.som.orchestrus.graph;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.alg.interfaces.CycleBasisAlgorithm.CycleBasis;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

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
	}
	
	public Graph<Artefact, WeightedEdge> getGraph() {
		return graph;
	}

	public Graph<Artefact, WeightedEdge> buildGraph(Trace t, boolean includeElements) {

		Graph<Artefact, WeightedEdge> g = GraphTypeBuilder.<Artefact, WeightedEdge>undirected()
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

	class WeightedEdge extends DefaultWeightedEdge {
		private static final long serialVersionUID = 1L;

		@Override
		protected double getWeight() {
			return super.getWeight();
		}

		public WeightedEdge() {
			super();
		}

		@Override
		public String toString() {
			return String.valueOf(getWeight());// super.toString() + "[" + getWeight() + "]";
		}
	}

	public boolean detectCycles() {

//		-4787022384348844445
//		-2221988569368117268
//		-94187287130004140

//		Artefact a1 = ArtefactFactory.getInstance().getArtefactWithID("-4787022384348844445");
//		Artefact a2 = ArtefactFactory.getInstance().getArtefactWithID("-2221988569368117268");
//		Artefact a3 = ArtefactFactory.getInstance().getArtefactWithID("-94187287130004140");

		CycleBasis<Artefact, WeightedEdge> cb = new PatonCycleBase<>(graph).getCycleBasis();
		cycles = cb.getCycles();
		LOGGER.fine(cycles.size()+" loops found.");
		if (LOGGER.getLevel() != null && LOGGER.getLevel().intValue() > Level.FINER.intValue())
			for (List<WeightedEdge> cycle : cycles) {
				LOGGER.finer(cycle.size()+" edges loop:");
				for (WeightedEdge e : cycle) {
					LOGGER.finer("  " + e);
				}
			}
		return !cycles.isEmpty();
	}

}
