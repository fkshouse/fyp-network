package com.fypnetwork.ui.feed;

import androidx.lifecycle.SavedStateHandle;
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
public final class PostDetailViewModel_Factory implements Factory<PostDetailViewModel> {
  private final Provider<PostsRepository> postsRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public PostDetailViewModel_Factory(Provider<PostsRepository> postsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.postsRepositoryProvider = postsRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public PostDetailViewModel get() {
    return newInstance(postsRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static PostDetailViewModel_Factory create(
      Provider<PostsRepository> postsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new PostDetailViewModel_Factory(postsRepositoryProvider, savedStateHandleProvider);
  }

  public static PostDetailViewModel newInstance(PostsRepository postsRepository,
      SavedStateHandle savedStateHandle) {
    return new PostDetailViewModel(postsRepository, savedStateHandle);
  }
}
