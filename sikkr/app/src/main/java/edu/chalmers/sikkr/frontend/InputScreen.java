package edu.chalmers.sikkr.frontend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import edu.chalmers.sikkr.R;

/**
 * Created by Jingis on 2014-10-22.
 */
public class InputScreen extends Activity {
    private int operator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.inputscreen_layout);
        numberExist();
        setupSpinner();
    }

    private void setupSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.operator_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operator_spinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                operator = getOperator((String) parent.getItemAtPosition(pos));
            }
            public void onNothingSelected(AdapterView<?> parent) {
                operator = 1;
            }
        });
    }

    private int getOperator(String chosenOperator) {
        switch (chosenOperator) {
            case("Tele2/Comviq"): return 1;
            case("Telia"): return 2;
            case("Telenor"): return 3;
            case("Halebop"): return 4;
            case("Tre"): return 5;
            default : return 0;
        }
    }

    private void numberExist() {
        File f = new File(getFilesDir(), "number");
        if(f.exists()) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void enterNbr(View view) {
        EditText tv = ((EditText)findViewById(R.id.editText));
        final String savedNbr = tv.getText().toString();
        final File parent = getFilesDir();
        final File file = new File(parent, "number");

        try {
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create parent folder");
            }

            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("Could not create file");
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.append(savedNbr + System.getProperty ("line.separator") + operator);
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Toast.makeText(this, "Exception when creating file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        numberExist();
    }

}
