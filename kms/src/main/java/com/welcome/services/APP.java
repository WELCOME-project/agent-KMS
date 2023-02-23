package com.welcome.services;

import com.welcome.auxiliary.Languages;
import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.SlotLists;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APP {

  Logger logger = LoggerFactory.getLogger(Utilities.class);

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries q = new Queries();
  AGENT agent = new AGENT();

  public void wrapperPostTCNProfile(String input) {
    /* Create RDF Model */
    String option = jsonToRdf(util.parseJSONLD(input));

    sendAckToAgent(option);
  }

  public void wrapperPostCHCPreferences(String body) {
    /* Create RDF Model */
    populateCHCPreferences(util.parseJSONLD(body));
  }

  public JSONObject wrapperGetCHCPreferences() {
    /* Create RDF Model */
    JSONObject obj = getCHCPreferences();

    return obj;
  }

  public void wrapperPostMinigame(String body, String minigame) {
    /* Create RDF Model */
    populateMinigame(body, util.parseJSONLD(body), minigame);
  }

  public void wrapperPostVRAppProgress(String body) {
    /* Create RDF Model */
    populateVRAppProgress(body);
  }

  public JSONObject wrapperGetTCNProfile(String section) {
    JSONObject obj = q.getTCNProfile_S(section);

    return obj;
  }

  public String wrapperGetMinigame() {
    String obj = q.getMinigameInfo_S();

    return obj;
  }

  public String wrapperVRData(String param) {
    String obj = q.getVRInfo_S(param);

    return obj;
  }

  private JSONObject getCHCPreferences() {
    JSONObject obj = new JSONObject();
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select ?os ?value where { \n";
    queryString += "    GRAPH welcome:CHCPreferences { \n";
    queryString += "		?s rdf:type ?o . \n";
    queryString += "    	?s welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    BIND(STRAFTER(STR(?o), \"#\") as ?os) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      /* we just iterate over all solutions in the result... */
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value os = solution.getBinding("os").getValue();
        Value value = solution.getBinding("value").getValue();

        if (value.stringValue().contentEquals("Dont mind")) {
          obj.put(os.stringValue(), "Don't mind");
        } else {
          obj.put(os.stringValue(), value.stringValue());
        }
      }
    }

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT DISTINCT ?lbound ?hbound WHERE { \n";
    queryString += "    GRAPH welcome:CHCPreferences { \n";
    queryString += "      ?s welcome:hasChcAgePreference/rdf:first/rdf:rest* ?age . \n";
    queryString += "      ?age welcome:higherBound ?hbound . \n";
    queryString += "      ?age welcome:lowerBound ?lbound . \n";
    queryString += "  } \n";
    queryString += "} \n";

    query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      JSONArray arr = new JSONArray();
      /* we just iterate over all solutions in the result... */
      if (result.hasNext()) {
        while (result.hasNext()) {
          BindingSet solution = result.next();

          Value lbound = solution.getBinding("lbound").getValue();
          Value hbound = solution.getBinding("hbound").getValue();

          JSONObject o = new JSONObject();
          o.put("lowerBound", lbound.stringValue());
          o.put("higherBound", hbound.stringValue());

          arr.add(o);
        }
      } else {
        JSONObject o = new JSONObject();
        o.put("lowerBound", "Don't mind");
        o.put("higherBound", "Don't mind");

        arr.add(o);
      }

      obj.put("ChcAgePreference", arr);
    }

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "SELECT DISTINCT (group_concat(distinct ?fam;separator=\",\") as ?fams) (group_concat(distinct ?loc;separator=\",\") as ?locs) WHERE { \n";
    queryString += "    GRAPH welcome:CHCPreferences {  \n";
    queryString += "    	?s welcome:hasChcFamilyPreference/rdf:rest*/rdf:first ?fam . \n";
    queryString += "    	?s welcome:hasChcLocationPreference/rdf:rest*/rdf:first ?loc . \n";
    queryString += "  } \n";
    queryString += "} GROUP BY ?s \n";

    query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {

      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value fams = solution.getBinding("fams").getValue();
        Value locs = solution.getBinding("locs").getValue();

        String[] arrOfStr1 = fams.stringValue().split(",");

        JSONArray arr1 = new JSONArray();
        for (String a : arrOfStr1) {
          arr1.add(a.replace("Dont", "Don't"));
        }

        obj.put("ChcFamilyPreference", arr1);

        String[] arrOfStr2 = locs.stringValue().split(",");

        JSONArray arr2 = new JSONArray();
        for (String a : arrOfStr2) {
          arr2.add(a);
        }

        obj.put("ChcLocationPreference", arr2);
      }

    }

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select ?startDate ?endDate ?min ?max where {  \n";
    queryString += "	?s1 welcome:startDate ?startDate . \n";
    queryString += "    ?s1 welcome:endDate ?endDate . \n";
    queryString += "    ?s2 welcome:min ?min . \n";
    queryString += "    ?s2 welcome:max ?max . \n";
    queryString += "} \n";

    query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {

      if (result.hasNext()) {
        BindingSet solution = result.next();

        Value startDate = solution.getBinding("startDate").getValue();
        Value endDate = solution.getBinding("endDate").getValue();
        Value min = solution.getBinding("min").getValue();
        Value max = solution.getBinding("max").getValue();

        JSONObject obj1 = new JSONObject();
        obj1.put("startDate", startDate.stringValue());
        obj1.put("endDate", endDate.stringValue());

        obj.put("ChcRentPeriodPreference", obj1);

        JSONObject obj2 = new JSONObject();
        obj2.put("min", min.stringValue());
        obj2.put("max", max.stringValue());

        obj.put("ChcShareWithPreference", obj2);

      }

    }

    return obj;
  }

  public void populateCHCPreferences(JSONObject chcObject) {
    /* Start working with other fields */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "CHCPreferences");
    Utilities.connection.clear(graph);

    /* Initialize RDF builder */
    ModelBuilder builder = util.getBuilder();

    /* String that will capture the dynamically populated delete and where clause in query */
    String deleteClause = "";
    String whereClause = "";
    String insertClause = "";

    /* String that will capture the final query */
    String queryString = "";

    /* temp counter */
    int counter = 0;

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CHCPreferences> { \n";

    for (Iterator iterator = chcObject.keySet().iterator(); iterator.hasNext(); ) {
      String key = (String) iterator.next();

      if (!SlotLists.chcPreferencesObjects.contains(key)) {
        String value = ((String) chcObject.get(key)).replace("'", "");
        if (!value.contentEquals("")) {

          boolean b = util.isNumeric(value);

          deleteClause += "   welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          deleteClause += "   ?" + key + " a welcome:" + key + " ; \n";
          deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
          deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

          insertClause += "    	welcome:tcn_user welcome:has" + key + " \n";
          insertClause += "        [   a welcome:" + key + " ; \n";

          if (b) {
            insertClause += "        	welcome:hasValue " + value + " ; \n";
          } else {
            insertClause += "        	welcome:hasValue \"" + value + "\" ; \n";
          }
          insertClause += "         welcome:lastUpdated " + timestamp + "  \n";
          insertClause += "        ]. \n";

          whereClause += "    OPTIONAL { \n";
          whereClause += "      welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          whereClause += "      ?" + key + " a welcome:" + key + " ; \n";
          whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
          whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
          whereClause += "    } \n";

          counter += 1;

          if (key.contentEquals("Birthday")) {
            LocalDate date1 = LocalDate.parse(value);
            LocalDate date2 = LocalDate.now();

            Period period = date1.until(date2);
            int yearsBetween = Integer.parseInt(String.valueOf(period.getYears()));

            deleteClause += "   welcome:tcn_user welcome:hasAge ?Age . \n";
            deleteClause += "   ?Age a welcome:Age ; \n";
            deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
            deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

            insertClause += "    	welcome:tcn_user welcome:hasAge \n";
            insertClause += "        [   a welcome:Age ; \n";
            insertClause += "        	welcome:hasValue " + yearsBetween + " ; \n";
            insertClause += "         welcome:lastUpdated " + timestamp + "  \n";
            insertClause += "        ]. \n";

            whereClause += "    OPTIONAL { \n";
            whereClause += "      welcome:tcn_user welcome:hasAge ?Age . \n";
            whereClause += "      ?Age a welcome:Age ; \n";
            whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
            whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
            whereClause += "    } \n";

            counter += 1;
          }

        } else {
          deleteClause += "   welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          deleteClause += "   ?" + key + " a welcome:" + key + " ; \n";
          deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
          deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

          whereClause += "    OPTIONAL { \n";
          whereClause += "      welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
          whereClause += "      ?" + key + " a welcome:" + key + " ; \n";
          whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
          whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
          whereClause += "    } \n";

          counter += 1;

          if (key.contentEquals("Birthday")) {
            deleteClause += "   welcome:tcn_user welcome:hasAge ?Age . \n";
            deleteClause += "   ?Age a welcome:Age ; \n";
            deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
            deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

            whereClause += "    OPTIONAL { \n";
            whereClause += "      welcome:tcn_user welcome:hasAge ?Age . \n";
            whereClause += "      ?Age a welcome:Age ; \n";
            whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
            whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
            whereClause += "    } \n";

            counter += 1;
          }
        }
      }
    }

    queryString += deleteClause;
    queryString += "  } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CHCPreferences> { \n";
    queryString += insertClause;
    queryString += "} \n";
    queryString += "} WHERE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CHCPreferences> { \n";
    queryString += whereClause;
    queryString += "  } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    IRI user = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "tcn_user");

    JSONArray speaksLanguage = (JSONArray) chcObject.get("speaksLanguage");

//    IRI property1 = Utilities.connection
//        .getValueFactory()
//        .createIRI(WELCOME.NAMESPACE + "speaksLanguage");
//
//    List<String> languages = new ArrayList<>();
//
//    Iterator<String> it = speaksLanguage.iterator();
//    while (it.hasNext()) {
//      languages.add((it.next()).replace("'", ""));
//    }
//
//    BNode head1 = Utilities.f.createBNode();
//    Model model1 = RDFCollections.asRDF(languages, head1, new LinkedHashModel());
//
//    model1.add(user, property1, head1);
//
//    util.commitModel(model1, graph);

    JSONArray familyPreference = (JSONArray) chcObject.get("ChcFamilyPreference");

    IRI property7 = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "hasChcFamilyPreference");

    List<String> familyPref = new ArrayList<>();

    Iterator<String> it = familyPreference.iterator();
    while (it.hasNext()) {
      familyPref.add((it.next()).replace("'", ""));
    }

    BNode head7 = Utilities.f.createBNode();
    Model model7 = RDFCollections.asRDF(familyPref, head7, new LinkedHashModel());

    model7.add(user, property7, head7);

    util.commitModel(model7, graph);

    JSONArray ChcLocationPreference = (JSONArray) chcObject.get("ChcLocationPreference");

    IRI property2 = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "hasChcLocationPreference");

    List<String> locations = new ArrayList<>();

    it = ChcLocationPreference.iterator();
    while (it.hasNext()) {
      locations.add((it.next()).replace("'", ""));
    }

    BNode head2 = Utilities.f.createBNode();
    Model model2 = RDFCollections.asRDF(locations, head2, new LinkedHashModel());

    model2.add(user, property2, head2);

    util.commitModel(model2, graph);

    JSONObject ChcRentPeriodPreference = (JSONObject) chcObject.get("ChcRentPeriodPreference");

    IRI property3 = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "hasChcRentPeriodPreference");

    IRI startDate = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "startDate");

    IRI endDate = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "endDate");

    BNode head3 = Utilities.f.createBNode();

    /* Update DipInfo graph */
    builder
        .namedGraph(graph)
        .subject("welcome:tcn_user")
        .add(property3, head3)
        .subject(head3);

    String temp = (String) ChcRentPeriodPreference.get("startDate");
    if (temp.toLowerCase().contains("mind")) {
      builder
          .add("welcome:hasValue", temp.replace("'", ""));
    } else {
      builder
          .add(startDate, LocalDate.parse((String) ChcRentPeriodPreference.get("startDate")))
          .add(endDate, LocalDate.parse((String) ChcRentPeriodPreference.get("endDate")));
    }

    JSONObject ChcShareWithPreference = (JSONObject) chcObject.get("ChcShareWithPreference");

    IRI property4 = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "hasChcShareWithPreference");

    IRI min = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "min");

    IRI max = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "max");

    BNode head4 = Utilities.f.createBNode();

    /* Update DipInfo graph */
    builder
        .namedGraph(graph)
        .subject("welcome:tcn_user")
        .add(property4, head4)
        .subject(head4);

    temp = (String) ChcShareWithPreference.get("min");
    if (temp.toLowerCase().contains("mind")) {
      builder
          .add("welcome:hasValue", temp.replace("'", ""));
    } else {
      builder
          .add(min, Integer.parseInt((String) ChcShareWithPreference.get("min")))
          .add(max, Integer.parseInt((String) ChcShareWithPreference.get("max")));
    }

    JSONArray ChcAgePreference = (JSONArray) chcObject.get("ChcAgePreference");

    IRI property5 = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "hasChcAgePreference");

    IRI lbound = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "lowerBound");

    IRI hbound = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "higherBound");

    // Head of the list.
    BNode head5 = Utilities.f.createBNode();

    Boolean flag = true;
    int agePrefCount = 0;
    BNode prevNode = Utilities.f.createBNode();
    BNode curNode = Utilities.f.createBNode();

    for (Object p : ChcAgePreference) {
      JSONObject agePref = (JSONObject) p;

      BNode node = Utilities.f.createBNode();

      curNode = node;

      agePrefCount += 1;

      temp = (String) agePref.get("lowerBound");
      if (temp.toLowerCase().contains("mind")) {
        builder
            .namedGraph(graph)
            .subject("welcome:tcn_user")
            .add(property5, node)
            .subject(node)
            .add("welcome:hasValue", temp.replace("'", ""));
        flag = false;
        break;
      } else {
        if (agePrefCount == 1) {
          builder
              .namedGraph(graph)
              .subject("welcome:tcn_user")
              .add(property5, head5)
              .subject(head5)
              .add(RDF.TYPE, RDF.LIST)
              .add(RDF.FIRST, node);
        } else {
          builder
              .subject(prevNode)
              .add(RDF.REST, node);
        }
        builder
            .subject(node)
            .add(lbound, Integer.parseInt((String) agePref.get("lowerBound")))
            .add(hbound, Integer.parseInt((String) agePref.get("higherBound")));
        prevNode = node;
      }
    }

    if (flag) {
      builder
          .subject(curNode)
          .add(RDF.REST, RDF.NIL);
    }

//    for (Object p : ChcAgePreference) {
//      JSONObject agePref = (JSONObject) p;
//
//      BNode node = Utilities.f.createBNode();
//
//      builder
//          .namedGraph(graph)
//          .subject("welcome:tcn_user")
//          .add(property5, node)
//          .subject(node)
//          .add(lbound, agePref.get("lowerBound"))
//          .add(hbound, agePref.get("higherBound"));
//    }

    /* We're done building, create our Model */
    Model model = builder.build();

    /* Commit model to repository */
    util.commitModel(model);
  }

  public String
  jsonToRdf(JSONObject appObject) {
    /* Create user IRI */
    IRI graph = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "ProfileInfo");

    /* String that will capture the dynamically populated delete and where clause in query */
    String deleteClause = "";
    String whereClause = "";
    String insertClause = "";

    /* String that will capture the final query */
    String queryString = "";

    /* temp counter */
    int counter = 0;

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";

    for (Iterator iterator = appObject.keySet().iterator(); iterator.hasNext(); ) {
      String key = (String) iterator.next();

      if (key.contentEquals("profile_updated")) {
        return "updated";

      } else if (key.contentEquals("displayed")) {
        q.updatedActiveSlotStatus_I("informFormDisclaimer");
        return "displayed";

      } else if (key.contentEquals("printed")) {
        return "printed";

      } else if (key.contentEquals("faqSatisfaction")) {
        String value = (String) appObject.get(key);
        this.updateFAQSlots(value);

        /* Export active DIP */
        agent.exportActiveDIP();

        return "faqSatisfaction";

      } else if (key.contentEquals("data")) {
        JSONObject data = (JSONObject) appObject.get("data");

        /* Retrieve the active dip */
        IRI activeDIP = q.activeDIP_S();

        for (Iterator it = data.keySet().iterator(); it.hasNext(); ) {
          String k = (String) it.next();
          JSONArray array = (JSONArray) data.get(k);

          if (k.contentEquals("skills")) {
            this.updateSkills(array);
          } else if (k.contentEquals("personal")) {
            this.updateCVOtherPersonalSection(array);
            q.resetSlotsStatus(activeDIP, SlotLists.personalSlotsFull, "Completed");
          } else if (k.contentEquals("education")) {
            this.updateCVSection("Education", array);
            q.resetSlotsStatus(activeDIP, SlotLists.educationSlotsFull, "Completed");
          } else if (k.contentEquals("course")) {
            this.updateCVSection("Course", array);
            q.resetSlotsStatus(activeDIP, SlotLists.courseSlotsFull, "Completed");
          } else if (k.contentEquals("language")) {
            this.updateCVSection("Language", array);
            q.resetSlotsStatus(activeDIP, SlotLists.languageSlotsFull, "Completed");
          } else if (k.contentEquals("employment")) {
            this.updateEmploymentSection("Employment", array);
            q.resetSlotsStatus(activeDIP, SlotLists.employmentSlotsFull, "Completed");
          } else if (k.contentEquals("other")) {
            this.updateCVOtherPersonalSection(array);
            q.resetSlotsStatus(activeDIP, SlotLists.otherSlotsFull, "Completed");
          }
        }

        /* Update status of remaining slots */
        this.updateSlot();

        /* Update CV Status */
        this.checkCVStatus();

        /* Export active DIP */
        agent.exportActiveDIP();

        return "confirmed";

      } else if (key.contentEquals("add")) {

        String value = (String) appObject.get(key);
        this.addNewCVObject(value);

        /* Export active DIP */
        agent.exportActiveDIP();

        return "add";

      } else {
        //TODO Confirm with CENTRIC about OtherLanguageCode
        if (!key.contentEquals("Age") && !key.contentEquals("OtherLanguageCode")) {
          String value = (String) appObject.get(key);

          if (!value.contentEquals("")) {
            deleteClause += "   welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
            deleteClause += "   ?" + key + " a welcome:" + key + " ; \n";
            deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
            deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

            insertClause += "    	welcome:tcn_user welcome:has" + key + " \n";
            insertClause += "        [   a welcome:" + key + " ; \n";
            insertClause += "        	welcome:hasValue \"" + value + "\" ; \n";
            insertClause += "         welcome:lastUpdated " + timestamp + "  \n";
            insertClause += "        ]. \n";

            whereClause += "    OPTIONAL { \n";
            whereClause += "      welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
            whereClause += "      ?" + key + " a welcome:" + key + " ; \n";
            whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
            whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
            whereClause += "    } \n";

            counter += 1;

            if (key.contentEquals("Birthday")) {
              LocalDate date1 = LocalDate.parse(value);
              LocalDate date2 = LocalDate.now();

              Period period = date1.until(date2);
              int yearsBetween = Integer.parseInt(String.valueOf(period.getYears()));

              deleteClause += "   welcome:tcn_user welcome:hasAge ?Age . \n";
              deleteClause += "   ?Age a welcome:Age ; \n";
              deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
              deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

              insertClause += "    	welcome:tcn_user welcome:hasAge \n";
              insertClause += "        [   a welcome:Age ; \n";
              insertClause += "        	welcome:hasValue " + yearsBetween + " ; \n";
              insertClause += "         welcome:lastUpdated " + timestamp + "  \n";
              insertClause += "        ]. \n";

              whereClause += "    OPTIONAL { \n";
              whereClause += "      welcome:tcn_user welcome:hasAge ?Age . \n";
              whereClause += "      ?Age a welcome:Age ; \n";
              whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
              whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
              whereClause += "    } \n";

              counter += 1;
            }

          } else {
            deleteClause += "   welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
            deleteClause += "   ?" + key + " a welcome:" + key + " ; \n";
            deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
            deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

            whereClause += "    OPTIONAL { \n";
            whereClause += "      welcome:tcn_user welcome:has" + key + " ?" + key + " . \n";
            whereClause += "      ?" + key + " a welcome:" + key + " ; \n";
            whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
            whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
            whereClause += "    } \n";

            counter += 1;

            if (key.contentEquals("Birthday")) {
              deleteClause += "   welcome:tcn_user welcome:hasAge ?Age . \n";
              deleteClause += "   ?Age a welcome:Age ; \n";
              deleteClause += "     welcome:hasValue ?value" + counter + " ; \n";
              deleteClause += "     welcome:lastUpdated ?timestamp" + counter + " . \n";

              whereClause += "    OPTIONAL { \n";
              whereClause += "      welcome:tcn_user welcome:hasAge ?Age . \n";
              whereClause += "      ?Age a welcome:Age ; \n";
              whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
              whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
              whereClause += "    } \n";

              counter += 1;
            }
          }
        }
      }
    }

    queryString += deleteClause;
    queryString += "  } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += insertClause;
    queryString += "} \n";
    queryString += "} WHERE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += whereClause;
    queryString += "  } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);

    /* Set default language to english */
    Locale locale = new Locale("en_US");
    Locale.setDefault(locale);

    if (appObject.containsKey("LanguageCode")) {
      String f6_iso = (String) appObject.get("LanguageCode");

      String value;
      if (!f6_iso.contentEquals("")) {
        if (f6_iso.length() == 3) {
          value = Languages.getNameFromCode(f6_iso);
        } else {
          /* Find the full language name */
          Locale l2 = new Locale(f6_iso, "");

          String language = l2.getDisplayLanguage();
          value = (language != null) ? language : f6_iso;
        }

        String key = "Language";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    String natLang = null;
    if (appObject.containsKey("NativeLanguageCode")) {
      String f6_iso = (String) appObject.get("NativeLanguageCode");

      String value;
      if (!f6_iso.contentEquals("")) {
        if (f6_iso.length() == 3) {
          value = Languages.getNameFromCode(f6_iso);

          // assign for later usage
          natLang = value;
        } else {
          /* Find the full language name */
          Locale l2 = new Locale(f6_iso, "");

          String language = l2.getDisplayLanguage();
          value = (language != null) ? language : f6_iso;

          // assign for later usage
          natLang = value;
        }

        String key = "NativeLanguageName";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    if (appObject.containsKey("OtherLanguageCode")) {

      if (appObject.get("OtherLanguageCode") instanceof JSONArray) {
        IRI user = Utilities.connection
            .getValueFactory()
            .createIRI(WELCOME.NAMESPACE + "tcn_user");

        // This will remove the whole list
        q.deleteOtherLanguageCode("hasOtherLanguageCode");
        q.deleteOtherLanguageCode("speaksLanguage");

        JSONArray languageCodeList = (JSONArray) appObject.get("OtherLanguageCode");

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
        String f6_iso = (String) appObject.get("OtherLanguageCode");
        String value;
        if (!f6_iso.contentEquals("")) {
          if (f6_iso.length() == 3) {
            value = Languages.getNameFromCode(f6_iso);
          } else {
            /* Find the full language name */
            Locale l2 = new Locale(f6_iso, "");

            String language = l2.getDisplayLanguage();
            value = (language != null) ? language : f6_iso;
          }

          String key = "OtherLanguage";
          boolean e = q.checkIfPopulated(key);

          if (!value.contentEquals("")) {
            if (e) {
              q.populateTCNProfile(key, value);
            } else {
              q.insertTCNData(key, value, graph);
            }
          } else {
            q.removeTCNData(key, graph);
          }
        }
      }
    }

    if (appObject.containsKey("CountryCode")) {
      String f8_iso = (String) appObject.get("CountryCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "Country";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    if (appObject.containsKey("CountryOfOriginCode")) {
      String f8_iso = (String) appObject.get("CountryOfOriginCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "CountryOfOrigin";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    if (appObject.containsKey("IDCountryCode")) {
      String f8_iso = (String) appObject.get("IDCountryCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "IDCountry";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    if (appObject.containsKey("CountryOfBirthCode")) {
      String f8_iso = (String) appObject.get("CountryOfBirthCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "CountryOfBirth";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    if (appObject.containsKey("NationalityCode")) {
      String f8_iso = (String) appObject.get("NationalityCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "Nationality";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    if (appObject.containsKey("PreviousResidenceOtherCode")) {
      String f8_iso = (String) appObject.get("PreviousResidenceOtherCode");

      if (!f8_iso.contentEquals("")) {
        /* Find the full country name */
        Locale l0 = new Locale("", f8_iso);

        String host_country = l0.getDisplayCountry();
        String value = (host_country != null) ? host_country : f8_iso;

        String key = "PreviousResidenceOther";
        boolean e = q.checkIfPopulated(key);

        if (!value.contentEquals("")) {
          if (e) {
            q.populateTCNProfile(key, value);
          } else {
            q.insertTCNData(key, value, graph);
          }
        } else {
          q.removeTCNData(key, graph);
        }
      }
    }

    if (appObject.containsKey(("RegistrationStatus"))) {
      String status = (String) appObject.get("RegistrationStatus");
      if (status.contentEquals("true")) {
        q.insertRegStatus();
      } else {
        q.deleteRegStatus();
      }
    }

    IRI cityType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "City");
    String city = q.getProfileValue(cityType, 0);

    if (!city.contentEquals("")) {
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

        q.deleteCoords();

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

            q.deleteCoords();

            updateProfileCoords(builder, cityType, modified, lng, lat, 0);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    return "default";
  }

  public void clearSection(String section) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "DELETE {  \n";
    queryString += "    GRAPH welcome:CVInfo { \n";
    queryString += "		  ?s welcome:hasElem ?elem . \n";
    queryString += "    	?elem rdf:type welcome:" + section + "Elem . \n";
    queryString += "    	?elem ?hasProperty ?property . \n";
    queryString += "    	?property rdf:type ?ptype . \n";
    queryString += "    	?property welcome:hasValue ?value . \n";
    queryString += "    	?property welcome:lastUpdated ?updated . \n";
    queryString += "    } \n";
    queryString += "} WHERE { \n";
    queryString += "    GRAPH welcome:CVInfo { \n";
    queryString += "		  ?s welcome:hasElem ?elem . \n";
    queryString += "    	?elem rdf:type welcome:" + section + "Elem . \n";
    queryString += "    	?elem ?hasProperty ?property . \n";
    queryString += "    	?property rdf:type ?ptype . \n";
    queryString += "    	?property welcome:hasValue ?value . \n";
    queryString += "    	?property welcome:lastUpdated ?updated . \n";
    queryString += "    } \n";
    queryString += "    FILTER(?ptype != welcome:CurrentlyEmployed) \n";
    queryString += "}  \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateEmploymentSection(String section, JSONArray array) {
    this.clearSection(section);

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    for (Object o : array) {
      JSONObject obj = (JSONObject) o;

      String id = (String) obj.get("id");
      String StartingDate = (String) obj.get("StartingDate");
      String Occupation = (String) obj.get("Occupation");
      String EmployerAddress = (String) obj.get("EmployerAddress");
      String EmployerName = (String) obj.get("EmployerName");
      String EndDate = (String) obj.get("EndDate");
      String MainActivities = (String) obj.get("MainActivities");

      /* Create user IRI */
      IRI objIRI = Utilities.f
          .createIRI(WELCOME.NAMESPACE, id);

      String prev = "";
      if (!EndDate.contentEquals("Current")) {
        prev = "Previous";
      }
      /* Create property IRI */
      IRI iri1 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "has" + prev + "StartingDate");
      IRI iri2 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "has" + prev + "Occupation");
      IRI iri3 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "has" + prev + "EmployerAddress");
      IRI iri4 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "has" + prev + "EmployerName");
      IRI iri5 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "has" + prev + "EndDate");
      IRI iri6 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, "has" + prev + "MainActivities");
      /* Create class IRI */
      IRI ciri1 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, prev + "StartingDate");
      IRI ciri2 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, prev + "Occupation");
      IRI ciri3 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, prev + "EmployerAddress");
      IRI ciri4 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, prev + "EmployerName");
      IRI ciri5 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, prev + "EndDate");
      IRI ciri6 = Utilities.f
          .createIRI(WELCOME.NAMESPACE, prev + "MainActivities");

      /* Create IRI for the field */
      BNode bnode1 = Utilities.f.createBNode();
      BNode bnode2 = Utilities.f.createBNode();
      BNode bnode3 = Utilities.f.createBNode();
      BNode bnode4 = Utilities.f.createBNode();
      BNode bnode5 = Utilities.f.createBNode();
      BNode bnode6 = Utilities.f.createBNode();

      /* Initialize RDF builder */
      ModelBuilder builder = new ModelBuilder()
          .setNamespace("welcome", WELCOME.NAMESPACE);

      builder.namedGraph("welcome:CVInfo")
          .subject(WELCOME.CV)
          .add(WELCOME.HASELEM, objIRI)

          .subject(objIRI)
          .add(RDF.TYPE, "welcome:" + section + "Elem")
          .add(iri1, bnode1)
          .add(iri2, bnode2)
          .add(iri3, bnode3)
          .add(iri4, bnode4)
          .add(iri5, bnode5)
          .add(iri6, bnode6)

          .subject(bnode1)
          .add(RDF.TYPE, ciri1)
          .add(WELCOME.HASVALUE, StartingDate)
          .add(WELCOME.LASUPDATED, timestamp)

          .subject(bnode2)
          .add(RDF.TYPE, ciri2)
          .add(WELCOME.HASVALUE, Occupation)
          .add(WELCOME.LASUPDATED, timestamp)

          .subject(bnode3)
          .add(RDF.TYPE, ciri3)
          .add(WELCOME.HASVALUE, EmployerAddress)
          .add(WELCOME.LASUPDATED, timestamp)

          .subject(bnode4)
          .add(RDF.TYPE, ciri4)
          .add(WELCOME.HASVALUE, EmployerName)
          .add(WELCOME.LASUPDATED, timestamp)

          .subject(bnode5)
          .add(RDF.TYPE, ciri5)
          .add(WELCOME.HASVALUE, EndDate)
          .add(WELCOME.LASUPDATED, timestamp)

          .subject(bnode6)
          .add(RDF.TYPE, ciri6)
          .add(WELCOME.HASVALUE, MainActivities)
          .add(WELCOME.LASUPDATED, timestamp);

      /* We're done building, create our Model */
      Model model = builder.build();

      /* Commit model to repository */
      util.commitModel(model);
    }
  }

  public void updateCVOtherPersonalSection(JSONArray array) {
    /* String that will capture the dynamically populated delete and where clause in query */
    String deleteClause = "";
    String whereClause = "";
    String insertClause = "";

    /* String that will capture the final query */
    String queryString = "";

    /* temp counter */
    int counter = 0;

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";

    for (Object o : array) {
      JSONObject obj = (JSONObject) o;

      String id = (String) obj.get("id");

      for (Iterator iterator = obj.keySet().iterator(); iterator.hasNext(); ) {
        String key = (String) iterator.next();
        String value = (String) obj.get(key);

        if (!key.contentEquals("id")) {

          deleteClause += "   welcome:tcn_user welcome:has" + key + " ?key" + counter + " . \n";
          deleteClause +=
              "   ?key" + counter + " welcome:lastUpdated ?timestamp" + counter + " . \n";
          deleteClause += "   ?key" + counter + " welcome:hasValue ?value" + counter + " . \n";
          deleteClause += "   ?key" + counter + " rdf:type welcome:" + key + " . \n";

          insertClause += "   welcome:tcn_user welcome:has" + key + " \n";
          insertClause += "        [   a welcome:" + key + " ; \n";
          insertClause += "        	welcome:hasValue \"" + value + "\" ; \n";
          insertClause += "         welcome:lastUpdated " + timestamp + "  \n";
          insertClause += "        ]. \n";

          whereClause += "    OPTIONAL { \n";
          whereClause += "      welcome:tcn_user welcome:has" + key + " ?key" + counter + " . \n";
          whereClause += "      ?key" + counter + " a welcome:" + key + " ; \n";
          whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
          whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
          whereClause += "    } \n";

          counter += 1;
        }
      }
    }

    queryString += deleteClause;
    queryString += "  } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += insertClause;
    queryString += "} \n";
    queryString += "} WHERE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += whereClause;
    queryString += "  } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateCVSection(String section, JSONArray array) {
    this.clearSection(section);

    /* String that will capture the dynamically populated delete and where clause in query */
    String deleteClause = "";
    String whereClause = "";
    String insertClause = "";

    /* String that will capture the final query */
    String queryString = "";

    /* temp counter */
    int counter = 0;

    /* Set unix timestamp */
    long timestamp = System.currentTimeMillis() / 1000;

    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "DELETE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";

    for (Object o : array) {
      JSONObject obj = (JSONObject) o;

      String id = (String) obj.get("id");

      for (Iterator iterator = obj.keySet().iterator(); iterator.hasNext(); ) {
        String key = (String) iterator.next();
        String value = (String) obj.get(key);

        if (!key.contentEquals("id")) {

          deleteClause += "   welcome:CV welcome:hasElem welcome:" + id + " . \n";
          deleteClause += "   welcome:" + id + " a welcome:" + section + "Elem . \n";
          deleteClause += "   welcome:" + id + " welcome:has" + key + " ?key" + counter + " . \n";
          deleteClause +=
              "   ?key" + counter + " welcome:lastUpdated ?timestamp" + counter + " . \n";
          deleteClause += "   ?key" + counter + " welcome:hasValue ?value" + counter + " . \n";
          deleteClause += "   ?key" + counter + " rdf:type welcome:" + key + " . \n";

          insertClause += "   welcome:CV welcome:hasElem welcome:" + id + " . \n";
          insertClause += "   welcome:" + id + " a welcome:" + section + "Elem . \n";
          insertClause += "   welcome:" + id + " welcome:has" + key + " \n";
          insertClause += "        [   a welcome:" + key + " ; \n";
          insertClause += "        	welcome:hasValue \"" + value + "\" ; \n";
          insertClause += "         welcome:lastUpdated " + timestamp + "  \n";
          insertClause += "        ]. \n";

          whereClause += "    OPTIONAL { \n";
          whereClause += "      welcome:CV welcome:hasElem welcome:" + id + " . \n";
          whereClause += "      welcome:" + id + " a welcome:" + section + "Elem . \n";
          whereClause += "      welcome:" + id + " welcome:has" + key + " ?key" + counter + " . \n";
          whereClause += "      ?key" + counter + " a welcome:" + key + " ; \n";
          whereClause += "        welcome:hasValue ?value" + counter + " ; \n";
          whereClause += "        welcome:lastUpdated ?timestamp" + counter + " . \n";
          whereClause += "    } \n";

          counter += 1;
        }
      }
    }

    queryString += deleteClause;
    queryString += "  } \n";
    queryString += "} \n";
    queryString += "INSERT { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += insertClause;
    queryString += "} \n";
    queryString += "} WHERE { \n";
    queryString += "  GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#CVInfo> { \n";
    queryString += whereClause;
    queryString += "  } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateSkills(JSONArray skills) {
    /* Create user IRI */
    IRI graph = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "SkillsGraph");
    Utilities.connection.clear(graph);

    /* String that will capture the final query */
    String queryString = "";

    queryString = "PREFIX rdf: <" + RDF.NAMESPACE + "> \n";
    queryString += "PREFIX welcome: <" + WELCOME.NAMESPACE + "> \n";
    queryString += "INSERT DATA { \n";
    queryString += "    GRAPH welcome:SkillsGraph { \n";

    for (Object o : skills) {
      JSONObject skill = (JSONObject) o;

      String skillValue = (String) skill.get("skill");
      Boolean selectedValue = Boolean.parseBoolean(skill.get("selected").toString());

      queryString +=
          "    	welcome:" + skillValue.replace(" ", "_") + " rdf:type welcome:Skill . \n";
      queryString +=
          "    	welcome:" + skillValue.replace(" ", "_") + " welcome:selected " + selectedValue
              + " . \n";
    }

    queryString += "    } \n";
    queryString += "} \n";

    /* Execute the query */
    util.executeQuery(queryString);
  }

  public void updateSlot() {
    /* Retrieve the active slot */
    List<IRI> activeSlot = q.activeSlot_S();

    /* Retrieve the active dip */
    IRI activeDIP = q.activeDIP_S();

    for (IRI temp : activeSlot) {
      /* Split IRI from actual name */
      String slotName = util.splitIRI(temp);

      if (slotName.contains("inform")) {
        q.setInfoSlotStatus_I(activeDIP, slotName, "Completed", "1.0");
      } else {
        q.setBSlotStatus_I(activeDIP, slotName, "Completed", "Yes", "1.0", null);
      }
      q.updatedActiveSlotStatus_I(slotName);
    }
  }

  public void updateFAQSlots(String value) {
    /* Retrieve the active dip */
    IRI activeDIP = q.activeDIP_S();

    if (value.contentEquals("true")) {
      q.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed", "Yes",
          "1.0", null);
      q.setInfoSlotStatus_I(activeDIP, "informInabilityAssist", "Completed", "1.0");
      q.setInfoSlotStatus_I(activeDIP, "informGenericContact", "Completed", "1.0");
    } else {
      q.setBSlotStatus_I(activeDIP, "obtainFAQSatisfaction", "Completed", "No",
          "1.0", null);
    }
  }

  public void sendAckToAgent(String option) {

    String sendAckToAgent = "@prefix welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> .\n";

    boolean dialogueStatus = q.checkDialogueStatus();

    IRI scenarioName = q.activeDipName_S();
    String name = scenarioName.stringValue();

    if (dialogueStatus && name.contains("FillFormCourse")) {
      logger.info("(RESPONSE) Form updated - Sending response to Agent-Core.");
      logger.info("Sending notification to agent: closeScenario");

      sendAckToAgent += "welcome:TCN_Profile welcome:updated \"true\" .  \n";

      /* Send data to the Agent-Core */
      util.sendData(sendAckToAgent, "agent-core", "closeScenario", "text/turtle");
    } else if (dialogueStatus) {
      switch (option) {
        case "updated":
        case "default":
          logger.info("(RESPONSE) Form updated - No response to Agent-Core.");
          break;
        case "displayed":
          sendAckToAgent += "welcome:preFilledForm welcome:displayed \"true\" .  \n";
          logger.info("(RESPONSE) Form displayed - Sending response to Agent-Core.");

          logger.info("Sending notification to agent: KMSNotificationS2a");

          /* Send data to the Agent-Core */
          util.sendData(sendAckToAgent, "agent-core", "KMSNotificationS2a", "text/turtle");
          break;
        case "printed":
          sendAckToAgent += "welcome:preFilledForm welcome:printed \"true\" .  \n";
          logger.info("(RESPONSE) Form printed - Sending response to Agent-Core.");

          logger.info("Sending notification to agent: KMSNotificationS2a");

          /* Send data to the Agent-Core */
          util.sendData(sendAckToAgent, "agent-core", "KMSNotificationS2a", "text/turtle");
          break;
      }
    } else {
      logger.info("(RESPONSE) Form updated - No response to Agent-Core.");
    }
  }

  public void wrapperPostAvatarConfig(String input) {
    /* Create RDF Model */
    populateAvatarConfig(util.parseJSONLD(input));
  }

  public JSONObject wrapperGetAvatarConfig() {
    JSONObject obj = q.getAvatarConfig_S();

    return obj;
  }

  public void populateAvatarConfig(JSONObject appObject) {
    /* Clear AvatarConfig graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "AvatarConfig");
    Utilities.connection.clear(graph);

    for (Iterator iterator = appObject.keySet().iterator(); iterator.hasNext(); ) {
      String key = (String) iterator.next();
      Object con = appObject.get(key);

      if (con instanceof Double) {
        double value = (Double) appObject.get(key);
        q.insertAvatarConfigDouble(key, value);
      } else if (con instanceof Integer) {
        int value = (Integer) appObject.get(key);
        q.insertAvatarConfigInteger(key, value);
      } else if (con instanceof String) {
        String value = (String) appObject.get(key);
        q.insertAvatarConfigString(key, value);
      } else if (con instanceof Long) {
        long value = (Long) appObject.get(key);
        q.insertAvatarConfigLong(key, value);
      }
    }
  }

  public void populateMinigame(String body, JSONObject input, String minigame) {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Set unique ID */
    UUID uuid = UUID.randomUUID();

    if (minigame.equals("Minigame")) {
      String minigameID;
      String score;
      String time;

      if (input.containsKey("minigameID")) {
        minigameID = input.get("minigameID").toString();
      } else {
        minigameID = "none";
      }

      if (input.containsKey("score")) {
        score = input.get("score").toString();
      } else {
        score = "0";
      }

      if (input.containsKey("time")) {
        time = input.get("time").toString();
      } else {
        time = "0";
      }

      /* Create Minigame graph */
      IRI graph = Utilities.f.createIRI(WELCOME.NAMESPACE + "MinigameInfo");

      /* Create Minigame Object */
      IRI mnObj = Utilities.f.createIRI(WELCOME.NAMESPACE, "Minigame-" + uuid);

      builder
          .namedGraph(graph)
          .subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasMinigameID"),
              minigameID)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasScore"),
              score)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasTime"),
              time)
          .add(RDF.TYPE, SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "Minigame"));
    } else if (minigame.equals("ApplicationForm")) {
      String scenarioID;
      String stage;
      String stageScore;
      String stageTime;
      String attempts;

      if (input.containsKey("scenarioID")) {
        scenarioID = input.get("scenarioID").toString();
      } else {
        scenarioID = "none";
      }

      if (input.containsKey("stage")) {
        stage = input.get("stage").toString();
      } else {
        stage = "0";
      }

      if (input.containsKey("stagescore")) {
        stageScore = input.get("stagescore").toString();
      } else {
        stageScore = "0";
      }

      if (input.containsKey("stagetime")) {
        stageTime = input.get("stagetime").toString();
      } else {
        stageTime = "0";
      }

      if (input.containsKey("attempts")) {
        attempts = input.get("attempts").toString();
      } else {
        attempts = "0";
      }

      /* Create ApplicationFormInfo graph */
      IRI graph = Utilities.f.createIRI(WELCOME.NAMESPACE + "ApplicationFormInfo");

      /* Create ApplicationForm Object */
      IRI mnObj = Utilities.f.createIRI(WELCOME.NAMESPACE, "ApplicationForm-" + uuid);

      builder
          .namedGraph(graph)
          .subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasScenarioID"),
              scenarioID)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasStage"),
              stage)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasStageScore"),
              stageScore)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasStageTime"),
              stageTime)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasAttempts"),
              attempts)
          .add(RDF.TYPE,
              SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "ApplicationForm"));

    } else if (minigame.equals("JobInterviewSimulation")) {
      /* Create JobInterviewSimulationInfo graph */
      IRI graph = Utilities.f.createIRI(WELCOME.NAMESPACE + "JobInterviewSimulationInfo");

      /* Create JobInterviewSimulation Object */
      IRI mnObj = Utilities.f.createIRI(WELCOME.NAMESPACE, "JobInterviewSimulation-" + uuid);

      builder
          .namedGraph(graph)
          .subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasScenarioID"),
              input.get("scenarioID").toString())
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasStage"),
              input.get("stage").toString())
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasHelp"),
              input.get("help").toString())
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasAnswers"),
              input.get("answers").toString())
          .add(RDF.TYPE,
              SimpleValueFactory.getInstance()
                  .createIRI(WELCOME.NAMESPACE, "JobInterviewSimulation"));

    } else if (minigame.equals("Vocab")) {
      /* Create VocabInfo graph */
      IRI graph = Utilities.f.createIRI(WELCOME.NAMESPACE + "VocabInfo");

      /* Create Vocab Object */
      IRI mnObj = Utilities.f.createIRI(WELCOME.NAMESPACE, "Vocab-" + uuid);

      builder
          .namedGraph(graph)
          .subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasScore"),
              input.get("score").toString())
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasAttempts"),
              input.get("attempts").toString())
          .add(RDF.TYPE, SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "Vocab"));

    } else if (minigame.equals("VRSaveProgress")) {
      /* Create VocabInfo graph */
      IRI graph = Utilities.f.createIRI(WELCOME.NAMESPACE + "VRSaveProgressInfo");
      Utilities.connection.clear(graph);

      /* Create VRSaveProgress Object */
      IRI mnObj = Utilities.f.createIRI(WELCOME.NAMESPACE, "VRSaveProgress-" + uuid);

      builder
          .namedGraph(graph)
          .subject(mnObj)
          .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasSaveData"),
              body)
          .add(RDF.TYPE,
              SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "VRSaveProgress"));
    }

    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Adding Minigame Info to the repository.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  public void populateVRAppProgress(String body) {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Set unique ID */
    UUID uuid = UUID.randomUUID();

    /* Create VocabInfo graph */
    IRI graph = Utilities.f.createIRI(WELCOME.NAMESPACE + "VRAppProgressInfo");
    Utilities.connection.clear(graph);

    /* Create VRSaveProgress Object */
    IRI mnObj = Utilities.f.createIRI(WELCOME.NAMESPACE, "VRAppProgress-" + uuid);

    builder
        .namedGraph(graph)
        .subject(mnObj)
        .add(SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "hasSaveData"),
            body)
        .add(RDF.TYPE,
            SimpleValueFactory.getInstance().createIRI(WELCOME.NAMESPACE, "VRAppProgress"));


    /* We're done building, create our Model */
    Model model = builder.build();

    logger.info("(REPO) Adding VRAppProgress Info to the repository.");

    /* Commit model to repository */
    util.commitModel(model);
  }

  public void addNewCVObject(String value) {
    /* Retrieve the active dip */
    IRI activeDIP = q.activeDIP_S();

    /* Remove old graph before updating the information */
    IRI tempGraph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "tempGraph");
    Utilities.connection.clear(tempGraph);

    /* Clear SlotInfo graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "SlotInfo");
    Utilities.connection.clear(graph);

    switch (value) {
      case "education":
        /* Change status to Pending and remove previous responses */
        q.resetSlotsStatus(activeDIP, SlotLists.addEducationSlots, "Pending");
        break;
      case "language":
        /* Change status to Pending and remove previous responses */
        q.resetSlotsStatus(activeDIP, SlotLists.addLanguageSlots, "Pending");
        break;
      case "employment":
        /* Change status to Pending and remove previous responses */
        q.resetSlotsStatus(activeDIP, SlotLists.addEmploymentSlots, "Pending");
        break;
      case "course":
        /* Change status to Pending and remove previous responses */
        q.resetSlotsStatus(activeDIP, SlotLists.addCourseSlots, "Pending");
        break;
    }
  }

  public void checkCVStatus() {
    /* Create user IRI */
    IRI graph = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "ProfileInfo");

    /* Create IRI for CVStatus */
    IRI section = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "CVStatus");

    String oldStatus = q.getProfileValue(section, 0);

    if (!oldStatus.contentEquals("Complete")) {
      /* Insert CV Status */
      q.removeTCNData("CVStatus", graph);
      q.insertTCNData("CVStatus", "Incomplete", graph);
    }
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

    logger.info("(REPO) Updating TCN profile with coordinates input.");

    /* Commit model to repository */
    util.commitModel(model);

    return builder;
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
}
