package eu.quelltext.wget.bin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import eu.quelltext.wget.R;

class Executable {
    private final String path;

    public Executable(String path) {
        this.path = path;
    }

    public Result run(String[] parameters) throws IOException {
        Process process = Runtime.getRuntime().exec(this.path);
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
            return input.readUTF();
        }
    }
}
