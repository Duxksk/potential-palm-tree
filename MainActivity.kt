package com.example.foxbrowser

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import org.mozilla.geckoview.*

class MainActivity : AppCompatActivity() {

    private lateinit var geckoView: GeckoView
    private lateinit var runtime: GeckoRuntime

    // UI
    private lateinit var urlBar: EditText
    private lateinit var tabContainer: LinearLayout
    private lateinit var desktopToggle: ToggleButton
    private lateinit var darkToggle: ToggleButton
    private lateinit var fullscreenToggle: ToggleButton

    // 탭 시스템 자료구조
    private val sessions = mutableListOf<GeckoSession>()
    private var currentTab = 0
    private val MAX_TABS = 500

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        geckoView = findViewById(R.id.geckoView)
        urlBar = findViewById(R.id.urlBar)
        tabContainer = findViewById(R.id.tabContainer)

        desktopToggle = findViewById(R.id.desktopBtn)
        darkToggle = findViewById(R.id.darkModeBtn)
        fullscreenToggle = findViewById(R.id.fullscreenBtn)

        val goBtn = findViewById<Button>(R.id.goBtn)
        val backBtn = findViewById<Button>(R.id.backBtn)
        val forwardBtn = findViewById<Button>(R.id.forwardBtn)
        val reloadBtn = findViewById<Button>(R.id.reloadBtn)
        val homeBtn = findViewById<Button>(R.id.homeBtn)
        val newTabBtn = findViewById<Button>(R.id.newTabBtn)
        val closeTabBtn = findViewById<Button>(R.id.closeTabBtn)

        // Runtime
        runtime = GeckoRuntime.create(this)

        // 첫 탭 생성
        addNewTab()

        // 검색 버튼
        goBtn.setOnClickListener { handleSearch() }
        urlBar.setOnEditorActionListener { _, _, _ ->
            handleSearch()
            true
        }

        // 기본 네비
        homeBtn.setOnClickListener { loadUrl("https://www.naver.com") }
        backBtn.setOnClickListener { currentSession().goBack() }
        forwardBtn.setOnClickListener { currentSession().goForward() }
        reloadBtn.setOnClickListener { currentSession().reload() }

        // 새 탭
        newTabBtn.setOnClickListener {
            addNewTab()
        }

        // 탭 닫기
        closeTabBtn.setOnClickListener {
            closeCurrentTab()
        }

        // 데스크탑 모드
        desktopToggle.setOnCheckedChangeListener { _, desktop ->
            val settings = currentSession().settings
            settings.userAgentMode =
                if (desktop) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                else GeckoSessionSettings.USER_AGENT_MODE_MOBILE

            currentSession().settings = settings
            currentSession().reload()
        }

        // 다크모드
        darkToggle.setOnCheckedChangeListener { _, dark ->
            if (dark)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // 전체화면
        fullscreenToggle.setOnCheckedChangeListener { _, full ->
            if (full) enterFullscreen()
            else exitFullscreen()
        }
    }

    // ==========================
    //    ★ 탭 시스템 구현
    // ==========================

    private fun addNewTab() {
        if (sessions.size >= MAX_TABS) {
            Toast.makeText(this, "탭은 최대 500개까지 가능합니다!", Toast.LENGTH_SHORT).show()
            return
        }

        val session = GeckoSession()
        session.open(runtime)

        // 광고차단 (간단)
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onLoadRequest(
                session: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest
            ): GeckoResult<GeckoSession.NavigationDelegate.LoadResponse>? {

                val url = request.uri
                val ads = listOf("doubleclick", "adservice", "googlesyndication", "/ads")

                if (ads.any { url.contains(it) }) {
                    return GeckoResult.fromValue(GeckoSession.NavigationDelegate.LoadResponse.CANCEL)
                }
                return null
            }
        }

        // 히스토리 + URL 반영
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(session: GeckoSession, url: String?) {
                if (sessions[currentTab] == session) {
                    urlBar.setText(url)
                }
            }
        }

        sessions.add(session)
        switchToTab(sessions.size - 1)

        createTabButton(sessions.size - 1)

        loadUrl("https://www.naver.com")
    }

    private fun createTabButton(index: Int) {
        val btn = Button(this)
        btn.text = "탭 ${index + 1}"
        btn.textSize = 12f
        btn.setPadding(6, 6, 6, 6)
        btn.setOnClickListener {
            switchToTab(index)
        }
        tabContainer.addView(btn)
    }

    private fun switchToTab(index: Int) {
        if (index < 0 || index >= sessions.size) return

        currentTab = index
        geckoView.setSession(currentSession())
        urlBar.setText(currentSession().currentUri ?: "")
    }

    private fun closeCurrentTab() {
        if (sessions.size <= 1) {
            Toast.makeText(this, "마지막 탭은 닫을 수 없습니다!", Toast.LENGTH_SHORT).show()
            return
        }

        sessions[currentTab].close()
        sessions.removeAt(currentTab)
        tabContainer.removeViewAt(currentTab)

        val newIndex = if (currentTab > 0) currentTab - 1 else 0
        switchToTab(newIndex)

        refreshTabLabels()
    }

    private fun refreshTabLabels() {
        for (i in 0 until tabContainer.childCount) {
            (tabContainer.getChildAt(i) as Button).text = "탭 ${i + 1}"
        }
    }

    private fun currentSession(): GeckoSession {
        return sessions[currentTab]
    }

    // ==========================
    //   URL 처리
    // ==========================

    private fun loadUrl(url: String) {
        currentSession().loadUri(url)
    }

    private fun handleSearch() {
        val input = urlBar.text.toString()
        val finalUrl = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.contains(".") -> "https://$input"
            else -> "https://search.naver.com/search.naver?query=${Uri.encode(input)}"
        }
        loadUrl(finalUrl)
    }

    // ==========================
    //   전체화면
    // ==========================

    private fun enterFullscreen() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun exitFullscreen() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}
