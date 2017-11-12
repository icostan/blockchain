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
	private double[] malicious;

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
		this.malicious = new double[followees.length];
	}

	public void setPendingTransaction(Set<Transaction> pendingTransactions) {
		this.transactions = pendingTransactions;
		this.pool = pendingTransactions;
	}

	public Set<Transaction> sendToFollowers() {
		return transactions;
	}

	public void receiveFromFollowees(Set<Candidate> candidates) {
		for (Candidate candidate : candidates) {
			pool.add(candidate.tx);
		}

		Map<Integer, Set<Transaction>> candidateTransactions = new HashMap<>();
		for (Candidate candidate : candidates) {
			if (!candidateTransactions.containsKey(candidate.sender))
				candidateTransactions.put(candidate.sender, new HashSet<>());

			candidateTransactions.get(candidate.sender).add(candidate.tx);
		}

		for (int sender : candidateTransactions.keySet()) {
			Set<Transaction> transactions = candidateTransactions.get(sender);

			// too less or too many transactions
			// if (transactions.size() < min_count() || transactions.size() > max_count()) {
			// malicious[sender] += round_weight();
			// }

			// dead node - zero transactions
			if (transactions.size() == 0) {
				mark_as_malicious(sender);
			}

			if (!is_malicious(sender)) {
				this.transactions.addAll(transactions);
			}
		}
	}

	private boolean is_malicious(int sender) {
		return malicious[sender] < p_malicious;
	}

	private void mark_as_malicious(int sender) {
		malicious[sender] += round_weight();
	}

	private double round_weight() {
		return numRounds / 100;
	}

	private int distribution_count() {
		return (int) (p_txDistribution * pool.size());
	}

	private int min_count() {
		return (int) (distribution_count() * 0.5);
	}

	private int max_count() {
		return (int) (distribution_count() * 1.5);
	}
}