package net.neoforged.neoforge.client.model.obj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Simple tokenizer for OBJ file format parsing.
 */
public class ObjTokenizer implements AutoCloseable {
    private final BufferedReader reader;

    public ObjTokenizer(Reader reader) {
        this.reader = reader instanceof BufferedReader br ? br : new BufferedReader(reader);
    }

    public String[] readAndSplitLine(boolean ignoreEmptyLines) throws IOException {
        String line;
        do {
            line = reader.readLine();
            if (line == null) return null;
            line = line.trim();
        } while (ignoreEmptyLines && (line.isEmpty() || line.startsWith("#")));

        return line.split("\\s+");
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
