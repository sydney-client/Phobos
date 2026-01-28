package net.sydneyclient.phobos;

import net.sydneyclient.phobos.utils.CryptoHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CountDownLatch;

public class AuthSocket {
    private static final int PORT = 8080;

    private static final byte[] REQUEST_3_PART_1 = new byte[]{127, 0, 0, 0, 28, 0, 0, 0, 7, 83, 85, 67, 67, 69, 83, 83, 0, 0, 0, 96};
    private static final byte[] REQUEST_3_PART_2;
    private static final byte[] REQUEST_3_PART_3 = new byte[]{0, 0, 0, 8, 83, 112, 97, 114, 107, 121, 75, 82, 40, 0, 0, 0, 27, 0, 0, 0, 32, 104, 116, 116, 112, 115, 58, 47, 47, 105, 46, 105, 109, 103, 117, 114, 46, 99, 111, 109, 47, 97, 65, 68, 49, 118, 68, 98, 46, 106, 112, 101, 103};

    private static final byte[] REQUEST_1 = new byte[]{11, 0, 0, 0, 47, 0, 0, 0, 3, 3, 2, 1};
    private static final byte[] REQUEST_2 = new byte[]{4, 0, 0, 0, 46, 0};
    private static final byte[] REQUEST_3;
    private static final byte[] REQUEST_4 = new byte[]{20, 0, 0, 0, 44, 0, 0, 0, 11, -62, -89, 98, 107, 119, 120, 100, 120, -62, -89, 114, 0};
    private static final byte[] REQUEST_5 = new byte[]{104, 0, 0, 0, 25, -97, -36, -117, 31, 109, 120, -119, -66, 100, 80, 109, -116, -97, -9, -81, -59, 12, -75, 74, -25, 106, 98, 105, 99, 89, -126, -127, 122, 37, 45, 122, 73, 14, -8, -28, -60, 60, -126, -99, -90, 89, -25, -98, -119, -52, -93, 103, -119, -125, -102, 48, -12, -110, 32, -25, -117, 95, -63, 101, -70, -101, -86, -27, 69, 0, 0, 0, 32, 97, 56, 52, 99, 53, 100, 54, 102, 97, 52, 56, 57, 49, 100, 102, 50, 51, 48, 56, 101, 102, 98, 55, 51, 53, 56, 100, 53, 50, 97, 102, 54};

    static {
        String userType = "Beta";

        Instant expiration = Instant.now().plus(6767, ChronoUnit.DAYS).plus(6767, ChronoUnit.HOURS).plus(6767, ChronoUnit.MINUTES);
        String formattedExpiration = DateTimeFormatter.ISO_INSTANT.format(expiration);

        String launches = "67";

        String userData = CryptoHelper.encrypt(userType + "+" + formattedExpiration + "+" + launches);

        byte[] bytes = userData.getBytes(StandardCharsets.UTF_8);
        StringBuilder hex = new StringBuilder();

        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        String result = hex.toString().toLowerCase();

        int len = result.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(result.charAt(i), 16) << 4) + Character.digit(result.charAt(i+1), 16));
        }

        REQUEST_3_PART_2 = data;

        REQUEST_3 = new byte[REQUEST_3_PART_1.length + REQUEST_3_PART_2.length + REQUEST_3_PART_3.length];

        System.arraycopy(REQUEST_3_PART_1, 0, REQUEST_3, 0, REQUEST_3_PART_1.length);
        System.arraycopy(REQUEST_3_PART_2, 0, REQUEST_3, REQUEST_3_PART_1.length, REQUEST_3_PART_2.length);
        System.arraycopy(REQUEST_3_PART_3, 0, REQUEST_3, REQUEST_3_PART_1.length + REQUEST_3_PART_2.length, REQUEST_3_PART_3.length);
    }

    public static void initialize() {
        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("[Phobos-Server] [Socket] Starting...");

        Thread thread = new Thread(() -> runServer(latch));
        thread.start();

        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("A critical error has occurred while trying to start the server thread!", exception);
        }

        System.out.println("[Phobos-Server] [Socket] Successfully started!");
    }

    private static void runServer(CountDownLatch latch) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            latch.countDown();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (Exception e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException("A critical error has occurred while trying to start the server!", exception);
        }
    }

    private static void handleClient(Socket socket) {
        try (socket; InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {
            try {
                byte[] buffer = new byte[1024];

                int n = in.read(buffer);
                if (n == -1) return;

                out.write(REQUEST_1);
                out.flush();

                n = in.read(buffer);
                if (n == -1) return;

                out.write(REQUEST_2);
                out.flush();
                out.write(REQUEST_3);
                out.flush();
                out.write(REQUEST_4);
                out.flush();

                n = in.read(buffer);
                if (n == -1) return;

                out.write(REQUEST_5);
                out.flush();

                System.out.println("[Phobos-Server] [Socket] The authentication sequence has successfully been completed.");
            } catch (Exception e) {
                System.err.println("[Phobos-Server] [Socket] An error has occurred while completing the authentication sequence: " + e.getMessage());
            }
        } catch (Exception ignored) {}
    }
}