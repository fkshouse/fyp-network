package com.fypnetwork.di;

import com.fypnetwork.data.remote.TasksApi;
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
public final class NetworkModule_ProvideTasksApiFactory implements Factory<TasksApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideTasksApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public TasksApi get() {
    return provideTasksApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideTasksApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideTasksApiFactory(retrofitProvider);
  }

  public static TasksApi provideTasksApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideTasksApi(retrofit));
  }
}
