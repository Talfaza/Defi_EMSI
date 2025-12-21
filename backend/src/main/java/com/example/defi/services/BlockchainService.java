package com.example.defi.services;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class BlockchainService {

    private static final String RPC_URL = "http://localhost:8545";
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(21000);
    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L); // 20 Gwei

    private Web3j web3j;

    @PostConstruct
    public void init() {
        web3j = Web3j.build(new HttpService(RPC_URL));
        System.out.println("BlockchainService connected to " + RPC_URL);
    }

    @PreDestroy
    public void shutdown() {
        if (web3j != null) {
            web3j.shutdown();
        }
    }

    /**
     * Transfer ETH from one wallet to another
     * 
     * @param privateKey The sender's private key (without 0x prefix)
     * @param toAddress  The recipient's address
     * @param amountEth  Amount in ETH
     * @return Transaction hash
     */
    public String transferEth(String privateKey, String toAddress, BigDecimal amountEth) throws Exception {
        // Clean up private key (remove 0x prefix if present)
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }

        Credentials credentials = Credentials.create(privateKey);
        String fromAddress = credentials.getAddress();

        // Get nonce
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                fromAddress, DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        // Convert ETH to Wei
        BigInteger value = Convert.toWei(amountEth, Convert.Unit.ETHER).toBigInteger();

        // Create transaction
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                GAS_PRICE,
                GAS_LIMIT,
                toAddress,
                value);

        // Sign transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        // Send transaction
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction.hasError()) {
            throw new Exception("Transaction failed: " + ethSendTransaction.getError().getMessage());
        }

        String transactionHash = ethSendTransaction.getTransactionHash();
        System.out.println("Transaction sent: " + transactionHash);
        System.out.println("From: " + fromAddress + " To: " + toAddress + " Amount: " + amountEth + " ETH");

        return transactionHash;
    }

    /**
     * Get ETH balance for an address
     */
    public BigDecimal getBalance(String address) throws Exception {
        BigInteger balanceWei = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send().getBalance();
        return Convert.fromWei(new BigDecimal(balanceWei), Convert.Unit.ETHER);
    }
}
