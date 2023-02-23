package com.welcome.auxiliary;

import com.welcome.ontologies.AJAN;
import com.welcome.ontologies.WELCOME;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utilities {

  /**
   * Function that sends a request to the python module to calculate the similarities given a JSON
   * string in the request body
   *
   * @param json
   * @param id
   * @return
   * @throws Exception
   */
  private static final OkHttpClient httpClient = new OkHttpClient
      .Builder()
      .readTimeout(30, TimeUnit.SECONDS)
      .build();
  public static String serverURL;
  public static String graphDB;
  public static HTTPRepository repository;
  public static RepositoryConnection connection;
  public static ValueFactory f;
  public static String key;
  public static String Url;
  public static String CorId;
  public static String TurnId;
  public static String Auth;
  public static boolean flag;
  public static String dispatcherURL;
  public static String kbsURL;
  public static String wpmURL;
  Logger logger = LoggerFactory.getLogger(Utilities.class);

  public static String sendPost(String url, String json, String version) throws Exception {
    RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
    Request request = new Request.Builder()
        .url(url + version)
        .post(body)
        .build();

    try (Response response = httpClient.newCall(request).execute()) {

      if (!response.isSuccessful()) {
        throw new IOException("Unexpected code " + response);
      }

      // Get response body
      String responseText = response.body().string().replaceAll("[\\[\\]]", "");

      return responseText;
    }
  }

  /**
   *
   */
  public void initKB() {
    logger.info("(INIT) Loading parameters and opening connection with the repository.");

    // Load Properties
    loadProperties();

    // START KB
    startKB(Utilities.graphDB, Utilities.serverURL, null);

    logger.info("(INIT) Initialization succeeded.");
  }

  /**
   * Function that loads the app.properties file.
   */
  public void loadProperties() {
    // Read properties
    String label = System.getenv("repositoryLabel");
    String url = System.getenv("serverURL");
    if (label == null || url == null) {
      // Utilities.serverURL = "http://localhost:8090/rdf4j-server";
      Utilities.serverURL = "http://localhost:7200";
      Utilities.graphDB = "test";
    } else {
      Utilities.serverURL = url;
      Utilities.graphDB = label;
    }

    // Set url to auxiliary component
    String dispatcherURL = System.getenv("dispatcherURL");
    if (dispatcherURL == null) {
      Utilities.dispatcherURL = "http://localhost:5000/kms/api/auxiliary/";
    } else {
      Utilities.dispatcherURL = dispatcherURL;
    }

    // Set url to KBS
    String kbsURL = System.getenv("kbsURL");
    if (kbsURL == null) {
      Utilities.kbsURL = "http://3.20.64.60:10013/kbs/api/geoLocation";
    } else {
      Utilities.kbsURL = kbsURL;
    }

    // Set url to WPM
    String wpmURL = System.getenv("wpmURL");
    if (wpmURL == null) {
      Utilities.wpmURL = "http://3.20.64.60:19997/welcome/integration/platform/agent/wakeUpAgents";
    } else {
      Utilities.wpmURL = wpmURL;
    }

    // Load properties file
    Properties properties = new Properties();
    InputStream is = Utilities.class.getResourceAsStream("/application.properties");
    try {
      properties.load(is);
    } catch (IOException e) {
      logger.warn("Reading property file failed!");
    }

    // Read properties
    Utilities.key = properties.getProperty("KEY");
    Utilities.Url = properties.getProperty("URL");
    Utilities.CorId = properties.getProperty("COR");
    Utilities.TurnId = properties.getProperty("TURN");
    Utilities.Auth = properties.getProperty("AUTH");
  }

  /**
   * Function that establishes a connection to a remote repository.
   *
   * @param repositoryId
   * @param serverURL
   */
  public boolean startKB(String repositoryId, String serverURL, String auth) {
    // Create an authorization header entry
    Map<String, String> authorizationHeader = new TreeMap<>();
    // Set up new connection to repo
    Utilities.repository = new HTTPRepository(serverURL, repositoryId);

    if (auth != null) {
      authorizationHeader.put("Authorization", auth);
      Utilities.repository.setAdditionalHttpHeaders(authorizationHeader);
    }

    // Init connection to repo
    Utilities.repository.init();

    // Set up new connection to repo
    Utilities.connection = Utilities.repository.getConnection();

    // Initialize value factory
    Utilities.f = Utilities.repository.getValueFactory();

    return true;
  }

  /**
   * Function that takes an RDF model and commits it
   *
   * @param model
   */
  public void commitModel(Model model) {
    // When adding data we need to start a transaction
    logger.info("(REPO) Starting Transaction.");
    Utilities.connection.begin();

    logger.info("(REPO) Adding Model.");
    Utilities.connection.add(model);

    logger.info("(REPO) Committing Transaction.");
    Utilities.connection.commit();
  }

  /**
   * Function that takes an RDF model and a resource and commits it
   *
   * @param model
   * @param resource
   */
  public void commitModel(Model model, Resource resource) {
    // When adding data we need to start a transaction
    logger.info("(REPO) Starting Transaction.");
    Utilities.connection.begin();

    logger.info("(REPO) Adding Model to named graph.");
    Utilities.connection.add(model, resource);

    logger.info("(REPO) Committing Transaction.");
    Utilities.connection.commit();
  }

  /**
   * Executes a SPARQL UPDATE (INSERT or DELETE) statement.
   *
   * @param repositoryConnection a connection to a repository
   * @param update               the SPARQL UPDATE query in text form
   * @param bindings             optional bindings to set on the prepared query
   * @throws MalformedQueryException
   * @throws RepositoryException
   */
  public void executeUpdate(RepositoryConnection repositoryConnection, String update,
      Binding... bindings)
      throws MalformedQueryException, RepositoryException, UpdateExecutionException {
    Update preparedUpdate = repositoryConnection.prepareUpdate(QueryLanguage.SPARQL, update);
    // Setting any potential bindings (query parameters)
    for (Binding b : bindings) {
      preparedUpdate.setBinding(b.getName(), b.getValue());
    }
    preparedUpdate.execute();
  }

  /**
   * This functions starts a transaction and executes the update query
   *
   * @param queryString
   */
  public void executeQuery(String queryString) {
    // When adding data we need to start a transaction
    logger.info("(REPO) Starting Transaction.");
    Utilities.connection.begin();

    logger.info("(REPO) Executing Update.");
    executeUpdate(connection, String.format(queryString));

    logger.info("(REPO) Committing Transaction.");
    Utilities.connection.commit();
  }

  /**
   * @throws IOException
   */
  public String exportActiveDIP() throws IOException {
    String queryString = "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select distinct ?g where { \n";
    queryString += "    graph ?g { \n";
    queryString += "       ?x rdf:type welcome:DIP . \n";
    queryString += "    } \n";
    queryString += "    graph welcome:ActiveDIP { \n";
    queryString += "       ?x welcome:isActive true . \n";
    queryString += "    } \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    Resource g = null;
    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();

        g = (Resource) solution.getBinding("g").getValue();
      }
    }

    return exportTempGraph(g);
  }

  /**
   * Function that loads the given ontology file in the repository.
   */
  public void loadOntologyURL(String namespace, String graph) {

    logger.info("[+] [Loading ontology file...]");

    try {
      Utilities.connection.begin();

      URL url = new URL(
          "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/" + namespace + ".ttl#");

      /* Create IRI for speech act */
      IRI global = Utilities.f
          .createIRI(WELCOME.NAMESPACE, graph);

      Utilities.connection.clear(global);

      Utilities.connection.add(url, RDFFormat.TURTLE, global);

      // Committing the transaction persists the data
      Utilities.connection.commit();
    } catch (RDFParseException | RepositoryException | IOException e) {
      logger.warn("[x] Loading ontology file failed!");
    }
  }

  /**
   * @param namedGraph
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
    m.setNamespace(RDFS.NS);

    // Write Model to file
    try {
      Rio.write(m, out, RDFFormat.TURTLE);
    } finally {
      out.close();
    }

    // Load file to a string
    String path = "graphExport.ttl";
    String content = "";
    try (Stream<String> lines = Files.lines(Paths.get(path))) {
      content = lines.collect(Collectors.joining(System.lineSeparator()));

      // Send data to the Agent-Core
      logger.info("Sending notification to agent: receiveSpeechAct");

      sendData(content, "agent-core", "receiveSpeechAct", "text/turtle");
    } catch (IOException e) {
      e.printStackTrace();
    }

    return content;
  }

  public String sendData(String jsonInput, String recipient, String param, String contentType) {
    URL url = null;

    try {
      if (recipient.contentEquals("agent-core")) {
        String getenv = System.getenv("agentResponse");
        if (getenv == null) {
          url = new URL(
              "http://localhost:8060/welcome/integration/coordination/ajan/agents/agent31");
        } else {
          String env = System.getenv("agentResponse");
          String base = env + "?capability=" + param;
          url = new URL(base);
        }
      } else if (recipient.contentEquals("kbs")) {
        String getenv = System.getenv("kbsURL");
        if (getenv == null) {
          url = new URL(
              "http://3.20.64.60:10013/kbs/api/geoLocation");
        } else {
          url = new URL(getenv);
        }
      } else if (recipient.contentEquals("wpm")) {
        String getenv = System.getenv("wpmURL");
        if (getenv == null) {
          url = new URL(
              "http://3.20.64.60:19997/welcome/integration/platform/agent/wakeUpAgents");
        } else {
          url = new URL(getenv);
        }
      }

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", contentType);
      connection.setDoOutput(true);
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(1000000);

      try (OutputStream os = connection.getOutputStream()) {
        byte[] in = jsonInput.getBytes(StandardCharsets.UTF_8);
        os.write(in, 0, in.length);
        logger.info("(RESPONSE) KMS Service Send Data Successfully.");
      }

      try (BufferedReader br = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());
        }

        if (!recipient.contentEquals("agent-core")) {
          return response.toString();
        } else {
          return "";
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    }

    return "";
  }

  /**
   * This function returns the IRIs of the dialogue user and the dialogue session.
   *
   * @return
   */
  public IRI[] getUserSession() {
    ValueFactory f = Utilities.repository.getValueFactory();

    // Init variables
    IRI[] iri = new IRI[2];
    String queryString;

    // Find the IRI of the Dialogue User and the Dialogue Session
    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#>\n";
    queryString += "SELECT DISTINCT ?user ?session where { \n";
    queryString += "    ?user rdf:type welcome:DialogueUser .\n";
    queryString += "    ?session rdf:type welcome:DialogueSession .\n";
    queryString += "    ?session welcome:hasTimestamp ?timestamp . \n";
    queryString += "} \n";
    queryString += "ORDER BY DESC(?timestamp) \n";
    queryString += "LIMIT 1 \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      while (result.hasNext()) {
        BindingSet solution = result.next();

        Value user = solution.getBinding("user").getValue();
        iri[0] = f.createIRI(user.stringValue());

        Value session = solution.getBinding("session").getValue();
        iri[1] = f.createIRI(session.stringValue());
      }
    }

    return iri;
  }

  public JSONObject parseJSONLD(String input) {
    // Read JSON file containing information about DMS Input
    JSONParser parser = new JSONParser();

    // Create JSONObject of DMS Input
    JSONObject object = new JSONObject();

    try {
      // Try to parse message
      logger.info("(INFO) Parsing json(-ld) to RDF.");
      object = (JSONObject) parser.parse(input);
    } catch (org.json.simple.parser.ParseException e) {
      logger.error("(ERROR) Unable to parse message!");
    }

    return object;
  }

  public ModelBuilder getBuilder() {
    /* Initialize RDF builder */
    ModelBuilder builder = new ModelBuilder()
        .setNamespace("welcome", WELCOME.NAMESPACE);

    return builder;
  }

  /**
   * Function that sends a request to babelNet and receives resources.
   *
   * @param bn
   * @return
   * @throws IOException
   */
  public String babelNetRequest(String bn) throws IOException {
    URL url = new URL(Utilities.Url);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");

    con.setRequestProperty("Accept-Encoding", "application/gzip");
    con.setConnectTimeout(5000);
    con.setReadTimeout(5000);

    Map<String, String> parameters = new HashMap<>();
    parameters.put("id", bn);
    parameters.put("key", Utilities.key);

    con.setDoOutput(true);
    DataOutputStream out = new DataOutputStream(con.getOutputStream());
    out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
    out.flush();
    out.close();

    int status = con.getResponseCode();

    String response = getFullResponse(con);

    return response;
  }

  public JSONObject coordinatesRequest(String location) throws IOException {
    JSONObject o = new JSONObject();

    try {
      URL url = new URL("https://nominatim.openstreetmap.org/search?q=Αθήνα&format=json&limit=1");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      int responseCode = con.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Read the response data
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        in.close();

        // Parse the JSON response
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(response.toString());
        JSONArray array = (JSONArray) obj;

        o = (JSONObject) array.get(0);
      } else {
        System.out.println("GET request failed");
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
    }

    return o;
  }

  /**
   * Returns the response of a GET Request in String format.
   *
   * @param con
   * @return
   */
  public String getFullResponse(HttpURLConnection con) {
    StringBuilder fullResponseBuilder = new StringBuilder();

    Reader streamReader;

    try {
      if (con.getResponseCode() > 299) {
        streamReader = new InputStreamReader(new GZIPInputStream(con.getErrorStream()));
      } else {
        streamReader = new InputStreamReader(new GZIPInputStream(con.getInputStream()));
      }

      BufferedReader in = new BufferedReader(streamReader);
      String inputLine;
      StringBuilder content = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }

      in.close();

      fullResponseBuilder.append(content);
    } catch (IOException e) {
      logger.warn("[x] Failed to get response from Server!");
    }

    return fullResponseBuilder.toString();
  }

  /**
   * Checks if string is numeric.
   *
   * @param string
   * @return
   */
  public boolean isNumeric(String string) {
    int intValue;

    if (string == null || string.equals("")) {
      logger.warn("String cannot be parsed, it is null or empty.");
      return false;
    }

    try {
      intValue = Integer.parseInt(string);
      return true;
    } catch (NumberFormatException e) {
      logger.warn("Input String cannot be parsed to Integer.");
    }
    return false;
  }

  public double twoPointDistance(double x0, double x1, double y0, double y1) {

    if ((x0 == x1) && (y0 == y1)) {
      return 0.0;
    } else {
      double theta = y0 - y1;
      double dist = Math.sin(Math.toRadians(x0)) * Math.sin(Math.toRadians(x1))
          + Math.cos(Math.toRadians(x0)) * Math.cos(Math.toRadians(x1)) * Math.cos(
          Math.toRadians(theta));

      dist = Math.acos(dist);
      dist = Math.toDegrees(dist);

      dist = dist * 60 * 1.1515;
      dist = dist * 1.609344;

      return dist;
    }
  }

  public String getSimilarSlots(String url, String anchor, String version) throws Exception {

    String similarSlots;

    logger.info(anchor);

    similarSlots = Utilities.sendPost(url, anchor, version);

    return similarSlots;
  }

  /**
   * Splits an IRI and returns value after #
   *
   * @param activeSlot
   * @return
   */
  public String splitIRI(IRI activeSlot) {
    /* Split IRI from actual name */
    String[] split = activeSlot.stringValue().split("#");
    String slotName = split[1];

    return slotName;
  }

  public JSONArray sortJSONArray(JSONArray array) {
    List<JSONObject> myList = new ArrayList<>();
    JSONArray sortedEntities = new JSONArray();

    for (Object en : array) {
      /* Create object and get entity */
      JSONObject entity = (JSONObject) en;
      myList.add(entity);
    }

    Collections.sort(myList, (objectA, objectB) -> {
      int compare = 0;
      try {
        String stringA = (String) objectA.get("id");
        String stringB = (String) objectB.get("id");

        int idA = Integer.parseInt(stringA.substring(stringA.lastIndexOf("_") + 1));
        int idB = Integer.parseInt(stringB.substring(stringB.lastIndexOf("_") + 1));

        compare = Integer.compare(idA, idB);

      } catch (Exception e) {
        e.printStackTrace();
      }
      return compare;
    });

    for (int i = 0; i < myList.size(); i++) {
      sortedEntities.add(myList.get(i));
    }

    return sortedEntities;
  }

  public List<String> getRelevantAgents(List<String> locList) {
    List<String> agentList = new ArrayList<>();

    // find user id
    IRI UserId = Utilities.f
        .createIRI(WELCOME.NAMESPACE, "UserId");

    String id = getProfileValue(UserId);

    // create a json object
    JSONObject obj = new JSONObject();

    // put id in the object
    obj.put("@id", "welcome:agent-core-" + id);

    // create a json array
    JSONArray arr = new JSONArray();

    // populate array with list of location preferences
    for (String loc : locList) {
      arr.add(loc);
    }

    // add array to json object
    obj.put("welcome:hasLocationPreference", arr);

    // send response to KBS
    String stringResponse = sendData(obj.toString(), "kbs", null, "application/json");

    // Parse response
    JSONParser parser = new JSONParser();
    JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse = (JSONObject) parser.parse(stringResponse);
    } catch (ParseException e) {
      e.printStackTrace();
    }

    // convert list of agents to array list
    JSONArray agents = (JSONArray) jsonResponse.get("welcome:hasListOfAgents");

    if (agents != null) {
      //Iterating JSON array
      for (int i = 0; i < agents.size(); i++) {
        //Adding each element of JSON array into ArrayList
        agentList.add(((String) agents.get(i)).replace("agent-core-", ""));
      }
    }

    // send response to main method
    return agentList;
  }

  public String getProfileValue(IRI cName) {
    String queryString;

    queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
    queryString += "PREFIX welcome: <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#> \n";
    queryString += "select DISTINCT ?value where { \n";
    queryString += "    GRAPH <https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#ProfileInfo> { \n";
    queryString += "		welcome:tcn_user ?pred ?obj . \n";
    queryString += "        ?obj rdf:type ?class . \n";
    queryString += "        ?obj welcome:hasValue ?value . \n";
    queryString += "    } \n";
    queryString += "    FILTER (?class = IRI(<" + cName.stringValue() + ">)) \n";
    queryString += "} \n";

    TupleQuery query = Utilities.connection.prepareTupleQuery(queryString);

    try (TupleQueryResult result = query.evaluate()) {
      // we just iterate over all solutions in the result...
      if (result.hasNext()) {
        BindingSet solution = result.next();
        Value v = solution.getBinding("value").getValue();
        return v.stringValue();
      } else {
        return "";
      }
    }
  }

  public JSONArray wakeUpRelevantAgents(List<String> agentsList) {
    // create json object
    JSONObject obj = new JSONObject();

    // add array to json object
    obj.put("agents", agentsList);

    logger.info(obj.toString());

    // send response to KBS
    String stringResponse = sendData(obj.toString(), "wpm", null, "application/json");

    if (!stringResponse.contentEquals("")) {
      // Parse response
      JSONParser parser = new JSONParser();
      JSONArray jsonResponse = new JSONArray();
      try {
        jsonResponse = (JSONArray) parser.parse(stringResponse);
      } catch (ParseException e) {
        e.printStackTrace();
      }

      return jsonResponse;
    }

    return (new JSONArray());
  }

  public void loadAgentsInLAKR(JSONArray arr) {
    /* Initialize RDF builder */
    ModelBuilder builder = getBuilder();

    /* Clear activeAgents graph */
    IRI graph = Utilities.connection
        .getValueFactory()
        .createIRI(WELCOME.NAMESPACE + "activeAgents");
    Utilities.connection.clear(graph);

    builder
        .namedGraph(graph)
        .add(AJAN.agentName, RDF.TYPE, OWL.ANNOTATIONPROPERTY)
        .add(WELCOME.hasAddress, RDF.TYPE, OWL.ANNOTATIONPROPERTY)
        .add(WELCOME.hasStatus, RDF.TYPE, OWL.ANNOTATIONPROPERTY);

    // iterate the array objects
    for (Object o : arr) {
      JSONObject obj = (JSONObject) o;

      String status = (String) obj.get("status");
      String userId = (String) obj.get("userId");
      String agentAddress = (String) obj.get("agentAddress");

      String address =
          agentAddress + "/welcome/integration/coordination/ajan/agents/agent-core-" + userId;

      IRI agent = Utilities.connection
          .getValueFactory()
          .createIRI(address);

      IRI st = Utilities.connection
          .getValueFactory()
          .createIRI(WELCOME.NAMESPACE + status);

      /* Update DipSession graph */
      builder
          .namedGraph(graph)
          .subject(agent)
          .add(RDF.TYPE, OWL.NAMEDINDIVIDUAL)
          .add(RDF.TYPE, AJAN.Agent)
          .add(AJAN.agentName, "agent-core-" + userId)
          .add(WELCOME.hasAddress, address)
          .add(WELCOME.hasStatus, st);
    }

    /* We're done building, create our Model */
    Model model = builder.build();

    /* Commit model to repository */
    commitModel(model);
  }
}
