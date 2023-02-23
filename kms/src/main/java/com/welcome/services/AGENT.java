package com.welcome.services;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.SlotLists;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import com.welcome.scenarios.CV;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AGENT {

  Logger logger = LoggerFactory.getLogger(AGENT.class);

  /* Boolean value in case of simulation */
  Integer sim;

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries q = new Queries();
  CV cv = new CV();

  /**
   * Wrapper function for handling new DIPS.
   *
   * @param input Refers to the input received from the endpoint.
   * @return
   */
  public String wrapperDIP(String input, long start) throws IOException {
    logger.info(input);

    /* Check if we are running a simulation */
    sim = q.checkSimulation();

    /* Load DIP */
    loadDIP(input);

    /* Get the scenario name selected by user */
    String scenario = q.scenarioSelection_S();

    /* Create IRI for CVPurpose */
    IRI section = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "CVSection");

    /* Check the purpose of the TCN e.g. "Create New" */
    String cvSection = q.getProfileValue(section, 0);

    if (scenario.toLowerCase().contains("cv creation")) {
      /* New code bloc that checks if user selected a particular CV section */
      boolean flag = false;
      if (!cvSection.contentEquals("None")) {
        /* Get DIP IRI */
        IRI activeDIP = q.activeDipName_S();

        /* Extract DIP name */
        String dipName = util.splitIRI(activeDIP);

        IRI graph = Utilities.connection
            .getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "CVInfo");

        if (!cvSection.contentEquals(dipName) && SlotLists.cvSections.contains(dipName)) {
          /* Check CV Purpose */
          IRI statusType = Utilities.f
              .createIRI(WELCOME.NAMESPACE, "CVStatus");

          String cvStatus = q.getProfileValue(statusType, 0);

          if (cvStatus.contentEquals("None")) {
            q.initProfile(scenario, 2);
            cv.copyCVPersonal(SlotLists.personalSlots);

            /* Add CV Purpose in CVInfo graph */
            q.removeTCNData("CVPurpose", graph);
            q.insertTCNData("CVPurpose", "Create New", graph);
          } else if (cvStatus.contentEquals("Incomplete")) {
            /* Add CV Purpose in CVInfo graph */
            q.removeTCNData("CVPurpose", graph);
            q.insertTCNData("CVPurpose", "Continue Incomplete", graph);
          } else {
            /* Add CV Purpose in CVInfo graph */
            q.removeTCNData("CVPurpose", graph);
            q.insertTCNData("CVPurpose", "Review Completed", graph);
          }

          flag = true;
        }
      }

      if (flag) {
        q.markAllCompleted();
        q.markAllDemandTrue();

        return (exportActiveDIP());
      } else {
        /* Checking conditions */
        sim = cv.preprocessing(sim);

        /* Update DIP based on existing knowledge */
        cv.updateCV(sim);

        /* Check if any slots require RDF triples */
        checkInformSlots();

        /* Check which slots required additional triples
         * for the NLG component.
         */
        checkDMSTemplates(2);

        /* Check if classes of the DIP depend on others */
        checkDependencies(2);

        /* Check if user has a CV */
        boolean status = q.checkIfPopulated("CVStatus");

        logger.info(String.valueOf(status));

        String faqStatus = q.checkFAQStatus();
        if (faqStatus != null && faqStatus.contentEquals("externalGenerate")) {
          return (cv.faqIdNotification2Agent());
        } else {
          /* Export active DIP */
          return (exportActiveDIP());
        }
      }
    } else {
      /* Reset simulation concern in CARITAS scenario */
      q.resetSimulationConcern();

      /* Reset asylum appointment in PRAKSIS scenario */
      q.resetAppointmentConcern();

      /* Reset value in S2a scenario */
      q.resetAsylumKnowledge();

      /* Reset agent role in PRAKSIS scenario */
      q.resetAgentRole();

      /* Reset Schooling Topics */
      if (scenario.toLowerCase().contains("schooling")) {
        q.resetSubTopics("schooling");
      }

      /* Reset Health Topics */
      if (scenario.toLowerCase().contains("health")) {
        q.resetSubTopics("health");
      }

      if (sim == 1) {
        /* Copy Simulation Slots */
        IRI activeDIP = q.activeDipName_S();

        if (activeDIP.stringValue().contains("Opening")) {
          q.copySimulationPersonal(SlotLists.simulationSlots);
        }

        /* Update DIP based on existing knowledge */
        updateDIPSim();

        /* Check if any slots require RDF triples */
        checkInformSlots();

        /* Check which slots required additional triples
         * for the NLG component.
         */
        checkDMSTemplatesSim();

        /* Check if classes of the DIP depend on others */
        checkDependenciesSim();
      } else if (sim == 0) {
        /* Update DIP based on existing knowledge */
        updateDIP();

        /* Check if any slots require RDF triples */
        checkInformSlots();

        /* Check which slots required additional triples
         * for the NLG component.
         */
        checkDMSTemplates(0);

        /* Check if classes of the DIP depend on others */
        checkDependencies(0);
      }

      long finish = System.currentTimeMillis();
      long timeElapsed = finish - start;

      logger.info("DIP: Time Elapsed: " + timeElapsed);

      /* Export active DIP */
      return (exportActiveDIP());
    }
  }

  /**
   * Loads a DIP file into the database.
   *
   * @param input Refers to the new DIP input.
   */
  public void loadDIP(String input) throws IOException {
    /* Convert String to InputStream */
    InputStream stream = IOUtils
        .toInputStream(input, StandardCharsets.UTF_8);

    /* Load stream to model */
    Model dipModel = Rio.parse(stream, "", RDFFormat.TURTLE);

    /* Create unique timestamp and date for DIP */
    long timestamp = System.currentTimeMillis();

    /* Create IRI for DIP */
    IRI context = Utilities
        .f.createIRI(WELCOME.NAMESPACE, "DIP-" + timestamp);

    /* Commit DIP to named graph */
    util.commitModel(dipModel, context);

    /* Assign ID to DIP */
    q.assignID_I(context, timestamp);

    /* Declare DIP as active */
    setDIPStatus(context);

    /* Assign it to active Session */
    assignToSession(context);

    /* Link DIP to Dialogue Turn */
    linkToTurn(context);

    /* Update Dialogue Status */
    q.updateDialogueStatus();
  }

  /**
   * Update DIP status to active.
   *
   * @param context Refers to the context of the DIP to be updated.
   */
  public void setDIPStatus(Resource context) {
    /* Get the context of the active DIP */
    IRI prevActiveDIP = q.activeDIP_S();

    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    /* Clear DipInfo graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "DipInfo");
    Utilities.connection.clear(graph);

    /* Update DipInfo graph */
    builder
        .namedGraph("welcome:DipInfo")
        .subject("welcome:activeDIP")
        .add(WELCOME.BELONGSTOGRAPH, context);

    if (prevActiveDIP != null) {
      /* Update DipInfo graph */
      builder
          .namedGraph("welcome:DipInfo")
          .subject("welcome:prevActiveDIP")
          .add(WELCOME.BELONGSTOGRAPH, prevActiveDIP);
    }

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Updating active DIP.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  /**
   * Assigns a new DIP into the active session.
   *
   * @param context Refers to the context of the DIP
   */
  public void assignToSession(Resource context) {
    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    /* Get active session */
    IRI activeSession = q.activeDialogueSession_S();

    /* Update DipInfo graph */
    builder
        .namedGraph(activeSession)
        .subject(activeSession)
        .add(WELCOME.INVOLVESDIP, context);

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Assign DIP to Session.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  /**
   * Links a DIP to the last turn of the current session. i.e. after which turn the DIP has been
   * received.
   *
   * @param context
   */
  public void linkToTurn(Resource context) {
    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    /* Get latest dialogue turn */
    IRI turnIRI = q.latestTurn_S();

    if (turnIRI != null) {
      /* Update DipInfo graph */
      builder
          .namedGraph(turnIRI)
          .subject(turnIRI)
          .add(WELCOME.TRIGGERSDIP, context);

      /* We're done building, create our Model */
      Model model = builder.build();

      logger.info("(REPO) Linking DIP to Dialogue Turn.");

      /* Commit model to repository */
      util.commitModel(model);
    }
  }

  public String exportGraph(String graphName) {
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + graphName);

    /* Write triples into a stream */
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    RDFHandler handler = Rio.createWriter(RDFFormat.JSONLD, ba);

    // RDFHandler handler = new TurtleWriter(ba);

    Utilities.connection.export(handler, graph);

    /* Create a string containing all triples */
    String finalString = ba.toString();

    logger.info(finalString);

    Utilities.connection.clear(graph);

    /* Send RDF triples to agent-core */
    util.sendData(finalString, "agent-core", "agentsCHC", "application/ld+json");

    return finalString;
  }

  /**
   * Returns all the triples of the active DIP
   */
  public String exportActiveDIP() {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    /* Clear DipInfo graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "tempDIP");

    Utilities.connection.clear(graph);

    /* Create a temp graph */
    q.createTempGraph_I(activeDIP);

    /* Write triples into a stream */
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    RDFHandler handler = new TurtleWriter(ba);

    Utilities.connection.export(handler, graph);

    /* Create a string containing all triples */
    String finalString = ba.toString();

    /* Print triples to console */
    logger.info(finalString);

    /* Write triples into a stream */
    ByteArrayOutputStream ba_temp = new ByteArrayOutputStream();
    RDFHandler handler_temp = new TurtleWriter(ba_temp);

    Utilities.connection.export(handler_temp, activeDIP);

    /* Create a string containing all triples */
    String tempString = ba_temp.toString();

    /* Print triples to console */
    logger.info(tempString);

    logger.info("Sending notification to agent: receiveSpeechAct");

    /* Send RDF triples to agent-core */
    util.sendData(finalString, "agent-core", "receiveSpeechAct", "text/turtle");

    return finalString;
  }

  public String exportActiveDIP(IRI activeDIP) {
//    /* Get the context of the active DIP */
//    IRI activeDIP = q.activeDIP_S();

    /* Clear DipInfo graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "tempDIP");

    Utilities.connection.clear(graph);

    /* Create a temp graph */
    q.createTempGraph_I(activeDIP);

    /* Write triples into a stream */
    ByteArrayOutputStream ba = new ByteArrayOutputStream();
    RDFHandler handler = new TurtleWriter(ba);

    Utilities.connection.export(handler, graph);

    /* Create a string containing all triples */
    String finalString = ba.toString();

    /* Print triples to console */
    logger.info(finalString);

    /* Write triples into a stream */
    ByteArrayOutputStream ba_temp = new ByteArrayOutputStream();
    RDFHandler handler_temp = new TurtleWriter(ba_temp);

    Utilities.connection.export(handler_temp, activeDIP);

    /* Create a string containing all triples */
    String tempString = ba_temp.toString();

    /* Print triples to console */
    logger.info(tempString);

    logger.info("Sending notification to agent: receiveSpeechAct");

    /* Send RDF triples to agent-core */
    util.sendData(finalString, "agent-core", "receiveSpeechAct", "text/turtle");

    return finalString;
  }

  /**
   * Updates DIP with existing knowledge.
   */
  public void updateDIP() {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    /* Update DIP using existing knowledge */
    q.updateDIP_I(activeDIP);
  }

  /**
   * Updates DIP with existing knowledge.
   */
  public void updateDIPSim() {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    /* Update DIP using existing knowledge */
    q.updateDIPSim_I(activeDIP);
  }

  /**
   * @param input
   */
  public String runAskQuery(String input) throws IOException {
    /* Convert String to InputStream */
    InputStream stream = IOUtils
        .toInputStream(input, StandardCharsets.UTF_8);

    /* Load stream to model */
    Model model = Rio.parse(stream, "", RDFFormat.TURTLE);

    // iterate over every statement in the Model
    logger.info("SPARQL query has been detected. Running query...");

    String askResult = "";

    for (Statement statement : model) {
      String query = statement.getObject()
          .toString()
          .replaceAll("[\\t]+", "");

      query = query.startsWith("\"") ? query.substring(1) : query;
      query = query.endsWith("\"") ? query.substring(0, query.length() - 1) : query;

      BooleanQuery booleanQuery = Utilities.connection
          .prepareBooleanQuery(QueryLanguage.SPARQL, query);

      boolean results = booleanQuery.evaluate();

      askResult = "@prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> . \n";

      if (results) {
        askResult += "welcome:SPARQL_query welcome:hasAskResult \"true\" . \n";
      } else {
        askResult += "welcome:SPARQL_query welcome:hasAskResult \"false\" . \n";
      }
    }

    logger.info("Sending notification to agent: receiveSpeechAct");

    // Send data to the Agent-Core
    util.sendData(askResult, "agent-core", "receiveSpeechAct", "text/turtle");

    return (askResult);
  }

  public void checkDMSTemplates(Integer sim) {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    q.updateDMSTemplates(activeDIP, sim);
  }

  public void checkDMSTemplatesSim() {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    q.updateDMSTemplatesSim(activeDIP);
  }

  public void checkDependencies(Integer sim) {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    q.updateDependencies(activeDIP, sim);
  }

  public void checkSlotDependencies(Integer sim) {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    q.updateSlotDependencies(activeDIP, sim);
  }

  public void checkDependenciesSim() {
    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    q.updateDependenciesSim(activeDIP);
  }

  /**
   * Check inform slots of active DIP and apply reification to the additional triples that are to be
   * assigned to the inform slots.
   */
  public void checkInformSlots() {
        /* The following 3 lists contain the slots of the DIPs that require extra information from the ontology. We
           check if those slots appear and then update property hasInputRDFContents with the additional triples after
           we apply reification to each triple e.g.:
               _:node_1 a rdf:Statement;
               rdf:subject ?subject;
               rdf:predicate ?predicate;
               rdf:object ?object .
         */

    /* Get the context of the active DIP */
    IRI activeDIP = q.activeDIP_S();

    /* Create temp graph */
    IRI tempGraph = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "TempGraph");

    /* Get slots name */
    String dipName = util.splitIRI(q.getActiveDIPName(activeDIP));

    if (dipName.contains("InformSkypeUser")) {
      if (q.calcOfficePRAKSIS() != null) {
        for (String temp : SlotLists.informSkype) {
          if (temp.contentEquals("informSkypeID")) {
            q.updateInformSkypeId_I(tempGraph, q.calcOfficePRAKSIS());

            /* Perform reification */
            q.reification(activeDIP, tempGraph, temp);

            /* Clear tempGraph graph */
            Utilities.connection.clear(tempGraph);
          } else if (temp.contentEquals("informTimeSlot")) {
            q.updateInformSkypeSlot_I(tempGraph, q.calcOfficePRAKSIS());

            /* Perform reification */
            q.reification(activeDIP, tempGraph, temp);

            /* Clear tempGraph graph */
            Utilities.connection.clear(tempGraph);
          }
        }
      }
    } else if (dipName.contains("InformContactPRAKSIS")) {
      if (q.updateInformContactPRAKSIS_S() != null) {
        IRI closestPRAKSIS = q.updateInformContactPRAKSIS_S();
        for (String temp : SlotLists.informPraksis) {
          if (temp.contentEquals("informTelephoneNumber")) {
            q.updateInformTelephone_I(tempGraph, temp, closestPRAKSIS);

            /* Perform reification */
            q.reification(activeDIP, tempGraph, temp);

            /* Clear tempGraph graph */
            Utilities.connection.clear(tempGraph);
          } else if (temp.contentEquals("informAddress")) {
            q.updateInformAddress_I(tempGraph, temp, closestPRAKSIS);

            /* Perform reification */
            q.reification(activeDIP, tempGraph, temp);

            /* Clear tempGraph graph */
            Utilities.connection.clear(tempGraph);
          } else if (temp.contentEquals("informEmail")) {
            q.updateInformEmail_I(tempGraph, temp, closestPRAKSIS);

            /* Perform reification */
            q.reification(activeDIP, tempGraph, temp);

            /* Clear tempGraph graph */
            Utilities.connection.clear(tempGraph);
          }
        }
      }
    } else if (dipName.contains("InformRegistrationService")) {
      if (q.updateInformRegistrationService_S() != null) {
        IRI closestRegistration = q.updateInformRegistrationService_S();
        for (String temp : SlotLists.informRegistration) {
          if (temp.contentEquals("informOfficeAddress")) {

          } else if (temp.contentEquals("informOfficeHours")) {
            q.updateInformOfficeHours_I(tempGraph, closestRegistration);

            /* Perform reification */
            q.reification(activeDIP, tempGraph, temp);

            /* Clear tempGraph graph */
            Utilities.connection.clear(tempGraph);
          }
        }
      }
    } else if (dipName.contains("MakeAppointment")) {
      q.updateProposeTimeSlot(activeDIP, false, "");
    }

    /* Update DIP based on annotation properties */
    q.updatedSystemInfo(activeDIP);

    q.updateConfirmationRequest(activeDIP);
  }

  public String wrapperCHC(String input, long start) {
    // find location preferences
    List<String> locList = q.getLocationPreferences();

    // call kbs
    List<String> agentList = util.getRelevantAgents(locList);

    if (agentList.size() > 0) {
      // call wpm
      JSONArray arr = util.wakeUpRelevantAgents(agentList);

      if (!arr.isEmpty()) {
        // load wpm response to to repo
        util.loadAgentsInLAKR(arr);

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        logger.info("CHC: Time Elapsed: " + timeElapsed);

        return this.exportGraph("activeAgents");
      } else {
        logger.warn("WPM was not able to wake any agent!");

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        logger.info("CHC: Time Elapsed: " + timeElapsed);

        return "WPM was not able to wake any agent!";
      }
    } else {
      logger.warn("No agents with similar location preference were found!");

      long finish = System.currentTimeMillis();
      long timeElapsed = finish - start;

      logger.info("CHC: Time Elapsed: " + timeElapsed);

      return "No agents with similar location preference were found!";
    }
  }

  public void parseAgentNotification(String agentNotification, long start) {
    logger.info(agentNotification);

    String parsed = StringUtils.substringAfter(agentNotification, "informed> <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#")
        .replace(".", "")
        .replace(">", "")
        .trim();

    // String topic = StringUtils.substringAfter(parsed, ":").replace("Requested", "Informed");
    String topic = parsed.replace("Requested", "Informed");

    String scenario = q.scenarioSelection_S();

    IRI context;
    if (scenario.toLowerCase().contains("schooling")) {
      context = Utilities.connection
          .getValueFactory()
          .createIRI(WELCOME.NAMESPACE + "S8TempGraph");
    } else {
      context = Utilities.connection
          .getValueFactory()
          .createIRI(WELCOME.NAMESPACE + "S5TempGraph");
    }

    /* Retrieve the active dip */
    IRI scenarioIntroDIP = q.previouslyActiveDIP_S(context);

    q.setBSlotStatus_I(scenarioIntroDIP, topic, "Completed", true, "1.0", null);

    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);
    
    if (scenario.toLowerCase().contains("schooling")) {
      q.updateTopicStatus(builder, topic, "Completed", "schooling");
    } else {
      q.updateTopicStatus(builder, topic, "Completed", "health");
    }

    /* We're done building, create our Model */
    Model model = builder.build();

    /* Commit model to repository */
    util.commitModel(model);

    Boolean check1 = q.checkPendingTopics(scenarioIntroDIP);
    Boolean check2 = q.checkOpenTopics(scenarioIntroDIP);
    if (check1) {
      /* Declare DIP as active */
      setDIPStatus(scenarioIntroDIP);

      /* Export active DIP */
      this.exportActiveDIP(scenarioIntroDIP);
    } else if (check2) {
      q.setInfoSlotStatus_I(scenarioIntroDIP, "informScenarioIntroduction", "Pending", "0.0");
      q.setBSlotStatus_I(scenarioIntroDIP, "obtainSubtopic", "Pending", "Unknown", "0.0", null);

      // this.exportActiveDIP();

      /* Update DIP based on annotation properties */
      q.updatedSystemInfoV2(scenarioIntroDIP);

      // this.exportActiveDIP();

      /* Check which slots required additional triples
       * for the NLG component.
       */
      checkDMSTemplates(0);

      // this.exportActiveDIP();

      /* Check if classes of the DIP depend on others */
      checkDependencies(0);

      /* Declare DIP as active */
      setDIPStatus(scenarioIntroDIP);

      this.exportActiveDIP(scenarioIntroDIP);
    } else {
      /* Declare DIP as active */
      setDIPStatus(scenarioIntroDIP);

      /* Export active DIP */
      this.exportActiveDIP(scenarioIntroDIP);
    }

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("NOT_TOPIC: Time Elapsed: " + timeElapsed);
  }
}
