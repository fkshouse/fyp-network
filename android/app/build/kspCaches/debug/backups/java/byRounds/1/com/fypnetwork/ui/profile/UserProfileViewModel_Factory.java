package com.fypnetwork.ui.profile;

import androidx.lifecycle.SavedStateHandle;
import com.fypnetwork.data.repository.ConnectionsRepository;
import com.fypnetwork.data.repository.PostsRepository;
import com.fypnetwork.data.repository.UsersRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class UserProfileViewModel_Factory implements Factory<UserProfileViewModel> {
  private final Provider<UsersRepository> usersRepositoryProvider;

  private final Provider<PostsRepository> postsRepositoryProvider;

  private final Provider<ConnectionsRepository> connectionsRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public UserProfileViewModel_Factory(Provider<UsersRepository> usersRepositoryProvider,
      Provider<PostsRepository> postsRepositoryProvider,
      Provider<ConnectionsRepository> connectionsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.usersRepositoryProvider = usersRepositoryProvider;
    this.postsRepositoryProvider = postsRepositoryProvider;
    this.connectionsRepositoryProvider = connectionsRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public UserProfileViewModel get() {
    return newInstance(usersRepositoryProvider.get(), postsRepositoryProvider.get(), connectionsRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static UserProfileViewModel_Factory create(
      Provider<UsersRepository> usersRepositoryProvider,
      Provider<PostsRepository> postsRepositoryProvider,
      Provider<ConnectionsRepository> connectionsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new UserProfileViewModel_Factory(usersRepositoryProvider, postsRepositoryProvider, connectionsRepositoryProvider, savedStateHandleProvider);
  }

  public static UserProfileViewModel newInstance(UsersRepository usersRepository,
      PostsRepository postsRepository, ConnectionsRepository connectionsRepository,
      SavedStateHandle savedStateHandle) {
    return new UserProfileViewModel(usersRepository, postsRepository, connectionsRepository, savedStateHandle);
  }
}
