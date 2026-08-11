package com.fypnetwork.ui.feed;

import com.fypnetwork.data.repository.AuthRepository;
import com.fypnetwork.data.repository.PostsRepository;
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
public final class FeedViewModel_Factory implements Factory<FeedViewModel> {
  private final Provider<PostsRepository> postsRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public FeedViewModel_Factory(Provider<PostsRepository> postsRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.postsRepositoryProvider = postsRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public FeedViewModel get() {
    return newInstance(postsRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static FeedViewModel_Factory create(Provider<PostsRepository> postsRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new FeedViewModel_Factory(postsRepositoryProvider, authRepositoryProvider);
  }

  public static FeedViewModel newInstance(PostsRepository postsRepository,
      AuthRepository authRepository) {
    return new FeedViewModel(postsRepository, authRepository);
  }
}
