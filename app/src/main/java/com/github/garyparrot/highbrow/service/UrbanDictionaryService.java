package com.github.garyparrot.highbrow.service;

import com.github.garyparrot.highbrow.model.dict.UrbanQueryResult;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UrbanDictionaryService {

    @GET("define")
    Single<UrbanQueryResult> query(@Query("term") String term);

}
