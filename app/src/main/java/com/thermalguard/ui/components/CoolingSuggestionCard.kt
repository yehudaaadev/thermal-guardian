package com.thermalguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thermalguard.R
import com.thermalguard.domain.model.CoolingSuggestion
import com.thermalguard.domain.model.SuggestionType
import com.thermalguard.util.HuaweiGuideHelper

@Composable
fun CoolingSuggestionCard(
    suggestions: List<CoolingSuggestion>,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) return

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.suggestions_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        suggestions.forEach { suggestion ->
            val icon = when (suggestion.type) {
                SuggestionType.BRIGHTNESS -> Icons.Default.BrightnessMedium
                SuggestionType.HOTSPOT -> Icons.Default.WifiTethering
                SuggestionType.CHARGING -> Icons.Default.PowerOff
                SuggestionType.REMOVE_CASE -> Icons.Default.PhoneAndroid
                SuggestionType.CLOSE_APPS -> Icons.Default.Close
                SuggestionType.REFRESH_RATE -> Icons.Default.Speed
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(suggestion.titleResId),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    Column {
                        Text(
                            text = stringResource(suggestion.titleResId),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(suggestion.descriptionResId),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (suggestion.actionResId != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            when (suggestion.type) {
                                SuggestionType.BRIGHTNESS, SuggestionType.REFRESH_RATE -> {
                                    HuaweiGuideHelper.openDisplaySettings(context)
                                }
                                SuggestionType.HOTSPOT -> {
                                    HuaweiGuideHelper.openHotspotSettings(context)
                                }
                                SuggestionType.CLOSE_APPS -> {
                                    HuaweiGuideHelper.openAppSettings(context)
                                }
                                else -> {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(suggestion.actionResId),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

