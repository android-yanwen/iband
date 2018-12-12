package com.manridy.iband.adapter;

import android.support.v4.app.FragmentManager;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

/**
 * 解决viewpager销毁
 * Created by jarLiao on 17/12/1.
 */

public class PageAdapter extends FragmentPagerItemAdapter {
    public PageAdapter(FragmentManager fm, FragmentPagerItems pages) {
        super(fm, pages);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
    }
}
