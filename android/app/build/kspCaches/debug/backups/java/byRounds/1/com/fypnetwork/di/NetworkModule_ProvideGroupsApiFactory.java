package com.fypnetwork.di;

import com.fypnetwork.data.remote.GroupsApi;
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
public final class NetworkModule_ProvideGroupsApiFactory implements Factory<GroupsApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideGroupsApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public GroupsApi get() {
    return provideGroupsApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideGroupsApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideGroupsApiFactory(retrofitProvider);
  }

  public static GroupsApi provideGroupsApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideGroupsApi(retrofit));
  }
}
