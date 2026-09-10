package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SellerEntity
import com.example.data.model.RankingPeriod
import com.example.ui.theme.FzCyanSecondary
import com.example.ui.theme.FzPurpleLight
import com.example.ui.theme.FzPurplePrimary
import com.example.ui.theme.FzStatusAmber
import com.example.ui.theme.FzStatusGreen
import com.example.ui.theme.FzSurfaceBorder
import com.example.ui.theme.FzSurfaceCard
import com.example.ui.theme.FzSurfaceElevated
import com.example.ui.theme.FzTextPrimary
import com.example.ui.theme.FzTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SellersScreen(
    sellers: List<SellerEntity>,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = RANKING, 1 = FIND SELLERS
    var selectedPeriod by remember { mutableStateOf(RankingPeriod.ALL_TIME) }
    var selectedScope by remember { mutableStateOf("GLOBAL") } // "GLOBAL" or "COUNTRY"
    var searchQuery by remember { mutableStateOf("") }
    var selectedCountryFilter by remember { mutableStateOf("All") }

    val countries = remember(sellers) {
        listOf("All") + sellers.map { it.country }.distinct()
    }

    val filteredSellers = remember(sellers, searchQuery, selectedCountryFilter, selectedTab) {
        sellers.filter { seller ->
            val matchesSearch = seller.displayName.contains(searchQuery, ignoreCase = true) ||
                    seller.territory.contains(searchQuery, ignoreCase = true) ||
                    seller.country.contains(searchQuery, ignoreCase = true)
            val matchesCountry = selectedCountryFilter == "All" || seller.country == selectedCountryFilter
            matchesSearch && matchesCountry
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 96.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Sub-tabs: RANKING / FIND SELLERS
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = FzSurfaceElevated,
            contentColor = FzTextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = FzPurplePrimary,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, FzSurfaceBorder, RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SELLER RANKINGS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FIND SELLERS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // RANKING VIEW
            // Filters Row: Time Period
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(RankingPeriod.entries) { period ->
                    val isSelected = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) FzPurplePrimary else FzSurfaceCard)
                            .border(1.dp, if (isSelected) FzPurplePrimary else FzSurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedPeriod = period }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = period.displayName,
                            color = if (isSelected) Color.White else FzTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scope Selector: Global vs Regional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP LICENSED DISTRIBUTORS",
                    color = FzCyanSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FzSurfaceCard)
                        .border(1.dp, FzSurfaceBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedScope == "GLOBAL") FzPurplePrimary else Color.Transparent)
                            .clickable { selectedScope = "GLOBAL" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("GLOBAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selectedScope == "GLOBAL") Color.White else FzTextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedScope == "COUNTRY") FzPurplePrimary else Color.Transparent)
                            .clickable { selectedScope = "COUNTRY" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("REGIONAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selectedScope == "COUNTRY") Color.White else FzTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sellers Ranking List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(sellers) { index, seller ->
                    RankingSellerCard(rank = index + 1, seller = seller, onShowSnackbar = onShowSnackbar)
                }
            }
        } else {
            // FIND SELLERS VIEW
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_sellers_input"),
                placeholder = { Text("Search by seller name, country, or territory...", color = FzTextSecondary.copy(alpha = 0.6f), fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FzTextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Clear", tint = FzCyanSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FzPurplePrimary,
                    unfocusedBorderColor = FzSurfaceBorder,
                    focusedContainerColor = FzSurfaceCard,
                    unfocusedContainerColor = FzSurfaceCard
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Country Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(countries) { country ->
                    val isSelected = selectedCountryFilter == country
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) FzCyanSecondary.copy(alpha = 0.2f) else FzSurfaceCard)
                            .border(1.dp, if (isSelected) FzCyanSecondary else FzSurfaceBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedCountryFilter = country }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = country,
                            color = if (isSelected) FzCyanSecondary else FzTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Safe Buying Advice Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF131A2B))
                    .border(1.dp, FzCyanSecondary.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = FzCyanSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SAFE BUYING GUARANTEE",
                        color = FzCyanSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Only purchase from sellers with the Verified badge. Never disclose your passwords. Digital license keys are authenticated in-app.",
                        color = FzTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtered Sellers
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSellers) { seller ->
                    FindSellerCard(seller = seller, onShowSnackbar = onShowSnackbar)
                }
            }
        }
    }
}

@Composable
private fun RankingSellerCard(
    rank: Int,
    seller: SellerEntity,
    onShowSnackbar: (String) -> Unit
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> FzTextSecondary
    }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurfaceElevated)
            .border(
                1.dp,
                if (rank <= 3) rankColor.copy(alpha = 0.4f) else FzSurfaceBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (rank <= 3) rankColor.copy(alpha = 0.15f) else FzSurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                color = rankColor,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Avatar monogram
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FzSurfaceCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = seller.avatarTag,
                color = FzPurpleLight,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Seller Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = seller.displayName,
                    color = FzTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (seller.verificationStatus) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Seller",
                        tint = FzCyanSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${seller.country} • ${seller.territory}",
                color = FzTextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = FzStatusAmber, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = "%.2f".format(seller.rating), color = FzTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${seller.totalSales} sales", color = FzTextSecondary, fontSize = 11.sp)
            }
        }

        // Amount sold
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currencyFormatter.format(seller.amountSoldUsd),
                color = FzStatusGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = "Volume",
                color = FzTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FindSellerCard(
    seller: SellerEntity,
    onShowSnackbar: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FzSurfaceElevated)
            .border(1.dp, FzSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(FzPurplePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = seller.avatarTag,
                            color = FzPurpleLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = seller.displayName,
                                color = FzTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (seller.verificationStatus) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Seller",
                                    tint = FzCyanSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = "${seller.country} (${seller.countryCode}) • ${seller.territory}",
                            color = FzTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(FzSurfaceCard)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = FzStatusAmber, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "%.2f".format(seller.rating), color = FzTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Published Contact Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FzSurfaceCard)
                    .border(1.dp, FzSurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = FzCyanSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = seller.publishedContactMethod,
                        color = FzTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(seller.publishedContactMethod))
                        onShowSnackbar("Copied contact to clipboard: ${seller.publishedContactMethod}")
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Contact",
                        tint = FzTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Action Button
            Button(
                onClick = {
                    val raw = seller.publishedContactMethod
                    val intent = when {
                        raw.contains("@") && raw.contains(".") -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$raw"))
                        raw.startsWith("@") -> Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/${raw.removePrefix("@")}"))
                        raw.contains("t.me") -> Intent(Intent.ACTION_VIEW, Uri.parse("https://${raw.substringAfter("https://").substringAfter("http://")}"))
                        else -> {
                            clipboard.setText(AnnotatedString(seller.publishedContactMethod))
                            onShowSnackbar("Contact details copied to clipboard: $raw")
                            null
                        }
                    }
                    if (intent != null) {
                        try {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            clipboard.setText(AnnotatedString(seller.publishedContactMethod))
                            onShowSnackbar("Copied contact info to clipboard.")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FzPurplePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("CONNECT WITH SELLER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}
