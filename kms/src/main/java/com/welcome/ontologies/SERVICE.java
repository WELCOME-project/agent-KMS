package com.welcome.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class SERVICE {
    public static final String NAMESPACE = "http://www.daml.org/services/owl-s/1.1/Service.owl#";

    public static final String PREFIX = "service";

    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

    public static final IRI SERVICE = getIRI("Service");

    public static final IRI DESCRIBES = getIRI("describes");

    public static final IRI DESCRIBEDBY = getIRI("describedBy");

    public static final IRI PRESENTS = getIRI("presents");

    public static final IRI PRESENTEDBY = getIRI("presentedBy");

    /**
     * Creates a new {@link IRI} with this vocabulary's namespace for the given local name.
     *
     * @param localName a local name of an IRI, e.g. 'creatorOf', 'name', 'Artist', etc.
     * @return an IRI using the http://www.semanticweb.org/image-ontology/ namespace and the given local name.
     */
    private static IRI getIRI(String localName) {
        return SimpleValueFactory.getInstance().createIRI(NAMESPACE, localName);
    }
}
