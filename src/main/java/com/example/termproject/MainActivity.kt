package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
/*
* 어플리케이션의 메인 화면.
* intent를 이용해 기능 화면으로 넘어가게 됩니다.
*/
class MainActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameEditText = findViewById<EditText>(R.id.ev_main) // 이름을 입력하는 editText입니다.
        val btn = findViewById<Button>(R.id.btn_main) //intent를 보내기 위한 버튼입니다. 이름을 입력하지 않을 시 보내지지 않습니다.
        btn.setOnClickListener {
            if (TextUtils.isEmpty(nameEditText.getText().toString())) { // editText가 비어있다면 보내지지 않도록 합니다.
                Toast.makeText(this, "이름... 입력하셨나요?", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val name: String = nameEditText.text.toString()
            val intent = Intent(this, WeatherActivity::class.java) // 인텐트에 이름을 실어 보냅니다.
            intent.putExtra("name", name)
            startActivity(intent)
        }
    }
}