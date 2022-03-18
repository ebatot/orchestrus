package edu.uoc.som.orchestrus.parsing;

import java.util.HashSet;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import edu.uoc.som.orchestrus.parsing.utils.XmlException;

public abstract class SpecificFileReferenceExtractor {
	private Set<Reference> references = new HashSet<>();;
	protected DocumentBuilder builder;
	
	public SpecificFileReferenceExtractor() {
		loadXMLTools();
	}

	private void loadXMLTools() {
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (ParserConfigurationException e) {
			throw new XmlException("Could not load the XML factory. (DEV)");
		}
	}
	
	public DocumentBuilder getBuilder() {
		return builder;
	}
	
	public Set<Reference> getReferences() {
		return references;
	}
	
	public boolean addReference(Reference r) {
		return references.add(r);
	}

	public abstract String getHRefJSon();
	public abstract String getFilePath();
}
