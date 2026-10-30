package co.edu.unicauca.urbanguard.ui.features.news

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.unicauca.urbanguard.core.common.NewsConstants
import co.edu.unicauca.urbanguard.ui.components.UrbanGuardTopBar
import co.edu.unicauca.urbanguard.ui.theme.UgBackground
import co.edu.unicauca.urbanguard.ui.theme.UgBorder
import co.edu.unicauca.urbanguard.ui.theme.UgErrorText
import co.edu.unicauca.urbanguard.ui.theme.UgNavy
import co.edu.unicauca.urbanguard.ui.theme.UgTextPrimary
import co.edu.unicauca.urbanguard.ui.theme.UgTextSecondary

@Composable
fun PublishNewsScreen(
    onPublished: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PublishNewsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isPublished) {
        if (uiState.isPublished) {
            onPublished()
            viewModel.resetPublished()
        }
    }

    PublishNewsContent(
        uiState = uiState,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onPublish = viewModel::publish,
        onNavigateBack = onNavigateBack,
        onAttachmentSelected = viewModel::onAttachmentSelected,
        onClearAttachment = viewModel::clearAttachment
    )
}

@Composable
private fun PublishNewsContent(
    uiState: PublishNewsUiState,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onPublish: () -> Unit,
    onNavigateBack: () -> Unit,
    onAttachmentSelected: (Uri?) -> Unit,
    onClearAttachment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UgBackground)
    ) {
        UrbanGuardTopBar(
            title = NewsConstants.NEWS_SCREEN_TITLE,
            onNavigateBack = onNavigateBack
        )
        PublishNewsForm(
            uiState = uiState,
            onTitleChange = onTitleChange,
            onBodyChange = onBodyChange,
            onPublish = onPublish,
            onAttachmentSelected = onAttachmentSelected,
            onClearAttachment = onClearAttachment
        )
    }
}

@Composable
private fun PublishNewsForm(
    uiState: PublishNewsUiState,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onPublish: () -> Unit,
    onAttachmentSelected: (Uri?) -> Unit,
    onClearAttachment: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        PublishNewsHeader()
        PublishNewsFields(uiState, onTitleChange, onBodyChange)
        AttachmentSection(uiState, onAttachmentSelected, onClearAttachment)
        PublishNewsFooter(uiState, onPublish)
    }
}

@Composable
private fun PublishNewsHeader() {
    Text(
        text = NewsConstants.PUBLISH_BUTTON_TEXT,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = UgTextPrimary
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun PublishNewsFields(
    uiState: PublishNewsUiState,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit
) {
    OutlinedTextField(
        value = uiState.title,
        onValueChange = onTitleChange,
        label = { Text(NewsConstants.TITLE_LABEL) },
        supportingText = { Text("${uiState.title.length}/${NewsConstants.TITLE_MAX_LENGTH}") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UgNavy,
            unfocusedBorderColor = UgBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = uiState.body,
        onValueChange = onBodyChange,
        label = { Text(NewsConstants.BODY_LABEL) },
        supportingText = { Text("${uiState.body.length}/${NewsConstants.BODY_MAX_LENGTH}") },
        minLines = 6,
        maxLines = 10,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UgNavy,
            unfocusedBorderColor = UgBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    )
    if (uiState.errorMessage != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = uiState.errorMessage.orEmpty(),
            color = UgErrorText,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun AttachmentSection(
    uiState: PublishNewsUiState,
    onAttachmentSelected: (Uri?) -> Unit,
    onClearAttachment: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        onAttachmentSelected(uri)
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = NewsConstants.ATTACH_BUTTON_TEXT,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = UgTextPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = NewsConstants.ATTACH_HELP_TEXT,
        fontSize = 12.sp,
        color = UgTextSecondary
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(
        onClick = { launcher.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.AttachFile, contentDescription = null)
        Spacer(modifier = Modifier.size(6.dp))
        Text("Seleccionar archivo")
    }
    if (uiState.attachmentName != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val icon = if (uiState.attachmentMime == "application/pdf") {
                Icons.Default.Description
            } else {
                Icons.Default.Image
            }
            Icon(icon, contentDescription = null, tint = UgNavy)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "${uiState.attachmentName} ${uiState.attachmentSize?.let { "(${it / 1024} KB)" } ?: ""}",
                fontSize = 13.sp,
                color = UgTextPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClearAttachment) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        }
    }
    if (uiState.attachmentError != null) {
        Text(
            text = uiState.attachmentError.orEmpty(),
            color = UgErrorText,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun PublishNewsFooter(
    uiState: PublishNewsUiState,
    onPublish: () -> Unit
) {
    Spacer(modifier = Modifier.height(20.dp))
    val isValid = uiState.title.trim().isNotEmpty() && uiState.body.trim().isNotEmpty()
    Button(
        onClick = onPublish,
        enabled = isValid && !uiState.isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = UgNavy,
            disabledContainerColor = UgBorder
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text(
            text = if (uiState.isLoading) "Publicando..." else NewsConstants.PUBLISH_ACTION,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    val limitText = "Límite: título ${NewsConstants.TITLE_MAX_LENGTH} caracteres, " +
        "cuerpo ${NewsConstants.BODY_MAX_LENGTH}."
    Text(
        text = limitText,
        fontSize = 12.sp,
        color = UgTextSecondary
    )
}
