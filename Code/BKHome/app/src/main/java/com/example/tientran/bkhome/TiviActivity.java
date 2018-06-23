package com.example.tientran.bkhome;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class TiviActivity extends AppCompatActivity {

    private static final int NOT_FIND = -1;
    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private FloatingActionButton btnVoice;
    private Switch sw_power;
    private ListView listView;
    private ArrayList<Kenh> arrayChannel = new ArrayList<Kenh>();
    private ArrayList<Kenh> selectedChannel = new ArrayList<Kenh>();
    private KenhAdapter adapter;
    {
        try {
            mSocket = IO.socket("https://bkhome-test.herokuapp.com/mobileApp");
        } catch (URISyntaxException e) {
        }
    }
    //Array Logo channel
    int[] idIcon = {
            R.drawable.tivi, R.drawable.vtv1, R.drawable.vtv2,
            R.drawable.vtv3, R.drawable.vtv4, R.drawable.vtv5, R.drawable.vtv6,
            R.drawable.vtv7, R.drawable.vtv8, R.drawable.vtv9,
            R.drawable.hanoi1, R.drawable.hanoi2, R.drawable.vinhphuc, R.drawable.bibi,
            R.drawable.hbo, R.drawable.thanhhoa
    };
    int idIconAdded = idIcon[0];
    //Save arrayChannel to file
    private SaveChannel saveChannel = new SaveChannel(this);
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tivi);
        //Emit event update status
        mSocket.emit("UPDATE");
        //Listen even update status power
        mSocket.on("ESP_APP", SetStaus);
        //Connect to server
        mSocket.connect();

        btnVoice = (FloatingActionButton) findViewById(R.id.btnVoice);
        sw_power = (Switch) findViewById(R.id.swpower);
        listView = (ListView) findViewById(R.id.listView);

        //Check file "ListChannel.txt"
        if (saveChannel.isSaveChannel()) {
            arrayChannel = saveChannel.ReadChannel();
        } else {
            arrayChannel.add(new Kenh("VTV1", "1", R.drawable.vtv1));
            arrayChannel.add(new Kenh("VTV2", "2", R.drawable.vtv2));
            arrayChannel.add(new Kenh("VTV3", "253", R.drawable.vtv3));
            arrayChannel.add(new Kenh("VTV4", "4", R.drawable.vtv4));
            arrayChannel.add(new Kenh("VTV5", "5", R.drawable.vtv5));
            saveChannel.WriteChannel(arrayChannel);
        }
        adapter = new KenhAdapter(TiviActivity.this, R.layout.kenhlv, arrayChannel);
        listView.setAdapter(adapter);
        //Longclick on ListView
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                SlectChannel(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.action_mode, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.btnDelete:
                        for (Kenh kenh : selectedChannel)
                            arrayChannel.remove(kenh);
                        selectedChannel.clear();
                        adapter.notifyDataSetChanged();
                        mode.finish();
                        saveChannel.WriteChannel(arrayChannel);
                        SendNumberChannelToServer();
                        break;
                }
                return true;
            }

            //Khi ket thuc ActionMode thi update ListView
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                for (int i = 0; i < arrayChannel.size(); i++)
                    if (arrayChannel.get(i).isChecked())
                        arrayChannel.get(i).setCheck(false);
                adapter.notifyDataSetChanged();
            }
        });
        //Click on ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                UpdateChannel(position);
            }
        });
        sw_power.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ControlPower(isChecked);
            }
        });
    }//end onCreate

    //Set status power for switch
    private Emitter.Listener SetStaus = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject)args[0];
                    String status = "";
                    try {
                        status = data.getString("Status");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(status.equals("1")){
                        sw_power.setChecked(true);
                    }else
                        sw_power.setChecked(false);
                }
            });
        }
    };

    //Send control power to Esp8266
    private void ControlPower(boolean isChecked){
        JSONObject status = new JSONObject();
        try {
            if(isChecked)
                status.put("Status", 1);
            else
                status.put("Status", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("APP_ESP", status);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btnAdd:
                AddChannel();
                adapter.notifyDataSetChanged();
                break;
            case R.id.btnSortName:
                Collections.sort(arrayChannel, ComparatorNameChannel);
                adapter.notifyDataSetChanged();
                saveChannel.WriteChannel(arrayChannel);
                break;
            case R.id.btnSortNum:
                Collections.sort(arrayChannel, ComparatorNumChannel);
                adapter.notifyDataSetChanged();
                saveChannel.WriteChannel(arrayChannel);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //onClick Add menu
    public void AddChannel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TiviActivity.this);
        final View customLayout = getLayoutInflater().inflate(R.layout.add_dialog, null);
        builder.setView(customLayout);
        final AlertDialog dialog = builder.create();
        //Ánh xạ
        final EditText tenKenh = (EditText) customLayout.findViewById(R.id.edtTenKenh);
        final EditText soKenh = (EditText) customLayout.findViewById(R.id.edtSoKenh);
        final ImageView imageIconAddDialog = (ImageView) customLayout.findViewById(R.id.imageAddDialog);
        Button selectIconAddDialog = (Button) customLayout.findViewById(R.id.btnIconAddDialog);
        Button addChannel = (Button) customLayout.findViewById(R.id.btnAddDialog);
        imageIconAddDialog.setImageResource(R.drawable.tivi);
        //Sự kiện ấn Thêm
        addChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kenh = tenKenh.getText().toString().trim().toUpperCase();
                String so = soKenh.getText().toString().trim().toUpperCase();
                if (kenh.equals("") || so.equals(""))
                    Toast.makeText(TiviActivity.this, "Nhập thông tin kênh", Toast.LENGTH_SHORT).show();
                else if (SearchChannel(kenh, so, arrayChannel))
                    Toast.makeText(TiviActivity.this, "Kênh đã tồn tại", Toast.LENGTH_SHORT).show();
                else {
                    arrayChannel.add(new Kenh(kenh, so, idIconAdded));
                    idIconAdded = idIcon[0];
                    saveChannel.WriteChannel(arrayChannel); //save channel
                    SendNumberChannelToServer();
                    dialog.dismiss();
                }
            }
        });
        //Su kien khi an SelectIcon
        selectIconAddDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelecIconAddDialog(imageIconAddDialog);
            }
        });
        dialog.show();
    }

    //Display add Icon on Dialog
    public void SelecIconAddDialog(final ImageView imageView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TiviActivity.this);
        final View customLayout = getLayoutInflater().inflate(R.layout.activity_icon, null);
        builder.setView(customLayout);
        final AlertDialog dialog = builder.create();
        IconAdapter adapter = new IconAdapter(TiviActivity.this, idIcon);
        GridView grid = (GridView) customLayout.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                idIconAdded = idIcon[position];
                imageView.setImageResource(idIconAdded);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //Display update icon on Dialog
    public void SelecIconUpdateDialog(final ImageView imageView, final int positionArrayList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TiviActivity.this);
        final View customLayout = getLayoutInflater().inflate(R.layout.activity_icon, null);
        builder.setView(customLayout);
        final AlertDialog dialog = builder.create();
        IconAdapter adapter = new IconAdapter(TiviActivity.this, idIcon);
        GridView grid = (GridView) customLayout.findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                arrayChannel.set(positionArrayList, new Kenh(arrayChannel.get(positionArrayList).nameChannel,
                        arrayChannel.get(positionArrayList).numberChannel,
                        idIcon[position]));
                imageView.setImageResource(idIcon[position]);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //Sort name channel
    public static Comparator<Kenh> ComparatorNameChannel = new Comparator<Kenh>() {
        public int compare(Kenh app1, Kenh app2) {
            Kenh channel1 = app1;
            Kenh channel2 = app2;
            return channel1.nameChannel.compareTo(channel2.nameChannel);
        }
    };
    //Sort number channel
    public static Comparator<Kenh> ComparatorNumChannel = new Comparator<Kenh>() {
        public int compare(Kenh app1, Kenh app2) {
            Kenh channel1 = app1;
            Kenh channel2 = app2;
            return Integer.valueOf(channel1.numberChannel).compareTo(Integer.valueOf(channel2.numberChannel));
        }
    };

    //Put channel selected on SelectChannel array
    public void SlectChannel(int position) {
        if (arrayChannel.get(position).isChecked()) {
            arrayChannel.get(position).setCheck(false);
            selectedChannel.remove(arrayChannel.get(position));
        } else {
            arrayChannel.get(position).setCheck(true);
            selectedChannel.add(arrayChannel.get(position));
        }
        adapter.notifyDataSetChanged();
    }

    //Update channel when click on listview
    public void UpdateChannel(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TiviActivity.this);
        View customLayout = getLayoutInflater().inflate(R.layout.update_dialog, null);
        builder.setView(customLayout);
        final AlertDialog dialog = builder.create();
        //Ánh xạ
        final EditText tenKenh = (EditText) customLayout.findViewById(R.id.edtTenKenh);
        final EditText soKenh = (EditText) customLayout.findViewById(R.id.edtSoKenh);
        Button update = (Button) customLayout.findViewById(R.id.btnUpdate);
        final ImageView imageIconUpdateDialog = (ImageView) customLayout.findViewById(R.id.imageUpdateDialog);
        Button selectIconUpdateDialog = (Button) customLayout.findViewById(R.id.btnIconupdateDialog);
        tenKenh.setText(arrayChannel.get(position).nameChannel);
        soKenh.setText(arrayChannel.get(position).numberChannel);
        imageIconUpdateDialog.setImageResource(arrayChannel.get(position).idImage);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String kenh = tenKenh.getText().toString().trim().toUpperCase();
                String so = soKenh.getText().toString().trim().toUpperCase();
                if (kenh.equals("") || so.equals(""))
                    Toast.makeText(TiviActivity.this, "Nhập thông tin kênh", Toast.LENGTH_SHORT).show();
                else if (SearchChannelExceptPosition(kenh, so, arrayChannel, position))
                    Toast.makeText(TiviActivity.this, "Kênh đã tồn tại", Toast.LENGTH_SHORT).show();
                else {
                    arrayChannel.set(position, new Kenh(kenh, so, arrayChannel.get(position).idImage));
                    adapter.notifyDataSetChanged();
                    saveChannel.WriteChannel(arrayChannel);//save channel
                    SendNumberChannelToServer();
                    dialog.dismiss();
                }
            }
        });
        selectIconUpdateDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelecIconUpdateDialog(imageIconUpdateDialog, position);
            }
        });
        dialog.show();
    }

    //Cilck on Voice button
    public void VoiceClick(View v) {
        Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Ra lệnh để điều khiển tivi");
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");
        startActivityForResult(voiceIntent, REQ_CODE_SPEECH_INPUT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    try {
                        processResult(result.get(0));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        }
    }

    //Ham tim kiem kenh
    private boolean SearchChannel(String nameChannel, String numberChannel, ArrayList<Kenh> arrayList) {
        for (int i = 0; i < arrayList.size(); i++)
            if (numberChannel.equalsIgnoreCase(arrayList.get(i).numberChannel) ||
                    nameChannel.equalsIgnoreCase(arrayList.get(i).nameChannel))
        return true;
        return false;
    }

    //Ham tim kiem kenh loai tru mot vi tri
    private boolean SearchChannelExceptPosition(String nameChannel, String numberChannel, ArrayList<Kenh> arrayList, int position) {
        int i = 0;
        while (i < arrayList.size() && i != position) {
            if (numberChannel.equalsIgnoreCase(arrayList.get(i).numberChannel) ||
                    nameChannel.equalsIgnoreCase(arrayList.get(i).nameChannel))
                return true;
            i++;
        }
        return false;
    }

    //Ham put so kenh trong arrayChannel sang arrayJson va gui len server
    private void SendNumberChannelToServer(){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < arrayChannel.size(); i++){
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("value", arrayChannel.get(i).numberChannel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
        //Emit len server
        mSocket.emit("CHANNEL", jsonArray);
    }

    //Xu ly data voice
    private void processResult(String textResult) throws JSONException {
        String textResultUpCase = textResult.trim().toUpperCase();
        Toast.makeText(this, "" + textResultUpCase, Toast.LENGTH_SHORT).show();
        JSONObject data = new JSONObject();
        data.put("remote", getString(R.string.TCL_TV));
        switch (textResultUpCase) {
            case "TĂNG ÂM LƯỢNG":
                data.put("code", getString(R.string.volume_up));
                mSocket.emit(getString(R.string.app_emit), data);
                break;
            case "GIẢM ÂM LƯỢNG":
                data.put("code", getString(R.string.volume_down));
                mSocket.emit(getString(R.string.app_emit), data);
                break;
            case "TĂNG KÊNH":
                data.put("code", getString(R.string.channel_up));
                mSocket.emit(getString(R.string.app_emit), data);
                break;
            case "GIẢM KÊNH":
                data.put("code", getString(R.string.channel_down));
                mSocket.emit(getString(R.string.app_emit), data);
                break;
            case "BẬT TIVI":
                data.put("code", getString(R.string.power));
                mSocket.emit(getString(R.string.app_emit), data);
                break;
            case "TẮT TIVI":
                data.put("code", getString(R.string.power));
                mSocket.emit(getString(R.string.app_emit), data);
                break;
            case "KÊNH":
                break;
            default:
                //Find index of "KENH" in textResultUpCase
                int lastIndex = textResultUpCase.lastIndexOf("KÊNH");
                //Get name channel in textResultUpCase
                String resultChannel = textResultUpCase.substring(lastIndex + 5);
                int resultIndex = SearchIndexChannel(resultChannel, arrayChannel);
                if (resultIndex != NOT_FIND) {
                    data.put("code", arrayChannel.get(resultIndex).numberChannel);
                    mSocket.emit(getString(R.string.app_emit), data);
                    Toast.makeText(this, "OK, bật kênh " + resultChannel, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Không tìm thấy kênh", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Find name channel or number channel in arrayList
     * Return NOT_FIND if not find
     */
    private int SearchIndexChannel(String channel, ArrayList<Kenh> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (channel.equals(arrayList.get(i).nameChannel)
                    || channel.equals(arrayList.get(i).numberChannel))
                return i;
        }
        return NOT_FIND;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSocket != null)
            mSocket.disconnect();
    }
}