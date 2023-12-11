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

package com.example.android.persistence.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.android.persistence.R;
import com.example.android.persistence.databinding.ListFragmentBinding;
import com.example.android.persistence.db.entity.ProductEntity;
import com.example.android.persistence.viewmodel.ProductListViewModel;

import java.util.List;

public class ProductListFragment extends Fragment {

    public static final String TAG = "ProductListFragment";

    private ProductAdapter mProductAdapter;

    private ListFragmentBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.list_fragment, container, false);

        mProductAdapter = new ProductAdapter(mProductClickCallback);
        mBinding.productsList.setAdapter(mProductAdapter);

        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ProductListViewModel viewModel =
                new ViewModelProvider(this).get(ProductListViewModel.class); //상품 리스트에 표출할 데이터를 갖는 ViewModel 생성

        mBinding.productsSearchBtn.setOnClickListener(v -> { //검색 버튼을 누름
            Editable query = mBinding.productsSearchBox.getText(); //검색창에 쓴 텍스트를 가져옴
            viewModel.setQuery(query); //ViewModel의 SaveStateHandle에 저장
        });

        subscribeUi(viewModel.getProducts()); // 뷰모델에서 livedata를 받아서 전달
    }

    private void subscribeUi(LiveData<List<ProductEntity>> liveData) {
        // Update the list when the data changes
        liveData.observe(getViewLifecycleOwner(), myProducts -> { //liveData 관찰
            if (myProducts != null) {
                mBinding.setIsLoading(false); //데이터바인딩 isLoading 변수값 설정
                mProductAdapter.setProductList(myProducts); //liveData 값 어댑터에 넘겨서 리스트 데이터 설정
            } else {
                mBinding.setIsLoading(true);
            }
            // espresso does not know how to wait for data binding's loop so we execute changes
            // sync.
            mBinding.executePendingBindings(); //데이터변경이 즉각적으로 일어나도록함.
        });
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        mProductAdapter = null;
        super.onDestroyView();
    }

    private final ProductClickCallback mProductClickCallback = product -> { //ProductClickCallback 인터페이스의 onClick 메소드 구현
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) { //STARTED 상태일 때
            ((MainActivity) requireActivity()).show(product); //ProductFragment를 띄우는 함수 호출
        }
    };
}
