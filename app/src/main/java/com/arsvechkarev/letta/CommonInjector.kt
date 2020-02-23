package com.arsvechkarev.letta

import androidx.fragment.app.Fragment
import com.arsvechkarev.imagesloader.GlideImagesLoader
import com.arsvechkarev.imagesloader.ImagesLoader

object CommonInjector {
  
  fun getImageLoader(fragment: Fragment): ImagesLoader {
    return GlideImagesLoader(fragment)
  }
  
}