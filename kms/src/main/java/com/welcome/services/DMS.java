package com.welcome.services;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.SlotLists;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import java.util.Date;
import java.util.List;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DMS {

  Logger logger = LoggerFactory.getLogger(DMS.class);

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries query = new Queries();
  AGENT agent = new AGENT();

  /**
   * @param input
   */
  public void wrapperDMS(String input) {
    /* Retrieve the active slot */
    List<IRI> activeSlot = query.activeSlot_S();

    if (activeSlot.size() == 0) {
      /* Create RDF Model */
      jsonldToRdf(util.parseJSONLD(input));
    }
  }

  /**
   * @param dmsObject
   */
  public void jsonldToRdf(JSONObject dmsObject) {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Get User IRI and Session IRI */
    IRI[] iri = util.getUserSession();
    IRI sessionObject = iri[1];

    /* Create unique timestamp and date for system turn */
    long timestamp = System.currentTimeMillis();
    Date date = new Date(timestamp);

    /* Create IRI for system turn */
    IRI turnObject = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "SystemTurn-" + timestamp);

    /* Add system turn to builder */
    builder
        .namedGraph(sessionObject)
        .subject(sessionObject)
        .add(WELCOME.HASTURN, turnObject)
        .namedGraph(turnObject)
        .subject(turnObject)
        .add(RDF.TYPE, WELCOME.TURN)
        .add(RDF.TYPE, WELCOME.SYSTEMTURN)
        .add(WELCOME.ID, timestamp)
        .add(WELCOME.TIMESTAMP, date);

    /* Check previous user turn */
    IRI temp = query.latestUserTurn_S();
    if (temp != null) {
      builder
          .add(WELCOME.HASPREVTURN, temp);
    }

    /* Get object of speechActs */
    JSONArray speechActs = (JSONArray) dmsObject
        .get("welcome:hasSpeechActs");

    /* Initialize counter */
    int counter = 1;

    /* Clear SlotInfo graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "SlotInfo");
    Utilities.connection.clear(graph);

    /* This flag remains true if the DMS input contains only SystemInfo slots */
    Utilities.flag = true;

    /* String to capture the slot name */
    String typeWithoutPrefix = "";

    /* Iterate through speechActs */
    for (Object o : speechActs) {
      /* Create object and get slot type */
      JSONObject speechAct = (JSONObject) o;
      JSONObject slot = ((JSONObject) speechAct.get("welcome:hasSlot"));

      if (slot == null) {
        continue;
      }

      String sp = (String) slot.get("@id");

      /* Get type name from IRI */
      String[] parts = sp.split(":");
      typeWithoutPrefix = parts[1];

      String slotType = (String) slot.get("rdf:type");

      /* Create IRI for slot object */
      IRI typeObject = Utilities.f
          .createIRI(WELCOME.NAMESPACE, typeWithoutPrefix);

      /* Create IRI for speech act */
      IRI actObject = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "S-SpeechAct-" + timestamp + "-" + counter);

      /* Add speech act to builder */
      builder
          .namedGraph("welcome:SystemTurn-" + timestamp)
          .subject(turnObject)
          .add(WELCOME.HASCONTAINER, actObject)
          .subject(actObject)
          .add(WELCOME.HASCONTAINERID, timestamp + "-" + counter)
          .add(RDF.TYPE, WELCOME.CONTAINER)
          .add(WELCOME.INVOLVESSLOT, typeObject);

      /* Update SlotInfo graph */
      builder
          .namedGraph("welcome:SlotInfo")
          .subject(typeObject)
          .add(WELCOME.ISACTIVESLOT, true);

      /* Increment counter */
      counter += 1;

//      if (slotType.contains("Info")) {
//        /* Retrieve the active dip */
//        IRI activeDIP = query.activeDIP_S();
//
//        /* In case of SystemInfo slots appearing in the dialogue just mark their status as completed */
//        query.setInfoSlotStatus_I(activeDIP, typeWithoutPrefix, "Completed", "1");
//      }
    }

    /* We're done building, create a Model */
    Model model = builder.build();

    logger.info("(REPO) Adding DMS input to the repository.");

    /* Commit model to repository */
    util.commitModel(model);

    switch (typeWithoutPrefix) {
      case "obtainPersonalInfoConfirmation":
        notification2Agent("PersonalInfo");
        break;
      case "obtainEducationalInformationConfirmation":
        notification2Agent("EducationInformation");
        break;
      case "obtainMoreEducationalInformationConfirmation":
        notification2Agent("OtherEducationInformation");
        break;
      case "obtainLanguageInformationConfirmation":
        notification2Agent("LanguageInformation");
        break;
      case "obtainEmploymentInformationConfirmation":
        notification2Agent("EmploymentInformation");
        break;
      case "obtainSkillInformationConfirmation":
      case "obtainSkills":
      case "informSkillsSectionIntro":
        notification2Agent("SkillInformation");
        break;
      case "obtainOtherInformationConfirmation":
        notification2Agent("OtherInformation");
        break;
    }
  }

  /* Function that sends notification to agent-core in s7 scenario */
  public void notification2Agent(String value) {
    String notification2Agent = "";
    notification2Agent = "@prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> .\n";
    notification2Agent += "welcome:KMSNotifies welcome:" + value + " \"true\" .  \n";

    logger.info(notification2Agent);

    logger.info("Sending notification to agent: KMSNotificationS7");

    /* Send data to the Agent-Core */
    util.sendData(notification2Agent, "agent-core", "KMSNotificationS7", "text/turtle");
  }
}
