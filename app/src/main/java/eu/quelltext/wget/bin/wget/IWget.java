package eu.quelltext.wget.bin.wget;

import androidx.annotation.NonNull;

import java.io.IOException;

import eu.quelltext.wget.bin.Executable;

public interface IWget {
    @NonNull
    String version();

    boolean isValid();

    Executable.Result run(String[] command, String[] envList) throws IOException;
}
