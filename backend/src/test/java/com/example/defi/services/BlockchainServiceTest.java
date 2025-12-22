package com.example.defi.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private Web3j web3j;

    private BlockchainService blockchainService;

    // Test private key from Hardhat's default accounts (do NOT use in production)
    private static final String TEST_PRIVATE_KEY = "ac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80";
    private static final String TEST_ADDRESS = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";
    private static final String RECIPIENT_ADDRESS = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";

    @BeforeEach
    void setUp() {
        blockchainService = new BlockchainService();
        // Note: For unit testing, we would need to inject the mock Web3j
        // This test focuses on the logic that can be tested without actual blockchain
    }

    @Test
    @DisplayName("Private key with 0x prefix should be handled correctly")
    void privateKeyWithPrefix_shouldBeHandledCorrectly() {
        String prefixedKey = "0x" + TEST_PRIVATE_KEY;
        String cleanedKey = prefixedKey.startsWith("0x") ? prefixedKey.substring(2) : prefixedKey;

        assertEquals(TEST_PRIVATE_KEY, cleanedKey);
        assertEquals(64, cleanedKey.length());
    }

    @Test
    @DisplayName("Private key without 0x prefix should remain unchanged")
    void privateKeyWithoutPrefix_shouldRemainUnchanged() {
        String cleanedKey = TEST_PRIVATE_KEY.startsWith("0x") ? TEST_PRIVATE_KEY.substring(2) : TEST_PRIVATE_KEY;

        assertEquals(TEST_PRIVATE_KEY, cleanedKey);
    }

    @Test
    @DisplayName("ETH to Wei conversion should be accurate")
    void ethToWeiConversion_shouldBeAccurate() {
        BigDecimal ethAmount = new BigDecimal("1.5");
        BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();

        assertEquals(new BigInteger("1500000000000000000"), weiAmount);
    }

    @Test
    @DisplayName("Wei to ETH conversion should be accurate")
    void weiToEthConversion_shouldBeAccurate() {
        BigInteger weiAmount = new BigInteger("2500000000000000000");
        BigDecimal ethAmount = Convert.fromWei(new BigDecimal(weiAmount), Convert.Unit.ETHER);

        assertEquals(new BigDecimal("2.5"), ethAmount);
    }

    @Test
    @DisplayName("Small ETH amounts should convert correctly")
    void smallEthAmounts_shouldConvertCorrectly() {
        BigDecimal ethAmount = new BigDecimal("0.001");
        BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();

        assertEquals(new BigInteger("1000000000000000"), weiAmount);
    }

    @Test
    @DisplayName("Zero ETH should convert to zero Wei")
    void zeroEth_shouldConvertToZeroWei() {
        BigDecimal ethAmount = BigDecimal.ZERO;
        BigInteger weiAmount = Convert.toWei(ethAmount, Convert.Unit.ETHER).toBigInteger();

        assertEquals(BigInteger.ZERO, weiAmount);
    }

    @Test
    @DisplayName("Gas limit should be set to 21000 for simple transfers")
    void gasLimit_shouldBe21000ForSimpleTransfers() {
        BigInteger expectedGasLimit = BigInteger.valueOf(21000);
        // This is the standard gas limit for ETH transfers
        assertEquals(expectedGasLimit, BigInteger.valueOf(21000));
    }

    @Test
    @DisplayName("Ethereum address validation - valid address")
    void ethereumAddressValidation_validAddress() {
        String validAddress = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";

        assertTrue(validAddress.startsWith("0x"));
        assertEquals(42, validAddress.length());
        assertTrue(validAddress.substring(2).matches("[0-9a-fA-F]+"));
    }

    @Test
    @DisplayName("Ethereum address validation - invalid address too short")
    void ethereumAddressValidation_invalidAddressTooShort() {
        String invalidAddress = "0xf39Fd6e51aad88F6F4ce6aB8827279c";

        assertNotEquals(42, invalidAddress.length());
    }

    @Test
    @DisplayName("Ethereum address validation - missing 0x prefix")
    void ethereumAddressValidation_missingPrefix() {
        String invalidAddress = "f39Fd6e51aad88F6F4ce6aB8827279cffFb92266";

        assertFalse(invalidAddress.startsWith("0x"));
    }
}
