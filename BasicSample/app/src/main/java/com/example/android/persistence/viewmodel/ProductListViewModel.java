/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.persistence.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import com.example.android.persistence.BasicApp;
import com.example.android.persistence.DataRepository;
import com.example.android.persistence.db.entity.ProductEntity;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;

import kotlin.jvm.functions.Function1;

public class ProductListViewModel extends AndroidViewModel {
    private static final String QUERY_KEY = "QUERY";

    private final SavedStateHandle mSavedStateHandler; //뷰모델 상태를 저장하는 SaveStateHandle
    private final DataRepository mRepository;
    private final LiveData<List<ProductEntity>> mProducts;

    public ProductListViewModel(@NonNull Application application,
            @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        mSavedStateHandler = savedStateHandle;

        mRepository = ((BasicApp) application).getRepository(); //BasicApp을 통해 리포지토리 싱글톤에 액세스

        // Use the savedStateHandle.getLiveData() as the input to switchMap,
        // allowing us to recalculate what LiveData to get from the DataRepository
        // based on what query the user has entered
        mProducts = Transformations.switchMap(
                savedStateHandle.getLiveData("QUERY", null), //SaveStateHandle에 저장된 LiveData가 switchMap의 두번째 파라미터에 반영됨
                (Function1<CharSequence, LiveData<List<ProductEntity>>>) query -> { //사용자 쿼리에 따라 LiveData를 가져올 수 있음.
                    if (TextUtils.isEmpty(query)) { //쿼리가 비어있으면
                        return mRepository.getProducts(); //상품리스트 모두 가져오기
                    }
                    return mRepository.searchProducts("*" + query + "*"); //쿼리가 비어있지 않으면 검색한 상품만 가져오기
                });
    }

    public void setQuery(CharSequence query) {
        // Save the user's query into the SavedStateHandle.
        // This ensures that we retain the value across process death
        // and is used as the input into the Transformations.switchMap above
        mSavedStateHandler.set(QUERY_KEY, query); //SaveStateHandle에 쿼리 저장
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<ProductEntity>> getProducts() { //ProductListFragment에서 호출함
        return mProducts;
    }
}
