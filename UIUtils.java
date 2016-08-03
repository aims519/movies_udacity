package com.aimtech.android.movies;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Andy on 14/03/2016.
 */
public class UIUtils {

    private static final String lOG_TAG = UIUtils.class.getSimpleName();

    /**
     * Sets ListView height dynamically based on the height of the items.
     *
     * @param listView to be resized
     * @return true if the listView is successfully resized, false otherwise
     */
    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }

    }


    public static String formatDateString(String rawDate) {

        Log.d("UIUtils", "Raw date received : " + rawDate);
        String[] splitString = rawDate.split("-");
        String year = splitString[0].substring(2, 4);
        String formattedDate = splitString[2].concat("/" + splitString[1]).concat("/" + year);
        Log.d("UIUtils", "Formatted date : " + formattedDate);

        return formattedDate;

    }
}

