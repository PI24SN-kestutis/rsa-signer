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

/**
 * Pagrindinis serverio klasė, skirta RSA skaitmeninių parašų tikrinimui.
 *
 * <p>Serveris klausosi nurodyto prievado (port'o), laukia kliento prisijungimo
 * ir gauna tris eilutes:
 * <ol>
 *   <li>Žinutę (pranešimo tekstą)</li>
 *   <li>Skaitmeninį parašą (Base64 formatu)</li>
 *   <li>Viešąjį RSA raktą (Base64 / X.509 formatu)</li>
 * </ol>
 * Gavęs šiuos duomenis, serveris patikrina, ar parašas atitinka žinutę ir
 * pateiktą viešąjį raktą, bei išveda rezultatą į konsolę.
 * </p>
 *
 * @author kskrebe
 * @version 1.0
 */
public class Main {

    /** TCP prievado numeris, kuriuo serveris klausosi prisijungimų. */
    private static final int PORT = 5000;

    /**
     * Programos įėjimo taškas.
     *
     * <p>Metodas:
     * <ul>
     *   <li>Sukuria serverio lizdą ({@link ServerSocket}) ir laukia vieno kliento;</li>
     *   <li>Nuskaito žinutę, parašą ir viešąjį raktą iš kliento srauto;</li>
     *   <li>Iškviečia {@link #verifySignature(String, String, String)} parašo tikrinimui;</li>
     *   <li>Išveda tikrinimo rezultatą į standartinę išvestį.</li>
     * </ul>
     * Po pirmojo kliento aptarnavimo serveris baigia darbą.
     * </p>
     *
     * @param args komandinės eilutės argumentai (nenaudojami)
     * @throws Exception bet kokia I/O arba saugumo klaida paleidimo metu
     */
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

            // Pirmoji eilutė – pasirašyta žinutė
            String message = reader.readLine();
            // Antroji eilutė – skaitmeninis parašas (Base64)
            String signatureBase64 = reader.readLine();
            // Trečioji eilutė – siuntėjo viešasis RSA raktas (Base64 / X.509)
            String publicKeyBase64 = reader.readLine();

            System.out.println("Received message:");
            System.out.println(message);

            System.out.println("\nReceived signature:");
            System.out.println(signatureBase64);

            System.out.println("\nReceived public key:");
            System.out.println(publicKeyBase64);

            // Tikrinama, ar parašas galioja
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

    /**
     * Tikrina RSA skaitmeninį parašą naudodamas SHA-256 maišos algoritmą.
     *
     * <p>Metodas atlieka šiuos žingsnius:
     * <ol>
     *   <li>Dekodas viešąjį raktą iš Base64 į {@link PublicKey} objektą (X.509 specifikacija);</li>
     *   <li>Dekodas parašą iš Base64 į baitų masyvą;</li>
     *   <li>Inicializuoja {@link Signature} objektą su algoritmu {@code SHA256withRSA};</li>
     *   <li>Pateikia žinutės baitus tikrinimui ir grąžina rezultatą.</li>
     * </ol>
     * </p>
     *
     * @param message        originali žinutė (paprastas tekstas), kurios parašas tikrinamas
     * @param signatureBase64 skaitmeninis parašas, užkoduotas Base64 formatu
     * @param publicKeyBase64 siuntėjo viešasis RSA raktas, užkoduotas Base64 / X.509 formatu
     * @return {@code true} – jei parašas teisingas; {@code false} – jei parašas neteisingas
     *         arba įvyko klaida apdorojant duomenis
     */
    private static boolean verifySignature(String message, String signatureBase64, String publicKeyBase64) {
        try {
            // Viešojo rakto atkūrimas iš Base64 / X.509 baitų
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Parašo tikrinimas su SHA-256 + RSA algoritmu
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());

            return signature.verify(signatureBytes);

        } catch (Exception e) {
            // Klaida tikrinimo metu – parašas laikomas negaliojančiu
            System.out.println("\nVerification error:");
            System.out.println(e.getMessage());
            return false;
        }
    }
}