package studio.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// The purposes at the moment is to have sorted keys in storing properties to disk
public class PropertiesConfig extends Properties {

    private static final Logger log = LogManager.getLogger();

    private final static long SAVE_DELAY_SEC = 2;
    private final static Charset CHARSET = StandardCharsets.ISO_8859_1;

    private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private final String filename;
    private final FilesBackup filesBackup;

    private Properties propertiesToSave = null;


    public PropertiesConfig(FilesBackup filesBackup, String filename) {
        this.filesBackup = filesBackup;
        this.filename = filename;
    }

    public PropertiesConfig cloneConfig() {
        saveToDisk();

        PropertiesConfig config = new PropertiesConfig(filesBackup, filename);
        config.putAll(this);
        return config;
    }

    public synchronized void save() {
        if (propertiesToSave == null) {
            executor.schedule(this::saveToDisk, SAVE_DELAY_SEC, TimeUnit.SECONDS);
        }
        propertiesToSave = new Properties();
        propertiesToSave.putAll(this);
    }

    private synchronized ByteArrayOutputStream getStreamToSave() {
        if (propertiesToSave == null) return null;

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            super.store(buffer, "Auto-generated by Studio for kdb+");
            return buffer;
        } catch (IOException e) {
            log.error("Error in serializing config for the file {}", filename, e);
        } finally {
            propertiesToSave = null;
        }

        return null;
    }

    public void saveToDisk() {
        ByteArrayOutputStream buffer = getStreamToSave();
        if (buffer == null) return;

        try (OutputStream out = filesBackup.newFileOutputStream(filename)) {
            byte[] lineSeparator = System.getProperty("line.separator").getBytes(CHARSET);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray()), CHARSET));

            List<String> lines = new ArrayList<>();
            for (; ; ) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.startsWith("#")) {
                    out.write(line.getBytes(CHARSET));
                    out.write(lineSeparator);
                } else {
                    lines.add(line);
                }
            }
            Collections.sort(lines);

            for (String line : lines) {
                out.write(line.getBytes(CHARSET));
                out.write(lineSeparator);
            }
        } catch (IOException e) {
            log.error("Error in saving config to the file {}", filename, e);
        }

    }

    @Override
    public void save(OutputStream out, String comments) {
        throw new IllegalStateException("save() should be called");
    }

    @Override
    public void store(Writer writer, String comments) throws IOException {
        throw new IllegalStateException("save() should be called");
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        throw new IllegalStateException("save() should be called");
    }

}
