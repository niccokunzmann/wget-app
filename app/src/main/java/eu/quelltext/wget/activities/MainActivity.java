package eu.quelltext.wget.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.wget.Command;

public class MainActivity extends AppCompatActivity {

    private static final String PREFRENCES_COMMANDS = "commands";
    static private String PREFERENCES = "preferences";

    private RecyclerView recyclerView;
    private CommandsAdapter mAdapter;
    private List<Command> commands;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.commands);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new CommandsAdapter();
        recyclerView.setAdapter(mAdapter);

        mPrefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        loadCommands();
    }

    private void loadCommands() {
        String commandsString = mPrefs.getString(PREFRENCES_COMMANDS, null);
        if (commandsString == null) {
            commands = defaultCommands();
        } else {
            commands = Command.listFromString(commandsString);
        }
        if (commands.size() == 0) {
            commands = defaultCommands();
        }
        mAdapter.notifyDataSetChanged();
    }

    private List<Command> defaultCommands() {
        List<Command> commands = new ArrayList<>();
        commands.add(Command.VERSION);
        commands.add(Command.GET_IMAGE);
        commands.add(Command.PORTAL_TO_STDOUT);
        commands.add(Command.LOCALHOST_TO_STDOUT);
        return commands;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCommands();
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeCommands();
    }

    private void storeCommands() {
        // see https://developer.android.com/reference/android/app/Activity.html#SavingPersistentState
        // for persistent state
        SharedPreferences.Editor ed = mPrefs.edit();
        String commandString = Command.listToString(commands);
        ed.putString(PREFRENCES_COMMANDS, commandString);
        ed.commit();
    }

    class CommandsAdapter extends RecyclerView.Adapter {

        class CommandViewHolder extends RecyclerView.ViewHolder {

            private final Button buttonRun;
            private final TextView rightText;
            private final TextView bottomText;
            private final View background;
            private final View root;

            public CommandViewHolder(@NonNull View root) {
                super(root);
                this.root = root;
                buttonRun = root.findViewById(R.id.button_run);
                rightText = root.findViewById(R.id.text_right);
                bottomText = root.findViewById(R.id.text_bottom);
                background = root.findViewById(R.id.background);
            }

            public void display(final Command command) {
                buttonRun.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // open a new activity, see https://stackoverflow.com/a/4186097/1320237
                        Intent myIntent = new Intent(MainActivity.this, CommandActivity.class);
                        myIntent.putExtra(CommandActivity.ARG_COMMAND, command); //Optional parameters
                        MainActivity.this.startActivity(myIntent);
                    }
                });

                if (command.getOptions().size() == 0 && command.getUrls().size() == 1) {
                    bottomText.setVisibility(View.GONE);
                    rightText.setText(command.getUrls().get(0));
                } else {
                    if (command.getUrls().size() == 0) {
                        bottomText.setVisibility(View.GONE);
                    } else {
                        bottomText.setVisibility(View.VISIBLE);
                        bottomText.setText(command.getUrlText());
                    }
                    rightText.setText(command.getOptionsText(MainActivity.this));
                }

                root.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        removeCommand(command);
                        return true;
                    }
                });
            }

            public void odd() {
                background.setBackgroundColor(getResources().getColor(R.color.background_odd));
            }

            public void even() {
                background.setBackgroundColor(getResources().getColor(R.color.background_even));
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_command_list_element, parent, false);

            CommandViewHolder vh = new CommandViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder_, int position) {
            CommandViewHolder holder = (CommandViewHolder)holder_;
            Command command = commands.get(position);
            holder.display(command);
            if (position % 2 == 1) {
                holder.odd();
            } else {
                holder.even();
            }
        }

        @Override
        public int getItemCount() {
            int count = commands.size();
            return count;
        }
    }

    private void removeCommand(Command command) {
        int index = commands.indexOf(command);
        commands.remove(index);
        // notify about removal https://stackoverflow.com/a/26645164/1320237
        mAdapter.notifyDataSetChanged();
    }

    private void addCommand(Command command) {
        commands.add(0, command);
        mAdapter.notifyDataSetChanged();
    }
}
