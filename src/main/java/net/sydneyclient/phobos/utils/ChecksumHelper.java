package net.sydneyclient.phobos.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ChecksumHelper {
    public static String getMD5Checksum(File file) throws Exception {
        int i;

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        MessageDigest digest = MessageDigest.getInstance("MD5");

        do {
            if ((i = fileInputStream.read(buffer)) <= 0) continue;
            digest.update(buffer, 0, i);
        } while (i != -1);

        fileInputStream.close();

        byte[] md5Checksum = digest.digest();

        StringBuilder builder = new StringBuilder();
        for (byte b : md5Checksum) {
            builder.append(Integer.toString((b & 0xFF) + 256, 16).substring(1));
        }

        return builder.toString();
    }

    public static File getModFile() {
        Path modsPath = FabricLoader.getInstance().getGameDir().resolve("mods");
        File[] files = modsPath.toFile().listFiles();
        if (files == null) return null;

        for (File file : files) {
            if (!isValidMod(file)) continue;
            return file;
        }

        return null;
    }

    private static boolean isValidMod(File file) {
        try (ZipFile zip = new ZipFile(file)) {
            ZipEntry fmjEntry = zip.getEntry("fabric.mod.json");
            if (fmjEntry == null) return false;

            try (InputStream stream = zip.getInputStream(fmjEntry)) {
                String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                JsonElement jsonElement = JsonParser.parseString(content);

                if (jsonElement instanceof JsonObject jsonObject) {
                    if (jsonObject.has("id")) {
                        return "phobos".equals(jsonObject.get("id").getAsString());
                    }
                }
            }
        } catch (Exception ignored) {
            return false;
        }

        return false;
    }
}
