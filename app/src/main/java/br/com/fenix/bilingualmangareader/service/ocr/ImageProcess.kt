package br.com.fenix.bilingualmangareader.service.ocr

import android.graphics.*

class ImageProcess {

    companion object {
        fun processGrayscale(image: Bitmap): Bitmap {
            val grayScale = toGrayscale(image)
            return grayscaleToBin(grayScale)
        }

        fun toGrayscale(image: Bitmap): Bitmap {
            val grayScale = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)

            val cmGray = ColorMatrix()
            cmGray.setSaturation(0F)
            val pGray = Paint()
            pGray.colorFilter = ColorMatrixColorFilter(cmGray)

            Canvas(grayScale).drawBitmap(image, 0F, 0F, pGray)

            return grayScale
        }

        fun otsuThreshold(image: Bitmap): Int {
            val histogram = IntArray(256)
            for (i in histogram.indices) histogram[i] = 0

            for (i in 0 until image.width) {
                for (j in 0 until image.height) {
                    histogram[image.getPixel(i, j) and 0xFF0000 shr 16]++
                }
            }

            val total: Int = image.height * image.width

            var sum = 0f
            for (i in 0..255) sum += (i * histogram[i]).toFloat()

            var sumB = 0f
            var wB = 0
            var wF = 0

            var varMax = 0f
            var threshold = 0

            for (i in 0..255) {
                wB += histogram[i]
                if (wB == 0) continue
                wF = total - wB
                if (wF == 0) break
                sumB += (i * histogram[i]).toFloat()
                val mB = sumB / wB
                val mF = (sum - sumB) / wF
                val varBetween = wB.toFloat() * wF.toFloat() * (mB - mF) * (mB - mF)
                if (varBetween > varMax) {
                    varMax = varBetween
                    threshold = i
                }
            }

            return threshold
        }

        fun grayscaleToBin(image: Bitmap): Bitmap {
            val threshold = otsuThreshold(image)
            val cm = ColorMatrix(
                floatArrayOf(
                    85f, 85f, 85f, 0f, -255f * threshold,
                    85f, 85f, 85f, 0f, -255f * threshold,
                    85f, 85f, 85f, 0f, -255f * threshold,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            val p = Paint()
            p.colorFilter = ColorMatrixColorFilter(cm)

            Canvas(image).drawBitmap(image, 0f, 0f, p)

            return image
        }
    }
}