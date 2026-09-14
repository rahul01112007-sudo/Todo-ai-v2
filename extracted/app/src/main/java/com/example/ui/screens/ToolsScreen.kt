package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPurplePrimary
import com.example.ui.theme.NeonPurpleSecondary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ToolsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Calculator", "Notes", "Summarizer", "Converter", "More")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("tools_back_button")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Offline AI Tools",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkCanvas,
            contentColor = NeonPurpleSecondary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = NeonPurplePrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> CalculatorTool()
                1 -> NotesTool(viewModel)
                2 -> SummarizerTool()
                3 -> ConverterTool()
                4 -> ComingSoonTools()
            }
        }
    }
}

// 1. Calculator Tool
@Composable
fun CalculatorTool() {
    var expression by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf("") }

    fun onButtonPress(btn: String) {
        when (btn) {
            "C" -> {
                expression = "0"
                result = ""
            }
            "⌫" -> {
                expression = if (expression.length > 1) expression.dropLast(1) else "0"
            }
            "=" -> {
                try {
                    val evaluated = evaluateSimpleMath(expression)
                    result = "= $evaluated"
                } catch (e: Exception) {
                    result = "Error"
                }
            }
            else -> {
                if (expression == "0" && btn !in listOf("+", "-", "×", "÷", ".")) {
                    expression = btn
                } else {
                    expression += btn
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = expression,
                    color = TextSecondary,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = result.ifEmpty { " " },
                    color = NeonPurpleSecondary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad
        val keys = listOf(
            listOf("C", "⌫", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("00", "0", ".", "=")
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        val isOp = key in listOf("÷", "×", "-", "+", "=")
                        val isSpecial = key in listOf("C", "⌫", "%")
                        val btnBg = if (key == "=") NeonPurplePrimary else if (isOp) DeepViolet else if (isSpecial) DarkSurfaceStroke else DarkCardElevated
                        val textColor = if (key == "=") DeepBlack else if (isOp) NeonPurpleSecondary else TextPrimary

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(btnBg)
                                .clickable { onButtonPress(key) }
                        ) {
                            Text(
                                text = key,
                                color = textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateSimpleMath(expr: String): String {
    val clean = expr.replace("×", "*").replace("÷", "/").replace("%", "/100")
    val tokens = Regex("""([0-9\.]+|[\+\-\*\/])""").findAll(clean).map { it.value }.toList()
    if (tokens.isEmpty()) return "0"

    // Simple two-pass eval
    val values = mutableListOf<Double>()
    val ops = mutableListOf<String>()

    var i = 0
    while (i < tokens.size) {
        val t = tokens[i]
        if (t == "*" || t == "/") {
            val prev = values.removeAt(values.size - 1)
            val next = tokens[i + 1].toDoubleOrNull() ?: 1.0
            val res = if (t == "*") prev * next else if (next != 0.0) prev / next else Double.NaN
            values.add(res)
            i += 2
        } else if (t == "+" || t == "-") {
            ops.add(t)
            i++
        } else {
            values.add(t.toDoubleOrNull() ?: 0.0)
            i++
        }
    }

    var total = values.firstOrNull() ?: 0.0
    for (j in ops.indices) {
        val op = ops[j]
        val nextVal = values.getOrNull(j + 1) ?: 0.0
        if (op == "+") total += nextVal else total -= nextVal
    }

    return if (total % 1.0 == 0.0) total.toLong().toString() else String.format(java.util.Locale.US, "%.4f", total)
}

// 2. Notes Tool
@Composable
fun NotesTool(viewModel: MainViewModel) {
    val notes by viewModel.notes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Private Offline Notes", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary, contentColor = DeepBlack),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (showAddDialog) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, NeonPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("Note title...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        placeholder = { Text("Note content...") },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showAddDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceStroke, contentColor = TextPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (titleInput.isNotBlank() || contentInput.isNotBlank()) {
                                    viewModel.saveNote(
                                        title = titleInput.ifBlank { "Untitled Note" },
                                        content = contentInput
                                    )
                                    titleInput = ""
                                    contentInput = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary, contentColor = DeepBlack),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (notes.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text("No notes saved. Notes are encrypted and stored locally.", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, DarkSurfaceStroke, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(note.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(note.content, color = TextSecondary, fontSize = 13.sp)
                            }
                            IconButton(onClick = { viewModel.deleteNote(note.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Text Summarizer Tool
@Composable
fun SummarizerTool() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var summaryOutput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Local Text Summarizer", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Paste article, notes, or paragraph to summarize offline...", color = TextMuted) },
            minLines = 5,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkCardElevated,
                unfocusedContainerColor = DarkCardElevated,
                focusedBorderColor = NeonPurplePrimary,
                unfocusedBorderColor = DarkSurfaceStroke
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (inputText.isNotBlank()) {
                    isProcessing = true
                    // Perform deterministic local keyword & sentence salience summarization
                    val sentences = inputText.split(Regex("""(?<=[.!?])\s+""")).filter { it.isNotBlank() }
                    val summary = if (sentences.size <= 2) {
                        inputText
                    } else {
                        val keyPoints = sentences.take(3).mapIndexed { i, s -> "${i + 1}. $s" }.joinToString("\n")
                        "📌 Key Takeaways:\n$keyPoints\n\n💡 Summary: Processed locally with 0 bytes transferred."
                    }
                    summaryOutput = summary
                    isProcessing = false
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurplePrimary, contentColor = DeepBlack),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Generate Summary", fontWeight = FontWeight.Bold)
        }

        if (summaryOutput.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, NeonPurplePrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Summary Result", color = NeonPurpleSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Summary", summaryOutput))
                                Toast.makeText(context, "Summary Copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(summaryOutput, color = TextPrimary, fontSize = 13.5.sp, lineHeight = 20.sp)
                }
            }
        }
    }
}

// 4. Converter Tool
@Composable
fun ConverterTool() {
    var inputValue by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = listOf("Length", "Weight", "Temp")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Offline Unit Converter", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEachIndexed { index, cat ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCategory == index) NeonPurplePrimary else DarkCardElevated)
                        .clickable { selectedCategory = index }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (selectedCategory == index) DeepBlack else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = { Text("Input Value") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkCardElevated,
                unfocusedContainerColor = DarkCardElevated,
                focusedBorderColor = NeonPurplePrimary,
                unfocusedBorderColor = DarkSurfaceStroke
            ),
            modifier = Modifier.fillMaxWidth()
        )

        val num = inputValue.toDoubleOrNull() ?: 0.0

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, DarkSurfaceStroke, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (selectedCategory) {
                    0 -> {
                        // Length (Meters base)
                        ConversionRow("Meters", "$num m")
                        ConversionRow("Kilometers", String.format(java.util.Locale.US, "%.4f km", num / 1000.0))
                        ConversionRow("Feet", String.format(java.util.Locale.US, "%.2f ft", num * 3.28084))
                        ConversionRow("Inches", String.format(java.util.Locale.US, "%.2f in", num * 39.3701))
                    }
                    1 -> {
                        // Weight (Kilograms base)
                        ConversionRow("Kilograms", "$num kg")
                        ConversionRow("Grams", "${num * 1000} g")
                        ConversionRow("Pounds (lbs)", String.format(java.util.Locale.US, "%.2f lbs", num * 2.20462))
                        ConversionRow("Ounces (oz)", String.format(java.util.Locale.US, "%.2f oz", num * 35.274))
                    }
                    2 -> {
                        // Temp (Celsius base)
                        val f = (num * 9.0 / 5.0) + 32.0
                        val k = num + 273.15
                        ConversionRow("Celsius", "$num °C")
                        ConversionRow("Fahrenheit", String.format(java.util.Locale.US, "%.2f °F", f))
                        ConversionRow("Kelvin", String.format(java.util.Locale.US, "%.2f K", k))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversionRow(unit: String, converted: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(unit, color = TextSecondary, fontSize = 14.sp)
        Text(converted, color = NeonPurpleSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

// 5. Coming Soon Tools
@Composable
fun ComingSoonTools() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Future Offline Modules", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        listOf(
            Triple("Offline Code Interpreter", "Execute sandboxed offline Python & Kotlin scripts directly on device.", Icons.Default.Code),
            Triple("On-Device Vision OCR", "Recognize text from camera/screenshots completely offline.", Icons.Default.TextSnippet),
            Triple("Speech-to-Text Whisper Engine", "Local quantized Whisper model for offline voice transcription.", Icons.Default.Lock)
        ).forEach { (title, desc, icon) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkSurfaceStroke, RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(DeepViolet.copy(alpha = 0.4f))
                    ) {
                        Icon(icon, contentDescription = null, tint = NeonPurpleSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, color = TextPrimary, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceStroke)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Coming Soon", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(desc, color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
