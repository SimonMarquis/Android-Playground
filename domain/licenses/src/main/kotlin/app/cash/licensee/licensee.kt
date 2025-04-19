package app.cash.licensee

import kotlinx.serialization.Serializable

// https://github.com/cashapp/licensee/blob/trunk/src/main/kotlin/app/cash/licensee/outputModel.kt

@Serializable
public data class ArtifactDetail(
    public val groupId: String,
    public val artifactId: String,
    public val version: String,
    public val name: String? = null,
    public val spdxLicenses: Set<SpdxLicense> = emptySet(),
    public val unknownLicenses: Set<UnknownLicense> = emptySet(),
    public val scm: ArtifactScm? = null,
)

@Serializable
public data class SpdxLicense(
    public val identifier: String,
    public val name: String,
    public val url: String,
)

@Serializable
public data class UnknownLicense(
    public val name: String?,
    public val url: String?,
)

@Serializable
public data class ArtifactScm(
    public val url: String,
)
