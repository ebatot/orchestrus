package edu.uoc.som.orchestrus.tracemodel.typing;

import java.util.logging.Logger;

import edu.uoc.som.orchestrus.tracemodel.TracingElement;

public abstract class TypedArtefact extends TracingElement {
	public final static Logger LOGGER = Logger.getLogger(TypedArtefact.class.getName());
	ArtefactType type;
	
	public TypedArtefact(String name, ArtefactType type) {
		super(name);
		setType(type);
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
