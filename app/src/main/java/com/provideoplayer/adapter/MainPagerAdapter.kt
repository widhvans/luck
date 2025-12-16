package com.provideoplayer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.provideoplayer.fragment.AudioFragment
import com.provideoplayer.fragment.BrowseFragment
import com.provideoplayer.fragment.PlaylistFragment
import com.provideoplayer.fragment.VideosFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    // Store fragment references for later access
    private val fragments = mutableMapOf<Int, Fragment>()
    
    override fun getItemCount(): Int = 4  // Videos, Audio, Browse, Playlist (Network is dialog)
    
    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> VideosFragment()
            1 -> AudioFragment()
            2 -> BrowseFragment()
            3 -> PlaylistFragment()
            else -> VideosFragment()
        }
        fragments[position] = fragment
        return fragment
    }
    
    fun getFragment(position: Int): Fragment? = fragments[position]
    
    fun getVideosFragment(): VideosFragment? = fragments[0] as? VideosFragment
    fun getAudioFragment(): AudioFragment? = fragments[1] as? AudioFragment
    fun getBrowseFragment(): BrowseFragment? = fragments[2] as? BrowseFragment
    fun getPlaylistFragment(): PlaylistFragment? = fragments[3] as? PlaylistFragment
}
