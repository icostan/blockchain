import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {

	UTXOPool utxoPool;

	/**
	 * Creates a public ledger whose current UTXOPool (collection of unspent
	 * transaction outputs) is {@code utxoPool}. This should make a copy of utxoPool
	 * by using the UTXOPool(UTXOPool uPool) constructor.
	 */
	public TxHandler(UTXOPool utxoPool) {
		this.utxoPool = new UTXOPool(utxoPool);
	}

	public UTXOPool getUTXOPool() {
		return utxoPool;
	}
	
	/**
	 * @return true if: (1) all outputs claimed by {@code tx} are in the current
	 *         UTXO pool, (2) the signatures on each input of {@code tx} are valid,
	 *         (3) no UTXO is claimed multiple times by {@code tx}, (4) all of
	 *         {@code tx}s output values are non-negative, and (5) the sum of
	 *         {@code tx}s input values is greater than or equal to the sum of its
	 *         output values; and false otherwise.
	 */
	public boolean isValidTx(Transaction tx) {
		return allOutputsExist(tx) && validSignatures(tx) && noDoubleSpending(tx) && nonNegativeOutputs(tx)
				&& noOverSpending(tx);
	}

	/**
	 * Handles each epoch by receiving an unordered array of proposed transactions,
	 * checking each transaction for correctness, returning a mutually valid array
	 * of accepted transactions, and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] txs) {
		List<Transaction> transactions = new ArrayList<Transaction>();
		for (Transaction tx : getSortedTransactions(txs)) {
			if (isValidTx(tx)) {
				for (Transaction.Input txIn : tx.getInputs()) {
					UTXO utxo = new UTXO(txIn.prevTxHash, txIn.outputIndex);
					utxoPool.removeUTXO(utxo);
				}
				tx.finalize();
				transactions.add(tx);
			}
		}
		return transactions.toArray(new Transaction[0]);
	}

	/**
	 * Sort transactions by different algorithms in derived class.
	 * 
	 * @param txs
	 * @return
	 */
	protected Transaction[] getSortedTransactions(Transaction[] txs) {
		return txs;
	}

	// 1
	boolean allOutputsExist(Transaction tx) {
		for (Transaction.Input txIn : tx.getInputs()) {
			UTXO utxo = new UTXO(txIn.prevTxHash, txIn.outputIndex);
			if (!utxoPool.contains(utxo))
				return false;
		}
		return true;
	}

	// 2
	boolean validSignatures(Transaction tx) {
		for (int i = 0; i < tx.numInputs(); i++) {
			Transaction.Input txIn = tx.getInput(i);
			UTXO utxo = new UTXO(txIn.prevTxHash, txIn.outputIndex);
			Transaction.Output txOut = utxoPool.getTxOutput(utxo);
			if (!Crypto.verifySignature(txOut.address, tx.getRawDataToSign(i), txIn.signature))
				return false;
		}
		return true;
	}

	// 3
	boolean noDoubleSpending(Transaction tx) {
		Set<UTXO> utxos = new HashSet<UTXO>();
		for (Transaction.Input txIn : tx.getInputs()) {
			UTXO utxo = new UTXO(txIn.prevTxHash, txIn.outputIndex);
			if (utxos.contains(utxo)) {
				return false;
			} else {
				utxos.add(utxo);
			}
		}
		return true;
	}

	// 4
	boolean nonNegativeOutputs(Transaction tx) {
		for (Transaction.Output txOut : tx.getOutputs()) {
			if (txOut.value < 0) {
				return false;
			}
		}
		return true;
	}

	// 5
	boolean noOverSpending(Transaction tx) {
		if (inputValues(tx) < outputValues(tx))
			return false;
		return true;
	}

	private double outputValues(Transaction tx) {
		double outputValues = 0;
		for (Transaction.Output txOut : tx.getOutputs()) {
			outputValues += txOut.value;
		}
		return outputValues;
	}

	protected double inputValues(Transaction tx) {
		double inputValues = 0;
		for (Transaction.Input txIn : tx.getInputs()) {
			UTXO utxo = new UTXO(txIn.prevTxHash, txIn.outputIndex);
			inputValues += utxoPool.getTxOutput(utxo).value;
		}
		return inputValues;
	}

	protected double fee(Transaction tx) {
		return inputValues(tx) - outputValues(tx);
	}
}
