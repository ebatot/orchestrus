package edu.uoc.som.orchestrus.graph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;


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
		return super.toString() + "[" + getWeight() + "]";
	}
	
	public static Function<WeightedEdge, Map<String, Attribute>> getAttributeProvider() {
		Function<WeightedEdge, Map<String, Attribute>> edgeAttributeProvider = v -> {
		    Map<String, Attribute> map = new LinkedHashMap<>();
		    map.put("weight", DefaultAttribute.createAttribute(v.getWeight()));
		    return map;
		};
		return edgeAttributeProvider;
	}
	
	//Define a vertex attribute provider
	

}