package lt.viko.eif.kskrebe;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.Scanner;

public class Main {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("RSA signature client started.");
        System.out.print("Enter message: ");
        String message = scanner.nextLine();

        KeyPair keyPair = generateKeyPair();
        String signatureBase64 = signMessage(message, keyPair);
        String publicKeyBase64 = Base64.getEncoder().encodeToString(
                keyPair.getPublic().getEncoded()
        );

        System.out.println("\nGenerated signature:");
        System.out.println(signatureBase64);

        System.out.println("\nPublic key:");
        System.out.println(publicKeyBase64);

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println(message);
            writer.println(signatureBase64);
            writer.println(publicKeyBase64);

            System.out.println("\nData sent to verifier server.");
        }

        System.out.println("Client stopped.");
    }

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String signMessage(String message, KeyPair keyPair) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(message.getBytes());

        byte[] signedBytes = signature.sign();

        return Base64.getEncoder().encodeToString(signedBytes);
    }
}