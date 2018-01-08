import static org.junit.Assert.assertFalse;
import static org.junit.Assert.*;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BlockChainTests {
	static PublicKey genesisAddress;
	static PrivateKey genesisSecret;

	Block genesisBlock;
	BlockChain blockChain;
	PublicKey address;
	PrivateKey secret;

	@BeforeAll
	static void setupAll() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			KeyPair kp = kpg.generateKeyPair();
			genesisAddress = kp.getPublic();
			genesisSecret = kp.getPrivate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@BeforeEach
	void setupEach() {
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			KeyPair kp = kpg.generateKeyPair();
			address = kp.getPublic();
			secret = kp.getPrivate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		this.genesisBlock = new Block(null, genesisAddress);
		this.genesisBlock.finalize();
		this.blockChain = new BlockChain(genesisBlock);
	}

	@Test
	@DisplayName("reject genesis block")
	void rejectGenesisBlock() {
		Block block = new Block(null, genesisAddress);
		assertFalse(blockChain.addBlock(block));
	}

	@Test
	@DisplayName("add block if less than cutoff distance")
	void addBlockIfLessThanCutoff() {
		Block block = new Block(genesisBlock.getHash(), genesisAddress);
		assertTrue(blockChain.addBlock(block));
	}

	@Test
	@DisplayName("reject block if greater than cutoff distance")
	void rejectBlockIfGreaterThanCutoff() {
		Block block = genesisBlock;
		for (int i = 0; i < 11; i++) {
			block = new Block(block.getHash(), genesisAddress);
			block.finalize();
			assertTrue(blockChain.addBlock(block));
		}
		block = new Block(genesisBlock.getHash(), genesisAddress);
		assertFalse(blockChain.addBlock(block));
	}
	
	@Test
	@DisplayName("spend coinbase transaction")
	void spendCoinbaseTransaction() {
		Transaction tx = new Transaction();
		tx.addInput(genesisBlock.getCoinbase().getHash(), 0);
		tx.addOutput(10, address);
		tx.addSignature(getSignature(tx, 0, genesisSecret), 0);
		tx.finalize();
		
		Block block = new Block(genesisBlock.getHash(), genesisAddress);
		block.addTransaction(tx);
		block.finalize();
		
		assertTrue(blockChain.addBlock(block));
	}
	
	private byte[] getSignature(Transaction tx, int index, PrivateKey secret) {
		try {
			Signature sign = Signature.getInstance("SHA256withRSA");
			sign.initSign(secret);
			sign.update(tx.getRawDataToSign(index));
			return sign.sign();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return null;
	}
}
