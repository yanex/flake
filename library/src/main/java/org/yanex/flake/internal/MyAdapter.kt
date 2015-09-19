package org.yanex.flake.internal

import android.content.Context
import android.database.DataSetObserver
import android.widget.ArrayAdapter

public class MyAdapter<T>(ctx: Context, res: Int, items: List<T>) : ArrayAdapter<T>(ctx, res, items) {

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }

}