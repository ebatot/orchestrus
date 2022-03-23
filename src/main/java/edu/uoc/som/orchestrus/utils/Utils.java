package edu.uoc.som.orchestrus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.IntNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.uoc.som.orchestrus.config.Config;
import edu.uoc.som.orchestrus.tracemodel.Artefact;
import edu.uoc.som.orchestrus.tracemodel.ArtefactFactory;
import edu.uoc.som.orchestrus.tracemodel.Trace;
import edu.uoc.som.orchestrus.tracemodel.TraceLink;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.module.loaders.BuiltinModuleLoader;

public class Utils {
	final static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Utils.class.getName());

//	public static void main(String[] args) {
//		 printLOC();
//		 System.exit(1);
//	}
	
	public static void storeD3Tracea(Trace t, boolean deploy) {
		storeD3Tracea(t, deploy, "meta\\d3viewer\\data\\input_data.json");
	}

	@SuppressWarnings("deprecation")
	public static void storeD3Tracea(Trace t, boolean deploy, String deployLocationPath) {
		File f = new File("data\\out\\"+Config.getInstance().getProjectName()+"\\"+Config.getInstance().getProjectName()+"_"+t.getName()+".tracea.d3.json");
		String d3trace = t.renderD3JSon();
		String log = "";
		try {
			FileUtils.write(f, d3trace);
			log += ("\n - Trace '"+t.getName()+"' ("+Config.getInstance().getProjectName()+") stored as D3Tracea in '"+f.getAbsolutePath()+"'");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(deploy) {
			f = new File(deployLocationPath);
			try {
				FileUtils.write(f, d3trace);
				log += ("\n - Trace '"+t.getName()+"' ("+Config.getInstance().getProjectName()+") deployed as D3Tracea in '"+f.getAbsolutePath()+"'");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info(""+log);
	}
	
	public static void storeMatrixTracea(Trace t, boolean deploy, double acceptanceThreshold) {
		storeMatrixTracea(t, deploy, "meta\\d3viewer\\data\\adjacencyMatrix.html", acceptanceThreshold);
	}
	
	@SuppressWarnings("deprecation")
	public static void storeMatrixTracea(Trace t, boolean deploy, String deployLocationPath, double acceptanceThreshold) {
		File f = new File("data\\out\\"+Config.getInstance().getProjectName()+"\\"+Config.getInstance().getProjectName()+"_"+t.getName()+".tracea.html");
		String htmlTrace = t.renderHTMLMatrix(acceptanceThreshold);
		String log = "";
		try {
			FileUtils.write(f, htmlTrace);
			log += ("\n - Trace '"+t.getName()+"' ("+Config.getInstance().getProjectName()+") stored as HTML matrix in '"+f.getAbsolutePath()+"'");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(deploy) {
			f = new File(deployLocationPath);
			try {
				FileUtils.write(f,htmlTrace);
				log += ("\n - Trace '"+t.getName()+"' ("+Config.getInstance().getProjectName()+") deployed as HTML matrix in '"+f.getAbsolutePath()+"'");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		LOGGER.info(""+log);
	}

	@SuppressWarnings("deprecation")
	public static void storeDependencies_HC(String interArtDependencies_JSON) {
		File f = new File("data\\out\\"+Config.getInstance().getProjectName()+"\\"+Config.getInstance().getProjectName()+".refs.json");
		try {
			FileUtils.write(f, interArtDependencies_JSON);
			LOGGER.info("References stored in '"+f.getAbsolutePath()+"'");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static File writeTmpJson_HC(String json) {
		File f = new File("data\\out\\junk\\tmp.json");
		try {
			FileUtils.write(f, json);
			LOGGER.info("JSon stored in '"+f.getAbsolutePath()+"'");
			return f;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static String cleanJSon(String res) {
		JsonParser parser = new JsonParser();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
		JsonElement el = parser.parse(res);
		res = gson.toJson(el); // done
		return res;
	}
	
	/**
	 * Execute a JQ query on the model passed in paramater and returns its JSon result.
	 * @param datamodel A SysMLv2 model written in JSon
	 * @param jqQuery A query written in JQ
	 * @return
	 * @throws IOException
	 * @throws JsonQueryException
	 * @throws JsonProcessingException
	 */
	public static String executeJQuery(String datamodel, String jqQuery)
			throws IOException, JsonQueryException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		JsonNode input = mapper.readTree(datamodel);

		Scope rootScope = Scope.newEmptyScope();

		// Use BuiltinFunctionLoader to load built-in functions from the classpath.
		BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);

		// For import statements to work, you need to set ModuleLoader. BuiltinModuleLoader uses ServiceLoader mechanism to
		// load Module implementations.
		rootScope.setModuleLoader(BuiltinModuleLoader.getInstance());
		
		// per every apply() invocations if you need to do so.
		Scope childScope = Scope.newChildScope(rootScope);

		// Scope#setValue(...) sets a custom variable that can be used from jq expressions. This variable is local to the
		// childScope and cannot be accessed from the rootScope. The rootScope will not be modified by this call.
		childScope.setValue("param", IntNode.valueOf(42));


		JsonQuery q = JsonQuery.compile(jqQuery, Versions.JQ_1_6);

		// You need a JsonNode to use as an input to the JsonQuery. There are many ways you can grab a JsonNode.
		// In this example, we just parse a JSON text into a JsonNode.

		// Finally, JsonQuery#apply(...) executes the query with given input and produces 0, 1 or more JsonNode.
		// The childScope will not be modified by this call because it internally creates a child scope as necessary.
		final List<JsonNode> out = new ArrayList<>();
		q.apply(childScope, input, out::add);
		
		String outText = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(out);
		return outText;
	}
	

	public static void printLOC(){
		int[] i;
		try {
			i = countLOC(new File("./src"));
			System.out.println("Main.main(src:"+i[0]+") ("+i[1]+" classes)");
			i = countLOC(new File("./meta/d3viewer/data"));
			System.out.println("Main.main(meta/d3viewer/data:"+i[0]+")");
			i = countLOC(new File("./meta/d3viewer/scripts/bootstrap"));
			System.out.println("Main.main(meta/d3viewer/scripts/bootstrap:"+i[0]+")");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static int[] countLOC(File f) throws IOException {
		int[] res = new int[] {0, 0};
		
		if(f.getName().equals("bootstrap"))
			return res;
		
		if(f.getName().endsWith(".java") )
			res[1]++;
		
		if(f.isDirectory()){
			
			for (File f2 : f.listFiles()) {
				res[0] += countLOC(f2)[0];
				res[1] += countLOC(f2)[1];
			}
//			System.out.println("Dir:"+f.getCanonicalPath()+" : "+res);
		} else {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line = "";
			while((line = br.readLine()) != null){
				if(!line.isEmpty())
					res[0]++;
			}
			br.close();
//			System.out.println(f.getCanonicalPath()+" : "+res);
		}
		return res;
	}

	public static String cleanUrlsForJson(String backSlashedlocation) {
		backSlashedlocation = backSlashedlocation.replaceAll("'", "\\\\'");
		backSlashedlocation = backSlashedlocation.replaceAll("\"", "\\\\\"");
		
		backSlashedlocation = backSlashedlocation.replace("\\", "/");
	
		return backSlashedlocation;
	}
	
	public static String printFragmentD3Json() {
		ArrayList<TraceLink> allFragment = new ArrayList<>();
		
		for (Artefact a : ArtefactFactory.getAncestors()) {
			List<TraceLink> aFragments = ArtefactFactory.getFragmentLinks(a);
			allFragment.addAll(aFragments);
		}
		
		Set<Artefact> artCollect = new HashSet<>();
		for (TraceLink tl : allFragment) {
			artCollect.addAll(tl.getSources());
			artCollect.addAll(tl.getTargets());
		}
		
		for (Artefact a : ArtefactFactory.sortArtefactsByLocation(ArtefactFactory.getArtefacts().values())) {
			if(!artCollect.contains(a)) {
				LOGGER.warning("[DEV] !! missing: "+a + " in collection list.");
			}
		}
		for (Artefact a : ArtefactFactory.sortArtefactsByLocation(artCollect)) {
			if(!ArtefactFactory.getArtefacts().values().contains(a)) {
				LOGGER.warning("[DEV] !! missing: "+a + " in complete list.");
			}
		}
		
		String links = "" ;
		for (TraceLink tl : allFragment) 
			links += tl.getD3Json()+",\n";
		if(!links.isBlank())
			links = links.substring(0, links.length()-2);
		links = "\"links\": [" + links + "]";
		
		// print ALL artefacts, IN THE UNIVERSE !
		String nodes = "";
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

	
	public static String limitStringSize(String s, int maxLength) {
		if(s.length() > maxLength)
			return s.substring(0, maxLength);
		return s;
	}
}
