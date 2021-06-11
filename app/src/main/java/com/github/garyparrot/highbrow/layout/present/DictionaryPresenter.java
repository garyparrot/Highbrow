package com.github.garyparrot.highbrow.layout.present;

import com.github.garyparrot.highbrow.model.dict.UrbanQueryResult;

import java.util.Locale;

public class DictionaryPresenter {

    public static String titleForQueryResult(String target) {
        return String.format(Locale.getDefault(), "'%s' from Urban Dictionary", target);
    }

}
