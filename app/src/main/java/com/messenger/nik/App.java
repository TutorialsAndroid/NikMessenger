package com.messenger.nik;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.tenor.android.core.network.ApiClient;
import com.tenor.android.core.network.ApiService;
import com.tenor.android.core.network.IApiClient;

public class App extends Application {
    public static String TENOR_API_KEY = "GOF84DOIHRTS";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);

        // Create a builder for ApiService
        ApiService.IBuilder<IApiClient> builder = new ApiService.Builder<>(this, IApiClient.class);
        // add your tenor API key here
        builder.apiKey(TENOR_API_KEY);
        // initialize the Tenor ApiClient
        ApiClient.init(this, builder);
    }
}
