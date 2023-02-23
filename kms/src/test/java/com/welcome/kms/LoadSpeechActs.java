package com.welcome.kms;

import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import com.welcome.services.LAS;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LoadSpeechActs {

  public static void main(String[] args) {
    LAS las = new LAS();
    Utilities util = new Utilities();

    // Load Properties
    util.loadProperties();
    util.startKB(Utilities.graphDB, Utilities.serverURL, null);

    String path = "C:\\Users\\dimos\\Documents\\GitHub\\kms-spring\\kms\\src\\main\\resources\\agent.utterances";

    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();

    /* Remove old graph before updating the information */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE, "AgentUtterancesProcessed");

    las.removeTempGraph(graph);

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        System.out.println("File " + listOfFiles[i].getName());

        String[] splitFullName = listOfFiles[i].getName().split("\\.");
        String baseName = splitFullName[0];
        Long timestamp = randomNumber(10);

        String[] splitBaseName = baseName.split("_");
        String dipName = splitBaseName[0];
        String slotName = splitBaseName[1];
        String slotType = splitBaseName[2];

        System.out.println(dipName);
        System.out.println(slotName);
        System.out.println(slotType);

        JSONParser parser = new JSONParser();
        try {
          Object obj = parser.parse(
              new FileReader(path + "\\" + listOfFiles[i].getName()));

          // A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
          JSONObject jsonObject = (JSONObject) obj;

          /* Initialize RDF builder */
          ModelBuilder builder = new ModelBuilder()
              .setNamespace("welcome", WELCOME.NAMESPACE);

          /* Create IRIs */
          IRI dipObject = Utilities.f
              .createIRI(WELCOME.NAMESPACE, dipName);

          IRI slotObject = Utilities.f
              .createIRI(WELCOME.NAMESPACE, slotName);

          IRI slotTypeObject = Utilities.f
              .createIRI(WELCOME.NAMESPACE, slotType);

          IRI speechObject = Utilities.f
              .createIRI(WELCOME.NAMESPACE, baseName);

          /* Add user to builder */
          builder
              .namedGraph(graph)
              .subject(dipObject)
              .add(RDF.TYPE, WELCOME.DIP)
              .add(WELCOME.HASSLOT, slotObject)
              .subject(slotObject)
              .add(RDF.TYPE, slotTypeObject);

          /* Get object of data */
          JSONObject data = (JSONObject) jsonObject.get("data");

          /* Get array of speech acts */
          JSONArray speechActs = (JSONArray) data.get("speechActs");

          /* Get array of entities */
          JSONArray entities = (JSONArray) data.get("entities");

          /* Convert speech acts to RDF model */
          speechActsToRDF(speechActs, timestamp, slotObject, speechObject, builder, true);

          /* Convert entities to RDF model */
          entitiesToRDF(entities, timestamp, slotObject, speechObject, builder, true);

          /* We're done building, create our Model */
          Model model = builder.build();

          /* Commit model to repository */
          util.commitModel(model);

        } catch (IOException e) {
          e.printStackTrace();
        } catch (ParseException | java.text.ParseException e) {
          e.printStackTrace();
        }
      } else if (listOfFiles[i].isDirectory()) {
        System.out.println("Directory " + listOfFiles[i].getName());
      }
    }
  }

  /* Generates a random string of digits of given length */
  static Long randomNumber(int len) {
    final String digits = "0123456789";
    SecureRandom rnd = new SecureRandom();

    StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(digits.charAt(rnd.nextInt(digits.length())));
    }
    return Long.parseLong(String.valueOf(sb));
  }

  public static ModelBuilder speechActsToRDF(JSONArray speechActs, long timestamp,
      Resource slotObject, Resource speechObject, ModelBuilder builder, boolean userLAS) {
    String fullSentence;
    fullSentence = "";

    if (speechActs != null) {
      /* Iterate through entities */
      for (Object sp : speechActs) {
        /* Create object and get entity */
        JSONObject speechAct = (JSONObject) sp;

        /* Get speech act anchor */
        String anchor = (String) speechAct.get("anchor");

        fullSentence += anchor;
      }

      /* Add entity to builder */
      builder
          .subject(speechObject)
          .add(WELCOME.FULLSENTENCE, fullSentence)
          .subject(WELCOME.FULLSENTENCE)
          .add(RDF.TYPE, OWL.DATATYPEPROPERTY);

    }

    return builder;
  }

  public static ModelBuilder entitiesToRDF(JSONArray entities, long timestamp,
      Resource slotObject, Resource speechObject, ModelBuilder builder, boolean userLAS)
      throws java.text.ParseException {

    String babelNetIDs;
    babelNetIDs = "";

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

        /* Check type of confidence since it may be either
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

        /* Create IRI for Entity */
        IRI entityObject = Utilities.f
            .createIRI(WELCOME.NAMESPACE, id + "_" + timestamp);

        IRI utteranceType;
        if (!userLAS) {
          utteranceType = Utilities.f
              .createIRI(WELCOME.NAMESPACE, "agentUtterance");
        } else {
          utteranceType = Utilities.f
              .createIRI(WELCOME.NAMESPACE, "tcnUtterance");
        }

        /* Add entity to builder */
        builder
            .subject(slotObject)
            .add(WELCOME.PROCESSED, speechObject)

            .subject(WELCOME.PROCESSED)
            .add(RDF.TYPE, OWL.OBJECTPROPERTY)

            .subject(speechObject)
            .add(RDF.TYPE, utteranceType)
            .add(WELCOME.HASENTITY, entityObject)

            .subject(WELCOME.HASENTITY)
            .add(RDF.TYPE, OWL.OBJECTPROPERTY)

            .subject(entityObject)
            .add(WELCOME.ID, id + "_" + timestamp)
            .subject(WELCOME.ID)
            .add(RDF.TYPE, OWL.DATATYPEPROPERTY)

            .subject(entityObject)
            .add(WELCOME.TIMESTAMP, new Date(timestamp))
            .subject(WELCOME.TIMESTAMP)
            .add(RDF.TYPE, OWL.DATATYPEPROPERTY)

            .subject(entityObject)
            .add(WELCOME.ETYPE, type)
            .subject(WELCOME.ETYPE)
            .add(RDF.TYPE, OWL.DATATYPEPROPERTY)

            .subject(entityObject)
            .add(WELCOME.EANCHOR, anchor)
            .subject(WELCOME.EANCHOR)
            .add(RDF.TYPE, OWL.DATATYPEPROPERTY)

            .subject(entityObject)
            .add(WELCOME.ECONFIDENCE, confidence)
            .subject(WELCOME.ECONFIDENCE)
            .add(RDF.TYPE, OWL.DATATYPEPROPERTY)

            .subject(entityObject)
            .add(RDF.TYPE, WELCOME.ENTITY);

        /* Get entity link */
        JSONArray link = (JSONArray) entity.get("links");
        if (link.size() > 0) {
          for (Object l : link) {
            String temp = (String) l;

            /* Add link to builder */
            builder
                .subject(entityObject)
                .add(WELCOME.ELINK, temp)
                .subject(WELCOME.ELINK)
                .add(RDF.TYPE, OWL.DATATYPEPROPERTY);

            if (temp.contains("bn")) {
              babelNetIDs += temp + " ";
            }
          }
        }
      }

      /* Add entity to builder */
      builder
          .subject(speechObject)
          .add(WELCOME.BNDOCUMENT, babelNetIDs)
          .subject(WELCOME.BNDOCUMENT)
          .add(RDF.TYPE, OWL.DATATYPEPROPERTY);
    }

    return builder;
  }
}
