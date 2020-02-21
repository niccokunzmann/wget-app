package eu.quelltext.wget.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.Executable;
import eu.quelltext.wget.bin.wget.Command;

public class CommandActivity extends AppCompatActivity {

    public static final String ARG_COMMAND = "command";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TextView output = (TextView)findViewById(R.id.text_output);

        // get parcelable from intent https://stackoverflow.com/a/7181792/1320237
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Command command = extras.getParcelable(ARG_COMMAND); //if it's a string you stored.

        try {
            Executable.Result result = command.run(this);
            result.waitFor();
            output.setText(result.getOutput());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
