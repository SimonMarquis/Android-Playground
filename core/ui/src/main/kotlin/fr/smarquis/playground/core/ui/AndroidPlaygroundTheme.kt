package fr.smarquis.playground.core.ui

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp

@Composable
public fun AndroidPlaygroundTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
): Unit = MaterialTheme(
    colorScheme = when {
        dynamicColor && SDK_INT >= S -> {
            if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
            else dynamicLightColorScheme(LocalContext.current)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    },
    content = content,
)

public typealias PlaygroundTheme = MaterialTheme

@PreviewLightDark
@Composable
private fun AndroidPlaygroundThemePreview() {
    AndroidPlaygroundTheme {
        Surface {
            Button(
                modifier = Modifier.padding(4.dp),
                onClick = {},
            ) {
                Text("AndroidPlaygroundTheme")
            }
        }
    }
}
