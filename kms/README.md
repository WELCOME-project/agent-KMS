# agent-KMS
The Knowledge Management Service component (KMS) is part of the Agent and is a central component of the WELCOME architecture. It acts as a middleware that handles storage, processing, and retrieval of local agent knowledge that is available in the Local Agent Knowledge Repository (LAKR). 

It encapsulates the following subcomponents:

* Knowledge Base Population (KBP) which is responsible for (a) translating incoming data from various formats to RDF-based representation, considering the schema of the WELCOME Ontologies, and (b) for populating them into the LAKR

* Dynamic Ontology Extension (DOE) which facilitates the integration of information from an external multilingual semantic network to retrieve information and dynamically extend existing knowledge

* Semantic Reasoning Framework (SRF) which implements a reasoning framework combining native OWL2 reasoning and SPARQL rules to support the semantic service selection performed by the Agent. In addition, SRF facilitates topic detection (Section 5.2.2.2), sentence similarity (Section 5.2.2.2) and discourse relation techniques (Section 6.2) to extract insights and knowledge from textual data.


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
```


## Run 
You can run the application with the following command 
```
$ docker run -d --restart always --name kms-spring kms-spring
$ docker run -d -p 8080:8080 -e JAVA_OPTS="-Xms1g -Xmx4g" \
	-v data:/var/rdf4j -v logs:/usr/local/tomcat/logs eclipse/rdf4j-workbench:latest
```

More information can be found in [dockerhub](https://hub.docker.com/r/eclipse/rdf4j-workbench)


