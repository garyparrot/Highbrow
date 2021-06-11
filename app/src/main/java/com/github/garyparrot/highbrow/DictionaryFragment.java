package com.github.garyparrot.highbrow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.github.garyparrot.highbrow.databinding.DictionaryEntryViewBinding;
import com.github.garyparrot.highbrow.databinding.FragmentDictionaryBinding;
import com.github.garyparrot.highbrow.event.DictionaryLookupEvent;
import com.github.garyparrot.highbrow.event.DictionaryLookupResultEvent;
import com.github.garyparrot.highbrow.model.dict.UrbanQueryEntry;
import com.github.garyparrot.highbrow.model.dict.UrbanQueryResult;
import com.github.garyparrot.highbrow.service.UrbanDictionaryService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class DictionaryFragment extends Fragment {

    @Inject
    EventBus eventBus;

    @Inject
    UrbanDictionaryService urbanDictionaryService;

    private RecyclerViewAdapter adapter = null;
    private FragmentDictionaryBinding binding;

    public DictionaryFragment() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveDictionaryLookupResultEvent(DictionaryLookupResultEvent event) {

        if(event.isQuerySucceedWithContent()) {
            binding.setShowResult(true);
            binding.setShowExceptionMessage(false);
            binding.setShowBigSadFace(false);
            binding.setTarget(event.getQueryWord());
            getAdapter().setItems(event.getResult().getList());
        } else if (event.isQueryEmpty()) {
            binding.setShowResult(false);
            binding.setShowExceptionMessage(true);
            binding.setShowBigSadFace(true);
            binding.setExceptionMessage("No definition found");
            binding.setExceptionContent(event.getQueryWord());
        } else {
            binding.setShowResult(false);
            binding.setShowExceptionMessage(true);
            binding.setShowBigSadFace(true);
            binding.setExceptionMessage("Oops");
            // TODO: Use a much friendly error message.
            if (event.getThrowable() != null) {
                binding.setExceptionContent(event.getThrowable().toString());
            } else {
                binding.setExceptionContent("Something wrong, try again later");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventBus.register(this);
    }

    @Override
    public void onDestroy() {
        eventBus.unregister(this);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDictionaryBinding.inflate(inflater, container, false);

        binding.setShowResult(true);
        binding.setShowExceptionMessage(false);
        binding.recycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recycleView.setAdapter(getAdapter());
        binding.recycleView.setNestedScrollingEnabled(true);

        return binding.getRoot();
    }

    private RecyclerViewAdapter getAdapter() {
        if(adapter == null)
            adapter = new RecyclerViewAdapter(Collections.emptyList(), getLayoutInflater());
        return adapter;
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
        private final List<UrbanQueryEntry> definitionEntries;

        /**
         * Initialize a RecyclerViewAdapter which specialize in deal with urban query results.
         * @param entries The initial list of definition entry.
         * @param inflater The layout inflator
         */
        public RecyclerViewAdapter(List<UrbanQueryEntry> entries, LayoutInflater inflater) {

            // Preserve a local copy of user given list
            List<UrbanQueryEntry> localCopy = new ArrayList<>();
            Collections.copy(localCopy, entries);

            this.definitionEntries = localCopy;
            this.inflater = inflater;
        }

        public void setItems(List<UrbanQueryEntry> content) {
            notifyItemRangeRemoved(0, definitionEntries.size());
            definitionEntries.clear();
            definitionEntries.addAll(content);
            notifyItemRangeInserted(0, definitionEntries.size());
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
            holder.binding.setEntry(definitionEntries.get(position));
            holder.binding.setIndex(position+1);
        }

        @Override
        public int getItemCount() {
            return definitionEntries.size();
        }
    }
}