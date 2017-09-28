package com.example.cc_android.mybluetoothlbe;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String UUID_KEY_DATA = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private final static int MSG_ASSETNUM = 10001;//子线程返回资产编号

    private ListView lv;
    private Button btnStart;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeHelper mBLE;
    private String TAG = "MainActivity------>";
    private Handler mHandler;
    private boolean mScanning;

    private LeDeviceListAdapter mLeDeviceListAdapter;
    private static final long SCAN_PERIOD = 10000;
    private AlertDialog alertDialog;
    private List<String> mlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                int id = msg.what;
                String assetNum = (String) msg.obj;
                switch (id){
                    case MSG_ASSETNUM:
                        boolean isHave = false;
                        for (String str : mlist){
                            if (str.equals(assetNum)){
                                isHave = true;
                            }
                        }
                        if (!isHave)
                            mlist.add((String) msg.obj);

                        lv.setAdapter(new MyBaseAdapter());
                        break;
                }

            }
        };
        lv = (ListView) findViewById(R.id.lv);
        btnStart = (Button) findViewById(R.id.btn_start);

        //检查设备是否支持BLE功能
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "抱歉，您的设备不支持BLE功能！", Toast.LENGTH_SHORT).show();
            finish();
        }

        //检获取蓝牙适配器
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //检查设备是否支持蓝牙功能
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "抱歉，您的设备不支持蓝牙功能！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //开启蓝牙
        mBluetoothAdapter.enable();

        mBLE = new BluetoothLeHelper(this);
        if (!mBLE.initialize()) {
            Log.e(TAG, "无法初始化蓝牙");
            finish();
        }

        //发现BLE终端的Service时回调
        mBLE.setOnServiceDiscoverListener(mOnServiceDiscover);
        //收到BLE终端数据交互的事件
        mBLE.setOnDataAvailableListener(mOnDataAvailable);
        //BLE断开连接
        mBLE.setOnDisconnectListener(mOnDisconnectListener);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mlist.clear();
                lv.setAdapter(new MyBaseAdapter());
                actionAlertDialog();
                scanLeDevice(true);
            }
        });

    }

    protected void actionAlertDialog(){
        AlertDialog.Builder builder;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.device_listview, (ViewGroup)findViewById(R.id.lv_alert));
        ListView myListView = (ListView) layout.findViewById(R.id.lv_alert);
        mLeDeviceListAdapter.clear();
        myListView.setAdapter(mLeDeviceListAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(i);
                if (device == null) return;
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }

                mBLE.connect(device.getAddress());

                alertDialog.hide();
            }
        });
        builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(this);
        mlist = new ArrayList<String>();
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
        mBLE.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBLE.close();
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        //invalidateOptionsMenu();
    }

    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeHelper.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeHelper.OnServiceDiscoverListener(){

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLE.getSupportedGattServices());
        }
    };

    /**
     * 搜索到BLE终端服务的事件
     */
    private BluetoothLeHelper.OnDisconnectListener mOnDisconnectListener = new BluetoothLeHelper.OnDisconnectListener() {
        @Override
        public void onDisconnect(BluetoothGatt gatt) {
            Log.i(TAG, "连接已断开！！！");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "连接已断开请重新连接！", Toast.LENGTH_SHORT).show();
                    actionAlertDialog();
                    scanLeDevice(true);
                }
            });
        }
    };

    /**
     * 收到BLE终端数据交互的事件
     */
    private BluetoothLeHelper.OnDataAvailableListener mOnDataAvailable = new BluetoothLeHelper.OnDataAvailableListener(){

        /**
         * BLE终端数据被读的事件
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)
                Log.e(TAG,"onCharRead "+gatt.getDevice().getName()
                        +" read "
                        +characteristic.getUuid().toString()
                        +" -> "
                        +Utils.bytesToHexString(characteristic.getValue()));
        }

        /**
         * 收到BLE终端写入数据回调
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            Log.e(TAG,"onCharWrite "+gatt.getDevice().getName()
                    +" write "
                    +characteristic.getUuid().toString()
                    +" -> "
                    +new String(characteristic.getValue()));

            String str = printHexString(characteristic.getValue());
            Log.i(TAG, "资产编号：" + str);
            Message msg = Message.obtain();// 从消息池中或消息
            msg.obj = str;
            msg.what = MSG_ASSETNUM;
            mHandler.sendMessage(msg);
        }
    };

    //将指定byte数组以转换为资产编号返回
    public String printHexString(byte[] b) {
        String assetNum = "";
        String[] strings = new String[16];
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            Log.i(TAG, "b["+ i +"]**********"+ hex.toUpperCase());
            strings[i] = hex.toUpperCase();
        }
        assetNum = strings[7] + strings[6] + strings[5] + strings[4];
        return assetNum;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        for (BluetoothGattService gattService : gattServices) {
            //-----Service的字段信息-----//
            int type = gattService.getType();
            Log.e(TAG,"-->service type:"+Utils.getServiceType(type));
            Log.e(TAG,"-->includedServices size:"+gattService.getIncludedServices().size());
            Log.e(TAG,"-->service uuid:"+gattService.getUuid());

            //-----Characteristics的字段信息-----//
            List<BluetoothGattCharacteristic> gattCharacteristics =gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic  gattCharacteristic: gattCharacteristics) {
                Log.e(TAG,"---->char uuid:"+gattCharacteristic.getUuid());

                int permission = gattCharacteristic.getPermissions();
                Log.e(TAG,"---->char permission:"+Utils.getCharPermission(permission));

                int property = gattCharacteristic.getProperties();
                Log.e(TAG,"---->char property:"+Utils.getCharPropertie(property));

                byte[] data = gattCharacteristic.getValue();
                if (data != null && data.length > 0) {
                    Log.e(TAG,"---->char value:"+new String(data));
                }

                //UUID_KEY_DATA是可以跟蓝牙模块串口通信的Characteristic
                if(gattCharacteristic.getUuid().toString().equals(UUID_KEY_DATA)){
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBLE.readCharacteristic(gattCharacteristic);
                        }
                    }, 500);

                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLE.setCharacteristicNotification(gattCharacteristic, true);
                    //设置数据内容
                    gattCharacteristic.setValue("send data->");
                    //往蓝牙模块写入数据
                    mBLE.writeCharacteristic(gattCharacteristic);
                }

                //-----Descriptors的字段信息-----//
                List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                    Log.e(TAG, "-------->desc uuid:" + gattDescriptor.getUuid());
                    int descPermission = gattDescriptor.getPermissions();
                    Log.e(TAG,"-------->desc permission:"+ Utils.getDescPermission(descPermission));

                    byte[] desData = gattDescriptor.getValue();
                    if (desData != null && desData.length > 0) {
                        Log.e(TAG, "-------->desc value:"+ new String(desData));
                    }
                }
            }
        }//

    }

    private class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int i) {
            return mlist.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View mView = null;
            if (view == null){
                mView = getLayoutInflater().inflate(R.layout.list_item, null);
            }else{
                mView = view;
            }

            TextView tvDeviceName = mView.findViewById(R.id.tv_deviceName);
            TextView tvAssetNum = mView.findViewById(R.id.tv_assetNum);

            tvDeviceName.setText("设备" + i + ":");
            tvAssetNum.setText("资产编号：" + getItem(i));

            return mView;
        }
    }
}


