package eu.quelltext.wget.bin;

import androidx.annotation.NonNull;

import java.io.IOException;

public class NullWget implements IWget {
    @Override
    @NonNull
    public String version() {
        return "null";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public Executable.Result run(String[] command) {
        return new NullResult();
    }

    private class NullResult implements Executable.Result {

        @Override
        public void waitFor() throws InterruptedException {
        }

        @Override
        public String getOutput() throws IOException {
            return "";
        }
    }
}
