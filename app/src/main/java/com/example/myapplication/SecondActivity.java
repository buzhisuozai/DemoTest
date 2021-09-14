package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView text;
    private BaiduMap baiduMap;
    private  boolean si=true;
    public LocationClient locationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationClient=new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(this.getApplicationContext());

        setContentView(R.layout.activity_second);
        text=(TextView)findViewById(R.id.text);
        mapView=(MapView)findViewById(R.id.bdmap);
        baiduMap=mapView.getMap();
        Button rrn = (Button) findViewById(R.id.rrn);

       // rrn.setOnClickListener(this);

        List<String> permissionList=new ArrayList<>();
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        if (ContextCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(SecondActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(SecondActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    private void requestLocation() {
        initLocation();
        locationClient.start();
    }

    private void initLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(5000);
        locationClient.setLocOption(option);
    }

    private void navigateTo(BDLocation location){
        if (si){
            LatLng l=new LatLng(location.getLatitude(),location.getLongitude());
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(l);
            baiduMap.animateMapStatus(update);
            update= MapStatusUpdateFactory.zoomBy(16f);
            baiduMap.animateMapStatus(update);
            si=false;
        }
        MyLocationData.Builder locationBuilder =new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
        mapView.onDestroy();
    baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results){
        switch (requestCode){
            case 1:
                if (results.length>0){
                    for (int result :results){
                        if (result !=PackageManager.PERMISSION_GRANTED){
                            Log.d("SecondActivity","必须同意权限才可使用本应用");
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Log.d("SecondActivity","必须同意权限才可使用本应用");
                    finish();
                }
                break;
            default:
        }
    }

    class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()
                    ==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }


/*
            StringBuilder rightposition=new StringBuilder();
            rightposition.append("纬度:").append(location.getLatitude()).append("\n");
            rightposition.append("经线:").append(location.getLongitude()).append("\n");
            rightposition.append("定位方式:");
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                rightposition.append("GPS");
            }
            else if (location.getLocType()== BDLocation.TypeNetWorkLocation){
                rightposition.append("网络");
            }
            text.setText(rightposition);

 */
        }
    }

}

