package edu.uoc.som.orchestrus.graph;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;

import edu.uoc.som.orchestrus.graph.TraceGraph.WeightedEdge;
import edu.uoc.som.orchestrus.tracemodel.Artefact;

public class ShowGraph {
	TraceGraph graph;
	public ShowGraph( TraceGraph tg) {
		this.graph = tg;
	}
	
	public void createAndShowGui() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("Show Trace Graph");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				Graph<Artefact, WeightedEdge> g = graph.getGraph();
				JGraphXAdapter<Artefact, WeightedEdge> graphAdapter = new JGraphXAdapter<Artefact, WeightedEdge>(g);

				mxIGraphLayout layout = new mxFastOrganicLayout(graphAdapter);
				layout.execute(graphAdapter.getDefaultParent());
				

				frame.add(new mxGraphComponent(graphAdapter));

				frame.pack();
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
            }
    	});
	
    }
}