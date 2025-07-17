package fr.smarquis.playground.data.licenses

import app.cash.licensee.ArtifactDetail
import app.cash.licensee.ArtifactScm
import app.cash.licensee.SpdxLicense
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import fr.smarquis.playground.core.di.TestAssetManager
import fr.smarquis.playground.core.utils.StandardCoroutineScopeRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.FileNotFoundException
import kotlin.test.Test
import kotlin.test.assertIs

class LicensesRepositoryImplTest {


    @get:Rule
    val temp = TemporaryFolder()

    @get:Rule
    val coroutines = StandardCoroutineScopeRule()

    private val assets = TestAssetManager(temp)

    @Test
    fun `missing file`() = runTest {
        /* Given */
        val repo = LicensesRepositoryImpl(
            assets = assets,
            dispatcher = coroutines.dispatcher,
        )
        /* Then */
        repo.licenses.test {
            assertIs<FileNotFoundException>(awaitError())
        }
    }

    @Test
    fun `corrupted file`() = runTest {
        /* Given */
        temp.newFolder("app", "cash", "licensee")
        temp.newFile("app/cash/licensee/artifacts.json").writeText("...")
        val repo = LicensesRepositoryImpl(
            assets = assets,
            dispatcher = coroutines.dispatcher,
        )
        /* Then */
        repo.licenses.test {
            assertIs<SerializationException>(awaitError())
        }
    }

    @Test
    fun `empty licenses file`() = runTest {
        /* Given */
        temp.newFolder("app", "cash", "licensee")
        temp.newFile("app/cash/licensee/artifacts.json").writeText("[]")
        val repo = LicensesRepositoryImpl(
            assets = assets,
            dispatcher = coroutines.dispatcher,
        )
        /* Then */
        repo.licenses.test {
            assertThat(awaitItem()).isEqualTo(persistentListOf())
            awaitComplete()
        }
    }

    @Test
    fun `valid licenses file`() = runTest {
        /* Given */
        @Language("JSON")
        val json = """
            [
                {
                    "groupId": "androidx.activity",
                    "artifactId": "activity",
                    "version": "1.10.1",
                    "name": "Activity",
                    "spdxLicenses": [
                        {
                            "identifier": "Apache-2.0",
                            "name": "Apache License 2.0",
                            "url": "https://www.apache.org/licenses/LICENSE-2.0"
                        }
                    ],
                    "scm": {
                        "url": "https://cs.android.com/androidx/platform/frameworks/support"
                    }
                }
            ]
            """.trimIndent()
        temp.newFolder("app", "cash", "licensee")
        temp.newFile("app/cash/licensee/artifacts.json").writeText(json)
        val repo = LicensesRepositoryImpl(
            assets = assets,
            dispatcher = coroutines.dispatcher,
        )
        /* Then */
        repo.licenses.test {
            assertThat(awaitItem())
                .isEqualTo(
                    ArtifactDetail(
                        groupId = "androidx.activity",
                        artifactId = "activity",
                        version = "1.10.1",
                        name = "Activity",
                        spdxLicenses = SpdxLicense(
                            identifier = "Apache-2.0",
                            name = "Apache License 2.0",
                            url = "https://www.apache.org/licenses/LICENSE-2.0",
                        ).let(::setOf),
                        scm = ArtifactScm(url = "https://cs.android.com/androidx/platform/frameworks/support"),
                    ).let { persistentListOf(it) },
                )
            awaitComplete()
        }
    }

}
