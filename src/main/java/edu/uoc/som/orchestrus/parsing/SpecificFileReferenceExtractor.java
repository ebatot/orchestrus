package edu.uoc.som.orchestrus.parsing;

import java.util.HashSet;
import java.util.Set;

public abstract class SpecificFileReferenceExtractor {
	private Set<Reference> references = new HashSet<>();;

	
	public Set<Reference> getReferences() {
		return references;
	}
	
	public boolean addReference(Reference r) {
		return references.add(r);
	}

	public abstract String getHRefJSon();
	public abstract String getFilePath();
}
