const hre = require("hardhat");
const fs = require("fs");

async function main() {
    console.log("Deploying ClinicPayment contract...");

    const ClinicPayment = await hre.ethers.getContractFactory("ClinicPayment");
    const clinicPayment = await ClinicPayment.deploy();

    await clinicPayment.waitForDeployment();

    const address = await clinicPayment.getAddress();
    console.log(`ClinicPayment deployed to: ${address}`);

    // Save the contract address to a file for frontend use
    const contractInfo = {
        address: address,
        network: hre.network.name,
        deployedAt: new Date().toISOString(),
    };

    fs.writeFileSync(
        "./deployed-contract.json",
        JSON.stringify(contractInfo, null, 2)
    );
    console.log("Contract info saved to deployed-contract.json");
}

main()
    .then(() => process.exit(0))
    .catch((error) => {
        console.error(error);
        process.exit(1);
    });
