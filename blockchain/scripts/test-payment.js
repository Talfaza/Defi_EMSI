const hre = require("hardhat");

async function main() {
    // Get signers - first is clinic, second is patient
    const [clinic, patient] = await hre.ethers.getSigners();

    console.log("\n=== Payment Flow Test ===\n");
    console.log("Clinic address:", clinic.address);
    console.log("Patient address:", patient.address);

    // Get contract at deployed address
    const contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3";
    const ClinicPayment = await hre.ethers.getContractFactory("ClinicPayment");
    const contract = ClinicPayment.attach(contractAddress);

    // Check initial balances
    const clinicBalanceBefore = await hre.ethers.provider.getBalance(clinic.address);
    const patientBalanceBefore = await hre.ethers.provider.getBalance(patient.address);

    console.log("\n--- Initial Balances ---");
    console.log("Clinic:", hre.ethers.formatEther(clinicBalanceBefore), "ETH");
    console.log("Patient:", hre.ethers.formatEther(patientBalanceBefore), "ETH");

    // Step 1: Clinic creates a payment request
    const paymentAmount = hre.ethers.parseEther("0.5"); // 0.5 ETH
    console.log("\n--- Step 1: Clinic Creates Payment Request ---");
    console.log("Amount:", hre.ethers.formatEther(paymentAmount), "ETH");
    console.log("Description: Medical consultation fee");

    const tx1 = await contract.connect(clinic).createPayment(
        patient.address,
        paymentAmount,
        "Medical consultation fee"
    );
    await tx1.wait();

    const paymentId = 1;
    const payment = await contract.getPayment(paymentId);
    console.log("✅ Payment created! ID:", paymentId.toString());
    console.log("   Status:", payment.status === 0n ? "PENDING" : "OTHER");

    // Check pending payments for patient
    console.log("\n--- Step 2: Patient Views Pending Payments ---");
    const pendingPayments = await contract.getPendingPayments(patient.address);
    console.log("Patient has", pendingPayments.length, "pending payment(s)");

    for (const p of pendingPayments) {
        console.log(`  - Payment ID: ${p.id}, Amount: ${hre.ethers.formatEther(p.amount)} ETH`);
        console.log(`    Description: ${p.description}`);
        console.log(`    From Clinic: ${p.clinic}`);
    }

    // Step 3: Patient pays
    console.log("\n--- Step 3: Patient Pays ---");
    const tx2 = await contract.connect(patient).pay(paymentId, { value: paymentAmount });
    await tx2.wait();
    console.log("✅ Payment completed!");

    // Check final balances
    const clinicBalanceAfter = await hre.ethers.provider.getBalance(clinic.address);
    const patientBalanceAfter = await hre.ethers.provider.getBalance(patient.address);

    console.log("\n--- Final Balances ---");
    console.log("Clinic:", hre.ethers.formatEther(clinicBalanceAfter), "ETH");
    console.log("Patient:", hre.ethers.formatEther(patientBalanceAfter), "ETH");

    const clinicGain = clinicBalanceAfter - clinicBalanceBefore;
    const patientSpent = patientBalanceBefore - patientBalanceAfter;

    console.log("\n--- Balance Changes ---");
    console.log("Clinic received:", hre.ethers.formatEther(clinicGain), "ETH");
    console.log("Patient spent:", hre.ethers.formatEther(patientSpent), "ETH (includes gas)");

    // Verify payment status
    const finalPayment = await contract.getPayment(paymentId);
    console.log("\n--- Payment Status ---");
    console.log("Status:", finalPayment.status === 1n ? "✅ COMPLETED" : "❌ NOT COMPLETED");

    console.log("\n=== Test Complete ===\n");
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error(error);
        process.exit(1);
    });
