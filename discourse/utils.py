import torch
import models

# Define discourse labels dictionary
DISCOURSE_DICT = {0: 'Contingency', 1: 'Temporal', 2: 'Comparison', 3: 'Expansion'}


def generate_output_logits(text, model):
    # Encode input pair to input_ids
    model_inputs = models.tokenizer(text,
                                    padding='max_length',
                                    add_special_tokens=True,
                                    return_tensors="pt").to(models.device)
    # Generate predictions
    output = model(**model_inputs)
    return output


def predict_label(output):
    # Convert output logits to probs and select the maximum
    probabilities = torch.nn.functional.softmax(output['logits'], dim=-1)
    predicted_label = torch.argmax(probabilities).item()
    return predicted_label


def convert_label(label):
    return DISCOURSE_DICT[label]


def extract_relation(data):
    # Preprocess input
    input_pair = data['utt1'].lower() + ' [SEP] ' + data['utt2'].lower()

    # Generate binary output (0: No Relation, 1: Relation)
    binary_output = generate_output_logits(input_pair, models.binary_model)
    binary_label = predict_label(binary_output)

    if binary_label == 0:
        return "No Relation"

    else:
        # If a relation is detected, then we pass the input pair to the explicit implicit model
        explicit_implicit_output = generate_output_logits(input_pair, models.explicit_implicit_model)
        explicit_label = predict_label(explicit_implicit_output)

        # If explicit label detected, then proceed with the explicit model
        if explicit_label == 1:
            output = generate_output_logits(input_pair, models.explicit_model)

        # If implicit label detected, proceed with the implicit model
        else:
            output = generate_output_logits(input_pair, models.implicit_model)

        # Convert integer value to string from dict
        label = convert_label(predict_label(output))

        return label
