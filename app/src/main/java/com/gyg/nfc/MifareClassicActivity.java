package com.gyg.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by gyg on 2017/6/5.
 */
public class MifareClassicActivity extends BaseNfcActivity implements View.OnClickListener{

    private static final String TAG = "MI";
    private EditText sectorNum;//扇区
    private EditText blockNum;//块
    private EditText writeData;//写入数据
    private EditText readData;//读出的数据
    private TextView tvClassic;//标签类型
    private TextView tvCurrentClassic;//当前标签类型
    //医生
    private String[] uidStrDr = new String[]{
            "6EB00ABD",
            "BEFE07BD",
            "350B29C9",
            "D5AC27C9"};
    //护士
    private String[] uidStrNurse = new String[]{"AC4EC9A0",
                                            "A5F635C9"};

    boolean isMafire = false;
    boolean is15693 = false;

    private boolean haveMifareClissic=false;//标签是否支持MifareClassic

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_mifareclassic);
        sectorNum= (EditText) findViewById(R.id.sector_num);
        blockNum= (EditText) findViewById(R.id.block_num);
        writeData= (EditText) findViewById(R.id.write_data);
        readData= (EditText) findViewById(R.id.read_data);
        tvClassic= (TextView) findViewById(R.id.tvClassic);
        tvCurrentClassic= (TextView) findViewById(R.id.tvCurrentClassic);
        findViewById(R.id.write_bn).setOnClickListener(this);
        findViewById(R.id.read_bn).setOnClickListener(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mTag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] techList=mTag.getTechList();
        Log.e(TAG, "onNewIntent支持的technology类型："+techList);
        for (String tech:techList){
            Log.e(TAG, "onNewIntent支持的tech："+tech);
            if (tech.indexOf("MifareClassic")>0){
                haveMifareClissic=true;
            }
            Log.e(TAG, "onNewIntent haveMifareClissic："+haveMifareClissic);
        }

        //Get card UID
        String id = NFCUtil.byte2HexString(MifareClassic.get((Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)).getTag().getId());
        //获取标签类型
        tvClassic.setText("");
        for (String tech : techList) {
            tvClassic.append(tech + "\n");
            if ("android.nfc.tech.MifareClassic".equals(tech)) {
                isMafire = true;
            }
            if ("android.nfc.tech.NfcV".equals(tech)) {
                is15693 = true;
            }
        }

        if (isMafire){
            tvCurrentClassic.setText("M1卡-android.nfc.tech.MifareClassic");
        }

        if (is15693){
            tvCurrentClassic.setText("ISO/IEC 15693协议标准的高频RFID无源IC卡");
        }



        Toast.makeText(this, "(Uid = " + id + ")", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Uid = "+id);

        //医生
        for (String s : uidStrDr) {
            if (s.equals(id)){
                Toast.makeText(this, "医生-跳转医生界面(Uid = " + id + ")", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Uid = "+id);
                break;
            }
        }

        //护士
        for (String s : uidStrNurse) {
            if (s.equals(id)){
                Toast.makeText(this, "护士-跳转护士界面(Uid = " + id + ")", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "护士Uid = "+id);
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.write_bn://写块
                writeBlock();
                break;
            case R.id.read_bn://读块
                readBlock();
                break;
        }
    }

    //写块
    private void writeBlock(){
        if (mTag==null){
            Toast.makeText(this,"无法识别的标签！",Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }
        if (!haveMifareClissic){
            Toast.makeText(this,"不支持MifareClassic",Toast.LENGTH_SHORT).show();
//           finish();
            return;
        }
        MifareClassic mfc=MifareClassic.get(mTag);
        try {
            mfc.connect();//打开连接
            boolean auth;
            int sector=Integer.parseInt(sectorNum.getText().toString().trim());//写入的扇区
            int block=Integer.parseInt(blockNum.getText().toString().trim());//写入的块区
            Log.e(TAG, "onNewIntent支持的sector："+sector);
            Log.e(TAG, "onNewIntent支持的block："+block);
//            byte[]  authCode=new byte[]{'f','f','f','f','f','f'};
//            auth=mfc.authenticateSectorWithKeyA(sector,MifareClassic.KEY_NFC_FORUM);//keyA验证扇区
//            auth=mfc.authenticateSectorWithKeyA(sector,MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY);//keyA验证扇区
            auth=mfc.authenticateSectorWithKeyA(sector,MifareClassic.KEY_DEFAULT);//keyA验证扇区
//            auth=mfc.authenticateSectorWithKeyA(sector,authCode);//keyA验证扇区
            String wiroiue = "0123456789012345";
            if (auth){
                mfc.writeBlock(block,wiroiue.getBytes());//写入数据
                Toast.makeText(this,"写块写入成功!",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"写块验证密码失败！",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                mfc.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //读取块
    private void readBlock(){
        if (mTag==null){
            Toast.makeText(this,"无法识别的标签！",Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }
        if (!haveMifareClissic){
            Toast.makeText(this,"不支持MifareClassic",Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }
        MifareClassic mfc = MifareClassic.get(mTag);
        byte[] id = mTag.getId();
        try {
            mfc.connect();//打开连接
            boolean auth;
            int sector=Integer.parseInt(sectorNum.getText().toString().trim());//写入的扇区
            int block=Integer.parseInt(blockNum.getText().toString().trim());//写入的块区
            auth=mfc.authenticateSectorWithKeyA(sector,MifareClassic.KEY_DEFAULT);//keyA验证扇区
            if (auth){
                Log.e(TAG, "bytesToHexString(mfc.readBlock(block):"+bytesToHexString(mfc.readBlock(block)));
                readData.setText(bytesToHexString(mfc.readBlock(block)));
            }else {
                Log.e(TAG, "readBlock 读取块验证密码失败");
                Toast.makeText(this,"读取块验证密码失败！",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                mfc.close();//关闭连接
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //字符序列转换为16进制字符串
    private String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        if (src == null || src.length <= 0) {
            return null;
        }
        char[] buffer = new char[2];
        for (int i = 0; i < src.length; i++) {
            buffer[0] = Character.forDigit((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit(src[i] & 0x0F, 16);
            System.out.println(buffer);
            stringBuilder.append(buffer);
        }
        return stringBuilder.toString();
    }
}
