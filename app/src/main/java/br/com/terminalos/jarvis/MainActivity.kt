package br.com.terminalos.jarvis

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Bg = Color(0xFF071015)
private val Panel = Color(0xFF101F27)
private val Neon = Color(0xFF50F5C2)
private val Cyan = Color(0xFF50DDF5)
private val Muted = Color(0xFF91A9B2)

class MainActivity : ComponentActivity() {
    private lateinit var workspace: File
    private val folderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workspace = File(filesDir, "workspace").apply { mkdirs() }
        setContent { JarvisApp(this, workspace, onPickFolder = { folderPicker.launch(null) }) }
    }
}

@Composable
private fun JarvisApp(context: Context, workspace: File, onPickFolder: () -> Unit) {
    var tab by remember { mutableStateOf("Terminal") }
    var command by remember { mutableStateOf("") }
    var output by remember { mutableStateOf(listOf("TERMINAL OS JARVIS // pronto", "Digite help para ver os comandos disponíveis.")) }
    var editorText by remember { mutableStateOf("") }
    var editorName by remember { mutableStateOf("main.py") }
    var assistantText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("ONLINE") }
    val tabs = listOf("Terminal", "Editor", "Arquivos", "Lembretes", "Sistema")

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Neon, secondary = Cyan,
        onBackground = Color(0xFFE8F5F4), onSurface = Color(0xFFE8F5F4)
    )) {
        Scaffold(containerColor = Bg, bottomBar = {
            NavigationBar(containerColor = Panel) {
                val icons = listOf(Icons.Default.Terminal, Icons.Default.Code, Icons.Default.Folder, Icons.Default.Notifications, Icons.Default.Settings)
                tabs.forEachIndexed { index, name ->
                    NavigationBarItem(selected = tab == name, onClick = { tab = name },
                        icon = { Icon(icons[index], contentDescription = name) },
                        label = { Text(name, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Neon, indicatorColor = Color(0xFF1C3940)))
                }
            }
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("TERMINAL OS", color = Neon, fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                        Text("J.A.R.V.I.S.", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("● $status", color = Neon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("CORE // MOBILE", color = Muted, fontSize = 10.sp)
                    }
                }
                when (tab) {
                    "Terminal" -> {
                        CardBlock("CONSOLE DE COMANDOS", "Workspace protegido • ${workspace.name}") {
                            LazyColumn(Modifier.weight(1f, fill = false).heightIn(min = 180.dp, max = 360.dp)) {
                                items(output) { line ->
                                    Text(line, color = if (line.startsWith("ERRO")) Color(0xFFFF8A8A) else if (line.startsWith(">")) Neon else Color(0xFFD0E3E5),
                                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.padding(vertical = 3.dp))
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("›", color = Neon, fontSize = 22.sp)
                                BasicTextField(command, { command = it }, Modifier.weight(1f).padding(8.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                                    singleLine = true, decorationBox = { inner -> if (command.isEmpty()) Text("digite um comando…", color = Muted); inner() })
                                IconButton(onClick = {
                                    val result = Terminal.run(context, workspace, command)
                                    output = (output + "> $command" + result).takeLast(160)
                                    command = ""
                                }) { Icon(Icons.Default.Send, "Executar", tint = Neon) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        CardBlock("CENTRAL DE COMANDO", "Ações locais, sem acesso root") {
                            BasicTextField(assistantText, { assistantText = it }, Modifier.fillMaxWidth().background(Bg, RoundedCornerShape(8.dp)).padding(12.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                decorationBox = { inner -> if (assistantText.isEmpty()) Text("Ex.: criar uma pasta chamada Projetos", color = Muted); inner() })
                            Button(onClick = {
                                val intent = assistantText.lowercase(Locale.ROOT)
                                val result = when {
                                    "abrir terminal" in intent -> { tab = "Terminal"; "Terminal aberto." }
                                    "configurações de wi-fi" in intent || "configuracoes de wi-fi" in intent -> { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)); "Configurações de Wi-Fi abertas." }
                                    "criar uma pasta chamada " in intent -> {
                                        val name = intent.substringAfter("criar uma pasta chamada ").trim().replace(Regex("[^a-z0-9_-]"), "_")
                                        if (name.isBlank()) "Informe o nome da pasta." else if (File(workspace, name).mkdirs()) "Pasta $name criada." else "Pasta já existe ou não foi possível criar."
                                    }
                                    "informações da rede" in intent || "informacoes da rede" in intent -> Terminal.run(context, workspace, "wifi").joinToString("\n")
                                    else -> "Não reconheci essa ação. Tente: criar uma pasta chamada Projetos, abrir terminal ou configurações de Wi-Fi."
                                }
                                output = (output + result).takeLast(160); assistantText = ""
                            }, modifier = Modifier.fillMaxWidth()) { Text("EXECUTAR AÇÃO") }
                        }
                    }
                    "Editor" -> {
                        CardBlock("EDITOR DE TEXTO", "Edite e salve arquivos no workspace local") {
                            OutlinedTextField(editorName, { editorName = it }, label = { Text("Nome do arquivo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            OutlinedTextField(editorText, { editorText = it }, label = { Text("Código") }, modifier = Modifier.fillMaxWidth().height(260.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    val safe = editorName.substringAfterLast('/').takeIf { it.matches(Regex("[A-Za-z0-9_.-]+")) } ?: "main.py"
                                    File(workspace, safe).writeText(editorText); editorName = safe
                                    output = output + "Salvo: $safe"
                                }, modifier = Modifier.weight(1f)) { Text("SALVAR") }
                            }
                            Text("Editor de texto simples. Não executa código nem inclui runtimes de programação.", color = Muted, fontSize = 11.sp)
                        }
                    }
                    "Arquivos" -> {
                        CardBlock("WORKSPACE", "Arquivos privados do aplicativo") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onPickFolder, modifier = Modifier.weight(1f)) { Text("IMPORTAR PASTA") }
                                Button(onClick = { File(workspace, "Nova pasta").mkdirs(); output += "Pasta Nova pasta criada." }, modifier = Modifier.weight(1f)) { Text("NOVA PASTA") }
                            }
                            LazyColumn(Modifier.heightIn(max = 430.dp)) {
                                val list = workspace.listFiles()?.sortedBy { it.name } ?: emptyList()
                                items(list) { f ->
                                    Row(Modifier.fillMaxWidth().clickable {
                                        if (f.isFile) { editorName = f.name; editorText = f.readText(); tab = "Editor" }
                                    }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(if (f.isDirectory) Icons.Default.Folder else Icons.Default.Description, null, tint = Cyan)
                                        Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                            Text(f.name, color = Color.White)
                                            Text(if (f.isDirectory) "Pasta" else "${f.length()} bytes", color = Muted, fontSize = 11.sp)
                                        }
                                        IconButton(onClick = { if (f.isDirectory) f.deleteRecursively() else f.delete(); output += "Removido: ${f.name}" }) { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF8A8A)) }
                                    }
                                    HorizontalDivider(color = Color(0xFF233A42))
                                }
                            }
                        }
                    }
                    "Lembretes" -> {
                        var title by remember { mutableStateOf("") }
                        var details by remember { mutableStateOf("") }
                        CardBlock("LEMBRETES", "Persistência local • agendamento simples") {
                            OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(details, { details = it }, label = { Text("Data/hora ISO (ex.: 2026-12-31T19:00)") }, modifier = Modifier.fillMaxWidth())
                            Button(onClick = {
                                val file = File(workspace, "lembretes.txt")
                                file.appendText("${title.ifBlank { "Lembrete" }} | ${details.ifBlank { "Sem data" }}\n")
                                output += "Lembrete salvo na lista local. Este build ainda não agenda alarmes/notificações."
                                title = ""; details = ""
                            }, modifier = Modifier.fillMaxWidth()) { Text("SALVAR LEMBRETE") }
                            Text(File(workspace, "lembretes.txt").takeIf { it.exists() }?.readText().orEmpty().ifBlank { "Nenhum lembrete salvo." }, color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            Text("Nota: alarmes do sistema e notificações agendadas não estão implementados nesta versão.", color = Color(0xFFFFD180), fontSize = 11.sp)
                        }
                    }
                    else -> {
                        CardBlock("DIAGNÓSTICO DO DISPOSITIVO", "Informações acessíveis por APIs públicas") {
                            InfoRow("Modelo", "${Build.MANUFACTURER} ${Build.MODEL}")
                            InfoRow("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                            InfoRow("App", "Terminal OS JARVIS 1.0.0")
                            InfoRow("Rede", networkState(context))
                            InfoRow("Armazenamento app", "${workspace.walkTopDown().filter { it.isFile }.sumOf { it.length() } / 1024} KB usados")
                            InfoRow("Workspace", workspace.absolutePath)
                            Button(onClick = { context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("CONFIGURAÇÕES DE WI-FI") }
                            Button(onClick = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }, modifier = Modifier.fillMaxWidth()) { Text("CONFIGURAÇÕES DE BLUETOOTH") }
                            Text("Sem root. O Android restringe informações privadas, senhas de rede e controle direto de rádios.", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardBlock(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = Neon, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text(subtitle, color = Muted, fontSize = 11.sp)
        content()
    }
    }
}
@Composable private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Text(label, color = Muted, modifier = Modifier.width(112.dp), fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}
private fun networkState(context: Context): String {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return "Desconectado"
    val caps = cm.getNetworkCapabilities(network) ?: return "Indisponível"
    return when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi conectado"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Rede móvel conectada"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        else -> "Conectado"
    }
}

private object Terminal {
    fun run(context: Context, root: File, raw: String): List<String> {
        if (raw.isBlank()) return listOf("ERRO: digite um comando.")
        val parts = raw.trim().split(Regex("\\s+"), limit = 3)
        val cmd = parts[0].lowercase(Locale.ROOT)
        val arg = parts.getOrNull(1).orEmpty()
        fun safe(path: String): File {
            val f = File(root, path).canonicalFile
            require(f.path == root.canonicalPath || f.path.startsWith(root.canonicalPath + File.separator)) { "Caminho fora do workspace bloqueado." }
            return f
        }
        return try {
            when (cmd) {
                "help" -> listOf("Comandos: help, clear, date, device, pwd, ls, mkdir, touch, cat, write, cp, mv, rm, find, grep, ipconfig, wifi, bluetooth, settings, reminder")
                "clear" -> listOf("LIMPAR: o histórico visual será substituído pelo próximo resultado.")
                "date" -> listOf(SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale("pt", "BR")).format(Date()))
                "device" -> listOf("${Build.MANUFACTURER} ${Build.MODEL} • Android ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}")
                "pwd" -> listOf(root.absolutePath)
                "ls" -> (safe(arg.ifBlank { "." }).listFiles()?.sortedBy { it.name }?.map { (if (it.isDirectory) "[DIR] " else "[FILE] ") + it.name } ?: listOf("Diretório vazio ou inexistente."))
                "mkdir" -> { require(arg.isNotBlank()) { "Uso: mkdir nome" }; if (safe(arg).mkdirs()) listOf("Pasta criada.") else listOf("ERRO: não foi possível criar (já existe?).") }
                "touch" -> { require(arg.isNotBlank()) { "Uso: touch arquivo" }; val f = safe(arg); f.parentFile?.mkdirs(); if (f.exists()) listOf("Arquivo já existe.") else if (f.createNewFile()) listOf("Arquivo criado.") else listOf("ERRO: falha ao criar arquivo.") }
                "cat" -> { require(arg.isNotBlank()) { "Uso: cat arquivo" }; listOf(safe(arg).readText().take(12000)) }
                "write" -> { require(parts.size == 3) { "Uso: write arquivo conteúdo" }; val f = safe(arg); f.parentFile?.mkdirs(); f.writeText(parts[2]); listOf("Conteúdo gravado em $arg") }
                "cp" -> { require(parts.size == 3) { "Uso: cp origem destino" }; val src = safe(arg); val dst = safe(parts[2]); if (src.isDirectory) error("cp não copia diretórios.") else { dst.parentFile?.mkdirs(); src.copyTo(dst, overwrite = true); listOf("Copiado.") } }
                "mv" -> { require(parts.size == 3) { "Uso: mv origem destino" }; val src = safe(arg); val dst = safe(parts[2]); dst.parentFile?.mkdirs(); if (src.renameTo(dst)) listOf("Movido.") else listOf("ERRO: não foi possível mover.") }
                "rm" -> { require(arg.isNotBlank() && arg != ".") { "Uso: rm caminho (não aceita .)" }; val f = safe(arg); if (!f.exists()) listOf("Nada para remover.") else { require(f != root) { "Remoção da raiz bloqueada." }; if (f.isDirectory) f.deleteRecursively() else f.delete(); listOf("Removido: $arg") } }
                "find" -> { val base = safe(arg.ifBlank { "." }); base.walkTopDown().take(500).map { it.relativeTo(root).path }.toList().ifEmpty { listOf("Nenhum arquivo.") } }
                "grep" -> { require(parts.size == 3) { "Uso: grep texto arquivo" }; safe(parts[2]).readLines().mapIndexedNotNull { i, line -> if (arg in line) "${i+1}: $line" else null }.ifEmpty { listOf("Nenhuma ocorrência.") } }
                "ipconfig", "wifi" -> listOf("Rede: ${networkState(context)}", "IP local detalhado depende das restrições e permissões da versão Android.")
                "bluetooth" -> { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); listOf("Configurações de Bluetooth abertas.") }
                "settings" -> { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); listOf("Configurações do Android abertas.") }
                "python" -> listOf("Comando removido: este aplicativo não inclui linguagens ou runtimes de programação.")
                "reminder" -> listOf("Use a aba Lembretes. Esta versão salva entradas localmente, mas não agenda notificações.")
                else -> listOf("ERRO: comando desconhecido '$cmd'. Digite help.")
            }
        } catch (e: Exception) { listOf("ERRO: ${e.message ?: "operação falhou"}") }
    }
}
