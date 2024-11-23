package com.example.termproject

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// xml 파일 형식을 data class로 구현
data class WEATHER (val response : RESPONSE)
data class RESPONSE(val header : HEADER, val body : BODY)
data class HEADER(val resultCode : Int, val resultMsg : String)
data class BODY(val dataType : String, val items : ITEMS)
data class ITEMS(val item : List<ITEM>)
// fcstDate : 예측 날짜, fcstTime : 예측 시간, fcstValue : 예보 값
data class ITEM(val category : String, val fcstDate : String, val fcstTime : String, val fcstValue : String)

private val okHttpClient: OkHttpClient by lazy { // api 작동 여부 및 값을 콘솔에 로그를 작성하는 코드
    val httpLoggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)
    OkHttpClient.Builder()
        .addInterceptor(httpLoggingInterceptor)
        .build()
}

// retrofit을 사용하기 위한 빌더 생성
private val retrofit = Retrofit.Builder()
    .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttpClient) // api가 작동하는 지 로그를 작성하는 코드
    .build()

object ApiObject {
    val retrofitService: WeatherInterface by lazy {
        retrofit.create(WeatherInterface::class.java)
    }
}
/*
* 날씨 프래그먼트 화면을 구성하는 코드가 작성되어 있습니다.
* 기상청 공공 API를 활용합니다.
*/
class WeatherFragment : Fragment() {

    lateinit var tvRainRatio : TextView     // 강수 확률
    lateinit var tvRainType : TextView      // 강수 형태
    lateinit var tvHumidity : TextView      // 습도
    lateinit var tvSky : TextView           // 하늘 상태
    lateinit var tvTemp : TextView          // 온도
    lateinit var btnRefresh : Button        // 새로고침 버튼
    lateinit var tvWeatherImage: ImageView  // 날씨 이미지
    lateinit var tvDate: TextView           // 오늘 날짜

    var base_date = ""  // 발표 일자
    var base_time = ""      // 발표 시각
    var nx = "98"               // 예보지점 X 좌표
    var ny = "77"              // 예보지점 Y 좌표

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        val view = inflater.inflate(R.layout.weather_frag, container, false)

        tvWeatherImage = view.findViewById(R.id.WeatherImage)
        tvDate = view.findViewById(R.id.tvDate)

        tvRainRatio = view.findViewById(R.id.tvRainRatio)
        tvRainType = view.findViewById(R.id.tvRainType)
        tvHumidity = view.findViewById(R.id.tvHumidity)
        tvSky = view.findViewById(R.id.tvSky)
        tvTemp = view.findViewById(R.id.tvTemp)
        btnRefresh = view.findViewById(R.id.btnRefresh)

        // nx, ny지점의 날씨 가져와서 설정하기
        setWeather(nx, ny)
        // <새로고침> 버튼 누를 때 날씨 정보 다시 가져오기
        btnRefresh.setOnClickListener {
            setWeather(nx, ny)
        }

        // 오늘 날짜 설정
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (EEE)", Locale.KOREA)
        val todayDate = dateFormat.format(calendar.time)
        tvDate.text = todayDate

        return view
    }

    // 날씨 데이터를 API를 이용해 가져와 프래그먼트에 설정하는 코드입니다.
    fun setWeather(nx : String, ny : String) {

        // 현재 날짜, 시간 정보를 작성합니다.
        val cal = Calendar.getInstance()
        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 현재 날짜
        val sdf = SimpleDateFormat("HH")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val time = sdf.format(cal.time) // 현재 시간
        // API를 호출하기 위해 날짜를 가공합니다.
        base_time = getTime(time)
        Log.v("base_date", base_date)
        Log.v("time", time)
        Log.v("base_time", base_time)
        // 동네예보  API는 3시간마다 현재시간+1시간 뒤의 날씨 예보를 알려주기에 해당 형식에 맞춰 날짜를 가공합니다.
        // 현재 시각이 00시가 넘었다면 작일 23시에 예보한 데이터를 가져와야 합니다.
        if (base_time >= "2000") {
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        // 날씨 정보 다운로드
        // (응답 자료 형식은 JSON이며, 한 페이지 결과 수 = 13, 페이지 번호 = 1, 발표 날싸, 발표 시각, 예보지점 좌표)
        val call = ApiObject.retrofitService.GetWeather("JSON", 14, 1, base_date, base_time, nx, ny)

        // 비동기적으로 실행합니다.
        call.enqueue(object : retrofit2.Callback<WEATHER> {
            // 응답 성공 시
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if (response.isSuccessful) {
                    // 날씨 정보 가져옵니다.
                    var it: List<ITEM> = response.body()!!.response.body.items.item

                    var rainRatio = ""      // 강수 확률
                    var rainType = ""       // 강수 형태
                    var humidity = ""       // 습도
                    var sky = ""            // 하늘 상태
                    var temp = ""           // 기온
                    for (i in 0..13) {
                        when(it[i].category) {
                            "POP" -> rainRatio = it[i].fcstValue    // 강수 기온
                            "PTY" -> rainType = it[i].fcstValue     // 강수 형태 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
                            "REH" -> humidity = it[i].fcstValue     // 습도
                            "SKY" -> sky = it[i].fcstValue          // 하늘 상태 맑음(1), 구름 많음(3), 흐림(4)
                            "TMP" -> temp = it[i].fcstValue         // 기온
                            else -> continue
                        }
                        Log.v("fcstTime", it[i].fcstTime)

                    }
                    // 날씨 이미지 설정
                    setWeatherImage(sky, rainType)

                    // 날씨 정보 텍스트뷰에 보이게 하기
                    setWeather(rainRatio, rainType, humidity, sky, temp)
                    setFragmentResult("requestKey", bundleOf("bundletemp" to temp,
                        "bundlesky" to sky, "bundleRain" to rainType))

                    // 토스트 띄우기
                    Toast.makeText(context, it[0].fcstDate + ", " + it[0].fcstTime + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            // 응답 실패 시
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                Toast.makeText(context, "기상청으로부터 날씨 정보 수신을 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 텍스트 뷰에 날씨 정보 보여주기
    fun setWeather(rainRatio : String, rainType : String, humidity : String, sky : String, temp : String) {
        // 강수 확률
        tvRainRatio.text = rainRatio + "%"
        // 강수 형태
        var result = ""
        when(rainType) {
            "0" -> result = "없음"
            "1" -> result = "비"
            "2" -> result = "비/눈"
            "3" -> result = "눈"
            "4" -> result = "소나기"
            "5" -> result = "빗방울"
            "6" -> result = "빗방울/눈날림"
            "7" -> result = "눈날림"
            else -> "오류"
        }
        tvRainType.text = result
        // 습도
        tvHumidity.text = humidity + "%"
        // 하능 상태
        result = ""
        when(sky) {
            "1" -> result = "맑음"
            "3" -> result = "구름 많음"
            "4" -> result = "흐림"
            else -> "오류"
        }
        tvSky.text = result
        // 온도
        tvTemp.text = temp + "°"
    }

    fun setWeatherImage(sky: String, rainType: String) {
        val drawableId = when (sky) {
            "3" -> R.drawable.partly_cloudy
            "4" -> R.drawable.cloudy
            else -> R.drawable.sunny // 기본 이미지
        }

        // 강수 형태에 따라 추가 이미지 설정
        val rainDrawableId = when (rainType) {
            "1", "4", "5" -> R.drawable.rainy
            "2", "6" -> R.drawable.snow_rain
            "3", "7" -> R.drawable.snow
            else -> null // 강수 형태가 없는 경우
        }

        // 이미지 뷰에 설정
        rainDrawableId?.let {
            tvWeatherImage.setImageResource(it)
        } ?: run {
            tvWeatherImage.setImageResource(drawableId)
        }
    }


    // 시간 설정하기
    // 동네 예보 API는 3시간마다 현재시각+4시간 뒤의 날씨 예보를 보여줌
    // 따라서 현재 시간대의 날씨를 알기 위해서는 아래와 같은 과정이 필요함. 자세한 내용은 함께 제공된 파일 확인
    fun getTime(time : String) : String {
        var result = ""
        when(time) {
            in "00".."02" -> result = "2300"    // 00~02
            in "03".."05" -> result = "0200"    // 03~05
            in "06".."08" -> result = "0500"    // 06~08
            in "09".."11" -> result = "0800"    // 09~11
            in "12".."14" -> result = "1100"    // 12~14
            in "15".."17" -> result = "1400"    // 15~17
            in "18".."20" -> result = "1700"    // 18~20
            else -> result = "2000"             // 21~23
        }
        return result
    }

}