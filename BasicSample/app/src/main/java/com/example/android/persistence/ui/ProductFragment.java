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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.android.persistence.R;
import com.example.android.persistence.databinding.ProductFragmentBinding;
import com.example.android.persistence.viewmodel.ProductViewModel;

public class ProductFragment extends Fragment {

    private static final String KEY_PRODUCT_ID = "product_id";

    private ProductFragmentBinding mBinding;

    private CommentAdapter mCommentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate this data binding layout
        mBinding = DataBindingUtil.inflate(inflater, R.layout.product_fragment, container, false);

        // Create and set the adapter for the RecyclerView.
        mCommentAdapter = new CommentAdapter(mCommentClickCallback);
        mBinding.commentList.setAdapter(mCommentAdapter); //어댑터 부착
        return mBinding.getRoot();
    }

    private final CommentClickCallback mCommentClickCallback = comment -> {
        // no-op
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProductViewModel.Factory factory = new ProductViewModel.Factory(
                requireActivity().getApplication(), requireArguments().getInt(KEY_PRODUCT_ID));

        final ProductViewModel model = new ViewModelProvider(this, factory)
                .get(ProductViewModel.class);

        mBinding.setLifecycleOwner(getViewLifecycleOwner());//바인딩에서 LiveData의 변경 사항을 관찰하는 데 사용해야 하는 LifecycleOwner를 설정
        //LiveData가 바인딩 표현식 중 하나에 있고 LifecycleOwner가 설정되지 않은 경우 LiveData가 관찰되지 않으며 이에 대한 업데이트가 UI에 전파되지 않음.
        //product_fragment에 ProductViewModel이 있으므로 설정해줘야하는 것으로 추측
        mBinding.setProductViewModel(model);

        subscribeToModel(model);
    }

    private void subscribeToModel(final ProductViewModel model) {
        // Observe comments
        model.getComments().observe(getViewLifecycleOwner(), commentEntities -> {
            if (commentEntities != null) {
                mBinding.setIsLoading(false);
                mCommentAdapter.submitList(commentEntities);
            } else {
                mBinding.setIsLoading(true);
            }
        });
    }

    @Override
    public void onDestroyView() {
        mBinding = null;
        mCommentAdapter = null;
        super.onDestroyView();
    }

    /** Creates product fragment for specific product ID */
    public static ProductFragment forProduct(int productId) { //상품 id를 가진 프래그먼트 생성하여 반환
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }
}
