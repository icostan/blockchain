import static org.junit.Assert.*;

import java.security.*;

import org.junit.jupiter.api.*;

public class TxHandlerTests {
	static PublicKey address;
	static PrivateKey secret;

	static Transaction transaction;
	static byte[] signature;

	UTXOPool utxoPool;
	TxHandler txHandler;

	@BeforeAll
	static void setupAll() {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			KeyPair kp = kpg.generateKeyPair();
			address = kp.getPublic();
			secret = kp.getPrivate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@BeforeEach
	void setupEach() {
		utxoPool = new UTXOPool();

		transaction = new Transaction();
		transaction.addInput(transaction.getRawTx(), 0);
		transaction.addOutput(9, address);
		transaction.finalize();

		UTXO utxo = new UTXO(transaction.getHash(), 0);
		utxoPool.addUTXO(utxo, transaction.getOutput(0));

		txHandler = new TxHandler(utxoPool);
	}

	@Test
	@DisplayName("(1) all outputs claimed by {@code tx} are in the current UTXO pool")
	void allOutputsExist() {
		Transaction tx = new Transaction();
		tx.addInput(transaction.getHash(), 0);

		assertTrue(txHandler.allOutputsExist(tx));
	}

	@Test
	@DisplayName("(2) the signatures on each input of {@code tx} are valid")
	void validSignatures() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Transaction tx = new Transaction();
		tx.addInput(transaction.getHash(), 0);

		tx.addSignature(getSignature(tx, 0), 0); // good signature
		assertTrue(txHandler.isValidTx(tx));

		tx.addSignature(getSignature(transaction, 0), 0); // wrong signature
		assertFalse(txHandler.validSignatures(tx));
	}

	@Test
	@DisplayName("(3) no UTXO is claimed multiple times by {@code tx}")
	void noDoubleSpending() {
		Transaction tx = new Transaction();
		tx.addInput(transaction.getHash(), 0);
		tx.addInput(transaction.getHash(), 0);

		assertFalse(txHandler.noDoubleSpending(tx));
	}

	@Test
	@DisplayName("(4) all of {@code tx}s output values are non-negative")
	void nonNegativeOutputs() {
		Transaction tx = new Transaction();
		tx.addInput(transaction.getHash(), 0);

		tx.addOutput(1, address);
		assertTrue(txHandler.nonNegativeOutputs(tx));

		tx.addOutput(-1, address);
		assertFalse(txHandler.nonNegativeOutputs(tx));
	}

	@Test
	@DisplayName("(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values")
	void noOverSpending() {
		Transaction tx = new Transaction();
		tx.addInput(transaction.getHash(), 0);

		tx.addOutput(8, address);
		assertTrue(txHandler.noOverSpending(tx));

		tx.addOutput(1, address);
		assertTrue(txHandler.noOverSpending(tx));

		tx.addOutput(1, address);
		assertFalse(txHandler.noOverSpending(tx));
	}

	@Test
	void handleTxs() {
		Transaction tx = new Transaction();
		tx.addInput(transaction.getHash(), 0);
		tx.addOutput(1, address);
		tx.addSignature(getSignature(tx, 0), 0);

		Transaction[] transactions = { tx };
		Transaction[] results = txHandler.handleTxs(transactions);
		assertArrayEquals(transactions, results);
	}

	private static byte[] getSignature(Transaction tx, int index) {
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
