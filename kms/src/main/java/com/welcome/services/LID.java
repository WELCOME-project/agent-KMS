package com.welcome.services;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.Utilities;
import com.welcome.ontologies.WELCOME;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LID {

  Logger logger = LoggerFactory.getLogger(LAS.class);

  /* Initialize external classes */
  Utilities util = new Utilities();
  Queries q = new Queries();

  public JSONObject wrapperLID(String input, long start) {
    String result = jsonToRdf(util.parseJSONLD(input));

    JSONObject jsonObject = new JSONObject();

    if (!result.contentEquals("und")) {
      jsonObject.put("status", "identified");
      jsonObject.put("language", result);
    } else {
      jsonObject.put("status", "unknown");
      jsonObject.put("language", "");
    }

    logger.info(result);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("LID: Time Elapsed: " + timeElapsed);

    return jsonObject;
  }

  public String jsonToRdf(JSONObject lidObject) {

    /* Create IRI for Language */
    IRI langType = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "LanguageCode");

    String language = q.getProfileValue(langType, 0);

//    HashMap<String, Double> scoreList = new HashMap<>();
//
//    for (Iterator iterator = lidObject.keySet().iterator(); iterator.hasNext(); ) {
//      String key = ((String) iterator.next()).toLowerCase();
//
//      Double score = Double.parseDouble((String) lidObject.get(key));
//
//      scoreList.put(key, score);
//    }
//
//    double max = Collections.max(scoreList.values());
//
//    if (max == 0.0) {
//      return language;
//    } else {
//      for (Entry<String, Double> entry : scoreList.entrySet()) {
//        if (entry.getValue() == max) {
//          if (entry.getKey().contentEquals("und")) {
//            return language;
//          } else {
//            return entry.getKey();
//          }
//        }
//      }
//    }

    return language;
  }
}
