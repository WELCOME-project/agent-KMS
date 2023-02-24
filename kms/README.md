# agent-KMS
The discourse relation tool can be used for identifying 
relations between two adjacent utterances of a user. More specifically, it can recognize 
four major types of discourse relations (temporal, contingency, comparison, and expansion) as well as 
the absense of a relation between the utterances. 

## Requirements
`Java=11.0.18` 
`Eclipse RDF4J Server and Workbench=3.7.7` 

## Installation 
To use the agent-KMS, you need to clone the repository locally and 
create a war file as follows:
```
$ cd kms
$ mvn clean package
```
To create a docker image, you need to place the Dockerfile and
the generated war file in the same folder and run:
```
$ docker build -t kms-spring .
```

In addition, you need to install Eclipse DF4J Server and Workbench
as follows:
```
$ docker pull eclipse/rdf4j-workbench:3.7.7


## Run 
You can run the application with the following command 
```
$ docker run -d --restart always --name kms-spring kms-spring
$ docker run -d -p 8080:8080 -e JAVA_OPTS="-Xms1g -Xmx4g" \
	-v data:/var/rdf4j -v logs:/usr/local/tomcat/logs eclipse/rdf4j-workbench:latest
```

More information can be found in [dockerhub](https://hub.docker.com/r/eclipse/rdf4j-workbench)

**Endpoint**

The module supports the following endpoints:

| URL | Method | Accepts | Description |
| --- | --- | --- | --- |
| /wpm/RegInput                 | POST      | JSON   | Receives TCN registration info |
| /wpm/handshaking              | POST      | JSON   | Receives handshaking message (i.e., user has logged in) |
| /app/speaktoavatar            | POST      | JSON   | Receives the scenario selection made by user through the mobile app |
| /lid/input                    | POST      | JSON   | Receives a list of language codes along with a percentage score for each language identified in an utterance |
| /app/tcnProfile               | POST,GET  | JSON   | This endpoint can be used to update and retrieve the TCN’s profile |
| /app/tcnProfile/{section}     | GET       | JSON   | This endpoint can be used to retrieve the CV sections |
| /app/avatarConfig             | POST,GET  | JSON   | This endpoint can be used to update and retrieve the avatar configuration settings |
| /app/minigame/{minigame}      | POST,GET  | JSON   | This endpoint can be used to update and retrieve the minigame progress |
| /app/chcPreferences           | POST,GET  | JSON   | This endpoint can be used to update and retrieve the preferences for the Cohabitation scenario |
| /app/AppProgress              | POST,GET  | JSON   | This endpoint can be used to store the progress of the language course coordination scenario |
| /dips/input                   | POST      | Turtle | This endpoint can be used to send a Dialogue Input Packages |
| /chc/notifications            | POST      | JSON   | This endpoint can be used to trigger the Cohabitation scenario |
| /agent/notifications          | POST      | Turtle | This endpoint can be used to send notifications|
| /las/input                    | POST      | JSON   | This endpoint can be used to send the results of the language analysis |
| /nlg/input                    | POST      | Text   | This endpoint can be used to send the results of the natural language generation |
| /dms/input                    | POST      | JSON   | This endpoint can be used to send the results of the dialogue management service |

