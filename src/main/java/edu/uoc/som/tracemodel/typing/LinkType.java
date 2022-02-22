package edu.uoc.som.tracemodel.typing;

import java.util.HashSet;
import java.util.Random;

import edu.uoc.som.tracemodel.TracingElement;

public class LinkType extends TracingElement {
	
	LinkType(String name) {
		super(name);
	}

	public String getJSon() {
		String res = "{";
		res += "\"id\": \""+getID()+"\",";
		res += "\"name\": \""+getName()+"\"";
//		res += "\"sources\": "+Utils.getElementsIDsAsJsonCollection(sources)+",";
//		res += "\"targets\": "+Utils.getElementsIDsAsJsonCollection(targets)+",";
//		res += "\"type\": \""+getTypeUID()+"\"";
		return res +"}";
	}
}
