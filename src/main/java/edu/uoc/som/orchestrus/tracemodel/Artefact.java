package edu.uoc.som.orchestrus.tracemodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.parsing.ReferenceFactory.Protocol;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactTypeFactory;
import edu.uoc.som.orchestrus.tracemodel.typing.TypedArtefact;

public class Artefact extends TypedArtefact {
	
	public final static Logger LOGGER = Logger.getLogger(Artefact.class.getName());

	private String location;
	private Artefact parent;
	private boolean resolves;
	/**
	 * Default is NO PROTOCOL.
	 */
	private Protocol protocol = Protocol.no_protocol;


//	private HashMap<String, Artefact> fragments = new HashMap<>();
	private HashSet<Artefact> fragments = new HashSet<>();
	private ArrayList<TraceLink> sourceOf;
	private ArrayList<TraceLink> targetOf;

	public Artefact(String name, ArtefactType type, String location, Artefact parent, boolean resolved) {
		super(name, type);
		this.parent = parent;
//		if(parent != null)
//			addFragment(parent);
		this.location = location;
		sourceOf = new ArrayList<>();
		targetOf = new ArrayList<>();
		this.resolves = resolved;
		if(resolves)
			protocol = Protocol.local;
		
		hashCode = (getProtocol()+getLocation()+getName()).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		Artefact rObj = (Artefact) obj;
		if (!rObj.getProtocol().toString().equals(this.getProtocol().toString()))
			return false;
		if (!rObj.getLocation().equals(this.getLocation()))
			return false;
		if (!rObj.getName().equals(this.getName()))
			return false;
		return true;
	}
	int hashCode;
	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * TEST PURPOSE.
	 * @param name
	 * @param type
	 * @param location
	 * @param parent
	 */
	public Artefact(String name, ArtefactType type, String location, Artefact parent) {
		this(name, type, location, parent, false);
	}

	/**
	 * TEST PURPOSE
	 * @param name
	 * @param type
	 */
	public Artefact(String name, ArtefactType type) {
		this(name, type, null, null, false);
	}
	
	public String getJSon() {
		String res = "{";
		res += "\"id\": \"" + getID() + "\",";
		res += "\"name\": \"" + getName() + "\",";
		res += "\"location\": \"" + edu.uoc.som.orchestrus.utils.Utils.cleanUrlsForJson(getLocation()) + "\",";
		res += getParent() == null ? "" : "\"parent\": \"" + getParent().getID() + "\",";
		res += "\"fragments\": " + Utils.getElementsIDsAsJsonCollection(fragments) + ",";
		res += "\"type\": \"" + getTypeUID() + "\"";
		return res + "}";
	}
	
	public String getD3JSon() {
		String res = "{";
		res += "\"id\": \"" + getID() + "\",";
//		res += "\"name\": \"" + getProtocol() + "::"+edu.uoc.som.orchestrus.utils.Utils.cleanUrlsForJson(getLocation()) + "::"+ edu.uoc.som.orchestrus.utils.Utils.cleanUrlsForJson(getName()) + "\",";
		res += "\"name\": \"" +  edu.uoc.som.orchestrus.utils.Utils.cleanUrlsForJson(getName()) + "\",";
		res += "\"type\": \"" + getType().getName() + "\",";
		res += "\"size\": " + getFragments().size()*10 + ",";
//		res += "\"size\": " + ArtefactTypeFactory.getD3Size(getType()) + ",";
		res += "\"group\": \"" + getType().getNumber() + "\"";
		return res + "}";
	}

	@Override
	public String toString() {
		return this.getType().getName()+"["+this.getName()+"]";
	}
	
	public String getHREF() {
		return protocol + "://" + getLocation() + File.separator+getName();
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}
	
	public Protocol getProtocol() {
		return protocol;
	}

	public Protocol getAncestorProtocol() {
		return getAncestor().getProtocol();
	}
	
	public ArtefactType getAncestorType() {
		return getAncestor().getType();
	}

	
	public Artefact getAncestor() {
		if(parent == null)
			return this;
		return parent.getAncestor();
	}

	public boolean isResolves() {
		return resolves;
	}

	public boolean hasParent() {
		return parent != null;
	}
	
	public void addFragment(Artefact af) {
//		System.out.println("Artefact.addFragment() "+fragments.size()+"---" + this);
		if(!fragments.contains(af)) {
			fragments.add(af);
			af.setParent(this);
//			System.out.println("    - : "+af.getProtocol());
//			System.out.println("    - : "+af.getLocation());
//			System.out.println("    - : "+af.getName());
			
			LOGGER.finest("Fragment: " + af + " ADDED to: " + this);
		}
	}

	public String getLocation() {
		return location;
	}
	
	public HashSet<Artefact> getFragments() {
		return fragments;
	}

	public Artefact getParent() {
		return parent;
	}

	private void setParent(Artefact artefact) {
		this.parent = artefact;
	}

	public ArrayList<TraceLink> getTargetOf() {
		return targetOf;
	}

	public ArrayList<TraceLink> getSourceOf() {
		return sourceOf;
	}

	public boolean removeTargetOf(TraceLink tl) {
		return targetOf.remove(tl);
	}

	public boolean removeTargetsOf(Collection<TraceLink> tls) {
		return targetOf.removeAll(tls);
	}

	public boolean removeSourceOf(TraceLink tl) {
		return sourceOf.remove(tl);
	}

	public boolean removeSourcesOf(Collection<TraceLink> tls) {
		return sourceOf.removeAll(tls);
	}

	public boolean addTargetOf(TraceLink tl) {
		return targetOf.add(tl);
	}

	public boolean addSourceOf(TraceLink tl) {
		return sourceOf.add(tl);
	}

	public boolean isOfType(ArtefactType at) {
		return getType().getName().equals(at.getName());
	}

	public boolean isOfType(String typeName) {
		return getType().getName().equals(typeName);
	}

	public boolean isLocal() {
		return protocol == Protocol.no_protocol || protocol == Protocol.local;
	}

	public String renderFragmentation(boolean recursive, boolean renderElements) {
		String res = "\"name\": \""+getName()+"\"" +
				",\"type\": \""+getType().getName()+"\""+
				",\"location\": \""+edu.uoc.som.orchestrus.utils.Utils.cleanUrlsForJson(getLocation())+"\""+
				"";
//				",\"references\": [\"Todo\"]";
		if(recursive) {
			String resChild = "";
			for (Artefact a : fragments) {
				if(renderElements || a.getType() != ArtefactTypeFactory.ELEMENT_ARTEFACT)
					resChild += a.renderFragmentation(recursive, renderElements)+",\n";
			}
			if(!resChild.isBlank())
				resChild = resChild.substring(0, resChild.length()-2);
			resChild = ",\n\"children\": [" + resChild + "]";
			res += resChild;
		}
		return "{"+res+"}";
	}
}
