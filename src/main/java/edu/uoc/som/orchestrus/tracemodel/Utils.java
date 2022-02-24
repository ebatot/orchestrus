package edu.uoc.som.orchestrus.tracemodel;

import java.util.ArrayList;
import java.util.Collection;

public class Utils {
	public static ArrayList<String> getElementsIDs(Collection<? extends TracingElement> tes){
		ArrayList<String> res = new ArrayList<>(tes.size());
		for (TracingElement te : tes) 
			res.add(te.getID());
		return res;
	}
	
	public static String getElementsIDsAsJsonCollection(Collection<? extends TracingElement> tes){
		String res = "[";
		for (TracingElement te : tes) 
			res += "\""+te.getID()+"\"," ;
		return res.substring(0,res.length()-1)+"]";
	}

}
