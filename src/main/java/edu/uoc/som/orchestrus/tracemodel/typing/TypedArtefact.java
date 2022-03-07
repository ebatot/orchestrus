package edu.uoc.som.orchestrus.tracemodel.typing;

import edu.uoc.som.orchestrus.tracemodel.TracingElement;

public abstract class TypedArtefact extends TracingElement {
	
	private ArtefactType type;
	
	public TypedArtefact(String name, ArtefactType type) {
		super(name);
		setType(type);
	}
	
	public TypedArtefact(String name) {
		super(name);
		setType(ArtefactType.UNDEFINED_TYPE);
	}

	public ArtefactType getType() {
		return type;
	}
	
	public void setType(ArtefactType type) {
		if(type == null)
			type = ArtefactType.UNDEFINED_TYPE;
		this.type = type;
	}
	
	public String getTypeUID() {
		return getType().getID();
	}

}
