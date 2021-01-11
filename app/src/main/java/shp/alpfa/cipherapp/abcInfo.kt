package shp.alpfa.cipherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.AlertDialog
import android.graphics.BitmapFactory
import kotlinx.android.synthetic.main.activity_abc_info.*

class abcInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abc_info)
        supportActionBar?.hide()
        choiseButton.text = "Выбор буквы"
        choiseButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val items = arrayOf("A", "Б", "В", "Г", "Д", "E", "Ж",
                "З", "И", "Й", "К", "Л", "М", "Н", "О", "П",
                "Р", "С", "Т", "У", "X", "Ч", "Ш", "Ы", "Ь", "Я")
            builder.setItems(items) { dialog, which ->
                when(which){
                    0 -> openPngFromPath("A.png")
                    1 -> openPngFromPath("B.png")
                    2 -> openPngFromPath("C.png")
                    3 -> openPngFromPath("G.png")

                    4 -> openPngFromPath("D.png")
                    5 -> openPngFromPath("E.png")
                    6 -> openPngFromPath("J.png")
                    7 -> openPngFromPath("ZZZ.png")

                    8 -> openPngFromPath("I.png")
                    9 -> openPngFromPath("I_I.png")
                    10 -> openPngFromPath("K.png")
                    11 -> openPngFromPath("L.png")

                    12 -> openPngFromPath("M.png")
                    13 -> openPngFromPath("AH.png")
                    14 -> openPngFromPath("O.png")
                    15 -> openPngFromPath("P.png")

                    16 -> openPngFromPath("R.png")
                    17 -> openPngFromPath("S.png")
                    18 -> openPngFromPath("T.png")
                    19 -> openPngFromPath("U.png")

                    20 -> openPngFromPath("X.png")
                    21 -> openPngFromPath("CH.png")
                    22 -> openPngFromPath("SH.png")
                    23 -> openPngFromPath("UAAA.png")

                    24 -> openPngFromPath("LZ.png")
                    25 -> openPngFromPath("YA.png")
                }
            }
            builder.show()
        }
    }
    private fun openPngFromPath(path: String){
        val assMan = assets
        val istream = assMan.open(path)
        val setBit = BitmapFactory.decodeStream(istream)
        sumbolImgView.setImageBitmap(setBit)
    }
}
