from sentence_transformers import SentenceTransformer, util
import numpy as np
from flask import Flask
from flask import request
from flask import jsonify
from transformers import pipeline

app = Flask(__name__)

@app.route('/kms/api/auxiliary/detection', methods=['POST'])
def new_prediction():
    input = request.get_json()
    candidate_labels = input["corpus"]
    input_text = input["text"]
    top = input["top"]

    cl = classifier(input_text, candidate_labels, multi_label=True)

    topics = []

    # top_k results to return
    top_k=int(top)

    response = ""
    for i in range(top_k):
        if cl['scores'][i] >= threshold:
            temp = cl['labels'][i]
            print(temp)
            response = response + '\n' + temp

    return response

@app.route('/kms/api/auxiliary/similarity', methods=['POST'])
def calcSimilarSlots():
    input = request.get_json()

    ids = input["ids"]
    corpus = input["corpus"]
    sentence = input["text"]
    top = input["top"]

    # encode corpus to get corpus embeddings
    corpus_embeddings = model.encode(corpus, convert_to_tensor=True)

    # encode sentence to get sentence embeddings
    sentence_embedding = model.encode(sentence, convert_to_tensor=True)

    # top_k results to return
    top_k=int(top)

    # compute similarity scores of the sentence with the corpus
    cos_scores = util.pytorch_cos_sim(sentence_embedding, corpus_embeddings)[0]

    # Sort the results in decreasing order and get the first top_k
    top_results = np.argpartition(-cos_scores, range(top_k))[0:top_k]

    print("Sentence:", sentence, "\n")
    print("Top", top_k, "most similar sentences in corpus:")
    response = ""
    for idx in top_results[0:top_k]:
        if cos_scores[idx] > 0.5:
            temp = corpus[idx] + "@Score:%.4f" % (cos_scores[idx]) + "@Index:%s" % ids[idx]
            print(temp)
            response = response + '\n' + temp

    return response
    # return "Hello World!"

@app.route('/kms/api/auxiliary/search', methods=['POST'])
def SemanticSearch():
    input = request.get_json()

    ids = input["ids"]
    corpus = input["corpus"]
    query = input["text"]
    top = input["top"]

    top_k=int(top)

    corpus_embeddings = embedder.encode(corpus, convert_to_tensor=True)
    query_embedding = embedder.encode(query, convert_to_tensor=True)

    hits = util.semantic_search(query_embedding, corpus_embeddings, top_k)
    hits = hits[0]      #Get the hits for the first query
    response = ""
    for hit in hits:
        if hit['score'] > 0.4:
            temp = corpus[hit['corpus_id']] + "@Score:{:.4f}".format(hit['score'])
            print(temp)
            response = response + '\n' + temp

    return response

if __name__ == '__main__':
    model = SentenceTransformer('multi-qa-mpnet-base-dot-v1')
    classifier = pipeline("zero-shot-classification", model="MoritzLaurer/DeBERTa-v3-xsmall-mnli-fever-anli-ling-binary")
    # candidate_labels = ["Reception", "Legal", "Appointment", "Language", "Inclusion", "Family", "Culture", "Job",
    #                     "Health", "Housing", "School", "Cohabitation", "House Search", "Interview", "CV",
    #                     "Driving License",
    #                     "Employment Contract", "Job Search", "Asylum", "Educational System", "Learning Disabilities",
    #                     "School Documents", "Enrollment Form", "School Statement", "Dress Code", "Certificate",
    #                     "Work Experience", "Company", "Εmployer"]
    
    # added new embedder for testing
    embedder = SentenceTransformer('all-MiniLM-L6-v2')

    threshold = 0.1
    app.run(host = "0.0.0.0")