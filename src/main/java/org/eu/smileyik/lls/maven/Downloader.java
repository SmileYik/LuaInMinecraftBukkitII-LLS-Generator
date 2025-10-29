package org.eu.smileyik.lls.maven;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;

public class Downloader {
    /**
     * download file to target location and return hash.
     */
    public static String download(String url, int bufferSize, int timeout, File out) throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException, TimeoutException {
        try (
                FileOutputStream fos = new FileOutputStream(out);
                BufferedOutputStream bos = new BufferedOutputStream(fos)
        ) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            download(url, bufferSize, timeout, (bytes, len) -> {
                digest.update(bytes, 0, len);
                bos.write(bytes, 0, len);
            });
            bos.flush();
            return HexUtil.bytesToHex(digest.digest());
        }
    }

    public static void download(String url, int bufferSize, int timeout, Callback callback) throws ExecutionException, InterruptedException, TimeoutException {
        java.net.HttpURLConnection.setFollowRedirects(true);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        try {
            Future<?> future = executorService.submit(() -> {
                try {
                    URL u = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestProperty("User-Agent", "curl");

                    try (
                            InputStream is = conn.getInputStream();
                            BufferedInputStream bis = new BufferedInputStream(is);
                    ) {
                        int len;
                        byte[] buffer = new byte[bufferSize];
                        while ((len = bis.read(buffer)) > 0) {
                            callback.accept(buffer, len);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            });
            future.get(timeout, TimeUnit.SECONDS);
        } finally {
            executorService.shutdownNow();
        }
    }

    public static byte[] download(String url, int bufferSize, int timeout) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedOutputStream bos = new BufferedOutputStream(baos);
        ) {
            download(url, bufferSize, timeout, (bytes, len) -> {
                bos.write(bytes, 0, len);
            });
            bos.flush();
            return baos.toByteArray();
        }
    }

    public static String getUrlContents(String url) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        return new String(download(url, 8192, 60));
    }

    public interface Callback {
        public void accept(byte[] buffer, int len) throws IOException;
    }
}
