package edu.uoc.som.orchestrus.parsing;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.uoc.som.orchestrus.parsing.ReferenceFactory.Protocol;
import edu.uoc.som.orchestrus.tracemodel.TracingElement;

public class Reference extends TracingElement {
	public final static Logger LOGGER = Logger.getLogger(Reference.class.getName());

	private boolean resolved = false;

	private String raw;
	private Protocol protocol;
	private String location;
	private String innerLocation;
	private Set<Source> sources = new HashSet<Source>();

	public Reference(String strRef, Source source) {
		this.raw = strRef;
		this.protocol = ReferenceFactory.extractProtocol(this.raw);
		this.location = ReferenceFactory.extractLocation(this.raw);
		this.innerLocation = ReferenceFactory.extractInnerPath(this.raw);
		newName();
		addSource(source);
	}

	/**
	 * A reference is considered "local" if it has no protocol {@link Protocol}
	 * 
	 * @return
	 */
	public boolean isLocal() {
		return protocol == Protocol.no_protocol || protocol == Protocol.local;
	}

	public boolean isResolved() {
		return resolved;
	}

	private static int counter = 0;

	private void newName() {
		setName("Ref_" + counter++);
	}

	public String getHREF() {
		String res = "";
		if (!this.isLocal()) {
			res += this.getProtocol() + "://";
		}
		res += getTargetFileArtefact() + "#" + getInnerLocation();
		return res;
	}

	public Set<Source> getSources() {
		return sources;
	}

	public void setLocation(String newLocation) {
		this.location = newLocation;

	}

	/**
	 * protocol == o.protocol && location == o.location && innerLocation ==
	 * o.innerLocation
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		Reference rObj = (Reference) obj;
		if (!rObj.getProtocol().equals(this.getProtocol()))
			return false;
		if (!rObj.getTargetFileArtefact().equals(this.getTargetFileArtefact()))
			return false;
		return (!rObj.getInnerLocation().equals(this.getInnerLocation()));
	}

	public String getRaw() {
		return raw;
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * returns what lies between protocol:// and # (the latter indicates that its
	 * about the inside of a file.
	 * 
	 * @return
	 */
	public String getTargetFileArtefact() {
		return location;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public String getInnerLocation() {
		return innerLocation;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
		if (resolved && protocol == Protocol.no_protocol)
			protocol = Protocol.local;
	}

	public boolean containsSource(String sSource) {
		return sources.contains(sSource);
	}

	public boolean addSource(Source source) {
		return this.sources.add(source);
	}
}