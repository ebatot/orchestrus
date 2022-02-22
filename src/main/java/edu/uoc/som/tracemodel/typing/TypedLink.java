package edu.uoc.som.tracemodel.typing;

import edu.uoc.som.tracemodel.TracingElement;

public abstract class TypedLink extends TracingElement {
	
	LinkType type;
	
	public TypedLink() {
		super();
	}
	
	public TypedLink(String name) {
		super(name);
	}

	public LinkType getType() {
		return type;
	}
	
	public void setType(LinkType type) {
		this.type = type;
	}
	
	public int getTypeUID() {
		return getType().getTypeUID();
	}
	
}
