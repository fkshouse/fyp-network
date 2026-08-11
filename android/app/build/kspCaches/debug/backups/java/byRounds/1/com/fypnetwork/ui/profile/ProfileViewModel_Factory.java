package com.fypnetwork.ui.profile;

import android.content.Context;
import com.fypnetwork.data.repository.AuthRepository;
import com.fypnetwork.data.repository.PostsRepository;
import com.fypnetwork.data.repository.UsersRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<UsersRepository> usersRepositoryProvider;

  private final Provider<PostsRepository> postsRepositoryProvider;

  private final Provider<Context> contextProvider;

  public ProfileViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<UsersRepository> usersRepositoryProvider,
      Provider<PostsRepository> postsRepositoryProvider, Provider<Context> contextProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.usersRepositoryProvider = usersRepositoryProvider;
    this.postsRepositoryProvider = postsRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(authRepositoryProvider.get(), usersRepositoryProvider.get(), postsRepositoryProvider.get(), contextProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<UsersRepository> usersRepositoryProvider,
      Provider<PostsRepository> postsRepositoryProvider, Provider<Context> contextProvider) {
    return new ProfileViewModel_Factory(authRepositoryProvider, usersRepositoryProvider, postsRepositoryProvider, contextProvider);
  }

  public static ProfileViewModel newInstance(AuthRepository authRepository,
      UsersRepository usersRepository, PostsRepository postsRepository, Context context) {
    return new ProfileViewModel(authRepository, usersRepository, postsRepository, context);
  }
}
