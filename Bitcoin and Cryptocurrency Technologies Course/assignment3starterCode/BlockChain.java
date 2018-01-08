import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

public class BlockChain {
	public static final int CUT_OFF_AGE = 10;

	private int height = 1;
	private BlockNode lastBlockNode;

	private TransactionPool txPool = new TransactionPool();
	private List<BlockNode> blockNodes = new ArrayList<>(CUT_OFF_AGE);

	/**
	 * create an empty block chain with just a genesis block. Assume
	 * {@code genesisBlock} is a valid block
	 */
	public BlockChain(Block genesisBlock) {
		lastBlockNode = new BlockNode(genesisBlock, new UTXOPool(), height);
		blockNodes.add(lastBlockNode);

		addUTXOs(lastBlockNode, genesisBlock.getCoinbase());
	}

	/** Get the maximum height block */
	public Block getMaxHeightBlock() {
		return getMaxHeightBlockNode().block;
	}

	/** Get the UTXOPool for mining a new block on top of max height block */
	public UTXOPool getMaxHeightUTXOPool() {
		return getMaxHeightBlockNode().utxoPool;
	}

	/** Get the transaction pool to mine a new block */
	public TransactionPool getTransactionPool() {
		return txPool;
	}

	/**
	 * Add {@code block} to the block chain if it is valid. For validity, all
	 * transactions should be valid and block should be at
	 * {@code height > (maxHeight - CUT_OFF_AGE)}.
	 * 
	 * <p>
	 * For example, you can try creating a new block over the genesis block (block
	 * height 2) if the block chain height is {@code <=
	 * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot
	 * create a new block at height 2.
	 * 
	 * @return true if block is successfully added
	 */
	public boolean addBlock(Block block) {
		if (isBlockValid(block)) {
			processBlock(block);
			// cleanupBlockNodes();
			return true;
		} else {
			return false;
		}
	}

	boolean isBlockValid(Block block) {
		// missing parent block
		BlockNode parentNode = getParentBlockNode(block);
		if (parentNode == null) {
			return false;
		}

		// reject genesis blocks
		if (block.getPrevBlockHash() == null) {
			return false;
		}

		// check cutoff distance
		if (height - parentNode.height > CUT_OFF_AGE) {
			return false;
		}

		// valid transactions
		TxHandler txHandler = new TxHandler(parentNode.utxoPool);
		for (Transaction transaction : block.getTransactions()) {
			if (!txHandler.isValidTx(transaction)) {
				return false;
			}
		}
		return true;
	}

	void processBlock(Block block) {
		BlockNode parentNode = getParentBlockNode(block);

		// add new block
		BlockNode blockNode = new BlockNode(block, parentNode.utxoPool, parentNode.height + 1);
		blockNodes.add(blockNode);

		// update tx and utxo pools
		for (Transaction tx : block.getTransactions()) {
			txPool.removeTransaction(tx.getHash());

			removeUTXOs(blockNode, tx);
			addUTXOs(blockNode, tx);
		}

		// add utxo for coinbase
		addUTXOs(blockNode, block.getCoinbase());

		// update longest chain
		if (blockNode.height == height + 1) {
			this.height = blockNode.height;
			this.lastBlockNode = blockNode;
		}
	}

	/** Add a transaction to the transaction pool */
	public void addTransaction(Transaction tx) {
		txPool.addTransaction(tx);
		addUTXOs(lastBlockNode, tx);
	}

	private BlockNode getMaxHeightBlockNode() {
		for (BlockNode blockNode : blockNodes) {
			if (blockNode.height == height) {
				return blockNode;
			}
		}
		return null;
	}

	private BlockNode getParentBlockNode(Block block) {
		for (BlockNode blockNode : blockNodes) {
			if (blockNode.block.getHash() == block.getPrevBlockHash()) {
				return blockNode;
			}
		}
		return null;
	}

	private void addUTXOs(BlockNode blockNode, Transaction tx) {
		int index = 0;
		for (Transaction.Output txOut : tx.getOutputs()) {
			UTXO utxo = new UTXO(tx.getHash(), index);
			blockNode.utxoPool.addUTXO(utxo, txOut);
			index++;
		}
	}

	private void removeUTXOs(BlockNode blockNode, Transaction tx) {
		for (Transaction.Input txIn : tx.getInputs()) {
			UTXO utxo = new UTXO(txIn.prevTxHash, txIn.outputIndex);
			blockNode.utxoPool.removeUTXO(utxo);
		}
	}

	private void cleanupBlockNodes() {
		Iterator<BlockNode> iterator = blockNodes.iterator();
		while (iterator.hasNext()) {
			BlockNode blockNode = iterator.next();
			if (blockNode.height < height - CUT_OFF_AGE - 1) {
				iterator.remove();
			}
		}
	}

	private class BlockNode {
		Block block;
		UTXOPool utxoPool;
		int height;

		public BlockNode(Block block, UTXOPool utxoPool, int height) {
			this.block = block;
			this.utxoPool = utxoPool;
			this.height = height;
		}
	}
}