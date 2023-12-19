package com.example.client
//
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//    }
//}
//import org.json.JSONObject
//import android.os.Bundle
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.example.client.R
//import com.squareup.picasso.Picasso
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.IOException
//import java.net.HttpURLConnection
//import java.net.URL
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var imageView: ImageView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        imageView = findViewById(R.id.imageView)
//        val refreshButton: Button = findViewById(R.id.button)
//
//        refreshButton.setOnClickListener {
//            fetchData()
//        }
//    }
//
//    private fun fetchData() {
//        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val result = downloadData("http://10.0.2.2:8000/list/") // HTTPS로 변경
//                withContext(Dispatchers.Main) {
//                    processJsonResult(result)
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@MainActivity, "데이터를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
import org.json.JSONObject
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        val refreshButton: Button = findViewById(R.id.button)

        refreshButton.setOnClickListener {
            fetchData()
        }

        // 주기적으로 데이터 업데이트
        startDataUpdate()
    }

    private fun startDataUpdate() {
        // 초기 데이터 로드
        fetchData()

        // 일정 주기로 데이터 업데이트
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchData()
                handler.postDelayed(this, 1000) // 10초마다 업데이트 (10000 밀리초)
            }
        }, 1000)
    }

    private fun fetchData() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = downloadData("http://10.0.2.2:8000/list/")
                withContext(Dispatchers.Main) {
                    processJsonResult(result)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "데이터를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private suspend fun downloadData(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection // HttpsURLConnection으로 변경

        return try {
            if (connection.responseCode == HttpURLConnection.HTTP_OK) { // HTTP_OK으로 변경
                val inputStream = connection.inputStream
                inputStream.bufferedReader().use { it.readText() }
            } else {
                throw IOException("HTTP error code: ${connection.responseCode}")
            }
        } catch (e: IOException) {
            // IOException이 발생하면 예외 정보를 출력합니다.
            e.printStackTrace()

            // 서버 응답 코드와 응답 내용을 추가로 출력합니다.
            val errorStream = connection.errorStream
            val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
            println("Server Response Code: ${connection.responseCode}")
            println("Server Response Message: $errorResponse")

            throw e
        } finally {
            connection.disconnect()
        }
    }

    private fun processJsonResult(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val postsArray = jsonObject.getJSONArray("posts")

            // 마지막 객체의 데이터를 가져오기
            val lastPost = postsArray.getJSONObject(postsArray.length() - 1)

            // 필요한 데이터 추출
            val title = lastPost.getString("title")
            val createdDate = lastPost.getString("created_date")
            val imageUrl = lastPost.getString("image")

            // UI 업데이트
            updateUI(title, createdDate, imageUrl)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateUI(title: String, createdDate: String, imageUrl: String) {
        val baseUrl = "http://10.0.2.2:8000"  // 서버의 베이스 URL로 변경

        // imageUrl에 베이스 URL을 추가하여 전체 URL 생성
        val fullImageUrl = baseUrl + imageUrl

        // Picasso를 사용하여 이미지 로드
        Picasso.get().load(fullImageUrl).into(imageView)

        // 여기에서 title, createdDate 등을 사용하여 필요한 UI 업데이트를 수행할 수 있습니다.
    }
}

