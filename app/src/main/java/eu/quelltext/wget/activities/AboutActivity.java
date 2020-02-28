package eu.quelltext.wget.activities;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import eu.quelltext.wget.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // create a back button at the top of the activity
        // see https://stackoverflow.com/a/16755282/1320237
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // make the links clickable
        // see https://stackoverflow.com/a/2746708/1320237
        TextView about = (TextView) findViewById(R.id.text_about);
        about.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
