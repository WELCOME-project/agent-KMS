package com.welcome.kms;

import com.welcome.auxiliary.Queries;
import com.welcome.auxiliary.Utilities;
import com.welcome.services.AGENT;
import com.welcome.services.APP;
import com.welcome.services.DMS;
import com.welcome.services.LAS;
import com.welcome.services.LID;
import com.welcome.services.NLG;
import com.welcome.services.WPM;
import java.io.IOException;
import java.text.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KmsController {

  Logger logger = LoggerFactory.getLogger(KmsController.class);

  Queries q = new Queries();
  WPM wpm = new WPM();
  LAS las = new LAS();
  DMS dms = new DMS();
  NLG nlg = new NLG();
  LID lid = new LID();
  AGENT agent = new AGENT();
  APP app = new APP();
  Utilities util = new Utilities();

  @PostMapping("/wpm/RegInput")
  public String loadRegData(
      @RequestBody String regInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @RequestHeader(value = "Authorization", required = false) String auth
  ) {

    long start = System.currentTimeMillis();

    boolean init;

    logger.info("(INIT) Loading parameters and opening connection with the repository.");

    // Load Properties
    util.loadProperties();

    // START KB
    init = util.startKB(Utilities.graphDB, Utilities.serverURL, auth);

    if (init) {
      logger.info("(INIT) Initialization succeeded.");
    } else {
      logger.error("(INIT) Initialization failed.");
    }

    /* Remove temporary ids used by agent for the initialization */
    q.removeTempIDs();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received Registration Input.");

    logger.info(regInput);

    wpm.wrapperRegInput(regInput);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("REG: Time Elapsed: " + timeElapsed);

    return regInput;
  }

  @PostMapping("/wpm/handshaking")
  public String loadHandshakingData(
      @RequestBody String logStatus,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @RequestHeader(value = "Authorization", required = false) String auth
  ) {
    boolean init;

    long start = System.currentTimeMillis();

    logger.info("(INIT) Loading parameters and opening connection with the repository.");

    // Load Properties
    util.loadProperties();

    // START KB
    init = util.startKB(Utilities.graphDB, Utilities.serverURL, auth);

    if (init) {
      logger.info("(INIT) Initialization succeeded.");
    } else {
      logger.error("(INIT) Initialization failed.");
    }

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received new Handshaking message.");

    return wpm.wrapperHandShaking(logStatus, start);
  }

  @PostMapping("/app/speaktoavatar")
  public String loadSpeakToAvatarData(
      @RequestBody String menuOption,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received new Speak To Avatar message.");

    return wpm.wrapperSpeakToAvatar(menuOption, start);
  }

  @PostMapping("/app/tcnProfile")
  public String loadTCNProfileData(
      @RequestBody String tcnProfileData,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @RequestHeader(value = "Authorization", required = false) String auth
  ) {
    long start = System.currentTimeMillis();

    boolean init;

    logger.info("(INIT) Loading parameters and opening connection with the repository.");

    // Load Properties
    util.loadProperties();

    // START KB
    init = util.startKB(Utilities.graphDB, Utilities.serverURL, auth);

    if (init) {
      logger.info("(INIT) Initialization succeeded.");
    } else {
      logger.error("(INIT) Initialization failed.");
    }

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received TCN-related data from the app.");

    app.wrapperPostTCNProfile(tcnProfileData);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("PROF_POST: Time Elapsed: " + timeElapsed);

    return "Received TCN-related data from the app.";
  }

  @PostMapping("/app/avatarConfig")
  public String loadAvatarConfig(
      @RequestBody String avatarData,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {
    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received Avatar-related data from the app.");

    app.wrapperPostAvatarConfig(avatarData);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("AVA_CONF: Time Elapsed: " + timeElapsed);

    return "Received Avatar-related data from the app.";
  }

  @PostMapping("/app/minigame/{minigame}")
  public String loadMinigameData(
      @RequestBody String body,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @PathVariable String minigame) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received Minigame-related data from the app.");

    app.wrapperPostMinigame(body, minigame);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("MIN: Time Elapsed: " + timeElapsed);

    return "Received Minigame-related data from the app.";
  }

  @PostMapping("/dips/input")
  public String loadDataDIPS(
      @RequestBody String dipInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) throws IOException {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received DIP from agent-core.");

    return agent.wrapperDIP(dipInput, start);
  }

  @PostMapping("/agent/notifications")
  public void loadAgentNotification(
      @RequestBody String agentNotification,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {
    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received Notification from agent-core.");

    agent.parseAgentNotification(agentNotification, start);
  }

  @PostMapping("/chc/notification")
  public String loadCHCNotification(
      @RequestBody String chcNotification,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {
    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received DIP from agent-core.");

    return agent.wrapperCHC(chcNotification, start);
  }

  @PostMapping("/las/systemInput")
  public String loadDataSystemLAS(
      @RequestBody String lasInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @RequestHeader(value = "X-Original-Input-Form", required = false) String form
  ) throws ParseException {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received input from LAS (System Turn).");

    las.wrapperSystemLAS(lasInput, form);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("LAS_SYS: Time Elapsed: " + timeElapsed);

    return "Received input from LAS (System Turn).";
  }

  @PostMapping("/las/input")
  public String loadDataLAS(
      @RequestBody String lasInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @RequestHeader(value = "X-Original-Input-Form", required = false) String form
  ) throws Exception {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received input from LAS.");

    las.wrapperLAS(lasInput, form);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("LAS: Time Elapsed: " + timeElapsed);

    return "Received input from LAS.";
  }

  @PostMapping("/nlg/input")
  public String loadDataNLG(
      @RequestBody String nlgInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received input from NLG.");

    logger.info(nlgInput);

    nlg.wrapperNLG(nlgInput);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("NLG: Time Elapsed: " + timeElapsed);

    return nlgInput;
  }

  @PostMapping("/agent/sparql")
  public String loadDataSparql(
      @RequestBody String sparqlInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) throws IOException {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received SPARQL from agent-core.");

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("SPARQL: Time Elapsed: " + timeElapsed);

    return agent.runAskQuery(sparqlInput);
  }

  @PostMapping("/dms/input")
  public String loadDataDMS(
      @RequestBody String dmsInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received input from DMS.");

    logger.info(dmsInput);

    dms.wrapperDMS(dmsInput);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("DMS: Time Elapsed: " + timeElapsed);

    return "Received input from DMS.";
  }

  @PostMapping("/lid/input")
  public JSONObject loadDataLID(
      @RequestBody String lidInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received input from LID.");

    logger.info(lidInput);

    return lid.wrapperLID(lidInput, start);
  }

  @PostMapping("/app/chcPreferences")
  public String loadCHCPreferences(
      @RequestBody String chcInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received input for CHC.");

    app.wrapperPostCHCPreferences(chcInput);

    logger.info(chcInput);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("CHC_POST: Time Elapsed: " + timeElapsed);

    return "Received CHC-related data from the app.";
  }

  @GetMapping("/app/chcPreferences")
  public JSONObject getCHCPreferences(
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received request for CHC.");

    JSONObject obj = app.wrapperGetCHCPreferences();

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("CHC_GET: Time Elapsed: " + timeElapsed);

    return obj;
  }

  @PostMapping("/dummyOutput")
  public String dummyEndpoint(
      @RequestBody String dummyInput,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received input to dummy endpoint.");

    logger.info(dummyInput);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("DUM: Time Elapsed: " + timeElapsed);

    return dummyInput;
  }

  @GetMapping("/version")
  public JSONObject verifyRestService(
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(TEST) KMS status check.");

    JSONObject obj = new JSONObject();
    obj.put("agent-KMS version", "v 5.2.0");

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("VER: Time Elapsed: " + timeElapsed);

    return obj;
  }

  @GetMapping({"/app/tcnProfile", "/app/tcnProfile/{section}"})
  public JSONObject getTCNProfile(
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @RequestHeader(value = "Authorization", required = false) String auth,
      @PathVariable(required = false) String section
  ) {
    long start = System.currentTimeMillis();

    boolean init;

    logger.info("(INIT) Loading parameters and opening connection with the repository.");

    // Load Properties
    util.loadProperties();

    // START KB
    init = util.startKB(Utilities.graphDB, Utilities.serverURL, auth);

    if (init) {
      logger.info("(INIT) Initialization succeeded.");
    } else {
      logger.error("(INIT) Initialization failed.");
    }

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) App requested TCN-related data.");

    JSONObject obj = app.wrapperGetTCNProfile(section);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("PROF_GET: Time Elapsed: " + timeElapsed);

    return obj;
  }

  @GetMapping("/app/avatarConfig")
  public JSONObject getAvatarConfig(
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {
    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) App requested Avatar-related data.");

    JSONObject obj = app.wrapperGetAvatarConfig();

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("AVA_GET: Time Elapsed: " + timeElapsed);

    return obj;
  }

  @GetMapping("/app/minigame/{minigame}")
  public String getMinigameData(
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId,
      @PathVariable String minigame) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) App requested Minigame-related data.");

    String obj = app.wrapperVRData(minigame);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("MINI_GET: Time Elapsed: " + timeElapsed);

    return obj;
  }
  
  @GetMapping("/app/AppProgress")
  public String getVRAppProgressData(
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {
    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) App requested VRAppProgress data.");

    String obj = app.wrapperVRData("VRAppProgress");

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("VR_GET: Time Elapsed: " + timeElapsed);

    return obj;
  }

  @PostMapping("/app/AppProgress")
  public String loadVRAppProgressData(
      @RequestBody String body,
      @RequestHeader(value = "X-Correlation-ID", required = false) String corId,
      @RequestHeader(value = "X-Turn-ID", required = false) String turnId
  ) {

    long start = System.currentTimeMillis();

    /* Check correlation id and turn id */
    q.checkHeaders(corId, turnId);

    logger.info("(ENDPOINT) Received VRAppProgress related data from the app.");

    app.wrapperPostVRAppProgress(body);

    long finish = System.currentTimeMillis();
    long timeElapsed = finish - start;

    logger.info("VR_POST: Time Elapsed: " + timeElapsed);

    return "Received Minigame-related data from the app.";
  }
}
