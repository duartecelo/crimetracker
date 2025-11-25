package com.crimetracker.app.ui.screens.post

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crimetracker.app.data.repository.PostRepository
import com.crimetracker.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

data class CreatePostUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun createPost(content: String, mediaUri: Uri?, isImportant: Boolean, context: Context) {
        if (content.isBlank() && mediaUri == null) {
            _uiState.update { it.copy(error = "Adicione texto ou mídia ao post") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val mediaFile = mediaUri?.let { uri ->
                getFileFromUri(context, uri)
            }

            when (val result = postRepository.createPost(groupId, content, mediaFile, isImportant)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, success = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> {}
            }

            // Cleanup temp file
            mediaFile?.delete()
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            val isImage = mimeType?.startsWith("image/") == true
            
            if (isImage) {
                // Compress image
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                
                // Compress to JPEG with 70% quality initially
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                outputStream.close()
                
                // Check if still too big (e.g., > 1MB), try compressing more
                if (file.length() > 1 * 1024 * 1024) {
                    val file2 = File(context.cacheDir, "temp_image_compressed_${System.currentTimeMillis()}.jpg")
                    val outputStream2 = FileOutputStream(file2)
                    // Reduce to 40% quality
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 40, outputStream2)
                    outputStream2.close()
                    file.delete()
                    
                    // If still > 1MB, resize the image
                    if (file2.length() > 1 * 1024 * 1024) {
                         val file3 = File(context.cacheDir, "temp_image_resized_${System.currentTimeMillis()}.jpg")
                         val outputStream3 = FileOutputStream(file3)
                         val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(
                             bitmap, 
                             (bitmap.width * 0.5).toInt(), 
                             (bitmap.height * 0.5).toInt(), 
                             true
                         )
                         scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 40, outputStream3)
                         outputStream3.close()
                         file2.delete()
                         return file3
                    }
                    return file2
                }
                
                return file
            } else {
                // Video or other: just copy but check size
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File(context.cacheDir, "temp_media_${System.currentTimeMillis()}.tmp")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                // Limit video to 50MB (arbitrary safe limit for typical servers)
                if (file.length() > 50 * 1024 * 1024) {
                    file.delete()
                    throw Exception("O arquivo é muito grande (Máx 50MB)")
                }
                
                return file
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(error = e.message ?: "Erro ao processar arquivo") }
            null
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
