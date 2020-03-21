package com.trangdv.orderfoodserver.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.trangdv.orderfoodserver.ui.orderdetail.OrderItemFragment;
import com.trangdv.orderfoodserver.ui.orderdetail.OrderStatusFragment;

public class PagerOrderDetailAdapger extends FragmentPagerAdapter {
    private String[] titles = {"Order Status","Ordered items"};

    public PagerOrderDetailAdapger(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return new OrderStatusFragment();
            case 1:
                return new OrderItemFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
