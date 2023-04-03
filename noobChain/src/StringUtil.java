import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/* PACKAGES INFO:
    The java.util.Base64 class is used to encode and decode binary data in the Base64 format. The Base64 encoding is a
    way of representing binary data in ASCII text format so that it can be easily transmitted over channels that only
    support ASCII characters.
    The Base64 class contains two static methods, getEncoder() and getDecoder(), which return Base64.Encoder and Base64.
    Decoder instances respectively. The Base64.Encoder class provides methods to encode binary data to Base64 format,
    while the Base64.Decoder class provides methods to decode Base64 encoded data back to its original binary form.

    The line of code import com.google.gson.GsonBuilder; imports the GsonBuilder class from the Google Gson library.
    Gson is a Java library that allows conversion between Java objects and their JSON representation. GsonBuilder is a
    class that provides configuration options for Gson, such as setting the date format or ignoring fields during
    serialization or deserialization.
    In summary, importing the GsonBuilder class enables the use of Gson to handle JSON data in a Java program, and
    provides configuration options to customize the Gson behavior.
*/
public class StringUtil {

    //Applies Sha256 to a string and returns the result.
    public static @NotNull String applySha256(String input){
        /* FUNCTION BREAKDOWN:
            This code defines a method called applySha256 that takes a String input and applies the SHA-256 hash
            function to it, returning the hash as a hexadecimal string.

            Here is a line-by-line explanation of what the code does:

            MessageDigest digest = MessageDigest.getInstance("SHA-256");: This line gets an instance of the
            MessageDigest class with the algorithm set to SHA-256. A MessageDigest object is used to calculate the hash
            of input data.

            byte[] hash = digest.digest(input.getBytes("UTF-8"));: This line applies the hash function to the input
            string by calling the digest() method on the MessageDigest object. The input string is first converted to a
            byte array using the UTF-8 character encoding.

            StringBuffer hexString = new StringBuffer();: This line creates a StringBuffer object to hold the hash as a
            hexadecimal string.

            for (int i = 0; i < hash.length; i++) {: This line starts a loop that iterates over each byte in the hash
            array.

            String hex = Integer.toHexString(0xff & hash[i]);: This line converts the current byte to a hexadecimal
            string using the toHexString() method of the Integer class. (0xff is a hexadecimal literal in Java
            representing the value 255 in decimal. The & operator performs a bitwise AND operation between 0xff and
            hash[i], meaning that it sets all bits to 0 except the 8 least significant bits of hash[i].)

            if(hex.length() == 1) hexString.append('0');: This line checks if the hexadecimal string has only one
            character. If it does, a leading 0 is added to the hexString object to ensure that the output string has an
            even number of characters.

            hexString.append(hex);: This line appends the hexadecimal string to the hexString object.
            return hexString.toString();: This line returns the final hash as a string of hexadecimal digits.

         */

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Applies sha256 to our input,
            byte[] hash = digest.digest(input.getBytes("UTF-8"));

            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexadecimal
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Applies ECDSA Signature and returns the result ( as bytes ).
    public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
        /* FUNCTION BREAKDOWN:
            This code is a method to apply an ECDSA (Elliptic Curve Digital Signature Algorithm) signature to a given
            input string using a private key. Here's what each part of the code does:

            dsa = Signature.getInstance("ECDSA", "BC");: This line initializes a new Signature object with the "ECDSA"
            algorithm and "BC" provider (which stands for Bouncy Castle, a third-party cryptography library).

            dsa.initSign(privateKey);: This line initializes the Signature object with the provided private key. This
            means that the signature will be generated using the private key associated with the public key that will
            later be used to verify the signature.

            byte[] strByte = input.getBytes();: This line converts the input string into a byte array, since the update()
            method of the Signature object takes a byte array as input.

            dsa.update(strByte);: This line adds the input byte array to the Signature object.

            byte[] realSig = dsa.sign();: This line generates the signature by calling the sign() method of the Signature
            object, which returns a byte array containing the signature.

            output = realSig;: This line sets the output variable to the generated signature byte array.

            Finally, the method returns the output variable containing the signature byte array.

         */
        Signature dsa;

        byte[] output;

        try {
            dsa = Signature.getInstance("ECDSA", "BC");

            dsa.initSign(privateKey);

            byte[] strByte = input.getBytes();

            dsa.update(strByte);

            byte[] realSig = dsa.sign();

            output = realSig;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    // Verifies a String signature
    public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
        /* FUNCTION BREAKDOWN:
            This code is a method for verifying an ECDSA signature. It takes three parameters:

            publicKey: A public key used for verifying the signature.
            data: The data that was signed.
            signature: The signature to be verified.

            Inside the method, the code first initializes an instance of the Signature class with the ECDSA algorithm
            and the BC provider.

            It then initializes the Signature object for verification using the given publicKey.

            The data parameter is then converted to a byte array and updated in the Signature object.

            Finally, the signature is verified against the provided data and publicKey.

            If the verification is successful, the method returns true. Otherwise, it throws a RuntimeException with
            the error message.

         */
        try {
            Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");

            ecdsaVerify.initVerify(publicKey);

            ecdsaVerify.update(data.getBytes());

            return ecdsaVerify.verify(signature);
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* DEPRECATED!!!
        // Shorthand helper to turn Object into a json string
        public static String getJson(Object o) {
            return new GsonBuilder().setPrettyPrinting().create().toJson(o);
        }
    */

    // Returns difficulty string target, to compare to hash. e.g. difficulty of 5 will return "00000"
    public static @NotNull String getDificultyString(int difficulty) {
        /* FUNCTION BREAKDOWN:
            This is a method that returns a string of zeros that is used to represent the difficulty level in mining a
            block in a blockchain. The input parameter difficulty specifies the number of leading zeros required in the
            hash of the block to be considered valid.

            The method creates a new String object by repeating the character \0 (which represents the null character)
            difficulty number of times. This creates a string of difficulty length with all characters initialized to \0.
            Then, it replaces each null character with the character 0 to get the desired string of zeros with length
            difficulty.

            For example, if difficulty is 3, the method will return the string "000". This string can be used in the
            mining process to check if the hash of the block being mined has the required number of leading zeros.

         */
        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getStringFromKey(@NotNull Key key) {
        /* FUNCTION BREAKDOWN:
            This is a method that takes a Key object (which could be a PublicKey or a PrivateKey) and returns its
            Base64-encoded representation as a String. The key's encoded bytes are converted into a sequence of base-64
            characters using the Base64 class provided by Java's standard library.

            The purpose of encoding a key as a string is to make it easier to share and transmit the key. By encoding
            the key in Base64, the key can be represented as a string of characters that is safe to transmit over the
            internet or other communication channels. When the encoded string is received, it can be decoded back into
            the original key using the appropriate decoding function.

         */
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getMerkleRoot(@NotNull ArrayList<Transaction> transactions) {
        /* FUNCTION BREAKDOWN:
            This code calculates the Merkle root of a list of transactions using the SHA-256 hashing algorithm. The
            Merkle root is a value that summarizes all the transactions included in a block of a blockchain. The
            calculation is done in the following steps:

            The function takes an ArrayList of transactions as input.

            It starts by creating a list of transaction IDs called previousTreeLayer, which is initialized with the
            transaction IDs of all the transactions in the input ArrayList.

            It creates a variable called treeLayer, which is initially set to previousTreeLayer.

            It enters a loop that continues until there is only one item in treeLayer.

            In each iteration of the loop, it creates an empty list called treeLayer.

            It loops through the previousTreeLayer list and applies the SHA-256 hashing algorithm to each pair of
            adjacent items in the list, concatenating them together before hashing.

            It adds the resulting hash value to the treeLayer list.

            It sets the count variable to the size of the treeLayer list and sets the previousTreeLayer variable to the
            treeLayer list.

            It repeats the loop until there is only one item in the treeLayer list.

            Finally, it checks if the size of the treeLayer list is 1, and if so, sets the Merkle root to be the first
            (and only) item in the treeLayer list. Otherwise, it sets the Merkle root to an empty string.

            The result is a single hash value, called the Merkle root, that represents all the transactions in the input
            ArrayList. The Merkle root is then used as part of the block header in the blockchain to ensure the integrity
            and authenticity of the included transactions.

         */
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();

        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }

        List<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<String>();

            for(int i=1; i < previousTreeLayer.size(); i+=2) {
                treeLayer.add(applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }

            count = treeLayer.size();

            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }
}
