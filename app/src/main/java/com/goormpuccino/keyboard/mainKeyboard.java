package com.goormpuccino.keyboard;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class mainKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private boolean isCaps = false;

    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.layout, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        RunnableThread runnable = new RunnableThread();
        Thread thread = new Thread(runnable);
        thread.start();

        return kv;
    }

    class RunnableThread implements Runnable {
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(5678);
                Socket sock = serverSocket.accept();
//                InputStream in = sock.getInputStream();
                InputStream in = new BufferedInputStream(sock.getInputStream());

                InputConnection ic;

                byte[] buf = new byte[18];
                boolean up;
                int code;
                boolean shift = false;

                while ((in.read(buf, 0, 18)) != -1) {
                    Log.e("Keyboard", new String(buf, "UTF-8"));

                    up = (Integer.parseInt(new String(Arrays.copyOfRange(buf, 1, 2), "UTF-8")) == 1);
                    code = Integer.parseInt(new String(Arrays.copyOfRange(buf, 10, 18),  "UTF-8"));

                    Log.e("Keyboard", "up: " +up);
                    Log.e("Keyboard", "code: " + code);

                    ic = getCurrentInputConnection();
                    //ic.commitText(String.valueOf(Character.toLowerCase((char)code)), 1);

                    if(up) {
                        // 48 ~ 57 : number
                        // 13: enter
                        // 8 : backspace
                        // 16 : shift
                        // 65 ~ 90 : capital alphabet letters
                        // 32 : space bar
                        // 190 : .
                        // 186 : ;
                        // 188 : ,
                        // 191 : /
                        // 222 : "

                        if(code == 16) {
                            Log.e("Keyboard", "shift released");
                            shift = false;
                            continue;
                        } else if (code == 188) {
                            if(!shift) {
                                ic.commitText(",", 1);
                            } else {
                                ic.commitText("<", 1);
                            }
                        } else if (code == 190) {
                            if(!shift) {
                                ic.commitText(".", 1);
                            } else {
                                ic.commitText(">", 1);
                            }
                        }  else if (code == 191) {
                            if(!shift) {
                                ic.commitText("/", 1);
                            } else {
                                ic.commitText("?", 1);
                            }
                        }   else if (code == 186) {
                            if(!shift) {
                                ic.commitText(";", 1);
                            } else {
                                ic.commitText(":", 1);
                            }
                        }   else if (code == 222) {
                            if(!shift) {
                                ic.commitText("'", 1);
                            } else {
                                ic.commitText("\"", 1);
                            }
                        } else if (code == 13) {
                            ic.sendKeyEvent(new KeyEvent(1, 1, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON));
                        } else if(code == 8) {
                            ic.deleteSurroundingText(1, 0);
                        } else if (code >= 65 && code <= 90) {
                            //alphabet
                            if(!shift) {
                                ic.commitText(String.valueOf(Character.toLowerCase((char)code)), 1);
                            } else {
                                ic.commitText(String.valueOf((Character.toUpperCase((char)code))), 1);
                            }
                        } else {
                            ic.commitText(String.valueOf((char)code), 1);
                        }
                    } else {
                        if(code == 16) {
                            //shift key
                            shift = true;
                            Log.e("Keyboard", "shift pushed");
                        }
                    }
                    //ic.sendKeyEvent(new KeyEvent(up ? KeyEvent.ACTION_UP : KeyEvent.ACTION_DOWN, code));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void onPress(int i ){

    }
    public void onRelease(int i){

    }
    public void onKey(int i, int[] ints){
        InputConnection ic = getCurrentInputConnection();
        playClick(i);
        switch(i){
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case 0:
            default:
                char code = (char)i;
                if(Character.isLetter(code) && isCaps)
                    code = Character.toUpperCase(code);
                ic.commitText(String.valueOf(code), 1);
        }
    }


    public void playClick(int i) {

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(i) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }


    }
    public void onText(CharSequence charSequence){

    }
    public void swipeLeft() {

    }
    public void swipeRight() {

    }
    public void swipeDown(){

    }
    public void swipeUp() {

    }

}
