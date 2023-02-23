package com.welcome.services;

import static com.welcome.auxiliary.WordsToNumbers.convertTextualNumbersInDocument;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.SlotLists;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.PROCESS;
import com.welcome.ontologies.PROFILE;
import com.welcome.ontologies.SERVICE;
import com.welcome.ontologies.WELCOME;
import com.welcome.scenarios.CV;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LAS {

  Logger logger = LoggerFactory.getLogger(LAS.class);

  /* Initialize map to store entity label and id for later */
  HashMap<String, String> entityMap = new HashMap<>();

  /* Declare temp counters */
  int cnt2;
  int cnt3;

  /* Boolean value in case of commit */
  boolean commit;

  /* Boolean value in case of simulation */
  Integer sim;

  String fullTranscription;
  String babelNetIDs;

  /* String value to hold scenario selection */
  String scenario;

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries query = new Queries();
  AGENT agent = new AGENT();
  WPM wpm = new WPM();
  CV cv = new CV();

  public static boolean validateJavaDate(String strDate) {
    /* Check if date is 'null' */
    if (strDate.trim().equals("")) {
      return true;
    }
    /* Date is not 'null' */
    else {
      /*
       * Set preferred date format,
       * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
      SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM-dd");
      sdfrmt.setLenient(false);
      /* Create Date object
       * parse the string into date
       */
      try {
        Date javaDate = sdfrmt.parse(strDate);
      }
      /* Date format is invalid */ catch (ParseException e) {
        return false;
      }
      /* Return true if date format is valid */
      return true;
    }
  }

  public static boolean validateJavaDateYM(String strDate) {
    /* Check if date is 'null' */
    if (strDate.trim().equals("")) {
      return true;
    }
    /* Date is not 'null' */
    else {
      /*
       * Set preferred date format,
       * For example MM-dd-yyyy, MM.dd.yyyy,dd.MM.yyyy etc.*/
      SimpleDateFormat sdfrmt = new SimpleDateFormat("yyyy-MM");
      sdfrmt.setLenient(false);
      /* Create Date object
       * parse the string into date
       */
      try {
        Date javaDate = sdfrmt.parse(strDate);
      }
      /* Date format is invalid */ catch (ParseException e) {
        return false;
      }
      /* Return true if date format is valid */
      return true;
    }
  }

  /**
   * Wrapper function for handling new LAS input.
   *
   * @param input Refers to the input received from the endpoint.
   * @return
   */
  public void wrapperLAS(String input, String form) throws Exception {
    logger.info(input);

    /* Check if we are running a simulation */
    sim = query.checkSimulation();

    /* Get name of active DIP */
    IRI activeDIP = query.activeDipName_S();

    /* In case we are working on the following DIP
     * information provided by TCN will be added in
     * a temporary graph and then passed to the main CV
     * as an object.
     */
    if (activeDIP.stringValue().contains("EducationInformation")
        || activeDIP.stringValue().contains("LanguageInformation")
        || activeDIP.stringValue().contains("OtherEducationInformation")
        || activeDIP.stringValue().contains("EmploymentInformation")) {
      sim = 3;
    }

    /* Create RDF Model */
    jsonToRdf(util.parseJSONLD(input), form);

    String faqStatus = query.checkFAQStatus();

    if (faqStatus != null
        && (faqStatus.contentEquals("internal") || faqStatus.contentEquals("externalGenerate"))
        && !activeDIP.stringValue().contains("FAQ")) {
      /* Send notification to agent */
      faqNotification2Agent();
    } else if (faqStatus != null && faqStatus.contentEquals("external")
        && !activeDIP.stringValue().contains("FAQ")) {
      /* Send notification to agent with FAQ ids */
      faqIdNotification2Agent();
    } else if (faqStatus != null && faqStatus.contentEquals("successExit")) {
      /* Retrieve the active dip */
      IRI activeIRI = query.prevActiveDIP_S();

      /* Declare DIP as active */
      agent.setDIPStatus(activeIRI);

      String slotName = query.prevActiveSlot_S();

      /* Get ontology type */
      IRI ontoType = query.getOntologyType_S(activeIRI, slotName);

      cv.clearFAQGraphs();

      /* Update the status of the slot */
      query.setSlotStatus_I(activeIRI, slotName, "Pending", "Unknown", "0.0", ontoType);

      /* Export active DIP */
      agent.exportActiveDIP();
    } else if (faqStatus != null && faqStatus.contentEquals("failureExit")) {
      /* Retrieve the active dip */
      IRI activeIRI = query.prevActiveDIP_S();

      /* Declare DIP as active */
      agent.setDIPStatus(activeIRI);

      String slotName = query.prevActiveSlot_S();

      /* Get ontology type */
      IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

      cv.clearFAQGraphs();

      /* Update the status of the slot */
      query.setSlotStatus_I(activeIRI, slotName, "Undefined", "Unknown", "1.0", ontoType);

      /* Export active DIP */
      agent.exportActiveDIP();
    } else {
      if (sim == 1) {
        /* Update DIP based on existing knowledge */
        agent.updateDIPSim();

        /* Check which slots required additional
         * triples for the NLG component.
         */
        agent.checkDMSTemplatesSim();
      } else if (sim == 0) {
        /* Update DIP based on existing knowledge */
        agent.updateDIP();

        /* Check which slots required additional
         * triples for the NLG component.
         */
        agent.checkDMSTemplates(0);
      } else {
        cv.updateCV(sim);

        agent.checkDMSTemplates(2);

        agent.checkSlotDependencies(2);
      }

      /* Export active DIP */
      agent.exportActiveDIP();
    }
  }

  public void wrapperSystemLAS(String input, String form) throws ParseException {
    logger.info(input);

    /* Create RDF Model */
    jsonToRdfSystem(util.parseJSONLD(input), form);
  }

  public void jsonToRdfSystem(JSONObject lasSystemObject, String form) throws ParseException {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Check previous system turn */
    IRI temp = query.latestSystemTurn_S();

    /* Check previous system turn */
    Long timestamp = query.latestSystemTurnID_S();

    /* Get object of data */
    JSONObject data = (JSONObject) lasSystemObject.get("data");

    /* Get array of entities */
    JSONArray entities = (JSONArray) data.get("entities");

    /* Init global variable to an empty string */
    babelNetIDs = "";

    /* Convert entities to RDF model */
    entitiesToRDF(entities, timestamp, temp, builder, false, form, null);

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Adding LAS input to the repository (System Turn).");

    /* Commit model to repository */
    util.commitModel(model);
  }

  /**
   * @param lasObject
   * @return
   */
  public void jsonToRdf(JSONObject lasObject, String form) throws Exception {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Create unique timestamp and date for user turn */
    long timestamp = System.currentTimeMillis();
    Date date = new Date(timestamp);

    /* Get User IRI and Session IRI */
    IRI[] iri = util.getUserSession();

    IRI sessionObject = iri[1];

    /* Create IRI */
    IRI turnObject = Utilities.f.
        createIRI(WELCOME.NAMESPACE, "UserTurn-" + timestamp);

    /* Add user turn to builder */
    builder
        .namedGraph(sessionObject)
        .subject(sessionObject)
        .add(WELCOME.HASTURN, turnObject)
        .namedGraph(turnObject)
        .subject(turnObject)
        .add(RDF.TYPE, WELCOME.TURN)
        .add(RDF.TYPE, WELCOME.USERTURN)
        .add(WELCOME.ID, timestamp)
        .add(WELCOME.TIMESTAMP, date);

    /* Check previous system turn */
    IRI temp = query.latestSystemTurn_S();
    if (temp != null) {
      builder
          .add(WELCOME.HASPREVTURN, temp);
    }

    /* Get object of data */
    JSONObject data = (JSONObject) lasObject.get("data");

    /* Step 1: Get array of speech acts */
    JSONArray speechActs = (JSONArray) data.get("speechActs");

    /* Convert speech acts to RDF model */
    speechActsToRDF(speechActs, timestamp, turnObject, builder, form);

    /* Step 2: Get array of entities */
    JSONArray entities = (JSONArray) data.get("entities");

    /* Sort entities in the array based on key: "id" */
    JSONArray sortedArray = util.sortJSONArray(entities);

    /* Init global variable to an empty string */
    babelNetIDs = "";

    /* Iterate through speechActs */
    for (Object sa : speechActs) {
      /* Create object and get speech act */
      JSONObject speechAct = (JSONObject) sa;

      String anchor = ((String) speechAct.get("anchor"))
          .replaceAll("[-+.^:,]", "");

      /* Convert entities to RDF model */
      entitiesToRDF(sortedArray, timestamp, turnObject, builder, true, form, anchor);

      break;
    }

    /* Step 3: Get array of relations */
    JSONArray relations = (JSONArray) data.get("relations");

    /* Convert relations to RDF model */
    relationsToRDF(relations, timestamp, turnObject, builder);

    /* Here check if the active slots are still Pending */
    List<IRI> activeSlot = query.activeSlot_S();

    for (IRI s : activeSlot) {

      /* Split IRI from actual name */
      String slotName = util.splitIRI(s);

      /* Get the context of the active DIP */
      IRI activeDIP = query.activeDIP_S();

      /* Get ontology type */
      IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

      if (slotName.contentEquals("obtainMatterConcern")) {

        updateProfile(builder, ontoType, "False", sim, activeDIP, slotName);

        query.updatedActiveSlotStatus_I(slotName);
      } else if (slotName.contentEquals("confirmFirstSurname")) {
        /* If negative then update the slot
         * as Completed with value "No" */
        query.setBSlotStatus_I(activeDIP, slotName, "Completed", "No", "1.0", null);

        query.updatedActiveSlotStatus_I(slotName);
      } else {
        IRI slotType = query.getSlotType_S(activeDIP, slotName);

        /* Update the status of the slot */
        query.setSlotStatus_I(activeDIP, slotName, "FailedAnalysis", "Unknown", "0.0", ontoType);

        if (util.splitIRI(slotType).contains("Demand")) {
          /* Also increment Number of Attempts */
          query.incrementNumberOfAttempts_I(activeDIP, slotName);
        } else {
          /* Also increment Number of Attempts */
          query.incrementNumberOfAttemptsConfirmation_I(activeDIP, slotName);
        }

        /* Update slot status to inactive */
        query.updatedActiveSlotStatus_I(slotName);
      }
    }

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Adding LAS input to the repository.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  /**
   * Stores the entities and relations that are available within each speech act e.g.:
   * <p>
   * "id": "act_2", "type": "Statement-non-opinion", "anchor": "I just arrived to Catalonia and I
   * need some information", "entities": [ "entity_2.1", "entity_2.2", "entity_2.3", "entity_2.4",
   * "relation_1" ]
   *
   * @param entities
   * @param timestamp
   * @param actObject
   * @param builder
   * @return
   */
  public ModelBuilder entitiesToRDF_internal(JSONArray entities, long timestamp,
      Resource actObject, ModelBuilder builder) {
    if (entities != null) {
      /* Iterate through entities */
      for (Object entity : entities) {
        /* Convert object to string */
        String label = ((String) entity)
            .toLowerCase();

        /* Get timestamp */
        long time = System.currentTimeMillis();

        if (label.contains("entity")) {
          /* Create IRI for entity */
          IRI entityObject = Utilities.f
              .createIRI(WELCOME.NAMESPACE, "Entity-" + timestamp + "-" + cnt2);

          /* Put <label,timestamp> pair to map */
          entityMap.put(label, timestamp + "-" + cnt2);

          /* Add entity to builder */
          builder.subject(actObject)
              .add(WELCOME.HASENTITY, entityObject)
              .subject(entityObject)
              .add(RDF.TYPE, WELCOME.ENTITY)
              .add(WELCOME.ID, timestamp + "-" + cnt2)
              .add(WELCOME.TIMESTAMP, new Date(time));

          /* Increment counter */
          cnt2 += 1;
        } else {
          /* Create IRI for relation */
          IRI relationObject = Utilities.f
              .createIRI(WELCOME.NAMESPACE, "Relation-" + timestamp + "-" + cnt3);

          /* Put <label,timestamp> pair to map */
          entityMap.put(label, timestamp + "-" + cnt3);

          /* Add relation to builder */
          builder.subject(actObject)
              .add(WELCOME.HASRELATION, relationObject)
              .subject(relationObject)
              .add(RDF.TYPE, WELCOME.RELATION)
              .add(WELCOME.ID, timestamp + "-" + cnt3)
              .add(WELCOME.TIMESTAMP, new Date(time));

          /* Increment counter */
          cnt3 += 1;
        }
      }
    }

    return builder;
  }

  /**
   * Updates existing entities stored in previous step with additional information e.g.:
   * <p>
   * { "id": "entity_1.1", "type": "Concept", "anchor": "Hello", "link": "bn:00043620n",
   * "confidence": 0.9 }
   *
   * @param entities
   * @param builder
   * @return
   */
  public ModelBuilder entitiesToRDF(JSONArray entities, long timestamp,
      Resource turnObject, ModelBuilder builder, boolean userLAS, String form, String speechAnchor)
      throws ParseException {

    if (entities != null) {
      /* Iterate through entities */
      for (Object en : entities) {
        /* Create object and get entity */
        JSONObject entity = (JSONObject) en;

        /* Get entity id */
        String id = (String) entity.get("id");

        /* Get entity type */
        String type = (String) entity.get("type");
        if (type == null) {
          type = "null";
        }

        /* Get entity anchor */
        String anchor = (String) entity.get("anchor");
        if (anchor == null) {
          anchor = "null";
        }

        /* Get entity confidence score */
        Object con = entity.get("confidence");
        Double confidence;

        /* Check type of confidence since in may be either
         * Long or Double. Integer is also checked just for
         * completeness.
         */
        if (con instanceof Double) {
          confidence = (Double) entity.get("confidence");
        } else if (con instanceof Integer) {
          confidence = 1.0 * (Integer) entity.get("confidence");
        } else {
          confidence = 1.0 * (Long) entity.get("confidence");
        }

        if (confidence == null) {
          confidence = 0.0;
        }

        IRI entityObject;
        /* Create IRI for Entity */
        entityObject = Utilities.f
            .createIRI(WELCOME.NAMESPACE, id + "_" + timestamp);

        /* Add entity to builder */
        builder
            .namedGraph(turnObject)
            .subject(entityObject)
            .add(RDF.TYPE, WELCOME.ENTITY)
            .add(WELCOME.ID, id + "_" + timestamp)
            .add(WELCOME.TIMESTAMP, new Date(timestamp))
            .add(WELCOME.ETYPE, type)
            .add(WELCOME.EANCHOR, anchor)
            .add(WELCOME.ECONFIDENCE, confidence);

        /* Get entity link */
        JSONArray link = (JSONArray) entity.get("links");
        if (link.size() > 0) {
          for (Object l : link) {
            String temp = (String) l;

            /* Add link to builder */
            builder
                .namedGraph(turnObject)
                .subject(entityObject)
                .add(WELCOME.ELINK, temp);

            if (temp.contains("bn")) {
              babelNetIDs += temp + " ";
            }
          }
        }

        /* Get active graph (i.e. active DIP) */
        IRI activeGraph = query.activeDIP_S();

        if (userLAS) {
          /* For each entity we check some options */
          builder = checkEntityType(activeGraph, type, builder, anchor, entity, form, speechAnchor);
        }

        if (commit) {
          builder = createCommitContainer(anchor, timestamp, builder, this.scenario);
        }
      }

      /* Add entity to builder */
      builder
          .namedGraph(turnObject)
          .subject(turnObject)
          .add(WELCOME.BNDOCUMENT, babelNetIDs);

      logger.info(babelNetIDs);

    }

    return builder;
  }

  /**
   * Updates existing relations stored in previous step with additional information.
   *
   * @param relations
   * @param builder
   */
  public void relationsToRDF(JSONArray relations, long timestamp,
      Resource turnObject, ModelBuilder builder) {

    if (relations != null) {
      /* Iterate through relations */
      for (Object rel : relations) {
        /* Create object and get relation */
        JSONObject relation = (JSONObject) rel;

        /* Get relation id */
        String id = (String) relation.get("id");

        /* Create IRI for Relation */
        IRI relationObject = Utilities.f
            .createIRI(WELCOME.NAMESPACE, id + "_" + timestamp);

        /* Get predicate */
        String predicate = (String) relation.get("predicate");

        /* Create object and get links */
        JSONArray links = (JSONArray) relation.get("links");

        /* Iterate through entities */
        for (Object link : links) {
          String temp = (String) link;

          /* Retrieve the active slot */
          List<IRI> activeSlot = query.activeSlot_S();

          for (IRI slot : activeSlot) {

            /* Retrieve the active dip */
            IRI activeDIP = query.activeDIP_S();

            /* Get slots name */
            String slotName = util.splitIRI(slot);

            /* Get onto type */
            IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

            if ((SlotLists.negationRelSlots.contains(slotName) || SlotLists.negationSlots.contains(
                slotName)) &&
                (temp.toLowerCase().contains("negation")
                    || temp.toLowerCase().contains("possession"))) {

              if (slotName.contentEquals("obtainCardNumber")) {
                /* Create IRI for Language */
                IRI langType = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "OfficerRoleFlag");
                String flag = query.getProfileValue(langType, sim);

                if (flag.contentEquals("false")) {
                  updateProfile(builder, ontoType, "Unknown", sim, activeDIP, slotName);
                } else {
                  /* Split IRI from actual name */
                  String pred = util.splitIRI(ontoType);
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Unknown", "1.0", pred);
                }
              } else if (slotName.contentEquals("obtainPhoneNumber")
                  || slotName.contentEquals("obtainPhoneAppointmentRejection")) {
                /* Split IRI from actual name */
                String pred = util.splitIRI(ontoType);
                query.setBSlotStatus_I(activeDIP, slotName, "Undefined", "Unknown", "1.0", pred);
              } else if (slotName.contentEquals("obtainAsylumPreRegistrationNumber")) {
                updateProfile(builder, ontoType, "Unknown", sim, activeDIP, slotName);
              } else {
                updateProfile(builder, ontoType, "No", sim, activeDIP, slotName);
              }

              query.updatedActiveSlotStatus_I(slotName);
            }
          }
        }

        /* Create IRI for Predicate */
        IRI predicateObject = Utilities.f
            .createIRI(WELCOME.NAMESPACE, predicate + "_" + timestamp);

        /* Add relation to builder */
        builder
            .namedGraph("welcome:UserTurn-" + timestamp)
            .subject(relationObject)
            .add(WELCOME.PREDICATERELATION, predicateObject)
            .add(RDF.TYPE, WELCOME.RELATION)
            .add(WELCOME.ID, predicate + "_" + timestamp)
            .add(WELCOME.TIMESTAMP, new Date(timestamp));

        /* Parse array of participants */
        JSONArray participants = (JSONArray) relation.get("participants");

        if (participants != null) {
          /* Iterate through relations */
          for (Object par : participants) {
            /* Create object and get participant */
            JSONObject participant = (JSONObject) par;

            String entity = (String) participant.get("entity");

            /* Create IRI for Entity */
            IRI participantObject = Utilities.f
                .createIRI(WELCOME.NAMESPACE, entity + "_" + timestamp);

            /* Create object and get entities */
            JSONArray roles = (JSONArray) participant.get("roles");

            /* Iterate through entities */
            for (Object role : roles) {
              String temp = (String) role;

              /* Get the list of active slots */
              List<IRI> activeSlot = query.activeSlot_S();

              /* Retrieve the active dip */
              IRI activeDIP = query.activeDIP_S();

              for (IRI aSlot : activeSlot) {

                /* Split IRI from actual name */
                String slotName = util.splitIRI(aSlot);

                if (slotName.contentEquals("obtainMatterConcern")) {

                  if (temp.toLowerCase().contentEquals("recipient")) {
                    /* Get ontology type */
                    IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

                    updateProfile(builder, ontoType, "True", sim, activeDIP, slotName);

                    query.updatedActiveSlotStatus_I(slotName);
                  }
                }
              }

              /* Add participant to builder */
              builder
                  .namedGraph("welcome:UserTurn-" + timestamp)
                  .subject(relationObject)
                  .add(WELCOME.HASPARTICIPANT, participantObject)
                  .subject(participantObject)
                  .add(WELCOME.ROLE, temp);
            }
          }
        }
      }
    }
  }

  /**
   * Takes as input an array of speech acts and creates RDF triples.
   *
   * @param speechActs
   * @param timestamp
   * @param turnObject
   * @param builder
   */
  public void speechActsToRDF(JSONArray speechActs, long timestamp,
      Resource turnObject, ModelBuilder builder, String form) throws Exception {
    /* Initialize counter */
    cnt2 = 1;
    cnt3 = 1;

    if (speechActs != null) {
      /* Initialize object */
      IRI prevObject = null;

      /* Initialize counter */
      int cnt1 = 1;

      /* Initialize transcription variable */
      String userTurnTranscription = "";

      /* Iterate through speechActs */
      for (Object sa : speechActs) {

        /* Create object and get speech act */
        JSONObject speechAct = (JSONObject) sa;

        /* Create IRI for speech act */
        IRI actObject = Utilities.f
            .createIRI(WELCOME.NAMESPACE, "U-SpeechAct-" + timestamp + "-" + cnt1);

        /* Get speech act type and anchor */
        String type = (String) speechAct.get("type");

        String anchor = ((String) speechAct.get("anchor"))
            .replaceAll("[-+.^:,]", "");

        /* Check speech type */
        builder = checkSpeechType(type, builder, anchor, form);

        /* Append anchor to variable */
        userTurnTranscription += anchor;

//                TODO: Un-comment those lines once LAS sends the Entity array updated.
//
//                /* Get array of entities */
//                JSONArray entities = (JSONArray) speechAct.get("entities");
//
//                /* Convert entities to RDF model */
//                builder = entitiesToRDF_internal(entities, timestamp, actObject, builder);

        /* Create IRI for speech act type */
        IRI typeObject = Utilities.f
            .createIRI(WELCOME.NAMESPACE, type.replaceAll(" ", "_"));

        /* Add speech act to builder */
        builder
            .namedGraph("welcome:UserTurn-" + timestamp)
            .subject(turnObject)
            .add(WELCOME.HASCONTAINER, actObject)
            .subject(actObject)
            .add(WELCOME.HASCONTAINERID, timestamp + "-" + cnt1)
            .add(RDF.TYPE, WELCOME.CONTAINER)
            .add(WELCOME.SPEECHTYPE, typeObject)
            .add(WELCOME.TRANSCRIPTION, anchor);

        /* Link to previous container */
        if (prevObject != null) {
          builder.subject(actObject)
              .add(WELCOME.PREVCONTAINER, prevObject);
        }

        /* Check if last container */
        if (cnt1 == speechActs.size()) {
          builder.subject(actObject)
              .add(WELCOME.ISLASTCONTAINER, "true");
        } else {
          builder.subject(actObject)
              .add(WELCOME.ISLASTCONTAINER, "false");
        }

        if (commit) {
          builder = createCommitContainer(anchor, timestamp, builder, this.scenario);
        }

        /* Update temp variables */
        prevObject = actObject;
        cnt1 += 1;
      }

      /* Add previous object to builder */
      builder
          .namedGraph("welcome:UserTurn-" + timestamp)
          .subject(turnObject)
          .add(WELCOME.TURNTRANSCRIPTION, userTurnTranscription);

      fullTranscription = userTurnTranscription;
    }
  }

  /**
   * Check the type of the speech act of the user (positive or negative response).
   *
   * @param type
   */
  public ModelBuilder checkSpeechType(String type, ModelBuilder builder, String anchor, String form)
      throws Exception {
    /* Retrieve the active slot */
    List<IRI> activeSlot = query.activeSlot_S();

    /* Retrieve the active dip */
    IRI activeDIP = query.activeDIP_S();

    /* By default, there is no commit */
    commit = false;
    scenario = "";

    for (IRI temp : activeSlot) {
      /* Split IRI from actual name */
      String slotName = util.splitIRI(temp);

      IRI slotType = query.getSlotType_S(activeDIP, slotName);

      boolean b = anchor.toLowerCase().contains("skip")
          || anchor.toLowerCase().contains("next");

      if (util.splitIRI(slotType).contains("Demand")) {
        /* Get ontology type */
        IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

        /* Get the status of the active slot */
        String status = query.getSlotStatus(activeDIP, temp);

        /* Get the scenario name selected by user */
        String scenario = query.scenarioSelection_S();

        /* If TCN says skip or next we skip the slot */
        if (b) {
          if (slotName.contentEquals("obtainSuggestOtherEducation")) {
            query.resetSlotsStatus(activeDIP, SlotLists.courseSlotsFull, "Completed");
            query.setBSlotStatus_I(activeDIP, "obtainMoreEducationalInformationConfirmation",
                "Completed", "Yes", "1.0", "CourseReview");
            this.updateSlot();
            break;
          }

          /* Update the status of the slot */
          query.setSlotStatus_I(activeDIP, slotName, "Completed", "Skip", "0.0", ontoType);
          query.updatedActiveSlotStatus_I(slotName);

        } else if (SlotLists.questionResponses.contains(type.toLowerCase())
            && !slotName.contentEquals("obtainSubtopic")
            && (scenario.toLowerCase().contains("cv creation") || scenario.toLowerCase()
            .contains("health"))) {
          String faqStatus = query.checkFAQStatus();

          if (faqStatus == null) {
            cv.createPrevDipGraph(activeDIP, slotName);
            /* Check Internal FAQs */
            if (cv.checkInternalFAQs(anchor, activeDIP, slotName)) {
              query.updatedActiveSlotStatus_I(slotName);
              break;
            } else if (cv.checkExternalFAQs(anchor, activeDIP, slotName, true)) {
              query.updatedActiveSlotStatus_I(slotName);
              break;
            }
          } else if (faqStatus.contentEquals("internal")) {
            /* Check FollowUp FAQs */
            if (cv.checkFollowUpFAQs(anchor, activeDIP, slotName)) {
              query.setBSlotStatus_I(activeDIP, "obtainSatisfaction", "Completed",
                  "No", "1.0", null);
              query.updatedActiveSlotStatus_I(slotName);
              break;
            } else if (cv.checkExternalFAQs(anchor, activeDIP, slotName, false)) {
              query.setBSlotStatus_I(activeDIP, "obtainSatisfaction", "Completed",
                  "No", "1.0", null);
              query.setInfoSlotStatus_I(activeDIP, "informFollowUpAnswer", "Completed", "1.0");
              query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed",
                  "Unknown", "1.0", null);
              query.updatedActiveSlotStatus_I(slotName);
              break;
            } else {
              /* The following slots should be skipped by DMS if first answer satisfies the user */
              query.setBSlotStatus_I(activeDIP, "obtainSatisfaction", "Completed", "No",
                  "1.0", null);
              query.setInfoSlotStatus_I(activeDIP, "informFollowUpAnswer", "Completed", "1.0");
              query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed",
                  "Unknown", "1.0", null);
              query.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed", "Unknown",
                  "1.0", null);
              query.updatedActiveSlotStatus_I(slotName);
            }
          } else if (faqStatus.contentEquals("followup")) {
            /* Check External FAQs */
            if (cv.checkExternalFAQs(anchor, activeDIP, slotName, false)) {
              query.updatedActiveSlotStatus_I(slotName);
              break;
            } else {
              /* The following slots should be skipped by DMS if first answer satisfies the user */
              query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed", "No",
                  "1.0", null);
              query.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed", "Unknown",
                  "1.0", null);
              query.setInfoSlotStatus_I(activeDIP, "informInabilityAssist", "Completed", "1.0");
              query.setInfoSlotStatus_I(activeDIP, "informGenericContact", "Completed", "1.0");
              query.updatedActiveSlotStatus_I(slotName);
            }
          }
        } else {
          /* Check if slot status is NeedsUpdate. */
          if (status.contentEquals("NeedsUpdate")) {
            if (SlotLists.positiveResponses.contains(type.toLowerCase())) {
              String value = query.getProfileValue(ontoType, sim);

              updateProfile(builder, ontoType, value, sim, activeDIP, slotName);

              query.updatedActiveSlotStatus_I(slotName);
            }
          } else {
            if (SlotLists.booleanSlots.contains(slotName)
                || SlotLists.confirmSlots.contains(slotName)
                || (SlotLists.negationSlots.contains(slotName)
                && !slotName.contentEquals("obtainCardNumber")
                && !slotName.contentEquals("obtainAsylumPreRegistrationNumber")
                && !slotName.contentEquals("obtainMobilePhone")
                && !slotName.contentEquals("obtainLandLine"))
                || slotName.contentEquals("obtainNationality")
                || slotName.contentEquals("obtainAdditionalEducation")
                || slotName.contentEquals("obtainAdditionalMoreEducation")
                || slotName.contentEquals("obtainAdditionalLanguage")
                || slotName.contentEquals("obtainAdditionalEmployment")) {

              /* Check if response is positive or negative.
               * At the moment we assume that the active slot
               * would expect such a response */
              if ((SlotLists.positiveResponses.contains(type.toLowerCase())
                  || anchor.toLowerCase().contentEquals("ok")
                  || anchor.toLowerCase().contentEquals("okay"))
                  && !SlotLists.negationSlots.contains(slotName)) {

                /* In case TCN wants to add more Education
                 * then reset the Education slots */
                if (slotName.contentEquals("obtainAdditionalEducation")) {
                  int number = cv.checkNumElements("education") + 1;

                  /* Copy Education Object from temp graph to CVInfo */
                  cv.copySection(SlotLists.educationSlots, "Education", number);

                  /* Remove old graph before updating the information */
                  IRI tempGraph = Utilities.connection
                      .getValueFactory()
                      .createIRI(WELCOME.NAMESPACE, "tempGraph");
                  Utilities.connection.clear(tempGraph);

                  /* Increment number of education elements */
                  query.increaseElements("education");

                  /* Change status to Pending and remove previous responses */
                  query.resetSlotsStatus(activeDIP, SlotLists.educationSlots, "Pending");

                  /* Update status of active slot to false */
                  query.updatedActiveSlotStatus_I(slotName);

                } else if (slotName.contentEquals("obtainAdditionalMoreEducation")) {
                  int number = cv.checkNumElements("course") + 1;

                  /* Copy Course Object from temp graph to CVInfo */
                  cv.copySection(SlotLists.courseSlots, "Course", number);

                  /* Remove old graph before updating the information */
                  IRI tempGraph = Utilities.connection
                      .getValueFactory()
                      .createIRI(WELCOME.NAMESPACE, "tempGraph");
                  Utilities.connection.clear(tempGraph);

                  /* Increment number of education elements */
                  query.increaseElements("course");

                  /* Change status to Pending and remove previous responses */
                  query.resetSlotsStatus(activeDIP, SlotLists.courseSlots, "Pending");

                  /* Update status of active slot to false */
                  query.updatedActiveSlotStatus_I(slotName);

                } else if (slotName.contentEquals("obtainAdditionalLanguage")) {
                  int number = cv.checkNumElements("language") + 1;

                  /* Copy Language Object from temp graph to CVInfo */
                  cv.copySection(SlotLists.languageSlots, "Language", number);

                  /* Remove old graph before updating the information */
                  IRI tempGraph = Utilities.connection
                      .getValueFactory()
                      .createIRI(WELCOME.NAMESPACE, "tempGraph");
                  Utilities.connection.clear(tempGraph);

                  /* Increment number of education elements */
                  query.increaseElements("language");

                  /* Change status to Pending and remove previous responses */
                  query.resetSlotsStatus(activeDIP, SlotLists.languageSlots, "Pending");

                  /* Update status of active slot to false */
                  query.updatedActiveSlotStatus_I(slotName);

                } else if (slotName.contentEquals("obtainAdditionalEmployment")) {
                  int number = cv.checkNumElements("employment") + 1;

                  /* Copy Employment Object from temp graph to CVInfo */
                  cv.copySection(SlotLists.pastEmploymentSlots, "Employment", number);

                  /* Remove old graph before updating the information */
                  IRI tempGraph = Utilities.connection
                      .getValueFactory()
                      .createIRI(WELCOME.NAMESPACE, "tempGraph");
                  Utilities.connection.clear(tempGraph);

                  /* Increment number of education elements */
                  query.increaseElements("employment");

                  /* Change status to Pending and remove previous responses */
                  query.resetSlotsStatus(activeDIP, SlotLists.pastEmploymentSlots, "Pending");

                  /* Update status of active slot to false */
                  query.updatedActiveSlotStatus_I(slotName);

                } else if (slotName.contentEquals("obtainIncludeFullAddress")) {
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", null);

                  cv.copyCVPersonal(SlotLists.addressSlots);

                  /* Update status of active slot to false */
                  query.updatedActiveSlotStatus_I(slotName);

                } else if (SlotLists.booleanSlots.contains(slotName)) {
                  /* If positive then update the slot
                   * as Completed with value "Yes" */
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", null);

                  query.updatedActiveSlotStatus_I(slotName);

                  if (slotName.contentEquals("obtainEmploymentStatus")) {
                    /* Create endDate IRI */
                    IRI currentEmploymentStatus = Utilities.f
                        .createIRI(WELCOME.NAMESPACE, "CurrentlyEmployed");

                    updateProfile(builder, currentEmploymentStatus, "Yes", sim, activeDIP, "null");

                    /* Create endDate IRI */
                    IRI endDate = Utilities.f
                        .createIRI(WELCOME.NAMESPACE, "EndDate");

                    updateProfile(builder, endDate, "Current", sim, activeDIP, "null");
                  } else if (slotName.contentEquals("obtainPreviousEmploymentStatus")) {
                    /* Create endDate IRI */
                    IRI currentEmploymentStatus = Utilities.f
                        .createIRI(WELCOME.NAMESPACE, "CurrentlyEmployed");

                    // String currentlyEmployed = query.getProfileValue(currentEmploymentStatus, sim);
                    String currentlyEmployed = query.getCVElementValue(currentEmploymentStatus,
                        false);

                    if (currentlyEmployed.contentEquals("Yes")) {
                      int number = cv.checkNumElements("employment") + 1;

                      /* Copy Education Object from temp graph to CVInfo */
                      cv.copySection(SlotLists.currentEmploymentSlots, "Employment", number);

                      /* Remove old graph before updating the information */
                      IRI tempGraph = Utilities.connection
                          .getValueFactory()
                          .createIRI(WELCOME.NAMESPACE, "tempGraph");
                      Utilities.connection.clear(tempGraph);

                      /* Increment number of education elements */
                      query.increaseElements("employment");
                    }
                  } else if (slotName.contentEquals("obtainSatisfaction")) {
                    /* The following slots should be skipped by DMS if first answer satisfies the user */
                    query.setBSlotStatus_I(activeDIP, "informFollowUpAnswer", "Completed", "Yes",
                        "1.0", null);
                    query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed",
                        "Yes", "1.0", null);
                    query.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed", "Yes",
                        "1.0", null);
                    query.setBSlotStatus_I(activeDIP, "informInabilityAssist", "Completed", "Yes",
                        "1.0", null);
                    query.setBSlotStatus_I(activeDIP, "informGenericContact", "Completed", "Yes",
                        "1.0", null);
                  } else if (slotName.contentEquals("obtainFollowUpSatisfaction")) {
                    /* The following slots should be skipped by DMS if follow-up answer satisfies the user */
                    query.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed", "Yes",
                        "1.0", null);
                    query.setBSlotStatus_I(activeDIP, "informInabilityAssist", "Completed", "Yes",
                        "1.0", null);
                    query.setBSlotStatus_I(activeDIP, "informGenericContact", "Completed", "Yes",
                        "1.0", null);
                  } else if (slotName.contentEquals("obtainFAQSatisfaction")) {
                    query.setBSlotStatus_I(activeDIP, "informInabilityAssist", "Completed", "Yes",
                        "1.0", null);
                    query.setBSlotStatus_I(activeDIP, "informGenericContact", "Completed", "Yes",
                        "1.0", null);
                  } else if (slotName.contentEquals("obtainContinueInterest")) {
                    /* The following slots should be skipped by DMS if follow-up answer satisfies the user */
                    query.setBSlotStatus_I(activeDIP, "obtainContinueInterest", "Completed", "Yes",
                        "1.0", null);

                    cv.successExitFAQ();
                    query.updatedActiveSlotStatus_I(slotName);
                    break;
                  }
                } else if (SlotLists.confirmSlots.contains(slotName)
                    || slotName.contentEquals("obtainNationality")) {

                  if (slotName.contentEquals("confirmLanguage")) {
                    String language = query.getProfileValue(ontoType, sim);

                    query.setSlotStatus_I(activeDIP, slotName, "Completed", language, "1.0",
                        ontoType);

                    /* Get the scenario name selected by user */
                    scenario = query.scenarioSelection_S();

                    query.updatedActiveSlotStatus_I(slotName);

                    this.commit = true;
                    this.scenario = scenario;

                  } else {
                    updateProfile(builder, ontoType, "Yes", sim, activeDIP, slotName);

                  }
                  query.updatedActiveSlotStatus_I(slotName);
                }
              } else if (SlotLists.negativeResponses.contains(type.toLowerCase())
                  && !slotName.contentEquals("obtainNationality")) {

                if ((SlotLists.booleanSlots.contains(slotName)
                    && !slotName.contentEquals("confirmTimeSlot")
                    && !slotName.contentEquals("obtainConfirmationTimeSlot"))
                    || slotName.contentEquals("obtainAdditionalEducation")
                    || slotName.contentEquals("obtainAdditionalMoreEducation")
                    || slotName.contentEquals("obtainAdditionalLanguage")
                    || slotName.contentEquals("obtainAdditionalEmployment")
                    || slotName.contentEquals("obtainIncludeFullAddress")
                    || slotName.contentEquals("obtainMoreGrade")) {
                  /* If negative then update the slot
                   * as Completed with value "No" */
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "No", "1.0", null);

                  if (slotName.contentEquals("obtainAdditionalLanguageConfirmation")) {
                    query.setBSlotStatus_I(activeDIP, "obtainLanguageInformationConfirmation",
                        "Completed", "Yes", "1.0", "LanguageReview");
                  } else if (slotName.contentEquals("obtainAdditionalEducation")) {
                    int number = cv.checkNumElements("education") + 1;

                    /* Increment number of education elements */
                    query.increaseElements("education");

                    /* Copy Education Object from temp graph to CVInfo */
                    cv.copySection(SlotLists.educationSlots, "Education", number);

                    /* Remove old graph before updating the information */
                    IRI tempGraph = Utilities.connection
                        .getValueFactory()
                        .createIRI(WELCOME.NAMESPACE, "tempGraph");
                    Utilities.connection.clear(tempGraph);

                    /* Change status to Pending and remove previous responses */
                    query.resetSlotsStatus(activeDIP, SlotLists.educationSlots, "Completed");

                    query.updatedActiveSlotStatus_I(slotName);

                  } else if (slotName.contentEquals("obtainAdditionalMoreEducation")) {
                    int number = cv.checkNumElements("course") + 1;

                    /* Increment number of education elements */
                    query.increaseElements("course");

                    /* Copy Education Object from temp graph to CVInfo */
                    cv.copySection(SlotLists.courseSlots, "Course", number);

                    /* Remove old graph before updating the information */
                    IRI tempGraph = Utilities.connection
                        .getValueFactory()
                        .createIRI(WELCOME.NAMESPACE, "tempGraph");
                    Utilities.connection.clear(tempGraph);

                    /* Change status to Pending and remove previous responses */
                    query.resetSlotsStatus(activeDIP, SlotLists.courseSlots, "Completed");

                    query.updatedActiveSlotStatus_I(slotName);

                  } else if (slotName.contentEquals("obtainAdditionalLanguage")) {
                    int number = cv.checkNumElements("language") + 1;

                    /* Increment number of education elements */
                    query.increaseElements("language");

                    /* Copy Education Object from temp graph to CVInfo */
                    cv.copySection(SlotLists.languageSlots, "Language", number);

                    /* Remove old graph before updating the information */
                    IRI tempGraph = Utilities.connection
                        .getValueFactory()
                        .createIRI(WELCOME.NAMESPACE, "tempGraph");
                    Utilities.connection.clear(tempGraph);

                    /* Change status to Pending and remove previous responses */
                    query.resetSlotsStatus(activeDIP, SlotLists.languageSlots, "Completed");

                    query.updatedActiveSlotStatus_I(slotName);

                  } else if (slotName.contentEquals("obtainAdditionalEmployment")) {
                    int number = cv.checkNumElements("employment") + 1;

                    /* Increment number of education elements */
                    query.increaseElements("employment");

                    /* Copy Education Object from temp graph to CVInfo */
                    cv.copySection(SlotLists.pastEmploymentSlots, "Employment", number);

                    /* Remove old graph before updating the information */
                    IRI tempGraph = Utilities.connection
                        .getValueFactory()
                        .createIRI(WELCOME.NAMESPACE, "tempGraph");
                    Utilities.connection.clear(tempGraph);

                    /* Change status to Pending and remove previous responses */
                    query.resetSlotsStatus(activeDIP, SlotLists.pastEmploymentSlots, "Completed");

                    /* Update status of active slot to false */
                    query.updatedActiveSlotStatus_I(slotName);

                  } else if (slotName.contentEquals("confirmFirstSurname")) {
                    //TODO CHECK HERE IF SLOT SHOULD BE UPDATED TO COMPLETED
                    query.deleteProfileInfo_D("FirstSurname", sim);
                    query.setInfoSlotStatus_I(activeDIP, "obtainFirstSurname", "Pending", "0.0");
                  } else {
                    if (slotName.contentEquals("obtainEmploymentStatus")) {
                      /* Create endDate IRI */
                      IRI currentEmploymentStatus = Utilities.f
                          .createIRI(WELCOME.NAMESPACE, "CurrentlyEmployed");

                      updateProfile(builder, currentEmploymentStatus, "No", sim, activeDIP, "null");
                    } else if (slotName.contentEquals("obtainPreviousEmploymentStatus")) {
                      /* Create employment status IRI */
                      IRI currentEmploymentStatus = Utilities.f
                          .createIRI(WELCOME.NAMESPACE, "CurrentlyEmployed");

                      // String currentlyEmployed = query.getProfileValue(currentEmploymentStatus,
                      //    sim);
                      String currentlyEmployed = query.getCVElementValue(currentEmploymentStatus,
                          false);

                      if (currentlyEmployed.contentEquals("Yes")) {
                        int number = cv.checkNumElements("employment") + 1;

                        /* Copy Education Object from temp graph to CVInfo */
                        cv.copySection(SlotLists.currentEmploymentSlots, "Employment", number);

                        /* Remove old graph before updating the information */
                        IRI tempGraph = Utilities.connection
                            .getValueFactory()
                            .createIRI(WELCOME.NAMESPACE, "tempGraph");
                        Utilities.connection.clear(tempGraph);

                        /* Increment number of education elements */
                        query.increaseElements("employment");
                      } else {
                        query.setBSlotStatus_I(activeDIP, "obtainEmploymentInformationConfirmation",
                            "Completed", "Yes", "1.0", "EmploymentReview");
                      }
                    } else if (slotName.contentEquals("obtainSatisfaction")) {
                      if (cv.checkExternalFAQs(anchor, activeDIP, slotName, false)) {
                        query.setBSlotStatus_I(activeDIP, "obtainSatisfaction", "Completed",
                            "No", "1.0", null);
                        query.setInfoSlotStatus_I(activeDIP, "informFollowUpAnswer", "Completed",
                            "1.0");
                        query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed",
                            "Unknown", "1.0", null);
                        query.updatedActiveSlotStatus_I(slotName);
                        break;
                      } else {
                        /* The following slots should be skipped by DMS if first answer satisfies the user */
                        query.setBSlotStatus_I(activeDIP, "obtainSatisfaction", "Completed", "No",
                            "1.0", null);
                        query.setInfoSlotStatus_I(activeDIP, "informFollowUpAnswer", "Completed",
                            "1.0");
                        query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed",
                            "Unknown", "1.0", null);
                        query.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed",
                            "Unknown",
                            "1.0", null);
                      }
                    } else if (slotName.contentEquals("obtainFollowUpSatisfaction")) {
                      /* Check External FAQs */
                      if (cv.checkExternalFAQs(anchor, activeDIP, slotName, false)) {
                        /* The following slots should be skipped by DMS if first answer satisfies the user */
                        query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed",
                            "No",
                            "1.0", null);
                        query.updatedActiveSlotStatus_I(slotName);
                        break;
                      } else {
                        /* The following slots should be skipped by DMS if first answer satisfies the user */
                        query.setBSlotStatus_I(activeDIP, "obtainFollowUpSatisfaction", "Completed",
                            "No",
                            "1.0", null);
                        query.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed",
                            "Unknown",
                            "1.0", null);
                      }
                    } else if (slotName.contentEquals("obtainContinueInterest")) {
                      /* The following slots should be skipped by DMS if follow-up answer satisfies the user */
                      query.setBSlotStatus_I(activeDIP, "obtainContinueInterest", "Completed", "No",
                          "1.0", null);

                      cv.failureExitFAQ();
                      query.updatedActiveSlotStatus_I(slotName);
                      break;
                    }

                    query.updatedActiveSlotStatus_I(slotName);
                  }

                } else if (SlotLists.confirmSlots.contains(slotName)) {
                  updateProfile(builder, ontoType, "No", sim, activeDIP, slotName);

                  if (slotName.contentEquals("obtainSuggestOtherEducation")) {
                    query.setBSlotStatus_I(activeDIP,
                        "obtainMoreEducationalInformationConfirmation",
                        "Completed", "Yes", "1.0", "CourseReview");
                  }

                  query.updatedActiveSlotStatus_I(slotName);
                } else if (SlotLists.negationSlots.contains(slotName)) {
                  updateProfile(builder, ontoType, "No", sim, activeDIP, slotName);

                  query.updatedActiveSlotStatus_I(slotName);
                }
              } else if (SlotLists.unclearResponses.contains(type.toLowerCase())) {
                /* If unclear then update the slot
                 * as TCNClarifyRequest with value "Unknown" */
                query.setSlotStatus_I(activeDIP, slotName, "TCNClarifyRequest", "Unknown", "0.0",
                    ontoType);

                /* Also increment Number of Attempts */
                query.incrementNumberOfAttempts_I(activeDIP, slotName);

                query.updatedActiveSlotStatus_I(slotName);

              } else if (SlotLists.previousResidence.contains(slotName)) {
                //TODO: TO BE CHECKED
                /* do nothing. the slots will be checked again based on the entities */
                if (SlotLists.negativeResponses.contains(type.toLowerCase())) {
                  updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);

                  query.updatedActiveSlotStatus_I(slotName);
                }
              } else if (slotName.contentEquals("obtainSSNumberAvailable")) {
                query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Unclear", "1.0", null);

                query.updatedActiveSlotStatus_I(slotName);
              }
            } else if (SlotLists.requestSlot.contains(slotName)) {
              /* Change status to Completed */
              query.setSlotStatus_I(activeDIP, "obtainRequest", "Completed", "Unknown", "1.0",
                  ontoType);

              /* Get the scenario name selected by user */
              scenario = query.scenarioSelection_S();

              query.updatedActiveSlotStatus_I(slotName);

              this.commit = true;
              this.scenario = scenario;

            } else if (slotName.contentEquals("obtainMatterConcernStatus")) {

              updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);

              query.updatedActiveSlotStatus_I(slotName);

            } else if (slotName.contentEquals("obtainMatterConcern")
                || slotName.contentEquals("obtainAppointmentConcern")) {
              /* Get the scenario name selected by user */
              scenario = query.scenarioSelection_S();

              if (scenario.contentEquals("Simulate Appointment Legal Service Praksis")) {
                updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);

                query.updatedActiveSlotStatus_I(slotName);
              } else {
                Set<String> hash_Set = query.checkSubtopics(anchor, slotName, scenario);

                if (!hash_Set.isEmpty()) {
                  Iterator<String> topicsIterator = hash_Set.iterator();

                  if (topicsIterator.hasNext()) {
                    String topic = topicsIterator.next();

                    updateProfile(builder, ontoType, topic, sim, activeDIP, slotName);

                    query.updatedActiveSlotStatus_I(slotName);
                  }
                }
              }
            } else if (slotName.contentEquals("confirmTimeSlot")
                || slotName.contentEquals("obtainConfirmationTimeSlot")) {

              if (SlotLists.positiveResponses.contains(type.toLowerCase())) {
                /* If positive then update the slot
                 * as Completed with value "Yes" */
                query.setSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", ontoType);

                query.updatedActiveSlotStatus_I(slotName);

                query.resetStatus();
              } else if (SlotLists.negativeResponses.contains(type.toLowerCase())) {
                /* In case the user did not agree with the proposal check if there is another Free slot */
                boolean check = query.checkFreeSlot_A();

                if (check) {
                  /* Update the previous suggestion to "Rejected" */
                  query.updateTimeSlotStatus(activeDIP);

                  /* Delete profile info */
                  query.deleteProfileInfo_D("AppointmentDay", sim);
                  query.deleteProfileInfo_D("AppointmentDate", sim);
                  query.deleteProfileInfo_D("AppointmentTime", sim);
                  query.deleteProfileInfo_D("AppointmentTimeEarlier", sim);

                  /* Propose a new time slot */
                  query.updateProposeTimeSlot(activeDIP, false, "");

                  /* Also increment Number of Attempts */
                  query.incrementNumberOfAttempts_I(activeDIP, slotName);

                  if (slotName.contentEquals("confirmTimeSlot")) {
                    /* Change status of proposeTimeSlot to "Pending" */
                    query.setInfoSlotStatus_I(activeDIP, "proposeTimeSlot", "Pending", "0.0");
                  } else {
                    /* Change status of proposeTimeSlot to "Pending" */
                    query.setInfoSlotStatus_I(activeDIP, "informProposeTimeSlot", "Pending", "0.0");
                  }
                  query.updatedActiveSlotStatus_I(slotName);
                }
              }
            } else if (slotName.contentEquals("obtainCardNumber")
                || slotName.contentEquals("obtainPostCode") /* Ignore post code */
                || slotName.contentEquals("obtainAsylumPreRegistrationNumber")
                || slotName.contentEquals("obtainLandline")
                || slotName.contentEquals("obtainMobilePhone")
                || slotName.contentEquals("obtainPhoneNumber")
                || slotName.contentEquals("obtainPhoneAppointmentRejection")
                || slotName.contentEquals("obtainPhoneNumberAppointment")
                || slotName.contentEquals("obtainMoreGrade")) {
              String t = "";
              String[] arrSplit = anchor.split(" ");

              for (int i = 0; i < arrSplit.length; i++) {
                String convertedText = convertTextualNumbersInDocument(arrSplit[i]);
                convertedText = convertedText.replaceAll("[^\\d]", "");
                if (!convertedText.contentEquals("")) {
                  t += convertedText;
                } else if (arrSplit[i].contentEquals("dot")
                    || arrSplit[i].contentEquals("point")
                    || arrSplit[i].contentEquals("comma")) {
                  t += ".";
                }
              }

              if (!t.contentEquals("")) {
                /* Get the scenario name selected by user */
                scenario = query.scenarioSelection_S();

                if (scenario.contentEquals("Simulate Appointment Legal Service Praksis")) {
                  if (slotName.contentEquals("obtainPhoneNumber")
                      || slotName.contentEquals("obtainPhoneAppointmentRejection")
                      || slotName.contentEquals("obtainPhoneNumberAppointment")
                      || slotName.contentEquals("obtainCardNumber")) {

                    if (slotName.contentEquals("obtainCardNumber")) {
//                      /* Create IRI for Language */
//                      IRI langType = Utilities.f
//                          .createIRI(WELCOME.NAMESPACE, "OfficerRoleFlag");
//                      String flag = query.getProfileValue(langType, sim);
//
//                      if (flag.contentEquals("false")) {
//                        updateProfile(builder, ontoType, t, sim, activeDIP, slotName);
//                      } else {
//                        /* Split IRI from actual name */
//                        String pred = util.splitIRI(ontoType);
//                        query.setBSlotStatus_I(activeDIP, slotName, "Completed", t, "1.0", pred);
//                      }
                      updateProfile(builder, ontoType, t, sim, activeDIP, slotName);
                    } else {
                      /* Split IRI from actual name */
                      String predicate = util.splitIRI(ontoType);

                      query.setBSlotStatus_I(activeDIP, slotName, "Completed", t, "1.0", predicate);
                    }
                  }
                } else if (slotName.contentEquals("obtainAsylumPreRegistrationNumber")) {
                  updateProfile(builder, ontoType, t, sim, activeDIP, slotName);

                  /* We store the value in the ProfileInfo as well */
                  updateProfile(builder, ontoType, t, 0, activeDIP, slotName);
                } else {
                  updateProfile(builder, ontoType, t, sim, activeDIP, slotName);
                }
                query.updatedActiveSlotStatus_I(slotName);
              }
            } else if (slotName.contentEquals("obtainCVCreationInterest")) {
              /* Create IRI for Language */
              IRI statusType = Utilities.f
                  .createIRI(WELCOME.NAMESPACE, "CVStatus");

              String cvStatus = query.getProfileValue(statusType, 0);

              if (cvStatus.contentEquals("None")) {
                /* System asks if TCN wants to create a new CV. */
                if (SlotLists.positiveResponses.contains(type.toLowerCase())) {
                  /* Update DIP */
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0",
                      "CVCreationInterest");

                  /* Update slot status */
                  query.updatedActiveSlotStatus_I(slotName);

                  /* Get scenario */
                  scenario = query.scenarioSelection_S();

                  /* Init profile */
                  IRI graph = Utilities.connection
                      .getValueFactory()
                      .createIRI(WELCOME.NAMESPACE + "CVInfo");
                  Utilities.connection.clear(graph);

                  query.initProfile(scenario, 2);

                  /* Add CV Purpose in CVInfo graph */
                  query.insertTCNData("CVPurpose", "Create New", graph);

//                  /* Reset CV Status */
//                  IRI profile = Utilities.connection
//                      .getValueFactory()
//                      .createIRI(WELCOME.NAMESPACE + "ProfileInfo");
//
//                  query.removeTCNData("CVStatus", profile);
//                  query.insertTCNData("CVStatus", "None", profile);
                }
              } else if (cvStatus.contentEquals("Incomplete")) {
                /* System asks if TCN wants to create new CV or continue. */
                String result = query.checkCVPurpose(anchor, 0);

                /* Init profile */
                IRI graph = Utilities.connection
                    .getValueFactory()
                    .createIRI(WELCOME.NAMESPACE + "CVInfo");

                if (result.contentEquals("Create New")) {
                  Utilities.connection.clear(graph);

                  /* Get scenario */
                  scenario = query.scenarioSelection_S();

                  query.initProfile(scenario, 2);

                  /* Add CV Purpose in CVInfo graph */
                  query.insertTCNData("CVPurpose", "Create New", graph);

                  /* Reset CV Status */
                  IRI profile = Utilities.connection
                      .getValueFactory()
                      .createIRI(WELCOME.NAMESPACE + "ProfileInfo");

                  query.removeTCNData("CVStatus", profile);
                  query.insertTCNData("CVStatus", "None", profile);

                  /* Update DIP */
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0",
                      "CVCreationInterest");

                  /* Update slot status */
                  query.updatedActiveSlotStatus_I(slotName);
                } else if (result.contentEquals("Continue Incomplete")) {
                  /* Add CV Purpose in CVInfo graph */
                  query.removeTCNData("CVPurpose", graph);
                  query.insertTCNData("CVPurpose", "Continue Incomplete", graph);

                  /* Update DIP */
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0",
                      "CVCreationInterest");

                  /* Update slot status */
                  query.updatedActiveSlotStatus_I(slotName);
                }
              } else if (cvStatus.contentEquals("Complete")) {
                /* System asks if TCN wants to create new CV or review completed. */
                String result = query.checkCVPurpose(anchor, 1);

                /* Init profile */
                IRI graph = Utilities.connection
                    .getValueFactory()
                    .createIRI(WELCOME.NAMESPACE + "CVInfo");

                if (result.contentEquals("Create New")) {
                  Utilities.connection.clear(graph);

                  /* Get scenario */
                  scenario = query.scenarioSelection_S();

                  query.initProfile(scenario, 2);

                  /* Add CV Purpose in CVInfo graph */
                  query.insertTCNData("CVPurpose", "Create New", graph);

                  /* Reset CV Status */
                  IRI profile = Utilities.connection
                      .getValueFactory()
                      .createIRI(WELCOME.NAMESPACE + "ProfileInfo");

                  query.removeTCNData("CVStatus", profile);
                  query.insertTCNData("CVStatus", "None", profile);

                  /* Update DIP */
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0",
                      "CVCreationInterest");

                  /* Update slot status */
                  query.updatedActiveSlotStatus_I(slotName);
                } else if (result.contentEquals("Review Completed")) {
                  /* Add CV Purpose in CVInfo graph */
                  query.removeTCNData("CVPurpose", graph);
                  query.insertTCNData("CVPurpose", "Review Completed", graph);

                  /* Update DIP */
                  query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0",
                      "CVCreationInterest");

                  /* Update slot status */
                  query.updatedActiveSlotStatus_I(slotName);
                }
              }
            } else if (slotName.contentEquals("obtainSubtopic")) {

              /* Get the scenario name selected by user */
              scenario = query.scenarioSelection_S();

              boolean count = query.countPendingTopics(scenario);
              // Check if there is only one pending topic
              if (count) {
                if (SlotLists.positiveResponses.contains(type.toLowerCase())) {

                  // Get topic name
                  String topic = query.getSingleTopic(scenario);

                  /* Get ontology type */
                  query.setBSlotStatus_I(activeDIP, topic.replace("Informed", "Requested"),
                      "Completed", true, "1.0", null);

                  /* Add selected topic to relevant graph */
                  if (scenario.toLowerCase().contains("schooling")) {
                    query.updateTopicStatus(builder, topic,
                        "Pending", "schooling");
                  } else {
                    query.updateTopicStatus(builder, topic,
                        "Pending", "health");
                  }

                  query.setBSlotStatus_I(activeDIP, "obtainSubtopic", "Completed", "Yes", "1.0",
                      null);
                  query.updatedActiveSlotStatus_I(slotName);

                  query.updateAllSlotStatus(activeDIP, "Completed");
                } else if (SlotLists.negativeResponses.contains(type.toLowerCase())) {
                  query.setBSlotStatus_I(activeDIP, "obtainSubtopic", "Completed", "No", "1.0",
                      null);
                  query.updatedActiveSlotStatus_I(slotName);
                }
              } else {
                if (SlotLists.negativeResponses.contains(type.toLowerCase())) {
                  query.setBSlotStatus_I(activeDIP, "obtainSubtopic", "Completed", "No", "1.0",
                      null);
                  query.updatedActiveSlotStatus_I(slotName);
                } else {
                  Set<String> hash_Set = query.checkSubtopics(anchor, slotName, scenario);

                  if (!hash_Set.isEmpty()) {
                    Iterator<String> topicsIterator = hash_Set.iterator();

                    while (topicsIterator.hasNext()) {
                      String topic = topicsIterator.next();
                      /* Get ontology type */
                      query.setBSlotStatus_I(activeDIP, topic, "Completed", true, "1.0", null);

                      /* Add selected topic to relevant graph */
                      if (scenario.toLowerCase().contains("schooling")) {
                        query.updateTopicStatus(builder, topic.replace("Requested", "Informed"),
                            "Pending", "schooling");
                      } else {
                        query.updateTopicStatus(builder, topic.replace("Requested", "Informed"),
                            "Pending", "health");
                      }
                    }
                    query.setBSlotStatus_I(activeDIP, "obtainSubtopic", "Completed", "Yes", "1.0",
                        null);
                    query.updatedActiveSlotStatus_I(slotName);

                    query.updateAllSlotStatus(activeDIP, "Completed");
                  }
                }
              }
            } else if (SlotLists.statusSlots.contains(slotName)) {
              /* System asks if TCN wants to create new CV or continue. */
              String result = query.checkCompletionStatus(anchor, slotName);

              if (!result.contentEquals("")) {
                updateProfile(builder, ontoType, result, sim, activeDIP, slotName);
                query.updatedActiveSlotStatus_I(slotName);
              }
            } else if (SlotLists.allowsString.contains(slotName)) {

              if (slotName.contentEquals("obtainMoreDegreeTitle")
                  || slotName.contentEquals("obtainMoreEducationalCourseSchoolType")
                  || slotName.contentEquals("obtainAdditionalLanguageLevel")
                  || slotName.contentEquals("obtainAdditionalLanguageCourseSchoolType")
                  || slotName.contentEquals("obtainCurrentOccupation")
                  || slotName.contentEquals("obtainPreviousOccupation")) {
                String t = util.splitIRI(ontoType);

                String field = null;
                if (slotName.contentEquals("obtainAdditionalLanguageCourseSchoolType")) {
                  field = cv.checkMultipleChoiceSlots(anchor, "CourseSchoolType", "detection");
                } else if (slotName.contentEquals("obtainAdditionalLanguageLevel")) {
                  field = cv.checkMultipleChoiceSlots(anchor, t, "similarity");
                } else {
                  // check if input provided via text
                  if (slotName.contentEquals("obtainMoreDegreeTitle")) {
                    if (form.toLowerCase().contentEquals("text")) {
                      updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);
                      query.updatedActiveSlotStatus_I(slotName);
                    } else {
                      field = cv.checkMultipleChoiceSlots(anchor, t, "detection");
                    }
                  } else if (slotName.contains("Occupation")) {
                    if (form.toLowerCase().contentEquals("text")) {
                      updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);
                      query.updatedActiveSlotStatus_I(slotName);
                    } else {
                      field = cv.checkMultipleChoiceSlots(anchor, "Occupation", "detection");
                    }
                  } else {
                    field = cv.checkMultipleChoiceSlots(anchor, t, "detection");
                  }
                }

                // check if kmsAux returned a match and update the slot
                if (field != null) {
                  updateProfile(builder, ontoType, field, sim, activeDIP, slotName);
                  query.updatedActiveSlotStatus_I(slotName);
                }

              } else {
                if (form.toLowerCase().contentEquals("text")) {
                  updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);
                } else {
                  String final_anchor = anchor.substring(0, 1).toUpperCase() + anchor.substring(1);
                  updateProfile(builder, ontoType, final_anchor, sim, activeDIP, slotName);
                }
              }

              if (!SlotLists.negationRelSlots.contains(slotName)) {
                query.updatedActiveSlotStatus_I(slotName);
              }
            }
          }
        }
      } else if (util.splitIRI(slotType).contains("ConfirmationRequest")) {

        if (SlotLists.positiveResponses.contains(type.toLowerCase())
            || anchor.toLowerCase().contains("ok")
            || anchor.toLowerCase().contains("okay")) {

          if (slotName.contentEquals("obtainContinueInterest")) {
            /* The following slots should be skipped by DMS if follow-up answer satisfies the user */
            query.setBSlotStatus_I(activeDIP, "obtainContinueInterest", "Completed", "Yes",
                "1.0", null);

            cv.successExitFAQ();
            query.updatedActiveSlotStatus_I(slotName);
            break;
          }

          /* Get ontology type */
          IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

          /* If positive then update the slot
           * as Completed with value "Yes" */
          query.setSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", ontoType);

          query.updatedActiveSlotStatus_I(slotName);
        } else if (SlotLists.negativeResponses.contains(type.toLowerCase())) {

          if (slotName.contentEquals("obtainContinueInterest")) {
            /* The following slots should be skipped by DMS if follow-up answer satisfies the user */
            query.setBSlotStatus_I(activeDIP, "obtainContinueInterest", "Completed", "Yes",
                "1.0", null);

            cv.failureExitFAQ();
            query.updatedActiveSlotStatus_I(slotName);
            break;
          }
          /* Get ontology type */
          IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

          /* If negative then update the slot
           * as Pending with value "No" */
          query.setSlotStatus_I(activeDIP, slotName, "Undefined", "No", "1.0", ontoType);

          /* Also increment Number of Attempts */
          // query.incrementNumberOfAttemptsConfirmation_I(activeDIP, slotName);
          query.updatedActiveSlotStatus_I(slotName);
        }
      } else {
        String anchorTrimmed = anchor.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
        if (b || anchorTrimmed.contentEquals("no") && SlotLists.skipSlots.contains(slotName)) {
          if (slotName.contentEquals("obtainIntroductionEducation")) {
            query.resetSlotsStatus(activeDIP, SlotLists.educationSlotsFull, "Completed");
            query.setBSlotStatus_I(activeDIP, "obtainEducationalInformationConfirmation",
                "Completed", "Yes", "1.0", "EducationReview");
            this.updateSlot();
            break;
          } else if (slotName.contentEquals("obtainSuggestOtherEducation")) {
            query.resetSlotsStatus(activeDIP, SlotLists.courseSlotsFull, "Completed");
            query.setBSlotStatus_I(activeDIP, "obtainMoreEducationalInformationConfirmation",
                "Completed", "Yes", "1.0", "CourseReview");
            this.updateSlot();
            break;
          } else if (slotName.contentEquals("informLanguagesSection")) {
            query.resetSlotsStatus(activeDIP, SlotLists.languageSlotsFull, "Completed");
            query.setBSlotStatus_I(activeDIP, "obtainLanguageInformationConfirmation", "Completed",
                "Yes", "1.0", "LanguageReview");
            this.updateSlot();
            break;
          } else if (slotName.contentEquals("informIntroductionEmployment")) {
            if (!anchor.toLowerCase().contains("no")) {
              query.resetSlotsStatus(activeDIP, SlotLists.employmentSlotsFull, "Completed");
              query.setBSlotStatus_I(activeDIP, "obtainEmploymentInformationConfirmation",
                  "Completed", "Yes", "1.0", "EmploymentReview");
              this.updateSlot();
              break;
            } else {
              this.updateSlot();
            }
          } else if (slotName.contentEquals("informOtherSectionIntro")) {
            query.resetSlotsStatus(activeDIP, SlotLists.otherSlotsFull, "Completed");
            query.setBSlotStatus_I(activeDIP, "obtainOtherInformationConfirmation", "Completed",
                "Yes", "1.0", "OtherReview");
            this.updateSlot();
            break;
          }
        } else {
          /* In case of SystemInfo slots appearing in the dialogue just mark
          their status as completed */
          query.setInfoSlotStatus_I(activeDIP, slotName, "Completed", "1.0");
          query.updatedActiveSlotStatus_I(slotName);
        }
      }
    }
    return builder;
  }

  public void updateSlot() {
    /* Retrieve the active slot */
    List<IRI> activeSlot = query.activeSlot_S();

    /* Retrieve the active dip */
    IRI activeDIP = query.activeDIP_S();

    for (IRI temp : activeSlot) {
      /* Split IRI from actual name */
      String slotName = util.splitIRI(temp);

      if (slotName.contains("inform")) {
        query.setInfoSlotStatus_I(activeDIP, slotName, "Completed", "1.0");
      } else {
        query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", null);
      }
      query.updatedActiveSlotStatus_I(slotName);
    }
  }

  /**
   * Checks several cases of the entity type considering the active slot.
   *
   * @param graph
   * @param type
   * @param builder
   * @param anchor
   */
  public ModelBuilder checkEntityType(IRI graph, String type, ModelBuilder builder,
      String anchor, JSONObject entity, String form, String speechAnchor) throws ParseException {

    /* Get the list of active slots */
    List<IRI> activeSlot = query.activeSlot_S();

    /* Check if entity contains coordinates */
    boolean locationSlot = false;
    JSONArray location = (JSONArray) entity.get("locations");
    if (location.size() > 0) {
      locationSlot = true;
    }

    /* Retrieve the active dip */
    IRI activeDIP = query.activeDIP_S();

    for (IRI temp : activeSlot) {

      /* Get slots name */
      String slotName = util.splitIRI(temp);

      IRI slotType = query.getSlotType_S(activeDIP, slotName);

      if (util.splitIRI(slotType).contains("Demand")) {
        /* Get ontology type */
        IRI ontoType = query.getOntologyType_S(activeDIP, slotName);

        if (SlotLists.courseNames.contains(slotName)) {
          if (form.toLowerCase().contentEquals("text")) {
            updateProfile(builder, ontoType, speechAnchor, sim, activeDIP, slotName);
          } else {
            if (type.toLowerCase().contains("work_of_art")
                || type.toLowerCase().contains("law")
                || type.toLowerCase().contains("event")) {

              String final_anchor = anchor.substring(0, 1).toUpperCase() + anchor.substring(1);

              updateProfile(builder, ontoType, final_anchor, sim, activeDIP, slotName);
            }
            query.updatedActiveSlotStatus_I(slotName);
          }
        } else if (SlotLists.personSlots.contains(slotName)) {
          if (type.toLowerCase().contains("person")
              || type.toLowerCase().contains("org")
              || form.toLowerCase().contentEquals("text")) {
            String[] split = anchor.split(" ");

            if (slotName.contentEquals("confirmFirstSurname")) {
              IRI surnameObject = Utilities.f
                  .createIRI(WELCOME.NAMESPACE, "FirstSurname");

              String final_anchor = anchor.substring(0, 1).toUpperCase() + anchor.substring(1);
              updateProfile(builder, surnameObject, final_anchor, sim, activeDIP, slotName);

              /* If positive then update the slot
               * as Completed with value "Yes" */
              query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", null);

              query.updatedActiveSlotStatus_I(slotName);
            } else {
              if (split.length == 1) {
                String final_anchor = anchor.substring(0, 1).toUpperCase() + anchor.substring(1);
                updateProfile(builder, ontoType, final_anchor, sim, activeDIP, slotName);
              } else if (split.length == 2
                  && (slotName.contentEquals("obtainName") || slotName.contentEquals(
                  "obtainNameAppointment"))) {
                IRI nameObject = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "Name");

                String final_anchor = split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
                updateProfile(builder, nameObject, final_anchor, sim, activeDIP, slotName);

                IRI surnameObject = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "FirstSurname");

                final_anchor = split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
                updateProfile(builder, surnameObject, final_anchor, sim, activeDIP, slotName);

              } else if (split.length == 3
                  && (slotName.contentEquals("obtainName") || slotName.contentEquals(
                  "obtainNameAppointment"))) {
                IRI nameObject = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "Name");

                String final_anchor = split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
                updateProfile(builder, nameObject, final_anchor, sim, activeDIP, slotName);

                IRI surnameObject = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "FirstSurname");

                final_anchor = split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
                updateProfile(builder, surnameObject, final_anchor, sim, activeDIP, slotName);

                IRI secondSurnameObject = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "SecondSurname");

                final_anchor = split[2].substring(0, 1).toUpperCase() + split[2].substring(1);
                updateProfile(builder, secondSurnameObject, final_anchor, sim, activeDIP, slotName);
              } else if (split.length == 2 && slotName.contentEquals("obtainFirstSurname")) {
                IRI surnameObject = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "FirstSurname");

                String final_anchor = split[0].substring(0, 1).toUpperCase() + split[0].substring(1);
                updateProfile(builder, surnameObject, final_anchor, sim, activeDIP, slotName);

                IRI secondSurnameObject = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "SecondSurname");

                final_anchor = split[1].substring(0, 1).toUpperCase() + split[1].substring(1);
                updateProfile(builder, secondSurnameObject, final_anchor, sim, activeDIP, slotName);
              }
            }

            query.updatedActiveSlotStatus_I(slotName);
          }
        } else if (SlotLists.langSlots.contains(slotName)) {
//          if (form.toLowerCase().contentEquals("text")) {
//            updateProfile(builder, ontoType, speechAnchor, sim, activeDIP, slotName);
//            query.setSlotStatus_I(activeDIP, slotName, "Completed", speechAnchor, "1.0", ontoType);
//            query.updatedActiveSlotStatus_I(slotName);
//
//            if (slotName.contentEquals("obtainAdditionalLanguageName")) {
//              query.updateRDFInputContent(activeDIP, SlotLists.S7LanguageSlots, "Language",
//                  "AdditionalLanguageName");
//            }
//
//            if (slotName.contentEquals("confirmLanguage")) {
//              /* Get the scenario name selected by user */
//              String scenario = query.scenarioSelection_S();
//
//              query.updatedActiveSlotStatus_I(slotName);
//
//              this.commit = true;
//              this.scenario = scenario;
//            }
//          } else
          if (type.toLowerCase().contains("language")
              || type.toLowerCase().contains("norp")) {

            String final_anchor = anchor.substring(0, 1).toUpperCase() + anchor.substring(1);

            updateProfile(builder, ontoType, final_anchor, sim, activeDIP, slotName);

            query.setSlotStatus_I(activeDIP, slotName, "Completed", final_anchor, "1.0", ontoType);

            query.updatedActiveSlotStatus_I(slotName);

            if (slotName.contentEquals("obtainAdditionalLanguageName")) {
              query.updateRDFInputContent(activeDIP, SlotLists.S7LanguageSlots, "Language",
                  "AdditionalLanguageName");
            }

            if (slotName.contentEquals("confirmLanguage")) {
              /* Get the scenario name selected by user */
              String scenario = query.scenarioSelection_S();

              query.updatedActiveSlotStatus_I(slotName);

              this.commit = true;
              this.scenario = scenario;
            }
          }

        } else if (SlotLists.locSlots.contains(slotName)
            || SlotLists.previousResidence.contains(slotName)) {

          if (type.toLowerCase().contains("gpe")
              || type.toLowerCase().contentEquals("norp")
              || type.toLowerCase().contentEquals("loc")
              || type.toLowerCase().contentEquals("fac")
              || (locationSlot && !type.toLowerCase().contentEquals("cardinal"))
              || form.toLowerCase().contentEquals("text")) {

            /* In case of a location object parse also
             * the location property from the entity object */
            Double lat = 0.0;
            Double lng = 0.0;
            if (entity.containsKey("locations")) {
              if (location.size() > 0) {
                for (Object l : location) {
                  JSONObject loc = (JSONObject) l;

                  // GET longitude and latitude
                  lng = (Double) loc.get("longitude");
                  lat = (Double) loc.get("latitude");

                  break;
                }
              } else {
                double[] coords;
                coords = query.checkExistingLocations_S(anchor);

                if (coords[0] != 0.0) {
                  lat = coords[0];
                  lng = coords[1];
                }
              }
            }

            if (slotName.contentEquals("obtainStreetName")) {
              String tr = convertTextualNumbersInDocument(fullTranscription);

              Pattern p = Pattern.compile("\\d+");
              Matcher m = p.matcher(tr);

              int number = -1;

              while (m.find()) {
                number = Integer.parseInt(m.group());
              }

              if (number != -1) {
                IRI streetNumber = Utilities.f
                    .createIRI(WELCOME.NAMESPACE, "StreetNumber");

                updateProfile(builder, streetNumber, String.valueOf(number), sim, activeDIP,
                    slotName);
              }
            }

            /* Split IRI from actual name */
            String predicate = util.splitIRI(ontoType);

            String final_anchor = anchor.substring(0, 1).toUpperCase() + anchor.substring(1);

            if (predicate.contains("City")) {
              updateProfileCoords(builder, ontoType, final_anchor, lng, lat, sim);
            } else {
              if (form.toLowerCase().contentEquals("text")
                  && slotName.contentEquals("obtainStreetName")) {
                String output = fullTranscription.replaceAll("\\d", "").trim();
                String final_output = output.substring(0, 1).toUpperCase() + anchor.substring(1);
                updateProfile(builder, ontoType, final_output, sim, activeDIP, slotName);
              } else {
                updateProfile(builder, ontoType, final_anchor, sim, activeDIP, slotName);
              }
            }

            query.updatedActiveSlotStatus_I(slotName);

            if (slotName.contentEquals("obtainAdditionalLanguageCountryName")) {
              query.updateRDFInputContent(activeDIP, SlotLists.S7CountrySlots, "Country",
                  "AdditionalLanguageCountryName");
            }
          }

          if (SlotLists.previousResidence.contains(slotName) &&
              (anchor.toLowerCase().contentEquals("no")
                  || anchor.toLowerCase().contentEquals("not"))) {

            updateProfile(builder, ontoType, "No", sim, activeDIP, slotName);

            query.updatedActiveSlotStatus_I(slotName);

          }
        } else if (SlotLists.numSlots.contains(slotName)
            || SlotLists.dateSlots.contains(slotName)
            || SlotLists.durationSlots.contains(slotName)) {

          if (type.toLowerCase().contentEquals("quantity")
              || type.toLowerCase().contentEquals("cardinal")
              || type.toLowerCase().contentEquals("ordinal")
              || type.toLowerCase().contentEquals("number")
              || type.toLowerCase().contentEquals("date")
              || type.toLowerCase().contentEquals("duration")
              || util.isNumeric(anchor)
              || anchor.matches("^(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]+$")) {

            /* Since all numbers will be delivered in words i.e.
             * "twenty" instead of "20" we need to convert them
             * to digit format.
             */
            if (type.toLowerCase().contentEquals("quantity")
                || type.toLowerCase().contentEquals("cardinal")
                || type.toLowerCase().contentEquals("number")
                || type.toLowerCase().contentEquals("duration")) {

              anchor = convertTextualNumbersInDocument(anchor);
              String finalAnchor = anchor.replaceAll("[^\\d]", "");

              if (!anchor.contentEquals("")) {
                if (type.toLowerCase().contentEquals("duration")) {
                  updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);
                } else {
                  if (slotName.contentEquals("obtainAsylumPreRegistrationNumber")) {
                    updateProfile(builder, ontoType, finalAnchor, sim, activeDIP, slotName);

                    /* We store the value in the ProfileInfo as well */
                    updateProfile(builder, ontoType, finalAnchor, 0, activeDIP, slotName);
                  } else {
                    updateProfile(builder, ontoType, finalAnchor, sim, activeDIP, slotName);
                  }
                }
                query.updatedActiveSlotStatus_I(slotName);
              }

            } else if (type.toLowerCase().contentEquals("date")) {

              /* In case of a date entity read the temporal analysis results */
              if (entity.get("temporal_analysis") != null) {
                JSONObject temporal_analysis = (JSONObject) entity.get("temporal_analysis");
                String value = (String) temporal_analysis.get("value");

                if ((slotName.contentEquals("obtainBirthDay")
                    || slotName.contentEquals("obtainBirthday"))
                    && validateJavaDate(value)) {

                  /* Get the scenario name selected by user */
                  String scenario = query.scenarioSelection_S();

                  if (scenario.contentEquals("Simulate Appointment Legal Service Praksis")) {
                    if (slotName.contentEquals("obtainBirthday")) {

                      /* Split IRI from actual name */
                      String predicate = util.splitIRI(ontoType);

                      query.setBSlotStatus_I(activeDIP, slotName, "Completed", anchor, "1.0",
                          predicate);
                    }
                  } else {
                    updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);
                  }

                  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                  //Parsing the given String to Date object
                  Date date = formatter.parse(value);

                  IRI yearType = Utilities.f
                      .createIRI(WELCOME.NAMESPACE, "BirthYear");

                  String[] split = anchor.split("-");
                  String year = split[0];

                  updateProfile(builder, yearType, year, sim, activeDIP, slotName);

                  IRI monthType = Utilities.f
                      .createIRI(WELCOME.NAMESPACE, "BirthMonth");

                  Map<String, Integer> map = new HashMap<>();

                  int score = 1;
                  for (String month : SlotLists.months) {
                    map.put(month, score);
                    score += 1;
                  }

                  String month = "";
                  for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    if (entry.getValue().equals(date.getMonth() + 1)) {
                      month = entry.getKey();
                      break;
                    }
                  }

                  updateProfile(builder, monthType, month, sim, activeDIP, slotName);

                  query.updatedActiveSlotStatus_I(slotName);
                } else if ((slotName.contentEquals("obtainBirthDay")
                    || slotName.contentEquals("obtainBirthday"))
                    && validateJavaDateYM(value)) {

                  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");

                  //Parsing the given String to Date object
                  Date dateYM = formatter.parse(value);

                  IRI yearType = Utilities.f
                      .createIRI(WELCOME.NAMESPACE, "BirthYear");

                  String[] split = anchor.split("-");
                  String year = split[0];

                  updateProfile(builder, yearType, year, sim, activeDIP, slotName);

                  IRI monthType = Utilities.f
                      .createIRI(WELCOME.NAMESPACE, "BirthMonth");

                  Map<String, Integer> map = new HashMap<>();

                  int score = 1;
                  for (String month : SlotLists.months) {
                    map.put(month, score);
                    score += 1;
                  }

                  String month = "";
                  for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    if (entry.getValue().equals(dateYM.getMonth() + 1)) {
                      month = entry.getKey();
                      break;
                    }
                  }

                  updateProfile(builder, monthType, month, sim, activeDIP, slotName);

                  /* Get the scenario name selected by user */
                  String scenario = query.scenarioSelection_S();

                  if (scenario.contentEquals("Simulate Appointment Legal Service Praksis")) {
                    if (slotName.contentEquals("obtainBirthday")) {

                      /* Split IRI from actual name */
                      String predicate = util.splitIRI(ontoType);

                      query.setBSlotStatus_I(activeDIP, slotName, "Completed", value, "1.0",
                          predicate);
                    }
                  }

                  query.updatedActiveSlotStatus_I(slotName);
                } else if (slotName.contentEquals("obtainBirthMonth")) {
                  updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);

                  query.updatedActiveSlotStatus_I(slotName);
                } else if (slotName.contentEquals("obtainBirthYear")) {
                  updateProfile(builder, ontoType, value, sim, activeDIP, slotName);

                  query.updatedActiveSlotStatus_I(slotName);
                } else {
                  /* Get the scenario name selected by user */
                  String scenario = query.scenarioSelection_S();

                  if (scenario.contentEquals("Simulate Appointment Legal Service Praksis")) {
                    if (slotName.contentEquals("obtainBirthday")) {

                      /* Split IRI from actual name */
                      String predicate = util.splitIRI(ontoType);

                      query.setBSlotStatus_I(activeDIP, slotName, "Completed", value, "1.0",
                          predicate);
                    }
                  } else {
//                    if (slotName.contentEquals("obtainStartingDate")) {
//                      /* Create endDate IRI */
//                      IRI endDate = Utilities.f
//                          .createIRI(WELCOME.NAMESPACE, "EndDate");
//
//                      updateProfile(builder, endDate, "Current", sim, activeDIP, "null");
//                    }
                    updateProfile(builder, ontoType, value, sim, activeDIP, slotName);
                  }

                  query.updatedActiveSlotStatus_I(slotName);
                }
              }
            } else if (type.toLowerCase().contentEquals("ordinal")) {
              Map<String, Integer> map = new HashMap<>();

              int num = 1;
              for (String ord : SlotLists.ordinal) {
                map.put(ord, num);
                num += 1;
              }

              for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getKey().equals(anchor)) {
                  anchor = String.valueOf(entry.getValue());
                  break;
                }
              }

              updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);

              query.updatedActiveSlotStatus_I(slotName);
            } else {
//              String scenario = query.scenarioSelection_S();
//
//              if (scenario.contentEquals("Simulate Appointment Legal Service Praksis")) {
//                if (slotName.contentEquals("obtainCardNumber")) {
//                  /* Create IRI for Language */
//                  IRI langType = Utilities.f
//                      .createIRI(WELCOME.NAMESPACE, "OfficerRoleFlag");
//                  String flag = query.getProfileValue(langType, sim);
//
//                  if (flag.contentEquals("false")) {
//                    updateProfile(builder, ontoType, anchor, sim);
//                  } else {
//                    /* Split IRI from actual name */
//                    String pred = util.splitIRI(ontoType);
//                    query.setBSlotStatus_I(activeDIP, slotName, "Completed", anchor, "1.0", pred);
//                  }
//                }
//              }

              if (slotName.contentEquals("obtainAsylumPreRegistrationNumber")) {
                updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);

                /* We store the value in the ProfileInfo as well */
                updateProfile(builder, ontoType, anchor, 0, activeDIP, slotName);
              } else {
                updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);
              }

              query.updatedActiveSlotStatus_I(slotName);
            }
          }
        } else if (SlotLists.multipleSlots.contains(slotName)) {
          SimilarityStrategy strategy = new JaroWinklerStrategy();
          StringSimilarityService service = new StringSimilarityServiceImpl(strategy);

          Map<String, Double> map = new HashMap<>();
          double score;
          for (String choice : SlotLists.multipleChoice) {
            if (anchor.toLowerCase().contains(choice.toLowerCase())) {
              score = 1.0;
            } else {
              score = service.score(anchor.toLowerCase(), choice.toLowerCase());
            }
            map.put(choice, score);
          }

          Map.Entry<String, Double> maxEntry = null;
          for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (maxEntry == null || entry.getValue()
                .compareTo(maxEntry.getValue()) > 0) {
              maxEntry = entry;
            }
          }

          if (maxEntry.getValue() > 0.8) {
            updateProfile(builder, ontoType, maxEntry.getKey(), sim, activeDIP, slotName);

            query.updatedActiveSlotStatus_I(slotName);
          }


        } else if (SlotLists.orgSlots.contains(slotName)) {
          if (type.toLowerCase().contentEquals("org")
              || type.toLowerCase().contentEquals("fac")) {
            updateProfile(builder, ontoType, anchor, sim, activeDIP, slotName);

            query.updatedActiveSlotStatus_I(slotName);
          }

        } else if (slotName.contentEquals("confirmTimeSlot")
            || slotName.contentEquals("obtainConfirmationTimeSlot")) {
          if (type.toLowerCase().contentEquals("date")) {
            if (SlotLists.dow.contains(anchor.toLowerCase())) {
                            /* In case the user did not agree with the propose
                               check if there is another Free slot
                            */
              boolean check = query.checkFreeSlot_A();

              if (check) {
                /* Update the previous suggestion to "Rejected" */
                query.updateTimeSlotStatus(activeDIP);

                /* Delete profile info */
                query.deleteProfileInfo_D("AppointmentDay", sim);
                query.deleteProfileInfo_D("AppointmentDate", sim);
                query.deleteProfileInfo_D("AppointmentTime", sim);
                query.deleteProfileInfo_D("AppointmentTimeEarlier", sim);

                /* Propose a new time slot */
                query.updateProposeTimeSlot(activeDIP, false, "");

                /* Also increment Number of Attempts */
                query.incrementNumberOfAttempts_I(activeDIP, slotName);

                if (slotName.contentEquals("confirmTimeSlot")) {
                  /* Change status of proposeTimeSlot to "Pending" */
                  query.setInfoSlotStatus_I(activeDIP, "proposeTimeSlot", "Pending", "0.0");
                } else {
                  /* Change status of proposeTimeSlot to "Pending" */
                  query.setInfoSlotStatus_I(activeDIP, "informProposeTimeSlot", "Pending", "0.0");
                }
                query.updatedActiveSlotStatus_I(slotName);
              }
            }
          } else if (anchor.toLowerCase().contentEquals("no")
              || anchor.toLowerCase().contentEquals("not")
              || anchor.toLowerCase().contentEquals("unfortunately")) {
            /* In case the user did not agree with the proposal check if there is another Free slot */
            boolean check = query.checkFreeSlot_A();

            if (check) {
              /* Update the previous suggestion to "Rejected" */
              query.updateTimeSlotStatus(activeDIP);

              /* Delete profile info */
              query.deleteProfileInfo_D("AppointmentDay", sim);
              query.deleteProfileInfo_D("AppointmentDate", sim);
              query.deleteProfileInfo_D("AppointmentTime", sim);
              query.deleteProfileInfo_D("AppointmentTimeEarlier", sim);

              /* Propose a new time slot */
              query.updateProposeTimeSlot(activeDIP, false, "");

              /* Also increment Number of Attempts */
              query.incrementNumberOfAttempts_I(activeDIP, slotName);

              if (slotName.contentEquals("confirmTimeSlot")) {
                /* Change status of proposeTimeSlot to "Pending" */
                query.setInfoSlotStatus_I(activeDIP, "proposeTimeSlot", "Pending", "0.0");
              } else {
                /* Change status of proposeTimeSlot to "Pending" */
                query.setInfoSlotStatus_I(activeDIP, "informProposeTimeSlot", "Pending", "0.0");
              }
              query.updatedActiveSlotStatus_I(slotName);
            }
          }
        } else {
          final boolean b = anchor.toLowerCase().contentEquals("yes")
              || anchor.toLowerCase().contentEquals("ok")
              || anchor.toLowerCase().contentEquals("okay");
          if (SlotLists.booleanSlots.contains(slotName)) {
            if (b) {
              query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", null);

              query.updatedActiveSlotStatus_I(slotName);
            } else if (anchor.toLowerCase().contentEquals("no")
                || anchor.toLowerCase().contentEquals("not")) {
              query.setBSlotStatus_I(activeDIP, slotName, "Completed", "No", "1.0", null);

              query.updatedActiveSlotStatus_I(slotName);
            }
          } else if (SlotLists.confirmSlots.contains(slotName)) {

            if (b) {
              updateProfile(builder, ontoType, "Yes", sim, activeDIP, slotName);

              query.updatedActiveSlotStatus_I(slotName);
            } else if (anchor.toLowerCase().contentEquals("no")
                || anchor.toLowerCase().contentEquals("not")) {
              updateProfile(builder, ontoType, "No", sim, activeDIP, slotName);

              query.updatedActiveSlotStatus_I(slotName);
            }
          }
        }

        if (SlotLists.negationSlots.contains(slotName) &&
            (anchor.toLowerCase().contentEquals("no")
                || anchor.toLowerCase().contentEquals("not"))) {

          /* Split IRI from actual name */
          String predicate = util.splitIRI(ontoType);

          if (slotName.contentEquals("obtainCardNumber")) {
            /* Create IRI for Language */
            IRI langType = Utilities.f
                .createIRI(WELCOME.NAMESPACE, "OfficerRoleFlag");
            String flag = query.getProfileValue(langType, sim);

            if (flag.contentEquals("false")) {
              updateProfile(builder, ontoType, "Unknown", sim, activeDIP, slotName);
            } else {
              /* Split IRI from actual name */
              String pred = util.splitIRI(ontoType);
              query.setBSlotStatus_I(activeDIP, slotName, "Completed", "Unknown", "1.0", pred);
            }
          } else if (slotName.contentEquals("obtainPhoneNumber")
              || slotName.contentEquals("obtainPhoneAppointmentRejection")) {
            query.setBSlotStatus_I(activeDIP, slotName, "Undefined", "Unknown", "1.0", predicate);
          } else if (slotName.contentEquals("obtainAsylumPreRegistrationNumber")) { /* s2a */
            updateProfile(builder, ontoType, "Unknown", sim, activeDIP, slotName);
          } else {
            updateProfile(builder, ontoType, "No", sim, activeDIP, slotName);
          }

          query.updatedActiveSlotStatus_I(slotName);
        }
      }
    }

    return builder;
  }

  /**
   * Creates a commit container.
   *
   * @param anchor
   * @param timestamp
   */
  public ModelBuilder createCommitContainer(String anchor, long timestamp, ModelBuilder builder,
      String scenario) {
    /* Get active graph (i.e. active DIP) */
    IRI activeGraph = query.activeDIP_S();

    /* Create IRI for speech act Service Profile and Process */
    IRI speech = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "C-SpeechAct" + timestamp);
    IRI service = Utilities.f
        .createIRI(SERVICE.NAMESPACE, "Service-" + timestamp);
    IRI profile = Utilities.f
        .createIRI(PROFILE.NAMESPACE, "Profile-" + timestamp);
    IRI process = Utilities.f
        .createIRI(PROCESS.NAMESPACE, "Process-" + timestamp);

    /* Set namespaces */
    builder
        .setNamespace("service", SERVICE.NAMESPACE);
    builder
        .setNamespace("profile", PROFILE.NAMESPACE);
    builder
        .setNamespace("process", PROCESS.NAMESPACE);

    /* Add speech act to builder */
    builder
        .namedGraph(activeGraph)
        .subject(speech)
        .add(RDF.TYPE, WELCOME.CONTAINER)
        .add(WELCOME.HASCONTAINERID, timestamp)
        .add(WELCOME.TRANSCRIPTION, anchor)
        .add(WELCOME.SPEECHOBJECT, service)
        .add(WELCOME.SPEECHTYPE, WELCOME.COMMIT)
        .add(WELCOME.ISLASTCONTAINER, "true");

    /* Add service to builder */
    builder
        .subject(service)
        .add(RDF.TYPE, SERVICE.SERVICE)
        .add(RDF.TYPE, WELCOME.SERVICEREQUEST)
        .add(SERVICE.DESCRIBEDBY, process)
        .add(SERVICE.PRESENTS, profile);

    /* Add process to builder */
    builder
        .subject(process)
        .add(RDF.TYPE, PROCESS.ATOMIC)
        .add(SERVICE.DESCRIBES, service);

    /* Add profile to builder */
    builder
        .subject(profile)
        .add(RDF.TYPE, PROFILE.PROFILE)
        .add(SERVICE.PRESENTEDBY, service)
        .add(PROFILE.DESCRIPTION, scenario)
        .add(PROFILE.NAME, scenario);

    return builder;
  }

  /**
   * @param isCommit
   * @param turnId
   * @return
   * @throws IOException
   */
  public String exportDialogueTurn(boolean isCommit, long turnId) throws IOException {
    // Check if the Active DIP is the OpeningDIP
    String askQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    askQuery += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    askQuery += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n";
    askQuery += "ASK WHERE { \n";
    askQuery += "	?s rdf:type welcome:DIP ; \n";
    askQuery += "    welcome:isActive true . \n";
    askQuery += "    FILTER(CONTAINS(STR(?s), \"Opening\")) \n";
    askQuery += "} \n";

    BooleanQuery booleanQuery = Utilities.connection
        .prepareBooleanQuery(QueryLanguage.SPARQL, askQuery);

    boolean results = booleanQuery.evaluate();

    String namespace = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#";
    ValueFactory f = Utilities.repository.getValueFactory();

    // CREATE IRI
    IRI tempDIP = f.createIRI(namespace, "tempTurn");

    // In case the active DIP is the welcome DIP we export the dialogue turn
    if (results || isCommit) {
      String queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "PREFIX service: <http://www.daml.org/services/owl-s/1.1/Service.owl#> \n";
      queryString += "PREFIX profile: <http://www.daml.org/services/owl-s/1.1/Profile.owl#> \n";
      queryString += "PREFIX process: <http://www.daml.org/services/owl-s/1.1/Process.owl#> \n";
      queryString += "INSERT { \n";
      queryString += "GRAPH <" + tempDIP + "> { \n";
      queryString += "	?d rdf:type welcome:DialogueUserTurn ; \n";
      queryString += "       welcome:hasSpeechActContainer ?container ; \n";
      queryString += "       welcome:hasTurnTranscription ?transcription ; \n";
      queryString += "       welcome:hasTimestamp ?timestamp . \n";
      queryString += "    ?container rdf:type welcome:SpeechActContainer ; \n";
      queryString += "               welcome:hasContainerTranscription ?tr ; \n";
      queryString += "               welcome:hasNextContainer ?next ; \n";
      queryString += "               welcome:hasPreviousContainer ?prev ; \n";
      queryString += "               welcome:hasSpeechActType ?type ; \n";
      queryString += "               welcome:hasContainerId ?id ; \n";
      queryString += "               welcome:isLastContainer ?flag . \n";
      if (isCommit) {
        queryString += "    ?container welcome:hasSpeechActObject ?service . \n";
        queryString += "    ?service rdf:type service:Service ; \n";
        queryString += "             rdf:type welcome:ServiceRequest ; \n";
        queryString += "             service:describedBy ?process ; \n";
        queryString += "             service:presents ?profile . \n";
        queryString += "    ?process rdf:type process:AtomicProcess ; \n";
        queryString += "             service:described ?service . \n";
        queryString += "    ?profile rdf:type profile:Profile ; \n";
        queryString += "             service:presentedBy ?service . \n";
        queryString += "    ?profile profile:textDescription ?text . \n";
      }
      queryString += "} \n";
      queryString += "} WHERE { \n";
      queryString += "   	?d rdf:type welcome:DialogueUserTurn ; \n";
      queryString += "   	   welcome:id ?turnid ; \n";
      queryString += "       welcome:hasSpeechActContainer ?container ; \n";
      queryString += "       welcome:hasTurnTranscription ?transcription ; \n";
      queryString += "       welcome:hasTimestamp ?timestamp . \n";
      queryString += "    ?container rdf:type welcome:SpeechActContainer ; \n";
      queryString += "               welcome:hasContainerTranscription ?tr ; \n";
      queryString += "               welcome:hasSpeechActType ?type ; \n";
      queryString += "               welcome:hasContainerId ?id ; \n";
      queryString += "               welcome:isLastContainer ?flag . \n";
      queryString += "    OPTIONAL { \n";
      queryString += "       ?container welcome:hasNextContainer ?next ; \n";
      queryString += "    } \n";
      queryString += "    OPTIONAL { \n";
      queryString += "       ?container welcome:hasPreviousContainer ?prev . \n";
      queryString += "    } \n";
      if (isCommit) {
        queryString += "    OPTIONAL { \n";
        queryString += "    ?container welcome:hasSpeechActObject ?service . \n";
        queryString += "    ?service service:describedBy ?process ; \n";
        queryString += "             service:presents ?profile . \n";
        queryString += "    ?profile profile:textDescription ?text . \n";
        queryString += "    } \n";
      }
      queryString += "    FILTER(?turnid = STR(\"" + turnId + "\")) \n";
      queryString += "} \n";

      // Execute Update
      util.executeQuery(queryString);

      // The following query assigns any values found in the ontology to the DIP fields.
      queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
      queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
      queryString += "DELETE { \n";
      queryString += "    ?s welcome:isActive true . \n";
      queryString += "} \n";
      queryString += "INSERT { \n";
      queryString += "GRAPH ?g { \n";
      queryString += "    ?s welcome:isActive false . \n";
      queryString += "} \n";
      queryString += "} WHERE { \n";
      queryString += "GRAPH ?g { \n";
      queryString += "	?s rdf:type welcome:DIP ; \n";
      queryString += "       welcome:isActive true . \n";
      queryString +=
          "    FILTER(CONTAINS(STR(?s), \"Opening\") || CONTAINS(STR(?s), \"RegistrationStatus\") "
              +
              "|| CONTAINS(STR(?s), \"ProposeService\") ) \n";
      queryString += "} \n";
      queryString += "} \n";

      // Execute Update
      util.executeQuery(queryString);
    }

    return exportTempGraph(tempDIP);
  }

  /**
   * @param namedGraph
   * @param userObject
   * @param actObject
   */
  public void createContainerService(Resource namedGraph, Resource userObject, Resource actObject) {
    // Create unique id
    String turnId = UUID.randomUUID().toString();

    ModelBuilder builder = new ModelBuilder();
    String service = "http://www.daml.org/services/owl-s/1.1/Service.owl#";
    String profile = "http://www.daml.org/services/owl-s/1.1/Profile.owl#";
    String process = "http://www.daml.org/services/owl-s/1.1/Process.owl#";
    builder.setNamespace("service", service);
    builder.setNamespace("profile", profile);
    builder.setNamespace("process", process);

    // Create IRI
    IRI serviceObject = Utilities.f.createIRI(service, "Service-" + turnId);
    IRI profileObject = Utilities.f.createIRI(profile, "Profile-" + turnId);
    IRI processObject = Utilities.f.createIRI(process, "Process-" + turnId);

    String textDescription = (WELCOME.REGISTERED).stringValue() + " " + userObject.stringValue();

    // CREATE MODEL
    builder.namedGraph(namedGraph).subject(namedGraph)
        .subject(actObject)
        .add(WELCOME.SPEECHOBJECT, serviceObject)
        .subject(serviceObject)
        .add(RDF.TYPE, SERVICE.SERVICE)
        .add(RDF.TYPE, WELCOME.SERVICEREQUEST)
        .add(SERVICE.DESCRIBEDBY, processObject)
        .add(SERVICE.PRESENTS, profileObject)
        .subject(processObject)
        .add(RDF.TYPE, PROCESS.ATOMIC)
        .add(SERVICE.DESCRIBES, serviceObject)
        .subject(profileObject)
        .add(RDF.TYPE, PROFILE.PROFILE)
        .add(SERVICE.PRESENTEDBY, serviceObject)
        .add(PROFILE.DESCRIPTION, textDescription);

    // We're done building, create our Model
    Model model = builder.build();

    // Commit model
    util.commitModel(model);
  }

  /**
   * @return
   * @throws IOException
   */
  public String updateProposeServiceDIP() throws IOException {
    // The following query assigns a unique ID to each DIP that has been sent by the agent.
    String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "SELECT ?x \n";
    queryString += "WHERE { \n";
    queryString += "    ?x rdf:type welcome:DIP . \n";
    queryString += "    FILTER(CONTAINS(STR(?x), \"ProposeService\")) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    ModelBuilder builder = new ModelBuilder();
    String namespace = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#";
    builder.setNamespace("welcome", namespace);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        logger.info("(INFO) Updating ProposeService DIP status.");

        queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
        queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
        queryString += "DELETE { \n";
        queryString += "    ?slot welcome:hasStatus ?status . \n";
        queryString += "} \n";
        queryString += "INSERT { \n";
        queryString += "    GRAPH ?g { \n";
        queryString += "    	?slot welcome:hasStatus welcome:Completed . \n";
        queryString += "    } \n";
        queryString += "} \n";
        queryString += "WHERE { \n";
        queryString += "    GRAPH ?g { \n";
        queryString += "    	?s welcome:hasSlot ?slot . \n";
        queryString += "        ?slot welcome:hasStatus ?status . \n";
        queryString += "    	FILTER(CONTAINS(STR(?slot), \"obtainInterest\")) \n";
        queryString += "    } \n";
        queryString += "} \n";

        // Execute Update
        util.executeQuery(queryString);
      }
    }

    // Find the active DIP
    return util.exportActiveDIP();
  }

  /**
   * @param namedGraph
   * @return
   * @throws IOException
   */
  public String exportTempGraph(Resource namedGraph) throws IOException {
    // Create a file output stream
    FileOutputStream out = new FileOutputStream("graphExport.ttl");

    // Export named graph into statements
    RepositoryResult<Statement> statements = Utilities.connection.getStatements(null, null, null,
        namedGraph);

    // Store results in a Model
    Model m = QueryResults.asModel(statements);

    // Define namespaces
    m.setNamespace(WELCOME.NS);
    m.setNamespace(RDF.NS);
    m.setNamespace(SERVICE.NS);
    m.setNamespace(PROCESS.NS);
    m.setNamespace(PROFILE.NS);

    // Write Model to file
    try {
      Rio.write(m, out, RDFFormat.TURTLE);
    } finally {
      out.close();
    }

    removeTempGraph(namedGraph);

    // Load file to a string
    String path = "graphExport.ttl";
    String content = "";
    try (Stream<String> lines = Files.lines(Paths.get(path))) {
      content = lines.collect(Collectors.joining(System.lineSeparator()));

      logger.info("Sending notification to agent: receiveSpeechAct");

      // Send data to the agent-core
      util.sendData(content, "agent-core", "receiveSpeechAct", "text/turtle");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return content;
  }

  /**
   * @param namedGraph
   */
  public void removeTempGraph(Resource namedGraph) {
    Utilities.connection.clear(namedGraph);
  }

  public ModelBuilder
  updateProfile(ModelBuilder builder, IRI ontoType, String anchor, Integer sim, IRI activeDIP,
      String slotName) {
    /* Create IRI for the field */
    BNode iri = Utilities.f.createBNode();

    /* Get user's IRI */
    IRI userIRI = wpm.getUser();

    /* Split IRI from actual name */
    String predicate = util.splitIRI(ontoType);

    query.deleteProfileInfo_D(predicate, sim);

    long timestamp = System.currentTimeMillis() / 1000;

    /* Initialize RDF builder */
    ModelBuilder b = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    if (sim == 1) {
      /* Add fields to builder */
      b.namedGraph("welcome:ProfileInfoSimulation")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);
    } else if (sim == 0) {
      /* Add fields to builder */
      b.namedGraph("welcome:ProfileInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);
    } else if (sim == 2) {
      /* Add fields to builder */
      b.namedGraph("welcome:CVInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);
    } else if (sim == 3) {
      /* Add fields to builder */
      b.namedGraph("welcome:tempGraph")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor)
          .add("welcome:lastUpdated", timestamp);

      if (!slotName.contentEquals("null")) {
        query.setBSlotStatus_I(activeDIP, slotName, "Completed", anchor, "1.0", predicate);
      }
    }

    /* We're done building, create our Model */
    Model model = b.build();

    logger.info("(REPO) Updating TCN profile with LAS input.");

    /* Commit model to repository */
    util.commitModel(model);

    return builder;
  }

  public ModelBuilder
  updateProfileCoords(ModelBuilder builder, IRI ontoType, String anchor, Double lng, Double lat,
      Integer sim) {
    /* Create IRI for the field */
    BNode iri = Utilities.f.createBNode();

    /* Get user's IRI */
    IRI userIRI = wpm.getUser();

    /* Split IRI from actual name */
    String predicate = util.splitIRI(ontoType);

    query.deleteProfileInfo_D(predicate, sim);

    long timestamp = System.currentTimeMillis() / 1000;

    /* Initialize RDF builder */
    ModelBuilder b = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    if (sim == 1) {
      /* Add fields to builder */
      b.namedGraph("welcome:ProfileInfoSimulation")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor.replaceAll("\\d", ""))
          .add("welcome:long", lng)
          .add("welcome:lat", lat)
          .add("welcome:lastUpdated", timestamp);
    } else if (sim == 0) {
      /* Add fields to builder */
      b.namedGraph("welcome:ProfileInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor.replaceAll("\\d", ""))
          .add("welcome:long", lng)
          .add("welcome:lat", lat)
          .add("welcome:lastUpdated", timestamp);
    } else if (sim == 2) {
      /* Add fields to builder */
      b.namedGraph("welcome:CVInfo")
          .subject(userIRI)
          .add("welcome:has" + predicate, iri)
          .subject(iri)
          .add(iri, RDF.TYPE, ontoType)
          .add("welcome:hasValue", anchor.replaceAll("\\d", ""))
          .add("welcome:long", lng)
          .add("welcome:lat", lat)
          .add("welcome:lastUpdated", timestamp);
    }

    /* We're done building, create our Model */
    Model model = b.build();

    logger.info("(REPO) Updating TCN profile with LAS input.");

    /* Commit model to repository */
    util.commitModel(model);

    return builder;
  }

  /* Function that sends notification to agent-core in s7 scenario */
  public void faqNotification2Agent() {
    String notification2Agent = "";
    notification2Agent = "@prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> .\n";
    notification2Agent += "welcome:KMSNotifies welcome:FAQRequest \"true\" .  \n";

    logger.info(notification2Agent);

    logger.info("Sending notification to agent: foundInternalFAQ ");

    /* Send data to the Agent-Core */
    util.sendData(notification2Agent, "agent-core", "foundInternalFAQ ", "text/turtle");
  }

  /* Function that sends notification to agent-core in s7 scenario */
  public void faqIdNotification2Agent() {
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

    logger.info("Sending notification to agent: foundExternalFAQ");

    /* Send data to the Agent-Core */
    util.sendData(notification2Agent, "agent-core", "foundExternalFAQ", "text/turtle");
  }
}
