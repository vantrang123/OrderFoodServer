package com.trangdv.orderfoodserver.ui.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.ui.MainActivity;

public class AddMenuDialog extends DialogFragment implements View.OnClickListener {
    String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

    private TextView tvYes, tvNo;
    private TextInputEditText edtName, edtDescription;
    public ImageView ivSelectIamge;
    private String name, descriptiom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            this.getDialog().setCanceledOnTouchOutside(true);
        }
        View view = inflater.inflate(R.layout.dialog_add_menu, container, false);
        findViewById(view);

        // event
        textWatcher();
//        init();
        return view;
    }

    private void findViewById(View view) {
        tvYes = view.findViewById(R.id.tv_post);
        tvNo = view.findViewById(R.id.tv_cancel);
        edtName = view.findViewById(R.id.edt_name);
        edtDescription = view.findViewById(R.id.edt_description);
        ivSelectIamge = view.findViewById(R.id.iv_select_image);

        tvYes.setOnClickListener(this);
        tvNo.setOnClickListener(this);
        ivSelectIamge.setOnClickListener(this);
    }

    private void textWatcher() {
        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!edtName.getText().toString().isEmpty() && !edtDescription.getText().toString().isEmpty()) {
                    tvYes.setBackground(getResources().getDrawable(R.drawable.bg_button));
                    tvYes.setClickable(true);
                } else {
                    tvYes.setBackground(getResources().getDrawable(R.drawable.bg_button_disable));
//                    Toast.makeText(getContext(), "Vui lòng điền đủ thông tin!", Toast.LENGTH_SHORT).show();
                    tvYes.setClickable(false);
                }
                name = edtName.getText().toString();
            }
        });

        edtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!edtName.getText().toString().isEmpty() && !edtDescription.getText().toString().isEmpty()) {
                    tvYes.setBackground(getResources().getDrawable(R.drawable.bg_button));
                    tvYes.setClickable(true);
                } else {
                    tvYes.setBackground(getResources().getDrawable(R.drawable.bg_button_disable));
//                    Toast.makeText(getContext(), "Vui lòng điền đủ thông tin!", Toast.LENGTH_SHORT).show();
                    tvYes.setClickable(false);
                }
                descriptiom = edtDescription.getText().toString();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_post:
                ((MainActivity)getActivity()).uploadImage(name, descriptiom);
                dismiss();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.iv_select_image:
                ((MainActivity)getActivity()).chooseImage();
            default:
                break;
        }
    }
}
