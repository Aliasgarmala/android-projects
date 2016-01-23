package com.mala.aliasgar.dictcrypto;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
/*
Few improvements can be done here. We can load the dictionary only once when the app first loads
//have a splash screen when the dictionary is been loaded
 */
public class MainActivity extends AppCompatActivity {

    //HashSet chosen since the add and search is O(1)
    Set<String> wordsSet = new HashSet<>();
    EditText editText;
    Button parse;
    TextView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createDictionary();

        editText = (EditText) findViewById(R.id.et_edit);
        parse = (Button) findViewById(R.id.b_submit);
        display = (TextView) findViewById(R.id.tv_display);

        parse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                findWords(editText.getText().toString().toLowerCase().replaceAll("\\s+", ""));
            }
        });
    }

    private void createDictionary(){

        //get the resources from asset folder
        AssetManager assetManager = getAssets();
        InputStream input;
        try {
            // the dictionary file is stored in the asset folder
            //you can add and remove words from the file
            input = assetManager.open("dictionary.txt");

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String text = new String(buffer);
            String [] words = text.split("[\\r\\n]");

            // add all the words from the dictionary into the HashSet
            for(String word : words){
                if(word.length() !=2)
                    wordsSet.add(word);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findWords(String s){

        StringBuilder allWords = new StringBuilder();
        StringBuilder characters = new StringBuilder();

        //parse through the entire string character by character
        //to search the word in the dictionary
        for(int i = 0; i < s.length(); i++){

           characters.append(s.charAt(i));
            //word is found in the dictionary
           if(wordsSet.contains(characters.toString())){
               allWords.append(characters+" ");
               characters.setLength(0);
           }
        }

        if(allWords.toString() != "")
            display.setText(allWords.toString());
        else{
            display.setText("Sorry, we can't find any words.. Please try a different search");
            editText.setText("");
        }
    }
}
