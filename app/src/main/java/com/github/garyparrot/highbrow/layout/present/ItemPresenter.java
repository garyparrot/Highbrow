package com.github.garyparrot.highbrow.layout.present;

import android.text.Html;
import android.text.Spanned;
import android.view.View;

import com.github.garyparrot.highbrow.R;
import com.github.garyparrot.highbrow.layout.view.CommentItem;
import com.github.garyparrot.highbrow.model.hacker.news.item.Comment;
import com.github.garyparrot.highbrow.model.hacker.news.item.Story;

import java.net.URI;
import java.util.Locale;

import timber.log.Timber;

public class ItemPresenter {

    public static String friendFoldingHint(boolean isFolded, int childCount) {
        if(isFolded) {
            if(childCount == 1)
                return "(1 comment folded)";
            else if(childCount > 1)
                return String.format("(%d comments folded)", childCount);
            else
                return "(Folded)";
        }
        return "";
    }

    public static String devOopsStatement() {
        return " ¯\\_(ツ)_/¯ \n\nFailed to load this comment, sad.";
    }

    public static String toScoreString(Story item) {
        return String.format(Locale.ENGLISH, "%dp",item.getScore());
    }

    public static String compactString(String string) {
        String newline = System.getProperty("line.separator");
        return compactString(string, newline);
    }
    public static String compactString(String string, String newline) {
        return string.replace(newline, " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public static CharSequence renderText(Comment comment, boolean fold) {
        if(comment == null || comment.getText() == null)
            return "";
        if(fold) {
            // If comment is in folding state, we will make the whole message into one single line,
            // merge all duplicated space in single space,
            // and finally trim all useless space.
            // Doing so will result in a more compact string which is better for user to have
            // a first hand insight of what this folded comment is talking about.
            return Html.fromHtml(compactString(comment.getText(), "<p>"));
        }
        return Html.fromHtml(comment.getText());
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

        if(momentSecond == 0)
            return "";

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
