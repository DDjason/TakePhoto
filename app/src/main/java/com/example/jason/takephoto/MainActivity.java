package com.example.jason.takephoto;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private  static  String Url_post = "http://120.77.84.254:8810/about";
    private  static  String Url_post1 = "http://120.77.84.254:8810/get_result/";
    private  static final  int REQ_1 =1;
    private  static final int REQ_2 =2;
    private  static final int CHOOSE_PHOTO = 3;
    private ImageView imageView;
    private String mFilePath;
    private Uri photoUri;
    private OkHttpClient okHttpClient = new OkHttpClient();

    private String reponse_add_id;
    private String getReponse_result;

    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 2://错误返回
                    Toast.makeText(MainActivity.this,"请选择照片",Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(MainActivity.this,"数据库存储id="+reponse_add_id,Toast.LENGTH_SHORT).show();
                    getresultWithokhttp();
                    break;
                case 4:
                    Toast.makeText(MainActivity.this,"种类="+getReponse_result,Toast.LENGTH_LONG).show();
                    showTheResult(getReponse_result);

                    break;
                case 6:
                    sendRequestWithPkhttp();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.show_image);
        Button button_start_camera = (Button)findViewById(R.id.start_Camera);
        button_start_camera.setOnClickListener(this);
        Button send_photo_http = (Button)findViewById(R.id.send_photo_http);
        send_photo_http.setOnClickListener(this);
        Button choose_photo_local = (Button)findViewById(R.id.choose_photo);
        choose_photo_local.setOnClickListener(this);
        mFilePath = Environment.getExternalStorageDirectory().getPath();
        mFilePath = mFilePath + "/" ;

    }
    private void showTheResult(String idstr){
        int getid = Integer.parseInt(idstr);
        String getid_name = "";
        switch (getid){
            case 1:
                getid_name = "短尾柯";
                break;
            case 2:
                getid_name = "枫香";
                break;
            case 3:
                getid_name = "枸骨";
                break;
            case 4:
                getid_name = "荚莲";
                break;
            case 5:
                getid_name = "罗汉松";
                break;
            case 6:
                getid_name = "日本晚樱";
                break;
            case 7:
                getid_name = "洒金珊瑚";
                break;
            case 8:
                getid_name = "山茶";
                break;
            case 9:
                getid_name = "秃瓣杜芙";
                break;
            case 10:
                getid_name = "樟叶槭";
                break;
            default:
                break;
        }
        TextView textView = (TextView)findViewById(R.id.plantname);
        textView.setText(getid_name);

    }
    public void startCamera(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,REQ_1);
    }


    File fileuse;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_Camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileuse = new File(getExternalCacheDir(),"temp.png");

                try {
                    if(fileuse.exists()){
                        fileuse.delete();
                    }
                    fileuse.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(Build.VERSION.SDK_INT >= 19){
                    photoUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.example.jason.takephoto.fileprovider",fileuse);
                }else{
                    photoUri = Uri.fromFile(fileuse);
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                Log.d("asdsad","1");
                startActivityForResult(intent,REQ_2);
                break;
            case R.id.send_photo_http:

              //  Log.d("sadf","sdaf");
                getThe32pic();


                break;
            case R.id.choose_photo:
                if(ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
                break;
            default:
                break;
        }
    }

    private void getresultWithokhttp(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{

                   // RequestBody requestBody = new FormBody.Builder().build();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(Url_post1+reponse_add_id)
                            .build();
                    Response response = client.newCall(request).execute();


                    String responseData = response.body().string();
                    //execute JSON

                    Log.d("sdfdsf",responseData);
                    String responseJsonData = "["+responseData+"]";
                    parseJSONWithJSONObject_getresult(responseJsonData);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSONWithJSONObject_getresult(String jsonData){
        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0 ; i < jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String judje = jsonObject.getString("judje");
                String requset = jsonObject.getString("request");
                Log.d("judje",judje);
                Log.d("request",requset);
                getReponse_result = requset;
                Message message = new Message();
                message.what = 4;
                handler.sendMessage(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getThe32pic(){

        File file =  new File(mFilePath);//通过文件路径
        if(file.exists()){
            if(file.isDirectory()){
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
                return;
            }
        }
        Bitmap bm = BitmapFactory.decodeFile(mFilePath);
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 设置想要的大小
        int newWidth = 32;
        int newHeight = 32;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        //imageView.setImageBitmap(newbm);
        // 放在画布上

        //命名不规范
        File f = new File(getExternalCacheDir(),"temp2.jpg");

        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            newbm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.i("TAG", "已经保存");
            mFilePath = getExternalCacheDir()+"/temp2.jpg";
            Message message = new Message();
            message.what = 6;
            handler.sendMessage(message);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    private void sendRequestWithPkhttp(){

        File file =  new File(mFilePath);//通过文件路径
        if(!file.exists()){
                return;
        }


        String _token = "HOOl3PzQ6HB7OjWSr9VrOQnXKw3DIfefBT54jfAR";
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);

        requestBody.addFormDataPart("_token",_token)
                .addFormDataPart("photo","upload.jpg",RequestBody.create(MediaType.parse("image/*"),file));

        Request.Builder builder = new Request.Builder();
        Request request = builder.url( Url_post ).post(requestBody.build()).build();

        Call call = okHttpClient.newBuilder()
                .readTimeout(5000, TimeUnit.MILLISECONDS).build().newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("okhttp","发送失败");
                Message message = new Message();
                message.what = 2;
                handler.sendMessage(message);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();
                Log.d("onResponse",responseStr);
                parseJSONWithJSONObject("["+responseStr+"]");
            }
        });
    }


    private void parseJSONWithJSONObject(String jsonData){

        try{
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0 ; i < jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String judje = jsonObject.getString("judje");
                String requset = jsonObject.getString("add_id");
                Log.d("judje",judje);
                Log.d("add_id",requset);
                reponse_add_id = requset;
                Message message = new Message();
                message.what = 3;
                handler.sendMessage(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    private void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && PackageManager.PERMISSION_GRANTED == grantResults[0]){
                    openAlbum();
                }else{
                    Toast.makeText(this,"You denied the permission!",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){

            Bitmap bitmap;
            switch (requestCode){
                case REQ_1:
                    Bundle bundle = data.getExtras();
                     bitmap = (Bitmap) bundle.get("data");
                    mFilePath = getExternalCacheDir()+"/temp.png";
                    Log.d("m",mFilePath);
                    imageView.setImageBitmap(bitmap);
                    break;
                case REQ_2:
                   try {
                         bitmap = BitmapFactory.decodeStream(getContentResolver()
                                .openInputStream(photoUri));
                       mFilePath = getExternalCacheDir()+"/temp.png";
                       Log.d("m",mFilePath);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case CHOOSE_PHOTO:
                    if(Build.VERSION.SDK_INT >= 19){
                        handleImageOnKitKat(data);
                    }else{
                        handleImageBeforeOnKitKat(data);
                    }
                    break;
                default:
                    break;

            }
        }
    }


    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" +id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }

        displayImage(imagePath);
    }

    private void handleImageBeforeOnKitKat(Intent data){

        Uri uri = data.getData();
        String imagePath = getImagePath(uri,null);

        displayImage(imagePath);
    }


    private String getImagePath(Uri uri,String selection){
        String path = null;
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
      //  Toast.makeText(this,imagePath,Toast.LENGTH_LONG).show();
        mFilePath = imagePath;
        if(imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        }else {
            Toast.makeText(this,"failed to get image",Toast.LENGTH_SHORT).show();
        }
    }
}
