package com.fypnetwork.data.repository;

import com.fypnetwork.data.local.TokenManager;
import com.fypnetwork.data.remote.AuthApi;
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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<AuthApi> authApiProvider;

  private final Provider<UsersApi> usersApiProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  public AuthRepository_Factory(Provider<AuthApi> authApiProvider,
      Provider<UsersApi> usersApiProvider, Provider<TokenManager> tokenManagerProvider) {
    this.authApiProvider = authApiProvider;
    this.usersApiProvider = usersApiProvider;
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(authApiProvider.get(), usersApiProvider.get(), tokenManagerProvider.get());
  }

  public static AuthRepository_Factory create(Provider<AuthApi> authApiProvider,
      Provider<UsersApi> usersApiProvider, Provider<TokenManager> tokenManagerProvider) {
    return new AuthRepository_Factory(authApiProvider, usersApiProvider, tokenManagerProvider);
  }

  public static AuthRepository newInstance(AuthApi authApi, UsersApi usersApi,
      TokenManager tokenManager) {
    return new AuthRepository(authApi, usersApi, tokenManager);
  }
}
