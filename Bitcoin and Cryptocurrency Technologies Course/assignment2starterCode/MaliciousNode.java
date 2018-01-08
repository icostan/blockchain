import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class MaliciousNode implements Node {

	Set<Transaction> pendingTransactions;
	
    public MaliciousNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
    }

    public void setFollowees(boolean[] followees) {
        return;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
    		if (Math.random() < 0.5) {
    			return new HashSet<Transaction>();
    		} else {
        	    	return pendingTransactions;
    		}
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        return;
    }
}
