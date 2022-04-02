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
	/*
	 * public static Map<String, Map<String, Object> > getClusteringSetupMap(File
	 * clusterSetupFile) { Map<String, Map<String, Object> > clusteringSetup = new
	 * HashMap<>();
	 * 
	 * @SuppressWarnings("deprecation") JSONParser parser = new JSONParser(); try {
	 * JSONArray a = (JSONArray) parser.parse(new
	 * FileReader(clusterSetupFile.getAbsoluteFile())); for (Object o : a) {
	 * JSONObject run = (JSONObject) o;
	 * clusteringSetup.put(run.getAsString("algorithm"), new HashMap<>()); for
	 * (String s : run.keySet())
	 * clusteringSetup.get(run.getAsString("algorithm")).put(s, run.getAsString(s));
	 * } } catch (FileNotFoundException e) { e.printStackTrace(); } catch
	 * (ParseException e) { e.printStackTrace(); } return clusteringSetup; }
	 */

	public static void deployClustering(TraceGraph tg, String outputFolder) throws IllegalAccessError {
		File folder = cleanOutputFolder(outputFolder);

		File clusterSetupFile = new File("src\\main\\resources\\clustering.json");
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
			printClusters(folder.getAbsolutePath(), traceClusters, ca);
		}
	}

	private static File cleanOutputFolder(String outputFolder) {
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
		String folderPath = clusterFolderPath + File.separator + Config.getInstance().getProjectName() + "\\";

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
			clusters += "{ \"filename\": \"" + tc.getName() + ".tracea.d3.json\", ";
			clusters += "  \"artefacts\": [ ";

			for (Artefact a : tc.getArtefacts())
				clusters += "{\"name\":" + "\"" + a.getName() + "\"," + "\"location\":" + " \""
						+ Utils.cleanUrlsForJson(a.getLocation()) + "\"},";
			if (clusters.endsWith(","))
				clusters = clusters.substring(0, clusters.length() - 1);
			clusters += "]";
			clusters += "},";
			String filePath = folderPath + tc.getName() + ".tracea.d3.json";
			Utils.writeJSon(filePath, tc.renderD3JSon(false));
		}
		if (clusters.endsWith(","))
			clusters = clusters.substring(0, clusters.length() - 1);
		clusters += "]";

		String res = "{" + setup + "," + clusters + "}";
		Utils.writeJSon(folderPath + ca.getParameterAsString("algorithm") + ".tracea.setup.json", res);
		return res;
	}
}
