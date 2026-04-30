package lt.viko.eif.kskrebe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Main {

    private static final int PORT = 5000;

    public static void main(String[] args) throws Exception {
        System.out.println("RSA signature verifier server started.");
        System.out.println("Waiting for client on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT);
             Socket socket = serverSocket.accept();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream())
             )) {

            System.out.println("\nClient connected.");
            System.out.println("Receiving signed data...\n");

            String message = reader.readLine();
            String signatureBase64 = reader.readLine();
            String publicKeyBase64 = reader.readLine();

            System.out.println("Received message:");
            System.out.println(message);

            System.out.println("\nReceived signature:");
            System.out.println(signatureBase64);

            System.out.println("\nReceived public key:");
            System.out.println(publicKeyBase64);

            boolean valid = verifySignature(message, signatureBase64, publicKeyBase64);

            System.out.println("\nVerification result:");
            if (valid) {
                System.out.println("SIGNATURE IS VALID");
            } else {
                System.out.println("SIGNATURE IS INVALID");
            }
        }

        System.out.println("\nServer stopped.");
    }

    private static boolean verifySignature(String message, String signatureBase64, String publicKeyBase64) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());

            return signature.verify(signatureBytes);

        } catch (Exception e) {
            System.out.println("\nVerification error:");
            System.out.println(e.getMessage());
            return false;
        }
    }
}