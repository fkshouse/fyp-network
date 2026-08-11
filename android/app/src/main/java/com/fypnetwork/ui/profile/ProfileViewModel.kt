package com.fypnetwork.ui.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fypnetwork.data.remote.dto.PostDto
import com.fypnetwork.data.remote.dto.UserDto
import com.fypnetwork.data.repository.AuthRepository
import com.fypnetwork.data.repository.PostsRepository
import com.fypnetwork.data.repository.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

data class ProfileUiState(
    val user: UserDto? = null,
    val posts: List<PostDto> = emptyList(),
    val headline: String = "",
    val company: String = "",
    val bio: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val showPasswordDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val passwordChangeError: String? = null,
    val passwordChangeSucceeded: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usersRepository: UsersRepository,
    private val postsRepository: PostsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser()
                val posts = postsRepository.getFeed(authorId = user.id).items
                _uiState.value = _uiState.value.copy(
                    user = user,
                    posts = posts,
                    headline = user.headline.orEmpty(),
                    company = user.company.orEmpty(),
                    bio = user.bio.orEmpty(),
                    isLoading = false,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Couldn't load your profile")
            }
        }
    }

    fun startEditing() { _uiState.value = _uiState.value.copy(isEditing = true) }

    fun cancelEditing() {
        // Discard unsaved edits and restore fields from the last loaded user.
        val user = _uiState.value.user
        _uiState.value = _uiState.value.copy(
            isEditing = false,
            headline = user?.headline.orEmpty(),
            company = user?.company.orEmpty(),
            bio = user?.bio.orEmpty(),
        )
    }

    fun onHeadlineChange(value: String) { _uiState.value = _uiState.value.copy(headline = value) }
    fun onCompanyChange(value: String) { _uiState.value = _uiState.value.copy(company = value) }
    fun onBioChange(value: String) { _uiState.value = _uiState.value.copy(bio = value) }

    fun save() {
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val updated = usersRepository.updateProfile(state.headline, state.company, state.bio)
                _uiState.value = _uiState.value.copy(user = updated, isSaving = false, isEditing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Couldn't save changes")
            }
        }
    }

    fun onProfilePictureSelected(uri: Uri) {
        viewModelScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val f = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        f.outputStream().use { output -> input.copyTo(output) }
                    }
                    f
                }
                val updated = usersRepository.uploadProfilePicture(file)
                _uiState.value = _uiState.value.copy(user = updated)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Couldn't upload photo")
            }
        }
    }

    fun showPasswordDialog() { _uiState.value = _uiState.value.copy(showPasswordDialog = true, passwordChangeError = null) }
    fun dismissPasswordDialog() { _uiState.value = _uiState.value.copy(showPasswordDialog = false) }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (newPassword.length < 8) {
            _uiState.value = _uiState.value.copy(passwordChangeError = "New password must be at least 8 characters")
            return
        }
        _uiState.value = _uiState.value.copy(isChangingPassword = true, passwordChangeError = null)
        viewModelScope.launch {
            try {
                // This also logs the device out (server revokes all refresh
                // tokens on a password change) - AppRoot will notice and
                // route back to the login screen automatically.
                authRepository.changePassword(currentPassword, newPassword)
                _uiState.value = _uiState.value.copy(isChangingPassword = false, passwordChangeSucceeded = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isChangingPassword = false,
                    passwordChangeError = "Current password is incorrect",
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
