package edu.uoc.som.orchestrus.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.JsonQuery;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.module.loaders.BuiltinModuleLoader;

public class Utils {

	@SuppressWarnings("deprecation")
	public static File writeTmpJson(String json) {
		File f = new File("R:\\Coding\\Git\\orchestrus\\data\\GlossaryML-ReferenceML\\tmp.json");
		try {
			FileUtils.write(f, json);
			return f;
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	

}
