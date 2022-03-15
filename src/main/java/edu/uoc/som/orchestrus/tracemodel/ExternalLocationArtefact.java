package edu.uoc.som.orchestrus.tracemodel;

import edu.uoc.som.orchestrus.parsing.refmanager.ReferenceFactory.Protocol;
import edu.uoc.som.orchestrus.tracemodel.typing.ArtefactType;

public class ExternalLocationArtefact extends Artefact {

	public ExternalLocationArtefact(String location, ArtefactType externalFolderArtefact,
			Protocol protocol) {
		super(location, externalFolderArtefact, location, null, false);
		setProtocol(protocol);
	}
}
