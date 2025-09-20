package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Greeting("Android")
                }
            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var showWebView by remember { mutableStateOf(false) }
    var context = LocalContext.current

    Box(modifier = Modifier
        .windowInsetsPadding(WindowInsets.systemBars)){
        Column {
            Text(text = "Scan the aadhar card and fetch text",
                modifier = Modifier.padding(10.dp))
            
            AadhaarScreen()
        }
    }
}

@Composable
fun AadhaarScreen() {

    var capturedImage by remember {
        mutableStateOf<Bitmap?>(null)
    }

    var ocrResult by remember {
        mutableStateOf<String?>(null)
    }

    var showAlertDialog by remember {
        mutableStateOf<Boolean>(false)
    }

    var context = LocalContext.current
    Column {


    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center) {
        //if (capturedImage == null) {
            CameraPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.Center),
                onPhotoCaptured = { bitmap ->
                    capturedImage = bitmap
                    detectText(bitmap) { text ->
                        ocrResult = text
                        showAlertDialog = true
                    }
                }
            )

        if (showAlertDialog){
            ShowAlertDialog(ocrResult, onDismiss = {
                showAlertDialog = false
            })
        }
//        } else {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                capturedImage?.let { bmp ->
//                    Image(
//                        bitmap = bmp.asImageBitmap(),
//                        contentDescription = "Captured Aadhaar",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(300.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                            .border(2.dp, Color.Green, RoundedCornerShape(12.dp)),
//                        contentScale = ContentScale.Fit
//                    )
//                }
//
//                Text(text = "Extracted text : " + ocrResult.toString())
//
//            }
//        }
    }
  }
}

@Composable
fun ShowAlertDialog(ocrResult: String?, onDismiss : () -> Unit) {

    var showDialog by remember {
        mutableStateOf(false)
    }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(text = "Dismiss")
            }
        },
        title = {
            Text(text = "Text Extraction data $ocrResult")
        },

        text = {
            Text(text = "This is the content of the alert dialog.", color = Color.DarkGray)
        },
        // set padding for contents inside the box
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        iconContentColor = Color.Red,
        titleContentColor = Color.Black,
        textContentColor = Color.DarkGray,
        tonalElevation = 8.dp,
        properties = DialogProperties(dismissOnBackPress = true,
            dismissOnClickOutside = false)
        )
    
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPhotoCaptured : (Bitmap) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    // Create single ImageCapture instance (remember ensures it's stable)
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = modifier.fillMaxSize(),

            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)

                        }

                        // Unbind before rebinding
                        cameraProvider.unbindAll()

                        // Bind preview + imageCapture
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )

                    },
                    ContextCompat.getMainExecutor(ctx)
                )
                previewView
            }
        )

        // Green box overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.Center)
                .border(3.dp, Color.Green, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Place Aadhaar card here", color = Color.White)
        }

        // Capture button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 15.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(onClick = {
                captureImage(context, imageCapture, onPhotoCaptured)
            }) {
                Text(text = "Capture")
            }
        }
    }
}


fun captureImage(context: Context, imageCapture: ImageCapture, onPhotoCaptured: (Bitmap) -> Unit) {

    val photoFile = File(context.externalCacheDir,"aadharDoc${System.currentTimeMillis()}.jpg")
    val outputoptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputoptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                Toast.makeText(context,"Loading..",Toast.LENGTH_SHORT).show()
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                onPhotoCaptured(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context,"Error ${exception.message.toString()}",Toast.LENGTH_SHORT).show()
                Log.d("AADHAR","Exception is ${exception.message.toString()}")
                exception.printStackTrace()
            }
        }
    )
}

private fun detectText(bitmap: Bitmap, onResult : (String) -> Unit){
    val image = InputImage.fromBitmap(bitmap,0)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            Log.d("AADHAR","The text is $visionText")
            try {
                val regex = Regex("\\b\\d{4}\\s\\d{4}\\s\\d{4}\\b")

                var aadhar = regex.find(visionText.text)

                onResult(aadhar!!.value.toString() ?: "No aadhar has been found")

                val stopWords = listOf("GOVERNMENT", "INDIA", "AADHAAR", "DOB", "DATE",
                    "YEAR", "MALE", "FEMALE", "GENDER", "ADDRESS", "UNIQUE", "IDENTIFICATION")


                val allLines = visionText.textBlocks.flatMap { it.lines }.map { it.text }

                val dobIndex = allLines.indexOfFirst { it.uppercase().contains("DOB") || it.uppercase().contains("YEAR") }

                if (dobIndex > 0) {
                    for (i in dobIndex - 1 downTo maxOf(0, dobIndex - 3)) {
                        val candidate = allLines[i].trim()
                        val up = candidate.uppercase()
                        if (candidate.isNotBlank() && candidate.length >= 3 &&
                            !up.any { it.isDigit() } &&
                            stopWords.none { up.contains(it) }) {
                            Log.d("AADHAR","Name Text is $candidate")
                        }
                    }
                }

                val nameCandidates = visionText.textBlocks
                   .flatMap { it.lines }
                   .map { it.text.trim() }
                   .filter { isNameLike(it, stopWords) }

                Log.d("AADHAR","AAdhar name is ${nameCandidates.toString()}")

            }catch (e : Exception){
                e.printStackTrace()
                Log.d("AADHAR","Exception ${e.printStackTrace()}")
            }
            for (block in visionText.textBlocks){
                //Log.d("AADHAR","BLOCK TEXT ${block.text}")

                for (line in block.lines){
                    //Log.d("AADHAR","LINE TEXT ${line.text}")
                }
            }
        }

        .addOnFailureListener {
            it.printStackTrace()
            onResult(it.message.toString())
            Log.d("AADHAR","The error is ${it.printStackTrace()}")
        }

}

private fun isNameLike(line : String, stopWords : List<String>): Boolean {
    val up = line.uppercase()
    if (up.any{ it -> it.isDigit() }) return false
    if (stopWords.any { it -> up.contains(it) }) return false
    return line.isNotBlank() && line.length >= 3

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
