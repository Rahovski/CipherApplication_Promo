package shp.alpfa.cipherapp
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.opencv.android.OpenCVLoader
import org.opencv.imgproc.Imgproc
import org.opencv.android.Utils
import org.opencv.core.*

class MainActivity : AppCompatActivity() {

    val buttonCaptureName: String? = "Детектор символа"
    var tfLiteModel: Interpreter? = null
    val abcCipher = "АБВГДЕЖЗИЙКЛМНОПРСТУХЧШЫЬЯ"
    val infoText = "https://informatics.ru/"
    val buttonAbcInfo = "ШифроАлфавит"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        answerTextView.isVisible = false
        infoButton.text = infoText
        tfLiteModel = Interpreter(loadModelFile("model70v5.tflite"))
        cameraButton.text = buttonCaptureName
        goToabc.text = buttonAbcInfo
        if(OpenCVLoader.initDebug()){
            Toast.makeText(this, "openCv successfully loaded", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "openCv cannot be loaded", Toast.LENGTH_SHORT).show()
        }
        cameraButton.setOnClickListener {
            dispatchTakePictureIntent()
        }
        infoButton.setOnClickListener {
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("Выберите удобный способ для связи!")
//            val items = arrayOf("+7 (495) 150-64-32", "info@informatics.ru", "https://informatics.ru/")
//            builder.setItems(items) { dialog, which ->
//                when(which){
//                    0 -> startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", items[which], null)))
//                    1 -> callClientEmail(items[which])
//                    2 -> callBrowser(items[which])
//                }
//            }
//            builder.show()
            callBrowser(infoText)
        }
        goToabc.setOnClickListener {
            val goToABC = Intent(this, abcInfo::class.java)
            startActivity(goToABC)
        }
    }

    private fun callBrowser(parseString: String){
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(parseString)
        startActivity(openURL)
    }

    private fun callClientEmail(sendString: String){
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType("text/plain")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, sendString)
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Write here")
        startActivity(emailIntent)
    }

    private fun loadModelFile(path: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(path)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Take a photo with a camera app
    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
            takePictureIntent -> takePictureIntent.resolveActivity(packageManager)?.also {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    // Launch after capture Image on Camera and send to imageView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val value: Bitmap? = data?.extras?.get("data") as Bitmap
            clientImgView.setImageBitmap(value)
            var bitmap = (clientImgView.drawable as BitmapDrawable).bitmap
            bitMapToGrey(bitmap)
            findContour(bitmap)
            answerTextView.isVisible = true
            convertToAr(bitmap)
            //checkDetect(bitmap)
        }
    }


    private fun bitMapToGrey(tmp_bit: Bitmap){
        val img = Mat()
        val grayImg = Mat()
        val img2 = Bitmap.createBitmap(tmp_bit)
        Utils.bitmapToMat(img2, img)
        Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_BGR2GRAY)
        Imgproc.resize(grayImg, grayImg, Size(70.0, 70.0),0.0,0.0)
        val img3 = Bitmap.createBitmap(70,70,Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(grayImg, img3)
        clientImgView.setImageBitmap(img3)
    }

    private fun findContour(tmp_bit: Bitmap){
        val screenMat = Mat()
        Utils.bitmapToMat(tmp_bit, screenMat)
        Imgproc.cvtColor(screenMat,screenMat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.adaptiveThreshold(screenMat,screenMat,255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY, 11, 7.0)
        val img3 = Bitmap.createBitmap(screenMat.width(),screenMat.height(),Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(screenMat, img3)
        clientImgView.setImageBitmap(img3)
    }

    private fun convertToAr(tmp_bit: Bitmap){
        val screenMat = Mat()
        Utils.bitmapToMat(tmp_bit, screenMat)
        Imgproc.cvtColor(screenMat,screenMat, Imgproc.COLOR_BGR2GRAY)
        Imgproc.resize(screenMat,screenMat, Size(70.0, 70.0), 0.0, 0.0)
        val inputs = FloatArray(4900)
        var inp = 0
        for (i in 0..69){
            for (j in 0..69){
                val someVal = screenMat.get(i, j)
                inputs[inp] = someVal[0].toFloat() / 255.0f
                inp += 1
            }
        }
        val outputs = Array(1, { FloatArray(26) })
        tfLiteModel?.run(inputs, outputs)

        val str3MaxVal = Array<String?>(3) { "" }
        for (j in str3MaxVal.indices){
            var max_val: Float = -2.0f
            var pos = 0
            for (i in outputs[0].indices){
                val fl_tmp = outputs[0][i]
                if (max_val < fl_tmp ){
                    max_val = fl_tmp
                    pos = i
                }
            }
            max_val *=  100
            val intMaxVal = max_val.toInt()
            str3MaxVal[j] += abcCipher[pos].toString()
            str3MaxVal[j] += ": "
            str3MaxVal[j] += intMaxVal.toString()
            str3MaxVal[j] += "%"
            outputs[0][pos] = -2.0f
        }
        answerTextView.text = str3MaxVal[0] + " " + str3MaxVal[1] + " " + str3MaxVal[2]
//        val tmpAdapter = ArrayAdapter(this,
//            android.R.layout.simple_list_item_1,str3MaxVal)
//        convertEndVal.adapter = tmpAdapter
    }

}