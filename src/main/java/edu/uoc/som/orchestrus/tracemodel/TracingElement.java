package edu.uoc.som.orchestrus.tracemodel;

import java.util.HashMap;
import java.util.Random;

public abstract class TracingElement {
	static String UNNAMED = "UNNAMED";
	
	public static HashMap<String, TracingElement> elements = new HashMap<>();
	
	String ID;
	public String getID() {
		return ID;
	}

	String name;
	String creationDate;
	String modificationDate;
	String deletionDate;
	
	public TracingElement() {
		this(UNNAMED);
	}

	public static TracingElement getElement(String ID) {
		return elements.get(ID);
	}
	
	public TracingElement(String name) {
		this.name = name;
		this.ID = ""+new Random().nextLong();
		
		elements.put(ID, this);
		/* Comment for exclusive IDing. No need for prototyping.*/
//		this.typeUID = new Random().nextInt();
//		while(typesIds.contains(typeUID))
//			this.typeUID = new Random().nextInt();
//		typesIds.add(this.typeUID);
	}
//	static HashSet<Integer> typesIds = new HashSet<>();

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
