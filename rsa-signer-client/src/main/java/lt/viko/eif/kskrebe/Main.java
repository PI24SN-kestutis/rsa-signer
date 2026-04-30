package lt.viko.eif.kskrebe;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;
import java.util.Scanner;

/**
 * RSA parašo klientas.
 *
 * <p>Ši programa leidžia vartotojui įvesti žinutę, sugeneruoja RSA raktų porą,
 * pasirašo žinutę privačiuoju raktu ir nusiunčia žinutę, skaitmeninį parašą
 * bei viešąjį raktą patikrinimo serveriui per TCP jungtį.</p>
 *
 * @author kskrebe
 * @version 1.0
 */
public class Main {

    /** Serverio, kuriam siunčiami duomenys, adresas. */
    private static final String HOST = "localhost";

    /** Prievadas (portas), prie kurio jungiamasi. */
    private static final int PORT = 5000;

    /**
     * Pagrindinis programos įėjimo taškas.
     *
     * <p>Vykdymo eiga:</p>
     * <ol>
     *   <li>Nuskaito vartotojo įvestą žinutę;</li>
     *   <li>Sugeneruoja 2048 bitų RSA raktų porą;</li>
     *   <li>Pasirašo žinutę privačiuoju raktu (SHA-256 su RSA algoritmu);</li>
     *   <li>Atspausdina sugeneruotą parašą ir viešąjį raktą;</li>
     *   <li>Nusiunčia žinutę, parašą ir viešąjį raktą patikrinimo serveriui.</li>
     * </ol>
     *
     * @param args komandinės eilutės argumentai (nenaudojami)
     * @throws Exception jei įvyksta klaida generuojant raktus, pasirašant arba siunčiant duomenis
     */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("RSA parašo klientas paleistas.");
        System.out.print("Įveskite žinutę: ");
        String message = scanner.nextLine();

        // Sugeneruojame RSA raktų porą
        KeyPair keyPair = generateKeyPair();

        // Pasirašome žinutę privačiuoju raktu
        String signatureBase64 = signMessage(message, keyPair);

        // Koduojame viešąjį raktą į Base64 formatą
        String publicKeyBase64 = Base64.getEncoder().encodeToString(
                keyPair.getPublic().getEncoded()
        );

        System.out.println("\nSugeneruotas parašas:");
        System.out.println(signatureBase64);

        System.out.println("\nViešasis raktas:");
        System.out.println(publicKeyBase64);

        // Jungiamės prie serverio ir siunčiame duomenis
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            writer.println(message);
            writer.println(signatureBase64);
            writer.println(publicKeyBase64);

            System.out.println("\nDuomenys išsiųsti patikrinimo serveriui.");
        }

        System.out.println("Klientas baigė darbą.");
    }

    /**
     * Sugeneruoja naują 2048 bitų RSA raktų porą.
     *
     * @return {@link KeyPair} objektas, turintis privatų ir viešąjį raktus
     * @throws Exception jei RSA algoritmas nepalaikomas arba įvyksta kita klaida
     */
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    /**
     * Pasirašo nurodytą žinutę naudodamas RSA privatų raktą ir SHA-256 maišos algoritmą.
     *
     * @param message  žinutė, kurią reikia pasirašyti
     * @param keyPair  RSA raktų pora, iš kurios naudojamas privatusis raktas
     * @return skaitmeninis parašas, užkoduotas Base64 formatu
     * @throws Exception jei pasirašymo metu įvyksta klaida
     */
    private static String signMessage(String message, KeyPair keyPair) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(message.getBytes());

        byte[] signedBytes = signature.sign();

        return Base64.getEncoder().encodeToString(signedBytes);
    }
}