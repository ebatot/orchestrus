package edu.uoc.som.orchestrus.tracemodel.typing;

import edu.uoc.som.orchestrus.tracemodel.TracingElement;

public class ArtefactType extends TracingElement {
	public static final ArtefactType UNDEFINED_TYPE = new ArtefactType("Undefined");
	
	String name;
	
	public ArtefactType(String name) {
		super(name);
	}

	public String getJSon() {
		String res = "{";
		res += "\"id\": \""+getID()+"\",";
		res += "\"name\": \""+getName()+"\"";
		return res +"}";
	}
	
}
