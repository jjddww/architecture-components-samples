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

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.android.persistence.databinding.ProductItemBinding;
import com.example.android.persistence.model.Product;
import com.example.android.persistence.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    List<? extends Product> mProductList;

    @Nullable
    private final ProductClickCallback mProductClickCallback;

    public ProductAdapter(@Nullable ProductClickCallback clickCallback) {
        mProductClickCallback = clickCallback; //click 콜백을 받아옴
        setHasStableIds(true);
    }

    public void setProductList(final List<? extends Product> productList) {
        if (mProductList == null) { //ProductList가 비어있으면 채워주기
            mProductList = productList;
            notifyItemRangeInserted(0, productList.size());
        } else {
           // 기존의 notifyDataSetChange() 대신에 dispatchUpdatesTo(Adapter adapter) 를 사용하면 부분적으로 데이터를 교체하는 notify가 실행됨
            // 리스트의 크기가 크다면 비교 연산이 길어질 수 있으므로 calculateDiff() 는 백그라운드 쓰레드에서 처리 를 해주고,
            // 메인 쓰레드에서 DiffUtil.DiffResult 를 가져와 사용하는 것이 권장됨. 목록의 최대 가능 크기는 2^26개

            //notifyDataSetChange() 를 호출하게 되면 리스트의 모든 데이터를 다시 처음부터 새로운 객체를 생성하여 랜더링 하기 때문에 비용이 크게 발생.
            //이런 경우를 위해 등장한 것이 DiffUtil 클래스.
            // DiffUtil은 유틸리티 클래스로, 이전 데이터와 현재 데이터 목록의 차이를 계산하여 업데이트 해야할 데이터에 대해서만 갱신을 할 수 있게 한다.
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mProductList.size();
                } //예전 리스트 항목의 갯수 반환

                @Override
                public int getNewListSize() {
                    return productList.size();
                } // 새로운 리스트 항목의 갯수 반환

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) { //두 객체가 같은 항목인지 여부 판단
                    return mProductList.get(oldItemPosition).getId() ==
                            productList.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) { // 두 항목의 데이터가 같은지 여부 판단
                    //areItemsTheSame이 true를 반환할때만 호출됨.
                    Product newProduct = productList.get(newItemPosition);
                    Product oldProduct = mProductList.get(oldItemPosition);
                    return newProduct.getId() == oldProduct.getId()
                            && TextUtils.equals(newProduct.getDescription(), oldProduct.getDescription())
                            && TextUtils.equals(newProduct.getName(), oldProduct.getName())
                            && newProduct.getPrice() == oldProduct.getPrice();
                    // false가 반환되면 변경 내용에 대한 페이로드를 가져옴
                }
            });
            mProductList = productList;
            result.dispatchUpdatesTo(this);
        }
    }

    @Override
    @NonNull
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //리사이클러뷰 뷰홀더
        ProductItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.product_item,
                        parent, false);
        binding.setCallback(mProductClickCallback); //product_item.xml의 callback 변수 set. (ProductClickCallback 인터페이스에 onClick()이 있음)
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.binding.setProduct(mProductList.get(position)); //product_item.xml의 product 변수 set.
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mProductList == null ? 0 : mProductList.size();
    }

    @Override
    public long getItemId(int position) {
        return mProductList.get(position).getId();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        final ProductItemBinding binding;

        public ProductViewHolder(ProductItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
