package org.davidmoten.oa3.codegen.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class DownloadExtras {

    public static void run(File list) {
        try {
            File cacheDirectory = new File(".openapi-codegen/cache");
            cacheDirectory.mkdirs();

            Files.readAllLines(list.toPath()) //
                    .stream().map(x -> x.trim()) //
                    .filter(x -> !x.isEmpty()) //
                    .map(x -> x.split(",")) //
                    .forEach(items -> download(items[0], items[1], cacheDirectory));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void download(String url, String toFilename, File cacheDirectory) {
        if (new File(cacheDirectory, toFilename).exists()) {
            System.out.println("[INFO ]" + toFilename + " exists alread in cache");
            return;
        }
        System.out.println("[INFO] downloading " + url);
        try {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            File tmp = new File(cacheDirectory, toFilename + ".tmp");
            try (InputStream in = con.getInputStream(); OutputStream out = new FileOutputStream(tmp)) {
                copy(in, out);
            }
            File destination = new File(cacheDirectory, toFilename);
            tmp.renameTo(destination);
            System.out.println("[INFO] downloaded " + toFilename + " to cache");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer))!= -1) {
            out.write(buffer, 0, n);
        }
    }

}
