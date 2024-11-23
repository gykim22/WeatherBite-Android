package com.example.termproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener

class RandomChoice : Fragment() {
    lateinit var Temp : TextView          // 온도
    lateinit var FoodRecommendation: TextView // 추천 음식
    lateinit var FoodImage: ImageView     // 음식 이미지
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.random_choice, container, false)
        setFragmentResultListener("requestKey") { requestKey, bundle ->
            //결과 값을 받는 곳입니다.
            val temp = bundle.getString("bundletemp")
            val sky = bundle.getString("bundlesky")
            val rain = bundle.getString("bundleRain")
            Temp = view.findViewById(R.id.tvTemp)
            Temp.text = temp.toString().replace("°", "")
            FoodRecommendation = view.findViewById(R.id.tvFoodRecommendation)
            FoodImage = view.findViewById(R.id.FoodImage)

            val re_choice = view.findViewById<Button>(R.id.re_button)

            if (temp != null && sky != null && rain !=null) {
                Temp.text = "$temp°C"
                val recommendedFood = recommendFood(temp.toInt(), rain.toInt())
                FoodRecommendation.text = "$recommendedFood"
                setFragmentResult("randomChoice", bundleOf("bundleFood" to FoodRecommendation.text))
                setFoodImage(recommendedFood)
            } else {
                Temp.text = "온도 정보 없음"
                FoodRecommendation.text = "정보 없음"
            }

            re_choice.setOnClickListener {
                if (temp != null && sky != null && rain !=null){
                    val recommendedFood = recommendFood(temp.toInt(), rain.toInt())
                    FoodRecommendation.text = "$recommendedFood"
                    setFragmentResult("randomChoice", bundleOf("bundleFood" to FoodRecommendation.text))
                    setFoodImage(recommendedFood)
                }else {
                    Temp.text = "온도 정보 없음"
                    FoodRecommendation.text = "정보 없음"
                }
            }
        }
        return view
    }

    private fun recommendFood(temperature: Int, rain: Int): String {
        /*
        temperature : 기온,
        rain : 강수 형태 없음(0), 비(1~4)
        음식을
        */

        val coldWeatherFoods = listOf("우동", "칼국수", "호떡", "설렁탕", "짬뽕", "샤브샤브", "국밥", "군고구마", "부대찌개", "라멘")
        val warmWeatherFoods = listOf("냉면", "초밥", "냉소바", "국수", "망고빙수", "스무디", "떡볶이", "물회", "삼계탕")
        val rainyWeatherFoods = listOf("쌀국수", "토스트", "파전", "해물파전", "김치전", "파스타", "부추전", "치킨", "칼국수", "짬뽕")
        val hotWeatherFoods = listOf("냉면", "초밥", "냉소바", "콩국수", "빙수", "스무디", "물회", "삼계탕", "배스킨라빈스 아이스크림")

        if(rain == 0){
            if(temperature <= 14)
                coldWeatherFoods.random()
            else if(temperature in 15..24)
                return warmWeatherFoods.random()
            else
                return hotWeatherFoods.random()
        }else{
            return rainyWeatherFoods.random()
        }
        return hotWeatherFoods.random()
    }

    private fun setFoodImage(food: String) {
        val drawableId = when (food) {
            "우동", "칼국수", "국수", "짬뽕", "라멘", "냉면", "냉소바", "쌀국수", "콩국수" -> R.drawable.noodle
            "호떡" -> R.drawable.hotteok
            "설렁탕", "국밥", "부대찌개" -> R.drawable.gukbap
            "샤브샤브" -> R.drawable.shabu
            "군고구마" -> R.drawable.roasted_sweet_potato
            "초밥" -> R.drawable.sushi
            "망고빙수", "빙수", "아이스크림" -> R.drawable.ice_cream
            "스무디" -> R.drawable.smoothie
            "떡볶이" -> R.drawable.tteokbokki
            "월남쌈" -> R.drawable.vietnamese_ginseng
            "삼계탕" -> R.drawable.samgyetang
            "토스트" -> R.drawable.toast
            "파전", "해물파전", "김치전", "부추전" -> R.drawable.jeon
            "파스타" -> R.drawable.pasta
            "치킨" -> R.drawable.chicken
            else -> R.drawable.default_img
        }
        FoodImage.setImageResource(drawableId)
    }


}