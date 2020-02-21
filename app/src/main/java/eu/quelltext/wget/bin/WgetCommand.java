package eu.quelltext.wget.bin;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

import eu.quelltext.wget.activities.CommandActivity;

public class WgetCommand implements Parcelable {
    private String[] command;

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(command.length);
        parcel.writeStringArray(command);
    }

    public WgetCommand(Parcel in) {
        int length = in.readInt();
        command = new String[length];
        in.readStringArray(command);
    }

    public WgetCommand(String[] command) {
        this.command = command;
    }

    public static final Creator<WgetCommand> CREATOR = new Creator<WgetCommand>() {
        @Override
        public WgetCommand createFromParcel(Parcel in) {
            return new WgetCommand(in);
        }

        @Override
        public WgetCommand[] newArray(int size) {
            return new WgetCommand[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Executable.Result run(Context context) throws IOException {
        IWget wget = new BinaryAccess(context).wget();
        return wget.run(command);
    }
}
