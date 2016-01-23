package com.mala.aliasgar.crypto_aes_gcm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/*
Ideas of the algorithm from
YouTube, StackOverflow, Wikipedia, http://csrc.nist.gov/publications/nistpubs/800-38D/SP-800-38D.pdf (AES_GSM paper)
 */

public class MainActivity extends AppCompatActivity {

    EditText et_editText;
    TextView tv_encrypt;
    TextView tv_decrypt;
    Button b_encrypt;
    Button b_decrypt;

    //key has to be either 128/256 bits
    final private String keyString = "1846248126576158" ;
    //this is can be bob's I.P, port etc.
    final private String additionalDataString = "8419651";
    //the random  factor for the key generation
    final private String ivString = "8546918738493548";

    final byte[] keyBytes = keyString.getBytes();
    final byte[] ADDBytes = additionalDataString.getBytes();

    private String encryptedData;
    private String decryptedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_editText = (EditText) findViewById(R.id.et_edit);
        b_encrypt = (Button) findViewById(R.id.b_encrypt);
        tv_encrypt = (TextView) findViewById(R.id.tv_encrypt);
        b_decrypt = (Button) findViewById(R.id.b_decrypt);
        tv_decrypt = (TextView) findViewById(R.id.tv_decrypt);

        b_encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                encryptedData = encrypt(et_editText.getText().toString());
                tv_encrypt.setText(encryptedData);
                tv_decrypt.setText("");
            }
        });


        b_decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                decryptedData = decrypt(encryptedData);
                tv_decrypt.setText(decryptedData);
            }
        });
    }

    private String encrypt(final String plainText){

        final byte[] plainTextBytes = plainText.getBytes();
        final byte[] ivBytes = ivString.getBytes();

        try {
            //cipher class which provides security algorithms
            //we use AES-GSM (No padding)
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final int blockSize = cipher.getBlockSize();

            // create the key
            final SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            // create the IV
            final IvParameterSpec iv = new IvParameterSpec(ivBytes);

            //initializing the cipher
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            //adding additional data to cipher which is not in cipher text
            cipher.updateAAD(ADDBytes);

            final byte[] encryptedText = cipher.doFinal(plainTextBytes);

            // concatenate IV and encrypted message
            final byte[] ivAndEncryptedText = new byte[ivBytes.length + encryptedText.length];

            System.arraycopy(ivBytes, 0, ivAndEncryptedText, 0, blockSize);
            System.arraycopy(encryptedText, 0, ivAndEncryptedText, blockSize, encryptedText.length);

            //convert into base64
            return Base64.encodeToString(ivAndEncryptedText, Base64.DEFAULT);


        } catch (NoSuchAlgorithmException | NoSuchPaddingException  e){
            e.printStackTrace();
            throw new IllegalStateException("Check the algorithm and padding scheme");

        } catch(InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e){

            e.printStackTrace();
            throw new IllegalStateException("Check the key scheme");
        }
    }


    private String decrypt(final String ivAndEncryptedMessageBase64){

        final byte[] ivAndEncryptedText = Base64.decode(ivAndEncryptedMessageBase64, Base64.DEFAULT);

        try {
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final int blockSize = cipher.getBlockSize();

            // create the key
            final SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // get random IV from start of the received message and tag is at the end of the message
            final byte[] ivData = new byte[blockSize];
            System.arraycopy(ivAndEncryptedText, 0, ivData, 0, blockSize);
            final IvParameterSpec iv = new IvParameterSpec(ivData);

            // retrieve the encrypted message itself
            final byte[] encryptedText = new byte[ivAndEncryptedText.length - blockSize];
            System.arraycopy(ivAndEncryptedText, blockSize, encryptedText, 0, encryptedText.length);

            //decipher
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            //ADD
            cipher.updateAAD(ADDBytes);
            final byte[] encodedMessage = cipher.doFinal(encryptedText);

            return new String(encodedMessage);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException  e){
            e.printStackTrace();
            throw new IllegalStateException("Check the algorithm and padding scheme");

        } catch(InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e){

            e.printStackTrace();
            throw new IllegalStateException("Check the key scheme");
        }


    }
}
