package edu.uoc.som.orchestrus.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.uoc.som.orchestrus.graph.TraceGraph;
import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.Trace;
import edu.uoc.som.orchestrus.utils.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class ClusteringSetup {
	public final static Logger LOGGER = Logger.getLogger(ClusteringSetup.class.getName());

	Map<String, ClusteringAlgo> algos = new HashMap<>();

	public Map<String, ClusteringAlgo> getAlgos() {
		return algos;
	}

	public Set<String> getAlgosNames() {
		return algos.keySet();
	}

	class ClusteringAlgo {
		Map<String, Object> parameters = new HashMap<>();
		String name;

		public ClusteringAlgo(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public Object getParameter(String key) {
			return parameters.get(key);
		}

		public String getParameterAsString(String key) {
			return (String) parameters.get(key);
		}

		public int getParameterAsInt(String key) {
			return Integer.parseInt((String) parameters.get(key));
		}

	}

	public ClusteringSetup(File clusterSetupFile) {

		@SuppressWarnings("deprecation")
		JSONParser parser = new JSONParser();
		try {
			JSONArray a = (JSONArray) parser.parse(new FileReader(clusterSetupFile.getAbsoluteFile()));
			for (Object o : a) {
				JSONObject run = (JSONObject) o;
				String algoName = run.getAsString("algorithm");
				ClusteringAlgo ca = new ClusteringAlgo(algoName);
				algos.put(algoName, ca);
				for (String s : run.keySet())
					ca.parameters.put(s, run.getAsString(s));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Specify deployFOlderPath for deployment. If left null, means no deployement.
	 * 
	 * @param tg
	 * @param outputFolderPath
	 * @param deployFolderPath
	 * @throws IllegalAccessError
	 */
	public static void deployClustering(TraceGraph tg, String outputFolderPath, String deployFolderPath)
			throws IllegalAccessError {
		LOGGER.info("\noutput:" + outputFolderPath + "\ndeploy:" + deployFolderPath);
		File outputFolder = cleanFolder(outputFolderPath);
		if (deployFolderPath != null)
			cleanFolder(deployFolderPath);
		String clusterResults = "";

		File clusterSetupFile = new File(Config.getClusteringSetupLocation());
		ClusteringSetup clusteringSetup = new ClusteringSetup(clusterSetupFile);
		for (ClusteringAlgo ca : clusteringSetup.getAlgos().values()) {

			List<Trace> traceClusters = null;
			switch (ca.getName()) {
			case "LabelPropagation":
				traceClusters = tg
						.getLabelPropagationClusters(Integer.parseInt((String) ca.parameters.get("maxIterations")));
				break;
			case "KSpan":
				traceClusters = tg.getKSpanClusters(Integer.parseInt((String) ca.parameters.get("k")));
				break;
			case "GirvenNewman":
				traceClusters = tg.getGirvanNewmanClusters(Integer.parseInt((String) ca.parameters.get("k")));
				break;
			default:
				throw new IllegalAccessError("Unrecognized algorithm name for clustering.");
			}
			String clusterRes = printClusters(outputFolder.getAbsolutePath(), traceClusters, ca);
			clusterResults += "\"" + ca.getParameterAsString("algorithm") + "\":" + clusterRes + ",";
			Utils.writeJSon(
					outputFolderPath + File.separator + ca.getParameterAsString("algorithm") + ".tracea.setup.json",
					clusterRes);
			if (deployFolderPath != null) {
				printClusters(deployFolderPath, traceClusters, ca);
				Utils.writeJSon(
						deployFolderPath + File.separator + ca.getParameterAsString("algorithm") + ".tracea.setup.json",
						clusterRes);
			}

		}
		if (clusterResults.endsWith(","))
			clusterResults = clusterResults.substring(0, clusterResults.length() - 1);
		Utils.writeJSon(outputFolderPath + File.separator + "clustering.tracea.json", "{" + clusterResults + "}");
		if (deployFolderPath != null) {
			Utils.writeJSon(deployFolderPath + File.separator + "clustering.tracea.json", "{" + clusterResults + "}");
		}
	}

	private static File cleanFolder(String outputFolder) {
		File folder = new File(outputFolder);
		if (folder.list() != null && folder.list().length != 0)
			for (File f : folder.listFiles()) {
				Utils.deleteFolder(f);
			}
		folder.mkdirs();
		return folder;
	}

	/**
	 * 
	 * @param algoName       Must fit the prefix of trace name -> used to browse
	 *                       json representation file names.
	 * @param traceClusters
	 * @param minSizeCluster
	 */
	private static String printClusters(String clusterFolderPath, List<Trace> traceClusters, ClusteringAlgo ca) {
		int sClusters = traceClusters.size();
		int minSizeCluster = ca.getParameterAsInt("minSizeCluster");
		traceClusters = traceClusters.stream().filter(t -> t.getTraceLinks().size() >= minSizeCluster)
				.collect(Collectors.toList());
		;
		LOGGER.fine(sClusters + " clusters found. " + traceClusters.size() + " with size >= " + minSizeCluster);
		String setup = "\"setup\": {";
		setup += "\"totalClusters\": \"" + sClusters + "\",";
		setup += "\"relevantClusters\": \"" + traceClusters.size() + "\",";
		for (String k : ca.getParameters().keySet()) {
			setup += "\"" + k + "\": \"" + ca.getParameterAsString(k) + "\",";
		}
		if (setup.endsWith(","))
			setup = setup.substring(0, setup.length() - 1);
		setup += "}";

		String clusters = "\"clusters\": [";

		for (Trace tc : traceClusters) {
			clusters += "{ \"" + tc.getName() + ".tracea.d3.json\": ";
			clusters += "  {";
			clusters += "\"size\":\"" + tc.getArtefacts().size() + "\",";
			clusters += "\"artefacts\": [ ";
			for (Artefact a : tc.getArtefacts())
				clusters += "{\"name\":" + "\"" + a.getName() + "\"," + "\"location\":" + " \""
						+ Utils.cleanUrlsForJson(a.getLocation()) + "\"},";
			if (clusters.endsWith(","))
				clusters = clusters.substring(0, clusters.length() - 1);
			clusters += "]" + "}";
			clusters += "},";
			String filePath = clusterFolderPath + File.separator + tc.getName() + ".tracea.d3.json";
			Utils.writeJSon(filePath, tc.renderD3JSon(false));
		}
		if (clusters.endsWith(","))
			clusters = clusters.substring(0, clusters.length() - 1);
		clusters += "]";

		String res = "{" + setup + "," + clusters + "}";

		return res;
	}
}
