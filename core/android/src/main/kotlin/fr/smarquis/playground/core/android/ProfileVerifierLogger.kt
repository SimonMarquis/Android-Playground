package fr.smarquis.playground.core.android

import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.profileinstaller.ProfileVerifier
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_COMPILED_WITH_PROFILE
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_COMPILED_WITH_PROFILE_NON_MATCHING
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_ERROR_CACHE_FILE_EXISTS_BUT_CANNOT_BE_READ
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_ERROR_CANT_WRITE_PROFILE_VERIFICATION_RESULT_CACHE_FILE
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_ERROR_NO_PROFILE_EMBEDDED
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_ERROR_PACKAGE_NAME_DOES_NOT_EXIST
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_ERROR_UNSUPPORTED_API_VERSION
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_NO_PROFILE_INSTALLED
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.RESULT_CODE_PROFILE_ENQUEUED_FOR_COMPILATION
import androidx.profileinstaller.ProfileVerifier.CompilationStatus.ResultCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

public class ProfileVerifierLogger @Inject constructor() {

    public operator fun invoke(): Job = ProcessLifecycleOwner.get().lifecycleScope.launch {
        val status = ProfileVerifier.getCompilationStatusAsync().await()
        Log.d("ProfileInstaller", status.profileInstallResultCode.asString())
    }

    private fun @receiver:ResultCode Int.asString() = when (this) {
        RESULT_CODE_ERROR_NO_PROFILE_EMBEDDED -> "ERROR_NO_PROFILE_EMBEDDED"
        RESULT_CODE_NO_PROFILE_INSTALLED -> "NO_PROFILE_INSTALLED"
        RESULT_CODE_COMPILED_WITH_PROFILE -> "COMPILED_WITH_PROFILE"
        RESULT_CODE_PROFILE_ENQUEUED_FOR_COMPILATION -> "PROFILE_ENQUEUED_FOR_COMPILATION"
        RESULT_CODE_COMPILED_WITH_PROFILE_NON_MATCHING -> "COMPILED_WITH_PROFILE_NON_MATCHING"
        RESULT_CODE_ERROR_PACKAGE_NAME_DOES_NOT_EXIST -> "ERROR_PACKAGE_NAME_DOES_NOT_EXIST"
        RESULT_CODE_ERROR_CACHE_FILE_EXISTS_BUT_CANNOT_BE_READ -> "ERROR_CACHE_FILE_EXISTS_BUT_CANNOT_BE_READ"
        RESULT_CODE_ERROR_CANT_WRITE_PROFILE_VERIFICATION_RESULT_CACHE_FILE -> "ERROR_CANT_WRITE_PROFILE_VERIFICATION_RESULT_CACHE_FILE"
        RESULT_CODE_ERROR_UNSUPPORTED_API_VERSION -> "ERROR_UNSUPPORTED_API_VERSION"
        else -> "Unknown result code [$this]"
    }

}
