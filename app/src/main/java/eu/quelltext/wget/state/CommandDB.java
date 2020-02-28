package eu.quelltext.wget.state;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import eu.quelltext.wget.bin.wget.Command;

public class CommandDB {

    static private final String PREFERENCES = "preferences";
    private static final String PREFERENCES_COMMANDS = "commands";

    List<Command> commands = new ArrayList<>();
    private SharedPreferences preferences;
    private Observer observer = new NullObserver();

    public static CommandDB of(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES, context.MODE_PRIVATE);
        CommandDB commands = new CommandDB(preferences);
        return commands;
    }

    public CommandDB(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void load() {
        String commandsString = preferences.getString(PREFERENCES_COMMANDS, null);
        if (commandsString == null) {
            reset();
        } else {
            commands = Command.listFromString(commandsString);
            observer.notifyDataSetChanged();
        }
        if (commands.size() == 0) {
            reset();
        }
    }

    public void reset() {
        commands = new ArrayList<>();
        commands.add(Command.VERSION);
        commands.add(Command.GET_IMAGE);
        commands.add(Command.PORTAL_TO_STDOUT);
        commands.add(Command.LOCALHOST_TO_STDOUT);
        observer.notifyDataSetChanged();
        save();
    }

    public int size() {
        return commands.size();
    }

    public void save() {
        // see https://developer.android.com/reference/android/app/Activity.html#SavingPersistentState
        // for persistent state
        SharedPreferences.Editor ed = preferences.edit();
        String commandString = Command.listToString(commands);
        ed.putString(PREFERENCES_COMMANDS, commandString);
        ed.apply();
    }

    public Command get(int position) {
        return commands.get(position);
    }

    public void removeWithoutSaving(Command command) {
        int index = commands.indexOf(command);
        commands.remove(index);
        // notify about removal https://stackoverflow.com/a/26645164/1320237
        observer.notifyItemRemoved(index);
    }

    public void remove(Command command) {
        removeWithoutSaving(command);
        save();
    }

    public void add(Command command) {
        while (commands.contains(command)) {
            removeWithoutSaving(command);
        }
        commands.add(0, command);
        observer.notifyItemInserted(0);
        save();
    }

    public void register(Observer observer) {
        this.observer = observer;
    }

    public List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        for (Command command : commands) {
            urls.addAll(command.getUrls());
        }
        return urls;
    }

    // add an observer
    // see https://en.wikipedia.org/wiki/Observer_pattern
    public interface Observer {
        void notifyItemInserted(int position);
        void notifyItemRemoved(int position);
        void notifyDataSetChanged();
    }
}
