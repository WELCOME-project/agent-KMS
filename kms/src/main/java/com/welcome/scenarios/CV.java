package com.welcome.scenarios;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.SlotLists;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import com.welcome.services.LAS;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CV {

  Logger logger = LoggerFactory.getLogger(LAS.class);

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries q = new Queries();

  public void copySkills() {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH welcome:SkillsGraph { \n";
    queryString += "        ?s rdf:type welcome:Skill . \n";
    queryString += "        ?s welcome:selected false . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH welcome:cvOntology { \n";
    queryString += "        ?s rdfs:subClassOf welcome:Skills. \n";
    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void copyCVPersonal(Object personal) {
    String values = String.valueOf(personal).replace("[", "(").replace("]", ")");

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += "        ?dialogueUser welcome:hasCV welcome:CV . \n";
    queryString += "        welcome:CV welcome:hasElem ?property . \n";
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

  /**
   * Updates DIP with existing knowledge.
   */
  public void updateCV(Integer sim) {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    /* Update DIP using existing knowledge */
    updateDIPCV_I(activeDIP, sim);
  }

  public void updateDIPCV_I(Resource context, Integer sim) {
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
    if (sim == 2) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    } else if (sim == 3) {
      queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#tempGraph> { \n";
    }
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

  public void copySection(Object slots, String section, int number) {
    String values = String.valueOf(slots).replace("[", "(").replace("]", ")");

    /* Create user IRI */
    IRI sectionIRI = Utilities.f
        .createIRI(WELCOME.NAMESPACE, section.toLowerCase() + "Element-" + number);

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "INSERT { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += "        ?dialogueUser welcome:hasCV welcome:CV . \n";
    queryString += "        welcome:CV welcome:hasElem ?elem . \n";
    queryString += "        ?elem rdf:type welcome:" + section + "Elem . \n";
    queryString += "        ?elem ?hasProperty ?property . \n";
    queryString += "        ?property rdf:type ?type . \n";
    queryString += "        ?property welcome:hasValue ?value . \n";
    queryString += "        ?property welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#tempGraph> { \n";
    queryString += "        ?dialogueUser ?hasProperty ?property . \n";
    queryString += "        ?property rdf:type ?type . \n";
    queryString += "        ?property welcome:hasValue ?value . \n";
    queryString += "        ?property welcome:lastUpdated ?timestamp . \n";
    queryString += "    } \n";
    queryString += "    BIND(IRI(<" + sectionIRI.stringValue() + ">) as ?elem) \n";
    queryString += "    FILTER (?type"
        + " in " + values + ") \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public int checkNumElements(String section) {
    int number = 0;

    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "SELECT ?number WHERE { \n";
    queryString += "  GRAPH welcome:CVInfo { \n";
    queryString += "    welcome:CV welcome:" + section + "Elements ?number . \n";
    queryString += "  } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        number = Integer.parseInt(solution.getBinding("number").getValue().stringValue());
      }
    }

    return number;
  }

  public Integer preprocessing(Integer sim) {
    IRI activeDIP = q.activeDipName_S();

    /* Retrieve the active dip */
    IRI activeIRI = q.activeDIP_S();

    /* Create user IRI */
    IRI graph = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "ProfileInfo");

    /* Create IRI for CVPurpose */
    IRI statusType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "CVPurpose");

    /* Check the purpose of the TCN e.g. "Create New" */
    String cvPurpose = q.getProfileValue(statusType, sim);

    /* Check if user just started or almost completed the scenario */
    if (activeDIP.stringValue().contains("EducationInformation")) {
      if (!cvPurpose.contentEquals("Create New")) {
        boolean flagEdu = q.checkElements("Education");

        if (flagEdu) {
          q.resetSlotsStatus(activeIRI, SlotLists.educationSlotsFull, "Completed");
        }
      }

      /* Clear temp graph when DIP is received to remove previous
       * objects that were not completed */
      IRI tempGraph = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "tempGraph");
      Utilities.connection.clear(tempGraph);

      sim = 3;
    } else if (activeDIP.stringValue().contains("OtherEducationInformation")) {
      if (!cvPurpose.contentEquals("Create New")) {
        boolean flagEdu = q.checkElements("Course");

        if (flagEdu) {
          q.resetSlotsStatus(activeIRI, SlotLists.courseSlotsFull, "Completed");
        }
      }

      /* Clear temp graph when DIP is received to remove previous
       * objects that were not completed */
      IRI tempGraph = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "tempGraph");
      Utilities.connection.clear(tempGraph);

      sim = 3;
    } else if (activeDIP.stringValue().contains("LanguageInformation")) {
      if (!cvPurpose.contentEquals("Create New")) {
        boolean flagEdu = q.checkElements("Language");

        if (flagEdu) {
          q.resetSlotsStatus(activeIRI, SlotLists.languageSlotsFull, "Completed");
        }
      }

      /* Clear temp graph when DIP is received to remove previous
       * objects that were not completed */
      IRI tempGraph = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "tempGraph");
      Utilities.connection.clear(tempGraph);

      sim = 3;
    } else if (activeDIP.stringValue().contains("EmploymentInformation")) {
      if (!cvPurpose.contentEquals("Create New")) {
        boolean flagEdu = q.checkElements("Employment");

        if (flagEdu) {
          q.resetSlotsStatus(activeIRI, SlotLists.employmentSlotsFull, "Completed");
        }
      }

      /* Clear temp graph when DIP is received to remove previous
       * objects that were not completed */
      IRI tempGraph = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "tempGraph");
      Utilities.connection.clear(tempGraph);

      sim = 3;
    } else if (activeDIP.stringValue().contains("OtherInformation")) {
      if (!cvPurpose.contentEquals("Create New")) {
        boolean flagEdu = q.checkElements("Other");

        if (flagEdu) {
          q.resetSlotsStatus(activeIRI, SlotLists.otherSlotsFull, "Completed");
        }
      }
    } else if (activeDIP.stringValue().contains("CreateCVDocument")) {
      if (cvPurpose.contentEquals("Create New")) {
        /* At this point the CV is considered Completed */
        q.removeTCNData("CVStatus", graph);
        q.insertTCNData("CVStatus", "Complete", graph);
      }
    } else if (activeDIP.stringValue().contains("PersonalInfo")) {
      if (cvPurpose.contentEquals("Create New")) {
        /* Copy personal information to CV graph */
        copyCVPersonal(SlotLists.personalSlots);
      } else {
        q.resetSlotsStatus(activeIRI, SlotLists.personalSlotsFull, "Completed");
        q.setBSlotStatus_I(activeIRI, "obtainIncludeFullAddress", "Completed", "Unknown", "1.0", null);
      }
    } else if (activeDIP.stringValue().contains("FAQ")) {
      String faqStatus = q.checkFAQStatus();

      if (faqStatus.contentEquals("internal")) {
        /* Update informAnswer slot with FAQ answer */
        this.updateFAQSlot(activeIRI, "informAnswer");
      } else if (faqStatus.contains("external")) {
        /* The following slots should be skipped by DMS if first answer satisfies the user */
        q.setInfoSlotStatus_I(activeIRI, "informAnswer", "Completed", "1.0");
        q.setBSlotStatus_I(activeIRI, "obtainSatisfaction", "Completed", "Unknown", "1.0", null);
        q.setInfoSlotStatus_I(activeIRI, "informFollowUpAnswer", "Completed","1.0");
        q.setBSlotStatus_I(activeIRI, "obtainFollowUpSatisfaction", "Completed",
            "Unknown", "1.0", null);
      }
    }

    return sim;
  }

  /* Function that sends notification to agent-core in s7 scenario */
  public String faqIdNotification2Agent() {
    String notification2Agent = "";
    notification2Agent = "@prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> .\n";

    String queryString;

    queryString = "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "SELECT ?o where { \n";
    queryString += "    welcome:ExternalFaqID welcome:hasValue ?o . \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    notification2Agent +=
        "welcome:KMSNotifies welcome:ExternalFaqID (";

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value o = solution.getBinding("o").getValue();

        notification2Agent += "\"" + o.stringValue() + "\" ";
      }
    }
    notification2Agent = notification2Agent.substring(0, notification2Agent.length() - 1);
    notification2Agent += ").";

    logger.info(notification2Agent);

    logger.info("Sending notification to agent: foundExternalFAQ ");

    /* Send data to the Agent-Core */
    util.sendData(notification2Agent, "agent-core", "foundExternalFAQ ", "text/turtle");

    return notification2Agent;
  }

  public void updateFAQSlot(Resource graph, String slot) {
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
    queryString += "       		rdf:subject welcome:faqAnswer; \n";
    queryString += "       		rdf:predicate welcome:hasValue; \n";
    queryString += "            rdf:object ?o ] . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH <" + graph.stringValue() + "> { \n";
    queryString += "        ?dip welcome:hasSlot ?slot . \n";
    queryString += "        ?slot welcome:hasInputRDFContents ?contents . \n";
    queryString += "    } \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#FAQInfo> { \n";
    queryString += "        welcome:faqAnswer welcome:hasValue ?o \n";
    queryString += "    } \n";
    queryString +=
        "    FILTER (STRAFTER(LCASE(STR(?slot)), \"#\") = \"" + slot.toLowerCase() + "\")  \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public boolean checkFollowUpFAQs(String anchor, IRI activeDIP, String slotName) throws Exception {
    HashMap<String, String> followUpFAQ = q.checkFollowUpFAQ(anchor);

    if (followUpFAQ != null) {
      Map.Entry<String, String> entry = followUpFAQ.entrySet().iterator().next();
      String id = entry.getKey();
      String answer = entry.getValue();

      /* Remove old graph before updating the information */
      IRI graph = Utilities.connection
          .getValueFactory()
          .createIRI(WELCOME.NAMESPACE, "FAQInfo");
      Utilities.connection.clear(graph);

      /* Initialize RDF builder */
      ModelBuilder temp_builder = util.getBuilder();
      temp_builder
          .namedGraph(graph)
          .subject("welcome:tcnQuestion")
          .add(WELCOME.HASVALUE, anchor)
          .subject("welcome:faqAnswer")
          .add(WELCOME.HASVALUE, answer)
          .subject("welcome:faq_id")
          .add(WELCOME.HASVALUE, id)
          .subject("welcome:faqStatus")
          .add(WELCOME.HASVALUE, "followup");

      /* We're done building, create our Model */
      Model model = temp_builder.build();

      /* Commit model to repository */
      util.commitModel(model);

      /* Retrieve the active dip */
      IRI activeIRI = q.activeDIP_S();

      /* Update informAnswer slot with FAQ answer */
      this.updateFAQSlot(activeIRI, "informFollowUpAnswer");

      return true;
    }

    return false;
  }

  public void successExitFAQ() {
    /* Remove old graph before updating the information */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "FAQInfo");
    Utilities.connection.clear(graph);

    /* Initialize RDF builder */
    ModelBuilder temp_builder = util.getBuilder();
    temp_builder
        .namedGraph(graph)
        .subject("welcome:faqStatus")
        .add(WELCOME.HASVALUE, "successExit");

    /* We're done building, create our Model */
    Model model = temp_builder.build();

    /* Commit model to repository */
    util.commitModel(model);
  }

  public void failureExitFAQ() {
    /* Remove old graph before updating the information */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "FAQInfo");
    Utilities.connection.clear(graph);

    /* Initialize RDF builder */
    ModelBuilder temp_builder = util.getBuilder();
    temp_builder
        .namedGraph(graph)
        .subject("welcome:faqStatus")
        .add(WELCOME.HASVALUE, "failureExit");

    /* We're done building, create our Model */
    Model model = temp_builder.build();

    /* Commit model to repository */
    util.commitModel(model);
  }

  public void clearFAQGraphs() {
    /* Remove old graph before updating the information */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "FAQInfo");
    Utilities.connection.clear(graph);

    /* Remove old graph before updating the information */
    IRI prevDip = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "prevDipInfo");
    Utilities.connection.clear(prevDip);
  }

  public void createPrevDipGraph(IRI activeDIP, String slotName) {
    /* Remove old graph before updating the information */
    IRI prevDip = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "prevDipInfo");
    Utilities.connection.clear(prevDip);

    /* Initialize RDF builder */
    ModelBuilder temp_builder = util.getBuilder();
    temp_builder
        .namedGraph(prevDip)
        .subject("welcome:faq_prev_active_DIP")
        .add(WELCOME.HASVALUE, activeDIP)
        .subject("welcome:faq_prev_active_slot")
        .add(WELCOME.HASVALUE, slotName);

    /* We're done building, create our Model */
    Model model = temp_builder.build();

    /* Commit model to repository */
    util.commitModel(model);
  }

  public boolean checkInternalFAQs(String anchor, IRI activeDIP, String slotName) throws Exception {
    HashMap<String, String> internalFAQ = q.checkInternalFAQ(anchor);

    if (internalFAQ != null) {
      Map.Entry<String, String> entry = internalFAQ.entrySet().iterator().next();
      String id = entry.getKey();
      String answer = entry.getValue();

      /* Remove old graph before updating the information */
      IRI graph = Utilities.connection
          .getValueFactory()
          .createIRI(WELCOME.NAMESPACE, "FAQInfo");
      Utilities.connection.clear(graph);

      /* Initialize RDF builder */
      ModelBuilder temp_builder = util.getBuilder();
      temp_builder
          .namedGraph(graph)
          .subject("welcome:tcnQuestion")
          .add(WELCOME.HASVALUE, anchor)
          .subject("welcome:faqAnswer")
          .add(WELCOME.HASVALUE, answer)
          .subject("welcome:faq_id")
          .add(WELCOME.HASVALUE, id)
          .subject("welcome:faqStatus")
          .add(WELCOME.HASVALUE, "internal");

      /* We're done building, create our Model */
      Model model = temp_builder.build();

      /* Commit model to repository */
      util.commitModel(model);

      return true;
    }

    return false;
  }

  public boolean checkExternalFAQs(String anchor, IRI activeDIP, String slotName, Boolean flag) throws Exception {
    HashMap<String, String> externalFAQ = q.checkExternalFAQ(anchor);

    if (externalFAQ != null) {
      /* Remove old graph before updating the information */
      IRI graph = Utilities.connection
          .getValueFactory()
          .createIRI(WELCOME.NAMESPACE, "FAQInfo");
      Utilities.connection.clear(graph);

      /* Initialize RDF builder */
      ModelBuilder temp_builder = util.getBuilder();
      temp_builder
          .namedGraph(graph)
          .subject("welcome:tcnQuestion")
          .add(WELCOME.HASVALUE, anchor);

      if (flag) {
        temp_builder
            .subject("welcome:faqStatus")
            .add(WELCOME.HASVALUE, "externalGenerate");
      } else {
        temp_builder
            .subject("welcome:faqStatus")
            .add(WELCOME.HASVALUE, "external");
      }

      Iterator it = externalFAQ.entrySet().iterator();

      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        String key = (String) pair.getKey();

        temp_builder
            .subject("welcome:ExternalFaqID")
            .add(WELCOME.HASVALUE, key);
      }

      /* We're done building, create our Model */
      Model model = temp_builder.build();

      /* Commit model to repository */
      util.commitModel(model);

      return true;
    }

    return false;
  }

  public String checkMultipleChoiceSlots(String anchor, String slotName, String topicName)
      throws Exception {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
    queryString += "SELECT ?label where { \n";
    queryString += "	GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#cvOntology> { \n";
    queryString += "        ?s rdfs:subClassOf welcome:" + slotName + " . \n";
    queryString += "        BIND(STRAFTER(STR(?s), \"#\") as ?label) \n";
    queryString += "  } \n";
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

          String s = solution.getBinding("label").getValue().stringValue().replace("_", " ");

          hmap.put("1", s);

          if (result.hasNext()) {
            ids += "\"1\",\n";
            corpus += "\"" + s + "\",\n";
          } else {
            ids += "\"1\"\n";
            corpus += "\"" + s + "\"\n";
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

      String response = util.getSimilarSlots(Utilities.dispatcherURL, finalMessage, topicName);

      String[] s1 = response.split("@");
      String answer = s1[0].replace("\n", "");

      if (!answer.contentEquals("")) {
        return answer;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
}
