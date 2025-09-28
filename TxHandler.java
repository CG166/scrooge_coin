import java.util.ArrayList;


public class TxHandler {

	/* Creates a public ledger whose current UTXOPool (collection of unspent 
	 * transaction outputs) is utxoPool. This should make a defensive copy of 
	 * utxoPool by using the UTXOPool(UTXOPool uPool) constructor.
	 */

	private UTXOPool dcUTXOPool;

	public TxHandler(UTXOPool utxoPool) {
		// IMPLEMENT THIS
		dcUTXOPool = new UTXOPool(utxoPool);
	}

	/* Returns true if 
	 * (1) all outputs claimed by tx are in the current UTXO pool,  X
	 * (2) the signatures on each input of tx are valid,            X
	 * (3) no UTXO is claimed multiple times by tx,                 X
	 * (4) all of tx’s output values are non-negative, and
	 * (5) the sum of tx’s input values is greater than or equal to the sum of   
	        its output values;
	   and false otherwise.
	 */

	public boolean isValidTx(Transaction tx) {
		// IMPLEMENT THIS
		ArrayList<UTXO> claimedUTXOs = new ArrayList<>();//ArrayList to keep track of all claimed UXTOs
		double sumOfOutputValues = 0;
		double sumOfInputValues = 0;
		
		for (int i = 0; i < tx.numInputs() ; i++) {
			//Retrieving Input object at index
			Transaction.Input input = tx.getInput(i);
			//Creating UTXO from input data
			byte[] txHash = input.prevTxHash;
			int index = input.outputIndex;
			UTXO tempUtxo = new UTXO(txHash, index);

			//**Check to see if UTXO specified by the input exist in our UTXOPool**
			if(!this.dcUTXOPool.contains(tempUtxo)) {
				return false; //If the UTXO does not exist in our UTXOPool the transaction is declared invalid/false
			}

			//Grab the output
			Transaction.Output output = dcUTXOPool.getTxOutput(tempUtxo);

			//**Check to see if the signiture of the input at this index is valid**
			byte[] message = tx.getRawDataToSign(i);
			byte[] signature = input.signature;
			if(!output.address.verifySignature(message, signature)){
				return false; //If it fails then the transaction is declared invalid/false
			}

			//**Check if no UTXO is claimed multiple times by tx**
			for (int j = 0; j < claimedUTXOs.size() ; j++) {
				if(tempUtxo.equals(claimedUTXOs.get(j))) {
					return false; //If UTXO was previously claimed the transaction is declared invalid
				}
			}
			claimedUTXOs.add(tempUtxo);

		}

		for (int i = 0; i < tx.numOutputs() ; i++) {
			Transaction.Output output = tx.getOutput(i);
			double outputValue = output.value;
			if(outputValue < 0) {
				return false; //If a tx’s output value is negative then the transaction is declared invalid.
			}
			sumOfOutputValues += outputValue;
		}

		for (int i = 0; i < claimedUTXOs.size() ; i++) {
			Transaction.Output iOutput = dcUTXOPool.getTxOutput(claimedUTXOs.get(i));
			double inputValue = iOutput.value;
			sumOfInputValues += inputValue;
		}

		if(sumOfInputValues < sumOfOutputValues) {
			return false;
		}

		return true;
	}

	/* Handles each epoch by receiving an unordered array of proposed 
	 * transactions, checking each transaction for correctness, 
	 * returning a mutually valid array of accepted transactions, 
	 * and updating the current UTXO pool as appropriate.
	 */
	public Transaction[] handleTxs(Transaction[] possibleTxs) {
		// IMPLEMENT THIS
		Transaction[] dcTransactions = possibleTxs;
		ArrayList<Transaction> acceptedTransactions = new ArrayList<>();

		for (int i = 0; i < possibleTxs.length ; i++) {
			Transaction tempTransaction = dcTransactions[i];
			if(!isValidTx(tempTransaction)) {
				//acceptedTransactions.add()
				continue;
			}
			acceptedTransactions.add(tempTransaction);
			//Remove all the UTXOs spent in the transaction from the UTXO pool
			for (int j = 0; j < tempTransaction.numInputs() ; j++) {
				//Retrieving Input object at index
				Transaction.Input input = tempTransaction.getInput(j);
				//Grab the UTXO
				byte[] txHash = input.prevTxHash;
				int index = input.outputIndex;
				UTXO utxo = new UTXO(txHash, index);
				//Remove UTXO from UTXO pool
				dcUTXOPool.removeUTXO(utxo);
			}

			for (int j = 0; j < tempTransaction.numOutputs() ; j++) {
				Transaction.Output output = tempTransaction.getOutput(j); 
				UTXO tempUTXO = new UTXO(tempTransaction.getHash(), j);
				dcUTXOPool.addUTXO(tempUTXO, output);	
			}
		}
		//Convert arraylist of accepted transactions into array
		Transaction[] rAcceptedTransactions = new Transaction[acceptedTransactions.size()];
		for (int i = 0; i < acceptedTransactions.size() ; i++) {
			rAcceptedTransactions[i] = acceptedTransactions.get(i);
		}

		return rAcceptedTransactions;
	}

} 
