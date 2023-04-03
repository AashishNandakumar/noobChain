import org.jetbrains.annotations.NotNull;

import java.security.*;
import java.util.ArrayList;

public class Transaction {

    public String transactionId; // Contains a hash of transaction
    public PublicKey sender; // Senders address/public key.
    public PublicKey reciepient; // Recipients address/public key.
    public float value; // Contains the amount we wish to send to the recipient.
    public byte[] signature; // This is to prevent anybody else from spending funds in our wallet.

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0; // A rough count of how many transactions have been generated

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, float value,  ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.reciepient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public boolean processTransaction() {
        /* FUNCTION BREAKDOWN:
            The method first verifies the signature of the transaction using the verifySignature() method. This ensures
            that the transaction was indeed signed by the owner of the sender public key.

            It then checks if each input transaction is valid and unspent by checking if the output is in the list of
            unspent transactions (UTXOs).

            If the total value of the input transactions is less than the value being sent (value), then the transaction
            is considered invalid and the method returns false.

            If the transaction is valid, it generates two outputs: one for the recipient and one for the sender
            (as change). These outputs are added to the outputs list.

            The transaction ID is then calculated based on the hash of the input data, and each output is associated
            with the transaction ID.

            Finally, the outputs are added to the list of unspent transactions (UTXOs) and the spent inputs are removed
            from the list of unspent transactions.

            Overall, the processTransaction() method plays a critical role in validating and processing new transactions
            within the blockchain network.
         */

        if(!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        // Gathers transaction inputs (Making sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = NoobChain.UTXOs.get(i.transactionOutputId);
        }

        // Checks if transaction is valid:
        if(getInputsValue() < NoobChain.minimumTransaction) {
            System.out.println("Transaction Inputs too small: " + getInputsValue());
            System.out.println("Please enter the amount greater than " + NoobChain.minimumTransaction);
            return false;
        }

        // Generate transaction outputs:
        float leftOver = getInputsValue() - value; // get value of inputs then the leftover change:
        transactionId = calulateHash();
        outputs.add(new TransactionOutput( this.reciepient, value, transactionId)); // send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver, transactionId)); // send the left over 'change' back to sender

        // Add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            NoobChain.UTXOs.put(o.id , o);
        }

        // Remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; // if Transaction can't be found skip it
            NoobChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    public float getInputsValue() {
        /* FUNCTION BREAKDOWN:
            This function calculates the total value of all inputs in a transaction by iterating through the list of
            inputs and adding up their corresponding values:

            Declare and initialize a total variable to 0.

            Iterate through the list of inputs using a for loop, with the loop variable I'm representing each input in turn.

            For each input, check if the corresponding unspent transaction output (UTXO) is null using an if statement.
            If it is null, skip the input and move on to the next one.

            If the UTXO is not null, add its value to the total.

            After all inputs have been processed, return the total value.

            Overall, this function is used to calculate the total value of inputs in a transaction, which is necessary
            for verifying the validity of the transaction and generating the change output.

         */
        float total = 0;

        for(TransactionInput i : inputs) {

            if(i.UTXO == null) continue; // if Transaction can't be found skip it, This behavior may not be optimal.

            total += i.UTXO.value;
        }

        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        /* FUNCTION BREAKDOWN:
            This function generates a digital signature for a transaction by using the sender's private key to sign the
            transaction data. Here are the steps it performs:

            Concatenate the sender's public key, recipient's public key, and transaction value to form the data string
            that will be signed.

            Use the StringUtil.applyECDSASig method to apply an ECDSA signature to the data using the sender's private
            key.

            Assign the resulting signature to the signature field of the Transaction object.

         */
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;

        signature = StringUtil.applyECDSASig(privateKey,data);
    }

    public boolean verifySignature() {
        /* FUNCTION GENERATOR:
            This function is used to verify the digital signature of a transaction. Here are the steps involved:

            Concatenate the sender's public key, the recipient's public key, and the value of the transaction as a string.

            Use the StringUtil.verifyECDSASig() method to verify the signature by passing in the sender's public key,
            the concatenated string from step 1, and the signature itself.

            If the signature is valid, the method returns true, indicating that the transaction is valid. Otherwise,
            it returns false.

         */
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Float.toString(value)	;

        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public float getOutputsValue() {
        /* FUNCTION BREAKDOWN:
            Initialize a variable total to 0.

            For each TransactionOutput in the outputs list:
            Add the value of the output to the total.

            Return the final total value.

            So, in summary, this function calculates the total value of all the transaction outputs in the outputs list
            by iterating over them and adding up their values.

         */
        float total = 0;

        for(TransactionOutput o : outputs) {

            total += o.value;
        }

        return total;
    }

    private @NotNull String calulateHash() {
        /* FUNCTION BREAKDOWN:
            This is a private method that is used to calculate the hash of the transaction. Here are the steps it takes:

            Increment the sequence number to avoid two identical transactions having the same hash.

            Concatenate the sender's public key, the recipient's public key, the transaction value, and the sequence
            number into a single string.

            Apply the SHA-256 hashing algorithm to the concatenated string to get the hash value.

            Return the hash value as a string.

         */
        sequence++; //increase the sequence to avoid 2 identical transactions having the same hash

        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(reciepient) +
                        Float.toString(value) + sequence
        );
    }
}
