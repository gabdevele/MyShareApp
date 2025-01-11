package dev.gabdevele.myshare.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.tbuonomo.viewpagerdotsindicator.compose.DotsIndicator
import com.tbuonomo.viewpagerdotsindicator.compose.model.DotGraphic
import com.tbuonomo.viewpagerdotsindicator.compose.type.ShiftIndicatorType
import dev.gabdevele.myshare.MainActivity
import dev.gabdevele.myshare.TitleImage
import dev.gabdevele.myshare.ui.setup.pages.AccountsPage
import dev.gabdevele.myshare.ui.setup.pages.PersonalDataPage
import dev.gabdevele.myshare.ui.setup.viewmodel.SetupView
import dev.gabdevele.myshare.ui.theme.MyShareTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupActivity : ComponentActivity() {

    private val viewModel: SetupView by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyShareTheme {
                val pageCount = 2
                val pagerState = rememberPagerState(pageCount = { pageCount })
                val coroutineScope = rememberCoroutineScope()

                val callback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (pagerState.currentPage > 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        } else {
                            finish()
                        }
                    }
                }
                onBackPressedDispatcher.addCallback(this@SetupActivity, callback)
                SetupScreen(pagerState, pageCount, coroutineScope, viewModel)
            }
        }

        lifecycleScope.launch {
            viewModel.setupFinished.collectLatest { isFinished ->
                if (isFinished) {
                    startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    @Composable
    fun SetupScreen(pagerState: PagerState, pageCount: Int, coroutineScope: CoroutineScope, setupView: SetupView) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(top = 26.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TitleImage()
                        TutorialText()
                        HorizontalPager(state = pagerState) { page ->
                            when (page) {
                                0 -> PersonalDataPage()
                                1 -> AccountsPage()
                            }
                        }
                    }

                    BottomBar(pagerState, pageCount, coroutineScope, setupView)
                }
            }
        }
    }
    @Composable
    fun TutorialText() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to MyShare!", textAlign = TextAlign.Center)
        }
    }

    @Composable
    fun BottomBar(pagerState: PagerState, pageCount: Int, coroutineScope: CoroutineScope, view: SetupView) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ){
            DotsIndicator(
                dotCount = pageCount,
                type = ShiftIndicatorType(dotsGraphic = DotGraphic(color = MaterialTheme.colorScheme.primary)),
                pagerState = pagerState
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < pageCount - 1) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            view.onFinishButtonClicked()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (pagerState.currentPage == pageCount - 1) "Finish" else "Next")
            }
        }
    }
}
