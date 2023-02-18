package nostr.postr.ui.feed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import nostr.postr.R

class FeedDetailActivity : AppCompatActivity() {

    private val viewModel by viewModels<FeedDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_detail)

        val id = intent.getStringExtra("feedId")
        viewModel.reqFeedInfo()
    }
}