"""
Flask API for MediDeFi Default Risk Prediction Model
Serves the trained ML model for payment risk assessment
"""

import os
import pickle
import numpy as np
from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

# Load the trained model
MODEL_PATH = os.path.join(os.path.dirname(__file__), 'mediDeFi_final_model.pkl')

model = None

def load_model():
    global model
    if model is None:
        try:
            with open(MODEL_PATH, 'rb') as f:
                model = pickle.load(f)
            print(f"Model loaded successfully from {MODEL_PATH}")
        except Exception as e:
            print(f"Error loading model: {e}")
            raise
    return model

# Feature order expected by the model (based on training data)
EXPECTED_FEATURES = [
    'payment_amount',
    'payment_amount_log',
    'payment_failed_before',
    'payment_hour',
    'payment_weekday',
    'payment_month',
    'patient_total_payments',
    'patient_failed_payments',
    'patient_avg_payment_amount',
    'clinic_default_rate',
    'risk_score'  # Historical risk score, defaults to 0.5 for new patients
]


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'model_loaded': model is not None
    })


@app.route('/predict', methods=['POST'])
def predict():
    """
    Predict payment default risk
    
    Expected JSON body:
    {
        "payment_amount": float,
        "payment_hour": int (0-23),
        "payment_weekday": int (0-6),
        "payment_month": int (1-12),
        "patient_total_payments": int,
        "patient_failed_payments": int,
        "patient_avg_payment_amount": float,
        "clinic_default_rate": float (0-1),
        "payment_failed_before": int (0 or 1)
    }
    
    Returns:
    {
        "risk_score": float (0-1),
        "risk_level": "LOW" | "MEDIUM" | "HIGH",
        "success": true
    }
    """
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({
                'success': False,
                'error': 'No JSON data provided'
            }), 400
        
        # Validate required fields
        required_fields = [
            'payment_amount', 'payment_hour', 'payment_weekday', 'payment_month',
            'patient_total_payments', 'patient_failed_payments', 
            'patient_avg_payment_amount', 'clinic_default_rate', 'payment_failed_before'
        ]
        
        missing_fields = [f for f in required_fields if f not in data]
        if missing_fields:
            return jsonify({
                'success': False,
                'error': f'Missing required fields: {missing_fields}'
            }), 400
        
        # Compute log of payment amount
        payment_amount = float(data['payment_amount'])
        payment_amount_log = np.log(payment_amount) if payment_amount > 0 else 0
        
        # Prepare features in correct order (11 features)
        # Use historical risk_score from data, default to 0.5 for new patients
        historical_risk_score = float(data.get('risk_score', 0.5))
        
        features = np.array([[
            payment_amount,
            payment_amount_log,
            int(data['payment_failed_before']),
            int(data['payment_hour']),
            int(data['payment_weekday']),
            int(data['payment_month']),
            int(data['patient_total_payments']),
            int(data['patient_failed_payments']),
            float(data['patient_avg_payment_amount']),
            float(data['clinic_default_rate']),
            historical_risk_score
        ]])
        
        # Get prediction
        loaded_model = load_model()
        
        # Check if model has predict_proba (for classifiers) or predict (for regressors)
        if hasattr(loaded_model, 'predict_proba'):
            # For classifiers, get probability of default (class 1)
            risk_score = float(loaded_model.predict_proba(features)[0][1])
        else:
            # For regressors, get direct prediction
            risk_score = float(loaded_model.predict(features)[0])
        
        # Clamp to 0-1 range
        risk_score = max(0.0, min(1.0, risk_score))
        
        # Determine risk level
        if risk_score < 0.3:
            risk_level = 'LOW'
        elif risk_score < 0.7:
            risk_level = 'MEDIUM'
        else:
            risk_level = 'HIGH'
        
        return jsonify({
            'success': True,
            'risk_score': round(risk_score, 4),
            'risk_level': risk_level
        })
        
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/model-info', methods=['GET'])
def model_info():
    """Get information about the loaded model"""
    try:
        loaded_model = load_model()
        return jsonify({
            'success': True,
            'model_type': type(loaded_model).__name__,
            'features': EXPECTED_FEATURES
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


if __name__ == '__main__':
    # Load model on startup
    load_model()
    
    # Run Flask app
    port = int(os.environ.get('ML_SERVICE_PORT', 5001))
    print(f"Starting ML service on port {port}")
    app.run(host='0.0.0.0', port=port, debug=True)
