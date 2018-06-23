package com.example.tientran.bkhome;

import java.io.Serializable;

/**
 * Created by tientran on 07/03/2018.
 */

public class Kenh implements Serializable{
    public String nameChannel;
    public String numberChannel;
    private boolean Checked = false;
    public int idImage;

    public Kenh(String nameChannel, String numberChannel, int idImage) {
        this.nameChannel = nameChannel;
        this.numberChannel = numberChannel;
        this.idImage = idImage;
    }
    public boolean isChecked() {
        return Checked;
    }
    public void setCheck(boolean check){
        Checked = check;
    }
}
