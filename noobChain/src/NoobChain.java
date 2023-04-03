import org.jetbrains.annotations.NotNull;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class NoobChain {

    // To store chain of blocks:
    public static ArrayList<Block> blockchain = new ArrayList<>();

    /*  WHAT ARE UTXO'S ?
    --> In the context of blockchain technology, UTXOs (Unspent Transaction Outputs) are the unspent transaction
        outputs that are available for use in new transactions. Each UTXO represents a certain amount of cryptocurrency
        that has been transferred to an address, but has not yet been spent or transferred elsewhere.

        The UTXOs HashMap is typically used to keep track of the unspent transaction outputs in a blockchain system.
        Each entry in the HashMap represents a UTXO, where the key is the unique identifier of the transaction output,
        and the value is the transaction output itself.
     */
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<>();

    public static int difficulty = 6;
    public static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;


    public static void main(String[] args) {

        /* Bouncy Castle:
            The Bouncy Castle provider is a popular third-party security provider for Java that provides a wide range
            of cryptographic algorithms and other security features that are not included in the standard Java Security
            providers. The Bouncy Castle provider is particularly well-known for its support of newer and more advanced
            cryptographic algorithms, such as Elliptic Curve Cryptography (ECC) and Post-Quantum Cryptography (PQC).
        */
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        // Create wallets:
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        // Create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 // Manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; // Manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // it's important to store our first transaction in the UTXOs list.

        System.out.println("\n\nCreating and Mining Genesis block... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // Testing by adding new blocks onto the blockchain and signing transactions:
        Block block1 = new Block(genesis.hash);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        System.out.println("\nWalletA's balance is: " + walletA.getBalance());
        System.out.println("WalletB's balance is: " + walletB.getBalance());

        isChainValid();

    }

    public static @NotNull Boolean isChainValid() {

        Block currentBlock;
        Block previousBlock;

        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<>(); // A temporary working list of unspent transactions at a given block state.

        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        // Loop through blockchain to check hashes:
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            // Compare registered hash and calculated hash:
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Current Hashes not equal");
                return false;
            }
            // Compare previous hash and registered previous hash
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            // Check if hash is solved
            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            // Loop through blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                // To check if the digital signature on a transaction is valid or not:
                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }

                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                     /*
                        In a blockchain system, a transaction involves transferring a certain amount of cryptocurrency
                        from one address to another. Each transaction has one or more inputs, which represent the unspent
                        outputs of previous transactions that are being used as the source of funds for the current
                        transaction, and one or more outputs, which represent the new amounts being transferred to the
                        recipient addresses.

                        The code block then checks whether these two values are equal using the != operator. If the values
                        are not equal, it means that the transaction is attempting to spend more cryptocurrency than is
                        available in the input addresses, which is not allowed in a blockchain system.
                     */

                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {

                    /*
                        This code block is also part of a transaction verification process and checks whether the input
                        transactions referenced by the current transaction are valid and exist in the UTXO set
                        (i.e., the list of unspent transaction outputs).

                        In a blockchain system, a transaction input references a previous transaction output that has
                        not been spent yet. Each input has a unique transaction ID and output index that points to the
                        corresponding transaction output in the UTXO set. When a new transaction is created, it must
                        reference the correct transaction inputs and provide a valid signature to prove ownership of
                        the input addresses.
                     */
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    /*
                        In a blockchain system, a transaction output represents the new amount of cryptocurrency being
                        transferred to the recipient address(es). Each output has a unique ID that is calculated based
                        on the transaction ID and output index, which helps to identify the output transaction in the
                        UTXO set.

                        This step ensures that the output transactions of the current transaction are available in the
                        UTXO set for future transactions to reference as inputs. In other words, it updates the UTXO
                        set with the new outputs created by the current transaction.
                     */
                    tempUTXOs.put(output.id, output);
                }


                /*
                    This code block is also part of the transaction verification process, specifically for verifying
                    that the transaction outputs are being sent to the correct recipients.

                    In a blockchain system, a transaction output represents the new amount of cryptocurrency being
                    transferred to the recipient address(es). Each output has a recipient field that contains the
                    public key or address of the recipient to whom the output is being sent.

                    The code block verifies that the recipient of the first output transaction in the currentTransaction
                     object matches the intended recipient specified in the currentTransaction.recipient field.
                     If they do not match, it prints an error message and returns false.

                    It also verifies that the recipient of the second output transaction (which is typically used to
                    send "change" back to the sender) in the currentTransaction object matches the sender's address
                    specified in the currentTransaction.sender field. If they do not match, it prints an error message
                    and returns false.

                    These checks ensure that the transaction outputs are being sent to the correct recipients and that
                    any change from the transaction is being returned to the sender's address. If these checks fail,
                    the transaction verification process is halted, and the transaction is considered invalid.
                 */
                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }

        // If all the condition satisfies, then the blockchain is obviously valid
        System.out.println("Blockchain is valid");
        return true;
    }

    public static void addBlock(@NotNull Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}