package edu.uoc.som.tracemodel.typing;

import edu.uoc.som.tracemodel.TracingElement;

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
	
	public int getTypeUID() {
		return getType().getTypeUID();
	}

}
