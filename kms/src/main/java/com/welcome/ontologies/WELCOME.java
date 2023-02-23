package com.welcome.ontologies;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class WELCOME {
    public static final String NAMESPACE = "https://raw.githubusercontent.com/gtzionis/WelcomeOntology/main/welcome.ttl#";

    public static final String PREFIX = "welcome";

    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

    public static final IRI TURN = getIRI("DialogueTurn");

    public static final IRI SERVICEREQUEST = getIRI("ServiceRequest");

    public static final IRI REGISTERED = getIRI("Registered");

    public static final IRI REGISTEREDSTATUS = getIRI("RegistrationStatus");

    public static final IRI USERTURN = getIRI("DialogueUserTurn");

    public static final IRI SYSTEMTURN = getIRI("DialogueSystemTurn");

    public static final IRI CONTAINER = getIRI("SpeechActContainer");

    public static final IRI HASCONTAINER = getIRI("hasSpeechActContainer");

    public static final IRI SPEECHTYPE = getIRI("hasSpeechActType");

    public static final IRI INVOLVESSLOT = getIRI("involvesSlot");

    public static final IRI SPEECHOBJECT = getIRI("hasSpeechActObject");

    public static final IRI TURNTRANSCRIPTION = getIRI("hasTurnTranscription");

    public static final IRI TRANSCRIPTION = getIRI("hasContainerTranscription");

    public static final IRI HASCONTAINERID = getIRI("hasContainerId");

    public static final IRI ISLASTCONTAINER = getIRI("isLastContainer");

    public static final IRI NEXTCONTAINER = getIRI("hasNextContainer");

    public static final IRI PREVCONTAINER = getIRI("hasPreviousContainer");

    public static final IRI ENTITY = getIRI("DetectedEntity");

    public static final IRI RELATION = getIRI("DetectedRelation");

    public static final IRI HASRELATION = getIRI("hasDetectedRelation");

    public static final IRI HASENTITY = getIRI("hasDetectedEntity");

    public static final IRI ELINK = getIRI("hasEntityLink");

    public static final IRI ECONFIDENCE = getIRI("hasEntityConfidence");

    public static final IRI ETYPE = getIRI("hasEntityType");

    public static final IRI EANCHOR = getIRI("hasEntityAnchor");

    public static final IRI PREDICATERELATION = getIRI("hasPredicateRelation");

    public static final IRI PARTICIPANT = getIRI("Participant");

    public static final IRI ROLE = getIRI("hasDetectedRole");

    public static final IRI HASPARTICIPANT = getIRI("hasParticipant");

    public static final IRI TIMESTAMP = getIRI("hasTimestamp");

    public static final IRI USER = getIRI("DialogueUser");

    public static final IRI SESSION = getIRI("DialogueSession");

    public static final IRI ISINVOLVED = getIRI("isInvolvedInDialogueSession");

    public static final IRI USERID = getIRI("hasUserId");

    public static final IRI USER_ID = getIRI("UserId");

    public static final IRI HASTURN = getIRI("hasDialogueTurn");

    public static final IRI ISACTIVE = getIRI("isActiveDIP");

    public static final IRI ACTIVE = getIRI("active");

    public static final IRI ISACTIVESLOT = getIRI("isActiveSlot");

    public static final IRI ISACTIVESESSION = getIRI("isActiveSession");

    public static final IRI BELONGSTOGRAPH = getIRI("belongsToGraph");

    public static final IRI REPLIESTO = getIRI("repliesToUserResponse");

    public static final IRI CITY = getIRI("City");

    public static final IRI ID = getIRI("id");

    public static final IRI HOSTCOUNTRY = getIRI("HostCountry");

    public static final IRI COUNTRY = getIRI("Country");

    public static final IRI COUNTRYOFBIRTH = getIRI("CountryOfBirth");

    public static final IRI COUNTRYOFORIGIN = getIRI("CountryOfOrigin");

    public static final IRI LANGUAGE = getIRI("Language");

    public static final IRI PREFERREDLANG = getIRI("PreferredLanguage");

    public static final IRI EMAIL = getIRI("Email");

    public static final IRI USERNAME = getIRI("Username");

    public static final IRI NAME = getIRI("Name");

    public static final IRI LASTNAME = getIRI("FirstSurname");

    public static final IRI PASSPORT = getIRI("IDNumber");

    public static final IRI REPLIESTOUSER = getIRI("repliesToUserResponse");

    public static final IRI REPLIESTOSYSTEM = getIRI("repliesToSystemResponse");

    public static final IRI HASNEXTTURN = getIRI("hasNextTurn");

    public static final IRI HASPREVTURN = getIRI("hasPrevTurn");

    public static final IRI TRIGGERSDIP = getIRI("triggersDIP");

    public static final IRI TRIGGEREDBY = getIRI("triggeredBy");

    public static final IRI INVOLVESDIP = getIRI("involvesDIP");

    public static final IRI COMMIT = getIRI("Commit");

    public static final IRI VERSION = getIRI("versionJsonNumber");

    public static final IRI LOCKED = getIRI("locked");

    public static final IRI SCENARIOID = getIRI("scenarioID");

    public static final IRI HASSCENARIO = getIRI("hasScenario");

    public static final IRI HASSTAGE = getIRI("hasStage");

    public static final IRI STAGEP = getIRI("stage");

    public static final IRI WRONGANSWER = getIRI("wrongAnswer");

    public static final IRI TIME = getIRI("time");

    public static final IRI SCORE = getIRI("score");

    public static final IRI MINIGAME = getIRI("Minigame");

    public static final IRI STAGE = getIRI("Stage");

    public static final IRI SCENARIO = getIRI("Scenario");

    public static final IRI GENDER = getIRI("Gender");

    public static final IRI AGE = getIRI("Age");

    public static final IRI BIRTHDAY = getIRI("Birthday");

    public static final IRI BIRTHYEAR = getIRI("BirthYear");

    public static final IRI BIRTHMONTH = getIRI("BirthMonth");

    public static final IRI POSTAL_ADDRESS = getIRI("PostalAddress");

    public static final IRI REGISTRATION_DATE = getIRI("RegistrationDate");

    public static final IRI ACCOUNT_STATUS = getIRI("AccountStatus");

    public static final IRI BNDOCUMENT = getIRI("BabelNetDocument");

    public static final IRI DIP = getIRI("DIP");

    public static final IRI HASSLOT = getIRI("hasSlot");

    public static final IRI SYSTEMINFO = getIRI("SystemInfo");

    public static final IRI SYSTEMDEMAND = getIRI("SystemDemand");

    public static final IRI PROCESSED = getIRI("hasProcessedUtterance");

    public static final IRI FULLSENTENCE = getIRI("hasFullSentence");

    public static final IRI HASVALUE = getIRI("hasValue");

    public static final IRI CV = getIRI("CV");

    public static final IRI HASELEM = getIRI("hasElem");

    public static final IRI LASUPDATED = getIRI("lastUpdated");

    public static final IRI ISLOGGED = getIRI("isLogged");

    public static final IRI hasAddress = getIRI("hasAddress");

    public static final IRI hasStatus = getIRI("hasStatus");

    /**
     * Creates a new {@link IRI} with this vocabulary's namespace for the given local name.
     *
     * @param localName a local name of an IRI, e.g. 'creatorOf', 'name', 'Artist', etc.
     * @return an IRI using the http://www.semanticweb.org/image-ontology/ namespace and the given local name.
     */
    private static IRI getIRI(String localName) {
        return SimpleValueFactory.getInstance().createIRI(NAMESPACE, localName);
    }
}
