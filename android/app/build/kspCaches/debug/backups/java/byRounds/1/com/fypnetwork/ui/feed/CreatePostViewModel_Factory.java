package com.fypnetwork.ui.feed;

import android.content.Context;
import com.fypnetwork.data.repository.PostsRepository;
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
public final class CreatePostViewModel_Factory implements Factory<CreatePostViewModel> {
  private final Provider<PostsRepository> postsRepositoryProvider;

  private final Provider<Context> contextProvider;

  public CreatePostViewModel_Factory(Provider<PostsRepository> postsRepositoryProvider,
      Provider<Context> contextProvider) {
    this.postsRepositoryProvider = postsRepositoryProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public CreatePostViewModel get() {
    return newInstance(postsRepositoryProvider.get(), contextProvider.get());
  }

  public static CreatePostViewModel_Factory create(
      Provider<PostsRepository> postsRepositoryProvider, Provider<Context> contextProvider) {
    return new CreatePostViewModel_Factory(postsRepositoryProvider, contextProvider);
  }

  public static CreatePostViewModel newInstance(PostsRepository postsRepository, Context context) {
    return new CreatePostViewModel(postsRepository, context);
  }
}
