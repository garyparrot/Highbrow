package com.github.garyparrot.highbrow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.garyparrot.highbrow.databinding.FragmentBrowserBinding;
import com.github.garyparrot.highbrow.service.HackerNewsService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BrowserFragment extends Fragment {

    private static final String BUNDLE_URL = "BUNDLE_URL";

    @Inject
    HackerNewsService hackerNewsService;

    public BrowserFragment() {
    }

    public static BrowserFragment newInstance(String url) {
        BrowserFragment fragment = new BrowserFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_URL, url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentBrowserBinding binding = FragmentBrowserBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();

        binding.webView.setWebViewClient(new WebViewClient());
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                binding.progressBar.setProgress(newProgress);

                if(newProgress == 100)
                    binding.progressBar.setVisibility(View.GONE);
            }
        });
        binding.webView.loadUrl(bundle.getString(BUNDLE_URL));

        return binding.getRoot();
    }
}