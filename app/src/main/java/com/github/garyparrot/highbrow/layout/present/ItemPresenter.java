package com.github.garyparrot.highbrow.layout.present;

import android.view.View;

import com.github.garyparrot.highbrow.model.hacker.news.item.Story;

import java.net.URI;
import java.util.Locale;

public class ItemPresenter {

    public static String toScoreString(Story item) {
        return String.format(Locale.ENGLISH, "%dp",item.getScore());
    }

    public static String toNumberFormat(int number) {
        return String.valueOf(number);
    }

    public static String getDomainName(String url) {
        if(url == null)
            return "news.ycombinator.com";
        URI uri = URI.create(url);
        return uri.getHost();
    }

    public static void onClick(View view) {
        view.performClick();
    }

    public static String getPastedTime(long momentSecond) {

        long nowSecond = System.currentTimeMillis() / 1000;

        long asSecond = nowSecond - momentSecond;
        long asMinute = asSecond / 60;
        long asHour = asMinute / 60;
        long asDay = asHour / 24;
        long asMonth = asDay / 30;
        long asYear = asDay / 365;

        if(asYear > 0) return asYear + "y";
        if(asMonth > 0) return asMonth + "m";
        if(asDay > 0) return asDay + "d";
        if(asHour > 0) return asHour + "h";

        return asMinute + "m";
    }

    public static String getInformation(Story item) {
        return String.format("%s - by %s", getPastedTime(item.getTime()), item.getAuthor());
    }

}
