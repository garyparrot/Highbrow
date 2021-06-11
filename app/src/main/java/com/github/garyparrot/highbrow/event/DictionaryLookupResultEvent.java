package com.github.garyparrot.highbrow.event;

import androidx.annotation.Nullable;

import com.github.garyparrot.highbrow.model.dict.UrbanQueryResult;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DictionaryLookupResultEvent {

    /**
     * The word we are looking up
     */
    String queryWord;

    /**
     * The query result object
     */
    UrbanQueryResult result;

    /**
     * Exception occurred or not, if this field is null that means no exception, vice versa.
    */
    @Nullable
    Throwable throwable;

    public boolean isQuerySucceedWithContent() {
        return throwable == null && !isQueryEmpty();
    }

    public boolean isQueryEmpty() {
        return result.getList().size() == 0;
    }
}
