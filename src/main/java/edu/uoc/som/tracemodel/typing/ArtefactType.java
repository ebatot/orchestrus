package edu.uoc.som.tracemodel.typing;

import java.util.HashSet;
import java.util.Random;

public class ArtefactType {
	String name;
	
	public ArtefactType(String name) {
		this.name = name;
		this.typeUID = new Random().nextInt();
		while(typesIds.contains(typeUID))
			this.typeUID = new Random().nextInt();
		typesIds.add(this.typeUID);
	}
	
	int typeUID;
	public int getTypeUID() {
		return typeUID;
	}
	
	static HashSet<Integer> typesIds = new HashSet<>();
}
