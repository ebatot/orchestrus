package edu.uoc.som.orchestrus.parsing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

public class Tests {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ResourceSet set = new ResourceSetImpl();
		UMLResourcesUtil.init(set);
		set.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
//		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
//		Resource resource = set.getResource(URI.createFileURI(new File("").getAbsolutePath()), true);
//		resource.load(null);
//		UMLResource umlResource = (UMLResource)resource;
		
		System.out.println("Tests.enclosing_method()");
	}
	
	

}
