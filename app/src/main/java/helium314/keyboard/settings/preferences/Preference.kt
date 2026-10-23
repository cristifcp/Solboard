// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.R
import helium314.keyboard.settings.IconOrImage
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.previewDark

// partially taken from StreetComplete / SCEE

@Composable
fun PreferenceCategory(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title.uppercase(),
        modifier = modifier.padding(top = 20.dp, start = 20.dp, end = 16.dp, bottom = 6.dp),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp)
    )
}

@Composable
fun Preference(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    @DrawableRes icon: Int? = null,
    value: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .heightIn(min = if (icon == null && description == null) 48.dp else 56.dp)
            .padding(vertical = 6.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // A tinted container gives the list a rhythm bare icons never have: the
        // eye follows a column of shapes instead of a ragged left edge, and the
        // glyph can shrink without leaving a hole where a big icon was.
        if (icon != null)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    IconOrImage(icon, name, 22)
                }
            }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                CompositionLocalProvider(
                    LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(top = 2.dp),
                        // Long summaries turned rows into paragraphs; three lines
                        // is enough to know what a setting does.
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (value != null) {
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(
                    textAlign = TextAlign.End,
                    hyphens = Hyphens.Auto
                ),
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 8.dp,
                        alignment = Alignment.End
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) { value() }
            }
        }
    }
}

@Preview
@Composable
private fun PreferencePreview() {
    Theme(previewDark) {
        Surface {
            Column {
                PreferenceCategory("Preference Category")
                Preference(
                    name = "Preference",
                    onClick = {},
                )
                Preference(
                    name = "Preference with icon",
                    onClick = {},
                    icon = R.drawable.ic_settings_about
                )
                SliderPreference(
                    name = "SliderPreference",
                    key = "",
                    default = 1,
                    description = { it.toString() },
                    range = -5f..5f
                )
                Preference(
                    name = "Preference with icon and description",
                    description = "some text",
                    onClick = {},
                    icon = R.drawable.ic_settings_about
                )
                Preference(
                    name = "Preference with switch",
                    onClick = {}
                ) {
                    Switch(checked = true, onCheckedChange = {})
                }
                SwitchPreference(
                    name = "SwitchPreference",
                    key = "none",
                    default = true
                )
                Preference(
                    name = "Preference",
                    onClick = {},
                    description = "A long description which may actually be several lines long, so it should wrap."
                ) {
                    Icon(painterResource(R.drawable.ic_arrow_left), null)
                }
                Preference(
                    name = "Long preference name that wraps",
                    onClick = {},
                ) {
                    Text("Long preference value")
                }
                Preference(
                    name = "Long preference name 2",
                    onClick = {},
                    description = "hello I am description"
                ) {
                    Text("Long preference value")
                }
            }
        }
    }
}
