package eu.quelltext.wget.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.wget.Command;
import eu.quelltext.wget.state.CommandDB;
import eu.quelltext.wget.state.RecyclerObserverAdapter;

public class CommandListActivity extends AppCompatActivity {

    private static final int ACTIVITY_EDIT_COMMAND = 0;

    private RecyclerView recyclerView;
    private CommandsAdapter mAdapter;
    private CommandDB commands;
    private SharedPreferences mPrefs;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.commands);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new CommandsAdapter();
        recyclerView.setAdapter(mAdapter);

        commands = CommandDB.of(this);
        commands.register(new RecyclerObserverAdapter(mAdapter));
        commands.load();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNewCommand();
            }
        });
    }

    private void editNewCommand() {
        editCommand(Command.createDefaultCommand());
    }

    @Override
    protected void onResume() {
        super.onResume();
        commands.load();
    }

    @Override
    protected void onPause() {
        super.onPause();
        commands.save();
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
                        Intent myIntent = new Intent(CommandListActivity.this, CommandActivity.class);
                        myIntent.putExtra(CommandActivity.ARG_COMMAND, command); //Optional parameters
                        CommandListActivity.this.startActivity(myIntent);
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
                    rightText.setText(command.getOptionsText(CommandListActivity.this));
                }

                root.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        commands.remove(command);
                        return true;
                    }
                });
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editCommand(command);
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

    private void editCommand(Command command) {
        // open a new activity, see https://stackoverflow.com/a/4186097/1320237
        Intent myIntent = new Intent(CommandListActivity.this, ConfigurationActivity.class);
        myIntent.putExtra(ConfigurationActivity.ARG_COMMAND, command); //Optional parameters
        startActivityForResult(myIntent, ACTIVITY_EDIT_COMMAND);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // see https://www.javatpoint.com/android-option-menu-example
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_new_command:
                editNewCommand();
                return true;
            case R.id.menu_delete_all_commands:
                commands.reset();
                return true;
            case R.id.menu_about:
                openAboutActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAboutActivity() {
        // open a new activity, see https://stackoverflow.com/a/4186097/1320237
        Intent myIntent = new Intent(this, AboutActivity.class);
        startActivity(myIntent);
    }
}
