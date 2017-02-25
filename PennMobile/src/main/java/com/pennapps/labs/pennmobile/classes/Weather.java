package com.pennapps.labs.pennmobile.classes;

import com.pennapps.labs.pennmobile.R;

import java.util.List;

/**
 * Created by Jason on 2/18/2017.
 */

public class Weather {

    public TempDetail main;
    public List<ImageDetail> weather;

    public class ImageDetail{
        public String icon;
    }

    public class TempDetail{
        public double temp;
    }

    public static int getIconId(String iconName) {
        switch(iconName) {
            case "01d":
                return R.drawable.h01d;
            case "01n":
                return R.drawable.h01n;
            case "02d":
                return R.drawable.h02d;
            case "02n":
                return R.drawable.h02n;
            case "03d":
                return R.drawable.h03d;
            case "03n":
                return R.drawable.h03n;
            case "04d":
                return R.drawable.h04d;
            case "04n":
                return R.drawable.h04n;
            case "09d":
                return R.drawable.h09d;
            case "09n":
                return R.drawable.h09n;
            case "10d":
                return R.drawable.h10d;
            case "10n":
                return R.drawable.h10n;
            case "11d":
                return R.drawable.h11d;
            case "11n":
                return R.drawable.h11n;
            case "13d":
                return R.drawable.h13d;
            case "13n":
                return R.drawable.h13n;
            case "50d":
                return R.drawable.h50d;
            case "50n":
                return R.drawable.h50n;
        }
        return 0;
    }
}
