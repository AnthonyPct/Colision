package com.anthooop.colision.feature.onboarding.joincode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import colision.composeapp.generated.resources.Res
import colision.composeapp.generated.resources.action_back
import colision.composeapp.generated.resources.action_continue
import colision.composeapp.generated.resources.join_code_banner_resolved
import colision.composeapp.generated.resources.join_code_error_generic
import colision.composeapp.generated.resources.join_code_error_invalid
import colision.composeapp.generated.resources.join_code_error_offline
import colision.composeapp.generated.resources.join_code_subtitle
import colision.composeapp.generated.resources.join_code_title
import com.anthooop.colision.app.ColisionTheme
import com.anthooop.colision.core.common.AppError
import com.anthooop.colision.core.design.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun JoinCodeScreen(
    state: JoinCodeState,
    onIntent: (JoinCodeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safe = WindowInsets.safeDrawing.asPaddingValues()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = Spacing.SP6,
                end = Spacing.SP6,
                top = Spacing.SP4 + safe.calculateTopPadding(),
                bottom = Spacing.SP8 + safe.calculateBottomPadding(),
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.SP4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onIntent(JoinCodeIntent.BackTapped) }) {
                Text(
                    text = stringResource(Res.string.action_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Text(
            text = stringResource(Res.string.join_code_title),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(Spacing.SP2))
        Text(
            text = stringResource(Res.string.join_code_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.SP7))

        // Hidden BasicTextField captures the input; OTP boxes render the value.
        Box {
            BasicTextField(
                value = TextFieldValue(state.code, selection = androidx.compose.ui.text.TextRange(state.code.length)),
                onValueChange = { tfv -> onIntent(JoinCodeIntent.CodeChanged(tfv.text)) },
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done,
                ),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
            )
            OtpRow(code = state.code, onTap = { focusRequester.requestFocus() })
        }

        if (state.resolvedProjectName != null) {
            Spacer(Modifier.height(Spacing.SP4))
            SuccessBanner(projectName = state.resolvedProjectName)
        } else if (state.error != null) {
            Spacer(Modifier.height(Spacing.SP4))
            ErrorBanner(error = state.error)
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                focusManager.clearFocus()
                onIntent(JoinCodeIntent.SubmitTapped)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = state.canSubmit,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(horizontal = Spacing.SP6),
        ) {
            Text(
                text = stringResource(Res.string.action_continue),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun OtpRow(code: String, onTap: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SP2),
    ) {
        repeat(6) { i ->
            val ch = code.getOrNull(i)?.toString().orEmpty()
            val isCursor = code.length == i
            val borderColor = when {
                ch.isNotEmpty() -> MaterialTheme.colorScheme.primary
                isCursor -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(width = 1.75.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onTap,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ch,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}


@Composable
private fun SuccessBanner(projectName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(14.dp),
            )
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.join_code_banner_resolved, projectName),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun ErrorBanner(error: AppError) {
    val message = when (error) {
        AppError.ProjectCodeInvalid -> stringResource(Res.string.join_code_error_invalid)
        AppError.NetworkUnavailable, AppError.ServerUnreachable ->
            stringResource(Res.string.join_code_error_offline)
        else -> stringResource(Res.string.join_code_error_generic)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.errorContainer,
                RoundedCornerShape(14.dp),
            )
            .padding(horizontal = Spacing.SP4, vertical = Spacing.SP3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Preview
@Composable
private fun JoinCodeScreenEmptyPreview() {
    ColisionTheme {
        JoinCodeScreen(state = JoinCodeState(), onIntent = {})
    }
}

@Preview
@Composable
private fun JoinCodeScreenResolvedPreview() {
    ColisionTheme {
        JoinCodeScreen(
            state = JoinCodeState(
                code = "KQ7H2P",
                resolvedProjectName = "Conseil municipal de Saint-Machin",
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun JoinCodeScreenInvalidPreview() {
    ColisionTheme {
        JoinCodeScreen(
            state = JoinCodeState(code = "ZZZZZZ", error = AppError.ProjectCodeInvalid),
            onIntent = {},
        )
    }
}
