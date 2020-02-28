package eu.quelltext.wget.bin.wget.options;

import android.content.Context;
import android.os.Parcel;

import org.json.JSONObject;

import eu.quelltext.wget.bin.wget.options.display.Display;

public class BinaryOption extends Option {
    private final String cmd;
    private final int nameId;
    private final int explanationId;

    public BinaryOption(String cmd, int nameId, int explanationId) {
        super();
        this.cmd = cmd;
        this.nameId = nameId;
        this.explanationId = explanationId;
    }

    public BinaryOption(Parcel in) {
        super(in);
        this.cmd = in.readString();
        this.nameId = in.readInt();
        this.explanationId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(cmd);
        parcel.writeInt(nameId);
        parcel.writeInt(explanationId);
    }

    @Override
    public String[] asArguments() {
        return new String[]{cmd};
    }

    @Override
    public String toShortText(Context context) {
        return context.getResources().getString(nameId);
    }

    @Override
    public String manualId() {
        return cmd;
    }

    @Override
    public Option fromManualJSON(JSONObject jsonOption) {
        return new BinaryOption(cmd, nameId, explanationId);
    }

    @Override
    public void displayIn(Display section) {
        section.addSwitch();
        section.addTitle(this.nameId);
        section.addExplanation(this.explanationId);
    }
}
