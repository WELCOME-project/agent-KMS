package com.welcome.auxiliary;

import com.welcome.ontologies.FAQ;
import com.welcome.ontologies.WELCOME;
import com.welcome.services.AGENT;
import com.welcome.services.APP;
import com.welcome.services.WPM;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@SuppressWarnings("unused")
public class Queries {

  Logger logger = LoggerFactory.getLogger(AGENT.class);

  /* Initialize external classes */
  Utilities util = new Utilities();

  /**
   * Returns latest user turn
   *
   * @return latest use turn
   */
  public IRI latestUserTurn_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?turn WHERE { \n";
    queryString += "    ?turn rdf:type welcome:DialogueUserTurn ; \n";
    queryString += "       welcome:hasTimestamp ?t1 . \n";
    queryString += "    { \n";
    queryString += "    	SELECT ?turn WHERE { \n";
    queryString += "    		?s rdf:type welcome:DialogueSession . \n";
    queryString += "    		?s welcome:hasDialogueTurn ?turn ; \n";
    queryString += "       		welcome:hasTimestamp ?t2 . \n";
    queryString += "        } ORDER BY DESC(?t2) \n";
    queryString += "    } \n";
    queryString += "} ORDER BY DESC(?t1) LIMIT 1 \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI userTurn = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value turn = solution.getBinding("turn").getValue();
        userTurn = Utilities.f.createIRI(turn.stringValue());
      }
    }

    return userTurn;
  }

  /**
   * Returns latest system turn.
   *
   * @return latest system turn
   */
  public IRI latestSystemTurn_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?turn WHERE { \n";
    queryString += "    ?turn rdf:type welcome:DialogueSystemTurn ; \n";
    queryString += "       welcome:hasTimestamp ?t1 . \n";
    queryString += "    { \n";
    queryString += "    	SELECT ?turn WHERE { \n";
    queryString += "    		?s rdf:type welcome:DialogueSession . \n";
    queryString += "    		?s welcome:hasDialogueTurn ?turn ; \n";
    queryString += "       		welcome:hasTimestamp ?t2 . \n";
    queryString += "        } ORDER BY DESC(?t2) \n";
    queryString += "    } \n";
    queryString += "} ORDER BY DESC(?t1) LIMIT 1 \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI systemTurn = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value turn = solution.getBinding("turn").getValue();
        systemTurn = Utilities.f.createIRI(turn.stringValue());
      }
    }

    return systemTurn;
  }

  public Long latestSystemTurnID_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?id WHERE { \n";
    queryString += "    ?turn rdf:type welcome:DialogueSystemTurn ; \n";
    queryString += "       welcome:hasTimestamp ?t1 ; \n";
    queryString += "       welcome:id ?id . \n";
    queryString += "    { \n";
    queryString += "    	SELECT ?turn WHERE { \n";
    queryString += "    		?s rdf:type welcome:DialogueSession . \n";
    queryString += "    		?s welcome:hasDialogueTurn ?turn ; \n";
    queryString += "       		welcome:hasTimestamp ?t2 ; \n";
    queryString += "          welcome:id ?id . \n";
    queryString += "        } ORDER BY DESC(?t2) \n";
    queryString += "    } \n";
    queryString += "} ORDER BY DESC(?t1) LIMIT 1 \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    Long systemId = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value id = solution.getBinding("id").getValue();
        systemId = Long.parseLong(id.stringValue());
      }
    }

    return systemId;
  }

  /**
   * Assigns an ID to the DIP of the given context.
   *
   * @param context graph name
   * @param id      unique id
   */
  public void assignID_I(Resource context, Long id) {
    /* The following query assigns an ID to the DIP under the given context */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "        ?x welcome:DIPId ?id . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "        ?x welcome:hasSlot ?slot . \n";
    queryString += "        BIND(STR(\"" + id + "\") as ?id) \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void removeTempIDs() {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString += "    welcome:message welcome:hasCorrelationID ?cID . \n";
    queryString += "    welcome:message welcome:hasTurnID ?tID . \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH welcome:LogInfo { \n";
    queryString += "    	welcome:message welcome:hasCorrelationID ?cID . \n";
    queryString += "    	welcome:message welcome:hasTurnID ?tID . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    welcome:message welcome:hasCorrelationID ?cID . \n";
    queryString += "    welcome:message welcome:hasTurnID ?tID . \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void removeTempUser() {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "DELETE WHERE { \n";
    queryString += "    welcome:tcn_user rdf:type welcome:DialogueUser . \n";
    queryString += "	  welcome:tcn_user welcome:hasUserId ?o . \n";
    queryString += "	  ?o welcome:hasValue ?v . \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }


  /**
   * Adds the correlation id to the ontology.
   *
   * @param id correlation id
   */
  public void correlationId_I(String id) {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    	welcome:message welcome:hasCorrelationID ?cId . \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH welcome:LogInfo { \n";
    queryString += "        welcome:message welcome:hasCorrelationID \"" + id + "\". \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    OPTIONAL { \n";
    queryString += "          welcome:message welcome:hasCorrelationID ?cId . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void turnId_I(String id) {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    	welcome:message welcome:hasTurnID ?tId . \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH welcome:LogInfo { \n";
    queryString += "        welcome:message welcome:hasTurnID \"" + id + "\". \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    OPTIONAL { \n";
    queryString += "       welcome:message welcome:hasTurnID ?tId . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void insertRegStatus() {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH welcome:ProfileInfo { \n";
    queryString += "        welcome:tcn_user rdf:type welcome:Registered . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void deleteRegStatus() {
    /* The following query inserts the correlation id */
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "DELETE DATA { \n";
    queryString += "    GRAPH welcome:ProfileInfo { \n";
    queryString += "        welcome:tcn_user rdf:type welcome:Registered . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public Integer checkSimulation() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?scenario WHERE { \n";
    queryString += "    ?session rdf:type welcome:DialogueSession ; \n";
    queryString += "       welcome:isActiveSession \"true\" ; \n";
    queryString += "       welcome:triggeredBy ?scenario ; \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value scenario = solution.getBinding("scenario").getValue();

        if (scenario.stringValue().contains("Training Appointment")
            || scenario.stringValue().contains("Simulate Appointment")) {
          initProfile(scenario.stringValue(), 1);
          return 1;
        } else if (scenario.stringValue().contains("CV Creation")) {
          initProfile(scenario.stringValue(), 2);
          return 2;
        } else {
          initProfile(scenario.stringValue(), 0);
          return 0;
        }
      }
    }

    return 0;
  }

  /**
   * Returns the active dialogue session.
   *
   * @return IRI of the active session
   */
  public IRI activeDialogueSession_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?session WHERE { \n";
    queryString += "    ?session rdf:type welcome:DialogueSession ; \n";
    queryString += "       welcome:isActiveSession \"true\" . \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI activeSession = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value session = solution.getBinding("session").getValue();
        activeSession = Utilities.f.createIRI(session.stringValue());
      }
    }

    return activeSession;
  }

  /**
   * Returns the active dialogue session.
   *
   * @return String of scenario selection
   */
  public String scenarioSelection_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?scenario WHERE { \n";
    queryString += "    ?session rdf:type welcome:DialogueSession ; \n";
    queryString += "       welcome:isActiveSession \"true\" ; \n";
    queryString += "       welcome:triggeredBy ?scenario . \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    String scenario = "";
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value temp = solution.getBinding("scenario").getValue();
        scenario = temp.stringValue();
      }
    }

    return scenario;
  }

  /**
   * Returns latest dialogue turn
   *
   * @return
   */
  public IRI latestTurn_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?turn WHERE { \n";
    queryString += "    ?turn rdf:type welcome:DialogueTurn ; \n";
    queryString += "          welcome:hasTimestamp ?t1 . \n";
    queryString += "    ?session welcome:hasDialogueTurn ?turn ; \n";
    queryString += "             welcome:isActiveSession ?active . \n";
    queryString += "} ORDER BY DESC(?t1) LIMIT 1 \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI dialogueTurn = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value turn = solution.getBinding("turn").getValue();
        dialogueTurn = Utilities.f.createIRI(turn.stringValue());
      }
    }

    return dialogueTurn;
  }

  /**
   * Returns IRI of active DIP
   *
   * @return
   */
  public IRI activeDIP_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?graph WHERE { \n";
    queryString += "  GRAPH welcome:DipInfo { \n";
    queryString += "    welcome:activeDIP welcome:belongsToGraph ?graph . \n";
    queryString += "  } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI activeDIP = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value graph = solution.getBinding("graph").getValue();
        activeDIP = Utilities.f.createIRI(graph.stringValue());
      }
    }

    return activeDIP;
  }

  public IRI previouslyActiveDIP_S(Resource context) {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?graph WHERE { \n";
    queryString += "GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    welcome:activeDIP welcome:belongsToGraph ?graph . \n";
    queryString += "} \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI activeDIP = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value graph = solution.getBinding("graph").getValue();
        activeDIP = Utilities.f.createIRI(graph.stringValue());
      }
    }

    return activeDIP;
  }

  public IRI prevActiveDIP_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?graph WHERE { \n";
    queryString += "    welcome:faq_prev_active_DIP welcome:hasValue ?graph . \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI activeDIP = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value graph = solution.getBinding("graph").getValue();
        activeDIP = Utilities.f.createIRI(graph.stringValue());
      }
    }

    return activeDIP;
  }

  public IRI activeDipName_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?dip WHERE { \n";
    queryString += "  GRAPH welcome:DipInfo { \n";
    queryString += "    welcome:activeDIP welcome:belongsToGraph ?graph . \n";
    queryString += "  } \n";
    queryString += "  GRAPH ?graph { \n";
    queryString += "    ?dip welcome:hasSlot ?slot . \n";
    queryString += "  } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI activeDIP = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value graph = solution.getBinding("dip").getValue();
        activeDIP = Utilities.f.createIRI(graph.stringValue());
      }
    }

    return activeDIP;
  }

  public IRI prevActiveDipName_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?dip WHERE { \n";
    queryString += "    welcome:prevActiveDIP welcome:belongsToGraph ?graph . \n";
    queryString += "    GRAPH ?graph { \n";
    queryString += "        ?dip welcome:hasSlot ?slot . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI activeDIP = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value graph = solution.getBinding("dip").getValue();
        activeDIP = Utilities.f.createIRI(graph.stringValue());
      }
    }

    return activeDIP;
  }

  /**
   * Updates DIP with existing knowledge
   *
   * @param context
   */
  public void updateDIP_I(Resource context) {
    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "    	?content a rdf:Statement ; \n";
    queryString += "    	         rdf:subject ?subjectX ; \n";
    queryString += "    	         rdf:predicate ?predicateX ; \n";
    queryString += "    	         rdf:object ?objectX . \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer \n";
    queryString += "        [ a rdf:Statement; \n";
    queryString += "       		rdf:subject ?type; \n";
    queryString += "       		rdf:predicate welcome:hasValue; \n";
    queryString += "            rdf:object ?value ] . \n";
    queryString += "    	?slot welcome:hasStatus ?newStatus . \n";
    queryString += "    	?slot welcome:confidenceScore \"1.0\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?dialogueUser ?hasProperty ?property . \n";
    queryString += "        ?property rdf:type ?type . \n";
    queryString += "        ?property welcome:hasValue ?value . \n";
    queryString += "        ?property welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasOntologyType ?t . \n";
    queryString += "        ?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	    ?content a rdf:Statement ; \n";
    queryString += "    	             rdf:subject ?subjectX ; \n";
    queryString += "    	             rdf:predicate ?predicateX ; \n";
    queryString += "    	             rdf:object ?objectX . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "    OPTIONAL { \n";
    queryString += "        welcome:expiration_period welcome:duration ?duration . \n";
    queryString += "    } \n";
    queryString += "    BIND(IRI(?t) as ?temp) \n";
    queryString += "    BIND(IF(!CONTAINS(STR(?temp), " +
        "\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\"), " +
        "(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", "
        +
        "STRAFTER(STR(?t), \":\")))), " +
        "?temp) " +
        "as ?final) \n";
    queryString += "    BIND(EXISTS {?final welcome:needsUpdate true} as ?update) \n";
    queryString += "    BIND(IF(?update = true && ((" + timestamp
        + " - ?timestamp) / 86400  >= ?duration), welcome:NeedsUpdate, welcome:Completed) as ?newStatus) \n";
    queryString += "    FILTER(?type != welcome:Language && ?type = ?final) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Updates DIP with existing knowledge
   *
   * @param context
   */
  public void updateDIPSim_I(Resource context) {
    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "    	?content a rdf:Statement ; \n";
    queryString += "    	         rdf:subject ?subjectX ; \n";
    queryString += "    	         rdf:predicate ?predicateX ; \n";
    queryString += "    	         rdf:object ?objectX . \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer \n";
    queryString += "        [ a rdf:Statement; \n";
    queryString += "       		rdf:subject ?type; \n";
    queryString += "       		rdf:predicate welcome:hasValue; \n";
    queryString += "            rdf:object ?value ] . \n";
    queryString += "    	?slot welcome:hasStatus ?newStatus . \n";
    queryString += "    	?slot welcome:confidenceScore \"1.0\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    queryString += "        ?dialogueUser ?hasProperty ?property . \n";
    queryString += "        ?property rdf:type ?type . \n";
    queryString += "        ?property welcome:hasValue ?value . \n";
    queryString += "        ?property welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasOntologyType ?t . \n";
    queryString += "        ?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	    ?content a rdf:Statement ; \n";
    queryString += "    	             rdf:subject ?subjectX ; \n";
    queryString += "    	             rdf:predicate ?predicateX ; \n";
    queryString += "    	             rdf:object ?objectX . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "    OPTIONAL { \n";
    queryString += "        welcome:expiration_period welcome:duration ?duration . \n";
    queryString += "    } \n";
    queryString += "    BIND(IRI(?t) as ?temp) \n";
    queryString += "    BIND(IF(!CONTAINS(STR(?temp), " +
        "\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\"), " +
        "(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", "
        +
        "STRAFTER(STR(?t), \":\")))), " +
        "?temp) " +
        "as ?final) \n";
    queryString += "    BIND(EXISTS {?final welcome:needsUpdate true} as ?update) \n";
    queryString += "    BIND(IF(?update = true && ((" + timestamp
        + " - ?timestamp) / 86400  >= ?duration), welcome:NeedsUpdate, welcome:Completed) as ?newStatus) \n";
    queryString += "    FILTER(?type != welcome:Language && ?type = ?final) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Returns IRI of active slot
   *
   * @return
   */
  public List<IRI> activeSlot_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?slot WHERE { \n";
    queryString += "    GRAPH welcome:SlotInfo { \n";
    queryString += "        ?slot welcome:isActiveSlot true . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    List<IRI> activeSlot = new ArrayList<>();
    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value slot = solution.getBinding("slot").getValue();
        activeSlot.add(Utilities.f.createIRI(slot.stringValue()));
      }
    }

    return activeSlot;
  }

  public String prevActiveSlot_S() {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?slot WHERE { \n";
    queryString += "    GRAPH welcome:prevDipInfo { \n";
    queryString += "        welcome:faq_prev_active_slot welcome:hasValue ?slot . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    String activeSlot = "";

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value slot = solution.getBinding("slot").getValue();
        activeSlot = slot.stringValue();
      }
    }

    return activeSlot;
  }

  public void setBSlotStatus_I(Resource dipGraph, String dipSlot, String status, String value,
      String confidence, String ontoType) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (status.contentEquals("Completed") || status.contentEquals("Pending")) {
      queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "    	?content a rdf:Statement ; \n";
      queryString += "    	         rdf:subject ?subjectX ; \n";
      queryString += "    	         rdf:predicate ?predicateX ; \n";
      queryString += "    	         rdf:object ?objectX . \n";
    }
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (status.contentEquals("Completed")) {
      queryString += "    	?slot welcome:hasTCNAnswer \n";
      queryString += "        [ a rdf:Statement; \n";
      if (ontoType != null) {
        queryString += "       		rdf:subject welcome:" + ontoType + "; \n";
      } else {
        queryString += "       		rdf:subject welcome:Boolean; \n";
      }
      queryString += "       		rdf:predicate welcome:hasValue; \n";
      queryString += "          rdf:object \"" + value + "\"] . \n";
    } else if (status.contentEquals("Pending")) {
      queryString += "    	?slot welcome:hasTCNAnswer welcome:Unknown . \n";
    }
    queryString += "    	?slot welcome:hasStatus welcome:" + status + " . \n";
    queryString += "    	?slot welcome:confidenceScore \"" + confidence + "\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (status.contentEquals("Completed") || status.contentEquals("Pending")) {
      queryString += "        ?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "        OPTIONAL { \n";
      queryString += "    	    ?content a rdf:Statement ; \n";
      queryString += "    	             rdf:subject ?subjectX ; \n";
      queryString += "    	             rdf:predicate ?predicateX ; \n";
      queryString += "    	             rdf:object ?objectX . \n";
      queryString += "        } \n";
    }
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + dipSlot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void setBSlotStatus_I(Resource dipGraph, String dipSlot, String status, Boolean value,
      String confidence, String ontoType) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (status.contentEquals("Completed")) {
      queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "    	?content a rdf:Statement ; \n";
      queryString += "    	         rdf:subject ?subjectX ; \n";
      queryString += "    	         rdf:predicate ?predicateX ; \n";
      queryString += "    	         rdf:object ?objectX . \n";
    }
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (status.contentEquals("Completed")) {
      queryString += "    	?slot welcome:hasTCNAnswer \n";
      queryString += "        [ a rdf:Statement; \n";
      if (ontoType != null) {
        queryString += "       		rdf:subject welcome:" + ontoType + "; \n";
      } else {
        queryString += "       		rdf:subject welcome:Boolean; \n";
      }
      queryString += "       		rdf:predicate welcome:hasValue; \n";
      queryString += "          rdf:object " + value + "] . \n";
    }
    queryString += "    	?slot welcome:hasStatus welcome:" + status + " . \n";
    queryString += "    	?slot welcome:confidenceScore \"" + confidence + "\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (status.contentEquals("Completed")) {
      queryString += "        ?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "        OPTIONAL { \n";
      queryString += "    	    ?content a rdf:Statement ; \n";
      queryString += "    	             rdf:subject ?subjectX ; \n";
      queryString += "    	             rdf:predicate ?predicateX ; \n";
      queryString += "    	             rdf:object ?objectX . \n";
      queryString += "        } \n";
    }
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + dipSlot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Updates the slot status and value of a given DIP.
   *
   * @param dipGraph
   * @param dipSlot
   * @param status
   * @param value
   */
  public void setSlotStatus_I(Resource dipGraph, String dipSlot, String status, String value,
      String confidence, Resource ontoType) {
    boolean b = status.contentEquals("Completed")
        || status.contentEquals("Pending")
        || status.contentEquals("Undefined");

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (b) {
      queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "    	?content a rdf:Statement ; \n";
      queryString += "    	         rdf:subject ?subjectX ; \n";
      queryString += "    	         rdf:predicate ?predicateX ; \n";
      queryString += "    	         rdf:object ?objectX . \n";
    }
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (b) {
      queryString += "    	?slot welcome:hasTCNAnswer \n";
      queryString += "        [ a rdf:Statement; \n";
      queryString += "       		rdf:subject <" + ontoType.stringValue() + ">; \n";
      queryString += "       		rdf:predicate welcome:hasValue; \n";
      queryString += "            rdf:object \"" + value + "\"] . \n";
    }
    queryString += "    	?slot welcome:hasStatus welcome:" + status + " . \n";
    queryString += "    	?slot welcome:confidenceScore \"" + confidence + "\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    if (b) {
      queryString += "        ?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "        OPTIONAL { \n";
      queryString += "    	    ?content a rdf:Statement ; \n";
      queryString += "    	             rdf:subject ?subjectX ; \n";
      queryString += "    	             rdf:predicate ?predicateX ; \n";
      queryString += "    	             rdf:object ?objectX . \n";
      queryString += "        } \n";
    }
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + dipSlot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void resetStatus() {
    String queryString;

    queryString = "PREFIX schema: <https://schema.org/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString += "    ?sa schema:actionStatus ?old_status . \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    ?sa schema:actionStatus welcome:Status_Free . \n";
    queryString += "}  \n";
    queryString += "WHERE { \n";
    queryString += "    ?sa schema:actionStatus ?old_status . \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateDialogueStatus() {
    IRI activeDIP = activeDipName_S();

    if (activeDIP.stringValue().contains("Opening")) {
      updateDialogueStatus_I("true");
    } else if (activeDIP.stringValue().contains("ClosingUponFailureFarewell")
        || activeDIP.stringValue().contains("NormalClosingFarewell")
        || activeDIP.stringValue().contains("ClosingUponFailure")
        || activeDIP.stringValue().contains("ContactNGOFarewell")
        || activeDIP.stringValue().contains("SimulationClosing")
        || activeDIP.stringValue().contains("InformContactNGO")) {
      updateDialogueStatus_I("false");
    }
  }

  public void updateDialogueStatus_I(String status) {
    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    /* Clear DipSession graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "DialogueInfo");
    Utilities.connection.clear(graph);

    /* Update DipSession graph */
    builder
        .namedGraph(graph)
        .subject(graph)
        .add(WELCOME.ACTIVE, status);

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Updating dialogue status.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  public void resetSubTopics(String scenarioName) {
    String queryString;

    IRI activeDIP = activeDipName_S();
    IRI graph = activeDIP_S();

    if (activeDIP.stringValue().contains("ScenarioIntroduction")) {

      queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
      queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
      queryString += "DELETE { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "    } \n";
      queryString += "} \n";
      queryString += "INSERT { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "    	?slot welcome:hasTCNAnswer \n";
      queryString += "        [ a rdf:Statement; \n";
      queryString += "       		rdf:subject welcome:Boolean; \n";
      queryString += "       		rdf:predicate welcome:hasValue; \n";
      queryString += "          rdf:object false] . \n";
      queryString += "    } \n";
      queryString += "} WHERE { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
      queryString += "    } \n";
      queryString += "    FILTER (CONTAINS(LCASE(STR(?slot)), \"flag\"))  \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);

      // Store DIP for later usage
      this.setScenarioIntroDIPStatus(graph, scenarioName);
    }
  }

  public void setScenarioIntroDIPStatus(Resource context, String scenarioName) {
    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    String tempString = (scenarioName.contentEquals("schooling")) ? "S8TempGraph" : "S5TempGraph";

    /* Clear DipInfo graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + tempString);
    Utilities.connection.clear(graph);

    /* Update DipInfo graph */
    builder
        .namedGraph("welcome:" + tempString)
        .subject("welcome:activeDIP")
        .add(WELCOME.BELONGSTOGRAPH, context);

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Updating active DIP.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  public void resetAgentRole() {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Create IRI for flag */
    String flag = "OfficerRoleFlag";
    IRI ontoType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, flag);

    IRI activeDIP = activeDipName_S();

    if (activeDIP.stringValue().contains("LegalServiceCallback")) {
      updateProfile(builder, ontoType, false, 1);
      /* Create user IRI */
      IRI profile = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "ProfileInfoSimulation");

      /* Remove AsylumPreRegistrationNumber */
      removeTCNData("CardNumber", profile);
    } else if (activeDIP.stringValue().contains("ScenarioIntroduction")) {
      IRI prevActiveDIP = prevActiveDipName_S();
      if (prevActiveDIP.stringValue().contains("ContactNGOFarewellSimulation")) {
        updateProfile(builder, ontoType, false, 0);
        /* Create user IRI */
        IRI profile = Utilities.f
            .createIRI(WELCOME.NAMESPACE, "ProfileInfoSimulation");

        /* Remove AsylumPreRegistrationNumber */
        removeTCNData("CardNumber", profile);
      }
    } else if (activeDIP.stringValue().contains("ObtainTCNInformation")) {
      /* Create user IRI */
      IRI profile = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "ProfileInfoSimulation");

      removeTCNData("Name", profile);
      removeTCNData("MobilePhone", profile);
    }
  }

  public void updateProfile(ModelBuilder builder, IRI ontoType, Boolean anchor, Integer sim) {
    /* Create IRI for the field */
    BNode iri = Utilities.f.createBNode();

    /* Get user's IRI */
    IRI userIRI = getUser();

    /* Split IRI from actual name */
    String predicate = util.splitIRI(ontoType);

    deleteProfileInfo_D(predicate, sim);

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    if (sim == 1) {
      /* Add fields to builder */
      builder.namedGraph("welcome:ProfileInfoSimulation")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);

      /* We're done building, create our Model */
      Model model = builder.build();

      /* Commit model to repository */
      util.commitModel(model);
    } else if (sim == 0) {
      /* Add fields to builder */
      builder.namedGraph("welcome:ProfileInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);

      /* We're done building, create our Model */
      Model model = builder.build();

      /* Commit model to repository */
      util.commitModel(model);
    } else if (sim == 2) {
      /* Add fields to builder */
      builder.namedGraph("welcome:CVInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);

      /* We're done building, create our Model */
      Model model = builder.build();

      /* Commit model to repository */
      util.commitModel(model);
    }
  }

  public IRI getUser() {
    ValueFactory f = Utilities.repository.getValueFactory();

    // Init variables
    IRI userIRI = null;
    String queryString;

    // Find the IRI of the Dialogue User and the Dialogue Session
    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\n";
    queryString += "SELECT DISTINCT ?user where { \n";
    queryString += "    ?user rdf:type welcome:DialogueUser .\n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value user = solution.getBinding("user").getValue();
        userIRI = f.createIRI(user.stringValue());
      }
    }

    return userIRI;
  }

  public void resetAsylumKnowledge() {
    IRI activeDIP = activeDipName_S();

    if (activeDIP.stringValue().contains("AsylumApplicationKnowledge")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasAsylumApplicationKnowledge ?knowledge . \n";
      queryString += "    ?knowledge rdf:type ?type . \n";
      queryString += "    ?knowledge welcome:hasValue ?value . \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    } else if (activeDIP.stringValue().contains("AsylumPreRegistration")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasAsylumPreregistration ?preReg . \n";
      queryString += "    ?preReg rdf:type ?type . \n";
      queryString += "    ?preReg welcome:hasValue ?value . \n";

      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    } else if (activeDIP.stringValue().contains("BeginFillingOnlineForm")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasAsylumPreRegistrationNumber ?preReg . \n";
      queryString += "    ?preReg rdf:type ?type . \n";
      queryString += "    ?preReg welcome:hasValue ?value . \n";

      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    }
  }

  public void resetAppointmentConcern() {

    IRI activeDIP = activeDipName_S();

    if (activeDIP.stringValue().contains("InformAsylumClaim")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasKnowledgeSkype ?concern . \n";
      queryString += "    ?concern rdf:type ?type . \n";
      queryString += "    ?concern welcome:hasValue ?value . \n";

      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    } else if (activeDIP.stringValue().contains("InformSkypeAccountCreation")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasInternetAccess ?concern . \n";
      queryString += "    ?concern rdf:type ?type . \n";
      queryString += "    ?concern welcome:hasValue ?value . \n";

      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    } else if (activeDIP.stringValue().contains("ObtainPersonalInformation")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasFirstTimeAsylumApplicant ?concern . \n";
      queryString += "    ?concern rdf:type ?type . \n";
      queryString += "    ?concern welcome:hasValue ?value . \n";

      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    } else if (activeDIP.stringValue().contains("FirstContactInformation")) {
      /* Get the context of the active DIP */
      IRI graph = activeDIP_S();

      String queryString;

      queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
      queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
      queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
      queryString += "DELETE { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "    	?slot welcome:hasStatus ?status . \n";
      queryString += "    	?slot welcome:confidenceScore ?score . \n";
      queryString += "    } \n";
      queryString += "} \n";
      queryString += "INSERT { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "    	?slot welcome:hasStatus ?newStatus . \n";
      queryString += "    	?slot welcome:confidenceScore \"1.0\"^^xsd:float .  \n";
      queryString += "    } \n";
      queryString += "} WHERE { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "        ?slot welcome:hasStatus ?status . \n";
      queryString += "        ?slot welcome:confidenceScore ?score . \n";
      queryString += "    } \n";
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
      queryString += "        ?p rdf:type welcome:OfficerRoleFlag . \n";
      queryString += "        ?p welcome:hasValue ?value . \n";
      queryString += "    } \n";
      queryString += "    BIND(IF(?value = true, welcome:Completed, ?status) as ?newStatus) \n";
      queryString += "    FILTER(CONTAINS(STR(?slot), \"informBehaviorRepetition\")) \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    }
  }

  public void resetSimulationConcern() {

    IRI activeDIP = activeDipName_S();

    if (activeDIP.stringValue().contains("SimulationIntro")
        || activeDIP.stringValue().contains("MatterConcernIdentification")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasAppointmentConcern ?concern . \n";
      queryString += "    ?concern rdf:type ?type . \n";
      queryString += "    ?concern welcome:hasValue ?value . \n";
      queryString += "    ?s welcome:hasMatterConcern ?m_concern . \n";
      queryString += "    ?m_concern rdf:type ?m_type . \n";
      queryString += "    ?m_concern welcome:hasValue ?m_value . \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    } else if (activeDIP.stringValue().contains("MakeAppointment")) {

      String queryString;

      queryString = "PREFIX schema: <https://schema.org/> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    ?s welcome:hasMatterConcernStatus ?concern . \n";
      queryString += "    ?concern rdf:type ?type . \n";
      queryString += "    ?concern welcome:hasValue ?value . \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    }
  }

  /**
   * Updates the slot status and value of a given DIP.
   *
   * @param dipGraph
   * @param dipSlot
   * @param status
   */
  public void setInfoSlotStatus_I(Resource dipGraph, String dipSlot, String status,
      String confidence) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus welcome:" + status + " . \n";
    queryString += "    	?slot welcome:confidenceScore \"" + confidence + "\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + dipSlot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Updates inform module slots of the InformFirstReception DIP.
   *
   * @param graph
   * @param moduleName
   */
  public void updateInformModule_I(Resource graph, String moduleName) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    ?module welcome:moduleName ?m_name ; \n";
    queryString += "            welcome:offersCourse ?course . \n";
    queryString += "    ?course welcome:hasNumberHours ?hours ; \n";
    queryString += "            welcome:hasCourseName ?c_name . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?module welcome:moduleName ?m_name ; \n";
    queryString += "            welcome:offersCourse ?course . \n";
    queryString += "    ?course welcome:hasNumberHours ?hours ; \n";
    queryString += "            welcome:hasCourseName ?c_name . \n";
    queryString += "    ?module dcterms:isRequiredBy ?slot . \n";
    queryString += "    FILTER (?slot = \"" + moduleName + "\") \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Updates inform module address slots of the InformFirstReception DIP.
   *
   * @param graph
   * @param moduleName
   */
  public void updateInformModuleHours_I(Resource graph, String moduleName) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX schema: <https://schema.org/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    ?module welcome:hasHoursSpec ?spec . \n";
    queryString += "    ?spec schema:dayOfWeek ?dow . \n";
    queryString += "    ?spec schema:opens ?opens ; \n";
    queryString += "          schema:closes ?closes . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?module welcome:hasHoursSpec ?spec . \n";
    queryString += "    ?spec schema:dayOfWeek ?dow . \n";
    queryString += "    ?spec schema:opens ?opens ; \n";
    queryString += "          schema:closes ?closes . \n";
    queryString += "    ?spec dcterms:isRequiredBy ?slot . \n";
    queryString += "    FILTER (?slot = \"" + moduleName + "\") \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Updates inform module hours slots of the InformFirstReception DIP.
   *
   * @param graph
   * @param moduleName
   */
  public void updateInformModuleAddress_I(Resource graph, String moduleName) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX gn: <https://www.geonames.org/ontology#> \n";
    queryString += "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    ?module gn:nearby ?poi . \n";
    queryString += "    ?poi actor:name ?loc . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?module gn:nearby ?poi . \n";
    queryString += "    ?poi actor:name ?loc . \n";
    queryString += "    ?poi dcterms:isRequiredBy ?slot . \n";
    queryString += "    FILTER (?slot = \"" + moduleName + "\") \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public boolean checkDialogueStatus() {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "ASK WHERE {  \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#DialogueInfo> { \n";
    queryString += "        ?dialogue welcome:active \"true\" . \n";
    queryString += "    } \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }

  public Boolean checkPendingTopics(Resource graph) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "ASK WHERE {\n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?s1 ?p1 ?o1 . \n";
    queryString += "        ?s1 welcome:hasTCNAnswer ?a1 . \n";
    queryString += "        ?a1 rdf:object ?value1 . \n";
    queryString += "         \n";
    queryString += "        ?s2 ?p2 ?o2 . \n";
    queryString += "        ?s2 welcome:hasTCNAnswer ?a2 . \n";
    queryString += "        ?a2 rdf:object ?value2 . \n";
    queryString += "    } \n";
    queryString += "    FILTER(CONTAINS(STR(?s1), \"flag\")) \n";
    queryString += "    FILTER(CONTAINS(STR(?s2), \"flag\")) \n";
    queryString += "     \n";
    queryString += "    BIND(STRAFTER(REPLACE(STR(?s1),\"Requested\",\"\"), \"#\") as ?temp1) \n";
    queryString += "    BIND(STRAFTER(REPLACE(STR(?s2),\"Informed\",\"\"), \"#\") as ?temp2) \n";
    queryString += "     \n";
    queryString += "    FILTER(?temp1 = ?temp2 && ?value1 != ?value2) \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }

  public Boolean checkOpenTopics(Resource graph) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "ASK WHERE {\n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?s1 ?p1 ?o1 . \n";
    queryString += "        ?s1 welcome:hasTCNAnswer ?a1 . \n";
    queryString += "        ?a1 rdf:object ?value1 . \n";
    queryString += "         \n";
    queryString += "        ?s2 ?p2 ?o2 . \n";
    queryString += "        ?s2 welcome:hasTCNAnswer ?a2 . \n";
    queryString += "        ?a2 rdf:object ?value2 . \n";
    queryString += "    } \n";
    queryString += "    FILTER(CONTAINS(STR(?s1), \"flag\")) \n";
    queryString += "    FILTER(CONTAINS(STR(?s2), \"flag\")) \n";
    queryString += "     \n";
    queryString += "    BIND(STRAFTER(REPLACE(STR(?s1),\"Requested\",\"\"), \"#\") as ?temp1) \n";
    queryString += "    BIND(STRAFTER(REPLACE(STR(?s2),\"Informed\",\"\"), \"#\") as ?temp2) \n";
    queryString += "     \n";
    queryString += "    FILTER(?temp1 = ?temp2 && ?value1 = false && ?value2 = false) \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }


  public String checkFAQStatus() {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "SELECT ?v WHERE {  \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#FAQInfo> { \n";
    queryString += "        welcome:faqStatus welcome:hasValue ?v . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    String status = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        status = solution.getBinding("v").getValue().stringValue();
      }
    }

    return status;
  }

  /**
   * Checks if a DIP has slots that are associated with RDF triples.
   *
   * @param graph
   * @return
   */
  public boolean checkInformSlots_A(Resource graph) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "ASK WHERE {  \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?dip welcome:hasSlot ?slot . \n";
    queryString += "    } \n";
    queryString += "	?module dcterms:isRequiredBy ?req . \n";
    queryString += "    FILTER(CONTAINS(LCASE(STR(?slot)),LCASE(STR(?req)))) \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }

  public boolean checkIfPopulated(String key) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "ASK WHERE { \n";
    queryString += "    welcome:tcn_user ?pred ?obj . \n";
    queryString += "    ?obj rdf:type ?type \n";
    queryString += "    FILTER(?type = welcome:" + key + ") \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }

  public void populateTCNProfile(String key, String value) {
    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "    	?s welcome:hasValue ?old_value ; \n";
    queryString += "         welcome:lastUpdated ?timestamp . \n";
    queryString += "    }  \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> {  \n";
    queryString += "    	?s welcome:hasValue ?value ; \n";
    queryString += "         welcome:lastUpdated " + timestamp + " \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> {  \n";
    queryString += "    	welcome:tcn_user ?pred ?s . \n";
    queryString += "        ?s rdf:type welcome:" + key + " . \n";
    queryString += "        ?s welcome:hasValue ?old_value . \n";
    queryString += "        ?s welcome:lastUpdated ?timestamp . \n";
    queryString += "        BIND(\"" + value + "\" as ?value) \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void insertTCNData(String key, String value, IRI graph) {
    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	welcome:tcn_user welcome:has" + key + " \n";
    queryString += "        [   a welcome:" + key + " ; \n";
    queryString += "        	welcome:hasValue \"" + value + "\" ; \n";
    queryString += "        	welcome:lastUpdated " + timestamp + " \n";
    queryString += "        ]. \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void removeTCNData(String key, IRI graph) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	welcome:tcn_user welcome:has" + key + " ?key . \n";
    queryString += "        ?key rdf:type welcome:" + key + " ; \n";
    queryString += "        	   welcome:hasValue ?value ; \n";
    queryString += "        	   welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	welcome:tcn_user welcome:has" + key + " ?key . \n";
    queryString += "        ?key rdf:type welcome:" + key + " ; \n";
    queryString += "        	   welcome:hasValue ?value . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "        	   ?key welcome:lastUpdated ?timestamp . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void initSubTopics(String scenarioName) {
    String queryString;

    queryString = "PREFIX wdrs: <http://www.w3.org/2007/05/powder-s#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select ?o ?scenario where { \n";
    queryString += "	?s wdrs:describedby ?o . \n";
    queryString += "    ?s welcome:thirdCondition \"Pending\" . \n";
    queryString += "    ?s welcome:secondaryCondition ?scenario . \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value topic = solution.getBinding("o").getValue();
        Value scenario = solution.getBinding("scenario").getValue();

        if (scenario.stringValue().toLowerCase().contains(scenarioName)) {
          updateTopicStatus(builder, topic.stringValue(), "Pending", scenarioName);
        }
      }

      /* We're done building, create our Model */
      Model model = builder.build();

      /* Commit model to repository */
      util.commitModel(model);
    }
  }

  public void insertAvatarConfigString(String key, String value) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#AvatarConfig> { \n";
    queryString += "    	welcome:Avatar welcome:has" + key + " welcome:" + key + " . \n";
    queryString += "        welcome:" + key + "	welcome:hasValue \"" + value + "\" . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void insertAvatarConfigLong(String key, long value) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#AvatarConfig> { \n";
    queryString += "    	welcome:Avatar welcome:has" + key + " welcome:" + key + " . \n";
    queryString += "        welcome:" + key + "	welcome:hasValue \"" + value + "\" . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void insertAvatarConfigDouble(String key, double value) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#AvatarConfig> { \n";
    queryString += "    	welcome:Avatar welcome:has" + key + " welcome:" + key + " . \n";
    queryString += "        welcome:" + key + "	welcome:hasValue " + value + " . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void insertAvatarConfigInteger(String key, int value) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#AvatarConfig> { \n";
    queryString += "    	welcome:Avatar welcome:has" + key + " welcome:" + key + " . \n";
    queryString += "        welcome:" + key + "	welcome:hasValue " + value + " . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public boolean checkFreeSlot_A() {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX schema: <https://schema.org/> \n";
    queryString += "ASK WHERE {  \n";
    queryString += "    ?s schema:actionStatus ?status . \n";
    queryString += "    ?status welcome:hasValue \"Free\" . \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }

  /**
   * Takes a graph with triples and performs reification.
   *
   * @param graph
   * @param temp
   * @param slot
   */
  public void reification(Resource graph, Resource temp, String slot) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "    } \n";
    queryString += " } \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents \n";
    queryString += "        [ a rdf:Statement; \n";
    queryString += "       		rdf:subject ?s; \n";
    queryString += "       		rdf:predicate ?p; \n";
    queryString += "            rdf:object ?o ] . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?dip welcome:hasSlot ?slot . \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "    } \n";
    queryString += "    GRAPH <" + temp.stringValue() + "> { \n";
    queryString += "        ?s ?p ?o \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + slot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Returns the ontology type of the given slot in the given graph.
   *
   * @param graph
   * @param slotName
   * @return
   */
  public IRI getOntologyType_S(Resource graph, String slotName) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "SELECT ?final \n";
    queryString += "FROM <" + graph.stringValue() + "> \n";
    queryString += "WHERE { \n";
    queryString += "    ?s welcome:hasOntologyType ?type . \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?s)), \"#\") = \"" + slotName.toLowerCase() + "\")  \n";
    queryString += "    BIND(IRI(?type) as ?temp) \n";
    queryString += "    BIND(IF(!CONTAINS(STR(?temp), " +
        "\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\"), " +
        "(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", "
        +
        "STRAFTER(STR(?type), \":\")))), " +
        "?temp) " +
        "as ?final) \n";
    queryString += "} \n";
    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI ontologyType = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value slot = solution.getBinding("final").getValue();
        ontologyType = Utilities.f.createIRI(slot.stringValue());
      }
    }

    return ontologyType;
  }

  /**
   * Returns the ontology type of the given slot in the given graph.
   *
   * @param graph
   * @param slotName
   * @return
   */
  public IRI getSlotType_S(Resource graph, String slotName) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "SELECT ?final \n";
    queryString += "FROM <" + graph.stringValue() + "> \n";
    queryString += "WHERE { \n";
    queryString += "    ?s rdf:type ?type . \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?s)), \"#\") = \"" + slotName.toLowerCase() + "\"" +
            " && (CONTAINS(STR(?type), \"System\" ) || CONTAINS(STR(?type), \"Confirmation\" )))  \n";
    queryString += "    BIND(IRI(?type) as ?temp) \n";
    queryString += "    BIND(IF(!CONTAINS(STR(?temp), " +
        "\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\"), " +
        "(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", "
        +
        "STRAFTER(STR(?type), \":\")))), " +
        "?temp) " +
        "as ?final) \n";
    queryString += "} \n";
    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI ontologyType = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value slot = solution.getBinding("final").getValue();
        ontologyType = Utilities.f.createIRI(slot.stringValue());
      }
    }

    return ontologyType;
  }

  /**
   * Returns the property whose range is the give IRI.
   *
   * @param ontologyType
   * @return
   */
  public IRI getProperty(IRI ontologyType) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdfs: <" + RDFS.NAMESPACE + "> \n";
    queryString += "SELECT ?s where {  \n";
    queryString += "	?s rdfs:range ?range . \n";
    queryString += "    FILTER(?range = IRI(<" + ontologyType.stringValue() + ">) ) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI property = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value s = solution.getBinding("s").getValue();
        property = Utilities.f.createIRI(s.stringValue());
      }
    }

    return property;
  }

  /**
   * Updates informSkypeId slot
   *
   * @param graph
   */
  public void updateInformSkypeId_I(Resource graph, IRI minOffice) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?skype  welcome:hasSkypeId ?id ; \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?skype rdf:type welcome:SkypeInformation ; \n";
    queryString += "           welcome:hasSkypeId ?id ; \n";
    queryString += "           welcome:requiresLang ?lang . \n";
    queryString += "    ?lang welcome:languageName ?langValue . \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "           ?u_lang rdf:type welcome:Language . \n";
    queryString += "           ?u_lang welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER(LCASE(STR(?langValue)) = LCASE(STR(?value))) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    /* Returns all asylum offices and coordinates */
    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX schema: <https://schema.org/> \n";
    queryString += "SELECT ?info WHERE { \n";
    queryString += "    ?s welcome:hasHoursSpec ?spec . \n";
    queryString += "    ?spec welcome:hasSkypeInfo ?info . \n";
    queryString += "    ?info welcome:requiresLang ?lang . \n";
    queryString += "    ?lang welcome:languageName ?langValue . \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?langx rdf:type welcome:Language . \n";
    queryString += "        ?langx welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?s = IRI(<" + minOffice.stringValue()
        + ">) && LCASE(STR(?langValue)) = LCASE(STR(?value))) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    Value info = null;
    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      if (result.hasNext()) {
        BindingSet solution = result.next();

        info = solution.getBinding("info").getValue();
      }
    }

    if (info != null) {
      queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
      queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
      queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
      queryString += "PREFIX schema: <https://schema.org/> \n";
      queryString += "INSERT { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "        welcome:Language welcome:hasValue ?value ; \n";
      queryString += "    } \n";
      queryString += "} WHERE { \n";
      queryString += "    ?s welcome:hasHoursSpec ?spec . \n";
      queryString += "    ?spec welcome:hasSkypeInfo ?info . \n";
      queryString += "    ?info welcome:requiresLang ?lang . \n";
      queryString += "    ?lang welcome:languageName ?value . \n";
      queryString += "    FILTER (?info = IRI(<" + info.stringValue() + ">)) \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    }
  }

  public IRI calcOfficePRAKSIS() {
    String queryString;

    /* Returns all asylum offices and coordinates */
    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select ?office ?lat ?long ?latx ?longx where { \n";
    queryString += "	?office rdf:type welcome:AsylumOffice ; \n";
    queryString += "         welcome:hasCity ?city . \n";
    queryString += "    ?city welcome:lat ?lat ; \n";
    queryString += "          welcome:long ?long . \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?x rdf:type welcome:City . \n";
    queryString += "        ?x welcome:lat ?latx . \n";
    queryString += "        ?x welcome:long ?longx . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    double minDistance = 100000000000000.0;
    Value minOffice = null;

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value office = solution.getBinding("office").getValue();
        double lat = Double.parseDouble((solution.getBinding("lat").getValue()).stringValue());
        double lng = Double.parseDouble((solution.getBinding("long").getValue()).stringValue());
        double latx = Double.parseDouble((solution.getBinding("latx").getValue()).stringValue());
        double lngx = Double.parseDouble((solution.getBinding("longx").getValue()).stringValue());

        /* Calculate the office with the minimum distance */
        if (minDistance > util.twoPointDistance(lat, latx, lng, lngx)) {
          minDistance = util.twoPointDistance(lat, latx, lng, lngx);
          minOffice = office;
        }
      }
    }

    if (minOffice != null) {
      IRI response = Utilities.f.createIRI(minOffice.stringValue());
      return response;
    } else {
      return null;
    }
  }

  /**
   * Updates informSkypeSlot slot
   *
   * @param graph
   */
  public void updateInformSkypeSlot_I(Resource graph, IRI minOffice) {
    String queryString;

    if (minOffice != null) {
      /* Takes the closest office and finds the time slot based on user's language. */
      queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
      queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
      queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
      queryString += "PREFIX schema: <https://schema.org/> \n";
      queryString += "INSERT { \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "        ?spec schema:dayOfWeek ?day ; \n";
      queryString += "            schema:opens ?opens ; \n";
      queryString += "            schema:closes ?closes . \n";
      queryString += "    } \n";
      queryString += "} WHERE { \n";
      queryString += "    ?s welcome:hasHoursSpec ?spec . \n";
      queryString += "    ?spec welcome:hasSkypeInfo ?info ; \n";
      queryString += "          schema:dayOfWeek ?dow ; \n";
      queryString += "          schema:opens ?opens ; \n";
      queryString += "          schema:closes ?closes . \n";
      queryString += "    ?dow welcome:dayOfWeekNoRepeat ?day . \n";
      queryString += "    ?info welcome:requiresLang ?lang . \n";
      queryString += "    ?lang welcome:languageName ?langValue . \n";
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
      queryString += "        ?langx rdf:type welcome:Language . \n";
      queryString += "        ?langx welcome:hasValue ?value . \n";
      queryString += "    } \n";
      queryString += "    FILTER (?s = IRI(<" + minOffice.stringValue()
          + ">) && LCASE(STR(?langValue)) = LCASE(STR(?value))) \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    }
  }

  public IRI updateInformContactPRAKSIS_S() {
    String queryString;

    /* Returns all asylum offices and coordinates */
    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX locn: <http://www.w3.org/ns/locn#> \n";
    queryString += "select ?address ?lat ?long ?latx ?longx where { \n";
    queryString += "	?address rdf:type locn:Address ; \n";
    queryString += "          welcome:lat ?lat ; \n";
    queryString += "          welcome:long ?long ; \n";
    queryString += "          dcterms:isRequiredBy ?slot . \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?x rdf:type welcome:City . \n";
    queryString += "        ?x welcome:lat ?latx . \n";
    queryString += "        ?x welcome:long ?longx . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?slot = \"informAddress\") \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    Double minDistance = 100000000000000.0;
    Value minAddress = null;

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value address = solution.getBinding("address").getValue();
        double lat = Double.parseDouble((solution.getBinding("lat").getValue()).stringValue());
        double lng = Double.parseDouble((solution.getBinding("long").getValue()).stringValue());
        double latx = Double.parseDouble((solution.getBinding("latx").getValue()).stringValue());
        double lngx = Double.parseDouble((solution.getBinding("longx").getValue()).stringValue());

        /* Calculate the office with the minimum distance */
        if (minDistance > util.twoPointDistance(lat, latx, lng, lngx)) {
          minDistance = util.twoPointDistance(lat, latx, lng, lngx);
          minAddress = address;
        }
      }
    }

    if (minAddress != null) {
      return Utilities.f.createIRI(minAddress.stringValue());
    } else {
      return null;
    }
  }

  public IRI updateInformRegistrationService_S() {
    String queryString;

    /* Returns all registration offices and coordinates */
    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX locn: <http://www.w3.org/ns/locn#> \n";
    queryString += "select ?s ?lat ?lng ?latx ?longx where { \n";
    queryString += "	?s rdf:type welcome:RegistrationOffice . \n";
    queryString += "          ?s locn:address ?address . \n";
    queryString += "          ?address welcome:lat ?lat ; \n";
    queryString += "                   welcome:long ?lng . \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?x rdf:type welcome:City . \n";
    queryString += "        ?x welcome:lat ?latx . \n";
    queryString += "        ?x welcome:long ?longx . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    Double minDistance = 100000000000000.0;
    Value minAddress = null;

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        IRI s = (IRI) solution.getBinding("s").getValue();
        Double lat = Double.parseDouble((solution.getBinding("lat").getValue()).stringValue());
        Double lng = Double.parseDouble((solution.getBinding("lng").getValue()).stringValue());
        Double latx = Double.parseDouble((solution.getBinding("latx").getValue()).stringValue());
        Double lngx = Double.parseDouble((solution.getBinding("longx").getValue()).stringValue());

        /* Calculate the office with the minimum distance */
        if (minDistance > util.twoPointDistance(lat, latx, lng, lngx)) {
          minDistance = util.twoPointDistance(lat, latx, lng, lngx);
          minAddress = s;
        }
      }
    }

    if (minAddress != null) {
      IRI response = Utilities.f.createIRI(minAddress.stringValue());
      return response;
    } else {
      return null;
    }
  }

  public void updateInformTelephone_I(Resource graph, String temp, Resource address) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#> \n";
    queryString += "PREFIX locn: <http://www.w3.org/ns/locn#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?office actor:phone ?phone ; \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?office locn:address ?address ; \n";
    queryString += "           actor:phone ?phone ; \n";
    queryString += "           dcterms:isRequiredBy ?slot .\n";
    queryString +=
        "    FILTER (?slot = \"" + temp + "\" && ?address = IRI(<" + address.stringValue()
            + ">)) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateInformEmail_I(Resource graph, String temp, Resource address) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#> \n";
    queryString += "PREFIX locn: <http://www.w3.org/ns/locn#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?office actor:email ?email ; \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?office locn:address ?address ; \n";
    queryString += "           actor:email ?email ; \n";
    queryString += "           dcterms:isRequiredBy ?slot .\n";
    queryString +=
        "    FILTER (?slot = \"" + temp + "\" && ?address = IRI(<" + address.stringValue()
            + ">)) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateInformAddress_I(Resource graph, String temp, Resource address) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#> \n";
    queryString += "PREFIX locn: <http://www.w3.org/ns/locn#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?address actor:physicalAddress ?p_address ; \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?office locn:address ?address . \n";
    queryString += "    ?address actor:physicalAddress ?p_address ; \n";
    queryString += "           dcterms:isRequiredBy ?slot .\n";
    queryString +=
        "    FILTER (?slot = \"" + temp + "\" && ?address = IRI(<" + address.stringValue()
            + ">)) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateInformOfficeAddress_I(Resource graph, Resource address) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#> \n";
    queryString += "PREFIX locn: <http://www.w3.org/ns/locn#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?address actor:physicalAddress ?p_address ; \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?office locn:address ?address . \n";
    queryString += "    ?address actor:physicalAddress ?p_address . \n";
    queryString += "    FILTER (?office = IRI(<" + address.stringValue() + ">)) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateInformOfficeHours_I(Resource graph, Resource address) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#> \n";
    queryString += "PREFIX locn: <http://www.w3.org/ns/locn#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX schema: <https://schema.org/> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?office schema:opens ?opens ; \n";
    queryString += "                schema:closes ?closes . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?office schema:opens ?opens ; \n";
    queryString += "            schema:closes ?closes . \n";
    queryString += "    FILTER (?office = IRI(<" + address.stringValue() + ">)) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateDependencies(Resource graph, Integer sim) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus welcome:Completed . \n";
    queryString += "    	?slot welcome:confidenceScore \"1.0\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "        ?slot welcome:hasOntologyType ?t . \n";
    queryString += "    } \n";
    queryString += "    BIND(IRI(?t) as ?temp) \n";
    queryString += "    BIND(IF(!CONTAINS(STR(?temp), " +
        "\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\"), " +
        "(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", "
        +
        "STRAFTER(STR(?t), \":\")))), " +
        "?temp) " +
        "as ?final) \n";
    queryString += "    ?final welcome:dependsOn ?dependency \n";
    if (sim == 0) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    } else if (sim == 2) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    }
    queryString += "        ?p rdf:type ?type . \n";
    queryString += "        ?p welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    BIND(STRAFTER(STR(?type), \"#\") as ?temp1) \n";
    queryString += "    FILTER(?temp1 = ?dependency && (?value = \"No\" || ?value = \"Skip\")) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateSlotDependencies(Resource graph, Integer sim) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?statusA . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?newStatus . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slotA welcome:hasStatus ?statusA . \n";
    queryString += "        ?slotA welcome:hasOntologyType ?type . \n";
    queryString += "        ?slotB welcome:hasTCNAnswer ?content . \n";
    queryString += "    	  ?content a rdf:Statement ; \n";
    queryString += "    	           rdf:subject ?subjectX ; \n";
    queryString += "    	           rdf:predicate ?predicateX ; \n";
    queryString += "    	           rdf:object ?objectX . \n";
    queryString += "    } \n";
    queryString += "    ?x welcome:dependsOnSlot ?dependency . \n";
    queryString += "    ?x welcome:conditionValue ?condition .\n";
    queryString +=
        "    BIND(((?objectX = \"No\" || ?objectX = \"Skip\") && (STRAFTER(STR(?type), \"#\") = STRAFTER(STR(?x), \"#\") "
            + "&& STRAFTER(STR(?slotB), \"#\") = ?dependency)) as ?check) \n";
    queryString += "    BIND(IF(?check && ?statusA != welcome:Completed, welcome:Completed, ?statusA) as ?newStatus) \n";
    queryString += "    FILTER(?check = true) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?statusA . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?newStatus . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slotA welcome:hasStatus ?statusA . \n";
    queryString += "        ?slotA welcome:hasOntologyType ?type . \n";
    queryString += "        ?slotB welcome:hasTCNAnswer ?content . \n";
    queryString += "    	  ?content a rdf:Statement ; \n";
    queryString += "    	           rdf:subject ?subjectX ; \n";
    queryString += "    	           rdf:predicate ?predicateX ; \n";
    queryString += "    	           rdf:object ?objectX . \n";
    queryString += "    } \n";
    queryString += "    ?x welcome:secondaryDependency ?dependency . \n";
    queryString += "    ?x welcome:secondaryCondition ?condition .\n";
    queryString += "    BIND(((STRAFTER(STR(?type), \"#\") = STRAFTER(STR(?x), \"#\")) && STRAFTER(STR(?slotB), \"#\") = ?dependency) as ?check) \n";
    queryString += "    BIND(IF(LCASE(STR(?objectX)) = LCASE(STR(?condition)) || LCASE(STR(?objectX)) = \"Skip\", true, false) as ?check2) \n";
    queryString += "    BIND(IF(?check, IF(?check2 && ?statusA != welcome:Completed, welcome:Pending, welcome:Completed), ?statusA) as ?newStatus) \n";
    queryString += "    FILTER(?check = true) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?statusA . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?newStatus . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slotA welcome:hasStatus ?statusA . \n";
    queryString += "        ?slotA welcome:hasOntologyType ?type . \n";
    queryString += "        ?slotB welcome:hasTCNAnswer ?content . \n";
    queryString += "    	  ?content a rdf:Statement ; \n";
    queryString += "    	           rdf:subject ?subjectX ; \n";
    queryString += "    	           rdf:predicate ?predicateX ; \n";
    queryString += "    	           rdf:object ?objectX . \n";
    queryString += "    } \n";
    queryString += "    ?x welcome:thirdDependency ?dependency . \n";
    queryString += "    ?x welcome:thirdCondition ?condition .\n";
    queryString += "    BIND(((STRAFTER(STR(?type), \"#\") = STRAFTER(STR(?x), \"#\")) && STRAFTER(STR(?slotB), \"#\") = ?dependency) as ?check) \n";
    queryString += "    BIND(IF(LCASE(STR(?objectX)) = LCASE(STR(?condition)) || LCASE(STR(?objectX)) = \"Skip\", true, false) as ?check2) \n";
    queryString += "    BIND(IF(?check, IF(?check2 && ?statusA != welcome:Completed, welcome:Pending, welcome:Completed), ?statusA) as ?newStatus) \n";
    queryString += "    FILTER(?check = true) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?statusA . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slotA welcome:hasStatus ?newStatus . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slotA welcome:hasStatus ?statusA . \n";
    queryString += "        ?slotA welcome:hasOntologyType ?type . \n";
    queryString += "        ?slotB welcome:hasTCNAnswer ?content . \n";
    queryString += "    	  ?content a rdf:Statement ; \n";
    queryString += "    	           rdf:subject ?subjectX ; \n";
    queryString += "    	           rdf:predicate ?predicateX ; \n";
    queryString += "    	           rdf:object ?objectX . \n";
    queryString += "    } \n";
    queryString += "    ?x welcome:fourthDependency ?dependency . \n";
    queryString += "    ?x welcome:fourthCondition ?condition .\n";
    queryString += "    BIND(((STRAFTER(STR(?type), \"#\") = STRAFTER(STR(?x), \"#\")) && STRAFTER(STR(?slotB), \"#\") = ?dependency) as ?check) \n";
    queryString += "    BIND(IF(LCASE(STR(?objectX)) = LCASE(STR(?condition)) || LCASE(STR(?objectX)) = \"Skip\", true, false) as ?check2) \n";
    queryString += "    BIND(IF(?check, IF(?check2 && ?statusA != welcome:Completed, welcome:Pending, welcome:Completed), ?statusA) as ?newStatus) \n";
    queryString += "    FILTER(?check = true) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateDependenciesSim(Resource graph) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX xsd: <" + XSD.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus welcome:Completed . \n";
    queryString += "    	?slot welcome:confidenceScore \"1.0\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "        ?slot welcome:hasOntologyType ?t . \n";
    queryString += "    } \n";
    queryString += "    BIND(IRI(?t) as ?temp) \n";
    queryString += "    BIND(IF(!CONTAINS(STR(?temp), " +
        "\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\"), " +
        "(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", "
        +
        "STRAFTER(STR(?t), \":\")))), " +
        "?temp) " +
        "as ?final) \n";
    queryString += "    ?final welcome:dependsOn ?dependency \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    queryString += "        ?p rdf:type ?type . \n";
    queryString += "        ?p welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    BIND(STRAFTER(STR(?type), \"#\") as ?temp1) \n";
    queryString += "    FILTER(?temp1 = ?dependency && (?value = \"No\" || ?value = \"Skip\")) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateDMSTemplates(Resource graph, Integer sim) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "ASK WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents .  \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	  ?contents a rdf:Statement ;  \n";
    queryString += "    	         rdf:subject ?subjectX ;  \n";
    queryString += "    	         rdf:predicate ?predicateX ;  \n";
    queryString += "    	         rdf:object ?objectX .  \n";
    queryString += "        } \n";
    queryString += "    }  \n";
    queryString += "	  ?p dcterms:isRequiredBy ?o .  \n";
    queryString += "	  ?p dcterms:component \"NLG\" .  \n";
//    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o && ?contents = welcome:Unknown || ?objectX = \"Unknown\")  \n";
    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o)  \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    boolean result = booleanQuery.evaluate();

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>  \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += "  \n";
    queryString += "DELETE {  \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents .  \n";
    queryString += "    	  ?contents a rdf:Statement ;  \n";
    queryString += "    	         rdf:subject ?subjectX ;  \n";
    queryString += "    	         rdf:predicate ?predicateX ;  \n";
    queryString += "    	         rdf:object ?objectX .  \n";
    queryString += "    }  \n";
    queryString += "} \n";
    queryString += "WHERE {  \n";
    queryString += "SELECT DISTINCT ?slot ?contents ?subjectX ?predicateX ?objectX WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot .  \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents .  \n";
    queryString += "        OPTIONAL {  \n";
    queryString += "    	    ?contents a rdf:Statement ;  \n";
    queryString += "    	             rdf:subject ?subjectX ;  \n";
    queryString += "    	             rdf:predicate ?predicateX ;  \n";
    queryString += "    	             rdf:object ?objectX .  \n";
    queryString += "        }  \n";
    queryString += "    }  \n";
    queryString += "	?p dcterms:isRequiredBy ?o .  \n";
    queryString += "	?p dcterms:component \"NLG\" .  \n";
//    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o && ?contents = welcome:Unknown || ?objectX = \"Unknown\")  \n";
    queryString += "OPTIONAL { \n";
    if (sim == 0) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    } else if (sim == 2) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    }
    queryString += "        ?sub rdf:type ?p .  \n";
    queryString += "        ?sub welcome:hasValue ?v .  \n";
    queryString += "    }  \n";
    queryString += "} \n";
    queryString += "    BIND (IF(BOUND(?subjectX), true, false) as ?check) \n";
    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o && (?p = ?subjectX || ?check = false))  \n";
    queryString += "}  \n";
    queryString += "}  \n";

    /* Execute the query */
    util.executeQuery(queryString);

//    if(result) {

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>  \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/>  \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#>  \n";
    queryString += " \n";
    queryString += "INSERT {  \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents [  \n";
    queryString += "          a rdf:Statement;  \n";
    queryString += "          rdf:subject ?p ;  \n";
    queryString += "          rdf:predicate welcome:hasValue ;  \n";
    queryString += "          rdf:object ?fv  \n";
    queryString += "        ].  \n";
    queryString += "    }  \n";
    queryString += "}  \n";
    queryString += "WHERE { \n";
    queryString += "SELECT DISTINCT ?slot ?sub ?p ?fv WHERE {  \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot .  \n";
    queryString += "    }  \n";
    queryString += "	?p dcterms:isRequiredBy ?o .  \n";
    queryString += "	?p dcterms:component \"NLG\" .  \n";
    queryString += "OPTIONAL { \n";
    if (sim == 0) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    } else if (sim == 2) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    }
    queryString += "        ?sub rdf:type ?p .  \n";
    queryString += "        ?sub welcome:hasValue ?v .  \n";
    queryString += "    }  \n";
    queryString += "} \n";
    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o)  \n";
    queryString += "    BIND(IF(BOUND(?sub), ?v, \"Unknown\") as ?fv)  \n";
    queryString += "}  \n";
    queryString += "}  \n";

    /* Execute the query */
    util.executeQuery(queryString);
//    }
  }

  public void updateDMSTemplatesSim(Resource graph) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "ASK WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents .  \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	  ?contents a rdf:Statement ;  \n";
    queryString += "    	         rdf:subject ?subjectX ;  \n";
    queryString += "    	         rdf:predicate ?predicateX ;  \n";
    queryString += "    	         rdf:object ?objectX .  \n";
    queryString += "        } \n";
    queryString += "    }  \n";
    queryString += "	  ?p dcterms:isRequiredBy ?o .  \n";
    queryString += "	  ?p dcterms:component \"NLG\" .  \n";
    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o && ?contents = welcome:Unknown || ?objectX = \"Unknown\")  \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    boolean result = booleanQuery.evaluate();

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>  \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += "  \n";
    queryString += "DELETE {  \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents .  \n";
    queryString += "    	?contents a rdf:Statement ;  \n";
    queryString += "    	         rdf:subject ?subjectX ;  \n";
    queryString += "    	         rdf:predicate ?predicateX ;  \n";
    queryString += "    	         rdf:object ?objectX .  \n";
    queryString += "    }  \n";
    queryString += "} \n";
    queryString += "WHERE {  \n";
    queryString += "SELECT DISTINCT ?slot ?contents ?subjectX ?predicateX ?objectX WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot .  \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents .  \n";
    queryString += "        OPTIONAL {  \n";
    queryString += "    	    ?contents a rdf:Statement ;  \n";
    queryString += "    	             rdf:subject ?subjectX ;  \n";
    queryString += "    	             rdf:predicate ?predicateX ;  \n";
    queryString += "    	             rdf:object ?objectX .  \n";
    queryString += "        }  \n";
    queryString += "    }  \n";
    queryString += "	?p dcterms:isRequiredBy ?o .  \n";
    queryString += "	?p dcterms:component \"NLG\" .  \n";
    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o && ?contents = welcome:Unknown || ?objectX = \"Unknown\")  \n";
    queryString += "}  \n";
    queryString += "}  \n";

    /* Execute the query */
    util.executeQuery(queryString);

    if (result) {

      queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/>  \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>  \n";
      queryString += "PREFIX dcterms: <http://purl.org/dc/terms/>  \n";
      queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#>  \n";
      queryString += " \n";
      queryString += "INSERT {  \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "        ?slot welcome:hasInputRDFContents [  \n";
      queryString += "          a rdf:Statement;  \n";
      queryString += "          rdf:subject ?p ;  \n";
      queryString += "          rdf:predicate welcome:hasValue ;  \n";
      queryString += "          rdf:object ?fv  \n";
      queryString += "        ].  \n";
      queryString += "    }  \n";
      queryString += "}  \n";
      queryString += "WHERE { \n";
      queryString += "SELECT DISTINCT ?slot ?sub ?p ?fv WHERE {  \n";
      queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
      queryString += "        ?s welcome:hasSlot ?slot .  \n";
      queryString += "    }  \n";
      queryString += "	?p dcterms:isRequiredBy ?o .  \n";
      queryString += "	?p dcterms:component \"NLG\" .  \n";
      queryString += "OPTIONAL { \n";
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
      queryString += "        ?sub rdf:type ?p .  \n";
      queryString += "        ?sub welcome:hasValue ?v .  \n";
      queryString += "    }  \n";
      queryString += "} \n";
      queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = ?o)  \n";
      queryString += "    BIND(IF(BOUND(?sub), ?v, \"Unknown\") as ?fv)  \n";
      queryString += "}  \n";
      queryString += "}  \n";

      /* Execute the query */
      util.executeQuery(queryString);
    }

  }

  public void updateConfirmationRequest(Resource graph) {
    IRI langType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "CountryCode");
    String code = getProfileValue(langType, 0);

    /* Create IRI for the field */
    BNode iriName = Utilities.f.createBNode();

    /* Create IRI for the field */
    BNode iriUrl = Utilities.f.createBNode();

    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += " \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot . \n";
    queryString += "        ?slot rdf:type \"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ConfirmationRequest\"^^xsd:anyURI . \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += "PREFIX actor: <http://www.daml.org/services/owl-s/1.1/ActorDefault.owl#> \n";
    queryString += " \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents " + iriName + " . \n";
    queryString += "          " + iriName + " a rdf:Statement; \n";
    queryString += "          rdf:subject ?source1 ; \n";
    queryString += "          rdf:predicate ?property1 ; \n";
    queryString += "          rdf:object ?target1 . \n";
    queryString += "        ?slot welcome:hasInputRDFContents " + iriUrl + " . \n";
    queryString += "          " + iriUrl + " a rdf:Statement; \n";
    queryString += "          rdf:subject ?source2 ; \n";
    queryString += "          rdf:predicate ?property2 ; \n";
    queryString += "          rdf:object ?target2 . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE { \n";
    queryString += "  SELECT DISTINCT ?slot ?source1 ?property1 ?target1 ?source2 ?property2 ?target2 WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot . \n";
    queryString += "        ?slot rdf:type \"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ConfirmationRequest\"^^xsd:anyURI . \n";
    queryString += "    } \n";

    switch (code.toLowerCase()) {
      case "gr":
        queryString += "BIND(welcome:AgentNGO as ?source1) \n";
        queryString += "BIND(welcome:agentName as ?property1) \n";
        queryString += "BIND(\"PRAKSIS\" as ?target1) \n";

        queryString += "BIND(welcome:AgentNGO as ?source2) \n";
        queryString += "BIND(actor:webURL as ?property2) \n";
        queryString += "BIND(\"https://praksis.gr/contact/\" as ?target2) \n";
        break;
      case "de":
        queryString += "BIND(welcome:AgentNGO as ?source1) \n";
        queryString += "BIND(welcome:agentName as ?property1) \n";
        queryString += "BIND(\"CARITAS\" as ?target1) \n";

        queryString += "BIND(welcome:AgentNGO as ?source2) \n";
        queryString += "BIND(actor:webURL as ?property2) \n";
        queryString += "BIND(\"https://www.caritas-hamm.de/einrichtungen/beratungszentrum/beratungszentrum\" as ?target2) \n";
        break;
      case "es":
        queryString += "BIND(welcome:AgentNGO as ?source1) \n";
        queryString += "BIND(welcome:agentName as ?property1) \n";
        queryString += "BIND(\"DIFE\" as ?target1) \n";

        queryString += "BIND(welcome:AgentNGO as ?source2) \n";
        queryString += "BIND(actor:webURL as ?property2) \n";
        queryString += "BIND(\"https://dretssocials.gencat.cat/ca/contacte_dasc/\" as ?target2) \n";
        break;
    }

    queryString += "  } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }


  public void updatedSystemInfo(Resource graph) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += " \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot . \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "    } \n";
    queryString += "	?p dcterms:isRequiredBy ?o . \n";
    queryString += "    BIND(STRBEFORE(STR(?o), \":\") as ?temp1)  \n";
    queryString += "    BIND(STRAFTER(STR(?o), \":\") as ?temp2)  \n";
    queryString += "    BIND(STRAFTER(STR(?slot), \"#\") as ?temp3)  \n";
    queryString += "    BIND(STRAFTER(STR(?s), \"#\") as ?temp4)  \n";
    queryString += "    FILTER((STRAFTER(STR(?slot), \"#\") = ?o || (?temp4 = ?temp1 && ?temp3 = ?temp2)) && ?contents = welcome:Unknown) \n";
    queryString += "    FILTER NOT EXISTS {?p dcterms:component \"NLG\"} \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += " \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents [ \n";
    queryString += "          a rdf:Statement; \n";
    queryString += "          rdf:subject ?source ; \n";
    queryString += "          rdf:predicate ?property ; \n";
    queryString += "          rdf:object ?target \n";
    queryString += "        ]. \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE { \n";
    queryString += "SELECT DISTINCT ?slot ?source ?property ?target WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot . \n";
    queryString += "    } \n";
    queryString += "	  ?p dcterms:isRequiredBy ?o . \n";
    queryString += "    ?p owl:annotatedSource ?source . \n";
    queryString += "    ?p owl:annotatedProperty ?property . \n";
    queryString += "    ?p owl:annotatedTarget ?target . \n";
    queryString += "    OPTIONAL { \n";
    queryString += "    	?p welcome:dependsOn ?d . \n";
    queryString += "    	?p welcome:conditionValue ?v . \n";
    queryString += "        BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?d)) as ?IRI) \n";
    queryString += "        ?x rdf:type ?IRI . \n";
    queryString += "        ?x welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    OPTIONAL { \n";
    queryString += "    	?p welcome:secondaryDependency ?d1 . \n";
    queryString += "    	?p welcome:secondaryCondition ?v1 . \n";
    queryString += "        BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?d1)) as ?IRI1) \n";
    queryString += "        ?x1 rdf:type ?IRI1 . \n";
    queryString += "        ?x1 welcome:hasValue ?value1 . \n";
    queryString += "    } \n";
    queryString += "    OPTIONAL { \n";
    queryString += "    	?p welcome:thirdDependency ?d2 . \n";
    queryString += "    	?p welcome:thirdCondition ?v2 . \n";
    queryString += "        BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?d2)) as ?IRI2) \n";
    queryString += "        ?x2 rdf:type ?IRI2 . \n";
    queryString += "        ?x2 welcome:hasValue ?value2 . \n";
    queryString += "    } \n";
    queryString += "    BIND(STRBEFORE(STR(?o), \":\") as ?temp1) \n";
    queryString += "    BIND(STRAFTER(STR(?o), \":\") as ?temp2) \n";
    queryString += "    BIND(STRAFTER(STR(?slot), \"#\") as ?temp3) \n";
    queryString += "    BIND(STRAFTER(STR(?s), \"#\") as ?temp4) \n";
    queryString += "    BIND(EXISTS {?p welcome:dependsOn ?d} as ?conditionCheck) \n";
    queryString += "    BIND(EXISTS {?p welcome:secondaryDependency ?d1} as ?conditionCheck1) \n";
    queryString += "    BIND(EXISTS {?p welcome:thirdDependency ?d2} as ?conditionCheck2) \n";
    queryString += "    BIND(IF(?conditionCheck = true, ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2)) && ?v = ?value , ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2))) as ?final) \n";
    queryString += "    BIND(IF(?conditionCheck1 = true, ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2)) && ?v1 = ?value1 , ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2))) as ?final1) \n";
    queryString += "    BIND(IF(?conditionCheck2 = true, ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2)) && ?v2 = ?value2 , ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2))) as ?final2) \n";
    queryString += "    FILTER((?final && ?final1 && ?final2) "
        + "|| (?final && (?conditionCheck1 = false) && (?conditionCheck2 = false)) "
        + "|| (?final && ?final1 && (?conditionCheck2 = false))) \n";
    queryString += "} \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updatedSystemInfoV2(Resource graph) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += " \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "    	  ?contents a rdf:Statement ; \n";
    queryString += "    	         rdf:subject ?subjectX ; \n";
    queryString += "    	         rdf:predicate ?predicateX ; \n";
    queryString += "    	         rdf:object ?objectX . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot . \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	      ?contents a rdf:Statement ; \n";
    queryString += "    	             rdf:subject ?subjectX ; \n";
    queryString += "    	             rdf:predicate ?predicateX ; \n";
    queryString += "    	             rdf:object ?objectX . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "	?p dcterms:isRequiredBy ?o . \n";
    queryString += "    BIND(STRBEFORE(STR(?o), \":\") as ?temp1)  \n";
    queryString += "    BIND(STRAFTER(STR(?o), \":\") as ?temp2)  \n";
    queryString += "    BIND(STRAFTER(STR(?slot), \"#\") as ?temp3)  \n";
    queryString += "    BIND(STRAFTER(STR(?s), \"#\") as ?temp4)  \n";
    queryString += "    FILTER((STRAFTER(STR(?slot), \"#\") = ?o || (?temp4 = ?temp1 && ?temp3 = ?temp2))) \n";
    queryString += "    FILTER NOT EXISTS {?p dcterms:component \"NLG\"} \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += " \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents [ \n";
    queryString += "          a rdf:Statement; \n";
    queryString += "          rdf:subject ?source ; \n";
    queryString += "          rdf:predicate ?property ; \n";
    queryString += "          rdf:object ?target \n";
    queryString += "        ]. \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE { \n";
    queryString += "SELECT DISTINCT ?slot ?source ?property ?target WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasSlot ?slot . \n";
    queryString += "    } \n";
    queryString += "	  ?p dcterms:isRequiredBy ?o . \n";
    queryString += "    ?p owl:annotatedSource ?source . \n";
    queryString += "    ?p owl:annotatedProperty ?property . \n";
    queryString += "    ?p owl:annotatedTarget ?target . \n";
    queryString += "    OPTIONAL { \n";
    queryString += "    	?p welcome:dependsOn ?d . \n";
    queryString += "    	?p welcome:conditionValue ?v . \n";
    queryString += "        BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?d)) as ?IRI) \n";
    queryString += "        ?x rdf:type ?IRI . \n";
    queryString += "        ?x welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    OPTIONAL { \n";
    queryString += "    	?p welcome:secondaryDependency ?d1 . \n";
    queryString += "    	?p welcome:secondaryCondition ?v1 . \n";
    queryString += "        BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?d1)) as ?IRI1) \n";
    queryString += "        ?x1 rdf:type ?IRI1 . \n";
    queryString += "        ?x1 welcome:hasValue ?value1 . \n";
    queryString += "    } \n";
    queryString += "    OPTIONAL { \n";
    queryString += "    	?p welcome:thirdDependency ?d2 . \n";
    queryString += "    	?p welcome:thirdCondition ?v2 . \n";
    queryString += "        BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?d2)) as ?IRI2) \n";
    queryString += "        ?x2 rdf:type ?IRI2 . \n";
    queryString += "        ?x2 welcome:hasValue ?value2 . \n";
    queryString += "    } \n";
    queryString += "    BIND(STRBEFORE(STR(?o), \":\") as ?temp1) \n";
    queryString += "    BIND(STRAFTER(STR(?o), \":\") as ?temp2) \n";
    queryString += "    BIND(STRAFTER(STR(?slot), \"#\") as ?temp3) \n";
    queryString += "    BIND(STRAFTER(STR(?s), \"#\") as ?temp4) \n";
    queryString += "    BIND(EXISTS {?p welcome:dependsOn ?d} as ?conditionCheck) \n";
    queryString += "    BIND(EXISTS {?p welcome:secondaryDependency ?d1} as ?conditionCheck1) \n";
    queryString += "    BIND(EXISTS {?p welcome:thirdDependency ?d2} as ?conditionCheck2) \n";
    queryString += "    BIND(IF(?conditionCheck = true, ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2)) && ?v = ?value , ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2))) as ?final) \n";
    queryString += "    BIND(IF(?conditionCheck1 = true, ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2)) && ?v1 = ?value1 , ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2))) as ?final1) \n";
    queryString += "    BIND(IF(?conditionCheck2 = true, ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2)) && ?v2 = ?value2 , ((STRAFTER(STR(?slot), \"#\") = ?o) "
        + "|| (?temp4 = ?temp1 && ?temp3 = ?temp2))) as ?final2) \n";
    queryString += "    FILTER((?final && ?final1 && ?final2) "
        + "|| (?final && (?conditionCheck1 = false) && (?conditionCheck2 = false)) "
        + "|| (?final && ?final1 && (?conditionCheck2 = false))) \n";
    queryString += "} \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateProposeTimeSlot(Resource graph, boolean date, String anchor) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX schema: <https://schema.org/> \n";
    queryString += "select ?dow where {  \n";
    queryString += "	?p dcterms:isRequiredBy ?o . \n";
    queryString += "    ?p schema:startTime ?timeSlot . \n";
    queryString += "    ?p schema:scheduledTime ?st . \n";
    queryString += "    ?p schema:actionStatus ?status . \n";
    queryString += "    ?p welcome:slotOrder ?order . \n";
    queryString += "    ?st welcome:dayOfWeekNoRepeat ?dow . \n";
    queryString += "    ?status welcome:hasValue ?value . \n";
    queryString += "    FILTER(CONTAINS(LCASE(STR(?o)), \"proposetimeslot\") && ?value = \"Free\") \n";
    queryString += "} ORDER BY ?order LIMIT 1 \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    String dow = null;
    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      if (result.hasNext()) {
        BindingSet solution = result.next();

        dow = solution.getBinding("dow").getValue().stringValue();
      }
    }

    /* Calculate the date of dow. For example if dow = Monday
    we need to calculate the date of the next week's Monday. */
    LocalDate dt = LocalDate.now();
    dt = dt.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

    LocalDate finalDate = null;
    switch (dow.toUpperCase()) {
      case "MONDAY":
        finalDate = dt.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        break;
      case "TUESDAY":
        finalDate = dt.with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
        break;
      case "WEDNESDAY":
        finalDate = dt.with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY));
        break;
      case "THURSDAY":
        finalDate = dt.with(TemporalAdjusters.next(DayOfWeek.THURSDAY));
        break;
      case "FRIDAY":
        finalDate = dt.with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        break;
      case "SATURDAY":
        finalDate = dt.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
        break;
    }

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += "PREFIX schema: <https://schema.org/> \n";
    queryString += " \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "        ?contents rdf:type rdf:Statement ; \n";
    queryString += "                  rdf:subject ?sub ; \n";
    queryString += "                  rdf:predicate ?pred ; \n";
    queryString += "                  rdf:object ?obj . \n";
    queryString += "     \n";
    queryString += "    } \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    queryString += "        welcome:tempTriple welcome:pointsTo ?sa . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE {  \n";
    queryString += "    SELECT ?slot ?contents ?sub ?pred ?obj ?sa WHERE { \n";
    queryString += "    	GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        	?s welcome:hasSlot ?slot . \n";
    queryString += "            ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "            OPTIONAL { \n";
    queryString += "            	?contents rdf:type rdf:Statement ; \n";
    queryString += "                  	rdf:subject ?sub ; \n";
    queryString += "                  	rdf:predicate ?pred ; \n";
    queryString += "                  	rdf:object ?obj . \n";
    queryString += "            }     \n";
    queryString += "    	} \n";
    queryString += "      GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    queryString += "          OPTIONAL { \n";
    queryString += "              welcome:tempTriple welcome:pointsTo ?sa . \n";
    queryString += "          } \n";
    queryString += "      } \n";
    queryString += "      FILTER(CONTAINS(LCASE(STR(?slot)), \"proposetimeslot\")) \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n";
    queryString += "PREFIX schema: <https://schema.org/> \n";
    queryString += " \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    queryString += "    	welcome:tcn_user welcome:hasAppointmentTime \n";
    queryString += "        [   a welcome:AppointmentTime ; \n";
    queryString += "        	welcome:hasValue ?timeSlot \n";
    queryString += "        ]. \n";
    queryString += "    	welcome:tcn_user welcome:hasAppointmentDay \n";
    queryString += "        [   a welcome:AppointmentDay ; \n";
    queryString += "        	welcome:hasValue ?dow \n";
    queryString += "        ]. \n";
    queryString += "    	welcome:tcn_user welcome:hasAppointmentTimeEarlier \n";
    queryString += "        [   a welcome:AppointmentTimeEarlier ; \n";
    queryString += "        	welcome:hasValue ?early \n";
    queryString += "        ]. \n";
    queryString += "    	welcome:tcn_user welcome:hasAppointmentDate \n";
    queryString += "        [   a welcome:AppointmentDate ; \n";
    queryString += "        	welcome:hasValue \"" + finalDate + "\" \n";
    queryString += "        ]. \n";
    queryString += "      welcome:tempTriple welcome:pointsTo ?p . \n";
    queryString += "    } \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents [ \n";
    queryString += "          a rdf:Statement ; \n";
    queryString += "          rdf:subject welcome:AppointmentTime ; \n";
    queryString += "          rdf:predicate welcome:hasValue ; \n";
    queryString += "          rdf:object ?timeSlot  \n";
    queryString += "        ]. \n";
    queryString += "        ?slot welcome:hasInputRDFContents [ \n";
    queryString += "          a rdf:Statement ; \n";
    queryString += "          rdf:subject welcome:AppointmentDay ; \n";
    queryString += "          rdf:predicate welcome:hasValue ; \n";
    queryString += "          rdf:object ?dow  \n";
    queryString += "        ]. \n";
    queryString += "        ?slot welcome:hasInputRDFContents [ \n";
    queryString += "          a rdf:Statement ; \n";
    queryString += "          rdf:subject welcome:AppointmentDate ; \n";
    queryString += "          rdf:predicate welcome:hasValue ; \n";
    queryString += "          rdf:object \"" + finalDate + "\" \n";
    queryString += "        ]. \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "WHERE {  \n";
    queryString += "    SELECT ?slot ?p ?timeSlot ?dow ?contents ?sub ?pred ?obj ?early WHERE { \n";
    queryString += "    	GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        	?s welcome:hasSlot ?slot . \n";
    queryString += "    	} \n";
    queryString += "		?p dcterms:isRequiredBy ?o . \n";
    queryString += "    	?p schema:startTime ?timeSlot . \n";
    queryString += "    	?p schema:scheduledTime ?st . \n";
    queryString += "    	?p schema:actionStatus ?status . \n";
    queryString += "    	?p welcome:slotOrder ?order . \n";
    queryString += "    	?p welcome:startTimeEarly ?early . \n";
    queryString += "    	?st welcome:dayOfWeekNoRepeat ?dow . \n";
    queryString += "    	?status welcome:hasValue ?value . \n";
    if (date) {
      queryString +=
          "    	FILTER(STRAFTER(STR(?slot), \"#\") = ?o && ?value = \"Free\" && LCASE(?dow) = \""
              + anchor + "\") \n";
    } else {
      queryString += "    	FILTER(STRAFTER(STR(?slot), \"#\") = ?o && ?value = \"Free\") \n";
    }
    queryString += "    } \n";
    queryString += "    ORDER BY ?order \n";
    queryString += "    LIMIT 1 \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateTimeSlotStatus(Resource graph) {
    String queryString;

    queryString = "PREFIX schema: <https://schema.org/> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString += "    ?sa schema:actionStatus ?old_status . \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    ?sa schema:actionStatus welcome:Status_Rejected . \n";
    queryString += "}  \n";
    queryString += "WHERE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    queryString += "        welcome:tempTriple welcome:pointsTo ?sa . \n";
    queryString += "    } \n";
    queryString += "    ?sa schema:actionStatus ?old_status . \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Returns the name of the active dip.
   *
   * @param activeDIP
   * @return
   */
  public IRI getActiveDIPName(Resource activeDIP) {
    String queryString;

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?dip WHERE { \n";
    queryString += "    GRAPH <" + activeDIP.stringValue() + "> { \n";
    queryString += "        ?dip welcome:hasSlot ?slot . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    IRI dipName = null;
    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value slot = solution.getBinding("dip").getValue();
        dipName = Utilities.f.createIRI(slot.stringValue());
      }
    }

    return dipName;
  }

  /**
   * Increments number of attempts for the give slot.
   *
   * @param dipGraph
   * @param dipSlot
   */
  public void incrementNumberOfAttempts_I(Resource dipGraph, String dipSlot) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasNumberAttempts ?attempts . \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasNumberAttempts ?updated_attempts . \n";
    queryString += "    	?slot welcome:hasStatus ?updated_status . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasNumberAttempts ?attempts . \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:isOptional ?optional . \n";
    queryString += "        BIND((?attempts + 1) AS ?updated_attempts) \n";
    queryString += "        BIND(IF(?updated_attempts >= 3, "
        + "IF(?optional = false || ?optional = \"no\", welcome:Undefined, welcome:Unfinished), ?status) "
        + "as ?updated_status) \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + dipSlot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "    	?content a rdf:Statement ; \n";
    queryString += "    	         rdf:subject ?subjectX ; \n";
    queryString += "    	         rdf:predicate ?predicateX ; \n";
    queryString += "    	         rdf:object ?objectX . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer \n";
    queryString += "        [ a rdf:Statement; \n";
    queryString += "       		rdf:subject ?subject; \n";
    queryString += "       		rdf:predicate welcome:hasValue; \n";
    queryString += "          rdf:object welcome:Unknown] . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	    ?content a rdf:Statement ; \n";
    queryString += "    	             rdf:subject ?subjectX ; \n";
    queryString += "    	             rdf:predicate ?predicateX ; \n";
    queryString += "    	             rdf:object ?objectX . \n";
    queryString += "        } \n";
    queryString += "        ?slot welcome:hasOntologyType ?subject . \n";
    queryString += "    	  ?slot welcome:hasNumberAttempts 3 . \n";
    queryString += "    } \n";
    queryString += "    OPTIONAL { \n";
    queryString += "        ?target welcome:requiresValue true . \n";
    queryString += "    } \n";
    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = STRAFTER(STR(?target), \"_\")) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void incrementNumberOfAttemptsConfirmation_I(Resource dipGraph, String dipSlot) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasNumberAttempts ?attempts . \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasNumberAttempts ?updated_attempts . \n";
    queryString += "    	?slot welcome:hasStatus ?updated_status . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasNumberAttempts ?attempts . \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        BIND((?attempts + 1) AS ?updated_attempts) \n";
    queryString += "        BIND(IF(?updated_attempts >= 2, welcome:Undefined, ?status) as ?updated_status) \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + dipSlot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  /**
   * Creates a temp graph by inserting 2 triples for the Agent-Core, the ID and the context of the
   * DIP.
   *
   * @param activeDIP
   */
  public void createTempGraph_I(Resource activeDIP) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH welcome:tempDIP { \n";
    queryString += "        ?s welcome:DIPId ?id . \n";
    queryString += "        ?s welcome:hasNamedGraph ?g . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH ?g { \n";
    queryString += "        ?s welcome:DIPId ?id . \n";
    queryString += "    } \n";
    queryString += "    FILTER(?g = IRI(<" + activeDIP.stringValue() + ">)) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void deleteProfileInfo_D(String field, Integer sim) {
    String queryString = "";

    if (sim == 1) {
      queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
      queryString += "        ?s welcome:has" + field + " ?o . \n";
      queryString += "        ?o rdf:type ?type . \n";
      queryString += "        ?o welcome:hasValue ?value . \n";
      queryString += "        ?o ?anyPredicate ?anyValue . \n";
      queryString += "    } \n";
      queryString += "} \n";
    } else if (sim == 0) {
      queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
      queryString += "        ?s welcome:has" + field + " ?o . \n";
      queryString += "        ?o rdf:type ?type . \n";
      queryString += "        ?o welcome:hasValue ?value . \n";
      queryString += "        ?o ?anyPredicate ?anyValue . \n";
      queryString += "    } \n";
      queryString += "} \n";
    } else if (sim == 2) {
      queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
      queryString += "        ?s welcome:has" + field + " ?o . \n";
      queryString += "        ?o rdf:type ?type . \n";
      queryString += "        ?o welcome:hasValue ?value . \n";
      queryString += "        ?o ?anyPredicate ?anyValue . \n";
      queryString += "    } \n";
      queryString += "} \n";
    } else if (sim == 3) {
      queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "DELETE WHERE { \n";
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#tempGraph> { \n";
      queryString += "        ?s welcome:has" + field + " ?o . \n";
      queryString += "        ?o rdf:type ?type . \n";
      queryString += "        ?o welcome:hasValue ?value . \n";
      queryString += "        ?o ?anyPredicate ?anyValue . \n";
      queryString += "    } \n";
      queryString += "} \n";
    }

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void deleteCoords() {
    String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>  \n";
    queryString += "DELETE WHERE {  \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> {  \n";
    queryString += "        ?s welcome:lat ?lat . \n";
    queryString += "        ?s welcome:long ?long . \n";
    queryString += "    }  \n";
    queryString += "}  \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateAllSlotStatus(IRI dipGraph, String status) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus ?newStatus . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    ?dip welcome:hasSlot ?slot . \n";
    queryString += "    GRAPH <" + dipGraph.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    } \n";
    queryString +=
        "    BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", \""
            + status + "\")) as ?newStatus) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updatedActiveSlotStatus_I(String slotName) {
    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH welcome:SlotInfo { \n";
    queryString += "    	?slot welcome:isActiveSlot ?status . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH welcome:SlotInfo { \n";
    queryString += "    	?slot welcome:isActiveSlot false . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH welcome:SlotInfo { \n";
    queryString += "        ?slot welcome:isActiveSlot ?status . \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + slotName.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public double[] checkExistingLocations_S(String anchor) {
    double[] coords = {0.0, 0.0};

    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select ?value ?lat ?long where { \n";
    queryString += "    ?s rdf:type welcome:Location . \n";
    queryString += "    ?s welcome:lat ?lat . \n";
    queryString += "    ?s welcome:long ?long . \n";
    queryString += "    ?s welcome:hasValue ?value . \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        String value = solution.getBinding("value").getValue().stringValue();
        double lat = Double.parseDouble(solution.getBinding("lat").getValue().stringValue());
        double lng = Double.parseDouble(solution.getBinding("long").getValue().stringValue());

        if ((value.toLowerCase()).contentEquals(anchor.toLowerCase())) {
          coords[0] = lat;
          coords[1] = lng;

          return coords;
        }
      }
    }

    return coords;

  }

  public String getVRInfo_S(String param) {
    String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
        + "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\r\n"
        + "select ?o {\r\n" + "    ?s rdf:type welcome:" + param + " .\r\n"
        + "    ?s welcome:hasSaveData ?o .\r\n" + "}";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    String progress = "";
    try (TupleQueryResult result = query.evaluate()) {
      while (result.hasNext()) {
        BindingSet solution = result.next();
        progress = solution.getValue("o").toString();
      }
    }

    return progress;
  }

  public String getMinigameInfo_S() {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select ?version where { \n";
    queryString += "	?minigame rdf:type welcome:Minigame ; \n";
    queryString += "              welcome:versionJsonNumber ?version . \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    JSONObject miniGameObj = new JSONObject();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();
        Value version = solution.getBinding("version").getValue();
        /* Put class name and value into the JSON object */
        miniGameObj.put("versionJsonNumber", version.stringValue());
      }
    }

    JSONArray scenarios = new JSONArray();

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select DISTINCT ?id ?locked where { \n";
    queryString += "    ?scenario rdf:type welcome:Scenario . \n";
    queryString += "    ?scenario welcome:scenarioID ?id . \n";
    queryString += "    ?scenario welcome:locked ?locked . \n";

    queryString += "} \n";

    query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        JSONObject scenarioObj = new JSONObject();

        BindingSet solution = result.next();
        Value id = solution.getBinding("id").getValue();
        Value locked = solution.getBinding("locked").getValue();

        scenarioObj.put("scenarioID", id.stringValue());
        scenarioObj.put("locked", Boolean.parseBoolean(locked.stringValue()));

        queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
        queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
        queryString += "select DISTINCT ?id ?score ?wrong ?time where { \n";
        queryString += "    ?scenario welcome:hasStage ?stage ; \n";
        queryString += "              welcome:scenarioID ?scID . \n";
        queryString += "    ?stage welcome:stage ?id ; \n";
        queryString += "           welcome:score ?score ; \n";
        queryString += "           welcome:wrongAnswer ?wrong ; \n";
        queryString += "           welcome:time ?time . \n";
        queryString += "    FILTER(?scID = \"" + id.stringValue() + "\") \n";
        queryString += "} \n";

        query = Utilities.connection.prepareTupleQuery(queryString);

        JSONArray data = new JSONArray();

        try (TupleQueryResult result_ = query.evaluate()) {
          while (result_.hasNext()) {
            JSONObject stageObj = new JSONObject();

            solution = result_.next();

            String stID = solution.getBinding("id").getValue().stringValue();
            String score = solution.getBinding("score").getValue().stringValue();
            String wrong = solution.getBinding("wrong").getValue().stringValue();
            String time = solution.getBinding("time").getValue().stringValue();

            stageObj.put("score", Long.parseLong(score));
            stageObj.put("stage", Long.parseLong(stID));
            stageObj.put("wrongAnswer", Long.parseLong(wrong));
            stageObj.put("time", Long.parseLong(time));

            data.add(stageObj);
          }
        }

        scenarioObj.put("data", data);

        scenarios.add(scenarioObj);
      }
    }

    miniGameObj.put("scenarios", scenarios);

    return miniGameObj.toString();
  }

  public String getCVElementValue(IRI cName, boolean flag) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select DISTINCT ?value where { \n";
    if (flag) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    } else {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#tempGraph> { \n";
    }
    queryString += "        ?elem rdf:type ?class . \n";
    queryString += "        ?elem welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?class = IRI(<" + cName.stringValue() + ">)) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();
        Value v = solution.getBinding("value").getValue();
        return v.stringValue();
      } else {
        return "";
      }
    }
  }

  public String getProfileValue(IRI cName, Integer c) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select DISTINCT ?value where { \n";
    if (c == 0) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    } else if (c == 1) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    } else if (c == 2) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    } else if (c == 3) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#tempGraph> { \n";
    }
    queryString += "		welcome:tcn_user ?pred ?obj . \n";
    queryString += "        ?obj rdf:type ?class . \n";
    queryString += "        ?obj welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?class = IRI(<" + cName.stringValue() + ">)) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();
        Value v = solution.getBinding("value").getValue();
        return v.stringValue();
      } else {
        return "";
      }
    }
  }

  public JSONObject getTCNProfile_S(String section) {
    JSONObject obj = new JSONObject();
    boolean skipFalse;

    if (section != null) {
      switch (section.toLowerCase()) {
        case "personal":
          obj = (JSONObject) getTCNCVSection(section, SlotLists.personalSlotsFull, false);
          break;
        case "education":
          obj = (JSONObject) getTCNEducationSection(false);
          break;
        case "course":
          obj = (JSONObject) getTCNCourseSection(false);
          break;
        case "language":
          obj = (JSONObject) getTCNLanguageSection(false);
          break;
        case "employment":
          obj = (JSONObject) getTCNEmploymentSection(false);
          break;
        case "other":
          obj = (JSONObject) getTCNCVSection(section, SlotLists.otherSlotsFull, false);
          break;
        case "skill":
          skipFalse = false;
          obj = (JSONObject) getTCNSkillSection(false, skipFalse);
          break;
        case "fullcv":
          JSONArray obj1 = (JSONArray) getTCNCVSection("personal", SlotLists.personalSlotsFull,
              true);
          JSONArray obj2 = (JSONArray) getTCNEducationSection(true);
          JSONArray obj3 = (JSONArray) getTCNCourseSection(true);
          JSONArray obj4 = (JSONArray) getTCNLanguageSection(true);
          JSONArray obj5 = (JSONArray) getTCNEmploymentSection(true);
          JSONArray obj6 = (JSONArray) getTCNCVSection("other", SlotLists.otherSlotsFull, true);

          skipFalse = true;
          JSONArray obj7 = (JSONArray) getTCNSkillSection(true, skipFalse);

          JSONObject list = new JSONObject();
          list.put("personal", obj1);
          list.put("education", obj2);
          list.put("course", obj3);
          list.put("language", obj4);
          list.put("employment", obj5);
          list.put("other", obj6);
          list.put("skills", obj7);

          /* Create the final object */
          JSONObject fullObject = new JSONObject();
          fullObject.put("data", list);

          return fullObject;
      }
    } else {
      obj = getTCNGeneralProfile();
    }

    return obj;
  }

  public Object getTCNCVSection(String s, Object section, boolean flag) {
    String values = String.valueOf(section).replace("[", "(").replace("]", ")");

    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select DISTINCT ?class ?value where { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += "		welcome:tcn_user welcome:hasCV welcome:CV . \n";
    queryString += "        welcome:CV welcome:hasElem ?elem . \n";
    queryString += "        ?obj rdf:type ?class . \n";
    queryString += "        ?obj welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?class in " + values + ") \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    JSONObject obj = new JSONObject();
    List<String> list = new ArrayList<>();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        /* Get the IRI of the class */
        Value c = solution.getBinding("class").getValue();
        IRI temp = Utilities.f.createIRI(c.stringValue());

        /* Split the IRI and get the class Name */
        String classLabel = util.splitIRI(temp);

        /* Keep track of the populated classes */
        list.add("welcome:" + classLabel);

        /* Get the actual value for the class */
        Value value = solution.getBinding("value").getValue();

        /* In case of slots supporting negation replace No with "" */
        String updatedValue = value.stringValue();

        if (s.contentEquals("personal")) {
          if (updatedValue.contentEquals("No")) {
            updatedValue = "";
          }
        }

        /* Put class name and value into the JSON object */
        obj.put(classLabel, updatedValue);
      }
    }

    /* Create a new list that holds all the class names */
    List<String> c = new ArrayList<>((List) section);

    /* Remove the class names that are populated in the lAKR */
    c.removeAll(list);

        /* For the remaining non-populated classes put an
           empty string in the JSON output
        */
    for (String temp : c) {
      String[] arrOfStr = temp.split(":");
      obj.put(arrOfStr[1], "");
    }

    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    array.add(obj);
    jsonObject.put(s, array);

    if (flag) {
      return array;
    } else {
      /* Create the final object */
      JSONObject fullObject = new JSONObject();
      fullObject.put("data", jsonObject);

      return fullObject;
    }
  }

  public JSONObject getTCNGeneralProfile() {
    String values = String.valueOf(SlotLists.appForm).replace("[", "(").replace("]", ")");

    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select DISTINCT ?class ?value where { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "		welcome:tcn_user ?pred ?obj . \n";
    queryString += "        ?obj rdf:type ?class . \n";
    queryString += "        ?obj welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?class in " + values + ") \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    JSONObject obj = new JSONObject();
    List<String> list = new ArrayList<>();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        /* Get the IRI of the class */
        Value c = solution.getBinding("class").getValue();
        IRI temp = Utilities.f.createIRI(c.stringValue());

        /* Split the IRI and get the class Name */
        String classLabel = util.splitIRI(temp);

        /* Keep track of the populated classes */
        list.add("welcome:" + classLabel);

        /* Get the actual value for the class */
        Value value = solution.getBinding("value").getValue();

        /* Put class name and value into the JSON object */
        obj.put(classLabel, value.stringValue());
      }
    }

    /* Create a new list that holds all the class names */
    List<String> c = new ArrayList<>(SlotLists.appForm);

    /* Remove the class names that are populated in the lAKR */
    c.removeAll(list);

        /* For the remaining non-populated classes put an
           empty string in the JSON output
        */
    for (String temp : c) {
      String[] arrOfStr = temp.split(":");
      obj.put(arrOfStr[1], "");
    }

    List<String> langList = getOtherLanguageList();
    JSONArray codeList = new JSONArray();
    if (langList.size() > 0) {
      for (int i = 0; i < langList.size(); i++) {
        String code = langList.get(i);
        codeList.add(code);
      }
      obj.put("OtherLanguageCode", codeList);
    } else {
      obj.put("OtherLanguageCode", codeList);
    }

    return obj;
  }

  public JSONObject getAvatarConfig_S() {

    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select DISTINCT ?obj ?value where { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#AvatarConfig> { \n";
    queryString += "		welcome:Avatar ?pred ?obj . \n";
    queryString += "        ?obj welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    JSONObject obj = new JSONObject();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        /* Get the IRI of the class */
        Value o = solution.getBinding("obj").getValue();
        IRI temp = Utilities.f.createIRI(o.stringValue());

        /* Split the IRI and get the class Name */
        String oLabel = util.splitIRI(temp);

        /* Get the actual value for the class */
        String r = (solution.getBinding("value").getValue()).stringValue();

        if (oLabel.contentEquals("Gender")) {
          long value = Long.parseLong(r);
          obj.put(oLabel, value);
        } else {
          obj.put(oLabel, r);
        }
      }
    }

    return obj;
  }

  public boolean countPendingTopics(String scenario) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>  \n";
    queryString += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
    queryString += "ask {  \n";
    queryString += "    {  \n";
    queryString += "        select (count(distinct(?flag)) as ?count){  \n";

    if (scenario.toLowerCase().contains("health")) {
      queryString += "    graph welcome:HealthTopics { \n";
    } else {
      queryString += "    graph welcome:SchoolingTopics { \n";
    }
    queryString += "                ?s rdf:type ?type . \n";
    queryString += "        		?s welcome:hasValue \"Pending\" .  \n";
    queryString += "     	 	} \n";
    queryString += "    		?topic welcome:thirdDependency ?flag . \n";
    queryString += "    		?topic welcome:secondaryCondition ?scenario . \n";
    queryString += "    		BIND(STRAFTER(STR(?type), \"#\") as ?typeSplit) \n";
    queryString += "    		FILTER(?typeSplit = ?flag && ?scenario = \"" + scenario + "\" ) \n";
    queryString += "    	}  \n";
    queryString += "  	}  \n";
    queryString += "    filter (?count = 1)  \n";
    queryString += "}  \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }

  public String getSingleTopic(String scenarioName) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "select ?topic where { \n";

    if (scenarioName.toLowerCase().contains("health")) {
      queryString += "    graph welcome:HealthTopics { \n";
    } else {
      queryString += "    graph welcome:SchoolingTopics { \n";
    }

    queryString += "    	?s rdf:type ?type . \n";
    queryString += "		?s welcome:hasValue \"Pending\" . \n";
    queryString += "    } \n";
    queryString += "    BIND(STRAFTER(STR(?type), \"#\") as ?topic) \n";
    queryString += "    ?topic1 welcome:thirdDependency ?flag . \n";
    queryString += "    ?topic1 welcome:secondaryCondition ?scenario . \n";
    queryString += "    BIND(STRAFTER(STR(?type), \"#\") as ?typeSplit) \n";
    queryString += "    FILTER(?typeSplit = ?flag && ?scenario = \"" + scenarioName + "\" ) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    Value o = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        /* Get the IRI of the class */
        o = solution.getBinding("topic").getValue();
      }
    }

    return o.stringValue();
  }

  public ModelBuilder updateTopicStatus(ModelBuilder builder, String anchor, String status,
      String scenarioName) {
    BNode iri = Utilities.f.createBNode();

    WPM wpm = new WPM();

    IRI userIRI = wpm.getUser();

    deleteTopicInfo(anchor, scenarioName);

    if (scenarioName.contains("schooling")) {
      /* Add fields to builder */
      builder.namedGraph("welcome:SchoolingTopics");
    } else {
      /* Add fields to builder */
      builder.namedGraph("welcome:HealthTopics");
    }

    builder.subject(userIRI)
        .add("welcome:has" + anchor, iri)
        .subject(iri)
        .add(iri, RDF.TYPE, "welcome:" + anchor)
        .add("welcome:hasValue", status);

    return builder;
  }

  public void deleteTopicInfo(String value, String scenarioName) {
    String queryString;

//    /* Add fields to builder */
//    builder.namedGraph("welcome:SchoolingTopics")
//        .subject(userIRI)
//        .add("welcome:has" + anchor, iri)
//        .subject(iri)
//        .add(iri, RDF.TYPE, "welcome:" + anchor)
//        .add("welcome:hasValue", "Pending");

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE WHERE { \n";
    if (scenarioName.toLowerCase().contains("schooling")) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#SchoolingTopics> { \n";
    } else {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#HealthTopics> { \n";
    }
    queryString += "        ?s welcome:has" + value + " ?o . \n";
    queryString += "        ?o rdf:type welcome:" + value + " . \n";
    queryString += "        ?o welcome:hasValue ?value . \n";
    queryString += "        ?o ?anyPredicate ?anyValue . \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void deleteOtherLanguageCode(String property) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += " \n";
    queryString += "DELETE { \n";
    queryString += "    welcome:tcn_user welcome:" + property + " ?list . \n";
    queryString += "    ?list rdf:type rdf:List . \n";
    queryString += "    ?z rdf:first ?head ; rdf:rest ?tail .  \n";
    queryString += "} WHERE {  \n";
    queryString += "      [] welcome:" + property + " ?list . \n";
    queryString += "      ?list rdf:rest* ?z . \n";
    queryString += "      ?z rdf:first ?head ; \n";
    queryString += "         rdf:rest ?tail . \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public ModelBuilder
  updateProfile(ModelBuilder builder, IRI ontoType, String anchor, Integer sim) {
    /* Create IRI for the field */
    BNode iri = Utilities.f.createBNode();

    WPM wpm = new WPM();

    /* Get user's IRI */
    IRI userIRI = wpm.getUser();

    /* Split IRI from actual name */
    String predicate = util.splitIRI(ontoType);

    deleteProfileInfo_D(predicate, sim);

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    if (sim == 1) {
      /* Add fields to builder */
      builder.namedGraph("welcome:ProfileInfoSimulation")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);
    } else if (sim == 0) {
      /* Add fields to builder */
      builder.namedGraph("welcome:ProfileInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);
    } else if (sim == 2) {
      /* Add fields to builder */
      builder.namedGraph("welcome:CVInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);
    }

    return builder;
  }

  public String getSlotStatus(IRI activeDIP, IRI slot) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select ?st where { \n";
    queryString += "    GRAPH <" + activeDIP.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "    } \n";
    queryString += "    BIND(IRI(<" + slot.stringValue() + ">) as ?temp) \n";
    queryString += "    FILTER(STRAFTER(STR(?slot), \"#\") = STRAFTER(STR(?temp), \"#\")) \n";
    queryString += "    BIND((STRAFTER((STR(?status)), \"#\") ) as ?st) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    String status = "";
    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      if (result.hasNext()) {
        BindingSet solution = result.next();

        status = solution.getBinding("st").getValue().stringValue();
      }
    }

    return status;
  }

  public void checkHeaders(String corId, String turnId) {

    if (corId != null) {
      MDC.put("correlationId", corId);

      if (!corId.contentEquals(Utilities.CorId)) {
        Utilities.CorId = corId;
        correlationId_I(corId);
      }
    }

    if (turnId != null) {
      MDC.put("turnId", turnId);

      if (!turnId.contentEquals(Utilities.TurnId)) {
        Utilities.TurnId = turnId;
        turnId_I(turnId);
      }
    }
  }

  public void copySimulationPersonal(Object personal) {
    String values = String.valueOf(personal).replace("[", "(").replace("]", ")");

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfoSimulation> { \n";
    queryString += "        ?dialogueUser ?hasProperty ?property . \n";
    queryString += "        ?property rdf:type ?type . \n";
    queryString += "        ?property welcome:hasValue ?value . \n";
    queryString += "        ?property welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?dialogueUser ?hasProperty ?property . \n";
    queryString += "        ?property rdf:type ?type . \n";
    queryString += "        ?property welcome:hasValue ?value . \n";
    queryString += "        ?property welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?type"
        + " in " + values + ") \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateRDFInputContent(IRI context, Object slots, String value, String slot) {
    String values = String.valueOf(slots).replace("[", "(").replace("]", ")");

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasInputRDFContents ?content . \n";
    queryString += "    	?content a rdf:Statement ; \n";
    queryString += "    	         rdf:subject ?subjectX ; \n";
    queryString += "    	         rdf:predicate ?predicateX ; \n";
    queryString += "    	         rdf:object ?objectX . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents [ \n";
    queryString += "          a rdf:Statement; \n";
    queryString += "          rdf:subject welcome:" + value + " ; \n";
    queryString += "          rdf:predicate welcome:hasValue ; \n";
    queryString += "          rdf:object ?target \n";
    queryString += "        ]. \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?content . \n";
    queryString += "        ?slot welcome:hasOntologyType ?type . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	    ?content a rdf:Statement ; \n";
    queryString += "    	             rdf:subject ?subjectX ; \n";
    queryString += "    	             rdf:predicate ?predicateX ; \n";
    queryString += "    	             rdf:object ?objectX . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#tempGraph> { \n";
    queryString += "        ?node rdf:type ?elem . \n";
    queryString += "        ?node welcome:hasValue ?target . \n";
    queryString += "    } \n";
    queryString += "    BIND(STRAFTER((STR(?type)), \"#\") as ?s) \n";
    queryString += "    BIND(STRAFTER((STR(?elem)), \"#\") as ?e) \n";
    queryString += "    BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?s)) as ?final) \n";
    queryString += "    FILTER (STR(?s) in " + values + " && ?e = \"" + slot + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void resetSlotsStatus(IRI context, Object slots, String status) {
    String values = String.valueOf(slots).replace("[", "(").replace("]", ")");

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "    	?content a rdf:Statement ; \n";
    queryString += "    	         rdf:subject ?subjectX ; \n";
    queryString += "    	         rdf:predicate ?predicateX ; \n";
    queryString += "    	         rdf:object ?objectX . \n";
    queryString += "    	?slot welcome:hasStatus ?status . \n";
    queryString += "    	?slot welcome:confidenceScore ?score . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "    	?slot welcome:hasTCNAnswer welcome:Unknown . \n";
    queryString += "    	?slot welcome:hasStatus welcome:" + status + ". \n";
    queryString += "    	?slot welcome:confidenceScore \"0.0\"^^xsd:float .  \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + context.stringValue() + "> { \n";
    queryString += "        ?slot welcome:hasTCNAnswer ?content . \n";
    queryString += "        ?slot welcome:hasStatus ?status . \n";
    queryString += "        ?slot welcome:confidenceScore ?score . \n";
    queryString += "        ?slot welcome:hasOntologyType ?type . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "    	    ?content a rdf:Statement ; \n";
    queryString += "    	             rdf:subject ?subjectX ; \n";
    queryString += "    	             rdf:predicate ?predicateX ; \n";
    queryString += "    	             rdf:object ?objectX . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "    BIND(STRAFTER((STR(?type)), \"#\") as ?s) \n";
    queryString += "    BIND(IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", ?s)) as ?final) \n";
    queryString +=
        "    FILTER (?final in " + values + " && !CONTAINS(STR(?final), \"Boolean\"))  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public HashMap<String, String> checkFollowUpFAQ(String anchor) throws Exception {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX faq: <" + FAQ.NAMESPACE + "> \n";
    queryString += "SELECT ?followUp ?f_id ?f_question ?f_answer where { \n";
    queryString += "	?followUp rdf:type welcome:InternalFAQ . \n";
    queryString += "    ?followUp faq:faqID ?f_id . \n";
    queryString += "    ?followUp faq:faqQuestion ?f_question . \n";
    queryString += "    ?followUp faq:faqAnswer ?f_answer . \n";
    queryString += "    ?s faq:hasFollowUp ?followUp . \n";
    queryString += "    ?s faq:faqID ?s_id .  \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#FAQInfo> { \n";
    queryString += "    	welcome:faq_id welcome:hasValue ?t_id . \n";
    queryString += "    } \n";
    queryString += "    FILTER(STR(?s_id) = ?t_id) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    HashMap<String, String> hmap = new HashMap<>();

    String corpus = "";
    String ids = "";
    String message = "";

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {

        ids = "\"ids\":[ \n";
        corpus = "\"corpus\":[ \n";

        /* we just iterate over all solutions in the result... */
        while (result.hasNext()) {
          BindingSet solution = result.next();

          String answer = solution.getBinding("f_answer").getValue().stringValue();
          String id = solution.getBinding("f_id").getValue().stringValue();

          hmap.put(id, answer);

          if (result.hasNext()) {
            ids += "\"" + id + "\",\n";
            corpus += "\"" + answer + "\",\n";
          } else {
            ids += "\"" + id + "\"\n";
            corpus += "\"" + answer + "\"\n";
          }
        }

        ids.substring(0, ids.length() - 1);
        ids += "], \n";

        corpus.substring(0, corpus.length() - 1);
        corpus += "], \n";

        message = ids + corpus;

        message += "\"text\":\"" + anchor + "\"";
      }
    }

    String finalMessage;
    if (!message.contentEquals("")) {
      finalMessage = "{ \n";
      finalMessage += message + ", \n";
      finalMessage += "\"top\": \"1\"";
      finalMessage += "}";

      String response = util.getSimilarSlots(Utilities.dispatcherURL, finalMessage, "similarity");

      if (!response.contentEquals("")) {

        String[] response_split = response.split("\n");
        String topResponse = response_split[1].replace("\n", "");

        String[] s1 = topResponse.split("@");

        String answer = s1[0].replace("\n", "");
        String id = s1[2].replace("Index:", "");

        HashMap<String, String> hmap_temp = new HashMap<>();
        hmap_temp.put(id, answer);

        logger.info(answer);

        return hmap_temp;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public HashMap<String, String> checkInternalFAQ(String anchor) throws Exception {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX faq: <" + FAQ.NAMESPACE + "> \n";
    queryString += "select ?s ?id ?question ?answer ?followUp where { \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#faqOntology> { \n";
    queryString += "        ?s rdf:type faq:InternalFAQ . \n";
    queryString += "        ?s faq:faqID ?id . \n";
    queryString += "        ?s faq:faqQuestion ?question . \n";
    queryString += "        ?s faq:faqAnswer ?answer . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "          ?s faq:hasFollowUp ?followUp . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    HashMap<String, String> hmap = new HashMap<>();

    String corpus = "";
    String ids = "";
    String message = "";

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {

        ids = "\"ids\":[ \n";
        corpus = "\"corpus\":[ \n";

        /* we just iterate over all solutions in the result... */
        while (result.hasNext()) {
          BindingSet solution = result.next();

          String answer = solution.getBinding("answer").getValue().stringValue();
          String id = solution.getBinding("id").getValue().stringValue();

          hmap.put(id, answer);

          if (result.hasNext()) {
            ids += "\"" + id + "\",\n";
            corpus += "\"" + answer + "\",\n";
          } else {
            ids += "\"" + id + "\"\n";
            corpus += "\"" + answer + "\"\n";
          }
        }

        ids.substring(0, ids.length() - 1);
        ids += "], \n";

        corpus.substring(0, corpus.length() - 1);
        corpus += "], \n";

        message = ids + corpus;

        message += "\"text\":\"" + anchor + "\"";
      }
    }

    String finalMessage;
    if (!message.contentEquals("")) {
      finalMessage = "{ \n";
      finalMessage += message + ", \n";
      finalMessage += "\"top\": \"1\"";
      finalMessage += "}";

      String response = util.getSimilarSlots(Utilities.dispatcherURL, finalMessage, "similarity");

      if (!response.contentEquals("")) {
        //long count = response.chars().filter(ch -> ch == '\n').count();

        String[] response_split = response.split("\n");
        String topResponse = response_split[1].replace("\n", "");

        String[] s1 = topResponse.split("@");

        String answer = s1[0].replace("\n", "");
        String id = s1[2].replace("Index:", "");

        HashMap<String, String> hmap_temp = new HashMap<>();
        hmap_temp.put(id, answer);

        logger.info(answer);

        return hmap_temp;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public HashMap<String, String> checkExternalFAQ(String anchor) throws Exception {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX faq: <" + FAQ.NAMESPACE + "> \n";
    queryString += "SELECT ?s ?id ?question ?answer ?followUp where { \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#faqOntology> { \n";
    queryString += "        ?s rdf:type faq:ExternalFAQ . \n";
    queryString += "        ?s faq:faqID ?id . \n";
    queryString += "        ?s faq:faqQuestion ?question . \n";
    queryString += "        ?s faq:faqAnswer ?answer . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    HashMap<String, String> hmap = new HashMap<>();

    String corpus = "";
    String ids = "";
    String message = "";

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {

        ids = "\"ids\":[ \n";
        corpus = "\"corpus\":[ \n";

        /* we just iterate over all solutions in the result... */
        while (result.hasNext()) {
          BindingSet solution = result.next();

          String answer = solution.getBinding("answer").getValue().stringValue();
          String id = solution.getBinding("id").getValue().stringValue();

          hmap.put(id, answer);

          if (result.hasNext()) {
            ids += "\"" + id + "\",\n";
            corpus += "\"" + answer + "\",\n";
          } else {
            ids += "\"" + id + "\"\n";
            corpus += "\"" + answer + "\"\n";
          }
        }

        ids.substring(0, ids.length() - 1);
        ids += "], \n";

        corpus.substring(0, corpus.length() - 1);
        corpus += "], \n";

        message = ids + corpus;

        message += "\"text\":\"" + anchor + "\"";
      }
    }

    String finalMessage;
    if (!message.contentEquals("")) {
      finalMessage = "{ \n";
      finalMessage += message + ", \n";
      finalMessage += "\"top\": \"3\"";
      finalMessage += "}";

      String response = util.getSimilarSlots(Utilities.dispatcherURL, finalMessage, "similarity");

      if (!response.contentEquals("")) {
        HashMap<String, String> hmap_temp = new HashMap<>();

        long count = response.chars().filter(ch -> ch == '\n').count();

        String[] response_split = response.split("\n");

        /* Loop over the results */
        for (int i = 1; i <= count; i++) {
          String topResponse = response_split[i].replace("\n", "");
          String[] s1 = topResponse.split("@");
          String answer = s1[0].replace("\n", "");
          String id = s1[2].replace("Index:", "");
          hmap_temp.put(id, answer);
        }

        return hmap_temp;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  public Set checkSubtopics(String anchor, String slotName, String scenarioName) throws Exception {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
    queryString += "SELECT ?sentence ?effectLabel WHERE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> {\n";
    queryString += "        ?x rdf:type ?type1 ;\n";
    queryString += "           welcome:hasValue ?value1 .\n";
    queryString += "    } \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#auxOntology> { \n";
    queryString += "    	?s welcome:slotLabel \"" + slotName + "\" . \n";
    queryString += "        ?s welcome:dependsOn ?type2 . \n";
    queryString += "        ?s welcome:conditionValue ?value2 . \n";
    queryString += "        ?s welcome:hasFullSentence ?sentence . \n";
    queryString += "        ?axiom owl:annotatedTarget ?sentence . \n";
    queryString += "        ?axiom welcome:effectLabel ?effectLabel . \n";
    queryString += "	  } \n";

    if (!scenarioName.toLowerCase().contains("training appointment")) {
      if (scenarioName.toLowerCase().contains("schooling")) {
        queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#SchoolingTopics> {\n";
      } else {
        queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#HealthTopics> {\n";
      }

      queryString += "        ?class rdf:type ?classType . \n";
      queryString += "        ?class welcome:hasValue ?classValue . \n";
      queryString += "    } \n";
      queryString += "    BIND(STRAFTER(STR(?type1), \"#\") AS ?temp1) \n";
      queryString += "    BIND(REPLACE(STRAFTER(STR(?classType), \"#\"), \"Informed\", \"Requested\") AS ?classTypeTrim) \n";
      queryString += "    FILTER(?temp1 = ?type2 && ?value1 = ?value2 && ?effectLabel = ?classTypeTrim && ?classValue = \"Pending\") \n";
      queryString += "} \n";
    } else {
      queryString += "    BIND(STRAFTER(STR(?type1), \"#\") AS ?temp1) \n";
      queryString += "    FILTER(?temp1 = ?type2 && ?value1 = ?value2) \n";
      queryString += "} \n";
    }

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    HashMap<String, String> hmap = new HashMap<>();
    Set<String> hash_Set = new HashSet<>();
    Set<String> temp_Set = new HashSet<>();

    String corpus;
    String ids;
    String message = "";

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {

        ids = "\"ids\":[ \n";
        corpus = "\"corpus\":[ \n";

        /* we just iterate over all solutions in the result... */
        while (result.hasNext()) {
          BindingSet solution = result.next();

          String sentence = solution.getBinding("sentence").getValue().stringValue();
          String effect = solution.getBinding("effectLabel").getValue().stringValue();

          hmap.put(sentence, effect);
          temp_Set.add(effect);

          if (result.hasNext()) {
            ids += "\"1\",\n";
            corpus += "\"" + sentence + "\",\n";
          } else {
            ids += "\"1\"\n";
            corpus += "\"" + sentence + "\"\n";
          }
        }

        ids.substring(0, ids.length() - 1);
        ids += "], \n";

        corpus.substring(0, corpus.length() - 1);
        corpus += "], \n";

        message = ids + corpus;

        message += "\"text\":\"" + anchor + "\"";
      }
    }

    String finalMessage;
    if (!message.contentEquals("")) {
      finalMessage = "{ \n";
      finalMessage += message + ", \n";

      int count = countOccurrences(anchor, "and");

      if (count == 0) {
        finalMessage += "\"top\": \"1\"";
      } else {
        int finalCount = temp_Set.size() < count + 1 ? temp_Set.size() : count + 1;
        finalMessage += "\"top\": \"" + finalCount + "\"";
      }
      finalMessage += "}";

      String response = util.getSimilarSlots(Utilities.dispatcherURL, finalMessage, "similarity");

      if (!response.contentEquals("")) {
        String[] response_split = response.split("\n");

        for (int i = 0; i < response_split.length - 1; i++) {
          String xResponse = response_split[i + 1].replace("\n", "");

          String[] s1 = xResponse.split("@");

          String sentence = s1[0].replace("\n", "");
          String effect = hmap.get(sentence.replace("\n", ""));

          hash_Set.add(effect);
        }

      }
    }
    return hash_Set;
  }

  public int countOccurrences(String str, String word) {
    // split the string by spaces in a
    String[] a = str.split(" ");

    // search for pattern in a
    int count = 0;
    for (int i = 0; i < a.length; i++) {
      // if match found increase count
      if (word.equals(a[i])) {
        count++;
      }
    }

    return count;
  }

  public String checkCompletionStatus(String anchor, String slotName) throws Exception {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "select ?sentence ?effectLabel where { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?x rdf:type welcome:LanguageCode ; \n";
    queryString += "           welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#auxOntology> { \n";
    queryString += "        ?s welcome:slotLabel \"" + slotName + "\" . \n";
    queryString += "        ?s welcome:effectLabel ?effectLabel . \n";
    queryString += "        ?s welcome:hasFullSentence ?sentence . \n";
    queryString += "    } \n";
//    queryString += "    FILTER (LANG(?sentence) = LCASE(?value)) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    HashMap<String, String> hmap = new HashMap<>();

    String corpus;
    String ids;
    String message = "";

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {

        ids = "\"ids\":[ \n";
        corpus = "\"corpus\":[ \n";

        /* we just iterate over all solutions in the result... */
        while (result.hasNext()) {
          BindingSet solution = result.next();

          String sentence = solution.getBinding("sentence").getValue().stringValue();
          String effect = solution.getBinding("effectLabel").getValue().stringValue();

          hmap.put(sentence, effect);

          if (result.hasNext()) {
            ids += "\"1\",\n";
            corpus += "\"" + sentence + "\",\n";
          } else {
            ids += "\"1\"\n";
            corpus += "\"" + sentence + "\"\n";
          }
        }

        ids.substring(0, ids.length() - 1);
        ids += "], \n";

        corpus.substring(0, corpus.length() - 1);
        corpus += "], \n";

        message = ids + corpus;

        message += "\"text\":\"" + anchor + "\"";
      }
    }

    String finalMessage;
    if (!message.contentEquals("")) {
      finalMessage = "{ \n";
      finalMessage += message + ", \n";
      finalMessage += "\"top\": \"1\"";
      finalMessage += "}";

      String response = util.getSimilarSlots(Utilities.dispatcherURL, finalMessage, "similarity");

      if (!response.contentEquals("")) {
        String[] response_split = response.split("\n");
        String topResponse = response_split[1].replace("\n", "");

        String[] s1 = topResponse.split("@");

        String sentence = s1[0].replace("\n", "");
        String effect = hmap.get(sentence.replace("\n", ""));

        return effect;
      } else {
        return "";
      }
    } else {
      return "";
    }
  }

  public String checkCVPurpose(String anchor, Integer c) throws Exception {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "select ?sentence ?effectLabel where { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "        ?x rdf:type welcome:LanguageCode ; \n";
    queryString += "           welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#auxOntology> { \n";
    queryString += "        ?s welcome:slotLabel \"obtainCVCreationInterest\" . \n";
    queryString += "        ?s welcome:effectLabel ?effectLabel . \n";
    queryString += "        ?s welcome:hasFullSentence ?sentence . \n";
    queryString += "    } \n";
    if (c == 0) {
//      queryString += "    FILTER (LANG(?sentence) = LCASE(?value) "
//          + "&& ((CONTAINS(?effectLabel,\"Create New\")) || CONTAINS(?effectLabel,\"Continue Incomplete\"))) \n";
      queryString += "    FILTER (((CONTAINS(?effectLabel,\"Create New\")) || CONTAINS(?effectLabel,\"Continue Incomplete\"))) \n";
    } else {
//      queryString += "    FILTER (LANG(?sentence) = LCASE(?value) "
//          + "&& ((CONTAINS(?effectLabel,\"Create New\")) || CONTAINS(?effectLabel,\"Review Completed\"))) \n";
      queryString += "    FILTER (((CONTAINS(?effectLabel,\"Create New\")) || CONTAINS(?effectLabel,\"Review Completed\"))) \n";
    }
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    HashMap<String, String> hmap = new HashMap<>();

    String corpus = "";
    String ids = "";
    String message = "";

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {

        ids = "\"ids\":[ \n";
        corpus = "\"corpus\":[ \n";

        /* we just iterate over all solutions in the result... */
        while (result.hasNext()) {
          BindingSet solution = result.next();

          String sentence = solution.getBinding("sentence").getValue().stringValue();
          String effect = solution.getBinding("effectLabel").getValue().stringValue();

          hmap.put(sentence, effect);

          if (result.hasNext()) {
            ids += "\"1\",\n";
            corpus += "\"" + sentence + "\",\n";
          } else {
            ids += "\"1\"\n";
            corpus += "\"" + sentence + "\"\n";
          }
        }

        ids.substring(0, ids.length() - 1);
        ids += "], \n";

        corpus.substring(0, corpus.length() - 1);
        corpus += "], \n";

        message = ids + corpus;

        message += "\"text\":\"" + anchor + "\"";
      }
    }

    String finalMessage;
    if (!message.contentEquals("")) {
      finalMessage = "{ \n";
      finalMessage += message + ", \n";
      finalMessage += "\"top\": \"1\"";
      finalMessage += "}";

      String response = util.getSimilarSlots(Utilities.dispatcherURL, finalMessage, "similarity");

      if (!response.contentEquals("")) {
        String[] response_split = response.split("\n");
        String topResponse = response_split[1].replace("\n", "");

        String[] s1 = topResponse.split("@");

        String sentence = s1[0].replace("\n", "");
        String effect = hmap.get(sentence.replace("\n", ""));

        return effect;
      } else {
        return "";
      }
    } else {
      return "";
    }
  }

  public void initProfile(String scenario, Integer sim) {
    /* Create IRI for Language */
    IRI langType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "Language");

    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    String language = getProfileValue(langType, 0);

    updateProfile(builder, langType, language, sim);

    /* Create IRI for Scenario */
    IRI scenarioType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "Scenario");

    updateProfile(builder, scenarioType, scenario, sim);

    /* We're done building, create our Model */
    Model model = builder.build();

    /* Commit model to repository */
    util.commitModel(model);
  }

  public String getCVPurpose() {
    String queryString;

    queryString = " PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += " PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += " SELECT ?value where { \n";
    queryString += " ?session welcome:involvesDIP ?g . \n";
    queryString += " ?session welcome:hasTimestamp ?t . \n";
    queryString += "      GRAPH ?g { \n";
    queryString += "   	      ?dip welcome:hasSlot ?s . \n";
    queryString += "          ?s welcome:hasTCNAnswer ?o . \n";
    queryString += "          ?o rdf:object ?value . \n";
    queryString += "      } \n";
    queryString += "      FILTER(CONTAINS(STR(?s), \"obtainCVCreationInterest\")) \n";
    queryString += " } ORDER BY DESC(?t) LIMIT 1 \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    String value = "";

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        value = solution.getBinding("sentence").getValue().stringValue();
      }
    }
    return value;
  }

  public void increaseElements(String element) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "ASK WHERE { \n";
    queryString += "    GRAPH welcome:CVInfo { \n";
    queryString += "        welcome:CV welcome:" + element + "Elements ?elem . \n";
    queryString += "    } \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    boolean check = booleanQuery.evaluate();

    if (check) {
      queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "INSERT DATA { \n";
      queryString += "    GRAPH welcome:CVInfo { \n";
      queryString += "        welcome:CV welcome:" + element + "Elements 0; \n";
      queryString += "    } \n";
      queryString += "} \n";

      /* Execute the query */
      util.executeQuery(queryString);
    }

    logger.info("Increasing " + element + " Elements by 1.");

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH welcome:CVInfo { \n";
    queryString += "        welcome:CV welcome:" + element + "Elements ?number . \n";
    queryString += "    }   \n";
    queryString += "} INSERT { \n";
    queryString += "    GRAPH welcome:CVInfo { \n";
    queryString += "        welcome:CV welcome:" + element + "Elements ?newNumber . \n";
    queryString += "    }  \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH welcome:CVInfo { \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            welcome:CV welcome:" + element + "Elements ?number . \n";
    queryString += "        } \n";
    queryString += "    } \n";
    queryString +=
        "    BIND(EXISTS {welcome:CV welcome:" + element + "Elements ?number .} as ?check) \n";
    queryString += "    BIND(IF(?check = true, ?number + 1, 1) as ?newNumber) \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public Object getTCNEducationSection(boolean flag) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT ?elem ?v1_ ?v2_ ?v3_ ?v4_ ?v5_ ?v6_ ?v7_ WHERE { \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += "        ?CV ?hasElem ?elem . \n";
    queryString += "        ?elem rdf:type welcome:EducationElem . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasDegreeTitle ?title . \n";
    queryString += "            ?title welcome:hasValue ?v1 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasDegreeCertificate ?certificate . \n";
    queryString += "            ?certificate welcome:hasValue ?v2 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasInstitutionName ?name . \n";
    queryString += "            ?name welcome:hasValue ?v3 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasGraduationYear ?year . \n";
    queryString += "            ?year welcome:hasValue ?v4 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "        	?elem welcome:hasGraduationStatus ?status . \n";
    queryString += "            ?status welcome:hasValue ?v5 . \n";
    queryString += "        } \n";
//    queryString += "        OPTIONAL { \n";
//    queryString += "            ?elem welcome:hasCompletedCourses ?courses . \n";
//    queryString += "            ?courses welcome:hasValue ?v6 . \n";
//    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasDegreeGrade ?grade . \n";
    queryString += "            ?grade welcome:hasValue ?v7 . \n";
    queryString += "        } \n";
    queryString += "        BIND(IF(!BOUND(?v1), \"\", ?v1) as ?v1_) \n";
    queryString += "        BIND(IF(!BOUND(?v2), \"\", ?v2) as ?v2_) \n";
    queryString += "        BIND(IF(!BOUND(?v3), \"\", ?v3) as ?v3_) \n";
    queryString += "        BIND(IF(!BOUND(?v4), \"\", ?v4) as ?v4_) \n";
    queryString += "        BIND(IF(!BOUND(?v5), \"\", ?v5) as ?v5_) \n";
//    queryString += "        BIND(IF(!BOUND(?v6), \"na\", ?v6) as ?v6_) \n";
    queryString += "        BIND(IF(!BOUND(?v7), \"\", ?v7) as ?v7_) \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {
        // we just iterate over all solutions in the result...
        while (result.hasNext()) {
          BindingSet solution = result.next();

          JSONObject obj = new JSONObject();

          /* Get the values */
          Value elem = solution.getBinding("elem").getValue();
          IRI temp = Utilities.f.createIRI(elem.stringValue());

          Value v1 = solution.getBinding("v1_").getValue();
          Value v2 = solution.getBinding("v2_").getValue();
          Value v3 = solution.getBinding("v3_").getValue();
          Value v4 = solution.getBinding("v4_").getValue();
          Value v5 = solution.getBinding("v5_").getValue();
//          Value v6 = solution.getBinding("v6_").getValue();
          Value v7 = solution.getBinding("v7_").getValue();

          /* Split the IRI and get the class Name */
          String classLabel = util.splitIRI(temp);

          /* Put class name and value into the JSON object */
          obj.put("id", classLabel);
          obj.put("DegreeTitle", v1.stringValue());
          obj.put("DegreeCertificate", v2.stringValue());
          obj.put("InstitutionName", v3.stringValue());
          obj.put("GraduationYear", v4.stringValue());
          obj.put("GraduationStatus", v5.stringValue());
//          obj.put("CompletedCourses", v6.stringValue());
          obj.put("DegreeGrade", v7.stringValue());

          array.add(obj);
        }

        jsonObject.put("education", array);
      } else {
        JSONObject obj = new JSONObject();

        /* Put class name and value into the JSON object */
        obj.put("id", "educationElement-1");
        obj.put("DegreeTitle", "");
        obj.put("DegreeCertificate", "");
        obj.put("InstitutionName", "");
        obj.put("GraduationYear", "");
        obj.put("GraduationStatus", "");
        obj.put("DegreeGrade", "");

        array.add(obj);

        jsonObject.put("education", array);
      }
    }

    if (flag) {
      return array;
    } else {
      /* Create the final object */
      JSONObject fullObject = new JSONObject();
      fullObject.put("data", jsonObject);

      return fullObject;
    }
  }

  public Object getTCNCourseSection(boolean flag) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT ?elem ?v1_ ?v2_ ?v3_ ?v4_ ?v5_ ?v6_ ?v7_ ?v8_ WHERE { \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += "        ?CV ?hasElem ?elem . \n";
    queryString += "        ?elem rdf:type welcome:CourseElem . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasCourseName ?name . \n";
    queryString += "            ?name welcome:hasValue ?v1 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasCourseCertificate ?certificate . \n";
    queryString += "            ?certificate welcome:hasValue ?v2 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasCourseSchool ?school . \n";
    queryString += "            ?school welcome:hasValue ?v3 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasCourseYear ?year . \n";
    queryString += "            ?year welcome:hasValue ?v4 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "        	?elem welcome:hasCourseStatus ?status . \n";
    queryString += "            ?status welcome:hasValue ?v5 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasCourseDuration ?duration . \n";
    queryString += "            ?duration welcome:hasValue ?v6 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasCourseGrade ?grade . \n";
    queryString += "            ?grade welcome:hasValue ?v7 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasCourseSchoolType ?type . \n";
    queryString += "            ?type welcome:hasValue ?v8 . \n";
    queryString += "        } \n";
    queryString += "        BIND(IF(!BOUND(?v1), \"\", ?v1) as ?v1_) \n";
    queryString += "        BIND(IF(!BOUND(?v2), \"\", ?v2) as ?v2_) \n";
    queryString += "        BIND(IF(!BOUND(?v3), \"\", ?v3) as ?v3_) \n";
    queryString += "        BIND(IF(!BOUND(?v4), \"\", ?v4) as ?v4_) \n";
    queryString += "        BIND(IF(!BOUND(?v5), \"\", ?v5) as ?v5_) \n";
    queryString += "        BIND(IF(!BOUND(?v6), \"\", ?v6) as ?v6_) \n";
    queryString += "        BIND(IF(!BOUND(?v7), \"\", ?v7) as ?v7_) \n";
    queryString += "        BIND(IF(!BOUND(?v8), \"\", ?v8) as ?v8_) \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {
        // we just iterate over all solutions in the result...
        while (result.hasNext()) {
          BindingSet solution = result.next();

          JSONObject obj = new JSONObject();

          /* Get the values */
          Value elem = solution.getBinding("elem").getValue();
          IRI temp = Utilities.f.createIRI(elem.stringValue());

          Value v1 = solution.getBinding("v1_").getValue();
          Value v2 = solution.getBinding("v2_").getValue();
          Value v3 = solution.getBinding("v3_").getValue();
          Value v4 = solution.getBinding("v4_").getValue();
          Value v5 = solution.getBinding("v5_").getValue();
          Value v6 = solution.getBinding("v6_").getValue();
          Value v7 = solution.getBinding("v7_").getValue();
          Value v8 = solution.getBinding("v8_").getValue();

          /* Split the IRI and get the class Name */
          String classLabel = util.splitIRI(temp);

          /* Put class name and value into the JSON object */
          obj.put("id", classLabel);
          obj.put("CourseName", v1.stringValue());
          obj.put("CourseCertificate", v2.stringValue());
          obj.put("CourseSchool", v3.stringValue());
          obj.put("CourseYear", v4.stringValue());
          obj.put("CourseStatus", v5.stringValue());
          obj.put("CourseDuration", v6.stringValue());
          obj.put("CourseGrade", v7.stringValue());
          obj.put("CourseSchoolType", v8.stringValue());

          array.add(obj);
        }

        jsonObject.put("course", array);
      } else {
        JSONObject obj = new JSONObject();

        /* Put class name and value into the JSON object */
        obj.put("id", "courseElement-1");
        obj.put("CourseName", "");
        obj.put("CourseCertificate", "");
        obj.put("CourseSchool", "");
        obj.put("CourseYear", "");
        obj.put("CourseStatus", "");
        obj.put("CourseDuration", "");
        obj.put("CourseGrade", "");
        obj.put("CourseSchoolType", "");

        array.add(obj);

        jsonObject.put("course", array);
      }
    }

    if (flag) {
      return array;
    } else {
      /* Create the final object */
      JSONObject fullObject = new JSONObject();
      fullObject.put("data", jsonObject);

      return fullObject;
    }
  }

  /* Return the list of skills */
  public Object getTCNSkillSection(boolean f, boolean skipFalse) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
    queryString += "select ?s ?flag where { \n";
    queryString += "    GRAPH welcome:SkillsGraph { \n";
    queryString += "        ?s rdf:type welcome:Skill . \n";
    queryString += "        ?s welcome:selected ?flag . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    JSONObject skillObject = new JSONObject();
    JSONArray skillArray = new JSONArray();

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {
        // we just iterate over all solutions in the result...
        while (result.hasNext()) {
          BindingSet solution = result.next();

          JSONObject obj = new JSONObject();

          /* Get the values */
          Value s = solution.getBinding("s").getValue();
          Boolean flag = Boolean.parseBoolean(solution.getBinding("flag").getValue().stringValue());

          if (skipFalse && !flag) {
            continue;
          }

          IRI temp = Utilities.f.createIRI(s.stringValue());

          /* Split the IRI and get the class Name */
          String classLabel = util.splitIRI(temp).replace("_", " ");

          /* Put class name and value into the JSON object */
          obj.put("skill", classLabel);
          obj.put("selected", flag);

          skillArray.add(obj);
        }

        skillObject.put("skills", skillArray);
      }
    }

    if (f) {
      return skillArray;
    } else {
      /* Create the final object */
      JSONObject fullObject = new JSONObject();
      fullObject.put("data", skillObject);

      return fullObject;
    }
  }

  public Object getTCNLanguageSection(boolean flag) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT ?elem ?v1_ ?v2_ ?v3_ ?v4_ ?v5_ ?v6_ ?v7_ ?v8_ ?v9_ ?v10_ ?v11_ ?v12_ ?v13_ ?v14_ WHERE { \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += "        ?CV ?hasElem ?elem . \n";
    queryString += "        ?elem rdf:type welcome:LanguageElem . \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageName ?name . \n";
    queryString += "            ?name welcome:hasValue ?v1 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageLevel ?level . \n";
    queryString += "            ?level welcome:hasValue ?v2 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCertificate ?certificate . \n";
    queryString += "            ?certificate welcome:hasValue ?v3 . \n";
    queryString += "        } \n";
//    queryString += "        OPTIONAL { \n";
//    queryString += "            ?elem welcome:hasAdditionalLanguageDegree ?degree . \n";
//    queryString += "            ?degree welcome:hasValue ?v4 . \n";
//    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "        	?elem welcome:hasAdditionalLanguageCourseName ?cName . \n";
    queryString += "            ?cName welcome:hasValue ?v5 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCourseCertificate ?cCertificate . \n";
    queryString += "            ?cCertificate welcome:hasValue ?v6 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCourseStatus ?cStatus . \n";
    queryString += "            ?cStatus welcome:hasValue ?v7 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCourseSchool ?cSchool . \n";
    queryString += "            ?cSchool welcome:hasValue ?v8 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCourseSchoolType ?cType . \n";
    queryString += "            ?cType welcome:hasValue ?v9 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCourseYear ?cYear . \n";
    queryString += "            ?cYear welcome:hasValue ?v10 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCourseDuration ?cDuration . \n";
    queryString += "            ?cDuration welcome:hasValue ?v11 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCourseGrade ?cGrade . \n";
    queryString += "            ?cGrade welcome:hasValue ?v12 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCountryName ?ccName . \n";
    queryString += "            ?ccName welcome:hasValue ?v13 . \n";
    queryString += "        } \n";
    queryString += "        OPTIONAL { \n";
    queryString += "            ?elem welcome:hasAdditionalLanguageCountryDuration ?ccDuration . \n";
    queryString += "            ?ccDuration welcome:hasValue ?v14 . \n";
    queryString += "        } \n";
    queryString += "        BIND(IF(!BOUND(?v1), \"\", ?v1) as ?v1_) \n";
    queryString += "        BIND(IF(!BOUND(?v2), \"\", ?v2) as ?v2_) \n";
    queryString += "        BIND(IF(!BOUND(?v3), \"\", ?v3) as ?v3_) \n";
//    queryString += "        BIND(IF(!BOUND(?v4), \"\", ?v4) as ?v4_) \n";
    queryString += "        BIND(IF(!BOUND(?v5), \"\", ?v5) as ?v5_) \n";
    queryString += "        BIND(IF(!BOUND(?v6), \"\", ?v6) as ?v6_) \n";
    queryString += "        BIND(IF(!BOUND(?v7), \"\", ?v7) as ?v7_) \n";
    queryString += "        BIND(IF(!BOUND(?v8), \"\", ?v8) as ?v8_) \n";
    queryString += "        BIND(IF(!BOUND(?v9), \"\", ?v9) as ?v9_) \n";
    queryString += "        BIND(IF(!BOUND(?v10), \"\", ?v10) as ?v10_) \n";
    queryString += "        BIND(IF(!BOUND(?v11), \"\", ?v11) as ?v11_) \n";
    queryString += "        BIND(IF(!BOUND(?v12), \"\", ?v12) as ?v12_) \n";
    queryString += "        BIND(IF(!BOUND(?v13), \"\", ?v13) as ?v13_) \n";
    queryString += "        BIND(IF(!BOUND(?v14), \"\", ?v14) as ?v14_) \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {
        // we just iterate over all solutions in the result...
        while (result.hasNext()) {
          BindingSet solution = result.next();

          JSONObject obj = new JSONObject();

          /* Get the values */
          Value elem = solution.getBinding("elem").getValue();
          IRI temp = Utilities.f.createIRI(elem.stringValue());

          Value v1 = solution.getBinding("v1_").getValue();
          Value v2 = solution.getBinding("v2_").getValue();
          Value v3 = solution.getBinding("v3_").getValue();
//          Value v4 = solution.getBinding("v4_").getValue();
          Value v5 = solution.getBinding("v5_").getValue();
          Value v6 = solution.getBinding("v6_").getValue();
          Value v7 = solution.getBinding("v7_").getValue();
          Value v8 = solution.getBinding("v8_").getValue();
          Value v9 = solution.getBinding("v9_").getValue();
          Value v10 = solution.getBinding("v10_").getValue();
          Value v11 = solution.getBinding("v11_").getValue();
          Value v12 = solution.getBinding("v12_").getValue();
          Value v13 = solution.getBinding("v13_").getValue();
          Value v14 = solution.getBinding("v14_").getValue();

          /* Split the IRI and get the class Name */
          String classLabel = util.splitIRI(temp);

          /* Put class name and value into the JSON object */
          obj.put("id", classLabel);
          obj.put("AdditionalLanguageName", v1.stringValue());
          obj.put("AdditionalLanguageLevel", v2.stringValue());
          obj.put("AdditionalLanguageCertificate", v3.stringValue());
//          obj.put("AdditionalLanguageDegree", v4.stringValue());
          obj.put("AdditionalLanguageCourseName", v5.stringValue());
          obj.put("AdditionalLanguageCourseCertificate", v6.stringValue());
          obj.put("AdditionalLanguageCourseStatus", v7.stringValue());
          obj.put("AdditionalLanguageCourseSchool", v8.stringValue());
          obj.put("AdditionalLanguageCourseSchoolType", v9.stringValue());
          obj.put("AdditionalLanguageCourseYear", v10.stringValue());
          obj.put("AdditionalLanguageCourseDuration", v11.stringValue());
          obj.put("AdditionalLanguageCourseGrade", v12.stringValue());
          obj.put("AdditionalLanguageCountryName", v13.stringValue());
          obj.put("AdditionalLanguageCountryDuration", v14.stringValue());

          array.add(obj);
        }

        jsonObject.put("language", array);
      } else {
        JSONObject obj = new JSONObject();

        /* Put class name and value into the JSON object */
        obj.put("id", "languageElement-1");
        obj.put("AdditionalLanguageName", "");
        obj.put("AdditionalLanguageLevel", "");
        obj.put("AdditionalLanguageCertificate", "");
        obj.put("AdditionalLanguageCourseName", "");
        obj.put("AdditionalLanguageCourseCertificate", "");
        obj.put("AdditionalLanguageCourseStatus", "");
        obj.put("AdditionalLanguageCourseSchool", "");
        obj.put("AdditionalLanguageCourseSchoolType", "");
        obj.put("AdditionalLanguageCourseYear", "");
        obj.put("AdditionalLanguageCourseDuration", "");
        obj.put("AdditionalLanguageCourseGrade", "");
        obj.put("AdditionalLanguageCountryName", "");
        obj.put("AdditionalLanguageCountryDuration", "");

        array.add(obj);

        jsonObject.put("language", array);
      }
    }

    if (flag) {
      return array;
    } else {
      /* Create the final object */
      JSONObject fullObject = new JSONObject();
      fullObject.put("data", jsonObject);

      return fullObject;
    }
  }

  public Object getTCNEmploymentSection(boolean flag) {
    /* Create employment status IRI */
    IRI currentEmploymentStatus = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "CurrentlyEmployed");

    String currentlyEmployed = getCVElementValue(currentEmploymentStatus, true);

    String queryString;
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    if (currentlyEmployed.contentEquals("Yes")) {
      queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "SELECT ?elem ?v1_ ?v2_ ?v3_ ?v4_ ?v5_ ?v6_ WHERE { \n";
      queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
      queryString += "        ?CV ?hasElem ?elem . \n";
      queryString += "        ?elem rdf:type welcome:EmploymentElem . \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasOccupation ?occ . \n";
      queryString += "            ?occ welcome:hasValue ?v1 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasEmployerName ?name . \n";
      queryString += "            ?name welcome:hasValue ?v2 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasEmployerAddress ?address . \n";
      queryString += "            ?address welcome:hasValue ?v3 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasStartingDate ?date . \n";
      queryString += "            ?date welcome:hasValue ?v4 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "        	?elem welcome:hasEndDate ?end . \n";
      queryString += "            ?end welcome:hasValue ?v5 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasMainActivities ?activities . \n";
      queryString += "            ?activities welcome:hasValue ?v6 . \n";
      queryString += "        } \n";
      queryString += "        BIND(IF(!BOUND(?v1), \"\", ?v1) as ?v1_) \n";
      queryString += "        BIND(IF(!BOUND(?v2), \"\", ?v2) as ?v2_) \n";
      queryString += "        BIND(IF(!BOUND(?v3), \"\", ?v3) as ?v3_) \n";
      queryString += "        BIND(IF(!BOUND(?v4), \"\", ?v4) as ?v4_) \n";
      queryString += "        BIND(IF(!BOUND(?v5), \"\", ?v5) as ?v5_) \n";
      queryString += "        BIND(IF(!BOUND(?v6), \"\", ?v6) as ?v6_) \n";
      queryString += "    } \n";
      queryString += "} \n";

      TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

      try (TupleQueryResult result = query.evaluate()) {
        if (result.hasNext()) {
          // we just iterate over all solutions in the result...
          while (result.hasNext()) {
            BindingSet solution = result.next();

            JSONObject obj = new JSONObject();

            /* Get the values */
            Value elem = solution.getBinding("elem").getValue();
            IRI temp = Utilities.f.createIRI(elem.stringValue());

            Value v1 = solution.getBinding("v1_").getValue();
            Value v2 = solution.getBinding("v2_").getValue();
            Value v3 = solution.getBinding("v3_").getValue();
            Value v4 = solution.getBinding("v4_").getValue();
            Value v5 = solution.getBinding("v5_").getValue();
            Value v6 = solution.getBinding("v6_").getValue();

            /* Split the IRI and get the class Name */
            String classLabel = util.splitIRI(temp);

            /* Put class name and value into the JSON object */
            obj.put("id", classLabel);
            obj.put("Occupation", v1.stringValue());
            obj.put("EmployerName", v2.stringValue());
            obj.put("EmployerAddress", v3.stringValue());
            obj.put("StartingDate", v4.stringValue());
            obj.put("EndDate", v5.stringValue());
            obj.put("MainActivities", v6.stringValue());

            array.add(obj);
          }
        }
      }
    }

    /* Create employment status IRI */
    IRI previousOccupation = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "PreviousOccupation");

    String prevOccupation = getCVElementValue(previousOccupation, true);

    if (!prevOccupation.contentEquals("")) {
      queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "SELECT ?elem ?v1_ ?v2_ ?v3_ ?v4_ ?v5_ ?v6_ ?v7_ WHERE { \n";
      queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
      queryString += "        ?CV ?hasElem ?elem . \n";
      queryString += "        ?elem rdf:type welcome:EmploymentElem . \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasPreviousOccupation ?occ . \n";
      queryString += "            ?occ welcome:hasValue ?v1 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasPreviousEmployerName ?name . \n";
      queryString += "            ?name welcome:hasValue ?v2 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasPreviousEmployerAddress ?address . \n";
      queryString += "            ?address welcome:hasValue ?v3 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasPreviousStartingDate ?date . \n";
      queryString += "            ?date welcome:hasValue ?v4 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "        	?elem welcome:hasPreviousEndDate ?end . \n";
      queryString += "            ?end welcome:hasValue ?v5 . \n";
      queryString += "        } \n";
      queryString += "        OPTIONAL { \n";
      queryString += "            ?elem welcome:hasPreviousMainActivities ?activities . \n";
      queryString += "            ?activities welcome:hasValue ?v7 . \n";
      queryString += "        } \n";
      queryString += "        BIND(IF(!BOUND(?v1), \"na\", ?v1) as ?v1_) \n";
      queryString += "        BIND(IF(!BOUND(?v2), \"na\", ?v2) as ?v2_) \n";
      queryString += "        BIND(IF(!BOUND(?v3), \"na\", ?v3) as ?v3_) \n";
      queryString += "        BIND(IF(!BOUND(?v4), \"na\", ?v4) as ?v4_) \n";
      queryString += "        BIND(IF(!BOUND(?v5), \"na\", ?v5) as ?v5_) \n";
      queryString += "        BIND(IF(!BOUND(?v7), \"na\", ?v7) as ?v7_) \n";
      queryString += "    } \n";
      queryString += "} \n";

      TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

      try (TupleQueryResult result = query.evaluate()) {
        if (result.hasNext()) {
          // we just iterate over all solutions in the result...
          while (result.hasNext()) {
            BindingSet solution = result.next();

            JSONObject obj = new JSONObject();

            /* Get the values */
            Value elem = solution.getBinding("elem").getValue();
            IRI temp = Utilities.f.createIRI(elem.stringValue());

            Value v1 = solution.getBinding("v1_").getValue();
            Value v2 = solution.getBinding("v2_").getValue();
            Value v3 = solution.getBinding("v3_").getValue();
            Value v4 = solution.getBinding("v4_").getValue();
            Value v5 = solution.getBinding("v5_").getValue();
            Value v7 = solution.getBinding("v7_").getValue();

            /* Split the IRI and get the class Name */
            String classLabel = util.splitIRI(temp);

            /* Put class name and value into the JSON object */
            obj.put("id", classLabel);
            obj.put("Occupation", v1.stringValue());
            obj.put("EmployerName", v2.stringValue());
            obj.put("EmployerAddress", v3.stringValue());
            obj.put("StartingDate", v4.stringValue());
            obj.put("EndDate", v5.stringValue());
            obj.put("MainActivities", v7.stringValue());

            array.add(obj);
          }
        }
      }
    }

    if (!currentlyEmployed.contentEquals("Yes")
        && prevOccupation.contentEquals("")) {
      JSONObject obj = new JSONObject();

      /* Put class name and value into the JSON object */
      obj.put("id", "employmentElement-1");
      obj.put("Occupation", "");
      obj.put("EmployerName", "");
      obj.put("EmployerAddress", "");
      obj.put("StartingDate", "");
      obj.put("EndDate", "");
      obj.put("MainActivities", "");

      array.add(obj);

      jsonObject.put("employment", array);
    } else if (currentlyEmployed.contentEquals("Yes")
        || !prevOccupation.contentEquals("")) {
      jsonObject.put("employment", array);
    }

    if (flag) {
      return array;
    } else {
      /* Create the final object */
      JSONObject fullObject = new JSONObject();
      fullObject.put("data", jsonObject);

      return fullObject;
    }
  }

  public boolean checkElements(String elem) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "ASK WHERE { \n";
    queryString += "  GRAPH welcome:CVInfo { \n";
    queryString += "    ?x welcome:hasElem ?elem . \n";
    queryString += "        ?elem rdf:type welcome:" + elem + "Elem . \n";
    queryString += "  } \n";
    queryString += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, queryString);

    return booleanQuery.evaluate();
  }

  public void checkCHCPreferences() {
    String queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select (count(*) as ?count) where { \n";
    queryString += "    GRAPH welcome:CHCPreferences { \n";
    queryString += "		?s ?p ?o . \n";
    queryString += "    } \n";
    queryString += "} \n";
    queryString += " \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    int count = -1;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        count = Integer.parseInt(solution.getBinding("count").getValue().stringValue());

        APP app = new APP();

        if (count == 0) {
          String message = "{\n"
              + "    \"enableSearch\": \"true\",\n"
              + "    \"ChcAgePreference\": [\n"
              + "        {\n"
              + "            \"lowerBound\": \"Don't mind\",\n"
              + "            \"higherBound\": \"Don't mind\"\n"
              + "        }\n"
              + "    ],\n"
              + "    \"ChcGenderPreference\": \"Don't mind\",\n"
              + "    \"ChcFamilyPreference\": [\n"
              + "        \"Don't mind\"\n"
              + "    ],\n"
              + "    \"ChcNationPreference\": \"Don't mind\",\n"
              + "    \"ChcReligionPreference\": \"Don't mind\",\n"
              + "    \"ChcEthnicPreference\": \"Don't mind\",\n"
              + "    \"ChcLocationPreference\": [\n"
              + "        \"EL301\",\n"
              + "        \"EL302\",\n"
              + "        \"EL303\",\n"
              + "        \"EL304\",\n"
              + "        \"EL305\",\n"
              + "        \"EL306\",\n"
              + "        \"EL307\",\n"
              + "        \"EL307\",\n"
              + "        \"EL411\",\n"
              + "        \"EL411\",\n"
              + "        \"EL412\",\n"
              + "        \"EL412\",\n"
              + "        \"EL413\",\n"
              + "        \"EL421\",\n"
              + "        \"EL421\",\n"
              + "        \"EL421\",\n"
              + "        \"EL421\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL422\",\n"
              + "        \"EL431\",\n"
              + "        \"EL432\",\n"
              + "        \"EL433\",\n"
              + "        \"EL434\",\n"
              + "        \"EL511\",\n"
              + "        \"EL512\",\n"
              + "        \"EL513\",\n"
              + "        \"EL514\",\n"
              + "        \"EL515\",\n"
              + "        \"EL515\",\n"
              + "        \"EL521\",\n"
              + "        \"EL522\",\n"
              + "        \"EL523\",\n"
              + "        \"EL524\",\n"
              + "        \"EL525\",\n"
              + "        \"EL526\",\n"
              + "        \"EL527\",\n"
              + "        \"EL531\",\n"
              + "        \"EL531\",\n"
              + "        \"EL532\",\n"
              + "        \"EL533\",\n"
              + "        \"EL541\",\n"
              + "        \"EL541\",\n"
              + "        \"EL542\",\n"
              + "        \"EL543\",\n"
              + "        \"EL611\",\n"
              + "        \"EL611\",\n"
              + "        \"EL612\",\n"
              + "        \"EL613\",\n"
              + "        \"EL621\",\n"
              + "        \"EL622\",\n"
              + "        \"EL623\",\n"
              + "        \"EL623\",\n"
              + "        \"EL624\",\n"
              + "        \"EL631\",\n"
              + "        \"EL632\",\n"
              + "        \"EL633\",\n"
              + "        \"EL641\",\n"
              + "        \"EL642\",\n"
              + "        \"EL643\",\n"
              + "        \"EL644\",\n"
              + "        \"EL645\",\n"
              + "        \"EL651\",\n"
              + "        \"EL651\",\n"
              + "        \"EL652\",\n"
              + "        \"EL653\",\n"
              + "        \"EL653\"\n"
              + "    ],\n"
              + "    \"ChcAccessibilityPreference\": \"Don't mind\",\n"
              + "    \"ChcRentPeriodPreference\": {\n"
              + "        \"startDate\": \"Don't mind\",\n"
              + "        \"endDate\": \"Don't mind\"\n"
              + "    },\n"
              + "    \"ChcShareWithPreference\": {\n"
              + "        \"min\": \"Don't mind\",\n"
              + "        \"max\": \"Don't mind\"\n"
              + "    }\n"
              + "}";

          app.populateCHCPreferences(util.parseJSONLD(message));
        }
      }
    }
  }

  public List<String> getLocationPreferences() {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT ?loc WHERE { \n";
    queryString += "  GRAPH welcome:CHCPreferences { \n";
    queryString += "    ?s welcome:hasChcLocationPreference/rdf:rest*/rdf:first ?loc . \n";
    queryString += "  } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    List<String> list = new ArrayList<>();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        String loc = solution.getBinding("loc").getValue().stringValue();

        list.add(loc);
      }
    }

    return list;
  }

  public List<String> getOtherLanguageList() {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT ?lang WHERE { \n";
    queryString += "  GRAPH welcome:ProfileInfo { \n";
    queryString += "    ?s welcome:hasOtherLanguageCode/rdf:rest*/rdf:first ?lang . \n";
    queryString += "  } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    List<String> list = new ArrayList<>();

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        String loc = solution.getBinding("lang").getValue().stringValue();

        list.add(loc);
      }
    }

    return list;
  }

  public void markAllCompleted() {
    IRI graph = activeDIP_S();

    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "		?s welcome:hasStatus ?status . \n";
    queryString += "    } \n";
    queryString += "} INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "		?s welcome:hasStatus welcome:Completed . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "		?s welcome:hasStatus ?status . \n";
    queryString += "    } \n";
    queryString += "} \n";

    util.executeQuery(queryString);
  }

  public void markAllDemandTrue() {
    IRI graph = activeDIP_S();

    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasTCNAnswer ?content . \n";
    queryString += "    } \n";
    queryString += "} INSERT { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasTCNAnswer \n";
    queryString += "                [ a rdf:Statement; \n";
    queryString += "                  rdf:subject ?final; \n";
    queryString += "                  rdf:predicate welcome:hasValue; \n";
    queryString += "                  rdf:object \"Yes\"] \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?s welcome:hasTCNAnswer ?content . \n";
    queryString += "        ?s welcome:hasOntologyType ?ontoType . \n";
    queryString += "        ?s rdf:type ?type; \n";
    queryString += "    } \n";
    queryString += "    BIND(IRI(?ontoType) as ?temp) \n";
    queryString += "    BIND(IF(!CONTAINS(STR(?temp), \"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\"), \n";
    queryString += "         IRI(CONCAT(\"https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#\", STRAFTER(STR(?ontoType), \":\"))), ?temp) as ?final) \n";
    queryString += " \n";
    queryString += "    FILTER(!CONTAINS(STR(?type), \"SystemInfo\")) \n";
    queryString += "} \n";

    util.executeQuery(queryString);
  }
}
