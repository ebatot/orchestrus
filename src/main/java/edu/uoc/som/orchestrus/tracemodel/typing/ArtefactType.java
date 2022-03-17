package edu.uoc.som.orchestrus.tracemodel.typing;

import edu.uoc.som.orchestrus.tracemodel.TracingElement;

public class ArtefactType extends TracingElement {
	public static final ArtefactType UNDEFINED_TYPE = new ArtefactType("Undefined");
	
	private static int counter = 0;
	private int number;

	public ArtefactType(String name) {
		super(name);
		this.number = counter++;
	}

	public String getJSon() {
		String res = "{";
		res += "\"id\": \""+getID()+"\",";
		res += "\"name\": \""+getName()+"\"";
		return res +"}";
	}
	
	@Override
	public String getName() {
		return super.getName();
	}
	
	public int getNumber() {
		return number;
	}
}
