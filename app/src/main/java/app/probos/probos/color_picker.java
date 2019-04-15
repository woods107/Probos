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

import android.view.View;




import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import yuku.ambilwarna.AmbilWarnaDialog;
public class color_picker extends AppCompatActivity {
    ConstraintLayout cpLayout;
    int mDefaultColor;
    Button cpButton;
    ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        cpLayout=(ConstraintLayout) findViewById(R.id.activity_color_picker);
        SharedPreferences sp1 = getSharedPreferences("Color", MODE_PRIVATE);
        mDefaultColor=sp1.getInt("BackgroundColor",0);
        if(mDefaultColor==0){
            mDefaultColor= ContextCompat.getColor(color_picker.this, R.color.colorPrimaryDark);
            cpLayout.setBackgroundColor(mDefaultColor);
        }else{
            cpLayout.setBackgroundColor(mDefaultColor);
        }

        cpButton =(Button) findViewById(R.id.colorPicker);
        cpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
            }
        });
        backButton=(ImageButton)findViewById(R.id.imageButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Intent intent = new Intent(this, TimelineActivity.class);
                startActivity(intent);
                finish();*/

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


}
