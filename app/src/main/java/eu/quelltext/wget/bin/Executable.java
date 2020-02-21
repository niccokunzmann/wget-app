package eu.quelltext.wget.bin;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class Executable {
    private final String path;

    public Executable(String path) {
        this.path = path;
    }

    public Result run(String[] parameters) throws IOException {
        // concatenate two arrays, see https://stackoverflow.com/a/80559/1320237
        String[] command = ArrayUtils.addAll(new String[]{this.path}, parameters);
        Process process = Runtime.getRuntime().exec(command);
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        DataInputStream is = new DataInputStream(process.getInputStream());
        return new Result(process, os, is);
    }

    public static class Result {

        private final Process process;
        private final DataOutputStream output;
        private final DataInputStream input;

        public Result(Process process, DataOutputStream os, DataInputStream is) {
            this.process = process;
            this.output = os;
            this.input = is;
        }

        public void waitFor() throws InterruptedException {
            process.waitFor();
        }

        public String getOutput() throws IOException {
            // input stream to array, see https://stackoverflow.com/a/1264756/1320237
            byte[] bytes = IOUtils.toByteArray(input);
            return new String(bytes);
        }
    }
}
