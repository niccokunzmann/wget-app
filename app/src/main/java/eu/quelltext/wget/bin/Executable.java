package eu.quelltext.wget.bin;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

import eu.quelltext.wget.R;

public class Executable {
    private final String path;

    public Executable(String path) {
        this.path = path;
    }

    public Result run(String[] arguments, String[] environmentVariables) throws IOException {
        // concatenate two arrays, see https://stackoverflow.com/a/80559/1320237
        String[] command = ArrayUtils.addAll(new String[]{this.path}, arguments);
        // should be removed in the future
        // see https://stackoverflow.com/a/57116787/1320237
        File cwd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Process process = Runtime.getRuntime().exec(command, environmentVariables, cwd);
        DataOutputStream os = new DataOutputStream(process.getOutputStream());
        DataInputStream is = new DataInputStream(process.getInputStream());
        return new ExecutionResult(process, os, is);
    }

    public interface Result {
        void waitFor() throws InterruptedException;
        boolean isRunning();
        String getOutput() throws IOException;

        int getReturnCodeStringId();

        void kill();
    }

    private static class ExecutionResult implements Result {

        private final Process process;
        private final DataOutputStream output;
        private final DataInputStream input;
        private byte[] outputBytes;

        public ExecutionResult(Process process, DataOutputStream os, DataInputStream is) {
            this.process = process;
            this.output = os;
            this.input = is;
            outputBytes = new byte[]{};
        }

        public void waitFor() throws InterruptedException {
            process.waitFor();
        }

        public String getOutput() throws IOException {
            // input stream to array, see https://stackoverflow.com/a/1264756/1320237
            int available = input.available();
            if (available > 0) {
                byte[] newData = new byte[available];
                int read = input.read(newData);
                if (read != available) {
                    // copy range, see https://stackoverflow.com/a/11001759/1320237
                    newData = Arrays.copyOf(newData, read);
                }
                outputBytes = ArrayUtils.addAll(outputBytes, newData);
            }
            return new String(outputBytes);
        }

        private final int[] ERROR_CODE_STRINGS = new int[]{
                R.string.command_result_0,
                R.string.command_result_1,
                R.string.command_result_2,
                R.string.command_result_3,
                R.string.command_result_4,
                R.string.command_result_5,
                R.string.command_result_6,
                R.string.command_result_7,
                R.string.command_result_8
        };

        @Override
        public int getReturnCodeStringId() {
            if (isRunning()) {
                return R.string.command_result_running;
            }
            int code = process.exitValue();
            if (code < ERROR_CODE_STRINGS.length) {
                return ERROR_CODE_STRINGS[code];
            }
            return R.string.command_result_unkonwn;
        }

        @Override
        public void kill() {
            process.destroy();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    process.wait(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                process.destroyForcibly();
            }
        }

        public boolean isRunning() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return process.isAlive();
            }
            try {
                // should raise, see https://www.baeldung.com/java-process-api#exitvalue-method
                process.exitValue();
            } catch (Exception e) {
                return true;
            }
            return false;
        }


    }
}
