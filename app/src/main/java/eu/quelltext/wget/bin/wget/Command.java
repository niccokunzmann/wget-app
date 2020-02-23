package eu.quelltext.wget.bin.wget;

import android.Manifest;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import eu.quelltext.wget.activities.MainActivity;
import eu.quelltext.wget.bin.BinaryAccess;
import eu.quelltext.wget.bin.Executable;


public class Command implements Parcelable {

    private static final String JSON_OPTIONS = "options";
    private static final String JSON_URLS = "urls";
    private static String BASE_COMMAND = "wget";
    private static final String EXAMPLE_PORTAL_URL = "http://detectportal.firefox.com/success.txt";
    private static final String EXAMPLE_IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/3/39/Official_gnu.svg";
    private static final String EXAMPLE_LOCALHOST_URL = "http://localhost:8080";

    public static final Command VERSION = new Command().addOption(Options.VERSION);
    public static final Command GET_IMAGE = new Command().addOption(Options.DEBUG).addUrl(EXAMPLE_IMAGE_URL);
    public static final Command PORTAL_TO_STDOUT = new Command().addOption(Options.OUTPUT.to("-")).addUrl(EXAMPLE_PORTAL_URL);
    public static final Command LOCALHOST_TO_STDOUT = new Command().addOption(Options.OUTPUT.to("-")).addUrl(EXAMPLE_LOCALHOST_URL);

    public static Command newDefault() {
        return new Command();
    }

    public Command addUrl(String url) {
        urls.add(url);
        return this;
    }

    public Command addOption(Option option) {
        options.add(option);
        return this;
    }

    private final List<Option> options = new ArrayList<>();
    private final List<String> urls = new ArrayList<>();

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(options.size());
        for (Option option: options) {
            parcel.writeParcelable(option, i);
        }
        parcel.writeInt(urls.size());
        for (String url: urls) {
            parcel.writeString(url);
        }
    }

    public Command(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            // load inner parcelable class, see
            // https://www.survivingwithandroid.com/android-parcelable-tutorial-list-class-2/
            options.add((Option)in.readParcelable(Option.class.getClassLoader()));
        }
        size = in.readInt();
        for (int i = 0; i < size; i++) {
            urls.add(in.readString());
        }
    }

    public Command() {
    }

    public static final Creator<Command> CREATOR = new Creator<Command>() {
        @Override
        public Command createFromParcel(Parcel in) {
            return new Command(in);
        }

        @Override
        public Command[] newArray(int size) {
            return new Command[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Executable.Result run(Context context) throws IOException {
        IWget wget = new BinaryAccess(context).wget();
        String[] commandList = getCommandList();
        return wget.run(commandList);
    }

    private String[] getCommandList() {
        List<String> command = new ArrayList<>();
        for (Option option: options) {
            for (String argument : option.asArguments()) {
                command.add(argument);
            }
        }
        for (String url: urls) {
            command.add(url);
        }
        String[] commandList = new String[command.size()];
        for (int i = 0; i < command.size(); i++) {
            commandList[i] = command.get(i);
        }
        return commandList;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getOptionsText(Context context) {
        if (options.size() == 0) {
            return "";
        }
        String result = "";
        String delimiter = ", ";
        for (Option option: options) {
            result += option.toShortText(context);
            result += delimiter;
        }
        return result.substring(0, result.length() - delimiter.length());
    }

    public String getUrlText() {
        if (options.size() == 0) {
            return "";
        }
        String result = "";
        String delimiter = "\n";
        for (String url: urls) {
            result += url + delimiter;
        }
        return result.substring(0, result.length() - delimiter.length());
    }

    public String asCommandLineText() {
        String cmd = BASE_COMMAND;
        String[] commands = getCommandList();
        for (String item: commands) {
            cmd += " " + StringEscapeUtils.escapeXSI(item);
        }
        return cmd;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject result = new JSONObject();
        JSONArray jsonOptions = new JSONArray();
        JSONArray jsonUrls = new JSONArray();
        result.put(JSON_OPTIONS, jsonOptions);
        result.put(JSON_URLS, jsonUrls);
        for (Option option: options) {
            jsonOptions.put(option.toJSON());
        }
        for (String url: urls) {
            jsonUrls.put(url);
        }
        return result;
    }

    public static Command fromJSON(JSONObject data) throws JSONException {
        Command command = new Command();
        JSONArray jsonOptions = data.getJSONArray(JSON_OPTIONS);
        JSONArray jsonUrls= data.getJSONArray(JSON_URLS);
        for (int i = 0; i < jsonOptions.length(); i++) {
            JSONObject jsonOption = jsonOptions.getJSONObject(i);
            Option option = Option.fromJSON(jsonOption);
            command.addOption(option);
        }
        for (int i = 0; i < jsonUrls.length(); i++) {
            String url = jsonUrls.getString(i);
            command.addUrl(url);
        }
        return command;
    }

    public static JSONArray listToJSON(List<Command> commands) throws JSONException {
        JSONArray json = new JSONArray();
        for (Command command: commands) {
            json.put(command.toJSON());
        }
        return json;
    }

    public static List<Command> listFromJSON(JSONArray json) throws JSONException {
        List<Command> commands = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            JSONObject jsonCommand = json.getJSONObject(i);
            Command command = fromJSON(jsonCommand);
            commands.add(command);
        }
        return commands;
    }

    public static String listToString(List<Command> commands) {
        try {
            JSONArray json = listToJSON(commands);
            String result = json.toString(2);
            return result;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Command> listFromString(String data) {
        try {
            JSONArray json = new JSONArray(data);
            return listFromJSON(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Option getOptionWithId(String id) {
        for (Option option: options) {
            if (option.manualId().equals(id)) {
                return option;
            }
        }
        return null;
    }

    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        for (Option option: options) {
            if (option.readsExternalStorage()) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (option.revokesWritingToExternalStorage()) {
                permissions.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        return permissions;
    }
}
