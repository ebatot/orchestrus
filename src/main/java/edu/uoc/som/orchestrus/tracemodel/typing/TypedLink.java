package edu.uoc.som.orchestrus.tracemodel.typing;

import edu.uoc.som.orchestrus.tracemodel.TracingElement;

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
	
	public String getTypeUID() {
		return getType().getID();
	}
	
}
