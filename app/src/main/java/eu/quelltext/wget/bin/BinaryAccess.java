package eu.quelltext.wget.bin;

import android.content.Context;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.wget.BinaryWget;
import eu.quelltext.wget.bin.wget.IWget;
import eu.quelltext.wget.bin.wget.NullWget;

/* This object is responsible for choosing the correct wget executable.
    https://stackoverflow.com/a/5642593/1320237
 */
public class BinaryAccess {


    private final Context context;

    public BinaryAccess(Context context) {
        this.context = context;
    }

    public IWget wget() {
        BinaryInstaller[] installers = installers();
        for (BinaryInstaller installer: installers) {
            if (installer.isInstalled() && installer.wget().isValid()) {
                return installer.wget();
            }
        }
        for (BinaryInstaller installer: installers) {
            try {
                installer.install();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (installer.wget().isValid()) {
                return installer.wget();
            }
            installer.uninstall();
        }
        return new NullWget();
    }

    private BinaryInstaller[] installers() {
        return new BinaryInstaller[]{
            // resources created like https://stackoverflow.com/a/29308338/1320237
                new BinaryInstaller(R.raw.wget, "wget"),
                new BinaryInstaller(R.raw.wget_x86, "wget_x86"),
                new BinaryInstaller(R.raw.wget_armeabi_no_pie, "wget_armeabi_no_pie"),
                new BinaryInstaller(R.raw.wget_mips, "wget_mips")
        };
    }

    private class BinaryInstaller {
        private final int id;
        private final String name;

        public BinaryInstaller(int rawId, String name) {
            this.id = rawId;
            this.name = name;
        }

        public void install() throws IOException {
            InputStream ins = context.getResources().openRawResource (id);
            FileOutputStream fos = context.openFileOutput(this.name, Context.MODE_PRIVATE);
            IOUtils.copy(ins, fos);
            ins.close();
            fos.close();

            File file = context.getFileStreamPath(name);
            if (!file.setExecutable(true)) {
                throw new IOException("Could not set executable bit for " + name);
            }
        }

        public IWget wget() {
            return new BinaryWget(getExecutable());
        }

        private Executable getExecutable() {
            return new Executable(getExecutablePath());
        }

        private String getExecutablePath() {
            String directory = context.getFilesDir().getPath();
            String path = directory + "/" + this.name;
            return path;
        }

        public boolean isInstalled() {
            File file = new File(getExecutablePath());
            return file.exists() && file.canExecute();
        }

        public void uninstall() {
            File file = context.getFileStreamPath(name);
            file.delete();
        }
    }
}
