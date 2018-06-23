package com.example.tientran.bkhome;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by tientran on 27/03/2018.
 */

public class SaveChannel {
    Context context;

    public SaveChannel(Context context) {
        this.context = context;
    }

    //Ham ghi arraylist vao file "ListChannel"
    public void WriteChannel(ArrayList<Kenh> arrayList) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("ListChannel.txt", context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(arrayList);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    //Ham doc arraylist tu file "ListChannel"
    //Return ArrayList
    public ArrayList<Kenh> ReadChannel() {
        ArrayList<Kenh> result = null;
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = context.openFileInput("ListChannel.txt");
            objectInputStream = new ObjectInputStream(fileInputStream);
            result = (ArrayList<Kenh>) objectInputStream.readObject();
            Log.d("ReadChannel", result.toString());
            objectInputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
        return result;
    }

    //Ham kiem tra file "ListChannel" ton tai chua
    //Return true neu ton tai
    //Return false neu chua ton tai
    public boolean isSaveChannel() {
        try {
            FileInputStream fileInputStream = context.openFileInput("ListChannel.txt");
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
