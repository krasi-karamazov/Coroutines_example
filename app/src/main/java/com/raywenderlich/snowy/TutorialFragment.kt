/*
 * Copyright (c) 2019 Razeware LLC
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 *  distribute, sublicense, create a derivative work, and/or sell copies of the
 *  Software in any work that is designed, intended, or marketed for pedagogical or
 *  instructional purposes related to programming, coding, application development,
 *  or information technology.  Permission for such use, copying, modification,
 *  merger, publication, distribution, sublicensing, creation of derivative works,
 *  or sale is expressly withheld.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package com.raywenderlich.snowy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.raywenderlich.snowy.model.Tutorial
import com.raywenderlich.snowy.utils.SnowFilter
import kotlinx.coroutines.*
import java.net.URL

class TutorialFragment : Fragment() {

  companion object {

    const val TUTORIAL_KEY = "TUTORIAL"
    private val parentJob: Job = Job()
    private val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler {_, throwable ->

        tutorialFragmentScope.launch (Dispatchers.Main) {
          Log.d("Error", "Error")
        }

      GlobalScope.launch { println("Cought $throwable") }
    }
    val tutorialFragmentScope: CoroutineScope = CoroutineScope(Dispatchers.Main + parentJob + coroutineExceptionHandler)

    fun newInstance(tutorial: Tutorial): TutorialFragment {
      val fragmentHome = TutorialFragment()
      val args = Bundle()
      args.putParcelable(TUTORIAL_KEY, tutorial)
      fragmentHome.arguments = args
      return fragmentHome
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    val tutorial = arguments?.getParcelable(TUTORIAL_KEY) as Tutorial
    val view = inflater.inflate(R.layout.fragment_tutorial, container, false)
    view.findViewById<TextView>(R.id.tutorialName).text = tutorial.name
    view.findViewById<TextView>(R.id.tutorialDesc).text = tutorial.description
    tutorialFragmentScope.launch(Dispatchers.Main) {
      val downloadedBitmap: Bitmap = getOriginalBitmapAsync(tutorial)
      val snowyBitmap: Bitmap = loadSnowFilterAsync(downloadedBitmap)
      loadImage(snowyBitmap)
    }

    return view
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val tutorial = arguments?.getParcelable(TUTORIAL_KEY) as Tutorial
  }

  private suspend fun getOriginalBitmapAsync(tutorial: Tutorial): Bitmap = withContext(Dispatchers.IO){
      URL(tutorial.url).openStream().use {
        return@withContext BitmapFactory.decodeStream(it)
      }
    }

  private suspend fun loadSnowFilterAsync (originalBitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
      SnowFilter.applySnowEffect(originalBitmap)
    }

  private fun loadImage(snowFilterBitmap: Bitmap){
    view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
    view?.findViewById<ImageView>(R.id.snowFilterImage)?.setImageBitmap(snowFilterBitmap)
  }

  override fun onDestroy() {
    super.onDestroy()
    parentJob.cancel()
  }
}
