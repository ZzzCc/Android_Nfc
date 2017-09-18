package com.example.zhangcc_nfc.my_nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;

/**
 * Created by zhangcc2 on 2017/9/18.
 */

public class MyNfc{

    public String[][] nfcData(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.i("Nfc", "ACTION_NDEF_DISCOVERED");
        }else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
            Log.i("Nfc", "ACTION_TECH_DISCOVERED");
            return resolveTechIntent(intent);
        }else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            Log.i("Nfc", "ACTION_TAG_DISCOVERED");
            return resolveTechIntent(intent);
        }else{
            Log.i("Nfc", "非标准");
        }
        return null;
    }

    //非NDEF类型标签的，数据处理方法
    private String[][] resolveTechIntent(Intent intent){
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        boolean isAuth = false;
        if(supportedTechs(tag.getTechList())){
            MifareClassic mfc = MifareClassic.get(tag);
            if (mfc != null){
                try {
                    mfc.connect();
                    int sectorCount = mfc.getSectorCount();
                    //扇形区的数量
                    Log.i("Nfc", "sectorCount:" + sectorCount);
                    for (int i = 0; i < sectorCount; i++){
                        if (mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)){
                            Log.i("Nfc", "sectorCount["+i+"]");
                            isAuth = true;
                        }else {
                            isAuth = false;
                        }
                        if (isAuth){
                            int nBlock = mfc.getBlockCountInSector(i);
                            //数据块的数量
                            Log.i("Nfc", "nBlock = " + nBlock);
                            String[][] strs = new String[4][16];
                            for (int j = 0; j < nBlock; j++){
                                byte[] data = mfc.readBlock(j);
                                strs[j] = printHexString(j, data);
                            }

                            for (int j = 0; j < strs.length; j++){
                                for (int n = 0; n < strs[j].length; n++){
                                    Log.i("Nfc", "strs["+j+"]["+n+"] = " + strs[j][n]);
                                }
                            }
                            return strs;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Log.i("Nfc", "您的标签不匹配");
            }
        }
        return null;
    }

    private boolean supportedTechs(String[] techList) {
        boolean isSupport = false;
        for (String s : techList){
            Log.i("Nfc","All SupportedTechs:" + s);
        }
        for (String s : techList){
            if (s.equals("android.nfc.tech.MifareClassic")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.MifareClassic");
            }else if (s.equals("android.nfc.tech.MifareUltralight")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.MifareUltralight");
            }else if (s.equals("android.nfc.tech.Ndef")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.Ndef");
            }else if (s.equals("android.nfc.tech.IsoDep")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.IsoDep");
            }else if (s.equals("android.nfc.tech.NdefFormatable")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.NdefFormatable");
            }else if (s.equals("android.nfc.tech.NfcA")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.NfcA");
            }else if (s.equals("android.nfc.tech.NfcB")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.NfcB");
            }else if (s.equals("android.nfc.tech.NfcF")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.NfcF");
            }else if (s.equals("android.nfc.tech.NfcV")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.NfcV");
            }else if (s.equals("android.nfc.tech.TagTechnology")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.TagTechnology");
            }else if (s.equals("android.nfc.tech.NfcBarcode")){
                isSupport = true;
                Log.i("Nfc","android.nfc.tech.NfcBarcode");
            }
        }

        return isSupport;
    }

    //将指定byte数组以16进制的形式打印到控制台
    public String[] printHexString(int n, byte[] b) {
//        for(int i = 0; i < b.length; i++){
//            Log.i("Nfc", b[i] + "");
//        }
        String[] strs = new String[16];
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            strs[i] = hex.toUpperCase();
            //Log.i("Nfc", n +"-----"+ hex.toUpperCase());
        }
        return strs;
    }
}
