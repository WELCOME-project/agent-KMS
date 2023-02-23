package com.welcome.services;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NLG {

  Logger logger = LoggerFactory.getLogger(NLG.class);

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries query = new Queries();
  AGENT agent = new AGENT();

  /**
   * Wrapper function for handling new NLG input.
   *
   * @param input Refers to the input received from the endpoint.
   * @return
   */
  public void wrapperNLG(String input) {
    // Read JSON file containing information about DMS Input
    JSONParser parser = new JSONParser();

    // Create JSONObject of DMS Input
    JSONObject object;

    String nlgInput;

    try {
      // Try to parse message
      logger.info("(INFO) Parsing json(-ld) to RDF.");
      object = (JSONObject) parser.parse(input);

      /* Get speech act type and anchor */
      nlgInput = (String) object.get("text");

      /* Create RDF Model */
      stringToRdf(nlgInput);
    } catch (org.json.simple.parser.ParseException e) {
      logger.warn("(WARN) Input is not JSON, parsing simple text!");

      /* Create RDF Model */
      stringToRdf(input);
    }
  }

  /**
   * Loads NLG input file into the database.
   *
   * @param input Refers to the new NLG input.
   */
  public void stringToRdf(String input) {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    /* Check previous system turn */
    IRI temp = query.latestSystemTurn_S();
    if (temp != null) {
      builder
          .namedGraph(temp)
          .subject(temp)
          .add(WELCOME.TURNTRANSCRIPTION, input);

      /* We're done building, create a Model */
      Model model = builder.build();

      logger.info("(REPO) Assigning NLG input to the corresponding System Turn.");

      /* Commit model to repository */
      util.commitModel(model);
    }
  }
}
