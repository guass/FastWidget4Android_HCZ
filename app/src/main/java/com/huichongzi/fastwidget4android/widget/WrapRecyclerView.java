package com.huichongzi.fastwidget4android.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * @author chz
 * @description
 * @date 2016/3/17 14:38
 */
public class WrapRecyclerView extends RecyclerView {

    private WrapAdapter mWrapAdapter;

    public WrapRecyclerView(Context context) {
        super(context);
        init();
    }

    public WrapRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WrapRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mWrapAdapter = new WrapAdapter();
    }

    public void addHeaderView(View header){
        mWrapAdapter.addHeaderView(header);
    }

    public void addFooterView(View footer){
        mWrapAdapter.addFooterView(footer);
    }

    @Override
    public void setLayoutManager(final LayoutManager layout) {
        if(layout instanceof GridLayoutManager){
            ((GridLayoutManager) layout).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if(mWrapAdapter.isHeader(position) || mWrapAdapter.isFooter(position)){
                        return ((GridLayoutManager) layout).getSpanCount();
                    }
                    return 1;
                }
            });
        }
        super.setLayoutManager(layout);
    }

    @Override
    public int getChildAdapterPosition(View child) {
        return super.getChildAdapterPosition(child) - mWrapAdapter.getHeaderCount();
    }

    @Override
    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        mWrapAdapter.setAdapter(adapter);
        super.swapAdapter(mWrapAdapter, removeAndRecycleExistingViews);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter.setAdapter(adapter);
        super.setAdapter(mWrapAdapter);
    }

    @Override
    public Adapter getAdapter() {
        return mWrapAdapter.getAdapter();
    }

    @Override
    public ViewHolder findViewHolderForAdapterPosition(int position) {
        return super.findViewHolderForAdapterPosition(position + mWrapAdapter.getHeaderCount());
    }


    class WrapAdapter extends RecyclerView.Adapter<ViewHolder>{
        private final static int TYPE_HEADER = -1000;
        private final static int TYPE_FOOTER = -2000;
        private RecyclerView.Adapter mAdapter;
        private ArrayList<View> mHeaderViews;
        private ArrayList<View> mFooterViews;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewHolder holder = null;
            int type = 0;
            int position = 0;
            if(viewType <= TYPE_FOOTER){
                type = TYPE_FOOTER;
                position = TYPE_FOOTER - viewType;
            }
            else if(viewType <= TYPE_HEADER){
                type = TYPE_HEADER;
                position = TYPE_HEADER - viewType;
            }
            else{
                type = viewType;
            }
            switch (type){
                case TYPE_HEADER:
                    View header = mHeaderViews.get(position);
                    setWrapParems(header);
                    holder = new WrapViewHolder(header);
                    break;
                case TYPE_FOOTER:
                    View footer = mFooterViews.get(position);
                    setWrapParems(footer);
                    holder = new WrapViewHolder(footer);
                    break;
                default:
                    holder = mAdapter.onCreateViewHolder(parent, viewType);
                    break;
            }
            return holder;
        }

        private void setWrapParems(View view){
            int width = getLayoutManager().canScrollVertically() ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = getLayoutManager().canScrollVertically() ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT;
            if(getLayoutManager() instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager.LayoutParams headerParams = new StaggeredGridLayoutManager.LayoutParams(width, height);
                headerParams.setFullSpan(true);
                view.setLayoutParams(headerParams);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (isHeader(position) || isFooter(position)){
            }
            else {
                mAdapter.onBindViewHolder(holder, position - getHeaderCount());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(position < getHeaderCount()){
                return TYPE_HEADER - position;
            }
            else if(position < getItemCount() - getFooterCount()){
                return mAdapter.getItemViewType(position - getHeaderCount());
            }
            else {
                return TYPE_FOOTER - position + getItemCount() - getFooterCount();
            }
        }

        public boolean isHeader(int position){
            return position < getHeaderCount();
        }

        public boolean isFooter(int position){
            return position >= getItemCount() - getFooterCount();
        }

        public void setAdapter(Adapter adapter){
            mAdapter = adapter;
            mAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    notifyDataSetChanged();
                }
                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    notifyItemRangeChanged(positionStart, itemCount);
                }
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    notifyItemRangeInserted(positionStart, itemCount);
                }
                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    notifyItemRangeRemoved(positionStart, itemCount);
                }
                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    notifyItemMoved(fromPosition, toPosition);
                }
            });
            notifyDataSetChanged();
        }

        public Adapter getAdapter(){
            return mAdapter;
        }

        public void addHeaderView(View header){
            if(mHeaderViews == null){
                mHeaderViews = new ArrayList<View>();
            }
            mHeaderViews.add(header);
            notifyDataSetChanged();
        }

        public void removeHeaderView(View header){
            if(mHeaderViews != null){
                mHeaderViews.remove(header);
                notifyDataSetChanged();
            }
        }

        public int getHeaderCount(){
            if(mHeaderViews == null){
                return 0;
            }
            return mHeaderViews.size();
        }

        public void addFooterView(View footer){
            if(mFooterViews == null){
                mFooterViews = new ArrayList<View>();
            }
            mFooterViews.add(footer);
            notifyDataSetChanged();
        }

        public void removeFooterView(View footer){
            if(mFooterViews != null){
                mFooterViews.remove(footer);
                notifyDataSetChanged();
            }
        }

        public int getFooterCount(){
            if(mFooterViews == null){
                return 0;
            }
            return mFooterViews.size();
        }

        @Override
        public int getItemCount() {
            int count = getHeaderCount() + getFooterCount();
            if(mAdapter != null){
                count += mAdapter.getItemCount();
            }
            return count;
        }

        class WrapViewHolder extends ViewHolder{
            public WrapViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}