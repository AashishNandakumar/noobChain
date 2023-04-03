import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;

    /* MERKLE ROOT:
        In a blockchain, a Merkle root is a summary or hash of all the transactions that are included in a block.
        It's named after the computer scientist Ralph Merkle, who came up with the idea.

        Basically, imagine you have a bunch of transactions that you want to include in a block in the blockchain. You
        can't just dump them all in there as-is, because that would make the block huge and hard to verify.
        Instead, you can group the transactions into pairs, and then take the hash of each pair.

        Then you can group those pairs into pairs, and take the hash of each of those pairs. You keep doing this until
        you end up with just one hash, which is the Merkle root.

        The Merkle root is important because it serves as a summary of all the transactions in the block. When someone
        wants to verify the integrity of a block, they can just check the Merkle root. If the Merkle root matches the
        one recorded in the blockchain header, then all the transactions in the block must be valid and haven't been
        tampered with.

        By using a Merkle tree to compute the Merkle root, we can efficiently summarize a large number of transactions
        into a single hash value, which makes it easier to verify the integrity of the block.
    */
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>(); // Our data in the transaction will be a simple message.
    public long timeStamp; // As number of milliseconds since 1/1/1970.("Unix epoch")

    /* NONCE:
        In a blockchain context, a nonce is a number that is included in the header of a block that is being mined. It
        is a 32-bit (or higher) integer that is initially set to zero and is incremented each time the block header is
        hashed until a valid hash is found.

        The hash of a block header must meet a specific difficulty target in order for the block to be considered valid
        and added to the blockchain. The difficulty target is determined by the network and is adjusted periodically
        based on the amount of computing power being used to mine blocks.

        By incrementing the nonce value and rehashing the block header each time, miners attempt to find a hash that is
        below the difficulty target. This process is known as proof of work, and the first miner to find a valid hash
        is rewarded with newly created cryptocurrency and any transaction fees included in the block.

        The use of a nonce in the block header helps to ensure that the process of mining blocks is fair and
        decentralized. Since the hash function used in blockchain is designed to be unpredictable, the only way to find
        a valid hash is through brute force trial-and-error. By incrementing the nonce value, miners are forced to
        expend significant computational resources to find a valid hash, which helps to prevent any single entity from
        monopolizing the mining process.

     */
    public int nonce;

    // Block Constructor.
    public Block(String previousHash ) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime(); // Returns the number of milliseconds that have elapsed since the Unix epoch

        this.hash = calculateHash(); // Making sure we do this after we set the other values.
    }

    // Calculate new hash based on blocks contents
    public String calculateHash() {
        /* FUNCTION BREAKDOWN:
            This code is a method for calculating the hash value of a block in a blockchain. The hash value is a unique
            digital fingerprint that identifies the block and all of its contents.

            The calculateHash() method takes the following steps to calculate the hash value:

            Concatenates the previous block's hash value, the timestamp of the current block, the nonce value, and the
            Merkle root of the block's transactions into a single string.
            Applies the SHA-256 hashing algorithm to the concatenated string to produce a 256-bit hash value.
            Returns the hash value as a string.

            SHA-256 (Secure Hash Algorithm 256-bit) is a cryptographic hash function that takes an input of arbitrary
            length and produces a fixed-size 256-bit hash value. It is one of the most widely used hash functions in
            modern cryptography and is considered to be secure.

            Let's break down the steps:

            previousHash is the hash value of the previous block in the chain. This is used to link the blocks together
            in a secure and tamper-proof way.

            timeStamp is a timestamp representing the time when the block was created.

            nonce is a random number that is used in the proof-of-work algorithm to mine the block. By changing the
            nonce value, the miner can alter the resulting hash value and try to find a value that meets the difficulty
            target.

            merkleRoot is the root of a Merkle tree that is used to store and verify the transactions in the block.
            The Merkle root is calculated by hashing all the individual transaction hashes in the tree.

            By concatenating these values and hashing them using the SHA-256 algorithm, the calculateHash() method
            produces a unique hash value that represents the contents of the block. This hash value is used to verify
            the integrity of the block and prevent any tampering with its contents.

         */
        return StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
    }

    // Increases nonce value until hash target is reached.
    public void mineBlock(int difficulty) {
        /* FUNCTION BREAKDOWN:
            This code defines a method called mineBlock that takes an integer difficulty as input. The purpose of this
            method is to mine a block by repeatedly calculating the block's hash until it satisfies a certain difficulty
            level.

            First, the method calculates the Merkle root of the block's transactions using the StringUtil.getMerkleRoot
            method. The Merkle root is a summary hash of all the transactions in the block.

            Next, the method sets a target string that consists of difficulty number of leading zeros. This is the
            target that the block's hash needs to match or exceed to be considered valid.

            The method then enters a loop that increments a nonce variable and recalculates the block's hash until the
            hash's first difficulty characters match the target. This process is called proof-of-work, and it requires
            a lot of computational effort to find a valid hash.

            Once a valid hash is found, the loop exits, and the method prints a message indicating that the block has
            been mined, along with its hash.

            Overall, the mineBlock method is an important component of the blockchain's consensus algorithm that helps
            ensure the integrity and security of the blockchain.
         */
        merkleRoot = StringUtil.getMerkleRoot(transactions); // while mining the blk, set the merkelRoot
        String target = StringUtil.getDificultyString(difficulty); // Create a string with difficulty * "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    // Add transactions to this block
    public boolean addTransaction(Transaction transaction) {
        /* FUNCTION BREAKDOWN:
            This is a method that adds a new transaction to the list of transactions in the block. Here is a step-by-step
            explanation of what the code does:

            The method takes a Transaction object as input parameter.

            It first checks if the transaction is null. If it is, then it returns false indicating that the transaction
            was not added to the block.

            If the block is not the genesis block (i.e., if the previousHash is not "0"), then it processes the
            transaction by calling its processTransaction() method. The processTransaction() method verifies the
            signatures of the transaction inputs, checks if the transaction inputs have enough funds, and creates new
            transaction outputs. If the transaction fails to process, then the method returns false indicating that the
            transaction was not added to the block.

            If the transaction is valid, it adds it to the transactions list of the block.

            Finally, it prints a message indicating that the transaction was successfully added to the block and returns
            true.

            Overall, this method ensures that only valid transactions are added to the block.

         */
        // process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;

        if((!"0".equals(previousHash))) {

            if((!transaction.processTransaction())) {

                System.out.println("Transaction failed to process. Discarded.");

                return false;
            }
        }

        transactions.add(transaction);

        System.out.println("Transaction Successfully added to Block");

        return true;
    }

}
