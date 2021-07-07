package com.stockaudit.auditstocks.ui.audit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.stockaudit.auditstocks.R;
import com.stockaudit.auditstocks.constant.Constant;
import com.stockaudit.auditstocks.model.audit.SaveCustomerAuditRequest;
import com.stockaudit.auditstocks.retrofit.APIClient;
import com.stockaudit.auditstocks.retrofit.APIInterface;
import com.stockaudit.auditstocks.sharedpreference.SharedPreference;
import com.stockaudit.auditstocks.util.CustomProgressBar;
import com.stockaudit.auditstocks.util.Util;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

enum CATEGORY {
    STOCK_STATEMENT("STOCK STATEMENT");

    private String category;
    CATEGORY(String category) {
        this.category = category;
    }
    public String getCategory() { return this.category; }
}

public class DocumentCategoryActivity extends AppCompatActivity {

    CardView card_stock_statement;
    AlertDialog progressDialog;
    SaveCustomerAuditRequest customerInfo;
    String category;
    String[] permissionRequired = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_document_category );
        initializeView();
        CustomProgressBar customProgressBar = new CustomProgressBar( this );
        progressDialog = customProgressBar.initializeLoader();
        customerInfo = (SaveCustomerAuditRequest) getIntent().getSerializableExtra( "customerInfo" );
        askPermission(getApplicationContext(),permissionRequired);


    }

    //=========== ask for the permission==============
    private boolean askPermission(Context context,String... permissions){
        if(context != null && permissions != null){
            for(String permission : permissions){
                if(ActivityCompat.checkSelfPermission( context,permission ) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }


    private void initializeView() {
        card_stock_statement = findViewById( R.id.card_stock_statement );
        card_stock_statement.setOnClickListener( new CardStockStatementClickListener() );
    }

    private class CardStockStatementClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.STOCK_STATEMENT.getCategory();
        }
    }

    

    private void createIntentToPickFile() {
        Intent takeVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
        takeVideoIntent.setType("*/*");
        launchSelectFileActivity.launch(takeVideoIntent);

    }

    ActivityResultLauncher<Intent> launchSelectFileActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri selectedImageUri = data.getData();
                    // GET FILE MIME TYPE
                    String mimeType = getContentResolver().getType(selectedImageUri);
                    Cursor returnCursor =
                            getContentResolver().query(selectedImageUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex( OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    // GET FILE NAME
                    Log.e("File Name is :: ",returnCursor.getString(nameIndex));
                    String fileName = returnCursor.getString(nameIndex);
                    returnCursor.close();

                    // MEDIA GALLERY
                    String selectedImagePath = Util.getPathFromURI(getApplicationContext(),selectedImageUri);
                    File videoFile = new File(selectedImagePath);

                    // CALL UPLOAD FILE FUNCTION
                    uploadFile(videoFile,fileName,category,mimeType,selectedImageUri);
                }
            } );


    private void uploadFile(File filePath, String fileName, String category, String mimeType, Uri selectedImageUri) {
        progressDialog.show();
        //INSTEAD OF RequestBody.create USE InputStreamRequestBody FOR FILE
        RequestBody requestBody = new InputStreamRequestBody( MediaType.parse( mimeType ),getContentResolver(),selectedImageUri);
        RequestBody uploadedBy = RequestBody.create( MediaType.parse( "text/plain" ), SharedPreference.getData(getApplicationContext() , Constant.USERID ) );
        RequestBody auditId = RequestBody.create( MediaType.parse( "text/plain" ), String.valueOf( customerInfo.getAuditId()) );
        RequestBody categoryType = RequestBody.create( MediaType.parse( "text/plain" ), category );
        MultipartBody.Part part = MultipartBody.Part.createFormData( "file",fileName,requestBody );
        APIInterface apiInterface = APIClient.getClient().create( APIInterface.class );
        Call<Void> call = apiInterface.uploadAuditFiles( part,uploadedBy,auditId,categoryType );
        call.enqueue( new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressDialog.dismiss();
                if(response.code() == 202){
                    Toast.makeText( getApplicationContext(), Util.getString( getApplicationContext(),R.string.txt_video_saved_success ), Toast.LENGTH_LONG ).show();
                }else if(response.code() == 401){
                    Toast.makeText( getApplicationContext(), response.code()+" "+Util.getString( getApplicationContext(),R.string.unauthorizedError ), Toast.LENGTH_LONG ).show();
                }else{
                    Toast.makeText( getApplicationContext(), response.code()+" "+Util.getString( getApplicationContext(),R.string.msg_video_uploaded_failed ), Toast.LENGTH_LONG ).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText( getApplicationContext(), t.getMessage()+" "+Util.getString( getApplicationContext(),R.string.defaultError ), Toast.LENGTH_LONG ).show();
            }
        } );
    }
}