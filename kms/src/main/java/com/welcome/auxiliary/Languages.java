package com.welcome.auxiliary;


import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;

public class Languages {

  public static String getNameFromCode(String iso3code) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "SELECT ?name where { \n";
    queryString += "    GRAPH welcome:langOntology { \n";
    queryString += "        ?s welcome:iso3code ?code . \n";
    queryString += "        ?s welcome:languageName ?name . \n";
    queryString += "    } \n";
    queryString += "    FILTER(?code = \"" + iso3code.toUpperCase() + "\") \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    String name = null;

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {
        BindingSet solution = result.next();
        name = solution.getBinding("name").getValue().stringValue();
      }
    }

    return name;
  }

  public static String getCodeFromName(String languageName) {
    String queryString;

    queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "SELECT ?code where { \n";
    queryString += "    GRAPH welcome:langOntology { \n";
    queryString += "        ?s welcome:iso3code ?code . \n";
    queryString += "        ?s welcome:languageName ?name . \n";
    queryString += "    } \n";
    queryString += "    FILTER(?code = \"" + languageName.toUpperCase() + "\") \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);
    String code = null;

    try (TupleQueryResult result = query.evaluate()) {
      if (result.hasNext()) {
        BindingSet solution = result.next();
        code = solution.getBinding("code").getValue().stringValue();
      }
    }

    return code;
  }
}
