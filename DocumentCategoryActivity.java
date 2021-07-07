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
    STOCK_STATEMENT("STOCK STATEMENT"),
    DEBTORS_STATEMENT("DEBTORS STATEMENT"),
    CREDITORS_STATEMENT("CREDITORS STATEMENT"),
    STOCK_INSURANCE("STOCK INSURANCE"),
    FINANCIAL_STATEMENT("FINANCIAL STATEMENT"),
    CC_AC_STATEMENT("CC A/C STATEMENT"),
    LAST_3_MONTH_STATEMENT("STOCK ST LAST 3 MONTH"),
    BUSINESS_PROFILE("BUSINESS PROFILE"),
    MOM_SALES("MOM SALES"),
    SANCTION_LETTERS("SANCTION LETTER"),
    OTHERS("OTHERS");

    private String category;
    CATEGORY(String category) {
        this.category = category;
    }
    public String getCategory() { return this.category; }
}

public class DocumentCategoryActivity extends AppCompatActivity {

    CardView card_stock_statement,card_debtors_statement,card_creditors_statement,card_stock_insurance,
            card_financial_statement,card_cc_ac_statement,card_stock_st_last_3_month,card_business_profile,
            card_mom_sales,card_sanction_letter,card_others;
    Toolbar toolbar;
    AlertDialog progressDialog;
    SaveCustomerAuditRequest customerInfo;
    String category;
    String[] permissionRequired = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_document_category );
        initializeToolBar();
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

    private void initializeToolBar() {
        toolbar = findViewById(R.id.app_toolbar);
        TextView lbl_heading = (TextView) findViewById( R.id.lbl_heading );
        lbl_heading.setText( getResources().getText( R.string.header_upload_photo ) );
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {

            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_white_24dp);

        }
    }

    private void initializeView() {
        card_stock_statement = findViewById( R.id.card_stock_statement );
        card_debtors_statement = findViewById( R.id.card_debtors_statement );
        card_creditors_statement = findViewById( R.id.card_creditors_statement );
        card_stock_insurance = findViewById( R.id.card_stock_insurance );
        card_financial_statement = findViewById( R.id.card_financial_statement );
        card_cc_ac_statement = findViewById( R.id.card_cc_ac_statement );
        card_stock_st_last_3_month = findViewById( R.id.card_stock_st_last_3_month );
        card_business_profile = findViewById( R.id.card_business_profile );
        card_mom_sales = findViewById( R.id.card_mom_sales );
        card_sanction_letter = findViewById( R.id.card_sanction_letter );
        card_others = findViewById( R.id.card_others );

        card_stock_statement.setOnClickListener( new CardStockStatementClickListener() );
        card_debtors_statement.setOnClickListener( new CardDebtorsStatementClickListener() );
        card_creditors_statement.setOnClickListener( new CardCreditorsStatementClickListener() );
        card_stock_insurance.setOnClickListener( new CardStockInsuranceClickListener() );
        card_financial_statement.setOnClickListener( new CardFinancialStatementClickListener() );
        card_cc_ac_statement.setOnClickListener( new CardCCACStatementClickListener() );
        card_stock_st_last_3_month.setOnClickListener( new CardLast3MonthStatementClickListener() );
        card_business_profile.setOnClickListener( new CardBusinessProfileClickListener() );
        card_mom_sales.setOnClickListener( new CardMOMSalesClickListener() );
        card_sanction_letter.setOnClickListener( new CardSanctionLetterClickListener() );
        card_others.setOnClickListener( new CardOthersClickListener() );
    }

    private class CardStockStatementClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.STOCK_STATEMENT.getCategory();
        }
    }

    private class CardDebtorsStatementClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.DEBTORS_STATEMENT.getCategory();
        }
    }

    private class CardCreditorsStatementClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.CREDITORS_STATEMENT.getCategory();
        }
    }

    private class CardStockInsuranceClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.STOCK_INSURANCE.getCategory();
        }
    }

    private class CardFinancialStatementClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.FINANCIAL_STATEMENT.getCategory();
        }
    }

    private class CardCCACStatementClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.CC_AC_STATEMENT.getCategory();
        }
    }

    private class CardLast3MonthStatementClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.LAST_3_MONTH_STATEMENT.getCategory();
        }
    }

    private class CardBusinessProfileClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.BUSINESS_PROFILE.getCategory();
        }
    }

    private class CardMOMSalesClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.MOM_SALES.getCategory();
        }
    }

    private class CardSanctionLetterClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.SANCTION_LETTERS.getCategory();
        }
    }

    private class CardOthersClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            createIntentToPickFile();
            category = CATEGORY.OTHERS.getCategory();
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
                    String mimeType = getContentResolver().getType(selectedImageUri);
                    Cursor returnCursor =
                            getContentResolver().query(selectedImageUri, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex( OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    Log.e("File Name is :: ",returnCursor.getString(nameIndex));
                    String fileName = returnCursor.getString(nameIndex);
                    returnCursor.close();

                    // MEDIA GALLERY
                    String selectedImagePath = Util.getPathFromURI(getApplicationContext(),selectedImageUri);
                    File videoFile = new File(selectedImagePath);

                    uploadFile(videoFile,fileName,category,mimeType,selectedImageUri);
                }
            } );


    private void uploadFile(File filePath, String fileName, String category, String mimeType, Uri selectedImageUri) {
        progressDialog.show();
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