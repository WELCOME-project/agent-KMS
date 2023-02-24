from transformers import AutoModelForSequenceClassification, AutoTokenizer
import torch

# Set device
if torch.cuda.is_available():
    device = torch.device('cuda')
else:
    device = torch.device('cpu')

# Define local directory that stores all the models of the pipeline
models_path = "./models"

# Load tokenizer
tokenizer = AutoTokenizer.from_pretrained('cross-encoder/nli-roberta-base')
tokenizer.add_tokens(["[SEP]"])

# Initialize and load all the models
binary_model = AutoModelForSequenceClassification.from_pretrained(models_path + '/binary/', num_labels=2,
                                                                  ignore_mismatched_sizes=True).to(device)

explicit_implicit_model = AutoModelForSequenceClassification.from_pretrained(models_path + '/explicit_implicit/',
                                                                             num_labels=2,
                                                                             ignore_mismatched_sizes=True).to(device)

explicit_model = AutoModelForSequenceClassification.from_pretrained(models_path + '/explicit/', num_labels=4,
                                                                    ignore_mismatched_sizes=True).to(device)

implicit_model = AutoModelForSequenceClassification.from_pretrained(models_path + '/implicit/', num_labels=4,
                                                                    ignore_mismatched_sizes=True).to(device)

# Resize tokenizers
binary_model.resize_token_embeddings(len(tokenizer))
explicit_implicit_model.resize_token_embeddings(len(tokenizer))
explicit_model.resize_token_embeddings(len(tokenizer))
implicit_model.resize_token_embeddings(len(tokenizer))


