import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/* PACKAGE INFO:
    java.security.spec.ECGenParameterSpec is a class in Java that provides an implementation of the
    java.security.spec.AlgorithmParameterSpec interface for specifying elliptic curve parameters used in generating
    cryptographic keys.
    This class allows you to specify which elliptic curve to use in creating an instance of a
    java.security.KeyPairGenerator object, which generates pairs of public and private keys used for digital signatures
    and other cryptographic purposes. By specifying the curve using the ECGenParameterSpec class, you can ensure that
    the keys generated by the KeyPairGenerator are compatible with other software that uses the same curve, and that
    they provide adequate security for your specific use case.

 */
public class Wallet {

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public Wallet() {
        generateKeyPair();
    }

    public void generateKeyPair() {
        /* FUNCTION BREAKDOWN:
            This code generates a new ECDSA key pair, which consists of a public and private key.

            It uses the KeyPairGenerator class from the java.security package to generate the key pair. This line
            creates a new instance of the KeyPairGenerator class that will be used to generate public and private key
            pairs using the Elliptic Curve Digital Signature Algorithm (ECDSA). The "ECDSA" argument specifies the
            algorithm to use, while the "BC" argument specifies the provider of the algorithm
            (in this case, the Bouncy Castle provider).

            The initialize method is called to initialize the key generator with an ECGenParameterSpec object and a
            SecureRandom object.

            The ECGenParameterSpec specifies the elliptic curve that the key pair will use. This line creates a new
            instance of the ECGenParameterSpec class, which is used to specify the elliptic curve parameters to use when
            generating the key pair. The "prime192v1" argument specifies the name of the elliptic curve to use.

            The SecureRandom is used to provide the key generator with a source of random data. In this case, the
            elliptic curve specified is "prime192v1", which is a 192-bit curve. This line creates a new instance of the
            SecureRandom class that will be used to generate random numbers for use in the key pair generation process.
            The "SHA1PRNG" argument specifies the algorithm to use.

            After the key pair is generated, the public and private keys are retrieved from the KeyPair object using
            the getPublic and getPrivate methods and stored in the publicKey and privateKey variables, respectively.

            If an exception occurs during the key generation process, a RuntimeException is thrown.

         */
        try {

            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random); // 256

            KeyPair keyPair = keyGen.generateKeyPair();

            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        /* FUNCTION BREAKDOWN:
            This function calculates the balance of the current wallet address by iterating through all the unspent
            transaction outputs (UTXOs) in the UTXO pool of the blockchain and adding up the total value of the outputs
            that belong to the current wallet address.

            Here are the steps:

            Initialize a variable total to 0 to keep track of the total value of UTXOs that belong to the current wallet
            address.

            Iterate through all the key-value pairs in the UTXOs map of the NoobChain class using a for loop and the
            entrySet() method.

            For each key-value pair, get the value, which is a TransactionOutput object representing an unspent
            transaction output (UTXO).

            Check if the UTXO belongs to the current wallet address by calling the isMine() method of the
            TransactionOutput class, passing in the publicKey of the current wallet as the argument. This method checks
            if the public key used to create the UTXO matches the public key of the current wallet.

            If the UTXO belongs to the current wallet address, add it to the UTXOs map of the current wallet by calling
            the put() method of the UTXOs map of the current wallet, passing in the ID of the UTXO as the key and the
            UTXO object as the value.

            Add the value of the UTXO to the total variable.

            After iterating through all the UTXOs in the UTXO pool, return the total variable as the balance of the
            current wallet address.

         */
        float total = 0;

        for (Map.Entry<String, TransactionOutput> item: NoobChain.UTXOs.entrySet()){

            TransactionOutput UTXO = item.getValue();

            if(UTXO.isMine(publicKey)) { // if output belongs to me ( if coins belong to me )

                UTXOs.put(UTXO.id,UTXO); // add it to our list of unspent transactions.

                total += UTXO.value ;
            }
        }

        return total;
    }

    public Transaction sendFunds(PublicKey _recipient, float value ) {
        /* FUNCTION BREAKDOWN:

            Here are the steps:

            The function takes two arguments: the public key of the recipient and the value of the funds to be sent.

            If the current balance of the sender is less than the value of the funds to be sent, the function prints a
            message indicating that the transaction has been discarded and returns null.

            The function creates an empty array list of TransactionInput objects to hold the inputs for the transaction.

            The function initializes a variable total to 0 to keep track of the total value of the transaction inputs.

            The function iterates over all the unspent transaction outputs (UTXOs) in the sender's UTXO pool, and adds
            each UTXO as a new TransactionInput to the list of inputs for the new transaction.

            As each input is added, the function adds the value of the UTXO to the total variable, and breaks out of
            the loop if the total is greater than or equal to the value of the funds to be sent.

            The function creates a new Transaction object with the sender's public key, recipient's public key, value
            of the funds, and the list of inputs.

            The function generates a signature for the new transaction using the sender's private key.

            The function removes the UTXOs corresponding to the inputs from the sender's UTXO pool.

            The function returns the new transaction object.

            In summary, the sendFunds function creates and signs a new transaction transferring funds from the sender
            to the recipient, using the sender's UTXOs as inputs for the transaction. It also updates the sender's UTXO
            pool by removing the spent UTXOs. If the sender does not have enough funds to complete the transaction, the
            function discards the transaction and returns null.


         */

        if(getBalance() < value) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;

        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){

            TransactionOutput UTXO = item.getValue();

            total += UTXO.value;

            inputs.add(new TransactionInput(UTXO.id));

            if(total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);

        newTransaction.generateSignature(privateKey);

        for(TransactionInput input: inputs){

            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }

}