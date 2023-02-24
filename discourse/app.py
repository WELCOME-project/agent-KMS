from flask import Flask, request, jsonify
from utils import extract_relation

# Initialize Flask server
app = Flask(__name__)


# Flash routes
@app.route("/", methods=["GET", "POST"])
def index():
    if request.method == "POST":
        data = request.get_json()

        if data is None:
            return jsonify({"Error": "Empty request."})

        try:
            output = extract_relation(data['input'])

            return jsonify({"input": data['input'],
                            "output": output})

        except Exception as exception:
            return jsonify({"Error": str(exception)})

    return "Discourse Relation API"


if __name__ == "__main__":
    app.run()
