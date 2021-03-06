/*****************************************************************************
* Copyright (c) 2015, 2022 CEA-LIST & SOM-UOC, Edouard Batot
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
* UOC-SOM - Initial API and implementation
*  -> Edouard Batot (UOC SOM) ebatot@uoc.edu 
*****************************************************************************/


package edu.uoc.som.orchestrus.parsing;

import java.io.File;
import java.io.FilenameFilter;

class XMILikeFileFilter implements FilenameFilter {

	static XMILikeFileFilter getFilter() {
		return new XMILikeFileFilter();
	}
	
	@Override
	public boolean accept(File dir, String fileName) {
	    return (
	    		//custom generation
	    		fileName.endsWith(".xmi") || 
	    		//specificatiion-models and profiles
	    		fileName.endsWith(".uml") || 
	    		fileName.endsWith(".notation") || 
	    		fileName.endsWith(".di") || 
	    		// profile
	    		fileName.endsWith(".ecore") || 
	    		fileName.endsWith(".genmodel") || 
	    		//architecture-framework
	    		fileName.endsWith(".architecture") || 
	    		//Editor and elementtype related
	    		fileName.endsWith(".creationmenumodel") ||
	    		fileName.endsWith(".elementtypesconfigurations") ||
	    		fileName.endsWith(".paletteconfiguration") ||
	    		fileName.endsWith(".ctx") ||
	    		//nattable
	    		fileName.endsWith(".nattableconfiguration") 
	    		);
	  }
}
