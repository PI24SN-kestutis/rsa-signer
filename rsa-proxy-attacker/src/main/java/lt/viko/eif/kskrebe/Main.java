package lt.viko.eif.kskrebe;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    private static final int PROXY_PORT = 5001;

    private static final String VERIFIER_HOST = "localhost";
    private static final int VERIFIER_PORT = 5000;

    public static void main(String[] args) throws Exception {
        System.out.println("RSA proxy attacker started.");
        System.out.println("Waiting for signer client on port " + PROXY_PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PROXY_PORT);
             Socket clientSocket = serverSocket.accept();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream())
             )) {

            System.out.println("\nSigner client connected.");
            System.out.println("Receiving data...\n");

            String message = reader.readLine();
            String signatureBase64 = reader.readLine();
            String publicKeyBase64 = reader.readLine();

            System.out.println("Received message:");
            System.out.println(message);

            System.out.println("\nReceived signature:");
            System.out.println(signatureBase64);

            System.out.println("\nReceived public key:");
            System.out.println(publicKeyBase64);

            Scanner scanner = new Scanner(System.in);

            System.out.print("\nDo you want to modify the signature? (yes/no): ");
            String answer = scanner.nextLine();

            if (answer.equalsIgnoreCase("yes")) {
                System.out.println("\nEnter modified signature:");
                signatureBase64 = scanner.nextLine();
                System.out.println("Signature was modified.");
            } else {
                System.out.println("Signature was not modified.");
            }

            try (Socket verifierSocket = new Socket(VERIFIER_HOST, VERIFIER_PORT);
                 PrintWriter writer = new PrintWriter(verifierSocket.getOutputStream(), true)) {

                writer.println(message);
                writer.println(signatureBase64);
                writer.println(publicKeyBase64);

                System.out.println("\nData forwarded to verifier server.");
            }
        }

        System.out.println("Proxy attacker stopped.");
    }
}