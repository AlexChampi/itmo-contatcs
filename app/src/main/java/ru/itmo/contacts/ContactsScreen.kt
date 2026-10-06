package ru.itmo.contacts

import android.Manifest
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

private const val HERO_KEY = "hero"
private const val PERMISSION_KEY = "permission"

private enum class ItemType { Hero, Permission, SectionIndex, Contact }

@Composable
fun ContactsRoute(viewModel: ContactsViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var hasPermission by remember { mutableStateOf(context.hasContactsPermission()) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LifecycleResumeEffect(Unit) {
        hasPermission = context.hasContactsPermission()
        onPauseOrDispose { }
    }
    LaunchedEffect(Unit) {
        if (!hasPermission && !permissionRequested) {
            permissionRequested = true
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.onPermissionGranted()
    }

    ContactsScreen(
        hasPermission = hasPermission,
        uiState = uiState,
        onRequestPermission = {
            val canAskAgain =
                activity?.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) == true
            if (canAskAgain) {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            } else {
                context.openAppSettings()
            }
        },
        onContactClick = { contact -> context.dial(contact.phoneNumber) },
    )
}

@Composable
fun ContactsScreen(
    hasPermission: Boolean,
    uiState: ContactsUiState,
    onRequestPermission: () -> Unit,
    onContactClick: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val isHeroScrolledAway by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Scaffold(
        modifier = modifier,
        containerColor = OneUiColors.Background,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = OneUiDimens.ScreenPadding,
                    end = OneUiDimens.ScreenPadding,
                    bottom = OneUiDimens.CardSpacing,
                ),
            ) {
                item(key = HERO_KEY, contentType = ItemType.Hero) {
                    Hero(subtitle = heroSubtitle(hasPermission, uiState))
                }
                when {
                    !hasPermission -> item(key = PERMISSION_KEY, contentType = ItemType.Permission) {
                        PermissionCard(onRequestPermission = onRequestPermission)
                    }

                    uiState is ContactsUiState.Loaded -> uiState.sections.forEach { section ->
                        contactSection(section, onContactClick)
                    }
                }
            }
            AnimatedVisibility(visible = isHeroScrolledAway, enter = fadeIn(), exit = fadeOut()) {
                CollapsedHeader()
            }
        }
    }
}

private fun LazyListScope.contactSection(section: ContactSection, onContactClick: (Contact) -> Unit) {
    item(key = section.initial.toString(), contentType = ItemType.SectionIndex) {
        SectionIndex(initial = section.initial)
    }
    itemsIndexed(
        items = section.contacts,
        key = { _, contact -> contact.id },
        contentType = { _, _ -> ItemType.Contact },
    ) { index, contact ->
        ContactRow(
            contact = contact,
            isFirst = index == 0,
            isLast = index == section.contacts.lastIndex,
            onClick = { onContactClick(contact) },
        )
    }
}

@Composable
private fun heroSubtitle(hasPermission: Boolean, uiState: ContactsUiState): String =
    if (!hasPermission) {
        stringResource(R.string.no_permission)
    } else {
        when (uiState) {
            ContactsUiState.Loading -> stringResource(R.string.loading)
            is ContactsUiState.Loaded -> if (uiState.count == 0) {
                stringResource(R.string.no_contacts)
            } else {
                pluralStringResource(R.plurals.contacts_found, uiState.count, uiState.count)
            }
        }
    }

@Composable
private fun CollapsedHeader(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(OneUiColors.Background)
            .heightIn(min = OneUiDimens.HeaderHeight)
            .padding(horizontal = OneUiDimens.CardSpacing),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.semantics { heading() },
            color = OneUiColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = OneUiType.HeaderSmall,
        )
    }
}

@Composable
private fun Hero(subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.semantics { heading() },
            color = OneUiColors.Ink,
            textAlign = TextAlign.Center,
            style = OneUiType.TitleLarge,
        )
        Text(
            text = subtitle,
            color = OneUiColors.InkMuted,
            textAlign = TextAlign.Center,
            style = OneUiType.Subtitle,
        )
    }
}

@Composable
private fun PermissionCard(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(OneUiColors.Surface, RoundedCornerShape(OneUiDimens.CardRadius))
            .padding(OneUiDimens.CardSpacing),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.no_permission_message),
            color = OneUiColors.Ink,
            textAlign = TextAlign.Center,
            style = OneUiType.ListPrimary,
        )
        Button(
            onClick = onRequestPermission,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = OneUiColors.Pill,
                contentColor = OneUiColors.Ink,
            ),
        ) {
            Text(text = stringResource(R.string.grant_permission), style = OneUiType.Button)
        }
    }
}

@Composable
private fun SectionIndex(initial: Char, modifier: Modifier = Modifier) {
    Text(
        text = initial.toString(),
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            .semantics { heading() },
        color = OneUiColors.InkMuted,
        style = OneUiType.SectionIndex,
    )
}

@Composable
private fun ContactRow(
    contact: Contact,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape(isFirst, isLast))
            .background(OneUiColors.Surface),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClickLabel = stringResource(R.string.call_contact), onClick = onClick)
                .semantics { contentDescription = contact.name },
        )
        if (!isFirst) {
            HorizontalDivider(
                modifier = Modifier.padding(
                    start = OneUiDimens.ScreenPadding * 2 + OneUiDimens.AvatarSize,
                    end = OneUiDimens.ScreenPadding,
                ),
                color = OneUiColors.Divider,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = OneUiDimens.RowHeight)
                .padding(horizontal = OneUiDimens.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(OneUiDimens.ScreenPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(contact = contact)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    color = OneUiColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = OneUiType.ListPrimary,
                )
                if (contact.phoneNumber != contact.name) {
                    Text(
                        text = contact.phoneNumber,
                        color = OneUiColors.InkMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = OneUiType.ListSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun Avatar(contact: Contact, modifier: Modifier = Modifier) {
    val brush = remember(contact.name) { avatarBrush(contact.name) }
    Box(
        modifier = modifier
            .size(OneUiDimens.AvatarSize)
            .background(brush, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = contact.initial.toString(),
            color = Color.White,
            style = OneUiType.AvatarInitial,
        )
    }
}

private fun avatarBrush(name: String): Brush {
    val colors = OneUiColors.Avatars
    val index = name.hashCode().mod(colors.size)
    return Brush.linearGradient(listOf(colors[index], colors[(index + 1) % colors.size]))
}

private fun rowShape(isFirst: Boolean, isLast: Boolean): Shape {
    val top = if (isFirst) OneUiDimens.CardRadius else 0.dp
    val bottom = if (isLast) OneUiDimens.CardRadius else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

@Preview(showBackground = true)
@Composable
private fun ContactsScreenPreview() {
    val contacts = listOf(
        Contact(1, "Ана М", "+7 921 000-00-01"),
        Contact(2, "Антон", "+7 921 000-00-02"),
        Contact(3, "Борис", "+7 921 000-00-03"),
    )
    OneUiTheme {
        ContactsScreen(
            hasPermission = true,
            uiState = ContactsUiState.Loaded(contacts.groupByInitial(), contacts.size),
            onRequestPermission = {},
            onContactClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoPermissionPreview() {
    OneUiTheme {
        ContactsScreen(
            hasPermission = false,
            uiState = ContactsUiState.Loading,
            onRequestPermission = {},
            onContactClick = {},
        )
    }
}
