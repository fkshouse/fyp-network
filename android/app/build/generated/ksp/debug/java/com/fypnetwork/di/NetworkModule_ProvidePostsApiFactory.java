package com.fypnetwork.di;

import com.fypnetwork.data.remote.PostsApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class NetworkModule_ProvidePostsApiFactory implements Factory<PostsApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvidePostsApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public PostsApi get() {
    return providePostsApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvidePostsApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvidePostsApiFactory(retrofitProvider);
  }

  public static PostsApi providePostsApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.providePostsApi(retrofit));
  }
}
