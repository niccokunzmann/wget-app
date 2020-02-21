package eu.quelltext.wget.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import eu.quelltext.wget.R;
import eu.quelltext.wget.bin.BinaryAccess;
import eu.quelltext.wget.bin.IWget;
import eu.quelltext.wget.bin.WgetCommand;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addCommand(R.id.run_version, new CommandCreator() {
            @Override
            public WgetCommand createCommand() {
                return new WgetCommand(new String[]{"--version"});
            }
        });
    }

    private void addCommand(int buttonId, final CommandCreator creator) {
        Button run = (Button)findViewById(buttonId);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WgetCommand command = creator.createCommand();
                // open a new activity, see https://stackoverflow.com/a/4186097/1320237
                Intent myIntent = new Intent(MainActivity.this, CommandActivity.class);
                myIntent.putExtra(CommandActivity.ARG_COMMAND, command); //Optional parameters
                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    interface CommandCreator {

        WgetCommand createCommand();
    }
}
