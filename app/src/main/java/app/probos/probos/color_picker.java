package app.probos.probos;

import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import yuku.ambilwarna.AmbilWarnaDialog;
public class color_picker extends AppCompatActivity {
    ConstraintLayout cpLayout;
    int mDefaultColor;
    Button cpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        cpLayout=(ConstraintLayout) findViewById(R.id.activity_color_picker);
        mDefaultColor= ContextCompat.getColor(color_picker.this, R.color.colorPrimaryDark);
        cpButton =(Button) findViewById(R.id.colorPicker);
        cpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openColorPicker();
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
            }
        });
        colorPicker.show();

    }
}
