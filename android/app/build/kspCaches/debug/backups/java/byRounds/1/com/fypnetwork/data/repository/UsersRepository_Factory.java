package com.fypnetwork.data.repository;

import com.fypnetwork.data.remote.UsersApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class UsersRepository_Factory implements Factory<UsersRepository> {
  private final Provider<UsersApi> usersApiProvider;

  public UsersRepository_Factory(Provider<UsersApi> usersApiProvider) {
    this.usersApiProvider = usersApiProvider;
  }

  @Override
  public UsersRepository get() {
    return newInstance(usersApiProvider.get());
  }

  public static UsersRepository_Factory create(Provider<UsersApi> usersApiProvider) {
    return new UsersRepository_Factory(usersApiProvider);
  }

  public static UsersRepository newInstance(UsersApi usersApi) {
    return new UsersRepository(usersApi);
  }
}
