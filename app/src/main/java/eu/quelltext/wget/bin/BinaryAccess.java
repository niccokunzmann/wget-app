package eu.quelltext.wget.bin;

import android.content.Context;
import android.net.http.HttpResponseCache;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import eu.quelltext.wget.R;
import eu.quelltext.wget.activities.MainActivity;

import static android.provider.Telephony.Mms.Part.FILENAME;

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
            try {
                installer.install();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (installer.wget().isValid()) {
                return installer.wget();
            }
        }
        return new NullWget();
    }

    private BinaryInstaller[] installers() {
        return new BinaryInstaller[]{
            // resources created like https://stackoverflow.com/a/29308338/1320237
                new BinaryInstaller(R.raw.wget, "wget"),
                new BinaryInstaller(R.raw.wget_x86, "wget_x86"),
                new BinaryInstaller(R.raw.wget_armeabi, "wget_armeabi"),
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
            file.setExecutable(true);
        }

        public IWget wget() {
            return new BinaryWget(getExecutable());
        }

        private Executable getExecutable() {
            return new Executable(context.getFilesDir().getPath() + "/" + this.name);
        }
    }
}
