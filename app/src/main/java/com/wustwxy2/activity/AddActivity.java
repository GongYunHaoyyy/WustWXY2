package com.wustwxy2.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.wustwxy2.R;
import com.wustwxy2.bean.Found;
import com.wustwxy2.bean.Lost;
import com.wustwxy2.bean.User;
import com.wustwxy2.i.IMainPresenter;
import com.wustwxy2.i.IMainView;
import com.wustwxy2.util.MainPresenter;

import java.io.File;
import java.io.FileNotFoundException;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadFileListener;

public class AddActivity extends BaseActivity implements View.OnClickListener, IMainView {

    private static final int SELECT_PIC_KITKAT = 0x11;
    private static final int SELECT_PIC= 0x12;
    private SystemBarTintManager tintManager;
    private IMainPresenter mMainPresenter;

    //����д�ĸ���
    EditText edit_title, edit_phone, edit_describe;
    //���ذ�ť��ȷ����ť
    Button btn_back, btn_true;
    //��Ҫ�ϴ���ͼƬ
    ImageView iv_photo;
    private static Bitmap bitmapSelected;
    TextView tv_add;
    //�����������Ի������
    private ProgressDialog dialog;

    String from = "";

    String old_title = "";
    String old_describe = "";
    String old_phone = "";

    String path="";

    @Override
    public void setContentView() {
        // TODO Auto-generated method stub
        setContentView(R.layout.activity_add);
    }

    @Override
    public void initViews() {
        // TODO Auto-generated method stub
        tv_add = (TextView) findViewById(R.id.tv_add);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_true = (Button) findViewById(R.id.btn_true);
        edit_phone = (EditText) findViewById(R.id.losing_add_phone);
        edit_describe = (EditText) findViewById(R.id.losing_add_describe);
        edit_title = (EditText) findViewById(R.id.losing_add_title);
        iv_photo = (ImageView) findViewById(R.id.losing_photo);
        initWindow();
    }

    @Override
    public void initListeners() {
        // TODO Auto-generated method stub
        btn_back.setOnClickListener(this);
        btn_true.setOnClickListener(this);
        iv_photo.setOnClickListener(this);
    }

    @Override
    public void initData() {
        // TODO Auto-generated method stub
        from = getIntent().getStringExtra("from");
        old_title = getIntent().getStringExtra("title");
        old_phone = getIntent().getStringExtra("phone");
        old_describe = getIntent().getStringExtra("describe");

        edit_title.setText(old_title);
        edit_describe.setText(old_describe);
        edit_phone.setText(old_phone);


        if (from.equals("Lost")) {
            tv_add.setText("���ʧ����Ϣ");
        } else {
            tv_add.setText("���������Ϣ");
        }

        mMainPresenter = new MainPresenter(this, this);

        //���ý�����
        dialog = new ProgressDialog(this);
        //���ý�������ʽ
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        //ʧȥ�����ʱ�򣬲���ȥ�Ի���
        dialog.setCancelable(false);
        dialog.setTitle("�����ϴ�");
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == btn_true) {
            addByType();
        }else if(v == iv_photo){
            /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image*//*");
            startActivityForResult(Intent.createChooser(intent,"ѡ��ͼƬ"),SELECT_PICTURE);*/
            Intent intent=new Intent(Intent.ACTION_GET_CONTENT);//ACTION_OPEN_DOCUMENT
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/jpeg");
            if(android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.KITKAT){
                startActivityForResult(intent, SELECT_PIC_KITKAT);
            }else{
                startActivityForResult(intent, SELECT_PIC);
            }
        } else if (v == btn_back) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        /*if (requestCode == PHOTO_RESULT) {
            bitmapSelected = decodeUriAsBitmap(Uri.fromFile(new File(IMG_PATH,
                    "temp.jpg")));
            losing_photo.setText("");
            Drawable drawable =new BitmapDrawable(bitmapSelected);
            losing_photo.setBackground(drawable);
        }*/
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try{
                if(bitmapSelected!=null)//�����ʩ�ŵĻ������϶�ȡͼƬ�������ڴ治��
                    bitmapSelected.recycle();
                bitmapSelected = BitmapFactory.decodeStream(cr.openInputStream(uri));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if(DocumentsContract.isDocumentUri(this, uri))
                        path = getPath(this,uri);
                    else
                        path = selectImage(this, data);
                }
                else{
                    path = getPath(this,uri);
                }

                /*String[] proj = {MediaStore.Images.Media.DATA};
                //������android��ý�����ݿ�ķ�װ�ӿڣ�����Ŀ�Android�ĵ�
                Cursor cursor = getContentResolver().query(uri,proj, null, null, null);
                //���Ҹ������ ����ǻ���û�ѡ���ͼƬ������ֵ
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //�����������ͷ ���������Ҫ����С�ĺ���������Խ��
                cursor.moveToFirst();
                //����������ֵ��ȡͼƬ·��
                path = cursor.getString(column_index);
                Log.i(TAG,""+path);*/
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //losing_photo.setText("");
            //Drawable drawable =new BitmapDrawable(bitmapSelected);
            iv_photo.setImageBitmap(bitmapSelected);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    String title = "";
    String describe = "";
    String phone="";

    /**�����������ʧ��/����
     * addByType
     * @Title: addByType
     * @throws
     */
    private void addByType(){
        title = edit_title.getText().toString();
        describe = edit_describe.getText().toString();
        phone = edit_phone.getText().toString();

        if(TextUtils.isEmpty(title)){
            ShowToast("����д����");
            return;
        }
        if(TextUtils.isEmpty(describe)){
            ShowToast("����д����");
            return;
        }
        if(TextUtils.isEmpty(phone)){
            ShowToast("����д�ֻ�");
            return;
        }
        if(from.equals("Lost")){
            addLost();
        }else{
            addFound();
        }
    }

    private void addLost(){
        if(!path.equals("")){
            final BmobFile bmobFile = new BmobFile(new File(path));
            dialog.show();
            bmobFile.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null){
                        Log.i(TAG, "ͼƬ�ϴ��ɹ�:"+bmobFile.getFileUrl());
                        User user = BmobUser.getCurrentUser(User.class);
                        String username = user.getUsername();
                        Log.i(TAG, username);
                        Lost lost = new Lost(title,phone,describe,bmobFile);
                        lost.setPhotoUrl(bmobFile.getFileUrl());
                        lost.setAuthor(user);
                        insertObject(lost);
                    }
                    else{
                        Log.i(TAG,"�ϴ�ʧ��"+e.getMessage()+","+e.getErrorCode());
                        ShowToast("�����쳣���ϴ�ʧ��");
                    }
                }
            });
        }
        else
        {
            dialog.setTitle("�����ύ");
            dialog.show();
            Lost lost = new Lost(title, phone,  describe);
            User user = BmobUser.getCurrentUser(User.class);
            lost.setAuthor(user);
            insertObject(lost);
        }
    }

    private void insertObject(final BmobObject obj){
        obj.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    if(from.equals("Lost")) {
                        ShowToast("ʧ����Ϣ��ӳɹ�!" );
                        Log.i(TAG, "ʧ����Ϣ��ӳɹ�:" + obj.getObjectId());
                    }else{
                        ShowToast("Ѱ����Ϣ��ӳɹ�!");
                        Log.i(TAG, "Ѱ����Ϣ��ӳɹ�:" + obj.getObjectId());
                    }
                    dialog.dismiss();
                    setResult(RESULT_OK);
                    finish();
                }
                else{
                    dialog.dismiss();
                    if(from.equals("Lost")){
                        Log.i(TAG,"ʧ����Ϣ���ʧ��");
                        ShowToast("ʧ����Ϣ���ʧ��");
                    }else{
                        Log.i(TAG,"Ѱ����Ϣ���ʧ��");
                        ShowToast("Ѱ����Ϣ���ʧ��");
                    }
                }
            }
        });
    }


    private void addFound(){
        if(!path.equals("")){
            final BmobFile bmobFile = new BmobFile(new File(path));
            dialog.show();
            bmobFile.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if(e==null){
                        Log.i(TAG, "ͼƬ�ϴ��ɹ�:"+bmobFile.getFileUrl());
                        User user = BmobUser.getCurrentUser(User.class);
                        String username = user.getUsername();
                        Log.i(TAG, username);
                        Found found = new Found(title,phone,describe,bmobFile);
                        found.setPhotoUrl(bmobFile.getFileUrl());
                        found.setAuthor(user);
                        insertObject(found);
                    }
                    else{
                        Log.i(TAG,"�ϴ�ʧ��"+e.getMessage()+","+e.getErrorCode());
                        ShowToast("�����쳣���ϴ�ʧ��");
                    }
                }
            });
        }
        else
        {
            dialog.setTitle("�����ύ");
            dialog.show();
            Found found = new Found(title,phone,describe);
            User user = BmobUser.getCurrentUser(User.class);
            found.setAuthor(user);
            insertObject(found);
        }
    }

    //���ó���ʽ״̬���͵�����
    private void initWindow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.colorPrimary));
            tintManager.setStatusBarTintEnabled(true);
        }
    }


    /**
     * ��ȡsd����·��
     *
     * @return ·�����ַ���
     */
    public static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // �ж�sd���Ƿ����
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// ��ȡ���Ŀ¼
        }
        return sdDir.toString();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String selectImage(Context context,Intent data){
        Uri selectedImage = data.getData();
//      Log.e(TAG, selectedImage.toString());
        if(selectedImage!=null){
            String uriStr=selectedImage.toString();
            String path=uriStr.substring(10,uriStr.length());
            if(path.startsWith("com.sec.android.gallery3d")){
                Log.e(TAG, "It's auto backup pic path:"+selectedImage.toString());
                return null;
            }
        }
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(selectedImage,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    @Override
    public void showImageView(Bitmap bitmap, String fileName) {

    }
}