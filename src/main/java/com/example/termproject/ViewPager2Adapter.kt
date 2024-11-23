package com.example.termproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
/*
* 프래그먼트 페이지를 만드는 코틀린 파일입니다.
*/

class ViewPager2Adapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    // fragments라는 ArrayList<Fragment> 변수를 선언합니다.
    // 이 변수는 어댑터가 관리할 프래그먼트들을 저장합니다.
    var fragments: ArrayList<Fragment> = ArrayList()

    // getItemCount 메서드는 어댑터가 관리하는 프래그먼트의 개수를 반환합니다.
    override fun getItemCount(): Int {
        return fragments.size // fragments 리스트의 크기를 반환합니다.
    }

    // createFragment 메서드로 프래그먼트를 생성합니다.
    override fun createFragment(position: Int): Fragment {
        return fragments[position] // 주어진 위치에 있는 프래그먼트를 반환합니다.
    }

    // addFragment 메서드는 새로운 프래그먼트를 fragments 리스트에 추가합니다.
    fun addFragment(fragment: Fragment) {
        // 새로운 프래그먼트를 리스트에 추가합니다.
        fragments.add(fragment)
        // 아이템이 삽입되었음을 어댑터에 알립니다.
        notifyItemInserted(fragments.size - 1)
    }

}