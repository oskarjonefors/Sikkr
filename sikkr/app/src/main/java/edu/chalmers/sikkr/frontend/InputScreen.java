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
    private String operator;

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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operator_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Save the spinners value and save it to operator variable
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                operator = (String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {
                operator = "Tele2/Comviq";
            }
        });
    }

    private void numberExist() {
        File f1 = new File(getFilesDir(), "number");
        File f2 = new File(getFilesDir(), "operator");
        if(f1.exists() && f2.exists()) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void enterNbr(View view) {
        final EditText tv = ((EditText)findViewById(R.id.editText));
        final String savedNbr = tv.getText().toString();
        Toast.makeText(this, "Number = " + savedNbr + "\n" + " Operator= " + operator,Toast.LENGTH_SHORT).show();
        final File parent = getFilesDir();
        final File numberFile = new File(parent, "number");
        final File operatorFile = new File(parent, "operator");
        BufferedWriter numberWriter = null, operatorWriter = null;

        try {
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create parent folder");
            }

            if (!numberFile.exists() && !numberFile.createNewFile()) {
                throw new IOException("Could not create number file");
            }

            if (!operatorFile.exists() && !operatorFile.createNewFile()) {
                throw new IOException("Could not create operator file");
            }

            numberWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(numberFile)));
            operatorWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(operatorFile)));
            numberWriter.append(savedNbr);
            numberWriter.newLine();
            numberWriter.flush();

            operatorWriter.append(operator);
            operatorWriter.newLine();
            operatorWriter.flush();
        } catch (Exception e) {
            Toast.makeText(this, "Exception when creating file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (numberWriter != null) {
                try {
                    numberWriter.close();
                } catch (IOException e) {
                    Toast.makeText(this, "Exception when creating file", Toast.LENGTH_SHORT).show();
                }
            }
            if (operatorWriter != null) {
                try {
                    operatorWriter.close();
                } catch (IOException e) {
                    Toast.makeText(this, "Exception when creating file", Toast.LENGTH_SHORT).show();
                }
            }
        }
        numberExist();
    }

}
