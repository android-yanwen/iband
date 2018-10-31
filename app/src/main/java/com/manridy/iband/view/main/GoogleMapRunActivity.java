package com.manridy.iband.view.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.maps2d.CoordinateConverter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.manridy.iband.IbandApplication;
import com.manridy.iband.IbandDB;
import com.manridy.iband.R;
import com.manridy.iband.bean.data.RunLocationModel;
import com.manridy.iband.map.LocationUtil;
import com.manridy.iband.service.LocationService;
import com.manridy.iband.view.base.BaseActionActivity;
import com.manridy.sdk.common.TimeUtil;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GoogleMapRunActivity  extends BaseActionActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener  {

    private GoogleMap mMap;

    @BindView(R.id.iv_map_close)
    ImageView ivMapClose;
    @BindView(R.id.tv_exercise_time)
    TextView tv_exercise_time;
    @BindView(R.id.tv_pace)
    TextView tv_pace;
    @BindView(R.id.tv_distance)
    TextView tv_distance;

    LocationService.LocationServiceBinder locationServiceBinder;
    private String stepDate;
    private List<RunLocationModel> runLocationModels;
    private List<LatLng> latLngs;

    boolean isGoogleMapReady = false;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_googlemap);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = new Intent(GoogleMapRunActivity.this,LocationService.class);
        bindService(intent,locationServiceConnection,BIND_AUTO_CREATE);
    }


    private ServiceConnection locationServiceConnection = new  ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            locationServiceBinder = (LocationService.LocationServiceBinder)service;
//            locationServiceBinder.startCheckSatellites(true);
//            locationServiceBinder.startLocation();
            if(locationServiceBinder.getIsRunning()){
                stepDate = TimeUtil.getYMDHMSTime(locationServiceBinder.getRunRecordDate());
                if(stepDate!=null&&!"".equals(stepDate)) {
                    handler.post(initRunLocationModels);
                }
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @OnClick({R.id.iv_map_close})
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_map_close:
                finish();
                break;
        }
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
//        float latitude = (float) SPUtil.get(App.getInstance().getApplicationContext(), AppGlobal.DATA_LOCATION_LATITUDE,0.0);
//        float longitude = (float) SPUtil.get(App.getInstance().getApplicationContext(), AppGlobal.DATA_LOCATION_LONGITUDE,0.0);

//        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("Marker"));
        isGoogleMapReady = true;
        mMap = googleMap;
        //添加标记
//        googleMap.addMarker(new MarkerOptions().position(new LatLng(App.location_latitude, App.location_longitude))
//                .title("Marker in Googleplex"));
        //移动摄像头
        googleMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(new LatLng(IbandApplication.location_latitude, IbandApplication.location_longitude), 18));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            mMap.setMyLocationEnabled(true);
//            return;
        }
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(false);
        mMap.setTrafficEnabled(true);
        mMap.setIndoorEnabled(true);
//        enableMyLocation();

    }

    @Override
    public void onResume() {
        super.onResume();
        Message message = handler.obtainMessage();
        message.what = 4;
        handler.sendMessageDelayed(message,1000);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    static MarkerOptions markerOptions;
    /**绘制两个坐标点之间的线段,从以前位置到现在位置*/
    private void setUpMap(LatLng oldData, LatLng newData,boolean isInCN ) {
        if(mMap==null)return;
        if(isInCN){
            com.amap.api.maps2d.model.LatLng aMapOldData = new com.amap.api.maps2d.model.LatLng(oldData.latitude,oldData.longitude);
            com.amap.api.maps2d.model.LatLng aMapnewData = new com.amap.api.maps2d.model.LatLng(newData.latitude,newData.longitude);

            CoordinateConverter converter  = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(aMapOldData);
            com.amap.api.maps2d.model.LatLng oldDesLatLng = converter.convert();
            LatLng googleOldLatLng = new LatLng(oldDesLatLng.latitude,oldDesLatLng.longitude);

            CoordinateConverter converter1  = new CoordinateConverter();
            converter1.from(CoordinateConverter.CoordType.GPS);
            converter1.coord(aMapnewData);
            com.amap.api.maps2d.model.LatLng newDesLatLng = converter1.convert();
            LatLng googleNewLatLng = new LatLng(newDesLatLng.latitude,newDesLatLng.longitude);

            // 绘制一个大地曲线
            mMap.addPolyline((new PolylineOptions())
                .add(googleOldLatLng, googleNewLatLng)
                .geodesic(true).color(Color.RED));
//            if(markerOptions == null){
//                markerOptions = new MarkerOptions();
//                markerOptions.position(googleNewLatLng);
//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.train_ic02)));
//                mMap.addMarker(markerOptions);
//            }
//                markerOptions.position(googleNewLatLng);
            mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(googleNewLatLng, mMap.getCameraPosition().zoom));
        }else{
            mMap.addPolyline((new PolylineOptions())
                    .add(oldData, newData)
                    .geodesic(true).color(Color.RED));

//            if(markerOptions == null){
//                markerOptions = new MarkerOptions();
//                markerOptions.position(newData);
//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.train_ic02)));
//                mMap.addMarker(markerOptions);
//            }
//            markerOptions.position(newData);
//            markerOptions.notify();
            mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(newData, mMap.getCameraPosition().zoom));
        }




    }
    private void setUpMap(List<LatLng> latLngs,boolean isInCN) {
        if(mMap==null)return;
          if(isInCN){
              List<LatLng> googleLatLngs = new ArrayList<>();

              for(LatLng latLng:latLngs){

                  com.amap.api.maps2d.model.LatLng aMapOldData = new com.amap.api.maps2d.model.LatLng(latLng.latitude,latLng.longitude);

                  CoordinateConverter converter  = new CoordinateConverter();
                  converter.from(CoordinateConverter.CoordType.GPS);
                  converter.coord(aMapOldData);
                  com.amap.api.maps2d.model.LatLng oldDesLatLng = converter.convert();
                  LatLng googleOldLatLng = new LatLng(oldDesLatLng.latitude,oldDesLatLng.longitude);
                  googleLatLngs.add(googleOldLatLng);

              }
              if(mMap!=null){
                  mMap.addPolyline((new PolylineOptions()).addAll(googleLatLngs)
                          .geodesic(true).color(Color.RED));

              }

          }else{
              if(mMap!=null){
                  mMap.addPolyline((new PolylineOptions()).addAll(latLngs)
                          .geodesic(true).color(Color.RED));

              }
          }

//        CoordinateConverter converter  = new CoordinateConverter();
//        converter.from(CoordinateConverter.CoordType.GPS);
//        converter.coord(oldData);
//        LatLng oldDesLatLng = converter.convert();

//        CoordinateConverter converter1  = new CoordinateConverter();
//        converter1.from(CoordinateConverter.CoordType.GPS);
//        converter1.coord(newData);
//        LatLng newDesLatLng = converter1.convert();




    }


    Runnable initRunLocationModels = new Runnable() {
        @Override
        public void run() {
            runLocationModels = IbandDB.getInstance().getRunLocationData(stepDate);
            latLngs = new ArrayList<LatLng>();
            String locationData;
            String[] locations;
            String[] dots;
            LatLng latLng;
            for(RunLocationModel runLocationModel : runLocationModels) {
                locationData = runLocationModel.getLocationData();
                locations = locationData.split(";");
                for(String location:locations){
                    dots = location.split(",");
                    latLng =new LatLng(Double.parseDouble(dots[0]),Double.parseDouble(dots[1]));
//                    CoordinateConverter converter  = new CoordinateConverter();
//                    converter.from(CoordinateConverter.CoordType.GPS);
//                    converter.coord(latLng);
//                    com.amap.api.maps2d.model.LatLng newLatLng = converter.convert();
//                    latLngs.add(newLatLng);
                    latLngs.add(latLng);
                }

            }
            Message message1 = handler.obtainMessage();
            message1.what = 1;
            handler.sendMessage(message1);
            Message message2 = handler.obtainMessage();
            message2.what = 2;
            handler.sendMessage(message2);
            Message message3 = handler.obtainMessage();
            message3.what = 3;
            handler.sendMessage(message3);

        }
    };

    DecimalFormat df=new DecimalFormat("0.00");
    SimpleDateFormat sdf= new SimpleDateFormat("HH:mm:ss");
    Location oldLocation = null;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if(oldLocation == null){
                        oldLocation = LocationUtil.getInstance(getApplication()).getNowLocation();
                    }else{
                        LatLng oldLatLng = new LatLng(oldLocation.getLatitude(),oldLocation.getLongitude());

                        Location newLocation = LocationUtil.getInstance(getApplication()).getNowLocation();
                        LatLng newLatLng = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());
                        boolean isInCN = locationServiceBinder.getIsInCN();
                        setUpMap(oldLatLng,newLatLng,isInCN);
                        oldLocation = LocationUtil.getInstance(getApplication()).getNowLocation();
//                        Toast.makeText(getApplication(),""+newLocation.getLongitude()+":"+newLocation.getLatitude(),Toast.LENGTH_SHORT).show();
                        Log.i(TAG,""+newLocation.getLongitude()+":"+newLocation.getLatitude());
                    }
//                    Message message = handler.obtainMessage();
//                    message.what = 1;
//                    handler.sendMessageDelayed(message,8);
                    handler.sendEmptyMessageDelayed(1, 3000);
                    break;
                case 2:
                    boolean isInCN = locationServiceBinder.getIsInCN();
                    if(latLngs.size()>0) {
                        setUpMap(latLngs, isInCN);
                    }
                    break;
                case 3:
                    tv_exercise_time.setText(locationServiceBinder.getRunningTime());

                    String str_distance_km = df.format((float)locationServiceBinder.getRunDistance()/1000);
                    tv_distance.setText(str_distance_km);

                    try {
                        if(locationServiceBinder.getRunDistance()>0) {
                            Date runningTime = sdf.parse(locationServiceBinder.getRunningTime());
                            Date zeroTime = sdf.parse("00:00:00");
                            long runningTime_s = (runningTime.getTime() - zeroTime.getTime()) / (1000);
                            double d_pace = runningTime_s / (locationServiceBinder.getRunDistance() / 1000);

                            int minutes = ((int) d_pace) / 60;
                            int remainingSeconds = ((int) d_pace) % 60;

                            String str_pace;
                            if(remainingSeconds<10){
                                str_pace = "" + minutes + ":0" + remainingSeconds;
                            }else{
                                str_pace = "" + minutes + ":" + remainingSeconds;
                            }

                            tv_pace.setText(str_pace);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    handler.removeMessages(3);
                    Message message = handler.obtainMessage();
                    message.what = 3;
                    handler.sendMessageDelayed(message,1000);


                    break;
                case 4:
                    if(!isGoogleMapReady) {
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GoogleMapRunActivity.this);
                        builder.setTitle(R.string.notifyTitle).setMessage(R.string.hint_not_support_google_map);
                        builder.setPositiveButton(R.string.hint_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(SportFunctionActivity.class);
                                finish();
                            }
                        });
                        builder.show();
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(locationServiceConnection);
    }


    @Override
    public void scrollToFinishActivity() {
    }

    @Override
    protected void initBack() {
    }
}
