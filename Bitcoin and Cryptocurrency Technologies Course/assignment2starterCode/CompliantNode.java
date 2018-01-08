import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	private double p_graph;
	private double p_malicious;
	private double p_txDistribution;
	private double numRounds;

	private boolean[] followees;
	private double followees_count;

	private int round;
	private double relay_threshold;

	private Set<Transaction> transactions = null; // transactions to broadcast

	public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
		this.p_graph = p_graph;
		this.p_malicious = p_malicious;
		this.p_txDistribution = p_txDistribution;
		this.numRounds = numRounds;
	}

	public void setFollowees(boolean[] followees) {
		this.followees = followees;

		calculate_followees_count(followees);
	}

	public void setPendingTransaction(Set<Transaction> pendingTransactions) {
		this.transactions = pendingTransactions;
	}

	public Set<Transaction> sendToFollowers() {
		return transactions;
	}

	public void receiveFromFollowees(Set<Candidate> candidates) {
		Map<Transaction, Set<Integer>> transactionCandidates = new HashMap<>();
		for (Candidate candidate : candidates) {
			if (!transactionCandidates.containsKey(candidate.tx))
				transactionCandidates.put(candidate.tx, new HashSet<>());

			transactionCandidates.get(candidate.tx).add(candidate.sender);
		}

		calculate_relay_threshold();
		System.out
				.println("Round: " + round + " Followees:" + followees_count + " Relay threshold: " + relay_threshold);

		calculate_transactions(transactionCandidates);
	}

	private void calculate_transactions(Map<Transaction, Set<Integer>> transactionCandidates) {
		this.transactions.clear();

		for (Transaction tx : transactionCandidates.keySet()) {
			Set<Integer> senders = transactionCandidates.get(tx);

			// the many senders pushed this tx the stronger it gets
			if (transaction_ratio(senders.size()) > relay_threshold) {
				this.transactions.add(tx);
			}
		}
	}

	private void calculate_followees_count(boolean[] followees) {
		for (boolean b : followees) {
			if (b)
				this.followees_count += 1;
		}
	}

	private double transaction_ratio(int senders_count) {
		return senders_count / followees_count;
	}

	private double round_weight() {
		return 100.0 / numRounds * 0.01;
	}

	private double followee_weight() {
		return round_weight() / followees_count;
	}

	// relay threshold increases with each round
	private void calculate_relay_threshold() {
		this.round++;

		if (relay_threshold == 0) {
			this.relay_threshold = round_weight();
		} else {
			// TODO: figure out a weight distribution based on exponential function
			this.relay_threshold += round_weight() * (round / 100.0) + followee_weight() * (round / 100);
		}
	}
}