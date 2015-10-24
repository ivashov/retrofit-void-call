package com.github.ivashov.voidretrofit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.Executors;

import retrofit.Call;
import retrofit.JacksonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.mock.CallBehaviorAdapter;
import retrofit.mock.Calls;
import retrofit.mock.MockRetrofit;
import retrofit.mock.NetworkBehavior;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";

	private Api api;

	static class Data {
		@JsonProperty
		public int data;

		public Data(int data) {
			this.data = data;
		}
	}

	interface Api {
		@DELETE
		Call<Void> deleteCrash();

		@DELETE
		Call<Void> deleteSuccess();

		@GET
		Call<Data> get();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		api = createApi();
	}

	public void onDeleteButton(View view) throws Exception{
		api.deleteSuccess().execute();
	}

	public void onCrashButton(View view) throws Exception {
		api.deleteCrash().execute();
	}

	public void onGetButtonSync(View view) throws Exception {
		Data data = api.get().execute().body();
		Toast.makeText(this, "Data received " + data.data, Toast.LENGTH_SHORT).show();
	}

	public void onGetButtonAsync(View view) {
		api.get().enqueue(new DataCallback());
	}

	private Api createApi() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("http://localhost:8080")
				.addConverterFactory(JacksonConverterFactory.create())
				.build();

		NetworkBehavior networkBehavior = NetworkBehavior.create();
		networkBehavior.setFailurePercent(0);

		MockRetrofit mockRetrofit = new MockRetrofit(networkBehavior,
				new CallBehaviorAdapter(retrofit, Executors.newSingleThreadExecutor()));

		return mockRetrofit.create(Api.class, new MockApi(retrofit));
	}

	private class MockApi implements Api {
		private final Retrofit retrofit;

		public MockApi(Retrofit retrofit) {
			this.retrofit = retrofit;
		}

		@Override
		public Call<Void> deleteCrash() {
			Log.d(TAG, "deleteCrash() called");
			return Calls.response(null, retrofit);
		}

		@Override
		public Call<Void> deleteSuccess() {
			Log.d(TAG, "deleteSuccess() called");
			return Calls.response(Response.success((Void) null), retrofit);
		}

		@Override
		public Call<Data> get() {
			return Calls.response(new Data(54), retrofit);
		}
	}

	private class DataCallback implements retrofit.Callback<Data> {
		@Override
		public void onResponse(Response<Data> response, Retrofit retrofit) {
			Toast.makeText(MainActivity.this, "Data received " + response.body().data, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onFailure(Throwable t) {
		}
	}
}
