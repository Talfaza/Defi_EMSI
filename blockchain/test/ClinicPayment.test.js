const { expect } = require("chai");
const { ethers } = require("hardhat");

describe("ClinicPayment", function () {
    let clinicPayment;
    let clinic;
    let patient;
    let otherAccount;

    beforeEach(async function () {
        [clinic, patient, otherAccount] = await ethers.getSigners();

        const ClinicPayment = await ethers.getContractFactory("ClinicPayment");
        clinicPayment = await ClinicPayment.deploy();
        await clinicPayment.waitForDeployment();
    });

    describe("Payment Creation", function () {
        it("Should create a payment request", async function () {
            const amount = ethers.parseEther("0.1");
            const description = "Medical consultation";

            await expect(
                clinicPayment.connect(clinic).createPayment(patient.address, amount, description)
            )
                .to.emit(clinicPayment, "PaymentCreated")
                .withArgs(1, clinic.address, patient.address, amount, description);

            const payment = await clinicPayment.getPayment(1);
            expect(payment.clinic).to.equal(clinic.address);
            expect(payment.patient).to.equal(patient.address);
            expect(payment.amount).to.equal(amount);
            expect(payment.status).to.equal(0n); // Pending
        });

        it("Should fail with zero amount", async function () {
            await expect(
                clinicPayment.connect(clinic).createPayment(patient.address, 0, "Test")
            ).to.be.revertedWith("Amount must be greater than 0");
        });

        it("Should fail with zero address", async function () {
            await expect(
                clinicPayment.connect(clinic).createPayment(
                    ethers.ZeroAddress,
                    ethers.parseEther("0.1"),
                    "Test"
                )
            ).to.be.revertedWith("Invalid patient address");
        });
    });

    describe("Payment Execution", function () {
        beforeEach(async function () {
            const amount = ethers.parseEther("0.1");
            await clinicPayment.connect(clinic).createPayment(patient.address, amount, "Test");
        });

        it("Should allow patient to pay", async function () {
            const amount = ethers.parseEther("0.1");
            const clinicBalanceBefore = await ethers.provider.getBalance(clinic.address);

            await clinicPayment.connect(patient).pay(1, { value: amount });

            const clinicBalanceAfter = await ethers.provider.getBalance(clinic.address);
            expect(clinicBalanceAfter - clinicBalanceBefore).to.equal(amount);

            const payment = await clinicPayment.getPayment(1);
            expect(payment.status).to.equal(1n); // Completed
        });

        it("Should fail with incorrect amount", async function () {
            await expect(
                clinicPayment.connect(patient).pay(1, { value: ethers.parseEther("0.05") })
            ).to.be.revertedWith("Incorrect payment amount");
        });

        it("Should fail if not the patient", async function () {
            await expect(
                clinicPayment.connect(otherAccount).pay(1, { value: ethers.parseEther("0.1") })
            ).to.be.revertedWith("Only patient can call this");
        });
    });

    describe("Payment Cancellation", function () {
        beforeEach(async function () {
            await clinicPayment.connect(clinic).createPayment(
                patient.address,
                ethers.parseEther("0.1"),
                "Test"
            );
        });

        it("Should allow clinic to cancel", async function () {
            await expect(clinicPayment.connect(clinic).cancelPayment(1))
                .to.emit(clinicPayment, "PaymentCancelled")
                .withArgs(1);

            const payment = await clinicPayment.getPayment(1);
            expect(payment.status).to.equal(2n); // Cancelled
        });

        it("Should fail if not the clinic", async function () {
            await expect(
                clinicPayment.connect(patient).cancelPayment(1)
            ).to.be.revertedWith("Only clinic can call this");
        });
    });

    describe("Query Functions", function () {
        it("Should return clinic payments", async function () {
            await clinicPayment.connect(clinic).createPayment(
                patient.address,
                ethers.parseEther("0.1"),
                "Test 1"
            );
            await clinicPayment.connect(clinic).createPayment(
                patient.address,
                ethers.parseEther("0.2"),
                "Test 2"
            );

            const payments = await clinicPayment.getClinicPayments(clinic.address);
            expect(payments.length).to.equal(2);
        });

        it("Should return pending payments for patient", async function () {
            await clinicPayment.connect(clinic).createPayment(
                patient.address,
                ethers.parseEther("0.1"),
                "Test 1"
            );
            await clinicPayment.connect(clinic).createPayment(
                patient.address,
                ethers.parseEther("0.2"),
                "Test 2"
            );

            const pending = await clinicPayment.getPendingPayments(patient.address);
            expect(pending.length).to.equal(2);
        });
    });
});
