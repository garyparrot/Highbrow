package com.github.garyparrot.highbrow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.garyparrot.highbrow.databinding.DictionaryEntryViewBinding;
import com.github.garyparrot.highbrow.databinding.FragmentDictionaryBinding;
import com.github.garyparrot.highbrow.event.DictionaryLookupEvent;
import com.github.garyparrot.highbrow.model.dict.UrbanQueryEntry;
import com.github.garyparrot.highbrow.model.dict.UrbanQueryResult;
import com.github.garyparrot.highbrow.service.UrbanDictionaryService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

import static java.security.AccessController.getContext;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DictionaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class DictionaryFragment extends Fragment {

    @Inject
    EventBus eventBus;

    @Inject
    UrbanDictionaryService urbanDictionaryService;

    private static final String ARG_TEXT = "TEXT";

    private String text;
    FragmentDictionaryBinding binding;

    public DictionaryFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DictionaryFragment.
     */
    public static DictionaryFragment newInstance(String text) {
        DictionaryFragment fragment = new DictionaryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveDictionaryLookupEvent(DictionaryLookupEvent event) {
        setText(event.getText());
    }

    public void setText(String targetText) {
        this.text = targetText;
        binding.setTarget(targetText);
        doQuery(targetText);
    }

    private void doQuery(String text) {
        if(text == null) {
            Timber.w("Ignore a null query");
            return;
        }
        urbanDictionaryService.query(text)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((query) -> {
                    binding.setEntries(query);
                    binding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.recycleView.setAdapter(getAdapter(query));
                }, (error) -> {
                    error.printStackTrace();
                    Timber.e(error);
                });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus.register(this);
        if (getArguments() != null) {
            text = getArguments().getString(ARG_TEXT);
        }
        if(text != null)
            setText(text);
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDictionaryBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }


    private RecyclerView.Adapter getAdapter(final UrbanQueryResult result) {
        return new RecyclerViewAdapter(result, getLayoutInflater());
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private final DictionaryEntryViewBinding binding;

        public ViewHolder(DictionaryEntryViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    private static class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final LayoutInflater inflater;
        private final UrbanQueryResult result;

        public RecyclerViewAdapter(UrbanQueryResult result, LayoutInflater inflater) {
            this.result = result;
            this.inflater = inflater;
        }

        @NonNull
        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            DictionaryEntryViewBinding binding = DictionaryEntryViewBinding.inflate(inflater, parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull DictionaryFragment.ViewHolder holder, int position) {
            holder.binding.setEntry(result.getList().get(position));
        }

        @Override
        public int getItemCount() {
            return result.getList().size();
        }
    }
}