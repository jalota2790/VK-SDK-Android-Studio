package com.download.vk.newsdk;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.VKLogin)
    Button logIn;

    @BindView(R.id.VKLogout)
    Button logOut;

    @BindView(R.id.userEmail)
    TextView dataView;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    //VkScope is used to get permissions to access profile data from server
    public static final String[] VK_SCOPES = new String[]{
            VKScope.FRIENDS,
            VKScope.MESSAGES,
            VKScope.NOTIFICATIONS,
            VKScope.OFFLINE,
            VKScope.STATUS,
            VKScope.STATS,
            VKScope.PHOTOS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //initialize VK sdk with your appId
        VKSdk.customInitialize(this,0123456,"5.52");

    }

    @OnClick({R.id.VKLogin,R.id.VKLogout})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.VKLogin:
                //Initiaize login
                VKSdk.login(this,VK_SCOPES);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case R.id.VKLogout:
                //Logout if already login
                VKSdk.logout();
                if (!VKSdk.isLoggedIn()) {
                   dataView.setText("");
                   logOut.setVisibility(View.GONE);
                   logIn.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //OAuth requires access token before you can actually login to VK.com
        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                //after access token is acquired you can access profile public data
                VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS,
                        "id,first_name,last_name,sex,bdate,city,country,photo_50,photo_100," +
                                "photo_200_orig,photo_200,photo_400_orig,photo_max,photo_max_orig,online," +
                                "online_mobile,lists,domain,has_mobile,contacts,connections,site,education," +
                                "universities,schools,can_post,can_see_all_posts,can_see_audio,can_write_private_message," +
                                "status,last_seen,common_count,relation,relatives,counters"));
                request.secure = false;
                request.useSystemLanguage = false;
                //register a listene r to listen to events of your call
                request.executeWithListener(mRequestListener);
            }

            @Override
            public void onError(VKError error) {
                if (progressBar.isShown())
                    progressBar.setVisibility(View.GONE);
                logIn.setVisibility(View.VISIBLE);
                logOut.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this,
                        "User didn't pass Authorization", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //listener is used to listen to result of the call made
    VKRequest.VKRequestListener mRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            logIn.setVisibility(View.GONE);
            logOut.setVisibility(View.VISIBLE);
            dataView.setText(response.json.toString());
            if (progressBar.isShown())
                progressBar.setVisibility(View.GONE);
        }
        @Override
        public void onError(VKError error) {
            logOut.setVisibility(View.GONE);
            logIn.setVisibility(View.VISIBLE);
            dataView.setText(error.toString());
            if (progressBar.isShown())
                progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
            // you can show progress of the request if you want
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            if (progressBar.isShown())
                progressBar.setVisibility(View.GONE);
            logOut.setVisibility(View.GONE);
            logIn.setVisibility(View.VISIBLE);
            Toast.makeText(LoginActivity.this, "Attempt %d/%d failed\n"+attemptNumber+"\n"+totalAttempts, Toast.LENGTH_SHORT).show();
        }
    };
}
