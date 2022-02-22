package edu.uoc.som.tracemodel;

public abstract class TracingElement {
	static String UNNAMED = "UNNAMED";
	
	String name;
	String creationDate;
	String modificationDate;
	String deletionDate;
	
	public TracingElement(String name) {
		this.name = name;
	}
	public TracingElement() {
		this.name = UNNAMED;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}
	
	public void setModificationDate(String modificationDate) {
		this.modificationDate = modificationDate;
	}
	
	public void setDeletionDate(String deletionDate) {
		this.deletionDate = deletionDate;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCreationDate() {
		return creationDate;
	}
	
	public String getModificationDate() {
		return modificationDate;
	}
	
	public String getDeletionDate() {
		return deletionDate;
	}
	
	public String toString() {
		return this.getClass().getName()+":"+name;
	};
}
