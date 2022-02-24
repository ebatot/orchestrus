package edu.uoc.som.orchestrus.tracemodel.typing;

import edu.uoc.som.orchestrus.tracemodel.TracingElement;

public abstract class TypedArtefact extends TracingElement {
	ArtefactType type;
	
	public TypedArtefact(String name, ArtefactType type) {
		super(name);
		setType(type);
	}
	
	public ArtefactType getType() {
		return type;
	}
	
	public void setType(ArtefactType type) {
		this.type = type;
	}
	
	public String getTypeUID() {
		return getType().getID();
	}

}
