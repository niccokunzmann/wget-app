package eu.quelltext.wget.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import eu.quelltext.wget.R;

public class ConfigurationActivity extends AppCompatActivity {

    public static final String ARG_COMMAND = "command";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
    }
}
