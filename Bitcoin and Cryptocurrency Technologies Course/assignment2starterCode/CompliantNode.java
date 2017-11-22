import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

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

	private Set<Transaction> pool = null; // all received transactions
	private Set<Transaction> transactions = null; // pending transactions

	public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
		this.p_graph = p_graph;
		this.p_malicious = p_malicious;
		this.p_txDistribution = p_txDistribution;
		this.numRounds = numRounds;
	}

	public void setFollowees(boolean[] followees) {
		this.followees = followees;
		for (boolean b : followees) {
			if (b)
				this.followees_count += 1;
		}
		System.out.println("Node follows " + followees_count);
	}

	public void setPendingTransaction(Set<Transaction> pendingTransactions) {
		this.transactions = pendingTransactions;
		this.pool = pendingTransactions;
	}

	public Set<Transaction> sendToFollowers() {
		return transactions;
	}

	public void receiveFromFollowees(Set<Candidate> candidates) {
		calculate_relay_threshold();

		for (Candidate candidate : candidates) {
			pool.add(candidate.tx);
		}

		Map<Transaction, Set<Integer>> transactionCandidates = new HashMap<>();
		for (Candidate candidate : candidates) {
			if (!transactionCandidates.containsKey(candidate.tx))
				transactionCandidates.put(candidate.tx, new HashSet<>());

			transactionCandidates.get(candidate.tx).add(candidate.sender);
		}

		this.transactions.clear();

		for (Transaction tx : transactionCandidates.keySet()) {
			Set<Integer> senders = transactionCandidates.get(tx);

			if (transaction_ratio(senders.size()) > relay_threshold) {
				this.transactions.add(tx);
			}
		}
	}

	private double transaction_ratio(int senders_count) {
		return senders_count / followees_count;
	}

	private double round_weight() {
		return numRounds / 100.0;
	}

	private void calculate_relay_threshold() {
		this.round++;
		this.relay_threshold = round_weight();

		System.out.println("Round: " + round + " relay threshold: " + relay_threshold);
	}
}