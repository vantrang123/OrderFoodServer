package com.trangdv.orderfoodserver.ui.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.trangdv.orderfoodserver.R;
import com.trangdv.orderfoodserver.common.Common;
import com.trangdv.orderfoodserver.ui.LoginActivity;
import com.trangdv.orderfoodserver.ui.MainActivity;
import com.trangdv.orderfoodserver.utils.SharedPrefs;

public class ConfirmLogoutDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private TextView tvNo;
    private TextView tvYes;
    private BottomSheetBehavior behavior;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.dialog_confirm_logout, null);
        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());

        findViewById(view);
        tvYes.setOnClickListener(this);
        tvNo.setOnClickListener(this);
        ((View) view.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
        return dialog;
    }

    private void findViewById(View view) {
        tvYes = view.findViewById(R.id.tv_yes);
        tvNo = view.findViewById(R.id.tv_no);
    }

    private void closeBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void onStart() {
        super.onStart();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_yes:
                closeBottomSheet();
                onLogout();
                break;
            case R.id.tv_no:
                closeBottomSheet();
                ((MainActivity)getActivity()).reSelectItem();
                break;
            default:
                break;
        }
    }

    private void onLogout() {
        SharedPrefs.getInstance().clear();
        FirebaseAuth.getInstance().signOut();
        Common.currentUser = null;
        Common.currentRestaurantOwner = null;
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Common.animateStart(getContext());
    }
}
