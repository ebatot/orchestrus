package edu.uoc.som.tracemodel.typing;

import java.util.HashMap;

public class LinkTypeFactory {
	static LinkTypeFactory instance;
	
	LinkType untyped;
	
	public static LinkTypeFactory getInstance() {
		if(instance == null)
			instance = new LinkTypeFactory();
		return instance;
	}
	
	public LinkTypeFactory() {
		untyped = addType("UNTYPED");
	}
	
	HashMap<String, LinkType> types = new HashMap<>();
	
	public LinkType addType(String typeName) {
		LinkType add = new LinkType(typeName);
		types.put(typeName, add);
		return add;
	}
	
	static public LinkType getUntyped() {
		return getInstance().untyped;
	}
	
}
