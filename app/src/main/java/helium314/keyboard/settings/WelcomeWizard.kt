// SPDX-License-Identifier: GPL-3.0-only
package helium314.keyboard.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import helium314.keyboard.latin.R
import helium314.keyboard.latin.utils.JniUtils
import helium314.keyboard.latin.utils.Theme
import helium314.keyboard.latin.utils.UncachedInputMethodManagerUtils
import helium314.keyboard.latin.utils.previewDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The first run: enable the keyboard, switch to it, done.
 *
 * Three steps that the user performs outside this screen, so the screen's job
 * is to say exactly which one is next and to be obviously tappable. It follows
 * the app theme rather than the fixed setup colours it used to carry, which
 * looked pasted-on next to the rest of the settings.
 */
@Composable
fun WelcomeWizard(
    close: () -> Unit,
    finish: () -> Unit
) {
    val ctx = LocalContext.current
    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    fun determineStep(): Int = when {
        !UncachedInputMethodManagerUtils.isThisImeEnabled(ctx, imm) -> 0
        !UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm) -> 2
        else -> 3
    }
    var step by rememberSaveable { mutableIntStateOf(determineStep()) }
    val scope = rememberCoroutineScope { Dispatchers.IO }
    LaunchedEffect(step) {
        if (step == 2)
            scope.launch {
                while (step == 2 && !UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm)) {
                    delay(50)
                }
                step = 3
            }
    }
    val useWideLayout = isWideScreen()
    val appName = stringResource(ctx.applicationInfo.labelRes)

    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (useWideLayout)
                Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                    Box(Modifier.weight(0.42f)) { Heading(step, appName) }
                    Box(Modifier.weight(0.58f)) { Steps(step, appName, imm, close, finish, { step = it }) }
                }
            else
                Column(
                    Modifier.widthIn(max = 520.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Heading(step, appName)
                    Spacer(Modifier.height(28.dp))
                    Steps(step, appName, imm, close, finish, { step = it })
                }
        }
    }
}

@Composable
private fun Heading(step: Int, appName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (step == 0) {
            Image(
                painterResource(R.drawable.setup_welcome_image),
                contentDescription = null,
                modifier = Modifier.heightIn(max = 220.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
        Text(
            stringResource(
                if (step == 0) R.string.setup_welcome_title else R.string.setup_steps_title,
                appName
            ),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        if (step == 0 && JniUtils.sHaveGestureLib) {
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.setup_welcome_additional_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Steps(
    step: Int,
    appName: String,
    imm: InputMethodManager,
    close: () -> Unit,
    finish: () -> Unit,
    onStep: (Int) -> Unit,
) {
    if (step == 0) {
        Button(
            onClick = { onStep(1) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text(stringResource(R.string.setup_start_action))
        }
        return
    }

    Column {
        val ctx = LocalContext.current
        StepProgress(step)
        Spacer(Modifier.height(20.dp))

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onStep(
                when {
                    !UncachedInputMethodManagerUtils.isThisImeEnabled(ctx, imm) -> 0
                    !UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm) -> 2
                    else -> 3
                }
            )
        }

        when (step) {
            1 -> StepCard(
                icon = painterResource(R.drawable.ic_setup_key),
                title = stringResource(R.string.setup_step1_title, appName),
                instruction = stringResource(R.string.setup_step1_instruction, appName),
                actionText = stringResource(R.string.setup_step1_action),
            ) {
                val intent = Intent()
                intent.action = Settings.ACTION_INPUT_METHOD_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                launcher.launch(intent)
            }
            2 -> StepCard(
                icon = painterResource(R.drawable.ic_setup_select),
                title = stringResource(R.string.setup_step2_title, appName),
                instruction = stringResource(R.string.setup_step2_instruction, appName),
                actionText = stringResource(R.string.setup_step2_action),
                secondaryText = stringResource(R.string.setup_step3_action),
                onSecondary = close,
            ) {
                imm.showInputMethodPicker()
            }
            else -> StepCard(
                icon = painterResource(R.drawable.sym_keyboard_language_switch),
                title = stringResource(R.string.setup_step3_title),
                instruction = stringResource(R.string.setup_step3_instruction, appName),
                actionText = stringResource(R.string.setup_step3_action),
                secondaryText = stringResource(R.string.setup_finish_action),
                onSecondary = finish,
            ) {
                close()
            }
        }
    }
}

/** Three bars: how much of the setup is behind the user. */
@Composable
private fun StepProgress(current: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { index ->
            val done = index + 1 <= current
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (done) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun StepCard(
    icon: Painter,
    title: String,
    instruction: String,
    actionText: String,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null,
    action: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                instruction,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = action,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text(actionText)
            }
            if (secondaryText != null && onSecondary != null) {
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(secondaryText)
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme(previewDark) {
        Surface {
            WelcomeWizard({}) { }
        }
    }
}

@Preview(
    device = "spec:orientation=landscape,width=400dp,height=780dp"
)
@Composable
private fun WidePreview() {
    Theme(previewDark) {
        Surface {
            WelcomeWizard({}) { }
        }
    }
}
