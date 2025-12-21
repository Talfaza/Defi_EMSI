// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

/**
 * @title ClinicPayment
 * @dev Simple ETH payment contract for clinic-patient transactions
 */
contract ClinicPayment {
    
    // Events
    event PaymentCreated(
        uint256 indexed paymentId,
        address indexed clinic,
        address indexed patient,
        uint256 amount,
        string description
    );
    
    event PaymentCompleted(
        uint256 indexed paymentId,
        address indexed patient,
        uint256 amount,
        uint256 timestamp
    );
    
    event PaymentCancelled(
        uint256 indexed paymentId
    );

    // Payment status
    enum PaymentStatus { Pending, Completed, Cancelled }

    // Payment structure
    struct Payment {
        uint256 id;
        address payable clinic;
        address patient;
        uint256 amount;
        string description;
        PaymentStatus status;
        uint256 createdAt;
        uint256 completedAt;
    }

    // State variables
    uint256 private paymentCounter;
    mapping(uint256 => Payment) public payments;
    mapping(address => uint256[]) public clinicPayments;
    mapping(address => uint256[]) public patientPayments;

    // Modifiers
    modifier onlyClinic(uint256 _paymentId) {
        require(payments[_paymentId].clinic == msg.sender, "Only clinic can call this");
        _;
    }

    modifier onlyPatient(uint256 _paymentId) {
        require(payments[_paymentId].patient == msg.sender, "Only patient can call this");
        _;
    }

    modifier paymentExists(uint256 _paymentId) {
        require(payments[_paymentId].id != 0, "Payment does not exist");
        _;
    }

    modifier isPending(uint256 _paymentId) {
        require(payments[_paymentId].status == PaymentStatus.Pending, "Payment is not pending");
        _;
    }

    /**
     * @dev Create a new payment request
     * @param _patient Address of the patient
     * @param _amount Amount in wei
     * @param _description Description of the payment
     */
    function createPayment(
        address _patient,
        uint256 _amount,
        string memory _description
    ) external returns (uint256) {
        require(_patient != address(0), "Invalid patient address");
        require(_amount > 0, "Amount must be greater than 0");

        paymentCounter++;
        
        Payment storage newPayment = payments[paymentCounter];
        newPayment.id = paymentCounter;
        newPayment.clinic = payable(msg.sender);
        newPayment.patient = _patient;
        newPayment.amount = _amount;
        newPayment.description = _description;
        newPayment.status = PaymentStatus.Pending;
        newPayment.createdAt = block.timestamp;

        clinicPayments[msg.sender].push(paymentCounter);
        patientPayments[_patient].push(paymentCounter);

        emit PaymentCreated(
            paymentCounter,
            msg.sender,
            _patient,
            _amount,
            _description
        );

        return paymentCounter;
    }

    /**
     * @dev Pay a pending payment (patient only)
     * @param _paymentId ID of the payment
     */
    function pay(uint256 _paymentId) 
        external 
        payable 
        paymentExists(_paymentId) 
        onlyPatient(_paymentId)
        isPending(_paymentId)
    {
        Payment storage payment = payments[_paymentId];
        require(msg.value == payment.amount, "Incorrect payment amount");

        payment.status = PaymentStatus.Completed;
        payment.completedAt = block.timestamp;

        // Transfer ETH to clinic
        payment.clinic.transfer(msg.value);

        emit PaymentCompleted(
            _paymentId,
            msg.sender,
            msg.value,
            block.timestamp
        );
    }

    /**
     * @dev Cancel a pending payment (clinic only)
     * @param _paymentId ID of the payment
     */
    function cancelPayment(uint256 _paymentId)
        external
        paymentExists(_paymentId)
        onlyClinic(_paymentId)
        isPending(_paymentId)
    {
        payments[_paymentId].status = PaymentStatus.Cancelled;
        
        emit PaymentCancelled(_paymentId);
    }

    /**
     * @dev Get payment details
     * @param _paymentId ID of the payment
     */
    function getPayment(uint256 _paymentId) 
        external 
        view 
        paymentExists(_paymentId)
        returns (Payment memory) 
    {
        return payments[_paymentId];
    }

    /**
     * @dev Get all payment IDs for a clinic
     * @param _clinic Address of the clinic
     */
    function getClinicPayments(address _clinic) external view returns (uint256[] memory) {
        return clinicPayments[_clinic];
    }

    /**
     * @dev Get all payment IDs for a patient
     * @param _patient Address of the patient
     */
    function getPatientPayments(address _patient) external view returns (uint256[] memory) {
        return patientPayments[_patient];
    }

    /**
     * @dev Get pending payments for a patient
     * @param _patient Address of the patient
     */
    function getPendingPayments(address _patient) external view returns (Payment[] memory) {
        uint256[] memory paymentIds = patientPayments[_patient];
        uint256 pendingCount = 0;
        
        // Count pending payments
        for (uint256 i = 0; i < paymentIds.length; i++) {
            if (payments[paymentIds[i]].status == PaymentStatus.Pending) {
                pendingCount++;
            }
        }
        
        // Create array of pending payments
        Payment[] memory pendingPayments = new Payment[](pendingCount);
        uint256 index = 0;
        
        for (uint256 i = 0; i < paymentIds.length; i++) {
            if (payments[paymentIds[i]].status == PaymentStatus.Pending) {
                pendingPayments[index] = payments[paymentIds[i]];
                index++;
            }
        }
        
        return pendingPayments;
    }

    /**
     * @dev Get total payments count
     */
    function getTotalPayments() external view returns (uint256) {
        return paymentCounter;
    }
}
