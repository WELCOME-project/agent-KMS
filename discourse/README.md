# Discourse Relation Tool
The discourse relation tool can be used for identifying 
relations between two adjacent utterances of a user. More specifically, it can recognize 
four major types of discourse relations (temporal, contingency, comparison, and expansion) as well as 
the absense of a relation between the utterances. 

## Requirements
`Python>=3.10`  
`Flask==2.1.3`  
`torch==1.12.0`  
`transformers==4.20.1`  

## Installation 
To use the discourse relation tool, you need to clone the repository locally and 
install the necessary library dependencies from requirements.txt as follows:
```
$ cd discourse_relation_tool
$ pip3 install -r requirements.txt
```

## Run 
You can run the application with the following command 
```
python3 -m flask run 
```

The module receives a POST request in a json format as follows:

```json
{
    "input": {
        "utt1": "I wake up at 8:00 am.",
        "utt2": "After I wake up I have breakfast."
    }
}
```

and return the following response:

```json
{
    "input": {
        "utt1": "I wake up at 8:00 am.",
        "utt2": "After I wake up I have breakfast."
    },
    
    "output": Temporal
}
```

Note: In order to run the discourse relation tool, you will also need to download and place the models into the models folder. 
If you need access to the models, please send an email to: stefanos@iti.gr, mavrathan@iti.gr, ntdimos@iti.gr



