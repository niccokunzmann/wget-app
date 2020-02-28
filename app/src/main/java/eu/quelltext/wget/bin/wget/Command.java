package eu.quelltext.wget.bin.wget;

import android.Manifest;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.quelltext.wget.bin.BinaryAccess;
import eu.quelltext.wget.bin.Executable;
import eu.quelltext.wget.bin.wget.options.Option;
import eu.quelltext.wget.bin.wget.options.Options;


public class Command implements Parcelable {

    private static final String JSON_OPTIONS = "options";
    private static final String JSON_URLS = "urls";
    private static String BASE_COMMAND = "wget";
    private static final String EXAMPLE_PORTAL_URL = "http://detectportal.firefox.com/success.txt";
    private static final String EXAMPLE_IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/3/39/Official_gnu.svg";
    private static final String EXAMPLE_LOCALHOST_URL = "http://localhost:8080";

    public static final Command VERSION = new Command().addOption(Options.VERSION);
    public static final Command GET_IMAGE = new Command().addOption(Options.DEBUG).addUrl(EXAMPLE_IMAGE_URL);
    public static final Command PORTAL_TO_STDOUT = new Command().addOption(Options.OUTPUT_DOCUMENT.to("-")).addUrl(EXAMPLE_PORTAL_URL);
    public static final Command LOCALHOST_TO_STDOUT = new Command().addOption(Options.OUTPUT_DOCUMENT.to("-")).addUrl(EXAMPLE_LOCALHOST_URL);

    public static Command createDefaultCommand() {
        Command command = new Command();
        // see the issue to create a user friendly default command
        // https://stackoverflow.com/a/57116787/1320237
        command.addOption(Options.OUTPUT_FILE.defaultOption());
        command.addOption(Options.DIRECTORY_PREFIX.defaultOption());
        command.addOption(Options.CONTINUE);
        return command;
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
        String[] arguments = getArgumentsList();
        String[] envList = getEnvironmentList();
        return wget.run(arguments, envList);
    }

    public String[] getArgumentsList() {
        List<String> command = new ArrayList<>();
        for (Option option: options) {
            for (String argument : option.asArguments()) {
                command.add(argument);
            }
        }
        for (String url: urls) {
            command.add(url);
        }
        return command.toArray(new String[]{});
    }
    public String[] getEnvironmentList() {
        List<String> envList = new ArrayList<>();
        for (Option option: options) {
            String environmentVariable = option.asEnvironmentVariable();
            if (environmentVariable != null) {
                envList.add(environmentVariable);
            }
        }
        return envList.toArray(new String[]{});
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
        String[] arguments = getArgumentsList();
        for (String item: arguments) {
            cmd += " " + StringEscapeUtils.escapeXSI(item);
        }
        String[] env = getEnvironmentList();
        for (String item: env) {
            int equalSignIndex = item.indexOf("=") + 1;
            cmd = item.substring(0, equalSignIndex) + StringEscapeUtils.escapeXSI(item.substring(equalSignIndex)) + " " + cmd;
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

    @Override
    public boolean equals(@Nullable Object other) {
        if (Command.class.isInstance(other)) {
            return ((Command)other).equalsCommand(this);
        }
        return super.equals(other);
    }

    public boolean equalsCommand(Command other) {
        // check for equality, see https://stackoverflow.com/a/13501662/1320237
        return getUrls().containsAll(other.getUrls()) && other.getUrls().containsAll(getUrls()) &&
                getOptions().containsAll(other.getOptions()) && other.getOptions().containsAll(getOptions());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
