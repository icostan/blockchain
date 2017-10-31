import java.util.Arrays;
import java.util.Comparator;

public class MaxFeeTxHandler extends TxHandler {

	public MaxFeeTxHandler(UTXOPool utxoPool) {
		super(utxoPool);
	}

	@Override
	protected Transaction[] getSortedTransactions(Transaction[] txs) {
		Arrays.sort(txs, new Comparator<Transaction>() {
			@Override
			public int compare(Transaction tx1, Transaction tx2) {
				double fee1 = fee(tx1);
				double fee2 = fee(tx2);

				if (fee1 < fee2) {
					return 1;
				} else if (fee1 > fee2) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		return txs;
	}
}
