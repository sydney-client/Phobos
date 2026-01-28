package net.sydneyclient.phobos;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.sydneyclient.phobos.utils.ChecksumHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class AuthHttpServer {
    private static final int PORT = 7565;

    public static void initialize() {
        CountDownLatch latch = new CountDownLatch(1);

        System.out.println("[Phobos-Server] [HTTP] Generating mod file checksum...");

        String checksum;
        try {
            checksum = ChecksumHelper.getMD5Checksum(ChecksumHelper.getModFile());
        } catch (Exception exception) {
            throw new RuntimeException("A critical error has occurred while trying to generate the checksum for the mod file!", exception);
        }

        Thread thread = new Thread(() -> runServer(latch, checksum));
        thread.start();

        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("A critical error has occurred while trying to start the server thread!", exception);
        }

        System.out.println("[Phobos-Server] [HTTP] Successfully started!");
    }

    private static void runServer(CountDownLatch latch, String checksum) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/api/v1/client/signature", exchange -> sendResponse(exchange, checksum));
            server.createContext("/api/v2/protection/manage", exchange -> sendResponse(exchange, ""));
            server.createContext("/api/v2/protection/table", exchange -> sendResponse(exchange, ""));
            server.createContext("/api/v1/client/report", exchange -> sendResponse(exchange, ""));

            server.setExecutor(Executors.newCachedThreadPool());

            server.start();

            latch.countDown();
        } catch (IOException exception) {
            throw new RuntimeException("A critical error has occurred while trying to start the HTTP server!", exception);
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(200, bytes.length == 0 ? -1 : bytes.length);

        try (OutputStream stream = exchange.getResponseBody()) {
            if (bytes.length > 0) {
                stream.write(bytes);
            }
        }

        exchange.close();
    }
}
