package com.welcome.services;

import com.welcome.auxiliary.Languages;
import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import com.welcome.scenarios.CV;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WPM {

  Logger logger = LoggerFactory.getLogger(Utilities.class);

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries q = new Queries();
  CV cv = new CV();

  /**
   * Wrapper function that handles handshaking.
   *
   * @return
   */
  public String wrapperHandShaking(String logStatus, long start) {

    /* Check if user exists */
    Boolean status = checkUser(logStatus);

    if (status) {
      /* load the WELCOME ontology file */
      util.loadOntologyURL("welcome", "welcomeOntology");

      /* load the FAQ ontology file */
      util.loadOntologyURL("faq", "faqOntology");

      /* load the CV ontology file */
      util.loadOntologyURL("cv", "cvOntology");

      /* load the AUX ontology file */
      util.loadOntologyURL("auxiliary", "auxOntology");

      /* load the Languages' ontology file */
      util.loadOntologyURL("languages", "langOntology");
    }

    q.checkCHCPreferences();

    /* Clear SlotInfo graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "SlotInfo");
    Utilities.connection.clear(graph);

    /* Send handshaking to agent */
    return (sendHandShaking(status, start));
  }

  public String wrapperSpeakToAvatar(String menuOption, long start) {

    // Read JSON file containing information about DMS Input
    JSONParser parser = new JSONParser();

    // Create JSONObject of DMS Input
    JSONObject object = new JSONObject();

    try {
      // Try to parse message
      logger.info("(INFO) Parsing json(-ld) to RDF.");
      object = (JSONObject) parser.parse(menuOption);
    } catch (org.json.simple.parser.ParseException e) {
      logger.error("(ERROR) Unable to parse message!");
    }

    /* Get speech act type and anchor */
    menuOption = (String) object.get("about");

    String menuLC = menuOption.toLowerCase();
    String sectionOption = "None";

    IRI graph;

    /* Remove old graph before updating the information */
    graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "ProfileInfoSimulation");
    Utilities.connection.clear(graph);

    IRI slotInfo = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "SlotInfo");
    Utilities.connection.clear(slotInfo);

    boolean schooling = false;
    boolean health = false;
    switch (menuLC) {
      case "firstreceptionservice":
        menuOption = "First Reception Service";
        break;
      case "registrationservice":
        menuOption = "Registration Service";
        break;
      case "asylumappointment":
        menuOption = "Asylum Appointment";
        break;
      case "trainingappointment": /* Simulation */
        menuOption = "Training Appointment";
        break;
      case "legalscenariofullregistration": /* S2a */
        menuOption = "Full Registration Procedure Asylum for Praksis";

        /* Create user IRI */
        IRI profile = Utilities.f
            .createIRI(WELCOME.NAMESPACE, "ProfileInfo");

        /* Remove AsylumPreRegistrationNumber */
        q.removeTCNData("AsylumPreRegistrationNumber", profile);
        break;
      case "legalscenarioappointment": /* S2b Simulation */
        menuOption = "Simulate Appointment Legal Service Praksis";
        /* Important for PRAKSIS S2b */
        updateRoleFlag();
        break;
      case "cvcreationscenario": /* S7 */
        menuOption = "Employment CV Creation";
        break;
      case "cvcreationscenariopersonal": /* S7 */
        menuOption = "Employment CV Creation";
        sectionOption = "PersonalInfo";
        break;
      case "cvcreationscenariolanguage": /* S7 */
        menuOption = "Employment CV Creation";
        sectionOption = "LanguageInformation";
        break;
      case "cvcreationscenarioeducation": /* S7 */
        menuOption = "Employment CV Creation";
        sectionOption = "EducationInformation";
        break;
      case "cvcreationscenariocourses": /* S7 */
        menuOption = "Employment CV Creation";
        sectionOption = "OtherEducationInformation";
        break;
      case "cvcreationscenarioemployment": /* S7 */
        menuOption = "Employment CV Creation";
        sectionOption = "EmploymentInformation";
        break;
      case "cvcreationscenarioskills": /* S7 */
        menuOption = "Employment CV Creation";
        sectionOption = "SkillInformation";
        break;
      case "cvcreationscenarioother": /* S7 */
        menuOption = "Employment CV Creation";
        sectionOption = "OtherInformation";
        break;
      case "schoolingscenariodife": /* S8 Dife */
        menuOption = "Schooling Professional Skills Recognition Diploma University Master Non-University qualification Spain";
        IRI dife_topics = Utilities.f
            .createIRI(WELCOME.NAMESPACE, "SchoolingTopics");
        Utilities.connection.clear(dife_topics);
        schooling = true;
        break;
      case "schoolingscenariocaritas": /* S8 Caritas */
        menuOption = "Schooling school degree university diploma recognition germany CARITAS";
        IRI car_topics = Utilities.f
            .createIRI(WELCOME.NAMESPACE, "SchoolingTopics");
        Utilities.connection.clear(car_topics);
        schooling = true;
        break;
      case "healthscenario": /* S5 */
        menuOption = "Greek Health System Healthcare";
        IRI health_topics = Utilities.f
            .createIRI(WELCOME.NAMESPACE, "HealthTopics");
        Utilities.connection.clear(health_topics);
        health = true;
        break;
      default:
        logger.warn("Not a valid menu option");
    }

    /* Init schooling topics */
    if (schooling) {
      q.initSubTopics("schooling");
    }

    /* Init health topics */
    if (health) {
      q.initSubTopics("health");
    }

    logger.info("(INFO) User selected scenario: " + menuOption);

    /* Create new session */
    createSession(menuOption, sectionOption);

    /* Clear temp graphs */
    cv.clearFAQGraphs();

    /* Send speak to avatar to agent */
    sendSpeakToAvatar();

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("SCE: Time Elapsed: " + timeElapsed);

    return menuOption;
  }

  /**
   * Wrapper function that handles registration.
   *
   * @param input
   */
  public void wrapperRegInput(String input) {
    /* load the Languages' ontology file */
    util.loadOntologyURL("languages", "langOntology");

    /* load the WELCOME ontology file */
    util.loadOntologyURL("welcome", "welcomeOntology");

    /* Create RDF Model */
    jsonToRdf(util.parseJSONLD(input));
  }

  /**
   * Takes a JSONObject and creates an RDF model.
   *
   * @param wpmObject
   */
  public void jsonToRdf(JSONObject wpmObject) {
    /* Check if user exists */
    checkUser(null);

    /* Remove old graph before updating the information */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "ProfileInfo");
    Utilities.connection.clear(graph);

    /* String that will capture the dynamically populated delete and where clause in query */
    String insertClause = "";

    /* String that will capture the final query */
    String queryString = "";

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";

    for (Iterator iterator = wpmObject.keySet().iterator(); iterator.hasNext(); ) {
      String key = (String) iterator.next();

      if (key.contentEquals("OtherLanguageCode")) {
        continue;
      }

      String value = (String) wpmObject.get(key);

      boolean b = false;

      if (key.contentEquals("Age")) {
        b = util.isNumeric(value);
      }

      if (!value.contentEquals("")) {
        insertClause += "    	welcome:tcn_user welcome:has" + key + " \n";
        insertClause += "        [   a welcome:" + key + " ; \n";

        if (b) {
          insertClause += "        	welcome:hasValue " + value + " ; \n";
        } else {
          insertClause += "        	welcome:hasValue \"" + value + "\" ; \n";
        }
        insertClause += "        	welcome:lastUpdated " + timestamp + " \n";
        insertClause += "        ]. \n";
      } else {
        q.removeTCNData(key, graph);
      }
    }

    insertClause += "    	welcome:tcn_user welcome:hasInitDataCollection \n";
    insertClause += "        [   a welcome:InitDataCollection ; \n";
    insertClause += "        	welcome:hasValue \"" + false + "\" ; \n";
    insertClause += "        	welcome:lastUpdated " + timestamp + " \n";
    insertClause += "        ]. \n";
    insertClause += "    	welcome:tcn_user welcome:hasAvatarConfig \n";
    insertClause += "        [   a welcome:AvatarConfig ; \n";
    insertClause += "        	welcome:hasValue \"" + false + "\" ; \n";
    insertClause += "        	welcome:lastUpdated " + timestamp + " \n";
    insertClause += "        ]. \n";

    queryString += "INSERT DATA { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += insertClause;
    queryString += "  } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    if (wpmObject.containsKey(("RegistrationStatus"))) {
      String status = (String) wpmObject.get("RegistrationStatus");
      if (status.contentEquals("true")) {
        q.insertRegStatus();
      } else {
        q.deleteRegStatus();
      }
    }

    /* Set default language to english */
    Locale locale = new Locale("en_US");
    Locale.setDefault(locale);

    if (wpmObject.containsKey("LanguageCode")) {
      String f6_iso = (String) wpmObject.get("LanguageCode");

      if (f6_iso.length() == 3) {
        String f6 = Languages.getNameFromCode(f6_iso);
        q.insertTCNData("Language", f6, graph);
      } else {
        /* Find the full language name */
        Locale l2 = new Locale(f6_iso, "");

        String language = l2.getDisplayLanguage();
        String f6 = (language != null) ? language : f6_iso;
        q.insertTCNData("Language", f6, graph);
      }
    }

    String natLang = null;
    if (wpmObject.containsKey("NativeLanguageCode")) {
      String f61_iso = (String) wpmObject.get("NativeLanguageCode");

      if (f61_iso.length() == 3) {
        String f61 = Languages.getNameFromCode(f61_iso);
        q.insertTCNData("NativeLanguageName", f61, graph);

        // assign for later usage
        natLang = f61;
      } else {
        /* Find the full language name */
        Locale l2 = new Locale(f61_iso, "");

        String language = l2.getDisplayLanguage();
        String f61 = (language != null) ? language : f61_iso;
        q.insertTCNData("NativeLanguageName", f61, graph);

        // assign for later usage
        natLang = f61;
      }
    }

    if (wpmObject.containsKey("OtherLanguageCode")) {

      if (wpmObject.get("OtherLanguageCode") instanceof JSONArray) {
        IRI user = Utilities.connection
            .getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "tcn_user");

        JSONArray languageCodeList = (JSONArray) wpmObject.get("OtherLanguageCode");

        IRI property1 = Utilities.connection
            .getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "speaksLanguage");

        IRI property2 = Utilities.connection
            .getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "hasOtherLanguageCode");

        List<String> languages = new ArrayList<>();
        List<String> languageCodes = new ArrayList<>();

        Iterator<String> it = languageCodeList.iterator();
        while (it.hasNext()) {
          String otherLanguageCode = it.next().replace("'", "");

          if (otherLanguageCode.length() == 3) {
            String f = Languages.getNameFromCode(otherLanguageCode);
            languages.add(f);
            languageCodes.add(otherLanguageCode);
          } else {
            /* Find the full language name */
            Locale l2 = new Locale(otherLanguageCode, "");

            String language = l2.getDisplayLanguage();
            String f = (language != null) ? language : otherLanguageCode;
            languages.add(f);
            languageCodes.add(otherLanguageCode);
          }
        }

        if (!languages.contains(natLang)) {
          languages.add(natLang);
        }

        BNode head1 = Utilities.f.createBNode();
        Model model1 = RDFCollections.asRDF(languages, head1, new LinkedHashModel());

        model1.add(user, property1, head1);

        util.commitModel(model1, graph);

        BNode head2 = Utilities.f.createBNode();
        Model model2 = RDFCollections.asRDF(languageCodes, head2, new LinkedHashModel());

        model2.add(user, property2, head2);

        util.commitModel(model2, graph);
      } else {
        String f62_iso = (String) wpmObject.get("OtherLanguageCode");
        q.insertTCNData("OtherLanguageCode", f62_iso, graph);

        if (f62_iso.length() == 3) {
          String f62 = Languages.getNameFromCode(f62_iso);
          q.insertTCNData("OtherLanguage", f62, graph);
        } else {
          /* Find the full language name */
          Locale l2 = new Locale(f62_iso, "");

          String language = l2.getDisplayLanguage();
          String f62 = (language != null) ? language : f62_iso;
          q.insertTCNData("OtherLanguage", f62, graph);
        }
      }
    }

    if (wpmObject.containsKey("CountryCode")) {
      String f8_iso = (String) wpmObject.get("CountryCode");

      /* Find the full country name */
      Locale l0 = new Locale("", f8_iso);

      String host_country = l0.getDisplayCountry();
      String f8 = (host_country != null) ? host_country : f8_iso;

      q.insertTCNData("Country", f8, graph);
    }

    if (wpmObject.containsKey("CountryOfOriginCode")) {
      String f8_iso = (String) wpmObject.get("CountryOfOriginCode");

      /* Find the full country name */
      Locale l0 = new Locale("", f8_iso);

      String host_country = l0.getDisplayCountry();
      String f8 = (host_country != null) ? host_country : f8_iso;

      q.insertTCNData("CountryOfOrigin", f8, graph);
    }

    /* Create IRI for CVStatus */
    IRI statusType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "CVStatus");

    String cvStatus = q.getProfileValue(statusType, 0);

    if (cvStatus.contentEquals("")) {
      /* Create user IRI */
      IRI profile = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "ProfileInfo");

      /* Insert CV Status */
      q.removeTCNData("CVStatus", profile);
      q.insertTCNData("CVStatus", "None", profile);
    }

    IRI cityType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "City");
    String city = q.getProfileValue(cityType, 0);

    String modified = city.substring(0, 1).toUpperCase() + city.substring(1).toLowerCase();

    double[] coords;
    coords = q.checkExistingLocations_S(modified);

    if (coords[0] != 0.0) {
      logger.info("Found existing location");

      Double lat = coords[0];
      Double lng = coords[1];

      /* Initialize RDF builder */
      ModelBuilder builder = new ModelBuilder()
          .setNamespace("welcome", WELCOME.NAMESPACE);

      updateProfileCoords(builder, cityType, modified, lng, lat, 0);
    } else {
      try {
        JSONObject obj = util.coordinatesRequest(modified);

        if (!obj.isEmpty()) {
          logger.info("Found coordinates with OSM");

          Double lat = Double.parseDouble((String) obj.get("lat"));
          Double lng = Double.parseDouble((String) obj.get("lon"));

          /* Initialize RDF builder */
          ModelBuilder builder = new ModelBuilder()
              .setNamespace("welcome", WELCOME.NAMESPACE);

          updateProfileCoords(builder, cityType, modified, lng, lat, 0);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    /* load the CV ontology file */
    util.loadOntologyURL("cv", "cvOntology");

    /* Create IRI for Skill */
    IRI skillGraph = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "SkillsGraph");
    Utilities.connection.clear(skillGraph);

    cv.copySkills();
  }

  public boolean isTimeStampValid(String inputString) {
    SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd");
    try {
      format.parse(inputString);
      return true;
    } catch (ParseException e) {
      return false;
    }
  }

  /**
   * Creates a new Session graph in the database.
   */
  public void createSession(String menuOption, String sectionOption) {
    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    /* Create unique timestamp and date for session */
    long timestamp = System.currentTimeMillis();
    Date date = new Date(timestamp);

    /* Create user IRI */
    IRI sessionIRI = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "DialogueSession-" + timestamp);

    /* Get user iri to associate it with the session */
    IRI userIRI = getUser();

    /* Add session to builder */
    builder
        .namedGraph("welcome:DialogueSession-" + timestamp)
        .subject(sessionIRI)
        .add(RDF.TYPE, WELCOME.SESSION)
        .add(WELCOME.ID, timestamp)
        .add(WELCOME.TIMESTAMP, date)
        .add(WELCOME.TRIGGEREDBY, menuOption)
        .subject(userIRI)
        .add(WELCOME.ISINVOLVED, sessionIRI);

    /* New code that stores scenario selection in the Profile graph */
    IRI Scenario = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "Scenario");

    IRI CVSection = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "CVSection");

    this.updateProfile(builder, Scenario, menuOption, 0, null, "null");
    this.updateProfile(builder, CVSection, sectionOption, 0, null, "null");
    /* End of new code */

    /* We're done building, create our Model */
    Model model = builder.build();

    /* Commit model */
    util.commitModel(model);

    /* Declare Session as active */
    setSessionStatus(sessionIRI);
  }

  /**
   * Creates the IRI of the dialogue user.
   */
  public Boolean checkUser(String logStatus) {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Clear DialogueUser graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "DialogueUserGraph");
    Utilities.connection.clear(graph);

    /* Create user IRI */
    IRI userObject = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "tcn_user");

    JSONObject statusObj;
    Boolean status = false;
    if (logStatus != null) {
      statusObj = util.parseJSONLD(logStatus);
      status = (Boolean) statusObj.get("handshake");
    }

    /* Add user to builder */
    builder
        .namedGraph("welcome:DialogueUserGraph")
        .subject(userObject)
        .add(RDF.TYPE, WELCOME.USER);

    if (logStatus != null) {
      builder.add(WELCOME.ISLOGGED, status);
    }


    /* We're done building, create our Model */
    Model model = builder.build();

    /* Commit model */
    util.commitModel(model);

    return status;
  }

  /**
   * Returns the IRI of the dialogue user.
   *
   * @return
   */
  public IRI getUser() {
    ValueFactory f = Utilities.repository.getValueFactory();

    // Init variables
    IRI userIRI = null;
    String queryString;

    // Find the IRI of the Dialogue User and the Dialogue Session
    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\n";
    queryString += "SELECT DISTINCT ?user where { \n";
    queryString += "    GRAPH welcome:DialogueUserGraph { \n";
    queryString += "        ?user rdf:type welcome:DialogueUser .\n";
    queryString += "    } \n";
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

  /**
   * Sends the handhsaking message to the agent.
   *
   * @return
   */
  public String sendHandShaking(Boolean status, long start) {

    if (status) {
      String handshaking = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n";
      handshaking += "@prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> .\n";
      handshaking += "welcome:TCN_Handshaking rdf:type welcome:Handshaking . \n";

      logger.info("(RESPONSE) Sending response to Agent-Core.");

      logger.info("Sending notification to agent: handshake");

      /* Send data to the Agent-Core */
      util.sendData(handshaking, "agent-core", "handshake", "text/turtle");

      long finish = System.currentTimeMillis();
      long timeElapsed = finish - start;

      logger.info("HAN: Time Elapsed: " + timeElapsed);

      return handshaking;
    } else {
      long finish = System.currentTimeMillis();
      long timeElapsed = finish - start;

      logger.info("HAN: Time Elapsed: " + timeElapsed);

      return "User logged out.";
    }
  }

  public void sendSpeakToAvatar() {
    String speakToAvatar = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n";
    speakToAvatar += "@prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> .\n";
    speakToAvatar += "welcome:TCN_SpeakToAvatar rdf:type welcome:speakToAvatar . \n";

    logger.info("(RESPONSE) Sending response to Agent-Core.");

    logger.info("Sending notification to agent: speakAvatar");

    /* Send data to the Agent-Core */
    util.sendData(speakToAvatar, "agent-core", "speakAvatar", "text/turtle");
  }

  public void setSessionStatus(Resource sessionIRI) {
    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    /* Clear DipSession graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "SessionInfo");
    Utilities.connection.clear(graph);

    /* Update DipSession graph */
    builder
        .namedGraph("welcome:SessionInfo")
        .subject(sessionIRI)
        .add(WELCOME.ISACTIVESESSION, "true");

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Updating active Session.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  public void updateRoleFlag() {
    Queries q = new Queries();

    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Create IRI for flag */
    String flag = "OfficerRoleFlag";
    IRI ontoType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, flag);

    q.updateProfile(builder, ontoType, true, 1);
  }

  public ModelBuilder
  updateProfile(ModelBuilder builder, IRI ontoType, String anchor, Integer sim, IRI activeDIP,
      String slotName) {
    /* Create IRI for the field */
    BNode iri = Utilities.f.createBNode();

    /* Get user's IRI */
    IRI userIRI = this.getUser();

    /* Split IRI from actual name */
    String predicate = util.splitIRI(ontoType);

    q.deleteProfileInfo_D(predicate, sim);

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
        q.setBSlotStatus_I(activeDIP, slotName, "Completed", anchor, "1.0", predicate);
      }
    }

    /* We're done building, create our Model */
    Model model = b.build();

    logger.info("(REPO) Updating TCN profile.");

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
    IRI userIRI = getUser();

    /* Split IRI from actual name */
    String predicate = util.splitIRI(ontoType);

    q.deleteProfileInfo_D(predicate, sim);

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
}
