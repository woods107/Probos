package app.probos.probos;

import android.content.Intent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.Toolbar;
import android.view.View;




import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
//import android.widget.Toolbar;

import yuku.ambilwarna.AmbilWarnaDialog;
public class color_picker extends AppCompatActivity {
    ConstraintLayout cpLayout;
    Toolbar tool;
    int mDefaultColor;
    int sDefaultColor;
    Button cpButton;
    Button scpButton;
    Button rdButton;
    Button backButton;
    //ImageButton backButton;
    //Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        tool = findViewById(R.id.tool);
        //setSupportActionBar(tool);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        cpLayout=(ConstraintLayout) findViewById(R.id.activity_color_picker);
        //tool=(Toolbar) findViewById(R.id.toolbar);
        backButton=(Button)findViewById(R.id.backButton);
        cpButton =(Button) findViewById(R.id.colorPicker);
        scpButton=(Button) findViewById(R.id.secondColorPicker);
        rdButton=(Button) findViewById(R.id.restoreDefault);
        SharedPreferences sp1 = getSharedPreferences("Color", MODE_PRIVATE);
        mDefaultColor=sp1.getInt("BackgroundColor",0);
        sDefaultColor=sp1.getInt("SecondaryColor",0);
        if(mDefaultColor==0){
            mDefaultColor= ContextCompat.getColor(color_picker.this, R.color.colorPrimaryDark);
            cpLayout.setBackgroundColor(mDefaultColor);
        }else{
            cpLayout.setBackgroundColor(mDefaultColor);
        }
        if(sDefaultColor==0){
            sDefaultColor= ContextCompat.getColor(color_picker.this, R.color.colorPrimary);
            cpButton.setBackgroundColor(sDefaultColor);
            scpButton.setBackgroundColor(sDefaultColor);
            backButton.setBackgroundColor(sDefaultColor);
            rdButton.setBackgroundColor(sDefaultColor);
            tool.setBackgroundColor(sDefaultColor);
        }else{
            cpButton.setBackgroundColor(sDefaultColor);
            scpButton.setBackgroundColor(sDefaultColor);
            backButton.setBackgroundColor(sDefaultColor);
            rdButton.setBackgroundColor(sDefaultColor);
            tool.setBackgroundColor(sDefaultColor);
        }
        //toolbar.setBackgroundColor(sDefaultColor);


        cpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Intent intent = new Intent(this, TimelineActivity.class);
                startActivity(intent);
                finish();*/
                //finish();
                onBackPressed();

            }
        });

        scpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker2();
            }
        });
        rdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDefaultColor=ContextCompat.getColor(color_picker.this,R.color.colorPrimaryDark);
                sDefaultColor=ContextCompat.getColor(color_picker.this,R.color.colorPrimary);
                cpLayout.setBackgroundColor(mDefaultColor);
                cpButton.setBackgroundColor(sDefaultColor);
                scpButton.setBackgroundColor(sDefaultColor);
                backButton.setBackgroundColor(sDefaultColor);
                rdButton.setBackgroundColor(sDefaultColor);
                tool.setBackgroundColor(sDefaultColor);
                //toolbar.setBackgroundColor(sDefaultColor);
                SharedPreferences li = getSharedPreferences("Color", MODE_PRIVATE);
                SharedPreferences.Editor Ed = li.edit();

                Ed.putInt("BackgroundColor", mDefaultColor);
                Ed.putInt("SecondaryColor", sDefaultColor);

                Ed.commit();
            }
        });
    }

    public void openColorPicker(){
        AmbilWarnaDialog colorPicker =new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                mDefaultColor=color;
                cpLayout.setBackgroundColor(mDefaultColor);
                SharedPreferences li = getSharedPreferences("Color", MODE_PRIVATE);
                SharedPreferences.Editor Ed = li.edit();

                Ed.putInt("BackgroundColor", mDefaultColor);

                Ed.commit();
            }
        });
        colorPicker.show();

    }
    public void openColorPicker2(){
        AmbilWarnaDialog colorPicker =new AmbilWarnaDialog(this, sDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                sDefaultColor=color;
                cpButton.setBackgroundColor(sDefaultColor);
                scpButton.setBackgroundColor(sDefaultColor);
                backButton.setBackgroundColor(sDefaultColor);
                rdButton.setBackgroundColor(sDefaultColor);
                tool.setBackgroundColor(sDefaultColor);
                //toolbar.setBackgroundColor(sDefaultColor);
                SharedPreferences li = getSharedPreferences("Color", MODE_PRIVATE);
                SharedPreferences.Editor Ed = li.edit();

                Ed.putInt("SecondaryColor", sDefaultColor);

                Ed.commit();
            }
        });
        colorPicker.show();

    }


}
