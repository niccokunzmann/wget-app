package eu.quelltext.wget.bin;

import androidx.annotation.NonNull;

import java.io.IOException;

public interface IWget {
    @NonNull
    String version();

    boolean isValid();

    Executable.Result run(String[] command) throws IOException;
}
